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
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.mss.ChunkResponder;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.router.beanconversion.BeanConversionException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;

/**
 * Tests HttpMethodResponseHandlerTest class.
 */
public class HttpMethodResponseHandlerTest {

    @Test
    public void testNoStatusCodeNoEntity() throws BeanConversionException, IOException {
        new HttpMethodResponseHandler()
                .setResponder(new HttpResponderMock((HttpResponseStatus status,
                                                     Object entity, Map<String, String> headers) -> {
                    Assert.assertTrue("Expected 204", status.code() == HttpResponseStatus.NO_CONTENT.code());
                    Assert.assertEquals(null, entity);
                }))
                .send();
    }

    @Test
    public void testNoStatusCodeWithEntity() throws BeanConversionException, IOException {
        new HttpMethodResponseHandler()
                .setResponder(new HttpResponderMock((HttpResponseStatus status,
                                                     Object entity, Map<String, String> headers) -> {
                    Assert.assertTrue("Expected 200", status.code() == HttpResponseStatus.OK.code());
                    Assert.assertTrue(entity instanceof ByteBuf);
                    Assert.assertEquals("Entity", ((ByteBuf) entity).toString(Charsets.UTF_8));
                }))
                .setEntity("Entity")
                .send();
    }

    @Test
    public void testStatusCodeOkWithNoEntity() throws BeanConversionException, IOException {
        new HttpMethodResponseHandler()
                .setResponder(new HttpResponderMock((HttpResponseStatus status,
                                                     Object entity, Map<String, String> headers) -> {
                    Assert.assertTrue("Expected 200", status.code() == HttpResponseStatus.OK.code());
                    Assert.assertEquals(null, entity);
                }))
                .setStatus(HttpResponseStatus.OK.code())
                .send();
    }

    @Test
    public void testStatusCodeNotFoundWithNoEntity() throws BeanConversionException, IOException {
        new HttpMethodResponseHandler()
                .setResponder(new HttpResponderMock((HttpResponseStatus status,
                                                     Object entity, Map<String, String> headers) -> {
                    Assert.assertTrue("Expected 404", status.code() == HttpResponseStatus.NOT_FOUND.code());
                    Assert.assertEquals(null, entity);
                }))
                .setStatus(HttpResponseStatus.NOT_FOUND.code())
                .send();
    }

    @Test
    public void testStatusCodeOkWithPlainTextMediaType() throws BeanConversionException, IOException {
        String content = "Text-Content";
        new HttpMethodResponseHandler()
                .setResponder(new HttpResponderMock((HttpResponseStatus status,
                                                     Object entity, Map<String, String> headers) -> {
                    Assert.assertEquals(status.code(), HttpResponseStatus.OK.code());
                    //Assert.assertTrue(headers.containsEntry(HttpHeaders.Names.CONTENT_TYPE, MediaType.TEXT_PLAIN));
                    Assert.assertTrue(entity instanceof ByteBuf);
                    Assert.assertEquals(content, ((ByteBuf) entity).toString(Charsets.UTF_8));
                }))
                .setEntity(content)
                .setMediaType(MediaType.TEXT_PLAIN)
                .setStatus(HttpResponseStatus.OK.code())
                .send();
    }

    @Test
    public void testSendFileWithNoMediaTypeSet() throws BeanConversionException, IOException {
        File file = new File("");
        new HttpMethodResponseHandler()
                .setResponder(new HttpResponderMock((HttpResponseStatus status,
                                                     Object entity, Map<String, String> headers) -> {
                    Assert.assertTrue(entity == file);
                }))
                .setEntity(file)
                .send();
    }

    private static class HttpResponderMock implements HttpResponder {

        Map<String, String> headers = new HashMap<>();
        private final CallBack cb;

        public HttpResponderMock(CallBack cb) {
            this.cb = cb;
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
            return headers.get(headers);
        }

        @Override
        public void sendJson(HttpResponseStatus status, Object object) {
            cb.values(status, object, headers);
        }

        @Override
        public void sendJson(HttpResponseStatus status, Object object, Type type) {
            cb.values(status, object, headers);
        }

        @Override
        public void sendJson(HttpResponseStatus status, Object object, Type type, Gson gson) {
            cb.values(status, object, headers);
        }

        @Override
        public void sendString(HttpResponseStatus status, String data) {
            cb.values(status, data, headers);
        }

        @Override
        public void sendStatus(HttpResponseStatus status) {
            cb.values(status, null, headers);
        }

        @Override
        public void sendByteArray(HttpResponseStatus status, byte[] bytes) {
            cb.values(status, bytes, headers);
        }

        @Override
        public void sendBytes(HttpResponseStatus status, ByteBuffer buffer) {
            cb.values(status, buffer, headers);
        }

        @Override
        public ChunkResponder sendChunkStart(HttpResponseStatus status) {
            cb.values(status, null, headers);
            return null;
        }

        @Override
        public void sendContent(HttpResponseStatus status, ByteBuf content, String contentType) {
            cb.values(status, content, headers);
        }

        @Override
        public void sendFile(File file, String contentType) {
            cb.values(null, file, headers);
        }
    }

    private static interface CallBack {
        void values(HttpResponseStatus status, Object entity, Map<String, String> headers);
    }

}
