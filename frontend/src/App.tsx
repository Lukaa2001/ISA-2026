import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Layout } from 'antd';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Layout/Navbar';
import ProtectedRoute from './components/Auth/ProtectedRoute';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ActivationPage from './pages/ActivationPage';
import VideoPage from './pages/VideoPage';
import UploadPage from './pages/UploadPage';
import ProfilePage from './pages/ProfilePage';
import WatchPartyPage from './pages/WatchPartyPage';

const { Content } = Layout;

const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Layout style={{ minHeight: '100vh' }}>
          <Navbar />
          <Content>
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/activate/:token" element={<ActivationPage />} />
              <Route path="/video/:id" element={<VideoPage />} />
              <Route path="/profile/:id" element={<ProfilePage />} />
              <Route
                path="/upload"
                element={
                  <ProtectedRoute>
                    <UploadPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/watch-party"
                element={
                  <ProtectedRoute>
                    <WatchPartyPage />
                  </ProtectedRoute>
                }
              />
            </Routes>
          </Content>
        </Layout>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
