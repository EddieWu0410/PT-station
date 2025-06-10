import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from 'antd';
import { API_BASE_URL } from './config';
import './LoginPage.css';

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
    <div className="garden-login-page">
      {/* 浮动花瓣装饰 */}
      <div className="floating-petals">
        <div className="petal">🌸</div>
        <div className="petal">🌺</div>
        <div className="petal">🌼</div>
        <div className="petal">🌷</div>
        <div className="petal">🌹</div>
        <div className="petal">🌻</div>
        <div className="petal">🌸</div>
        <div className="petal">🌺</div>
      </div>

      <div className="garden-login-container">
        {/* 装饰性元素 */}
        <div className="garden-decoration top-left">🌿</div>
        <div className="garden-decoration top-right">🦋</div>
        <div className="garden-decoration bottom-left">🌱</div>
        <div className="garden-decoration bottom-right">🌳</div>
        <div className="garden-icon">⚡</div>

        <h1 className="garden-title">注册NeuraFlux</h1>

        <form className="garden-form">
          <div className="garden-form-group">
            <label htmlFor="inviteCode" className="garden-label">被邀请邮箱</label>
            <Input
              className="garden-input"
              placeholder="请输入被邀请邮箱"
              id="inviteCode"
              name="inviteCode"
              value={formData.inviteCode || ''}
              onChange={handleChange}
              required
              style={{ background: 'rgba(240,255,240,0.5)' }}
            />
            <div className="garden-input-icon">📧</div>
          </div>
          <div className="garden-form-group">
            <label htmlFor="username" className="garden-label">用户名</label>
            <Input
              className="garden-input"
              placeholder="请输入用户名"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
              style={{ background: 'rgba(240,255,240,0.5)' }}
            />
            <div className="garden-input-icon">👤</div>
          </div>
          <div className="garden-form-group">
            <label htmlFor="password" className="garden-label">密码</label>
            <Input
              className="garden-input"
              placeholder="请输入8位密码"
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              style={{ background: 'rgba(240,255,240,0.5)' }}
            />
            <div className="garden-input-icon">🔒</div>
          </div>
          <div className="garden-form-group">
            <label htmlFor="confirmPassword" className="garden-label">确认密码</label>
            <Input
              className="garden-input"
              placeholder="请再次输入密码"
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword || ''}
              onChange={handleChange}
              required
              style={{ background: 'rgba(240,255,240,0.5)' }}
            />
            <div className="garden-input-icon">🔒</div>
          </div>
          {errorMessage && (
            <div className="garden-error">
              {errorMessage}
            </div>
          )}
          <div className="garden-button-group" style={{ marginTop: 30 }}>
            <button
              type="button"
              className="garden-button garden-register-btn"
              onClick={handleRegister}
            >
              注册神经节点
            </button>
            <button
              type="button"
              className="garden-button garden-login-btn"
              style={{ marginTop: 0 }}
              onClick={() => navigate('/login')}
            >
              返回登录
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default RegisterPage;