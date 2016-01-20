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

package org.wso2.carbon.mss.internal.router;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.mss.HttpResponder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

/**
 * Base implementation of {@link org.wso2.carbon.mss.HttpResponder} to simplify child implementations.
 */
public abstract class AbstractHttpResponder implements HttpResponder {

    private static final Gson GSON = new Gson();

    @Override
    public void sendJson(HttpResponseStatus status, Object object) {
        sendJson(status, object, object.getClass());
    }

    @Override
    public void sendJson(HttpResponseStatus status, Object object, Type type) {
        sendJson(status, object, type, GSON);
    }

    @Override
    public void sendJson(HttpResponseStatus status, Object object, Type type, Gson gson) {
        try {
            ByteBuf channelBuffer = Unpooled.buffer();
            try (
                    JsonWriter jsonWriter = new JsonWriter(
                            new OutputStreamWriter(new ByteBufOutputStream(channelBuffer), Charsets.UTF_8))
            ) {
                gson.toJson(object, type, jsonWriter);
            }

            sendContent(status, channelBuffer, "application/json");
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void sendString(HttpResponseStatus status, String data) {
        if (data == null) {
            sendStatus(status);
            return;
        }
        try {
            ByteBuf channelBuffer = Unpooled.wrappedBuffer(Charsets.UTF_8.encode(data));
            sendContent(status, channelBuffer, "text/plain; charset=utf-8");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void sendStatus(HttpResponseStatus status) {
        sendContent(status, null, null);
    }

    @Override
    public void sendByteArray(HttpResponseStatus status, byte[] bytes) {
        ByteBuf channelBuffer = Unpooled.wrappedBuffer(bytes);
        sendContent(status, channelBuffer, "application/octet-stream");
    }

    @Override
    public void sendBytes(HttpResponseStatus status, ByteBuffer buffer) {
        sendContent(status, Unpooled.wrappedBuffer(buffer), "application/octet-stream");
    }

}
