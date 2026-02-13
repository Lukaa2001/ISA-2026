import React, { useState } from 'react';
import { Form, Input, Button, Upload, Card, Typography, message, Select } from 'antd';
import { UploadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { videoApi } from '../api/video.api';

const { Title } = Typography;
const { TextArea } = Input;

const UploadPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [videoFile, setVideoFile] = useState<File | null>(null);
  const [thumbnailFile, setThumbnailFile] = useState<File | null>(null);
  const navigate = useNavigate();

  const onFinish = async (values: any) => {
    if (!videoFile || !thumbnailFile) {
      message.error('Odaberite video i thumbnail datoteku');
      return;
    }

    setLoading(true);
    const formData = new FormData();
    formData.append('title', values.title);
    formData.append('description', values.description);
    formData.append('tags', JSON.stringify(values.tags || []));
    formData.append('video', videoFile);
    formData.append('thumbnail', thumbnailFile);

    if (values.latitude) formData.append('latitude', values.latitude);
    if (values.longitude) formData.append('longitude', values.longitude);

    try {
      const res = await videoApi.upload(formData);
      message.success('Video uspješno uploadan!');
      navigate(`/video/${res.data.id}`);
    } catch (error: any) {
      message.error(error.response?.data?.error || 'Greška pri uploadu');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '24px' }}>
      <Card style={{ width: 600 }}>
        <Title level={3} style={{ textAlign: 'center' }}>Upload Video</Title>
        <Form onFinish={onFinish} layout="vertical">
          <Form.Item name="title" label="Naslov" rules={[{ required: true, message: 'Unesite naslov' }]}>
            <Input placeholder="Naslov videa" />
          </Form.Item>

          <Form.Item name="description" label="Opis" rules={[{ required: true, message: 'Unesite opis' }]}>
            <TextArea rows={4} placeholder="Opis videa" />
          </Form.Item>

          <Form.Item name="tags" label="Oznake">
            <Select mode="tags" placeholder="Dodajte oznake" />
          </Form.Item>

          <Form.Item label="Video datoteka" required>
            <Upload
              beforeUpload={(file) => {
                setVideoFile(file);
                return false;
              }}
              maxCount={1}
              accept=".mp4,.webm,.avi,.mov,.mkv"
              onRemove={() => setVideoFile(null)}
            >
              <Button icon={<UploadOutlined />}>Odaberi video</Button>
            </Upload>
          </Form.Item>

          <Form.Item label="Thumbnail" required>
            <Upload
              beforeUpload={(file) => {
                setThumbnailFile(file);
                return false;
              }}
              maxCount={1}
              accept=".jpg,.jpeg,.png,.webp"
              onRemove={() => setThumbnailFile(null)}
            >
              <Button icon={<UploadOutlined />}>Odaberi thumbnail</Button>
            </Upload>
          </Form.Item>

          <Form.Item name="latitude" label="Latitude">
            <Input type="number" step="any" placeholder="npr. 45.815" />
          </Form.Item>

          <Form.Item name="longitude" label="Longitude">
            <Input type="number" step="any" placeholder="npr. 15.982" />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              style={{ backgroundColor: '#ff4d4f', borderColor: '#ff4d4f', color: 'white' }}
            >
              🎬 Upload Video
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default UploadPage;
