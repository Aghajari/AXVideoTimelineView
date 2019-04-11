package com.aghajari.axvideotimelineview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * @author Amir Hossein Aghajari
 * @version 1.00
 *
 */
public class AXVideoTimelineView extends View{

    long videoLength;

    private boolean playEnabled=true;
    private Paint paint;
    private Paint paint2;
    private boolean pressedLeft;
    private boolean pressedRight;
    private boolean pressedPlay;
    private Drawable drawableLeft;
    private Drawable drawableRight;
    private Rect rect1;
    private Rect rect2;
    private RectF rect3 = new RectF();
    private int lastWidth;
    private float radius;


    //progress
    private float maxProgressDiff = 1.0f;
    private float minProgressDiff = 0.0f;
    private float progressLeft;
    private float progressRight = 1;
    private float playProgress = 0.5f;
    private float pressDx;

    //frames
    private boolean isRoundFrames;
    MediaMetadataRetriever mediaMetadataRetriever;
    public ArrayList<Bitmap> frames = new ArrayList<>();
    private AXFrameDecoder currentTask;
    private static final Object sync = new Object();
    AXFrameDecoderUtils utils;

    private AXTimelineViewListener listener;

    public AXVideoTimelineView(Context context) {
        super(context);
        init(null);
    }

    public AXVideoTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AXVideoTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private float density = 1;
    int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    private int getTimelineColor (int color){
        return Color.argb(127,Color.red(color),Color.green(color),Color.blue(color));
    }

    private void init(AttributeSet attrs){
        density = getContext().getResources().getDisplayMetrics().density;
        utils = new AXFrameDecoderUtils();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2 = new Paint();
        drawableLeft = getContext().getResources().getDrawable(R.drawable.video_cropleft);
        drawableRight = getContext().getResources().getDrawable(R.drawable.video_cropright);


        int color=Color.WHITE;
        int iconColor=Color.BLACK;
        int timelineColor = Color.BLACK;

        if (attrs!=null){
            TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.AXVideoTimelineView);
            color = a.getColor(R.styleable.AXVideoTimelineView_color,Color.WHITE);
            iconColor = a.getColor(R.styleable.AXVideoTimelineView_iconColor,Color.BLACK);
            timelineColor = a.getColor(R.styleable.AXVideoTimelineView_timelineColor,Color.BLACK);
            minProgressDiff = a.getFloat(R.styleable.AXVideoTimelineView_minProgress,0.0f);
            maxProgressDiff = a.getFloat(R.styleable.AXVideoTimelineView_minProgress,1.0f);
            progressRight = a.getFloat(R.styleable.AXVideoTimelineView_rightProgress,1.0f);
            progressLeft = a.getFloat(R.styleable.AXVideoTimelineView_leftProgress,0.0f);
            playProgress = a.getFloat(R.styleable.AXVideoTimelineView_playProgress,0.5f);
            playEnabled = a.getBoolean(R.styleable.AXVideoTimelineView_playLine,true);
            setRoundFrames(a.getBoolean(R.styleable.AXVideoTimelineView_roundFrames,false));
            radius = a.getDimension(R.styleable.AXVideoTimelineView_roundRadius,dp(2));

            paint.setColor(color);
            drawableRight.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
            drawableLeft.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
            paint2.setColor(getTimelineColor(timelineColor));

            String video = a.getString(R.styleable.AXVideoTimelineView_videoPath);
            if (video!=null && !video.isEmpty() && video.length()>2){
                setVideoPath(video);
            }

            a.recycle();
        }else {
            paint.setColor(color);
            paint2.setColor(getTimelineColor(timelineColor));
            drawableRight.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
            drawableLeft.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
            radius = dp(2);
        }

    }

    public void setPlayLineEnabled(boolean playEnabled) {
        this.playEnabled = playEnabled;
    }

    public boolean isPlayLineEnabled() {
        return playEnabled;
    }

    public void setIconColors(int Color){
        drawableLeft.setColorFilter(new PorterDuffColorFilter(Color, PorterDuff.Mode.MULTIPLY));
        drawableRight.setColorFilter(new PorterDuffColorFilter(Color, PorterDuff.Mode.MULTIPLY));
    }

    public void setTimelineColor(int Color){
        paint2.setColor(getTimelineColor(Color));
    }

    public float getPlayProgress() {
        return playProgress;
    }

    public float getLeftProgress() {
        return progressLeft;
    }

    public float getRightProgress() {
        return progressRight;
    }

    public void setMinProgressDiff(float value) {
        minProgressDiff = value;
    }

    public void setMaxProgressDiff(float value) {
        maxProgressDiff = value;
        if (progressRight - progressLeft > maxProgressDiff) {
            progressRight = progressLeft + maxProgressDiff;
            invalidate();
        }
    }

    public void setRoundFrames(boolean value) {
        isRoundFrames = value;
        if (isRoundFrames) {
            rect1 = new Rect(dp(14), dp(14), dp(14 + 28), dp(14 + 28));
            rect2 = new Rect();
        }
    }

    public boolean isRoundFrames(){
        return isRoundFrames;
    }

    public int getFrameWidth() {
        return utils.frameWidth;
    }

    public int getFrameHeight() {
        return  utils.frameHeight;
    }

    public long getFrameTimeOffset() {
        return  utils.frameTimeOffset;
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setListener(AXTimelineViewListener listener) {
        this.listener = listener;
    }

    public long getVideoDuration() { return videoLength; }
    public long getCroppedDuration(){
        float time = getRightProgress() - getLeftProgress();
        return  (long) (getVideoDuration()*time);
    }

    public boolean isPlayDragging() {
        return pressedPlay;
    }
    public boolean isLeftDragging() { return pressedLeft; }
    public boolean isRightDragging() { return pressedRight; }
    public boolean isDragging(){
        if (pressedPlay||pressedLeft||pressedRight) return true;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();

        int width = getMeasuredWidth() - dp(32);
        int startX = (int) (width * progressLeft) + dp(16);
        int playX = (int) (width * (progressLeft + (progressRight - progressLeft) * playProgress)) + dp(16);
        int endX = (int) (width * progressRight) + dp(16);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
            if (mediaMetadataRetriever == null) {
                return false;
            }
            int additionWidth = dp(12);
            int additionWidthPlay = dp(8);
            if (playEnabled&&playX - additionWidthPlay <= x && x <= playX + additionWidthPlay && y >= 0 && y <= getMeasuredHeight()) {
                pressedPlay = true;
                if (listener != null) listener.onDraggingStateChanged(true);
                pressDx = (int) (x - playX);
                invalidate();
                return true;
            } else if (startX - additionWidth <= x && x <= startX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedLeft = true;
                if (listener != null) listener.onDraggingStateChanged(true);
                pressDx = (int) (x - startX);
                invalidate();
                return true;
            } else if (endX - additionWidth <= x && x <= endX + additionWidth && y >= 0 && y <= getMeasuredHeight()) {
                pressedRight = true;
                if (listener != null) listener.onDraggingStateChanged(true);
                pressDx = (int) (x - endX);
                invalidate();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (pressedLeft) {
                pressedLeft = false;
                if (listener != null) listener.onDraggingStateChanged(false);
                return true;
            } else if (pressedRight) {
                pressedRight = false;
                if (listener != null) listener.onDraggingStateChanged(false);
                return true;
            } else if (pressedPlay) {
                pressedPlay = false;
                if (listener != null) listener.onDraggingStateChanged(false);
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressedPlay && playEnabled) {
                playX = (int) (x - pressDx);
                playProgress = (float) (playX - dp(16)) / (float) width;
                if (playProgress < progressLeft) {
                    playProgress = progressLeft;
                } else if (playProgress > progressRight) {
                    playProgress = progressRight;
                }
                playProgress = (playProgress - progressLeft) / (progressRight - progressLeft);
                if (listener != null) {
                    listener.onPlayProgressChanged(progressLeft + (progressRight - progressLeft) * playProgress);
                }
                invalidate();
                return true;
            } else if (pressedLeft) {
                startX = (int) (x - pressDx);
                if (startX < dp(16)) {
                    startX = dp(16);
                } else if (startX > endX) {
                    startX = endX;
                }
                progressLeft = (float) (startX - dp(16)) / (float) width;
                if (progressRight - progressLeft > maxProgressDiff) {
                    progressRight = progressLeft + maxProgressDiff;
                } else if (minProgressDiff != 0 && progressRight - progressLeft < minProgressDiff) {
                    progressLeft = progressRight - minProgressDiff;
                    if (progressLeft < 0) {
                        progressLeft = 0;
                    }
                }
                if (listener != null) {
                    listener.onLeftProgressChanged(progressLeft);
                    listener.onDurationChanged(getCroppedDuration());
                }
                invalidate();
                return true;
            } else if (pressedRight) {
                endX = (int) (x - pressDx);
                if (endX < startX) {
                    endX = startX;
                } else if (endX > width + dp(16)) {
                    endX = width + dp(16);
                }
                progressRight = (float) (endX - dp(16)) / (float) width;
                if (progressRight - progressLeft > maxProgressDiff) {
                    progressLeft = progressRight - maxProgressDiff;
                } else if (minProgressDiff != 0 && progressRight - progressLeft < minProgressDiff) {
                    progressRight = progressLeft + minProgressDiff;
                    if (progressRight > 1.0f) {
                        progressRight = 1.0f;
                    }
                }
                if (listener != null) {
                    listener.onRightProgressChanged(progressRight);
                    listener.onDurationChanged(getCroppedDuration());
                }
                invalidate();
                return true;
            }
        }
        return false;
    }


    public void setVideoPath(File file) {
        destroy();
        mediaMetadataRetriever = new MediaMetadataRetriever();
        progressLeft = 0.0f;
        progressRight = 1.0f;
        try {
            FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());
            mediaMetadataRetriever.setDataSource(inputStream.getFD());
            String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoLength = Long.parseLong(duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        invalidate();
    }

    public void setVideoPath(String path) {
        setVideoPath(new File(path));
    }

    private void reloadFrames(int frameNum) {
        if (mediaMetadataRetriever == null) {
            return;
        }
        if (frameNum == 0) {
            utils.load(this);
        }
        currentTask = new AXFrameDecoder(this,
                new AXFrameDecoder.AXFrameDecoderListener() {
            @Override
            public void frameDecoded(Bitmap frame, int frameNumber) {
                frames.add(frame);
                invalidate();
                if (frameNumber < utils.framesToLoad) {
                    reloadFrames(frameNumber + 1);
                }
            }
        });
        currentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, frameNum, null, null);
    }

    public void reloadFrames(){
        reloadFrames(0);
    }

    public void destroy() {
        synchronized (sync) {
            try {
                if (mediaMetadataRetriever != null) {
                    mediaMetadataRetriever.release();
                    mediaMetadataRetriever = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        clearListFrames();
    }

    public void setProgress(float value) {
        if (!playEnabled) return;
        playProgress = value;
        invalidate();
    }

    public void clearFrames() {
        clearListFrames();
        invalidate();
    }

    private void clearListFrames() {
        for (int a = 0; a < frames.size(); a++) {
            Bitmap bitmap = frames.get(a);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        frames.clear();
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (lastWidth != widthSize) {
            clearFrames();
            lastWidth = widthSize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth() - dp(36);
        int startX = (int) (width * progressLeft) + dp(16);
        int endX = (int) (width * progressRight) + dp(16);

        canvas.save();
        canvas.clipRect(dp(16), dp(4), width + dp(20), dp(48));
        if (frames.isEmpty() && currentTask == null) {
            reloadFrames(0);
        } else {
            int offset = 0;
            for (int a = 0; a < frames.size(); a++) {
                Bitmap bitmap = frames.get(a);
                if (bitmap != null) {
                    int x = dp(16) + offset * (isRoundFrames ? utils.frameWidth / 2 : utils.frameWidth);
                    int y = dp(2 + 4);
                    if (isRoundFrames) {
                        rect2.set(x, y, x + dp(28), y + dp(28));
                        canvas.drawBitmap(bitmap, rect1, rect2, null);
                    } else {
                        canvas.drawBitmap(bitmap, x, y, null);
                    }
                }
                offset++;
            }
        }

        int top = dp(6);
        int end = dp(48);

        canvas.drawRect(dp(16), top, startX, dp(46), paint2);
        canvas.drawRect(endX + dp(4), top, dp(16) + width + dp(4), dp(46), paint2);

        canvas.drawRect(startX, dp(4), startX + dp(2), end, paint);
        canvas.drawRect(endX + dp(2), dp(4), endX + dp(4), end, paint);
        canvas.drawRect(startX + dp(2), dp(4), endX + dp(4), top, paint);
        canvas.drawRect(startX + dp(2), end - dp(2), endX + dp(4), end, paint);
        canvas.restore();

        rect3.set(startX - dp(8), dp(4), startX + dp(2), end);
        canvas.drawRoundRect(rect3, radius, radius, paint);
        drawableLeft.setBounds(startX - dp(8), dp(4) + (dp(44) - dp(18)) / 2, startX + dp(2), (dp(44) - dp(18)) / 2 + dp(18 + 4));
        drawableLeft.draw(canvas);

        rect3.set(endX + dp(2), dp(4), endX + dp(12), end);
        canvas.drawRoundRect(rect3, radius, radius, paint);
        drawableRight.setBounds(endX + dp(2), dp(4) + (dp(44) - dp(18)) / 2, endX + dp(12), (dp(44) - dp(18)) / 2 + dp(18 + 4));
        drawableRight.draw(canvas);

        if (playEnabled) {
            float cx = dp(18) + width * (progressLeft + (progressRight - progressLeft) * playProgress);
            rect3.set(cx - dp(1.5f), dp(2), cx + dp(1.5f), dp(50));
            canvas.drawRoundRect(rect3, dp(1), dp(1), paint2);
            canvas.drawCircle(cx, dp(52), dp(3.5f), paint2);

            rect3.set(cx - dp(1), dp(2), cx + dp(1), dp(50));
            canvas.drawRoundRect(rect3, dp(1), dp(1), paint);
            canvas.drawCircle(cx, dp(52), dp(3), paint);
        }
    }

}
