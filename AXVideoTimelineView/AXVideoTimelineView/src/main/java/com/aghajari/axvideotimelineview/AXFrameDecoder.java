package com.aghajari.axvideotimelineview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

class AXFrameDecoder extends AsyncTask<Integer, Integer, Bitmap> {

    interface AXFrameDecoderListener{
         void frameDecoded(Bitmap frame, int frameNumber);
    }

    AXFrameDecoder(AXVideoTimelineView view,AXFrameDecoderListener listener){
    this.mediaMetadataRetriever = view.mediaMetadataRetriever;
    this.listener = listener;
    this.utils = view.utils;
    }

    private AXFrameDecoderUtils utils;
    private AXFrameDecoderListener listener;
    private MediaMetadataRetriever mediaMetadataRetriever;

    int frameNum;

    @Override
    protected Bitmap doInBackground(Integer... objects) {
        frameNum = objects[0];
        Bitmap bitmap = null;
        if (isCancelled()) {
            return null;
        }
        try {
            bitmap = mediaMetadataRetriever.getFrameAtTime(utils.frameTimeOffset * frameNum * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (isCancelled()) {
                return null;
            }
            if (bitmap != null) {
                bitmap = utils.prepareFrame(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (!isCancelled() && listener!=null) {
            listener.frameDecoded(bitmap,frameNum);
        }
    }

}
