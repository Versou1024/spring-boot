/*
 * Copyright 2012-2020 the original author or authors.
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Auto-configuration specific variant of Spring Framework's {@link Order @Order}
 * annotation. Allows auto-configuration classes to be ordered among themselves without
 * affecting the order of configuration classes passed to
 * {@link AnnotationConfigApplicationContext#register(Class...)}.
 * <p>
 * As with standard {@link Configuration @Configuration} classes, the order in which
 * auto-configuration classes are applied only affects the order in which their beans are
 * defined. The order in which those beans are subsequently created is unaffected and is
 * determined by each bean's dependencies and any {@link DependsOn @DependsOn}
 * relationships.
 *
 * @author Andy Wilkinson
 * @since 1.3.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface AutoConfigureOrder {
	// @AutoConfigureOrder属于1.3.0版本新增，表示绝对顺序（数字越小，优先顺序越高）
	// 另外，这几个注解并不互斥，可以同时标注在同一个@Configuration自动配置类上。
	
	//@Configuration(proxyBeanMethods = false)
	//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
	//@AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class, ValidationAutoConfiguration.class })
	//public class WebMvcAutoConfiguration { ... }
	
	
	//@Configuration(proxyBeanMethods = false)
	//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
	//@AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
	//public class DispatcherServletAutoConfiguration { ... }
	
	
	//@Configuration(proxyBeanMethods = false)
	//@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
	//public class ServletWebServerFactoryAutoConfiguration { ... }
	
	// 1.WebMvcAutoConfiguration被载入的前提是：DispatcherServletAutoConfiguration、TaskExecutionAutoConfiguration、ValidationAutoConfiguration这三个哥们都已经完成初始化
	// 2.DispatcherServletAutoConfiguration被载入的前提是：ServletWebServerFactoryAutoConfiguration已经完成初始化
	// 3.ServletWebServerFactoryAutoConfiguration被载入的前提是：@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)最高优先顺序，也就是说它无其它依赖，希望自己是最先被初始化的
	// 当碰到多个配置都是最高优先顺序的时候，且互相之前没有关系的话，顺序也是不定的。
	// 但若互相之间存在依赖关系（如本利的DispatcherServletAutoConfiguration和ServletWebServerFactoryAutoConfiguration都是最高优先级），那就按照相对顺序走
	
	/*
	注意误区:
	@Configuration
	public class B_ParentConfig {
	    B_ParentConfig() {
	        System.out.println("配置類ParentConfig構造器被執行...");
	    }
	}
	
	@Configuration
	public class A_SonConfig {
	
	    A_SonConfig() {
	        System.out.println("配置類SonConfig構造器被執行...");
	    }
	}
	
	@Configuration
	public class C_DemoConfig {
	    public C_DemoConfig(){
	        System.out.println("我是被自動掃描的配置，初始化啦....");
	    }
	}
	
	@SpringBootApplication
	public class Application {
	    public static void main(String[] args) {
	        SpringApplication.run(Application.class, args).close();
	    }
	}
	// 通过名称能知道我想要的达到的效果是：ParentConfig先载入，SonConfig后载入。（DemoConfig作为一个参考配置，作为日志参考使用即可）
	// 直接通过
	 @AutoConfigureBefore(A_SonConfig.class)
	 @Configuration
	public class B_ParentConfig {
	
	    B_ParentConfig() {
	        System.out.println("配置類ParentConfig構造器被執行...");
	    }
	 }
	 // 是不会生效的哦
	 // 必须满足以下条件:
	 // 1.把A_SonConfig和B_ParentConfig挪动到Application扫描不到的包内，切记：一定且必须是扫描不到的包内
	 // 2.当前工程里增加配置META-INF/spring.factories，内容为(配置里Son和Parent前后顺序对结果无影响)：
	 // 注意事项: 
	 // 1. 若你不用@AutoConfigureBefore这个注解，单单就想依赖于spring.factories里的先后顺序的来控制实际的载入顺序，答案是不可以，控制不了
	 // 2. 在项目根路径下被扫描进去配置类是在前面（见错误示例），通过spring.factories方式扫描进去是在它的后面（见正确姿势）
	 // 3. 从这个小细节可以衍生得到结论：Spring Boot的自动配置均是通过spring.factories来指定的，它的优先顺序最低（执行时机是最晚的）；
	 // 	通过扫描进来的一般都是你自己自定义的配置类，所以优先顺序是最高的，肯定在自动配置之前载入
	 // 4. 请尽量不要让自动配置类既被扫描到了，又放在spring.factories配置了，否则后者会覆盖前者，很容易造成莫名其妙的错误
	 */
	
	/**
	 * The default order value.
	 */
	int DEFAULT_ORDER = 0;

	/**
	 * The order value. Default is {@code 0}.
	 * @see Ordered#getOrder()
	 * @return the order value
	 */
	int value() default DEFAULT_ORDER;

}
