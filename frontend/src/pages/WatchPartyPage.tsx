import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Typography, Button, Input, Card, List, message, Space, Divider, Tag } from 'antd';
import { TeamOutlined, PlayCircleOutlined } from '@ant-design/icons';
import { io, Socket } from 'socket.io-client';
import { watchPartyApi, WatchParty } from '../api/watchParty.api';
import { videoApi, Video } from '../api/video.api';
import { useAuth } from '../context/AuthContext';

const { Title, Text } = Typography;

const WatchPartyPage: React.FC = () => {
  const { token, user } = useAuth();
  const navigate = useNavigate();
  const [socket, setSocket] = useState<Socket | null>(null);
  const [party, setParty] = useState<WatchParty | null>(null);
  const [joinCode, setJoinCode] = useState('');
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(false);

  const [serverTimeOffset, setServerTimeOffset] = useState(0);

  // Connect socket
  useEffect(() => {
    if (!token) return;

    const apiUrl = import.meta.env.VITE_API_URL || '';
    const socketUrl = import.meta.env.VITE_SOCKET_URL || apiUrl || window.location.origin;
    const s = io(socketUrl, {
      auth: { token },
      query: { token },
    });

    s.on('connect', () => {
      console.log('Socket connected');
      // Sync time with server
      const clientTime = Date.now();
      s.emit('time-sync', (serverTime: number) => {
        const offset = serverTime - clientTime - (Date.now() - clientTime) / 2;
        setServerTimeOffset(offset);
        console.log('Server time offset:', offset);
      });
    });

    s.on('play-video', (data: { videoId: number; serverTime: number; startAt: number }) => {
      // Calculate local startAt using server time offset - use the data.serverTime directly
      const localStartAt = data.startAt;
      setParty((currentParty) => {
        navigate(`/video/${data.videoId}`, { 
          state: { 
            watchPartyStartAt: localStartAt,
            watchPartyRoomCode: currentParty?.roomCode 
          } 
        });
        return currentParty;
      });
    });

    s.on('user-joined', (data: { userId: number }) => {
      message.info(`Korisnik ${data.userId} se pridružio`);
      // Refresh party info
      setParty((currentParty) => {
        if (currentParty?.roomCode) {
          watchPartyApi.getRoom(currentParty.roomCode).then((res) => setParty(res.data)).catch(() => {});
        }
        return currentParty;
      });
    });

    s.on('user-left', (data: { userId: number }) => {
      message.info(`Korisnik ${data.userId} je napustio sobu`);
    });

    setSocket(s);

    return () => {
      s.disconnect();
    };
  }, [token, navigate]);

  // Load videos for selection
  useEffect(() => {
    videoApi.list(1, 50).then((res) => setVideos(res.data.videos)).catch(() => {});
  }, []);

  // Re-subscribe to user-joined when party changes
  useEffect(() => {
    if (!socket || !party) return;

    const handler = () => {
      watchPartyApi.getRoom(party.roomCode).then((res) => setParty(res.data)).catch(() => {});
    };

    socket.on('user-joined', handler);
    return () => {
      socket.off('user-joined', handler);
    };
  }, [socket, party?.roomCode]);

  const handleCreate = async () => {
    setLoading(true);
    try {
      const res = await watchPartyApi.create();
      setParty(res.data);
      socket?.emit('join-room', res.data.roomCode);
      message.success(`Soba kreirana! Kod: ${res.data.roomCode}`);
    } catch {
      message.error('Greška pri kreiranju sobe');
    } finally {
      setLoading(false);
    }
  };

  const handleJoin = async () => {
    if (!joinCode.trim()) return;
    setLoading(true);
    try {
      const res = await watchPartyApi.join(joinCode.trim());
      setParty(res.data);
      socket?.emit('join-room', res.data.roomCode);
      message.success('Pridruženi sobi!');
    } catch {
      message.error('Soba nije pronađena');
    } finally {
      setLoading(false);
    }
  };

  const handlePlayVideo = (videoId: number, videoTitle: string) => {
    if (!party || !socket) return;
    socket.emit('play-video', {
      roomCode: party.roomCode,
      videoId,
      videoTitle,
    });
  };

  const handleClose = async () => {
    if (!party) return;
    try {
      await watchPartyApi.close(party.roomCode);
      socket?.emit('leave-room', party.roomCode);
      setParty(null);
      message.success('Soba zatvorena');
    } catch {
      message.error('Greška pri zatvaranju sobe');
    }
  };

  const handleLeave = () => {
    if (!party || !socket) return;
    socket.emit('leave-room', party.roomCode);
    setParty(null);
  };

  if (!party) {
    return (
      <div style={{ padding: 24, maxWidth: 600, margin: '0 auto' }}>
        <Title level={2}><TeamOutlined /> Watch Party</Title>

        <Card style={{ marginBottom: 24 }}>
          <Title level={4}>Kreiraj novu sobu</Title>
          <Button type="primary" onClick={handleCreate} loading={loading} size="large">
            Kreiraj sobu
          </Button>
        </Card>

        <Card>
          <Title level={4}>Pridruži se sobi</Title>
          <Space>
            <Input
              placeholder="Unesite kod sobe"
              value={joinCode}
              onChange={(e) => setJoinCode(e.target.value)}
              onPressEnter={handleJoin}
              style={{ width: 200 }}
            />
            <Button type="primary" onClick={handleJoin} loading={loading}>
              Pridruži se
            </Button>
          </Space>
        </Card>
      </div>
    );
  }

  return (
    <div style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
      <Title level={2}><TeamOutlined /> Watch Party</Title>

      <Card style={{ marginBottom: 24 }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <div>
            <Text strong>Kod sobe: </Text>
            <Tag color="red" style={{ fontSize: 18, padding: '4px 12px' }}>{party.roomCode}</Tag>
          </div>
          <div>
            <Text strong>Kreator: </Text>
            <Text>{party.creator.username}</Text>
          </div>
          <div>
            <Text strong>Članovi ({party.members.length}): </Text>
            {party.members.map((m) => (
              <Tag key={m.id}>{m.user.username}</Tag>
            ))}
          </div>
          <Space>
            {user?.id === party.creator.id ? (
              <Button danger onClick={handleClose}>Zatvori sobu</Button>
            ) : (
              <Button onClick={handleLeave}>Napusti sobu</Button>
            )}
          </Space>
        </Space>
      </Card>

      <Divider>Odaberi video za gledanje</Divider>

      <List
        grid={{ gutter: 16, xs: 1, sm: 2, md: 3 }}
        dataSource={videos}
        renderItem={(video) => (
          <List.Item>
            <Card
              hoverable
              size="small"
              cover={
                <img
                  alt={video.title}
                  src={videoApi.getThumbnailUrl(video.id)}
                  style={{ height: 120, objectFit: 'cover' }}
                />
              }
              actions={[
                <Button
                  type="link"
                  icon={<PlayCircleOutlined />}
                  onClick={() => handlePlayVideo(video.id, video.title)}
                >
                  Pokreni
                </Button>,
              ]}
            >
              <Card.Meta title={video.title} description={video.user.username} />
            </Card>
          </List.Item>
        )}
      />
    </div>
  );
};

export default WatchPartyPage;
