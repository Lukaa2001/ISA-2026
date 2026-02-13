import React, { useEffect, useState } from 'react';
import { Typography, Pagination, Spin } from 'antd';
import VideoGrid from '../components/Video/VideoGrid';
import { videoApi, Video } from '../api/video.api';

const { Title } = Typography;

const HomePage: React.FC = () => {
  const [videos, setVideos] = useState<Video[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);

  useEffect(() => {
    loadVideos();
  }, [page]);

  const loadVideos = async () => {
    setLoading(true);
    try {
      const res = await videoApi.list(page);
      setVideos(res.data.videos);
      setTotal(res.data.pagination.total);
    } catch {
      // Error handled silently
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Title level={2}>Najnoviji videi</Title>
      {loading ? (
        <div style={{ textAlign: 'center', padding: 50 }}>
          <Spin size="large" />
        </div>
      ) : (
        <>
          <VideoGrid videos={videos} />
          {total > 12 && (
            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Pagination
                current={page}
                total={total}
                pageSize={12}
                onChange={setPage}
              />
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default HomePage;
