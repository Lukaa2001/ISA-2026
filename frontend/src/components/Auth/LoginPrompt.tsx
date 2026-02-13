import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Alert } from 'antd';

const LoginPrompt: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Alert
      message="Morate biti prijavljeni da biste komentirali"
      type="warning"
      showIcon
      action={
        <Button size="small" type="primary" onClick={() => navigate('/login')}>
          Prijava
        </Button>
      }
      style={{ marginBottom: 16 }}
    />
  );
};

export default LoginPrompt;
