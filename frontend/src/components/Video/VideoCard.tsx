import React from 'react';
import { Card, Typography, Tag } from 'antd';
import { EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { Video, videoApi } from '../../api/video.api';

const { Meta } = Card;
const { Text } = Typography;

interface VideoCardProps {
  video: Video;
}

const VideoCard: React.FC<VideoCardProps> = ({ video }) => {
  const navigate = useNavigate();

  return (
    <Card
      hoverable
      onClick={() => navigate(`/video/${video.id}`)}
      style={{
        border: '2px solid red',
        position: 'relative',
        overflow: 'hidden',
      }}
      cover={
        <div style={{ position: 'relative' }}>
          <img
            alt={video.title}
            src={videoApi.getThumbnailUrl(video.id)}
            style={{ width: '100%', height: 180, objectFit: 'cover' }}
          />
          {/* Red dot in top-right corner */}
          <div
            style={{
              position: 'absolute',
              top: 8,
              right: 8,
              width: 12,
              height: 12,
              borderRadius: '50%',
              backgroundColor: 'red',
            }}
          />
        </div>
      }
    >
      <Meta
        title={video.title}
        description={
          <div>
            <Text type="secondary">{video.user.username}</Text>
            <div style={{ marginTop: 4 }}>
              <EyeOutlined /> {video.viewCount} pregleda
            </div>
            <div style={{ marginTop: 4 }}>
              {video.tags?.slice(0, 3).map((tag) => (
                <Tag key={tag} color="red" style={{ marginBottom: 2 }}>
                  {tag}
                </Tag>
              ))}
            </div>
          </div>
        }
      />
    </Card>
  );
};

export default VideoCard;
