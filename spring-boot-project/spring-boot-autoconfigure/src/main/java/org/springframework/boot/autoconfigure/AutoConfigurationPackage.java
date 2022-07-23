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

import org.springframework.context.annotation.Import;

/**
 * Indicates that the package containing the annotated class should be registered with
 * {@link AutoConfigurationPackages}.
 *
 * @author Phillip Webb
 * @since 1.3.0
 * @see AutoConfigurationPackages
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AutoConfigurationPackages.Registrar.class)
public @interface AutoConfigurationPackage {
	// AutoConfigurationPackage注解的作用是将 添加该注解的类所在的package 作为 自动配置package 进行管理
	// 可以通过 AutoConfigurationPackages 工具类获取自动配置package列表
	
	// 当通过注解@SpringBootApplication标注启动类时，已经为启动类添加了@AutoConfigurationPackage注解。
	// 路径为 @SpringBootApplication -> @EnableAutoConfiguration -> @AutoConfigurationPackage。
	// 也就是说当SpringBoot应用启动时默认会将启动类所在的package作为自动配置的package。
	
	// 注解链:
	// @SpringBootApplication -> @EnableAutoConfiguration -> @AutoConfigurationPackage -> @Import(AutoConfigurationPackages.Registrar.class)
	// AutoConfigurationPackages.Registrar -> 向BeanDefinitionRegistry直接注册BeanDefinition ->
	// 首先找出启动类所在的package -- beanName就是AutoConfigurationPackages.class.getName(),beanClass就是BasePackages,并将启动类的package作为BasePackages的构造器参数
}
