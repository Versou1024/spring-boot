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

package org.springframework.boot.env;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Allows for customization of the application's {@link Environment} prior to the
 * application context being refreshed.
 * <p>
 * EnvironmentPostProcessor implementations have to be registered in
 * {@code META-INF/spring.factories}, using the fully qualified name of this class as the
 * key.
 * <p>
 * {@code EnvironmentPostProcessor} processors are encouraged to detect whether Spring's
 * {@link org.springframework.core.Ordered Ordered} interface has been implemented or if
 * the {@link org.springframework.core.annotation.Order @Order} annotation is present and
 * to sort instances accordingly if so prior to invocation.
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @since 1.3.0
 */
@FunctionalInterface
public interface EnvironmentPostProcessor {
	
	// 作用:
	// 允许在 refresh ApplicationContext 之前自定义应用程序的Environment 
	// EnvironmentPostProcessor 实现必须在META-INF/spring.factories中注册，使用此类的完全限定名称作为键。
	
	// 实现建议:
	// EnvironmentPostProcessor处理器检测Spring的Ordered接口是否已实现或是否存在@Order注释，并在调用之前对实例进行相应的排序

	// 起作用的原因:
	// Spring.factories 中有一行:
	// org.springframework.boot.env.EnvironmentPostProcessor = com.baomidou.mybatisplus.autoconfigure.SafetyEncryptProcessor
	// 其实主要还是:
	// SpringBoot下的
	// class ConfigFileApplicationListener implements EnvironmentPostProcessor, SmartApplicationListener, Ordered
	// 	    @Override
	//	    public void onApplicationEvent(ApplicationEvent event) {
	//	    	if (event instanceof ApplicationEnvironmentPreparedEvent) {
	//              // 在 refresh ApplicationContext之前发布ApplicationEnvironmentPreparedEvent事件,允许用户自定义应用程序的Environment
	//	    		onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event); 
	//	    	}
	//	    	if (event instanceof ApplicationPreparedEvent) {
	//	    		onApplicationPreparedEvent(event);
	//	    	}
	//	    }
	//
	//	    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
	//	    	List<EnvironmentPostProcessor> postProcessors = loadPostProcessors(); // 加载EnvironmentPostProcessor的bean出来
	//	    	postProcessors.add(this);
	//	    	AnnotationAwareOrderComparator.sort(postProcessors); // 对EnvironmentPostProcessor的bean排序
	//	    	for (EnvironmentPostProcessor postProcessor : postProcessors) {
	//              // 触发 EnvironmentPostProcessor#postProcessEnvironment(..)
	//	    		postProcessor.postProcessEnvironment(event.getEnvironment(), event.getSpringApplication());
	//	    	}
	//	    }
	// 
	//      List<EnvironmentPostProcessor> loadPostProcessors() {
	//          // 为什么 spring.factories 的 EnvironmentPostProcessor.class 作为 key 生效的原因哦
	//		    return SpringFactoriesLoader.loadFactories(EnvironmentPostProcessor.class, getClass().getClassLoader());
	//	    }

	/**
	 * Post-process the given {@code environment}.
	 * @param environment the environment to post-process
	 * @param application the application to which the environment belongs
	 */
	void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application);

}
