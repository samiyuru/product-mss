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

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of an in memory session store.
 */
public class InMemorySessionStore implements SessionStore {

    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private int NO_CLEANUP = -1;
    private long sessionIdleTimeout = Long.MAX_VALUE;
    private long sessionTimeout = Long.MAX_VALUE;
    private long cleanupInterval = NO_CLEANUP;

    @Override
    public SessionStore init() {
        if (cleanupInterval != NO_CLEANUP) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Collection<Session> sessions = sessionMap.values();
                    sessions.stream()
                            .filter(session -> (isSessionIdleTimeExpired(session) || isSessionExpired(session)))
                            .forEach(session -> sessionMap.remove(session.getSessionId()));
                }
            }, sessionTimeout, cleanupInterval);
        }
        return this;
    }

    @Override
    public void putSession(Session session) {
        sessionMap.put(session.getSessionId(), session);
    }

    @Override
    public Session getSession(String sessionId) {
        Session session = sessionMap.get(sessionId);
        if (session != null && (isSessionIdleTimeExpired(session) || isSessionExpired(session))) {
            sessionMap.remove(sessionId);
            return null;
        }
        return session;
    }

    @Override
    public SessionStore setSessionTimeout(long timeout) {
        sessionTimeout = timeout;
        return this;
    }

    @Override
    public SessionStore setSessionIdleTimeout(long timeout) {
        sessionIdleTimeout = timeout;
        return this;
    }

    @Override
    public SessionStore setSessionCleanupInterval(long interval) {
        cleanupInterval = interval;
        return this;
    }

    private boolean isSessionIdleTimeExpired(Session session) {
        return Instant.now().toEpochMilli() - session.getLastAccessTime() > sessionIdleTimeout;
    }

    private boolean isSessionExpired(Session session) {
        return Instant.now().toEpochMilli() - session.getCreationTime() > sessionTimeout;
    }
}
