import React, { forwardRef, useImperativeHandle, useRef, useEffect } from 'react';
import { videoApi } from '../../api/video.api';

interface VideoPlayerProps {
  videoPath: string;
  autoPlayAt?: number; // Unix timestamp in ms when video should start
  onPlay?: () => void;
  onPause?: () => void;
  onSeeked?: (currentTime: number) => void;
}

export interface VideoPlayerHandle {
  play: () => void;
  pause: () => void;
  seek: (time: number) => void;
  getCurrentTime: () => number;
}

const VideoPlayer = forwardRef<VideoPlayerHandle, VideoPlayerProps>(
  ({ videoPath, autoPlayAt, onPlay, onPause, onSeeked }, ref) => {
    const videoRef = useRef<HTMLVideoElement>(null);

    useImperativeHandle(ref, () => ({
      play: () => videoRef.current?.play(),
      pause: () => videoRef.current?.pause(),
      seek: (time: number) => {
        if (videoRef.current) {
          videoRef.current.currentTime = time;
        }
      },
      getCurrentTime: () => videoRef.current?.currentTime ?? 0,
    }));

    // Auto-play at scheduled time for watch party sync
    useEffect(() => {
      if (!autoPlayAt || !videoRef.current) return;

      const delay = autoPlayAt - Date.now();
      if (delay <= 0) {
        // Start immediately if time has passed
        videoRef.current.play();
        return;
      }

      const timer = setTimeout(() => {
        videoRef.current?.play();
      }, delay);

      return () => clearTimeout(timer);
    }, [autoPlayAt]);

    return (
      <video
        ref={videoRef}
        controls
        style={{ width: '100%', maxHeight: '70vh', backgroundColor: '#000' }}
        src={videoApi.getVideoStreamUrl(videoPath)}
        onPlay={onPlay}
        onPause={onPause}
        onSeeked={() => onSeeked?.(videoRef.current?.currentTime ?? 0)}
      >
        Your browser does not support the video tag.
      </video>
    );
  }
);

VideoPlayer.displayName = 'VideoPlayer';

export default VideoPlayer;
