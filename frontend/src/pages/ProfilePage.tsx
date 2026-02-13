import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Typography, Spin, Statistic, Row, Col, Divider, Pagination } from 'antd';
import { UserOutlined, VideoCameraOutlined, CommentOutlined } from '@ant-design/icons';
import VideoGrid from '../components/Video/VideoGrid';
import client from '../api/client';
import { Video } from '../api/video.api';

const { Title, Text } = Typography;

interface UserProfile {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  createdAt: string;
  _count: {
    videos: number;
    comments: number;
  };
}

const ProfilePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [totalVideos, setTotalVideos] = useState(0);

  useEffect(() => {
    if (id) {
      loadProfile(parseInt(id));
    }
  }, [id]);

  useEffect(() => {
    if (id) {
      loadUserVideos(parseInt(id));
    }
  }, [id, page]);

  const loadProfile = async (userId: number) => {
    try {
      const res = await client.get(`/users/${userId}`);
      setProfile(res.data);
    } catch {
      // Error handled silently
    } finally {
      setLoading(false);
    }
  };

  const loadUserVideos = async (userId: number) => {
    try {
      const res = await client.get(`/users/${userId}/videos`, { params: { page } });
      setVideos(res.data.videos);
      setTotalVideos(res.data.pagination.total);
    } catch {
      // Error handled silently
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!profile) {
    return <div style={{ padding: 24 }}>Korisnik nije pronađen</div>;
  }

  return (
    <div style={{ padding: 24, maxWidth: 900, margin: '0 auto' }}>
      <div style={{ textAlign: 'center', marginBottom: 24 }}>
        <UserOutlined style={{ fontSize: 64, color: '#ccc' }} />
        <Title level={3}>{profile.username}</Title>
        <Text>{profile.firstName} {profile.lastName}</Text>
        <br />
        <Text type="secondary">
          Član od {new Date(profile.createdAt).toLocaleDateString('hr-HR')}
        </Text>
      </div>

      <Row gutter={24} justify="center" style={{ marginBottom: 24 }}>
        <Col>
          <Statistic title="Videi" value={profile._count.videos} prefix={<VideoCameraOutlined />} />
        </Col>
        <Col>
          <Statistic title="Komentari" value={profile._count.comments} prefix={<CommentOutlined />} />
        </Col>
      </Row>

      <Divider>Videi korisnika</Divider>

      <VideoGrid videos={videos} />

      {totalVideos > 12 && (
        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <Pagination
            current={page}
            total={totalVideos}
            pageSize={12}
            onChange={setPage}
          />
        </div>
      )}
    </div>
  );
};

export default ProfilePage;
