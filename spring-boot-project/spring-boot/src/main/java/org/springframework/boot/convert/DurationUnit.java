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

package org.springframework.boot.convert;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Annotation that can be used to change the default unit used when converting a
 * {@link Duration}.
 *
 * @author Phillip Webb
 * @since 2.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DurationUnit {

//	属性配置类:
//	@Configuration(prefix="myapp.mail")
//	class MailModuleProperties{
//		@Duration(ChronoUnit.SECONDS)
//		private Duration pauseBetweenMails;
//	}
//	配置文本:
//	myapp.mail.pause-between-mails=5s
//	官网说明:
//	配置Duration不写单位,默认按照毫秒来指定,我们可以通过@DurationUnit来指定单位
	
	/**
	 * The duration unit to use if one is not specified.
	 * @return the duration unit
	 */
	ChronoUnit value();

}
