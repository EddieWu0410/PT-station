import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from 'antd';
import { API_BASE_URL } from './config';
import './App.css';

const RegisterPage = () => {
  const [formData, setFormData] = useState({ username: '', password: '', inviteCode: '', confirmPassword: '' });
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleRegister = async () => {
    if (formData.password.length !== 8) {
      setErrorMessage('密码必须为八位字符！');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      setErrorMessage('两次输入的密码不匹配！');
      return;
    }

    // send registration request to backend
    try {
      console.log('注册信息:', formData);
      const response = await fetch(`${API_BASE_URL}/api/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: formData.username, password: formData.password, invite_email: formData.inviteCode })
      });
      const data = await response.json();
      if (!response.ok) {
        setErrorMessage(data.message || '注册失败，请重试');
        return;
      }
      setErrorMessage('');
      navigate('/login');
      alert('注册成功！');
    } catch (error) {
      setErrorMessage('网络错误，请稍后重试');
    }
  };

  return (
    <div className="register-page" style={{ minHeight: '100vh', background: 'linear-gradient(135deg, #e0e7ff 0%, #f0f8ff 100%)', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <div className="register-form-container" style={{ width: 360, padding: '40px 32px 64px 32px', borderRadius: 18, boxShadow: '0 8px 32px rgba(60, 80, 180, 0.10)', background: '#fff', position: 'relative' }}>
        <h1 style={{ textAlign: 'center', marginBottom: 32, color: '#222', fontWeight: 700, fontSize: 32, letterSpacing: 2 }}>欢迎注册</h1>
        <form className="register-form">
          <div className="form-row" style={{ marginBottom: 24 }}>
            <label htmlFor="inviteCode" style={{ display: 'block', marginBottom: 8, color: '#333', fontWeight: 500, fontSize: 16 }}>被邀请邮箱</label>
            <Input
              placeholder="请输入被邀请邮箱"
              id="inviteCode"
              name="inviteCode"
              value={formData.inviteCode || ''}
              onChange={handleChange}
              required
              style={{ width: '100%', padding: '12px', borderRadius: 8, fontSize: 16, background: '#f7faff' }}
            />
          </div>
          <div className="form-row" style={{ marginBottom: 24 }}>
            <label htmlFor="username" style={{ display: 'block', marginBottom: 8, color: '#333', fontWeight: 500, fontSize: 16 }}>用户名</label>
            <Input
              placeholder="请输入用户名"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
              style={{ width: '100%', padding: '12px', borderRadius: 8, fontSize: 16, background: '#f7faff' }}
            />
          </div>
          <div className="form-row" style={{ marginBottom: 24 }}>
            <label htmlFor="password" style={{ display: 'block', marginBottom: 8, color: '#333', fontWeight: 500, fontSize: 16 }}>密码</label>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <Input
                placeholder="请输入密码"
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
                style={{ flex: 1, padding: '12px', borderRadius: 8, fontSize: 16, background: '#f7faff' }}
              />
            </div>
          </div>
          <div className="form-row" style={{ marginBottom: 24 }}>
            <label htmlFor="confirmPassword" style={{ display: 'block', marginBottom: 8, color: '#333', fontWeight: 500, fontSize: 16 }}>确认密码</label>
            <Input
              placeholder="请再次输入密码"
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword || ''}
              onChange={handleChange}
              required
              style={{ width: '100%', padding: '12px', borderRadius: 8, fontSize: 16, background: '#f7faff' }}
            />
          </div>
          {errorMessage && <p style={{ color: '#e53935', textAlign: 'center', marginBottom: 18, fontWeight: 500 }}>{errorMessage}</p>}
          <div className="form-row button-row" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', position: 'absolute', left: 0, right: 0, bottom: 24 }}>
            <button
              type="button"
              className="register-button"
              onClick={handleRegister}
              style={{ width: 120, padding: '10px 0', borderRadius: 8, border: 'none', background: 'linear-gradient(90deg, #4f8cff 0%, #6ad1ff 100%)', color: '#fff', fontWeight: 600, fontSize: 16, cursor: 'pointer', boxShadow: '0 2px 8px #b2d8ea' }}
            >
              注册
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default RegisterPage;
