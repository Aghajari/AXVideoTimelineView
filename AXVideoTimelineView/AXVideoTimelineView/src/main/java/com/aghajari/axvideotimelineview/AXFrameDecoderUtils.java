package com.aghajari.axvideotimelineview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

class AXFrameDecoderUtils {

    long frameTimeOffset;
    int frameWidth;
    int frameHeight;
    int framesToLoad;

    public void load(AXVideoTimelineView view){
        if (view.isRoundFrames()) {
            frameHeight = frameWidth = view.dp(56);
            framesToLoad = (int) Math.ceil((view.getMeasuredWidth() - view.dp(16)) / (frameHeight / 2.0f));
        } else {
            frameHeight = view.dp(40);
            framesToLoad = (view.getMeasuredWidth() - view.dp(16)) / frameHeight;
            frameWidth = (int) Math.ceil((float) (view.getMeasuredWidth() - view.dp(16)) / (float) framesToLoad);
        }
        frameTimeOffset = view.videoLength / framesToLoad;
    }

    public Bitmap prepareFrame (Bitmap bitmap){
        Bitmap result = Bitmap.createBitmap(frameWidth,frameHeight, bitmap.getConfig());
        Canvas canvas = new Canvas(result);
        float scaleX = (float) frameWidth / (float) bitmap.getWidth();
        float scaleY = (float) frameHeight / (float) bitmap.getHeight();
        float scale = scaleX > scaleY ? scaleX : scaleY;
        int w = (int) (bitmap.getWidth() * scale);
        int h = (int) (bitmap.getHeight() * scale);
        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect destRect = new Rect((frameWidth - w) / 2, (frameHeight - h) / 2, w, h);
        canvas.drawBitmap(bitmap, srcRect, destRect, null);
        bitmap.recycle();
        return result;
    }

}
