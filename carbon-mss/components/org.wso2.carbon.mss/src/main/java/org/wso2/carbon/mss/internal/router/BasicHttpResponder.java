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

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedFile;
import org.wso2.carbon.mss.ChunkResponder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

/**
 * HttpResponder responds back to the client that initiated the request. Caller can use sendJson method to respond
 * back to the client in json format.
 */
public class BasicHttpResponder extends AbstractHttpResponder {

    private final Channel channel;
    private final boolean keepAlive;
    private final AtomicBoolean responded;
    private Map<String, String> headers = new HashMap<>();

    public BasicHttpResponder(Channel channel, boolean keepAlive) {
        this.channel = channel;
        this.keepAlive = keepAlive;
        responded = new AtomicBoolean(false);
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public void setHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }

    @Override
    public String getHeader(String headerName) {
        return headers.get(headerName);
    }

    @Override
    public ChunkResponder sendChunkStart(HttpResponseStatus status) {
        Preconditions.checkArgument(responded.compareAndSet(false, true), "Response has been already sent");
        Preconditions.checkArgument((status.code() >= 200 && status.code() < 210), "Http Chunk Failure");
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);

        response.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);

        boolean responseKeepAlive = setResponseKeepAlive(response);
        channel.write(response);
        return new ChannelChunkResponder(channel, responseKeepAlive);
    }

    @Override
    public void sendContent(HttpResponseStatus status, @Nullable ByteBuf content, String contentType) {
        Preconditions.checkArgument(responded.compareAndSet(false, true), "Response has been already sent");
        HttpResponse response;
        if (content != null) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
            HttpHeaders responseHeaders = response.headers();
            responseHeaders.set(HttpHeaders.Names.CONTENT_TYPE, contentType);
            responseHeaders.set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
        } else {
            response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
            HttpHeaders responseHeaders = response.headers();
            responseHeaders.set(HttpHeaders.Names.CONTENT_LENGTH, 0);
        }
        setCustomHeaders(response);

        boolean responseKeepAlive = setResponseKeepAlive(response);
        ChannelFuture future = channel.write(response);
        if (!responseKeepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void sendFile(File file, String contentType) throws IOException {
        Preconditions.checkArgument(responded.compareAndSet(false, true), "Response has been already sent");
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

        setCustomHeaders(response);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);

        if (!response.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)) {
            response.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
        }
        channel.writeAndFlush(response);

        HttpChunkedInput httpChunkWriter = new HttpChunkedInput(new ChunkedFile(file));
        ChannelFuture sendFileFuture = channel.write(httpChunkWriter);

        final boolean responseKeepAlive = setResponseKeepAlive(response);
        sendFileFuture.addListener(future -> {
            if (!responseKeepAlive) {
                channel.close();
            }
        });
    }

    private void setCustomHeaders(HttpResponse response) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                response.headers().add(entry.getKey(), entry.getValue());
            }
        }
    }

    private boolean setResponseKeepAlive(HttpResponse response) {
        HttpHeaders headers = response.headers();
        boolean closeConn = HttpHeaders.Values.CLOSE.equalsIgnoreCase(headers.get(HttpHeaders.Names.CONNECTION));
        boolean responseKeepAlive = this.keepAlive && !closeConn;

        if (responseKeepAlive) {
            headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        } else {
            headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        }

        return responseKeepAlive;
    }
}
