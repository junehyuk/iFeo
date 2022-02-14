package com.feedle.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by infinitu on 2014. 6. 14..
 */
public class ProxyCommandServer {
    private static final byte[] COMMAND_VERSION = {0x00,0x01};
    private static final byte[] HERTBEAT_MESSAGE = {0x00,0x00,0x02,COMMAND_VERSION[0],COMMAND_VERSION[1]};
    private static final Long   HEARTBEAT_INTERVAL_IN_SECONDS = 30L;
    private static final Long   TRAFFIC_CNT_INTERVAL_IN_SECONDS = 10L;
    private static final int COMMAND_SERVER_PORT = 9458;

    private static final String FEEDLE_PROXY_ASIA_HOST  = "107.6.119.148";
    private static final String FEEDLE_PROXY_EU_HOST    = "72.251.246.234";
    private static final String FEEDLE_PROXY_USA_HOST   = "209.191.162.25";
    private static final String FEEDLE_PROXY_KOREA_HOST = "127.0.0.1";//"175.126.111.98";

    protected static final int FEEDLE_PROXY_ASIA_ID  = 1;
    protected static final int FEEDLE_PROXY_EU_ID    = 2;
    protected static final int FEEDLE_PROXY_USA_ID   = 3;
    protected static final int FEEDLE_PROXY_KOREA_ID = 4;

    private static Channel proxyASIA  = null;
    private static Channel proxyEU    = null;
    private static Channel proxyUSA   = null;
    private static Channel proxyKOREA = null;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ResponseReceiver proxyHandler =null;
    private TrafficCntCycleHandler cycleHandler = null;

    public ProxyCommandServer(ResponseReceiver responseReceiver, TrafficCntCycleHandler cycleHandler){
        this.proxyHandler = responseReceiver;
        this.cycleHandler = cycleHandler;
    }

    public void run(){
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        workerGroup.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(proxyASIA !=null) sendHeartbeat(proxyASIA );
                if(proxyEU   !=null) sendHeartbeat(proxyEU   );
                if(proxyUSA  !=null) sendHeartbeat(proxyUSA  );
                if(proxyKOREA!=null) sendHeartbeat(proxyKOREA);
            }
        },0, HEARTBEAT_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);

        workerGroup.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(proxyASIA !=null) cycleHandler.tick(FEEDLE_PROXY_ASIA_ID );
                if(proxyEU   !=null) cycleHandler.tick(FEEDLE_PROXY_EU_ID   );
                if(proxyUSA  !=null) cycleHandler.tick(FEEDLE_PROXY_USA_ID  );
                if(proxyKOREA!=null) cycleHandler.tick(FEEDLE_PROXY_KOREA_ID);
            }
        },0, TRAFFIC_CNT_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder());
                        System.out.println(ch.remoteAddress().getAddress().getHostAddress() + " is Connected");
                        switch (ch.remoteAddress().getAddress().getHostAddress()) {
                            case FEEDLE_PROXY_ASIA_HOST:
                                System.out.println("ASIA Host");
                                if (proxyASIA != null)
                                    proxyASIA.close();
                                proxyASIA = ch;
                                ch.pipeline().addLast(new ProxyResponseHandler(FEEDLE_PROXY_ASIA_ID));
                                break;
                            case FEEDLE_PROXY_EU_HOST:
                                System.out.println("EU Host");
                                if (proxyEU != null)
                                    proxyEU.close();
                                proxyEU = ch;
                                ch.pipeline().addLast(new ProxyResponseHandler(FEEDLE_PROXY_EU_ID));
                                break;
                            case FEEDLE_PROXY_USA_HOST:
                                System.out.println("USA Host");
                                if (proxyUSA != null)
                                    proxyUSA.close();
                                proxyUSA = ch;
                                ch.pipeline().addLast(new ProxyResponseHandler(FEEDLE_PROXY_USA_ID));
                                break;
                            case FEEDLE_PROXY_KOREA_HOST:
                                System.out.println("Korea Host");
                                if (proxyKOREA != null)
                                    proxyKOREA.close();
                                proxyKOREA = ch;
                                ch.pipeline().addLast(new ProxyResponseHandler(FEEDLE_PROXY_KOREA_ID));
                                ch.pipeline().addFirst(new LoggingHandler(LogLevel.DEBUG));
                                break;
                        }
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 32)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        b.bind(COMMAND_SERVER_PORT);
    }

    public void stop(){
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    private void sendHeartbeat(Channel channel){
        channel.write(HERTBEAT_MESSAGE);
    }

    protected void sendCommand(int proxyId,int Command,String Content){
        switch (proxyId) {
            case FEEDLE_PROXY_ASIA_ID:
                if (proxyASIA != null)
                    sendMsg(proxyASIA, (byte) Command,Content);
                break;
            case FEEDLE_PROXY_EU_ID:
                if (proxyEU != null)
                    sendMsg(proxyEU, (byte) Command,Content);
                break;
            case FEEDLE_PROXY_USA_ID:
                if (proxyUSA != null)
                    sendMsg(proxyUSA, (byte) Command,Content);
                break;
            case FEEDLE_PROXY_KOREA_ID:
                if (proxyKOREA != null)
                    sendMsg(proxyKOREA, (byte) Command,Content);
                break;
        }
    }

    private void sendMsg(Channel channel,byte code, String str){
        if(str==null) str="";
        byte[] content = str.getBytes();
        byte[] msg = new byte[3+content.length];
        msg[0]= code;
        msg[1]= (byte) (content.length>>8);
        msg[2]= (byte) (content.length&0xff);
        System.arraycopy(content,0,msg,3,content.length);
        channel.writeAndFlush(msg);
    }

    private class ProxyResponseHandler extends ChannelInboundHandlerAdapter{

        private int proxyId = -1;
        protected ProxyResponseHandler(int proxy_id){
            this.proxyId =proxy_id;
        }

        final Object lock = new Object();
        int resCode = -1;
        int length =-1;
        byte[] content;
        int idx = 0;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            proxyHandler.active(proxyId);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            byte[] bytes = (byte[]) msg;
            int lastLength;
            synchronized (lock) {
                if (resCode < 0) {
                    resCode = bytes[0] & 0xff;
                    length = ((bytes[1] & 0xff) << 8) | (bytes[2] & 0xff);
                    content = new byte[length];
                    lastLength = bytes.length - 3;
                    if (lastLength < length) {
                        System.arraycopy(bytes, 3, content, 0, lastLength);
                        idx = lastLength;
                    } else {
                        System.arraycopy(bytes, 3, content, 0, length);
                        receiveResponse(resCode,new String(content));
                        resCode = -1;
                    }
                } else {
                    lastLength = bytes.length;
                    if (lastLength < length - idx) {
                        System.arraycopy(bytes, 0, content, idx, lastLength);
                        idx += lastLength;
                    } else {
                        System.arraycopy(bytes, 0, content, idx, length - idx);
                        receiveResponse(resCode,new String(content));
                        resCode = -1;
                    }
                }
            }
            ReferenceCountUtil.release(msg);
        }

        private void receiveResponse(int resCode, String content){
            if(resCode==0x00){
                //Heartbeat Success.
            }
            else
                proxyHandler.receive(proxyId, resCode, content);
        }
    }

    protected static abstract class ResponseReceiver{
        protected abstract void active(int proxyId);
        protected abstract void receive(int proxyId, int resCode, String content);
    }

    protected static abstract class TrafficCntCycleHandler{
        protected  abstract void tick(int proxyId);
    }

}