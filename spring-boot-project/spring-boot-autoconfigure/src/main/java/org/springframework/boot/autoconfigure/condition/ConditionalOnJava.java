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

package org.springframework.boot.autoconfigure.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.system.JavaVersion;
import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional @Conditional} that matches based on the JVM version the application
 * is running on.
 *
 * @author Oliver Gierke
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnJavaCondition.class)
public @interface ConditionalOnJava {
	// 根据运行应用程序的 JVM 版本匹配。

	/**
	 * Configures whether the value configured in {@link #value()} shall be considered the
	 * upper exclusive or lower inclusive boundary. Defaults to
	 * {@link Range#EQUAL_OR_NEWER}.
	 * @return the range
	 */
	Range range() default Range.EQUAL_OR_NEWER;
	// 配置value()中配置的值是否应被视为上排他性边界或下包容性边界
	// Range.EQUAL_OR_NEWER; 表示应该大于或者等于指定的JavaVersion版本
	// Range.OLDER_THAN; 表示应该小于指定的JavaVersion版本

	/**
	 * The {@link JavaVersion} to check for. Use {@link #range()} to specify whether the
	 * configured value is an upper-exclusive or lower-inclusive boundary.
	 * @return the java version
	 */
	JavaVersion value();

	/**
	 * Range options.
	 */
	enum Range {

		/**
		 * Equal to, or newer than the specified {@link JavaVersion}.
		 */
		EQUAL_OR_NEWER,

		/**
		 * Older than the specified {@link JavaVersion}.
		 */
		OLDER_THAN

	}

}
