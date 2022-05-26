/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.web.servlet.context;

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.ServletContextAwareProcessor;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.ServletContextScope;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A {@link WebApplicationContext} that can be used to bootstrap itself from a contained
 * {@link ServletWebServerFactory} bean.
 * <p>
 * This context will create, initialize and run an {@link WebServer} by searching for a
 * single {@link ServletWebServerFactory} bean within the {@link ApplicationContext}
 * itself. The {@link ServletWebServerFactory} is free to use standard Spring concepts
 * (such as dependency injection, lifecycle callbacks and property placeholder variables).
 * <p>
 * In addition, any {@link Servlet} or {@link Filter} beans defined in the context will be
 * automatically registered with the web server. In the case of a single Servlet bean, the
 * '/' mapping will be used. If multiple Servlet beans are found then the lowercase bean
 * name will be used as a mapping prefix. Any Servlet named 'dispatcherServlet' will
 * always be mapped to '/'. Filter beans will be mapped to all URLs ('/*').
 * <p>
 * For more advanced configuration, the context can instead define beans that implement
 * the {@link ServletContextInitializer} interface (most often
 * {@link ServletRegistrationBean}s and/or {@link FilterRegistrationBean}s). To prevent
 * double registration, the use of {@link ServletContextInitializer} beans will disable
 * automatic Servlet and Filter bean registration.
 * <p>
 * Although this context can be used directly, most developers should consider using the
 * {@link AnnotationConfigServletWebServerApplicationContext} or
 * {@link XmlServletWebServerApplicationContext} variants.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Scott Frederick
 * @since 2.0.0
 * @see AnnotationConfigServletWebServerApplicationContext
 * @see XmlServletWebServerApplicationContext
 * @see ServletWebServerFactory
 */
public class ServletWebServerApplicationContext extends GenericWebApplicationContext
		implements ConfigurableWebServerApplicationContext {

	private static final Log logger = LogFactory.getLog(ServletWebServerApplicationContext.class);

	/**
	 * Constant value for the DispatcherServlet bean name. A Servlet bean with this name
	 * is deemed to be the "main" servlet and is automatically given a mapping of "/" by
	 * default. To change the default behavior you can use a
	 * {@link ServletRegistrationBean} or a different bean name.
	 */
	public static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";

	private volatile WebServer webServer;

	private ServletConfig servletConfig;

	private String serverNamespace;

	/**
	 * Create a new {@link ServletWebServerApplicationContext}.
	 */
	public ServletWebServerApplicationContext() {
	}

	/**
	 * Create a new {@link ServletWebServerApplicationContext} with the given
	 * {@code DefaultListableBeanFactory}.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 */
	public ServletWebServerApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	/**
	 * Register ServletContextAwareProcessor.
	 * @see ServletContextAwareProcessor
	 */
	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		beanFactory.addBeanPostProcessor(new WebApplicationContextServletContextAwareProcessor(this));
		beanFactory.ignoreDependencyInterface(ServletContextAware.class);
		registerWebApplicationScopes();
	}

	@Override
	public final void refresh() throws BeansException, IllegalStateException {
		try {
			super.refresh();
		}
		catch (RuntimeException ex) {
			stopAndReleaseWebServer();
			throw ex;
		}
	}

	@Override
	protected void onRefresh() {
		// 超类 AbstractApplicationContext#onRefresh() 提供的扩展方法
		// 如下
		//                this.postProcessBeanFactory(beanFactory);
		//                this.invokeBeanFactoryPostProcessors(beanFactory);
		//                this.registerBeanPostProcessors(beanFactory);
		//                this.initMessageSource();
		//                this.initApplicationEventMulticaster();
		//                this.onRefresh();		我在这里哦,留给子类扩展的方法
		//                this.registerListeners();
		//                this.finishBeanFactoryInitialization(beanFactory);
		//                this.finishRefresh();
		super.onRefresh();
		try {
			createWebServer();
		}
		catch (Throwable ex) {
			throw new ApplicationContextException("Unable to start web server", ex);
		}
	}

	@Override
	protected void finishRefresh() {
		super.finishRefresh();
		WebServer webServer = startWebServer();
		if (webServer != null) {
			publishEvent(new ServletWebServerInitializedEvent(webServer, this));
		}
	}

	@Override
	protected void onClose() {
		super.onClose();
		stopAndReleaseWebServer();
	}

	private void createWebServer() {
		// 创建Tomcat服务器

		// 1. 表示完全配置的 Web 服务器（例如 Tomcat、Jetty、Netty）的简单界面。允许started和stopped服务器。
		WebServer webServer = this.webServer;
		// 2. 获取ServletContext
		ServletContext servletContext = getServletContext();
		// 3. 在SpringBoot中,如果是内嵌的tomcat,那么servletContext是null值
		if (webServer == null && servletContext == null) {
			// 3.1 一般情况,获取的都是 TomcatServletWebServerFactory
			ServletWebServerFactory factory = getWebServerFactory();
			// 3.2 然后 TomcatServletWebServerFactory 去获取 webServer
			// 但在这之前,需要先拿到 Spring自带的ServletContextInitializer初始化器
			// getSelfInitializer() 创建的 ServletContextInitializer 实际上是一个就是用来延迟调用
			// this::selfInitialize -> 这里面会真实处理Filter/Servlet/Listener三大组件注册到servlet中
			this.webServer = factory.getWebServer(getSelfInitializer());
		}
		else if (servletContext != null) {
			try {
				getSelfInitializer().onStartup(servletContext);
			}
			catch (ServletException ex) {
				throw new ApplicationContextException("Cannot initialize servlet context", ex);
			}
		}
		initPropertySources();
	}

	/**
	 * Returns the {@link ServletWebServerFactory} that should be used to create the
	 * embedded {@link WebServer}. By default this method searches for a suitable bean in
	 * the context itself.
	 * @return a {@link ServletWebServerFactory} (never {@code null})
	 */
	protected ServletWebServerFactory getWebServerFactory() {
		// Use bean names so that we don't consider the hierarchy
		// 1. 使用 bean 名称，以便我们不考虑层次结构
		String[] beanNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
		if (beanNames.length == 0) {
			throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to missing "
					+ "ServletWebServerFactory bean.");
		}
		if (beanNames.length > 1) {
			throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to multiple "
					+ "ServletWebServerFactory beans : " + StringUtils.arrayToCommaDelimitedString(beanNames));
		}
		// 2. 获取 IOC 容器中的 ServletWebServerFactory
		return getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
	}

	/**
	 * Returns the {@link ServletContextInitializer} that will be used to complete the
	 * setup of this {@link WebApplicationContext}.
	 * @return the self initializer
	 * @see #prepareWebApplicationContext(ServletContext)
	 */
	private org.springframework.boot.web.servlet.ServletContextInitializer getSelfInitializer() {
		// 返回将用于完成此WebApplicationContext设置的ServletContextInitializer
		// 注意哦 -> 这个函数的返回值是 ServletContextInitializer
		// 而这里返回的确实 return this::selfInitialize; 函数
		// 实际上返回值应该是一个lambda表达式,对应一个匿名的ServletContextInitializer对象
		// 即 return new ServletContextInitializer{
		// 			private ServletWebServerApplicationContext = [this]; // 这里就是我们的this
		//			// 所以当这个 TomcatStart 调用 initializer.onStartup() 方法实际对于这里的initializer
		//			// 执行的就是这段代码 -- selfInitialize
		// 			void onStartup(ServletContext servletContext) throws ServletException{
		//				servletWebServerApplicationContext.selfInitialize(servletContext);
		//			}
		// }

		return this::selfInitialize;
	}

	private void selfInitialize(ServletContext servletContext) throws ServletException {
		// 1. 准备 Web 应用程序上下文
		// 主要工作: 将this做为ServletContext的父容器存入属性中/调用setServletContext()设置到ApplicationContext上
		prepareWebApplicationContext(servletContext);
		// 2. 注册 Web 应用范围 ~ 可忽略
		registerApplicationScope(servletContext);
		// 3. 注册环境 Bean
		// 		1. 将 servletContext 注册到 BeanFactory 中
		// 		2. 将 servletContext 中的InitParameter做成HashMap也注册到BeanFactory中
		//		3. 将 servletContext 中的Attributes做成HashMap也注册到BeanFactory中
		WebApplicationContextUtils.registerEnvironmentBeans(getBeanFactory(), servletContext);
		// 4. 获取 Servlet 上下文初始化器 Bean -- 核心
		// 然后分别调取 onStartup方法
		for (ServletContextInitializer beans : getServletContextInitializerBeans()) {
			// 4.1 getServletContextInitializerBeans() 实际上就返回的是 ServletContextInitializerBeans
			// ServletContextInitializerBeans 本身就是有迭代器哦
			// 关键 -- ServletContextInitializerBeans 去发现SpringBoot环境中直接配置的 ServletRegistrationBean/FilterRegistrationBean 各种初始化Bean
			// 然后再去ioc容器发现相关的Filter/Listener/Servlet等组件,并用适配器包装起来,放入一个集合中
			// 然后到这里遍历到
			// 这些包装器有点特点 --
			// 比如 Filter 会通过构造器传入 FilterRegistrationBean -> 然后再 FilterRegistrationBean.onStartup() 中调用 servletContext.addFilter() 然后将Filter添加进去
			// Servlet 会通过构造器传入 ServletRegistrationBean -> 然后再 ServletRegistrationBean.onStartup() 中调用 servletContext.addServlet() 然后将Servlet添加进去
			beans.onStartup(servletContext);
		}
	}

	private void registerApplicationScope(ServletContext servletContext) {

		// 1. ServletContextScope 是 ServletContext 的Scope包装器，即用于全局 Web 应用程序属性。
		ServletContextScope appScope = new ServletContextScope(servletContext);
		// 2. 注册到BeanFactory中,角色就是 SCOPE_APPLICATION
		getBeanFactory().registerScope(WebApplicationContext.SCOPE_APPLICATION, appScope);
		// Register as ServletContext attribute, for ContextCleanupListener to detect it.
		// 3. 注册为 ServletContext 属性，供 ContextCleanupListener 检测。
		servletContext.setAttribute(ServletContextScope.class.getName(), appScope);
	}

	private void registerWebApplicationScopes() {
		ExistingWebApplicationScopes existingScopes = new ExistingWebApplicationScopes(getBeanFactory());
		WebApplicationContextUtils.registerWebApplicationScopes(getBeanFactory());
		existingScopes.restore();
	}

	/**
	 * Returns {@link ServletContextInitializer}s that should be used with the embedded
	 * web server. By default this method will first attempt to find
	 * {@link ServletContextInitializer}, {@link Servlet}, {@link Filter} and certain
	 * {@link EventListener} beans.
	 * @return the servlet initializer beans
	 */
	protected Collection<ServletContextInitializer> getServletContextInitializerBeans() {
		// 关键类 -- ServletContextInitializerBeans
		return new ServletContextInitializerBeans(getBeanFactory());
	}

	/**
	 * Prepare the {@link WebApplicationContext} with the given fully loaded
	 * {@link ServletContext}. This method is usually called from
	 * {@link ServletContextInitializer#onStartup(ServletContext)} and is similar to the
	 * functionality usually provided by a {@link ContextLoaderListener}.
	 * @param servletContext the operational servlet context
	 */
	protected void prepareWebApplicationContext(ServletContext servletContext) {
		// 使用给定的完全加载的ServletContext准备WebApplicationContext 。
		// 此方法通常从ServletContextInitializer.onStartup(ServletContext)调用，类似于通常由ContextLoaderListener提供的功能。

		// 1. 主要就是一个步骤:
		// servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this); 设置根容器
		// 注意:SpringBoot中不存在父子容器的关系啦,一般就只有一个容器即this,作为ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE属性存放进去,方便获取
		Object rootContext = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (rootContext != null) {
			if (rootContext == this) {
				throw new IllegalStateException(
						"Cannot initialize context because there is already a root application context present - "
								+ "check whether you have multiple ServletContextInitializers!");
			}
			return;
		}
		servletContext.log("Initializing Spring embedded WebApplicationContext");
		try {
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this);
			if (logger.isDebugEnabled()) {
				logger.debug("Published root WebApplicationContext as ServletContext attribute with name ["
						+ WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
			}
			// 2. 将 ServletContext 设置到 ApplicationContext 中
			setServletContext(servletContext);
			if (logger.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - getStartupDate();
				logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
			}
		}
		catch (RuntimeException | Error ex) {
			logger.error("Context initialization failed", ex);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
			throw ex;
		}
	}

	private WebServer startWebServer() {
		WebServer webServer = this.webServer;
		if (webServer != null) {
			webServer.start();
		}
		return webServer;
	}

	private void stopAndReleaseWebServer() {
		WebServer webServer = this.webServer;
		if (webServer != null) {
			try {
				webServer.stop();
				this.webServer = null;
			}
			catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
	}

	@Override
	protected Resource getResourceByPath(String path) {
		if (getServletContext() == null) {
			return new ClassPathContextResource(path, getClassLoader());
		}
		return new ServletContextResource(getServletContext(), path);
	}

	@Override
	public String getServerNamespace() {
		return this.serverNamespace;
	}

	@Override
	public void setServerNamespace(String serverNamespace) {
		this.serverNamespace = serverNamespace;
	}

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	@Override
	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	/**
	 * Returns the {@link WebServer} that was created by the context or {@code null} if
	 * the server has not yet been created.
	 * @return the embedded web server
	 */
	@Override
	public WebServer getWebServer() {
		return this.webServer;
	}

	/**
	 * Utility class to store and restore any user defined scopes. This allow scopes to be
	 * registered in an ApplicationContextInitializer in the same way as they would in a
	 * classic non-embedded web application context.
	 */
	public static class ExistingWebApplicationScopes {

		private static final Set<String> SCOPES;

		static {
			Set<String> scopes = new LinkedHashSet<>();
			scopes.add(WebApplicationContext.SCOPE_REQUEST);
			scopes.add(WebApplicationContext.SCOPE_SESSION);
			SCOPES = Collections.unmodifiableSet(scopes);
		}

		private final ConfigurableListableBeanFactory beanFactory;

		private final Map<String, Scope> scopes = new HashMap<>();

		public ExistingWebApplicationScopes(ConfigurableListableBeanFactory beanFactory) {
			this.beanFactory = beanFactory;
			for (String scopeName : SCOPES) {
				Scope scope = beanFactory.getRegisteredScope(scopeName);
				if (scope != null) {
					this.scopes.put(scopeName, scope);
				}
			}
		}

		public void restore() {
			this.scopes.forEach((key, value) -> {
				if (logger.isInfoEnabled()) {
					logger.info("Restoring user defined scope " + key);
				}
				this.beanFactory.registerScope(key, value);
			});
		}

	}

}
