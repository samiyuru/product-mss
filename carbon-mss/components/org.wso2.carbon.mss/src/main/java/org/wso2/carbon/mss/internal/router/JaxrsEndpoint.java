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

package org.wso2.carbon.mss.internal.router;

import org.wso2.carbon.mss.internal.router.api.EndpointBean;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Contains JAX-RS like resource method endpoint info
 */
public class JaxrsEndpoint implements EndpointBean {

    private Method method;
    private Object handler;
    private List<String> consumesMediaTypes;
    private List<String> producesMediaTypes;
    private List<HttpResourceModel.ParameterInfo<?>> parameterInfoList;

    public JaxrsEndpoint(Method method,
                         Object handler,
                         List<String> consumesMediaTypes,
                         List<String> producesMediaTypes,
                         List<HttpResourceModel.ParameterInfo<?>> parameterInfoList) {
        this.method = method;
        this.handler = handler;
        this.consumesMediaTypes = consumesMediaTypes;
        this.producesMediaTypes = producesMediaTypes;
        this.parameterInfoList = parameterInfoList;
    }

    public List<String> getConsumesMediaTypes() {
        return consumesMediaTypes;
    }

    public List<String> getProducesMediaTypes() {
        return producesMediaTypes;
    }

    /**
     * @return handler method that handles an http end-point.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return the object that contains the method.
     */
    public Object getHttpHandler() {
        return handler;
    }

    public List<HttpResourceModel.ParameterInfo<?>> getParamInfoList() {
        return parameterInfoList;
    }

}
