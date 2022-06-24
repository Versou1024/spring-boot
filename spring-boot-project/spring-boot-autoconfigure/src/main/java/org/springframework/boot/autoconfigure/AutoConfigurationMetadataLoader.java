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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

/**
 * Internal utility used to load {@link AutoConfigurationMetadata}.
 *
 * @author Phillip Webb
 */
final class AutoConfigurationMetadataLoader {
	// AutoConfigurationMetadataLoader = Auto ConfigurationMetadata Loader

	// 路径必须是默认的哦 --
	// 将 META-INF/spring-autoconfigure-metadata.properties 放入到 PropertiesAutoConfigurationMetadata的properties中
	protected static final String PATH = "META-INF/spring-autoconfigure-metadata.properties";

	// 以Mybatis-Plus就会有一个"META-INF/spring-autoconfigure-metadata.properties"文件存在
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration=
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.AutoConfigureAfter=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.ConditionalOnClass=org.apache.ibatis.session.SqlSessionFactory,org.mybatis.spring.SqlSessionFactoryBean
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.ConditionalOnSingleCandidate=javax.sql.DataSource
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration=
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$FreeMarkerConfiguration=
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$FreeMarkerConfiguration.ConditionalOnClass=org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver,org.mybatis.scripting.freemarker.FreeMarkerLanguageDriverConfig
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$LegacyFreeMarkerConfiguration=
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$LegacyFreeMarkerConfiguration.ConditionalOnClass=org.mybatis.scripting.freemarker.FreeMarkerLanguageDriver
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$LegacyVelocityConfiguration=
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$LegacyVelocityConfiguration.ConditionalOnClass=org.mybatis.scripting.velocity.Driver
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$ThymeleafConfiguration=
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$ThymeleafConfiguration.ConditionalOnClass=org.mybatis.scripting.thymeleaf.ThymeleafLanguageDriver
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$VelocityConfiguration=
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration$VelocityConfiguration.ConditionalOnClass=org.mybatis.scripting.velocity.VelocityLanguageDriver,org.mybatis.scripting.velocity.VelocityLanguageDriverConfig
	//com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration.ConditionalOnClass=org.apache.ibatis.scripting.LanguageDriver

	private AutoConfigurationMetadataLoader() {
	}

	// 加载自动配置的元数据
	static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader) {
		return loadMetadata(classLoader, PATH);
	}

	static AutoConfigurationMetadata loadMetadata(ClassLoader classLoader, String path) {
		try {
			// 1. 将META-INF/spring-autoconfigure-metadata.properties 的加入到 properties
			Enumeration<URL> urls = (classLoader != null) ? classLoader.getResources(path)
					: ClassLoader.getSystemResources(path);
			Properties properties = new Properties();
			while (urls.hasMoreElements()) {
				properties.putAll(PropertiesLoaderUtils.loadProperties(new UrlResource(urls.nextElement())));
			}
			// 2. 加载元数据
			return loadMetadata(properties);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load @ConditionalOnClass location [" + path + "]", ex);
		}
	}

	static AutoConfigurationMetadata loadMetadata(Properties properties) {
		// 构造 PropertiesAutoConfigurationMetadata
		return new PropertiesAutoConfigurationMetadata(properties);
	}

	/**
	 * {@link AutoConfigurationMetadata} implementation backed by a properties file.
	 */
	private static class PropertiesAutoConfigurationMetadata implements AutoConfigurationMetadata {
		// AutoConfigurationMetadata 是自动配置的元数据

		private final Properties properties;

		PropertiesAutoConfigurationMetadata(Properties properties) {
			this.properties = properties;
		}

		@Override
		public boolean wasProcessed(String className) {
			return this.properties.containsKey(className);
		}


		// className + "." + key 将作为 properties 中查询的key
		@Override
		public Integer getInteger(String className, String key) {
			return getInteger(className, key, null);
		}

		@Override
		public Integer getInteger(String className, String key, Integer defaultValue) {
			String value = get(className, key);
			return (value != null) ? Integer.valueOf(value) : defaultValue;
		}

		@Override
		public Set<String> getSet(String className, String key) {
			return getSet(className, key, null);
		}

		@Override
		public Set<String> getSet(String className, String key, Set<String> defaultValue) {
			String value = get(className, key);
			return (value != null) ? StringUtils.commaDelimitedListToSet(value) : defaultValue;
		}

		@Override
		public String get(String className, String key) {
			return get(className, key, null);
		}

		@Override
		public String get(String className, String key, String defaultValue) {
			String value = this.properties.getProperty(className + "." + key);
			return (value != null) ? value : defaultValue;
		}

	}

}