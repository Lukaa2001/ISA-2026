import React, { useState } from 'react';
import { Input, Button, message, Space } from 'antd';
import { commentApi } from '../../api/comment.api';

const { TextArea } = Input;

interface CommentFormProps {
  videoId: number;
  onCommentAdded: () => void;
}

const CommentForm: React.FC<CommentFormProps> = ({ videoId, onCommentAdded }) => {
  const [text, setText] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!text.trim()) return;

    setLoading(true);
    try {
      await commentApi.create(videoId, text.trim());
      setText('');
      onCommentAdded();
      message.success('Komentar dodan');
    } catch (error: any) {
      if (error.response?.status === 429) {
        message.error('Previše komentara. Pokušajte ponovo kasnije.');
      } else {
        message.error('Greška pri dodavanju komentara');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <TextArea
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="Napišite komentar..."
        rows={3}
        maxLength={1000}
        showCount
      />
      <Button
        type="primary"
        onClick={handleSubmit}
        loading={loading}
        disabled={!text.trim()}
      >
        Objavi komentar
      </Button>
    </Space>
  );
};

export default CommentForm;
