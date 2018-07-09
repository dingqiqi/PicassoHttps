package com.dingqiqi.picassohttps;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.picasso.Downloader;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public abstract class ImageDownLoader implements Downloader {

    //请求参数
    private Map<String, String> mParams;

    //请求头
    private Map<String, String> mHeadParams;

    //超时时间
    private int mTimeOut = 30000;

    //请求方式
    private String mRequestMethod = "GET";

    public void setTimeOut(int mTimeOut) {
        this.mTimeOut = mTimeOut;
    }

    public void setRequestMethod(String requestMethod) {
        this.mRequestMethod = requestMethod;
    }

    public void setParams(Map<String, String> mParams) {
        this.mParams = mParams;
    }

    public void setHeadParams(Map<String, String> mHeadParams) {
        this.mHeadParams = mHeadParams;
    }

    public void setParams(Map<String, String> mParams, Map<String, String> mHeadParams) {
        this.mParams = mParams;
        this.mHeadParams = mHeadParams;
    }

    public ImageDownLoader() {
        setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return hostnameVerifier(hostname, session);
            }
        });

        setSSLSocketFactory(getSSLSocketFactory());
    }

    public void setHostnameVerifier(HostnameVerifier verifier) {
        HttpsURLConnection.setDefaultHostnameVerifier(verifier);
    }

    public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
    }

    @Override
    public Response load(Uri uri, int networkPolicy) throws IOException {
        URL mUrl = new URL(uri.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) mUrl.openConnection();


        if (mHeadParams != null) {
            for (String key : mHeadParams.keySet()) {
                urlConnection.setRequestProperty(key, mHeadParams.get(key));
            }
        }

        urlConnection.setRequestMethod(mRequestMethod);

        urlConnection.setConnectTimeout(mTimeOut);

        String params = "";
        if (mParams != null) {
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            params = new JSONObject(mParams).toString();
        }

        urlConnection.connect();

        if (!TextUtils.isEmpty(params)) {
            DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
            dos.writeUTF(params);
            dos.flush();
            dos.close();
        }

        int responseCode = urlConnection.getResponseCode();

        Log.i("ImageDownLoader", responseCode + "  " + urlConnection.getResponseMessage());

        if (responseCode >= 300) {
            throw new ResponseException(responseCode + " " + urlConnection.getResponseMessage(), networkPolicy,
                    responseCode);
        }

        InputStream inputStream = urlConnection.getInputStream();

        if (inputStream == null) {
            return null;
        }

        boolean fromCache = urlConnection.getUseCaches();


        return new Response(urlConnection.getInputStream(), fromCache, urlConnection.getContentLength());
    }

    @Override
    public void shutdown() {

    }

    public abstract boolean hostnameVerifier(String hostname, SSLSession session);

    public abstract SSLSocketFactory getSSLSocketFactory();

}
