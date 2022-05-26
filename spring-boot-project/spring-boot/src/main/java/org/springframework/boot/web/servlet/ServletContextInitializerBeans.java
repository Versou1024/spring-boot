/*
 * Copyright 2012-2019 the original author or authors.
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

package org.springframework.boot.web.servlet;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * A collection {@link ServletContextInitializer}s obtained from a
 * {@link ListableBeanFactory}. Includes all {@link ServletContextInitializer} beans and
 * also adapts {@link Servlet}, {@link Filter} and certain {@link EventListener} beans.
 * <p>
 * Items are sorted so that adapted beans are top ({@link Servlet}, {@link Filter} then
 * {@link EventListener}) and direct {@link ServletContextInitializer} beans are at the
 * end. Further sorting is applied within these groups using the
 * {@link AnnotationAwareOrderComparator}.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Brian Clozel
 * @since 1.4.0
 */
public class ServletContextInitializerBeans extends AbstractCollection<ServletContextInitializer> {
	// 从 ListableBeanFactory 获得的集合ServletContextInitializer。
	// 包括已经注册的所有 ServletContextInitializer的bean，
	// 还能够将 Servlet\Filter\EventListener 适配为 RegistrationBean 来初始化。

	// ServletContextInitializerBeans = ServletContext InitializerBean s


	private static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";

	private static final Log logger = LogFactory.getLog(ServletContextInitializerBeans.class);

	/**
	 * Seen bean instances or bean names.
	 */
	private final Set<Object> seen = new HashSet<>();

	// Class<?> 只有四种 -- Filter/EventListener/Servlet 三大组件
	// value 就是多值的初始化器
	private final MultiValueMap<Class<?>, ServletContextInitializer> initializers;

	private final List<Class<? extends ServletContextInitializer>> initializerTypes;

	private List<ServletContextInitializer> sortedList;

	@SafeVarargs
	public ServletContextInitializerBeans(ListableBeanFactory beanFactory,
			Class<? extends ServletContextInitializer>... initializerTypes) {
		// 核心 -- 初始化操作
		// 常用构造函数 initializerTypes.length == 0 的

		this.initializers = new LinkedMultiValueMap<>();
		// 0. initializerTypes 目前一般都是只有 ServletContextInitializer 这种类型的
		this.initializerTypes = (initializerTypes.length != 0) ? Arrays.asList(initializerTypes)
				: Collections.singletonList(ServletContextInitializer.class);
		// 1. 在 BeanFactory 找到所有 ServletContextInitializer 的实现类并进行排序
		// 然后将其组件类型以及初始化器加入到 initializers 中
		addServletContextInitializerBeans(beanFactory);
		// 2. 在 BeanFactory 中找到所有的 Filter/Listener/Servlet 然后通过 FilterRegistrationBean/ServletRegistrationBean 适配为初始化器,统一管理
		// 统一加入 initializers 中
		addAdaptableBeans(beanFactory);
		// 3. 对所有的FilterList进行排序
		// 回到这里:又可以发现 -- 实际上 Filter 和 FilterRegistrationBean 最终都是用Order排序的
		// 所以 Filter 和 FilterRegistrationBean 没有优先级,只有通过@Order或Order接口完成优先级比较哦
		List<ServletContextInitializer> sortedInitializers = this.initializers.values().stream()
				.flatMap((value) -> value.stream().sorted(AnnotationAwareOrderComparator.INSTANCE))
				.collect(Collectors.toList());
		// 4. 做一个不可变
		this.sortedList = Collections.unmodifiableList(sortedInitializers);
		// 5. 做一个记录
		logMappings(this.initializers);
	}

	private void addServletContextInitializerBeans(ListableBeanFactory beanFactory) {
		// 1. 查找BeanFactory中所有org.springframework.boot.web.servlet.ServletContextInitializer的类型
		for (Class<? extends ServletContextInitializer> initializerType : this.initializerTypes) {
			for (Entry<String, ? extends ServletContextInitializer> initializerBean : getOrderedBeansOfType(beanFactory,
					initializerType)) {
				// 2. 将找到的有序的ServletContextInitializer的类型,依次添加到initializers中
				addServletContextInitializerBean(initializerBean.getKey(), initializerBean.getValue(), beanFactory);
			}
		}
	}

	private void addServletContextInitializerBean(String beanName, ServletContextInitializer initializer,
			ListableBeanFactory beanFactory) {
		// 检查 ServletRegistrationBean 的类型

		// 1. ServletRegistrationBean -> Servlet组件
		if (initializer instanceof ServletRegistrationBean) {
			Servlet source = ((ServletRegistrationBean<?>) initializer).getServlet();
			addServletContextInitializerBean(Servlet.class, beanName, initializer, beanFactory, source);
		}
		// 2. FilterRegistrationBean -> Filter组件
		else if (initializer instanceof FilterRegistrationBean) {
			Filter source = ((FilterRegistrationBean<?>) initializer).getFilter();
			addServletContextInitializerBean(Filter.class, beanName, initializer, beanFactory, source);
		}
		// 3. DelegatingFilterProxyRegistrationBean -> Filter组件
		else if (initializer instanceof DelegatingFilterProxyRegistrationBean) {
			String source = ((DelegatingFilterProxyRegistrationBean) initializer).getTargetBeanName();
			addServletContextInitializerBean(Filter.class, beanName, initializer, beanFactory, source);
		}
		// 4. ServletListenerRegistrationBean -> EventListener
		else if (initializer instanceof ServletListenerRegistrationBean) {
			EventListener source = ((ServletListenerRegistrationBean<?>) initializer).getListener();
			addServletContextInitializerBean(EventListener.class, beanName, initializer, beanFactory, source);
		}
		// 5. ServletContextInitializer -> 位置的
		else {
			addServletContextInitializerBean(ServletContextInitializer.class, beanName, initializer, beanFactory,
					initializer);
		}
	}

	// 添加 ServletContext 中的三大组件的初始化器
	private void addServletContextInitializerBean(Class<?> type, String beanName, ServletContextInitializer initializer,
			ListableBeanFactory beanFactory, Object source) {

		// 1. 组件类型 type 和 初始化器 initializer 加入 initializers
		this.initializers.add(type, initializer);
		if (source != null) {
			// Mark the underlying source as seen in case it wraps an existing bean
			// 2. source 加入 seen
			this.seen.add(source);
		}
		// 3. 日志
		if (logger.isTraceEnabled()) {
			String resourceDescription = getResourceDescription(beanName, beanFactory);
			int order = getOrder(initializer);
			logger.trace("Added existing " + type.getSimpleName() + " initializer bean '" + beanName + "'; order="
					+ order + ", resource=" + resourceDescription);
		}
	}

	private String getResourceDescription(String beanName, ListableBeanFactory beanFactory) {
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			return registry.getBeanDefinition(beanName).getResourceDescription();
		}
		return "unknown";
	}

	@SuppressWarnings("unchecked")
	protected void addAdaptableBeans(ListableBeanFactory beanFactory) {
		// 添加适配的Bean

		// 1. 获取BeanFactory总共的MultipartConfig
		MultipartConfigElement multipartConfig = getMultipartConfig(beanFactory);
		// 2. 添加 ServletRegistrationBeanAdapter
		addAsRegistrationBean(beanFactory, Servlet.class, new ServletRegistrationBeanAdapter(multipartConfig));
		// 3. 添加 FilterRegistrationBeanAdapter
		addAsRegistrationBean(beanFactory, Filter.class, new FilterRegistrationBeanAdapter());
		for (Class<?> listenerType : ServletListenerRegistrationBean.getSupportedTypes()) {
			addAsRegistrationBean(beanFactory, EventListener.class, (Class<EventListener>) listenerType,
					new ServletListenerRegistrationBeanAdapter());
		}
	}

	private MultipartConfigElement getMultipartConfig(ListableBeanFactory beanFactory) {
		List<Entry<String, MultipartConfigElement>> beans = getOrderedBeansOfType(beanFactory,
				MultipartConfigElement.class);
		return beans.isEmpty() ? null : beans.get(0).getValue();
	}

	protected <T> void addAsRegistrationBean(ListableBeanFactory beanFactory, Class<T> type,
			RegistrationBeanAdapter<T> adapter) {
		addAsRegistrationBean(beanFactory, type, type, adapter);
	}

	private <T, B extends T> void addAsRegistrationBean(ListableBeanFactory beanFactory, Class<T> type,
			Class<B> beanType, RegistrationBeanAdapter<T> adapter) {
		// 1. 查找Bean工厂中所有beanType[只有Servlet/Filter]的bean,并需要排序在seen已经加载过
		List<Map.Entry<String, B>> entries = getOrderedBeansOfType(beanFactory, beanType, this.seen);
		for (Entry<String, B> entry : entries) {
			String beanName = entry.getKey();
			B bean = entry.getValue();
			// 2. 将bean加入到已经加载过的集合seen中
			if (this.seen.add(bean)) {
				// One that we haven't already seen
				// 3. 对于用户注册的Filter/Servlet,由于不是我们的目标 FilterRegistrationBean/ServletRegistrationBean 初始化器
				// 是无法在 初始化器的onStartup中被注册到 servletContext 中 -- 因此我们需要适配为 初始化器
				// 然后加入到 initializers
				// 后续再到 ServletWebServerApplicationContext 中 selfInitialize(ServletContext servletContext).onRefresh() -> createWebServer() -> selfInitialize() 完成启动哦

				// 3.1 调用 adapter的createRegistrationBean 创建组件对应的合适的RegistrationBean
				RegistrationBean registration = adapter.createRegistrationBean(beanName, bean, entries.size());
				// ❗️❗️❗️
				// 注意哦 -- 到这里就可以发现一个问题,以Filter组件为例
				// 实际上 SpringBoot 首先加载的是ioc容器的FilterRegistrationBean注册到initializers [从BeanFactory获取时都是支持排序]
				// 然后才是将ioc容器的Filter做一个适配到RegistrationBean后,又注册到initializers [从BeanFactory获取时都是支持排序]
				// 但是但是 -- 最终在构造器中会再次使用 排序器进行排序 -> 所以上面的这里仅仅现在的list,后面还会对其sort一遍
				int order = getOrder(bean);
				registration.setOrder(order);
				// 3.2 然后注入到 initializers 中去
				this.initializers.add(type, registration);
				if (logger.isTraceEnabled()) {
					logger.trace("Created " + type.getSimpleName() + " initializer for bean '" + beanName + "'; order="
							+ order + ", resource=" + getResourceDescription(beanName, beanFactory));
				}
			}
		}
	}

	private int getOrder(Object value) {
		return new AnnotationAwareOrderComparator() {
			@Override
			public int getOrder(Object obj) {
				return super.getOrder(obj);
			}
		}.getOrder(value);
	}

	private <T> List<Entry<String, T>> getOrderedBeansOfType(ListableBeanFactory beanFactory, Class<T> type) {
		return getOrderedBeansOfType(beanFactory, type, Collections.emptySet());
	}

	private <T> List<Entry<String, T>> getOrderedBeansOfType(ListableBeanFactory beanFactory, Class<T> type,
			Set<?> excludes) {
		String[] names = beanFactory.getBeanNamesForType(type, true, false);
		Map<String, T> map = new LinkedHashMap<>();
		for (String name : names) {
			if (!excludes.contains(name) && !ScopedProxyUtils.isScopedTarget(name)) {
				T bean = beanFactory.getBean(name, type);
				if (!excludes.contains(bean)) {
					map.put(name, bean);
				}
			}
		}
		List<Entry<String, T>> beans = new ArrayList<>(map.entrySet());
		beans.sort((o1, o2) -> AnnotationAwareOrderComparator.INSTANCE.compare(o1.getValue(), o2.getValue()));
		return beans;
	}

	private void logMappings(MultiValueMap<Class<?>, ServletContextInitializer> initializers) {
		if (logger.isDebugEnabled()) {
			logMappings("filters", initializers, Filter.class, FilterRegistrationBean.class);
			logMappings("servlets", initializers, Servlet.class, ServletRegistrationBean.class);
		}
	}

	private void logMappings(String name, MultiValueMap<Class<?>, ServletContextInitializer> initializers,
			Class<?> type, Class<? extends RegistrationBean> registrationType) {
		List<ServletContextInitializer> registrations = new ArrayList<>();
		registrations.addAll(initializers.getOrDefault(registrationType, Collections.emptyList()));
		registrations.addAll(initializers.getOrDefault(type, Collections.emptyList()));
		String info = registrations.stream().map(Object::toString).collect(Collectors.joining(", "));
		logger.debug("Mapping " + name + ": " + info);
	}

	@Override
	public Iterator<ServletContextInitializer> iterator() {
		// 核心 -- 在 ServletWebServerApplicationContext#selfInitialize(ServletContext servletContext)
		// 遍历所有的ServletContextInitializer调用onStartup方法时生效
		return this.sortedList.iterator();
	}

	@Override
	public int size() {
		return this.sortedList.size();
	}

	/**
	 * Adapter to convert a given Bean type into a {@link RegistrationBean} (and hence a
	 * {@link ServletContextInitializer}).
	 *
	 * @param <T> the type of the Bean to adapt
	 */
	@FunctionalInterface
	protected interface RegistrationBeanAdapter<T> {
		// 适配器将给定的 Bean 类型转换为RegistrationBean （因此是ServletContextInitializer ）。

		// source 是来源,比如Filter\Servlet
		// name 是beanName
		RegistrationBean createRegistrationBean(String name, T source, int totalNumberOfSourceBeans);

	}

	/**
	 * {@link RegistrationBeanAdapter} for {@link Servlet} beans.
	 */
	private static class ServletRegistrationBeanAdapter implements RegistrationBeanAdapter<Servlet> {

		private final MultipartConfigElement multipartConfig;

		ServletRegistrationBeanAdapter(MultipartConfigElement multipartConfig) {
			this.multipartConfig = multipartConfig;
		}

		@Override
		public RegistrationBean createRegistrationBean(String name, Servlet source, int totalNumberOfSourceBeans) {
			String url = (totalNumberOfSourceBeans != 1) ? "/" + name + "/" : "/";
			if (name.equals(DISPATCHER_SERVLET_NAME)) {
				url = "/"; // always map the main dispatcherServlet to "/"
			}
			ServletRegistrationBean<Servlet> bean = new ServletRegistrationBean<>(source, url);
			bean.setName(name);
			bean.setMultipartConfig(this.multipartConfig);
			return bean;
		}

	}

	/**
	 * {@link RegistrationBeanAdapter} for {@link Filter} beans.
	 */
	private static class FilterRegistrationBeanAdapter implements RegistrationBeanAdapter<Filter> {
		// 很简单的一个适配器
		// 实现 RegistrationBeanAdapter<T> 接口,然后创建一个 RegistrationBean 就可以啦
		// 比如 FilterRegistrationBeanAdapter的createRegistrationBean(String name, Filter source, int totalNumberOfSourceBeans)
		// 就是 new 一个 FilterRegistrationBean 将 filter 放进去
		// 然后用 这个Filter的BeanName作为filterName

		@Override
		public RegistrationBean createRegistrationBean(String name, Filter source, int totalNumberOfSourceBeans) {
			FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>(source);
			bean.setName(name);
			return bean;
		}

	}

	/**
	 * {@link RegistrationBeanAdapter} for certain {@link EventListener} beans.
	 */
	private static class ServletListenerRegistrationBeanAdapter implements RegistrationBeanAdapter<EventListener> {

		@Override
		public RegistrationBean createRegistrationBean(String name, EventListener source,
				int totalNumberOfSourceBeans) {
			return new ServletListenerRegistrationBean<>(source);
		}

	}

}
