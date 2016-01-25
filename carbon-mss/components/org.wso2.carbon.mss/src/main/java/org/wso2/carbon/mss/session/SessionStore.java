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

/**
 * Interface for the session store.
 */
public interface SessionStore {

    SessionStore init();

    /**
     * Store a session in session store.
     *
     * @param session session object to be stored
     */
    void putSession(Session session);

    /**
     * Retrieve a stored session via the session id.
     *
     * @param sessionId Id of the session
     * @return Session object or null if no session object for the session ID
     */
    Session getSession(String sessionId);

    /**
     * Timeout to expire sessions after creation.
     *
     * @param timeout Session expiration timeout
     */
    SessionStore setSessionTimeout(long timeout);

    /**
     * Timeout to expire sessions after last access time.
     *
     * @param timeout Session expiration timeout
     */
    SessionStore setSessionIdleTimeout(long timeout);

    /**
     * Interval to cleanup expired sessions.
     *
     * @param interval Interval to cleanup expired sessions
     */
    SessionStore setSessionCleanupInterval(long interval);
}
