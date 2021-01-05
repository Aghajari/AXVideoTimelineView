# AXVideoTimelineView
<img src="https://github.com/Aghajari/AXVideoTimelineView/blob/master/AXVideoTimelineView.png" width=512 title="Screen">

AXVideoTimelineView - Video Cropper View - Android

- Customize Theme,Colors
- Auto load frames in background
- Play Line View
- set Max/Min value
- get cropped video duration

...

**Version 1.00**
**Deprecated**

## Listener
``` java
        void onLeftProgressChanged(float progress)
        void onRightProgressChanged(float progress)
        void onDurationChanged(long Duration)
        void onPlayProgressChanged(float progress)
        void onDraggingStateChanged(boolean isDragging)
```

## Attrs
```
        playLine : format=boolean
        videoPath : format=string
        color : format=color
        iconColor : format=color
        timelineColor : format=color
        minProgress : format=float
        maxProgress : format=float
        leftProgress : format=float
        rightProgress : format=float
        playProgress : format=float
        roundFrames : format=boolean
        roundRadius : format=dimension
```

## Example
```java
        AXVideoTimelineView  axVideoTimeline = findViewById(R.id.axView);
        axVideoTimeline.setVideoPath(File);
```

## Author
- Amir Hossein Aghajari
