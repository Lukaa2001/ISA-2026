import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Result, Spin, Button } from 'antd';
import client from '../api/client';

const ActivationPage: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (token) {
      client.get(`/auth/activate/${token}`)
        .then(() => setStatus('success'))
        .catch((err) => {
          setStatus('error');
          setErrorMessage(err.response?.data?.error || 'Aktivacija nije uspjela');
        });
    }
  }, [token]);

  if (status === 'loading') {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
        <p>Aktivacija računa...</p>
      </div>
    );
  }

  if (status === 'success') {
    return (
      <Result
        status="success"
        title="Račun aktiviran!"
        subTitle="Sada se možete prijaviti."
        extra={
          <Link to="/login">
            <Button type="primary">Prijava</Button>
          </Link>
        }
      />
    );
  }

  return (
    <Result
      status="error"
      title="Aktivacija nije uspjela"
      subTitle={errorMessage}
      extra={
        <Link to="/register">
          <Button type="primary">Registriraj se ponovo</Button>
        </Link>
      }
    />
  );
};

export default ActivationPage;
