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

import io.netty.handler.codec.http.HttpRequest;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.session.Session;

import java.util.List;
import java.util.Map;

/**
 * Container class that will hold HttpResourceModel and
 * request information inorder to process the request when all
 * data is ready.
 */
public class HttpMethodInfoBuilder {

    private HttpResourceModel httpResourceModel;
    private HttpRequest request;
    private Session session;
    private HttpResponder responder;
    private Map<String, String> groupValues;
    private String contentType;
    private List<String> acceptTypes;
    private HttpMethodInfo httpMethodInfo;

    public static HttpMethodInfoBuilder getInstance() {
        HttpMethodInfoBuilder httpMethodInfoBuilder = new HttpMethodInfoBuilder();
        return httpMethodInfoBuilder;
    }

    public HttpMethodInfoBuilder httpResourceModel(HttpResourceModel httpResourceModel) {
        this.httpResourceModel = httpResourceModel;
        return this;
    }

    public HttpMethodInfoBuilder httpRequest(HttpRequest request) {
        this.request = request;
        return this;
    }

    public HttpMethodInfoBuilder httpResponder(HttpResponder responder) {
        this.responder = responder;
        return this;
    }

    public HttpMethodInfoBuilder session(Session session) {
        this.session = session;
        return this;
    }

    public HttpMethodInfoBuilder requestInfo(Map<String, String> groupValues,
                                             String contentType,
                                             List<String> acceptTypes) {
        this.groupValues = groupValues;
        this.contentType = contentType;
        this.acceptTypes = acceptTypes;
        return this;
    }

    public HttpMethodInfo build() throws HandlerException {
        if (httpMethodInfo == null) {
            httpMethodInfo = (new HttpResourceModelProcessor(httpResourceModel))
                    .buildHttpMethodInfo(request, responder, session, groupValues, contentType, acceptTypes);
        }
        return httpMethodInfo;
    }

    public HttpResourceModel getHttpResourceModel() {
        return httpResourceModel;
    }

    public HttpResponder getResponder() {
        return responder;
    }

    public HttpRequest getRequest() {
        return request;
    }
}
