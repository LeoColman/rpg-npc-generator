package me.kerooker.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageFormatter {

    public static Bitmap getBitmap(String bits) {
        byte[] b = Base64.decode(bits, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }

    public static String convertToString(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] b = stream.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
