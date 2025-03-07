/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.alan.clients.util;

import com.alan.clients.util.chat.ChatUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtils {
    public static String get(String u) {
        try {
            String line;
            URL url = new URL(u);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            connection.disconnect();
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getWithCookie(String u, String cookie) {
        try {
            String line;
            URL url = new URL(u);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Cookie", cookie);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            connection.disconnect();
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static void downloadFile(String url, String filepath) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            long progress = 0L;
            long totalLen = entity.getContentLength();
            long unit = totalLen / 100L;
            File file = new File(filepath);
            FileOutputStream fileout = new FileOutputStream(file);
            byte[] buffer = new byte[10240];
            int ch = 0;
            while ((ch = is.read(buffer)) != -1) {
                fileout.write(buffer, 0, ch);
                progress += (long)ch;
            }
            if (progress % 10L == 0L) {
                ChatUtil.info("Downloaded " + progress / unit + "%");
            }
            is.close();
            fileout.flush();
            fileout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] sendPostRequest(String targetUrl, String body, Map<String, String> headers) throws Exception {
        String inputLine;
        String[] response = new String[2];
        URL url = new URL(targetUrl);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        os.write(body.getBytes());
        os.flush();
        os.close();
        response[0] = Integer.toString(connection.getResponseCode());
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        connection.disconnect();
        response[1] = content.toString();
        return response;
    }

    public static void downloadAsync(String url, String filepath, Runnable callback) {
        new Thread(() -> {
            try {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet httpget = new HttpGet(url);
                HttpResponse response = client.execute(httpget);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                long progress = 0L;
                long totalLen = entity.getContentLength();
                long unit = totalLen / 100L;
                File file = new File(filepath);
                FileOutputStream fileout = new FileOutputStream(file);
                byte[] buffer = new byte[10240];
                int ch = 0;
                while ((ch = is.read(buffer)) != -1) {
                    fileout.write(buffer, 0, ch);
                    progress += (long)ch;
                }
                if (progress % 10L == 0L) {
                    ChatUtil.info("Downloaded " + progress / unit + "%");
                }
                is.close();
                fileout.flush();
                fileout.close();
                callback.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

