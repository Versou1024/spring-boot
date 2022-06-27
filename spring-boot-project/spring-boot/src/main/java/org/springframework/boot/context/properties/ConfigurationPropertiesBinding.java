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

package org.springframework.boot.context.properties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import liquibase.pro.packaged.W;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Qualifier for beans that are needed to configure the binding of
 * {@link ConfigurationProperties @ConfigurationProperties} (e.g. Converters).
 *
 * @author Dave Syer
 * @since 1.3.0
 */
@Qualifier(ConfigurationPropertiesBinding.VALUE)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationPropertiesBinding {
	// @ConfigurationPropertiesBinding 注解是让 Spring Boot 知道如何使用该转换器做将String的属性转为某个对象数据绑定
	
// 比如:
// 传入
// myapp.mail.max-attachment-weight=5kg
// 对应的转换器
	
//	class WeightConverter implements Converter<String,Weight> {
//		@Override
//		public Weight conver(String source) {
//			
//		}
//	}
//	
//	@Configuration
//	class PropertiesConfig{
//		@Bean
//		@ConfigurationPropertiesBinding
//		public WeightConverter weightConverter() {
//			return new WeightConverter();
//		}
//	}

	/**
	 * Concrete value for the {@link Qualifier @Qualifier}.
	 */
	String VALUE = "org.springframework.boot.context.properties.ConfigurationPropertiesBinding";

}
