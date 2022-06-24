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

import java.util.Set;

/**
 * Provides access to meta-data written by the auto-configure annotation processor.
 *
 * @author Phillip Webb
 * @since 1.5.0
 */
public interface AutoConfigurationMetadata {
	// boot 1.5支持的,提供对自动配置注解@EnableAutoConfiguration处理的元数据支持


	/**
	 * Return {@code true} if the specified class name was processed by the annotation
	 * processor.
	 * @param className the source class
	 * @return if the class was processed
	 */
	boolean wasProcessed(String className); // 指定的类名已由注解处理器处理，则返回true

	/**
	 * Get an {@link Integer} value from the meta-data.
	 * @param className the source class
	 * @param key the meta-data key
	 * @return the meta-data value or {@code null}
	 */
	Integer getInteger(String className, String key); // 元数据中获取Integer数值

	/**
	 * Get an {@link Integer} value from the meta-data.
	 * @param className the source class
	 * @param key the meta-data key
	 * @param defaultValue the default value
	 * @return the meta-data value or {@code defaultValue}
	 */
	Integer getInteger(String className, String key, Integer defaultValue);
	// 从元数据中获取Integer数值,可以指定默认值

	/**
	 * Get a {@link Set} value from the meta-data.
	 * @param className the source class
	 * @param key the meta-data key
	 * @return the meta-data value or {@code null}
	 */
	Set<String> getSet(String className, String key);

	/**
	 * Get a {@link Set} value from the meta-data.
	 * @param className the source class
	 * @param key the meta-data key
	 * @param defaultValue the default value
	 * @return the meta-data value or {@code defaultValue}
	 */
	Set<String> getSet(String className, String key, Set<String> defaultValue);

	/**
	 * Get an {@link String} value from the meta-data.
	 * @param className the source class
	 * @param key the meta-data key
	 * @return the meta-data value or {@code null}
	 */
	String get(String className, String key);

	/**
	 * Get an {@link String} value from the meta-data.
	 * @param className the source class
	 * @param key the meta-data key
	 * @param defaultValue the default value
	 * @return the meta-data value or {@code defaultValue}
	 */
	String get(String className, String key, String defaultValue);

}