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

package org.wso2.carbon.mss.session;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.Interceptor;
import org.wso2.carbon.mss.ServiceMethodInfo;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.ws.rs.core.Context;

/**
 * Session interceptor that enables session support of the micro services.
 */
public class SessionInterceptor implements Interceptor {

    private final SessionStore sessionStore;
    private final SessionIdGenerator sessionIdGenerator;
    private static final String SESSION_ID_KEY = "session_id";
    private static final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

    public SessionInterceptor(SessionStore sessionStore, SessionIdGenerator sessionIdGenerator) {
        this.sessionStore = sessionStore;
        this.sessionIdGenerator = sessionIdGenerator;
    }

    @Override
    public boolean preCall(HttpRequest request, HttpResponder responder, ServiceMethodInfo serviceMethodInfo) {
        Session session = null;
        try {
            String sessionId = getSessionId(getCookies(request)).value();
            session = sessionStore.getSession(sessionId);
        } catch (NoSuchElementException e) {
            log.info("Session cookie not available");
        }
        if (session == null) {
            String sessionId = sessionIdGenerator.generateSessionId();
            session = Session.newSession(sessionId);
            sessionStore.putSession(session);
            responder.setHeader(HttpHeaders.Names.SET_COOKIE,
                    ServerCookieEncoder.LAX.encode(SESSION_ID_KEY, session.getSessionId()));
        }
        session.updateAccessTime();

        Parameter[] parameters = serviceMethodInfo.getMethod().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Context.class)
                    && parameter.getType().isAssignableFrom(Session.class)) {
                serviceMethodInfo.overrideMethodParam(i, session);
            }
        }
        return true;
    }

    @Override
    public void postCall(HttpRequest request, HttpResponseStatus status, ServiceMethodInfo serviceMethodInfo) {
        // Not required
    }

    private Cookie getSessionId(Set<Cookie> cookies) {
        return cookies.stream()
                .filter(cookie -> SESSION_ID_KEY.equals(cookie.name()))
                .findFirst()
                .get();
    }

    private Set<Cookie> getCookies(HttpRequest request) {
        final String cookieString = request.headers().get(HttpHeaders.Names.COOKIE);
        if (cookieString == null || cookieString.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return ServerCookieDecoder.LAX.decode(cookieString);
    }
}
