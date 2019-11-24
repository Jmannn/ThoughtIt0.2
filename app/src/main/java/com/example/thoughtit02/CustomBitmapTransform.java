package com.example.thoughtit02;

import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.Charset;
import java.security.MessageDigest;

public class CustomBitmapTransform extends BitmapTransformation {
    private static final String ID = "com.bumptech.glide.transformations.FillSpace";
    private static final byte[] ID_BYTES = ID.getBytes(Charset.forName("UTF-8"));
    private int maxHeight;

    public CustomBitmapTransform(int maxHeight) {
        this.maxHeight = maxHeight;
    }
    @Override
    public Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        if (toTransform.getWidth() < outWidth && toTransform.getHeight() < maxHeight) {
            return toTransform;
        }

        return Bitmap.createScaledBitmap(toTransform, outWidth, maxHeight, /*filter=*/ true);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CustomBitmapTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
