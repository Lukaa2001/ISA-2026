import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, message, Alert } from 'antd';
import { Link } from 'react-router-dom';

import { authApi } from '../api/auth.api';

const { Title } = Typography;

const RegisterPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState<{ message: string; emailPreviewUrl?: string } | null>(null);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      const res = await authApi.register(values);
      setSuccess({
        message: res.data.message,
        emailPreviewUrl: res.data.emailPreviewUrl,
      });
    } catch (error: any) {
      if (error.response?.status === 409) {
        message.error(error.response.data.error);
      } else if (error.response?.data?.details) {
        error.response.data.details.forEach((d: any) => message.error(d.message));
      } else {
        message.error('Greška pri registraciji');
      }
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px 24px' }}>
        <Card style={{ width: 500 }}>
          <Alert
            message="Registracija uspješna!"
            description={
              <div>
                <p>{success.message}</p>
                {success.emailPreviewUrl && (
                  <p>
                    <a href={success.emailPreviewUrl} target="_blank" rel="noopener noreferrer">
                      Pogledajte aktivacijski email (Ethereal)
                    </a>
                  </p>
                )}
              </div>
            }
            type="success"
            showIcon
          />
          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <Link to="/login">Idi na prijavu</Link>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '50px 24px' }}>
      <Card style={{ width: 450 }}>
        <Title level={3} style={{ textAlign: 'center' }}>Registracija</Title>
        <Form onFinish={onFinish} layout="vertical">
          <Form.Item name="email" rules={[{ required: true, type: 'email', message: 'Unesite ispravni email' }]}>
            <Input placeholder="Email" />
          </Form.Item>

          <Form.Item name="username" rules={[{ required: true, min: 3, message: 'Korisničko ime (min. 3 znaka)' }]}>
            <Input placeholder="Korisničko ime" />
          </Form.Item>

          <Form.Item name="password" rules={[{ required: true, min: 8, message: 'Lozinka (min. 8 znakova)' }]}>
            <Input.Password placeholder="Lozinka" style={{ color: '#8B0000' }} />
          </Form.Item>

          <Form.Item name="firstName" rules={[{ required: true, message: 'Unesite ime' }]}>
            <Input placeholder="Ime" />
          </Form.Item>

          <Form.Item name="lastName" rules={[{ required: true, message: 'Unesite prezime' }]}>
            <Input placeholder="Prezime" />
          </Form.Item>

          <Form.Item name="address" rules={[{ required: true, message: 'Unesite adresu' }]}>
            <Input placeholder="Adresa" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              Registriraj se
            </Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          Već imate račun? <Link to="/login">Prijavite se</Link>
        </div>
      </Card>
    </div>
  );
};

export default RegisterPage;
