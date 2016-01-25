/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mss.example;

import org.wso2.carbon.mss.MicroservicesRunner;
import org.wso2.carbon.mss.session.InMemorySessionStore;
import org.wso2.carbon.mss.session.SessionInterceptor;
import org.wso2.carbon.mss.session.SimpleSessionIdGenerator;

/**
 * Application entry point.
 */
public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner()
                .addInterceptor(new SessionInterceptor(new InMemorySessionStore()
                        .setSessionTimeout(1000 * 60)
                        .setSessionIdleTimeout(1000 * 60)
                        .setSessionCleanupInterval(1000 * 60 * 10)
                        .init(),
                        new SimpleSessionIdGenerator()))
                .deploy(new HelloService())
                .start();
    }
}
