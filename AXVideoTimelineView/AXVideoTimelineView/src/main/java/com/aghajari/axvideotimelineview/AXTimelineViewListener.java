package com.aghajari.axvideotimelineview;

public interface AXTimelineViewListener {
        void onLeftProgressChanged(float progress);
        void onRightProgressChanged(float progress);
        void onDurationChanged(long Duration);
        void onPlayProgressChanged(float progress);
        void onDraggingStateChanged(boolean isDragging);
    }