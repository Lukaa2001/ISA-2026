import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { MailOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '../api/auth.api';
import { useAuth } from '../context/AuthContext';

const { Title } = Typography;

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const onFinish = async (values: { email: string; password: string }) => {
    setLoading(true);
    try {
      const res = await authApi.login(values);
      login(res.data.token, res.data.user);
      message.success('Uspješna prijava!');
      navigate('/');
    } catch (error: any) {
      if (error.response?.status === 429) {
        message.error('Previše pokušaja prijave. Pokušajte ponovo za minutu.');
      } else if (error.response?.status === 401) {
        message.error('Neispravni podaci za prijavu');
      } else if (error.response?.status === 403) {
        message.error('Račun nije aktiviran. Provjerite email.');
      } else {
        message.error('Greška pri prijavi');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '50px 24px' }}>
      <Card style={{ width: 400 }}>
        <Title level={3} style={{ textAlign: 'center' }}>Prijava</Title>
        <Form onFinish={onFinish} layout="vertical">
          <Form.Item
            name="email"
            rules={[{ required: true, message: 'Unesite email' }, { type: 'email', message: 'Neispravni email' }]}
          >
            <Input prefix={<MailOutlined />} placeholder="Email" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Unesite lozinku' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Lozinka"
              style={{ color: '#8B0000' }}
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              Prijava
            </Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          Nemate račun? <Link to="/register">Registrirajte se</Link>
        </div>
      </Card>
    </div>
  );
};

export default LoginPage;
