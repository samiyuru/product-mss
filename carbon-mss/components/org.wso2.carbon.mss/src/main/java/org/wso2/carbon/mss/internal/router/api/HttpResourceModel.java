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

package org.wso2.carbon.mss.internal.router.api;

import io.netty.handler.codec.http.HttpMethod;
import org.wso2.carbon.mss.internal.router.ExceptionHandler;

import java.util.List;
import java.util.Set;

/**
 * Object that implements this interface contains
 * information of a route endpoint
 */
public interface HttpResourceModel {

    /**
     * @return path associated with this model.
     */
    String getPath();

    /**
     * @return httpMethods.
     */
    Set<HttpMethod> getHttpMethod();

    boolean isStreamingReqSupported();

    ExceptionHandler getExceptionHandler();

    boolean matchConsumeMediaType(String consumesMediaType);

    boolean matchProduceMediaType(List<String> producesMediaTypes);

}
