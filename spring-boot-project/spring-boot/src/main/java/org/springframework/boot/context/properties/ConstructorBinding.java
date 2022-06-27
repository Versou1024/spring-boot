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

/**
 * Annotation that can be used to indicate that configuration properties should be bound
 * using constructor arguments rather than by calling setters. Can be added at the type
 * level (if there is an unambiguous constructor) or on the actual constructor to use.
 *
 * @author Phillip Webb
 * @since 2.2.0
 * @see ConfigurationProperties
 */
@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConstructorBinding {
	
	// # 自定义配置
	//minbox:
	//  config:
	//    author: 恒宇少年 - 于起宇
	//    blog-address: https://blog.yuqiyu.com
	
	//@ConfigurationProperties(prefix = PREFIX)
	//@ConstructorBinding
	//public class MinBoxConfig {
	//    /**
	//     * 映射绑定 "minbox.config"前缀的配置信息
	//     */
	//    public static final String PREFIX = "minbox.config";
	//    /**
	//     * 配置信息：作者
	//     */
	//    private String author;
	//    /**
	//     * 配置信息：博客地址
	//     */
	//    private String blogAddress;
	//
	//    public MinBoxConfig(String author, String blogAddress) {
	//        this.author = author;
	//        this.blogAddress = blogAddress;
	//    }
	//
	//    public String getAuthor() {
	//        return author;
	//    }
	//
	//    public String getBlogAddress() {
	//        return blogAddress;
	//    }
	//}

}
