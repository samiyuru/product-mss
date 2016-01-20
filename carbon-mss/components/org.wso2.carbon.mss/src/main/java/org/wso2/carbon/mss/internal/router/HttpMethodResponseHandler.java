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
import com.google.common.io.Files;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.mime.MimeMapper;
import org.wso2.carbon.mss.internal.mime.MimeMappingException;
import org.wso2.carbon.mss.internal.router.beanconversion.BeanConversionException;
import org.wso2.carbon.mss.internal.router.beanconversion.BeanConverter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Handles the return values of the resource methods
 * of JAX-RS resource classes.
 */
public class HttpMethodResponseHandler {

    private HttpResponder responder;
    private HttpResponseStatus status = null;
    private String mediaType = null;
    private Object entity;
    private Map<String, String> headers = new HashMap<>();

    /**
     * Set netty-http responder object.
     *
     * @param responder HttpResponder
     * @return this HttpMethodResponseHandler instance
     */
    public HttpMethodResponseHandler setResponder(HttpResponder responder) {
        this.responder = responder;
        return this;
    }

    /**
     * Set response http status code.
     *
     * @param status HTTP status code
     * @return this HttpMethodResponseHandler instance
     */
    public HttpMethodResponseHandler setStatus(int status) {
        this.status = HttpResponseStatus.valueOf(status);
        return this;
    }

    /**
     * Set media type of the entity.
     *
     * @param mediaType entity media type
     * @return this HttpMethodResponseHandler instance
     */
    public HttpMethodResponseHandler setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * Set entity body fro the response. If the entity is
     * type of javax.ws.rs.core.Response extract entity,
     * status code etc from it.
     *
     * @param entity the HTTP entity
     * @return this HttpMethodResponseHandler instance
     */
    public HttpMethodResponseHandler setEntity(Object entity) {
        if (entity instanceof Response) {
            Response response = (Response) entity;
            this.entity = response.getEntity();
            MultivaluedMap<String, String> multivaluedMap = response.getStringHeaders();
            if (multivaluedMap != null) {
                multivaluedMap.forEach((key, values) -> headers.put(key, String.join(", ", values)));
            }
            setStatus(response.getStatus());
            if (response.getMediaType() != null) {
                setMediaType(response.getMediaType().toString());
            }
        } else {
            this.entity = entity;
        }
        return this;
    }

    /**
     * Send response using netty-http provided responder.
     *
     * @throws BeanConversionException If bean conversion fails
     */
    public void send() throws BeanConversionException, IOException {
        HttpResponseStatus status;
        if (this.status != null) {
            status = this.status;
        } else if (entity != null) {
            status = HttpResponseStatus.OK;
        } else {
            status = HttpResponseStatus.NO_CONTENT;
        }
        Object entityToSend;
        if (entity != null) {
            if (entity instanceof File) {
                File file = (File) entity;
                if (mediaType == null || mediaType.equals(MediaType.WILDCARD)) {
                    try {
                        mediaType = MimeMapper.getMimeType(Files.getFileExtension(file.getName()));
                    } catch (MimeMappingException e) {
                        mediaType = MediaType.WILDCARD;
                    }
                }
                responder.setHeaders(headers);
                responder.sendFile(file, mediaType);
            } else {
                if (mediaType != null) {
                    entityToSend = BeanConverter.instance(mediaType)
                            .toMedia(entity);
                } else {
                    mediaType = MediaType.WILDCARD;
                    entityToSend = entity;
                }
                //String.valueOf() is used to send correct response for entity types other than String
                //such as primitives like numbers
                ByteBuf channelBuffer = Unpooled.wrappedBuffer(Charsets.UTF_8.encode(String.valueOf(entityToSend)));
                responder.setHeaders(headers);
                responder.sendContent(status, channelBuffer, mediaType);
            }
        } else {
            responder.setHeaders(headers);
            responder.sendStatus(status);
        }
    }
}
