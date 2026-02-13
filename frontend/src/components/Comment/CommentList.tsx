import React, { useEffect, useState } from 'react';
import { List, Typography, Pagination, Spin, Space } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { Link } from 'react-router-dom';
import { commentApi, Comment } from '../../api/comment.api';

const { Text, Paragraph } = Typography;

interface CommentListProps {
  videoId: number;
  refreshKey: number;
}

const CommentList: React.FC<CommentListProps> = ({ videoId, refreshKey }) => {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);

  useEffect(() => {
    loadComments();
  }, [videoId, page, refreshKey]);

  const loadComments = async () => {
    setLoading(true);
    try {
      const res = await commentApi.list(videoId, page);
      setComments(res.data.comments);
      setTotal(res.data.pagination.total);
    } catch {
      // Error silently handled
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <Spin />;
  }

  return (
    <div>
      <Typography.Title level={5}>{total} komentara</Typography.Title>
      <List
        dataSource={comments}
        renderItem={(comment) => (
          <List.Item>
            <List.Item.Meta
              avatar={<UserOutlined style={{ fontSize: 24 }} />}
              title={
                <Space>
                  <Link to={`/profile/${comment.user.id}`}>
                    <Text strong>{comment.user.username}</Text>
                  </Link>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {new Date(comment.createdAt).toLocaleString('hr-HR')}
                  </Text>
                </Space>
              }
              description={<Paragraph>{comment.text}</Paragraph>}
            />
          </List.Item>
        )}
      />
      {total > 20 && (
        <Pagination
          current={page}
          total={total}
          pageSize={20}
          onChange={setPage}
          style={{ marginTop: 16, textAlign: 'center' }}
        />
      )}
    </div>
  );
};

export default CommentList;
