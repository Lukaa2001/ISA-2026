import React, { useEffect, useState, useRef } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import { Typography, Tag, Spin, Divider, Space, Alert } from 'antd';
import { EyeOutlined, UserOutlined, CalendarOutlined, TeamOutlined } from '@ant-design/icons';
import { io, Socket } from 'socket.io-client';
import VideoPlayer, { VideoPlayerHandle } from '../components/Video/VideoPlayer';
import CommentList from '../components/Comment/CommentList';
import CommentForm from '../components/Comment/CommentForm';
import LoginPrompt from '../components/Auth/LoginPrompt';
import { videoApi, Video } from '../api/video.api';
import { useAuth } from '../context/AuthContext';

const { Title, Text, Paragraph } = Typography;

interface LocationState {
  watchPartyStartAt?: number;
  watchPartyRoomCode?: string;
}

const VideoPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const [video, setVideo] = useState<Video | null>(null);
  const [loading, setLoading] = useState(true);
  const [commentRefresh, setCommentRefresh] = useState(0);
  const [countdown, setCountdown] = useState<number | null>(null);
  const { user, token } = useAuth();
  const videoRef = useRef<VideoPlayerHandle>(null);
  const socketRef = useRef<Socket | null>(null);
  const isSyncingRef = useRef(false);

  const locationState = location.state as LocationState | null;
  const watchPartyStartAt = locationState?.watchPartyStartAt;
  const watchPartyRoomCode = locationState?.watchPartyRoomCode;

  // Countdown timer for watch party
  useEffect(() => {
    if (!watchPartyStartAt) return;

    const updateCountdown = () => {
      const remaining = Math.ceil((watchPartyStartAt - Date.now()) / 1000);
      if (remaining > 0) {
        setCountdown(remaining);
      } else {
        setCountdown(null);
      }
    };

    updateCountdown();
    const interval = setInterval(updateCountdown, 100);
    return () => clearInterval(interval);
  }, [watchPartyStartAt]);

  // Connect to socket for watch party sync
  useEffect(() => {
    if (!watchPartyRoomCode || !token) return;

    const apiUrl = import.meta.env.VITE_API_URL || '';
    const socketUrl = import.meta.env.VITE_SOCKET_URL || apiUrl || window.location.origin;
    const socket = io(socketUrl, {
      auth: { token },
      query: { token },
    });

    socket.on('connect', () => {
      socket.emit('join-room', watchPartyRoomCode);
    });

    socket.on('sync-video', (data: { action: 'play' | 'pause' | 'seek'; currentTime?: number }) => {
      isSyncingRef.current = true;
      if (data.action === 'play') {
        videoRef.current?.play();
      } else if (data.action === 'pause') {
        videoRef.current?.pause();
      } else if (data.action === 'seek' && data.currentTime !== undefined) {
        videoRef.current?.seek(data.currentTime);
      }
      setTimeout(() => { isSyncingRef.current = false; }, 100);
    });

    socketRef.current = socket;

    return () => {
      socket.disconnect();
    };
  }, [watchPartyRoomCode, token]);

  const handlePlay = () => {
    if (watchPartyRoomCode && socketRef.current && !isSyncingRef.current) {
      socketRef.current.emit('sync-video', {
        roomCode: watchPartyRoomCode,
        action: 'play',
      });
    }
  };

  const handlePause = () => {
    if (watchPartyRoomCode && socketRef.current && !isSyncingRef.current) {
      socketRef.current.emit('sync-video', {
        roomCode: watchPartyRoomCode,
        action: 'pause',
        currentTime: videoRef.current?.getCurrentTime(),
      });
    }
  };

  const handleSeeked = (currentTime: number) => {
    if (watchPartyRoomCode && socketRef.current && !isSyncingRef.current) {
      socketRef.current.emit('sync-video', {
        roomCode: watchPartyRoomCode,
        action: 'seek',
        currentTime,
      });
    }
  };

  useEffect(() => {
    if (id) {
      loadVideo(parseInt(id));
    }
  }, [id]);

  const loadVideo = async (videoId: number) => {
    setLoading(true);
    try {
      const res = await videoApi.getById(videoId);
      setVideo(res.data);
    } catch {
      // Error handled silently
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!video) {
    return <div style={{ padding: 24 }}>Video nije pronađen</div>;
  }

  return (
    <div style={{ padding: 24, maxWidth: 900, margin: '0 auto' }}>
      {watchPartyRoomCode && (
        <Alert
          message={<><TeamOutlined /> Watch Party Mode</>}
          description={countdown !== null ? `Video počinje za ${countdown}s...` : 'Video je sinkroniziran s ostalim članovima'}
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      <VideoPlayer
        ref={videoRef}
        videoPath={video.videoPath}
        autoPlayAt={watchPartyStartAt}
        onPlay={handlePlay}
        onPause={handlePause}
        onSeeked={handleSeeked}
      />

      <Title level={3} style={{ marginTop: 16 }}>{video.title}</Title>

      <Space size="large">
        <Text>
          <EyeOutlined /> {video.viewCount} pregleda
        </Text>
        <Text>
          <UserOutlined />{' '}
          <Link to={`/profile/${video.user.id}`}>{video.user.username}</Link>
        </Text>
        <Text>
          <CalendarOutlined /> {new Date(video.createdAt).toLocaleDateString('hr-HR')}
        </Text>
      </Space>

      <div style={{ marginTop: 12 }}>
        {video.tags?.map((tag) => (
          <Tag key={tag} color="red">{tag}</Tag>
        ))}
      </div>

      <Paragraph style={{ marginTop: 16 }}>{video.description}</Paragraph>

      {video.latitude && video.longitude && (
        <Text type="secondary">
          Lokacija: {video.latitude.toFixed(4)}, {video.longitude.toFixed(4)}
        </Text>
      )}

      <Divider />

      {user ? (
        <CommentForm videoId={video.id} onCommentAdded={() => setCommentRefresh((r) => r + 1)} />
      ) : (
        <LoginPrompt />
      )}

      <div style={{ marginTop: 24 }}>
        <CommentList videoId={video.id} refreshKey={commentRefresh} />
      </div>
    </div>
  );
};

export default VideoPage;
