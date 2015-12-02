/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mss.internal.router.lambda;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.Route;
import org.wso2.carbon.mss.internal.router.ExceptionHandler;
import org.wso2.carbon.mss.internal.router.HttpMethodResponseHandler;
import org.wso2.carbon.mss.internal.router.api.HttpMethodInfo;

import java.util.Map;
import javax.ws.rs.core.MediaType;

/**
 * Object of this class contains all information
 * required to invoke a route handler
 */
public class LambdaMethodInfo implements HttpMethodInfo {

    private HttpRequest httpRequest;
    private HttpResponder httpResponder;
    private Map<String, String> groupValues;
    private Route route;
    private ExceptionHandler exceptionHandler;

    public LambdaMethodInfo(HttpRequest httpRequest,
                            HttpResponder httpResponder,
                            Map<String, String> groupValues,
                            Route route,
                            ExceptionHandler exceptionHandler) {
        this.httpRequest = httpRequest;
        this.httpResponder = httpResponder;
        this.groupValues = groupValues;
        this.route = route;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void invoke() {
        try {
            Object returnVal = route.handle(httpRequest, httpResponder);
            //sending return value as output
            new HttpMethodResponseHandler()
                    .setResponder(httpResponder)
                    .setEntity(returnVal)
                    .setMediaType(MediaType.WILDCARD)
                    .send();
        } catch (Exception e) {
            exceptionHandler.handle(e, httpRequest, httpResponder);
        }
    }

    @Override
    public void chunk(HttpContent chunk) {

    }
}
