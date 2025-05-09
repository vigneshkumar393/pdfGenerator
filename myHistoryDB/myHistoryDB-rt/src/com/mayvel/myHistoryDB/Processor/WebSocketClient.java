package com.mayvel.myHistoryDB.Processor;

import com.mayvel.myHistoryDB.Const.Consts;
import com.mayvel.myHistoryDB.utils.Generic;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class WebSocketClient {
    public static void clientSideWebsocketRealtime(String apiUrl,String json,String PORT,String endpoint){
        Thread sendToSocker = new Thread(()->{
           try{
               LocalDateTime now = LocalDateTime.now();
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
               String formattedDateTime = now.format(formatter);
               System.out.println(formattedDateTime+": "+ json);
               sendToSocketWithChannel(apiUrl,json,PORT,endpoint);
           }catch (Exception e){
               e.printStackTrace();
           }
        });
        sendToSocker.start();
    }

    private static void sendToSocketWithChannel(String apiUrl, String json, String PORT,String endpoint){
        try{
            TrustManager[] trustAllCertificates = new TrustManager[]{
              new X509TrustManager() {
                  @Override
                  public X509Certificate[] getAcceptedIssuers(){return null;}
                  @Override
                  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                  }
                  @Override
                  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                  }
              }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null,trustAllCertificates,new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(apiUrl,Integer.parseInt(PORT));
            PrintWriter out = Generic.buildHeader(sslSocket,true, Consts.host,String.valueOf(Consts.port),endpoint);
            if(out == null) throw new Exception("Failed in websocket headers");
            out.flush();
            sendWebSocketFrame(sslSocket.getOutputStream(),json);
            // Optionally, add a delay or a mechanism to know when to close the connection
            Thread.sleep(1000);
            sslSocket.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private static void sendWebSocketFrame(OutputStream out, String message) throws IOException{
        byte[] data = message.getBytes();
        out.write(0x81); // 0x81 means this is a final frame, text payload with the MASK bit set

        if (data.length <= 65535) {
            out.write(126 | 0x80); // Setting the MASK bit to 1 for medium payloads
            out.write((data.length >> 8) & 0xFF);
            out.write(data.length & 0xFF);
        } else {
            out.write(127 | 0x80); // Setting the MASK bit to 1 for large payloads
            out.write((data.length >> 56) & 0xFF);
            out.write((data.length >> 48) & 0xFF);
            out.write((data.length >> 40) & 0xFF);
            out.write((data.length >> 32) & 0xFF);
            out.write((data.length >> 24) & 0xFF);
            out.write((data.length >> 16) & 0xFF);
            out.write((data.length >> 8) & 0xFF);
            out.write(data.length & 0xFF);
        }

        // Generate a random mask key (4 bytes)
        byte[] maskKey = new byte[4];
        new Random().nextBytes(maskKey);
        out.write(maskKey); // Write the mask key to the frame

        // Mask the data using the mask key
        for (int i = 0; i < data.length; i++) {
            data[i] ^= maskKey[i % 4];
        }

        // Write the masked data to the frame
        out.write(data);
        out.flush();
    }
}
