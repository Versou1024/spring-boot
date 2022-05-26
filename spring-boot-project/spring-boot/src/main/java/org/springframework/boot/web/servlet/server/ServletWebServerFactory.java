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

package org.springframework.boot.web.servlet.server;

import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;

/**
 * Factory interface that can be used to create a {@link WebServer}.
 *
 * @author Phillip Webb
 * @see WebServer
 * @since 2.0.0
 */
@FunctionalInterface
public interface ServletWebServerFactory {
	// 可用于创建WebServer的工厂接口。
	// 其子类包括:
	// TomcatServletWebServerFactory
	// JettyServletWebServerFactory 等

	/**
	 * Gets a new fully configured but paused {@link WebServer} instance. Clients should
	 * not be able to connect to the returned server until {@link WebServer#start()} is
	 * called (which happens when the {@code ApplicationContext} has been fully
	 * refreshed).
	 * @param initializers {@link ServletContextInitializer}s that should be applied as
	 * the server starts
	 * @return a fully configured and started {@link WebServer}
	 * @see WebServer#stop()
	 */
	WebServer getWebServer(ServletContextInitializer... initializers);
	// 取一个新的完全配置但暂停的WebServer实例。在调用WebServer.start()之前，客户端应该无法连接到返回的服务器（这在ApplicationContext已完全刷新时发生）。
	// params：initializers —— 应该在服务器启动时应用的 ServletContextInitializer.onStartUp(ServletContext) -- 这是Spring友好的ServletContextInitializer

}
