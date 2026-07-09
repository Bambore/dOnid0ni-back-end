package com.donidoni.auth.sms;

import com.donidoni.auth.otp.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implémentation du service SMS utilisant l'API LeSmsBus (gov.bf.utils logic).
 */
@Slf4j
@Primary
@Service
public class LesmsbusSmsService implements SmsService {

    private static final String SMS_API_URL = "https://www.lesmsbus.com:7170/ines.smsbus/smsbusMt";
    private static final String SMS_ID = "12E956RUVA892138UIO";
    private static final String SMS_SENDER_ID = "TRPOST";

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("[SMS-BUS] Envoi d'un SMS à {}...", maskPhone(phoneNumber));
        Map<String, String> params = new HashMap<>();
        params.put("id", SMS_ID);
        params.put("from", SMS_SENDER_ID);
        params.put("to", phoneNumber);
        params.put("text", message);
        
        String response = read(SMS_API_URL, "POST", params, 60000);
        log.info("[SMS-BUS] Réponse de l'API : {}", response);
    }

    private String read(String URL, String method, Map<String, String> params, int httpConnectionTimeOut) {
        HttpURLConnection ucon = null;
        try {
            URL url = null;
            if (URL.contains("https")) {
                TrustManager[] tms = { new SmsbusTrustManager() };
                SSLContext sslctx2 = SSLContext.getInstance("SSL");
                sslctx2.init(null, tms, null);
                HttpsURLConnection.setDefaultSSLSocketFactory(sslctx2.getSocketFactory());

                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String string, SSLSession ssls) {
                        return true;
                    }
                });
            }

            if (method == null || method.trim().equalsIgnoreCase("GET")) {
                String paramStr = null;
                if (params != null) {
                    Iterator<String> it = params.keySet().iterator();
                    while (it.hasNext()) {
                        String param = it.next();
                        String value = params.get(param);
                        if (paramStr == null) {
                            paramStr = param + "=" + URLEncoder.encode(value, "UTF-8");
                        } else {
                            paramStr = paramStr + "&" + param + "=" + URLEncoder.encode(value, "UTF-8");
                        }
                    }
                }
                url = new URL(URL + (paramStr != null ? "?" + paramStr : ""));
                ucon = (HttpURLConnection) url.openConnection();
                if (httpConnectionTimeOut > 0) {
                    ucon.setConnectTimeout(httpConnectionTimeOut);
                    ucon.setReadTimeout(httpConnectionTimeOut);
                }
                ucon.setRequestMethod("GET");
                ucon.setDoOutput(true);
            } else {
                url = new URL(URL);
                ucon = (HttpURLConnection) url.openConnection();
                ucon.setRequestMethod("POST");
                ucon.setDoOutput(true);
                if (httpConnectionTimeOut > 0) {
                    ucon.setConnectTimeout(httpConnectionTimeOut);
                    ucon.setReadTimeout(httpConnectionTimeOut);
                }

                if (params != null) {
                    String paramStr = null;
                    Iterator<String> it = params.keySet().iterator();
                    while (it.hasNext()) {
                        String param = it.next();
                        String value = params.get(param);
                        if (paramStr == null) {
                            paramStr = param + "=" + URLEncoder.encode(value, "UTF-8");
                        } else {
                            paramStr = paramStr + "&" + param + "=" + URLEncoder.encode(value, "UTF-8");
                        }
                    }

                    if (paramStr != null) {
                        try (PrintWriter pw = new PrintWriter(ucon.getOutputStream())) {
                            pw.print(paramStr);
                            pw.flush();
                        }
                    }
                }
            }
            ucon.connect();
            String str = null;
            StringBuilder response = new StringBuilder("");

            InputStream ins = ucon.getInputStream();
            try (InputStreamReader istr = new InputStreamReader(ins, "UTF-8")) {
                BufferedReader br = new BufferedReader(istr);
                while ((str = br.readLine()) != null) {
                    response.append(str);
                }
            }
            return response.toString();

        } catch (Exception e) {
            log.error("[SMS-BUS] Erreur lors de l'appel à l'API", e);
            return null;
        } finally {
            try {
                if (ucon != null) {
                    ucon.disconnect();
                }
            } catch (Exception e) {
            }
        }
    }

    static class SmsbusTrustManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    private static String maskPhone(final String phone) {
        if (phone == null || phone.length() < 8) {
            return "****";
        }
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 4);
    }
}
