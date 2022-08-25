package com.sidorov.culturalvillageveshenskaya.Other;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.Random;

public class ProgressIndicator {

    private View view;
    private int width;
    private int[] colors;
    private ValueAnimator valueAnimator;

    public ProgressIndicator(View view, int width) {
        this.view = view;
        this.width = width;
    }

    public void startAnimation() {
        colors = new int[]{
                Color.rgb(255, 90, 90),
                Color.rgb(255, 172, 90),
                Color.rgb(255, 255, 90),
                Color.rgb(172, 255, 90),
                Color.rgb(90, 255, 90)
        };

        int[] colorPixels = new int[width];
        int step = width / 4;
        for (int i = 0; i < width; i++) {
            int red, green, blue = 90;
            if (i <= step) {
                red = 255;
                green = 90 + 165 * i / step;
            } else if (i <= step * 2) {
                red = 255 - 165 * (i - step) / step;
                green = 255;
            } else if (i <= step * 3) {
                red = 90 + 165 * (i - step * 2) / step;
                green = 255;
            } else {
                red = 255;
                green = 255 - 165 * (i - step * 3) / step;
            }
            colorPixels[i] = Color.rgb(red, green, blue);
        }

        int index = 0;
        valueAnimator = ValueAnimator.ofInt(index, 100000);
        valueAnimator.addUpdateListener(animation -> {
            int val = (Integer) animation.getAnimatedValue();
            int[] pixels = new int[width];
            for (int i = 0; i < width; i++) {
                pixels[i] = colorPixels[(i + val) % width];
            }

            Bitmap bitmap = Bitmap.createBitmap(width, 1, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, 1);
            ((ImageView) view).setImageBitmap(bitmap);
        });
        valueAnimator.setDuration(60000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }

    public void stopAnimation() {
        valueAnimator.cancel();
        ((ImageView) view).setImageResource(0);
    }
}
