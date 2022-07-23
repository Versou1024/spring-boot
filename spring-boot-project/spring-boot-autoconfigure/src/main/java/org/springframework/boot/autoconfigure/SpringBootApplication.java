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

package org.springframework.boot.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.repository.Repository;

/**
 * Indicates a {@link Configuration configuration} class that declares one or more
 * {@link Bean @Bean} methods and also triggers {@link EnableAutoConfiguration
 * auto-configuration} and {@link ComponentScan component scanning}. This is a convenience
 * annotation that is equivalent to declaring {@code @Configuration},
 * {@code @EnableAutoConfiguration} and {@code @ComponentScan}.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @since 1.2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
	// 是 @SpringBootConfiguration 与 @EnableAutoConfiguration 与 @ComponentScan 的组合注解
	// 核心就在:
	// @SpringBootConfiguration -> 
	// 		元注解 @Configuration -> 主要是在SpringBoot中使用@SpringBootConfiguration表示为一个SpringBoot项目的配置类
	// @EnableAutoConfiguration ->
	//		元注解1: @Import(AutoConfigurationImportSelector.class) -> 从jar包里的spring.factories分析出需要自动配置的配置类,加入到ioc容器中
	//		元注解2: @AutoConfigurationPackage ->  @Import(AutoConfigurationPackages.Registrar.class) -> 将默认从启动类的basePackage封装为BasePackage类加入到ioc容器中
	// @ComponentScan -> 
	// 		ComponentScanAnnotationParser#parse()方法中关于@ComponentScan的basePackages、basePackageCLasses的处理逻辑如下：
	//		1. 先后获取 basePackage指定包名、basePackageClasses指定的类所在的包
	//		2. 如果上一个步骤，获取的结果为空，也就是没有配置basePackage或basePackageClasses，就会将声明@ComponentScan元注解的类的包作为basePackage使用哦
	//		对应这里如果仅仅使用@SpringBootApplication那么就是扫描启动类所在package下的所有的可导入到ioc容器的类
	//		包括但不限于,@Configuration\@Component\@Controller\@Bean等等
	
	// ❗️❗️❗️
	// note: 注意一下 @EnableAutoConfiguration -> @Import(AutoConfigurationImportSelector.class) 
	// 中的 AutoConfigurationImportSelector 是DeferredImportSelector类型的
	// 分析:
	// 在容器ConfigurationClassPostProcessor中由于启动类使用了@SpringBootConfiguration
	// 因此会作为配置类,被解析 -> 其中就包括这里的 @ComponentScan 以及 @EnableAutoConfiguration的@Import(AutoConfigurationImportSelector.class)
	// 在之前学习的Spring源码中,会优先处理@ComponentScan的扫描的组件类并加载到BeanDefinitionRegistry中,并且如果是配置类的还会继续递归处理
	// 然后才会去处理DeferredImportSelector,因此我们断定spring.factories外部配置的加载是比内部配置的组件类的加载的慢
	// 另一方面如果@ComponentScan扫描的组件类是配置类并且由@Bean的方法,并不会立即将@Bean的方法给处理加载到BeanDefinitionRegistry中
	// 而会在this.deferredImportSelectorHandler.process()之后执行,所以说deferredImportSelector中的配置类会比配置类中的@Bean的方法先解析
	// 但是虽然解析啦但是deferredImportSelector导入的类并不会立即被加载BeanDefinitionRegistry中,而会被存入到ConfigurationClassParser.configurationClasses中
	// 然后在ConfigurationClassPostProcessor中先回去对配置类->配置类中的@Bean方法先后引入到BeanDefinitionRegistry中
	// 在由于@ComponentScane的扫描的组件类已经加载到BeanDefinition中,而扫描的组件类会被继续处理,对于其中的@Import为ImportSelector的情况会立即执行并继续尝试解析
	
	/**
	 * Exclude specific auto-configuration classes such that they will never be applied.
	 * @return the classes to exclude
	 */
	@AliasFor(annotation = EnableAutoConfiguration.class)
	Class<?>[] exclude() default {};
	// 等价@EnableAutoConfiguration的exclude()

	/**
	 * Exclude specific auto-configuration class names such that they will never be
	 * applied.
	 * @return the class names to exclude
	 * @since 1.3.0
	 */
	@AliasFor(annotation = EnableAutoConfiguration.class)
	String[] excludeName() default {};
	// 等价@EnableAutoConfiguration的exclude()


	/**
	 * Base packages to scan for annotated components. Use {@link #scanBasePackageClasses}
	 * for a type-safe alternative to String-based package names.
	 * <p>
	 * <strong>Note:</strong> this setting is an alias for
	 * {@link ComponentScan @ComponentScan} only. It has no effect on {@code @Entity}
	 * scanning or Spring Data {@link Repository} scanning. For those you should add
	 * {@link org.springframework.boot.autoconfigure.domain.EntityScan @EntityScan} and
	 * {@code @Enable...Repositories} annotations.
	 * @return base packages to scan
	 * @since 1.3.0
	 */
	@AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
	String[] scanBasePackages() default {};
	// 等价: @ComponentScan的basePackage

	/**
	 * Type-safe alternative to {@link #scanBasePackages} for specifying the packages to
	 * scan for annotated components. The package of each class specified will be scanned.
	 * <p>
	 * Consider creating a special no-op marker class or interface in each package that
	 * serves no purpose other than being referenced by this attribute.
	 * <p>
	 * <strong>Note:</strong> this setting is an alias for
	 * {@link ComponentScan @ComponentScan} only. It has no effect on {@code @Entity}
	 * scanning or Spring Data {@link Repository} scanning. For those you should add
	 * {@link org.springframework.boot.autoconfigure.domain.EntityScan @EntityScan} and
	 * {@code @Enable...Repositories} annotations.
	 * @return base packages to scan
	 * @since 1.3.0
	 */
	@AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
	Class<?>[] scanBasePackageClasses() default {};
	// 	// 等价: @ComponentScan的basePackageClasses

	/**
	 * Specify whether {@link Bean @Bean} methods should get proxied in order to enforce
	 * bean lifecycle behavior, e.g. to return shared singleton bean instances even in
	 * case of direct {@code @Bean} method calls in user code. This feature requires
	 * method interception, implemented through a runtime-generated CGLIB subclass which
	 * comes with limitations such as the configuration class and its methods not being
	 * allowed to declare {@code final}.
	 * <p>
	 * The default is {@code true}, allowing for 'inter-bean references' within the
	 * configuration class as well as for external calls to this configuration's
	 * {@code @Bean} methods, e.g. from another configuration class. If this is not needed
	 * since each of this particular configuration's {@code @Bean} methods is
	 * self-contained and designed as a plain factory method for container use, switch
	 * this flag to {@code false} in order to avoid CGLIB subclass processing.
	 * <p>
	 * Turning off bean method interception effectively processes {@code @Bean} methods
	 * individually like when declared on non-{@code @Configuration} classes, a.k.a.
	 * "@Bean Lite Mode" (see {@link Bean @Bean's javadoc}). It is therefore behaviorally
	 * equivalent to removing the {@code @Configuration} stereotype.
	 * @since 2.2
	 * @return whether to proxy {@code @Bean} methods
	 */
	@AliasFor(annotation = Configuration.class)
	boolean proxyBeanMethods() default true;
	// 等价 @SpringConfiguration的元注解@Configuration的proxyBeanMethods()

}
