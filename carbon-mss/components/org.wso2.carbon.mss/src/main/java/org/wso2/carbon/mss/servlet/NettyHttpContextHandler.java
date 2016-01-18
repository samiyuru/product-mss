/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mss.servlet;

import org.wso2.carbon.mss.servlet.servlet.NettyHttpServletRequest;
import org.wso2.carbon.mss.servlet.servlet.NettyServletContext;
import org.wso2.carbon.mss.servlet.servlet.NettyServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class NettyHttpContextHandler {
    private static final Logger LOG = Logger.getAnonymousLogger();
    private final ServletContext servletContext;
    private List<NettyHttpHandler> nettyHttpHandlerList = new CopyOnWriteArrayList<NettyHttpHandler>();

    public NettyHttpContextHandler(String contextPath) {
        servletContext = new NettyServletContext(contextPath);
    }

    public void addNettyHttpHandler(NettyHttpHandler handler) {
        // setup the servletContext for it
        handler.setServletContext(servletContext);
        nettyHttpHandlerList.add(handler);
    }

    public NettyHttpHandler getNettyHttpHandler(String urlName) {
        for (NettyHttpHandler handler : nettyHttpHandlerList) {
            if (urlName.equals(handler.getName())) {
                return handler;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return nettyHttpHandlerList.isEmpty();
    }

    public String getContextPath() {
        return servletContext.getContextPath();
    }

    public void removeNettyHttpHandler(String urlName) {
        NettyHttpHandler handler = getNettyHttpHandler(urlName);
        if (handler != null) {
            nettyHttpHandlerList.remove(handler);
        } else {
            LOG.log(Level.WARNING, "REMOVE_HANDLER_FAILED_MSG", urlName);
        }
    }

    public void handle(String target, NettyHttpServletRequest request,
                       NettyServletResponse response) throws IOException, ServletException {

        for (NettyHttpHandler handler : nettyHttpHandlerList) {
            handler.handle(target, request, response);
        }

    }
}
