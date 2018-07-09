package com.dingqiqi.picassohttps;

import android.content.Context;

import com.dingqiqi.httpsutil.HttpsUtil;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class PicassoManager {

    private Picasso mPicasso;

    private static WeakReference<Context> mWeakReference;

    private ImageDownLoader mImageDownLoader;

    public ImageDownLoader getImageDownLoader() {
        return mImageDownLoader;
    }

    private PicassoManager() {
        if (mWeakReference != null && mWeakReference.get() != null) {

            mImageDownLoader = new ImageDownLoader() {
                @Override
                public boolean hostnameVerifier(String hostname, SSLSession session) {
                    return true;
                }

                @Override
                public SSLSocketFactory getSSLSocketFactory() {
                    return HttpsUtil.getInstance().getSocketFactory();
                }
            };

            mPicasso = new Picasso.Builder(mWeakReference.get())
                    .downloader(mImageDownLoader)
                    .build();

            Picasso.setSingletonInstance(mPicasso);
        }
    }

    public static PicassoManager getInstance(Context context) {
        if (mWeakReference == null) {
            mWeakReference = new WeakReference<>(context.getApplicationContext());
        }

        return PicassoManagerInstance.mInstance;
    }

    private static class PicassoManagerInstance {
        private static final PicassoManager mInstance = new PicassoManager();
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

}
