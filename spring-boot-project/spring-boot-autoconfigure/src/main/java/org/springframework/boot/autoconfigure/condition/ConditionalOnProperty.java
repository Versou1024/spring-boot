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

import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;

/**
 * {@link Conditional @Conditional} that checks if the specified properties have a
 * specific value. By default the properties must be present in the {@link Environment}
 * and <strong>not</strong> equal to {@code false}. The {@link #havingValue()} and
 * {@link #matchIfMissing()} attributes allow further customizations.
 * <p>
 * The {@link #havingValue} attribute can be used to specify the value that the property
 * should have. The table below shows when a condition matches according to the property
 * value and the {@link #havingValue()} attribute:
 *
 * <table border="1">
 * <caption>Having values</caption>
 * <tr>
 * <th>Property Value</th>
 * <th>{@code havingValue=""}</th>
 * <th>{@code havingValue="true"}</th>
 * <th>{@code havingValue="false"}</th>
 * <th>{@code havingValue="foo"}</th>
 * </tr>
 * <tr>
 * <td>{@code "true"}</td>
 * <td>yes</td>
 * <td>yes</td>
 * <td>no</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td>{@code "false"}</td>
 * <td>no</td>
 * <td>no</td>
 * <td>yes</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td>{@code "foo"}</td>
 * <td>yes</td>
 * <td>no</td>
 * <td>no</td>
 * <td>yes</td>
 * </tr>
 * </table>
 * <p>
 * If the property is not contained in the {@link Environment} at all, the
 * {@link #matchIfMissing()} attribute is consulted. By default missing attributes do not
 * match.
 * <p>
 * This condition cannot be reliably used for matching collection properties. For example,
 * in the following configuration, the condition matches if {@code spring.example.values}
 * is present in the {@link Environment} but does not match if
 * {@code spring.example.values[0]} is present.
 *
 * <pre class="code">
 * &#064;ConditionalOnProperty(prefix = "spring", name = "example.values")
 * class ExampleAutoConfiguration {
 * }
 * </pre>
 *
 * It is better to use a custom condition for such cases.
 *
 * @author Maciej Walkowiak
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnPropertyCondition.class)
public @interface ConditionalOnProperty {
	// @ConditionalOnProperty检查指定属性是否具有特定值。默认情况下，属性必须存在于Environment中并且不等于false. havingValue()和matchIfMissing()属性允许进一步自定义。
	// havingValue()属性可用于指定属性应具有的值。下表显示了根据属性值和havingValue()属性匹配条件的时间：
	// Property Value  havingValue=""  havingValue="true"  havingValue="false"  havingValue="foo"
	// "true"     		yes      		yes     			no     				no
	// "false"    		 no     		no     				yes     			no
	// "foo"      		yes      		no      			no      			yes

	// 如果该属性根本不包含在Environment中，则matchIfMissing()属性。默认情况下，缺少的属性不匹配。
	// 此条件不能可靠地用于匹配集合属性。例如，在以下配置中，如果spring.example.values存在于Environment中，则条件匹配，但如果spring.example.values[0]存在则不匹配。
	//   @ConditionalOnProperty(prefix = "spring", name = "example.values")
	//   class ExampleAutoConfiguration {
	//   }

	/**
	 * Alias for {@link #name()}.
	 * @return the names
	 */
	String[] value() default {};
	// name()属性的别名

	/**
	 * A prefix that should be applied to each property. The prefix automatically ends
	 * with a dot if not specified. A valid prefix is defined by one or more words
	 * separated with dots (e.g. {@code "acme.system.feature"}).
	 * @return the prefix
	 */
	String prefix() default "";
	// 属性前缀,如果该前缀不是.结尾的,则会自动加上

	/**
	 * The name of the properties to test. If a prefix has been defined, it is applied to
	 * compute the full key of each property. For instance if the prefix is
	 * {@code app.config} and one value is {@code my-value}, the full key would be
	 * {@code app.config.my-value}
	 * <p>
	 * Use the dashed notation to specify each property, that is all lower case with a "-"
	 * to separate words (e.g. {@code my-long-property}).
	 * @return the names
	 */
	String[] name() default {};
	// 属性名,如果前缀被声明了,则会拼接为prefix+name 去查找.通过-进行分割单词,name需要为小写

	/**
	 * The string representation of the expected value for the properties. If not
	 * specified, the property must <strong>not</strong> be equal to {@code false}.
	 * @return the expected value
	 */
	String havingValue() default "";
	// 表明所期望的结果,如果没有指定该属性,则该属性所对应的值只要不为false时才匹配

	/**
	 * Specify if the condition should match if the property is not set. Defaults to
	 * {@code false}.
	 * @return if should match if the property is missing
	 */
	boolean matchIfMissing() default false;
	// 表明配置的属性如果没有指定的话,是否匹配,默认不匹配

}
