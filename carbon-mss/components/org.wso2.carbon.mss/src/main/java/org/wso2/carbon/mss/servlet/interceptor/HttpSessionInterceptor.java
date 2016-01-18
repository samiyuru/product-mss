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

package org.wso2.carbon.mss.servlet.interceptor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.wso2.carbon.mss.servlet.servlet.HttpSessionThreadLocal;
import org.wso2.carbon.mss.servlet.servlet.NettyHttpSession;
import org.wso2.carbon.mss.servlet.session.HttpSessionStore;
import org.wso2.carbon.mss.servlet.util.Utils;

import java.util.Collection;

public class HttpSessionInterceptor implements NettyInterceptor {
    private boolean sessionRequestedByCookie;

    public HttpSessionInterceptor(HttpSessionStore sessionStore) {
        HttpSessionThreadLocal.setSessionStore(sessionStore);
    }

    @Override
    public void onRequestReceived(ChannelHandlerContext ctx, HttpRequest request) {

        HttpSessionThreadLocal.unset();
        
        Collection<Cookie> cookies = Utils.getCookies(
                NettyHttpSession.SESSION_ID_KEY, request);
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String jsessionId = cookie.getValue();
                NettyHttpSession s = HttpSessionThreadLocal.getSessionStore()
                        .findSession(jsessionId);
                if (s != null) {
                    HttpSessionThreadLocal.set(s);
                    this.sessionRequestedByCookie = true;
                    break;
                }
            }
        }
    }

    @Override
    public void onRequestSuccessed(ChannelHandlerContext ctx,
                                   HttpResponse response) {

        NettyHttpSession s = HttpSessionThreadLocal.get();
        if (s != null && !this.sessionRequestedByCookie) {
            // setup the Cookie for session
            HttpHeaders.addHeader(response, Names.SET_COOKIE,
                                  ClientCookieEncoder.encode(NettyHttpSession.SESSION_ID_KEY, s.getId()));
        }

    }

    @Override
    public void onRequestFailed(ChannelHandlerContext ctx, Throwable e) {
        this.sessionRequestedByCookie = false;
        HttpSessionThreadLocal.unset();
    }
}
