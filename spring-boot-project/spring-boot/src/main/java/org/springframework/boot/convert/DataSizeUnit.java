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

import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

/**
 * Annotation that can be used to change the default unit used when converting a
 * {@link DataSize}.
 *
 * @author Stephane Nicoll
 * @since 2.1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSizeUnit {
	
//	属性配置类:
//	@Configuration(prefix="myapp.mail")
//	class MailModuleProperties{
//		@DataSizeUnit(DataUnit.MEGABYTES)
//		private DataSize maxAttachmentSize;
//	}
//	配置文本:
//	myapp.mail.max-attachment-size=1MB
//	官网说明:
//	配置DataSize不写单位,默认按照BYTE来指定,我们可以通过@DurationUnit来指定单位
			
	/**
	 * The {@link DataUnit} to use if one is not specified.
	 * @return the data unit
	 */
	DataUnit value();

}
