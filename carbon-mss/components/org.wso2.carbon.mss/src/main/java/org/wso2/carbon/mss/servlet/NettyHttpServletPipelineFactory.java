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

package org.wso2.carbon.mss.servlet;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.wso2.carbon.mss.servlet.interceptor.ChannelInterceptor;
import org.wso2.carbon.mss.servlet.interceptor.HttpSessionInterceptor;
import org.wso2.carbon.mss.servlet.session.DefaultHttpSessionStore;
import org.wso2.carbon.mss.servlet.session.HttpSessionStore;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyHttpServletPipelineFactory extends ChannelInitializer<Channel> {

    Logger LOG = Logger.getAnonymousLogger();

    //Holds the child channel
    private final ChannelGroup allChannels = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    ;

    private final HttpSessionWatchdog watchdog;

    private final boolean supportSession;

    private final Map<String, NettyHttpContextHandler> handlerMap;

    private final int maxChunkContentSize;

    private final EventExecutorGroup applicationExecutor;

    private final NettyHttpServerEngine nettyHttpServerEngine;

    public NettyHttpServletPipelineFactory(boolean supportSession, int threadPoolSize, int maxChunkContentSize,
                                           Map<String, NettyHttpContextHandler> handlerMap,
                                           NettyHttpServerEngine engine) {
        this.supportSession = supportSession;
        this.watchdog = new HttpSessionWatchdog();
        this.handlerMap = handlerMap;
        this.maxChunkContentSize = maxChunkContentSize;
        this.nettyHttpServerEngine = engine;
        //TODO need to configure the thread size of EventExecutorGroup
        applicationExecutor = new DefaultEventExecutorGroup(threadPoolSize);
    }


    public Map<String, NettyHttpContextHandler> getHttpContextHandlerMap() {
        return handlerMap;
    }

    public ChannelGroup getAllChannels() {
        return allChannels;
    }

    public NettyHttpContextHandler getNettyHttpHandler(String url) {
        for (Map.Entry<String, NettyHttpContextHandler> entry : handlerMap.entrySet()) {
            // Here just check the context path first
            if (url.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void start() {
        if (supportSession) {
            new Thread(watchdog).start();
        }
    }

    public void shutdown() {
        allChannels.close().awaitUninterruptibly();
        watchdog.stopWatching();
        applicationExecutor.shutdownGracefully();
    }

    protected HttpSessionStore getHttpSessionStore() {
        return new DefaultHttpSessionStore();
    }

    protected NettyHttpServletHandler getServletHandler() {

        NettyHttpServletHandler handler = new NettyHttpServletHandler(this);
        handler.addInterceptor(new ChannelInterceptor());
        if (supportSession) {
            handler.addInterceptor(new HttpSessionInterceptor(getHttpSessionStore()));
        }
        return handler;
    }

    protected ChannelPipeline getDefaulHttpChannelPipeline(Channel channel) throws Exception {

        // Create a default pipeline implementation.
        ChannelPipeline pipeline = channel.pipeline();

        SslHandler sslHandler = configureServerSSLOnDemand();
        if (sslHandler != null) {
            LOG.log(Level.FINE,
                    "Server SSL handler configured and added as an interceptor against the ChannelPipeline: {}",
                    sslHandler);
            pipeline.addLast("ssl", sslHandler);
        }

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(maxChunkContentSize));
        pipeline.addLast("encoder", new HttpResponseEncoder());

        // Remove the following line if you don't want automatic content
        // compression.
        pipeline.addLast("deflater", new HttpContentCompressor());
        // Set up the idle handler
        pipeline.addLast("idle", new IdleStateHandler(nettyHttpServerEngine.getReadIdleTime(),
                nettyHttpServerEngine.getWriteIdleTime(), 0));

        return pipeline;
    }

    private SslHandler configureServerSSLOnDemand() throws Exception {
        return null;
    }

    private class HttpSessionWatchdog implements Runnable {

        private boolean shouldStopWatching;

        @Override
        public void run() {

            while (!shouldStopWatching) {

                try {
                    HttpSessionStore store = getHttpSessionStore();
                    if (store != null) {
                        store.destroyInactiveSessions();
                    }
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    return;
                }

            }

        }

        public void stopWatching() {
            this.shouldStopWatching = true;
        }

    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = getDefaulHttpChannelPipeline(ch);

        pipeline.addLast(applicationExecutor, "handler", this.getServletHandler());
    }

}
