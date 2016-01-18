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

package org.wso2.carbon.mss.example;

import org.wso2.carbon.mss.session.Session;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

/**
 * Hello service resource class.
 */
@Path("/hello")
public class HelloService {

    private static final String COUNT_KEY = "COUNT_KEY";

    @GET
    @Path("/{name}")
    public String hello(@PathParam("name") String name, @Context Session session) {
        Integer count = (Integer) session.get(COUNT_KEY);
        if (count == null) {
            count = 1;
        } else {
            count = count + 1;
        }
        session.put(COUNT_KEY, count);
        return "Hello " + name + " count " + count;
    }

}
