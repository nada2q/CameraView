package com.flurgle.camerakit.utils;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class YuvUtils {

    public static byte[] createRGB(Image image, @Nullable Rect crop) {
        byte[] data = getYUVData(image);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        if (crop == null) {
            crop = new Rect(0, 0, image.getWidth(), image.getHeight());
        }

        yuvImage.compressToJpeg(crop, 50, out);

        return out.toByteArray();
    }

    public static byte[] getYUVData(Image image) {
        ByteBuffer bufferY = image.getPlanes()[0].getBuffer();
        byte[] y = new byte[bufferY.remaining()];
        bufferY.get(y);

        ByteBuffer bufferU = image.getPlanes()[1].getBuffer();
        byte[] u = new byte[bufferU.remaining()];
        bufferU.get(u);

        ByteBuffer bufferV = image.getPlanes()[2].getBuffer();
        byte[] v = new byte[bufferV.remaining()];
        bufferV.get(v);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(y);
            outputStream.write(v);
            outputStream.write(u);
        } catch (IOException e) {

        }
        return outputStream.toByteArray();
    }

    public static byte[] rotateNV21(final byte[] yuv, final int width, final int height, final int rotation) {
        if (rotation == 0) return yuv;
        if (rotation % 90 != 0 || rotation < 0 || rotation > 270) {
            throw new IllegalArgumentException("0 <= rotation < 360, rotation % 90 == 0");
        }

        final byte[] output = new byte[yuv.length];
        final int frameSize = width * height;
        final boolean swap = rotation % 180 != 0;
        final boolean xflip = rotation % 270 != 0;
        final boolean yflip = rotation >= 180;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                final int yIn = j * width + i;
                final int uIn = frameSize + (j >> 1) * width + (i & ~1);
                final int vIn = uIn + 1;

                final int wOut = swap ? height : width;
                final int hOut = swap ? width : height;
                final int iSwapped = swap ? j : i;
                final int jSwapped = swap ? i : j;
                final int iOut = xflip ? wOut - iSwapped - 1 : iSwapped;
                final int jOut = yflip ? hOut - jSwapped - 1 : jSwapped;

                final int yOut = jOut * wOut + iOut;
                final int uOut = frameSize + (jOut >> 1) * wOut + (iOut & ~1);
                final int vOut = uOut + 1;

                output[yOut] = (byte) (0xff & yuv[yIn]);
                output[uOut] = (byte) (0xff & yuv[uIn]);
                output[vOut] = (byte) (0xff & yuv[vIn]);
            }
        }
        return output;
    }

}

