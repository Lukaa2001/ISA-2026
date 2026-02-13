import React from 'react';
import { Row, Col, Empty } from 'antd';
import VideoCard from './VideoCard';
import { Video } from '../../api/video.api';

interface VideoGridProps {
  videos: Video[];
}

const VideoGrid: React.FC<VideoGridProps> = ({ videos }) => {
  if (videos.length === 0) {
    return <Empty description="Nema videa" />;
  }

  return (
    <Row gutter={[16, 16]}>
      {videos.map((video) => (
        <Col key={video.id} xs={24} sm={12} md={8} lg={6}>
          <VideoCard video={video} />
        </Col>
      ))}
    </Row>
  );
};

export default VideoGrid;
