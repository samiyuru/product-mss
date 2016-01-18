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

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Object to store session information.
 */
public class Session {

    private final String sessionId;
    private final Map<Object, Object> sessionInfo;
    private long creationTime;
    private long lastAccessTime;

    /**
     * Constructor of the Session object.
     *
     * @param sessionId Id of the session
     */
    private Session(String sessionId) {
        this.sessionInfo = new ConcurrentHashMap<>();
        this.sessionId = sessionId;
        this.creationTime = Instant.now().toEpochMilli();
    }

    public void updateAccessTime() {
        lastAccessTime = Instant.now().toEpochMilli();
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void put(Object key, Object val) {
        sessionInfo.put(key, val);
    }

    public Object get(Object key) {
        return sessionInfo.get(key);
    }

    public static Session newSession(String sessionId) {
        Session session = new Session(sessionId);
        return session;
    }
}
