import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Layout, Menu, Button, Space, Typography } from 'antd';
import {
  HomeOutlined,
  UploadOutlined,
  UserOutlined,
  LoginOutlined,
  LogoutOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { useAuth } from '../../context/AuthContext';

const { Header } = Layout;
const { Title } = Typography;

const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <Header style={{ display: 'flex', alignItems: 'center', background: '#fff', padding: '0 24px', borderBottom: '2px solid red' }}>
      <Link to="/" style={{ marginRight: 24 }}>
        <Title level={3} style={{ margin: 0, color: 'red' }}>Jutjubić</Title>
      </Link>

      <Menu mode="horizontal" style={{ flex: 1, border: 'none' }} selectedKeys={[]}>
        <Menu.Item key="home" icon={<HomeOutlined />}>
          <Link to="/">Početna</Link>
        </Menu.Item>
        {user && (
          <>
            <Menu.Item key="upload" icon={<UploadOutlined />}>
              <Link to="/upload">Upload</Link>
            </Menu.Item>
            <Menu.Item key="party" icon={<TeamOutlined />}>
              <Link to="/watch-party">Watch Party</Link>
            </Menu.Item>
          </>
        )}
      </Menu>

      <Space>
        {user ? (
          <>
            <Button
              type="text"
              icon={<UserOutlined />}
              onClick={() => navigate(`/profile/${user.id}`)}
            >
              {user.username}
            </Button>
            <Button
              type="text"
              icon={<LogoutOutlined />}
              onClick={handleLogout}
            >
              Odjava
            </Button>
          </>
        ) : (
          <>
            <Button
              type="primary"
              icon={<LoginOutlined />}
              onClick={() => navigate('/login')}
            >
              Prijava
            </Button>
            <Button onClick={() => navigate('/register')}>
              Registracija
            </Button>
          </>
        )}
      </Space>
    </Header>
  );
};

export default Navbar;
