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

package org.wso2.carbon.mss;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * HttpResponder is used to send response back to clients.
 */
public interface HttpResponder {

    /**
     * Set header values of the response object.
     * This replaces the existing headers with new header values.
     *
     * @param headers map containing the header values
     */
    void setHeaders(Map<String, String> headers);

    /**
     * Set headers in response object.
     *
     * @param headerName  name of the header to set
     * @param headerValue value of the header
     */
    void setHeader(String headerName, String headerValue);

    /**
     * Get header value of the response object.
     *
     * @param headerName name of the header to get
     * @return header value for the header name
     */
    String getHeader(String headerName);

    /**
     * Sends json response back to the client.
     *
     * @param status Status of the response.
     * @param object Object that will be serialized into Json and sent back as content.
     */
    void sendJson(HttpResponseStatus status, Object object);

    /**
     * Sends json response back to the client.
     *
     * @param status Status of the response.
     * @param object Object that will be serialized into Json and sent back as content.
     * @param type   Type of object.
     */
    void sendJson(HttpResponseStatus status, Object object, Type type);

    /**
     * Sends json response back to the client using the given gson object.
     *
     * @param status Status of the response.
     * @param object Object that will be serialized into Json and sent back as content.
     * @param type   Type of object.
     * @param gson   Gson object for serialization.
     */
    void sendJson(HttpResponseStatus status, Object object, Type type, Gson gson);

    /**
     * Send a string response back to the http client.
     *
     * @param status status of the Http response.
     * @param data   string data to be sent back.
     */
    void sendString(HttpResponseStatus status, String data);

    /**
     * Send only a status code back to client without any content.
     *
     * @param status status of the Http response.
     */
    void sendStatus(HttpResponseStatus status);

    /**
     * Send a response containing raw bytes. Sets "application/octet-stream" as content type header.
     *
     * @param status status of the Http response.
     * @param bytes  bytes to be sent back.
     */
    void sendByteArray(HttpResponseStatus status, byte[] bytes);

    /**
     * Sends a response containing raw bytes. Default content type is "application/octet-stream", but can be
     * overridden in the headers.
     *
     * @param status status of the Http response
     * @param buffer bytes to send
     */
    void sendBytes(HttpResponseStatus status, ByteBuffer buffer);

    /**
     * Respond to the client saying the response will be in chunks. The response body can be sent in chunks
     * using the {@link ChunkResponder} returned.
     *
     * @param status the status code to respond with
     * @return ChunkResponder
     */
    ChunkResponder sendChunkStart(HttpResponseStatus status);

    /**
     * Send response back to client.
     *
     * @param status      Status of the response.
     * @param content     Content to be sent back.
     * @param contentType Type of content.
     */
    void sendContent(HttpResponseStatus status, ByteBuf content, String contentType);


    /**
     * Sends a file content back to client with response status 200.
     *
     * @param file The file to send
     */
    void sendFile(File file, String contentType) throws IOException;
}
