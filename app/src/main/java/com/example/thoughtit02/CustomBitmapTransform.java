package com.example.thoughtit02;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.nio.charset.Charset;
import java.security.MessageDigest;
/* This class takes a bitmap image. Formats it for use with glide
 * @author Johnny Mann
 */
public class CustomBitmapTransform extends BitmapTransformation {
    /* For hash code. */
    private static final String ID = "com.bumptech.glide.transformations.FillSpace";
    /* For message digest key. */
    private static final byte[] ID_BYTES = ID.getBytes(Charset.forName("UTF-8"));
    /* This is the max height for the image. */
    private int maxHeight;

    CustomBitmapTransform(int maxHeight) {
        this.maxHeight = maxHeight;
    }
    /* Forces image to be less than maxHeight.
     * @param bitmap pool
     * @param the not yet transformed bitmap
     * @param width
     * @param height
     * @return the height conforming bitmap
     */
    @Override
    public Bitmap transform(@NonNull BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        if (toTransform.getWidth() < outWidth && toTransform.getHeight() < maxHeight) {
            return toTransform;
        }
        return Bitmap.createScaledBitmap(toTransform, outWidth, maxHeight,true);
    }
    /* For checking object equality.
     * @param object to confirm
     * @return true if same
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof CustomBitmapTransform;
    }
    /* A hashcode based on a set string ID
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return ID.hashCode();
    }
    /* Updates the key used to cache the image
     * @param hash function
     */
    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
