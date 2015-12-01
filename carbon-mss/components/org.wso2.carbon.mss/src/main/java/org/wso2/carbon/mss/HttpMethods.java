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

package org.wso2.carbon.mss;

import io.netty.handler.codec.http.HttpMethod;
import org.wso2.carbon.mss.internal.router.ExceptionHandler;
import org.wso2.carbon.mss.internal.router.LambdaEndpoint;
import org.wso2.carbon.mss.internal.router.LambdaResourceModel;
import org.wso2.carbon.mss.internal.router.api.HttpResourceModel;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for holding handlers for routes
 */
public class HttpMethods {

    private static ExceptionHandler exceptionHandler = new ExceptionHandler();
    private List<HttpResourceModel> lambdaResourceModels = new LinkedList<>();
    private static final HttpMethod
            GET = HttpMethod.GET,
            POST = HttpMethod.POST,
            PUT = HttpMethod.PUT,
            DELETE = HttpMethod.DELETE;

    /**
     * Map the route for HTTP GET requests
     *
     * @param path  the path
     * @param route The route
     */
    public void get(String path, Route route) {
        LambdaResourceModel lambdaResourceModel = new LambdaResourceModel(path,
                Collections.singleton(GET),
                exceptionHandler,
                new LambdaEndpoint(route));
        lambdaResourceModels.add(lambdaResourceModel);
    }

    /**
     * Map the route for HTTP POST requests
     *
     * @param path  the path
     * @param route The route
     */
    public void post(String path, Route route) {
        LambdaResourceModel lambdaResourceModel = new LambdaResourceModel(path,
                Collections.singleton(POST),
                exceptionHandler,
                new LambdaEndpoint(route));
        lambdaResourceModels.add(lambdaResourceModel);
    }

    /**
     * Map the route for HTTP PUT requests
     *
     * @param path  the path
     * @param route The route
     */
    public void put(String path, Route route) {
        LambdaResourceModel lambdaResourceModel = new LambdaResourceModel(path,
                Collections.singleton(PUT),
                exceptionHandler,
                new LambdaEndpoint(route));
        lambdaResourceModels.add(lambdaResourceModel);
    }

    /**
     * Map the route for HTTP DELETE requests
     *
     * @param path  the path
     * @param route The route
     */
    public void delete(String path, Route route) {
        LambdaResourceModel lambdaResourceModel = new LambdaResourceModel(path,
                Collections.singleton(DELETE),
                exceptionHandler,
                new LambdaEndpoint(route));
        lambdaResourceModels.add(lambdaResourceModel);
    }

    public List<HttpResourceModel> getLambdaResourceModels() {
        return lambdaResourceModels;
    }
}
