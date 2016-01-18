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

package org.wso2.carbon.mss.session;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Netty handler for enabling sessions
 */
public class SessionHandler extends SimpleChannelInboundHandler {

    private final SessionStore sessionStore;
    private final SessionIdGenerator sessionIdGenerator;
    private final static String SESSION_ID_KEY = "session_id";
    private static final Logger log = LoggerFactory.getLogger(SessionHandler.class);
    private Session session = null;
    private boolean isNewSession = false;
    public static final String SESSION_OBJECT = "SESSION_OBJECT";
    public static final String SESSION_WRITER = "session-writer";

    public SessionHandler(SessionStore sessionStore, SessionIdGenerator sessionIdGenerator) {
        this.sessionStore = sessionStore;
        this.sessionIdGenerator = sessionIdGenerator;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().pipeline().addLast(SESSION_WRITER, new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof HttpResponse && isNewSession) {
                    HttpResponse response = (HttpResponse) msg;
                    response.headers().add(HttpHeaders.Names.SET_COOKIE,
                            ServerCookieEncoder.LAX.encode(SESSION_ID_KEY, session.getSessionId()));
                    isNewSession = false;
                }
                ctx.write(msg, promise);
            }
        });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            try {
                String sessionId = getSessionId(getCookies(request)).value();
                session = sessionStore.getSession(sessionId);
            } catch (NoSuchElementException e) {
                log.info("Session cookie not available");
            }
            if (session == null) {
                String sessionId = sessionIdGenerator.generateSessionId();
                isNewSession = true;
                session = Session.newSession(sessionId);
                sessionStore.putSession(session);
                ctx.attr(AttributeKey.valueOf(SESSION_OBJECT)).set(session);
            }
            session.updateAccessTime();
            ctx.fireChannelRead(msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private Cookie getSessionId(Set<Cookie> cookies) {
        return cookies.stream()
                .filter(cookie -> SESSION_ID_KEY.equals(cookie.name()))
                .findFirst()
                .get();
    }

    private Set<Cookie> getCookies(HttpRequest request) {
        final String cookieString = request.headers().get(HttpHeaders.Names.COOKIE);
        if (cookieString == null || cookieString.trim().isEmpty()) return Collections.emptySet();
        return ServerCookieDecoder.LAX.decode(cookieString);
    }
}
