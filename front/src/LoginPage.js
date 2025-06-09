import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';
import { API_BASE_URL } from './config';

const LoginPage = () => {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleLogin = async () => {
    // 进入管理员页面
    // if (formData.username === "admin" && formData.password === "admin123") {
    //   navigate('/admin');
    //   return;
    // }

    if (formData.password.length < 8) {
      setErrorMessage('密码必须至少包含八位字符！');
      return;
    }

    // send login request to backend
    try {
      // console.log('登录信息:', formData);
      const response = await fetch(`${API_BASE_URL}/api/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: formData.username, password: formData.password })
      });
      console.log(response);
      const data = await response.json();
      if (!response.ok) {
        setErrorMessage(data.message || '登录失败，请检查账号密码');
        return;
      }
      // store returned userId in cookie
      const userId = data.userId || data.userid || data.id;
      if (!userId) {
        setErrorMessage('登录失败，未返回用户ID');
        return;
      }
      document.cookie = `userId=${userId}; path=/`;
      // 如果是管理员ID，则跳转到管理员页面
      if (userId === 'admin111') {
        setErrorMessage('');
        navigate('/admin');
        return;
      }
      setErrorMessage('');
      navigate('/home');
    } catch (error) {
      setErrorMessage('网络错误，请稍后重试');
    }
  };

  const handleRegister = () => {
    navigate('/register');
  };

  // 自动填充注册信息
  React.useEffect(() => {
    const regUser = sessionStorage.getItem('registeredUser');
    if (regUser) {
      try {
        const { username, password } = JSON.parse(regUser);
        setFormData({ username, password });
        sessionStorage.removeItem('registeredUser');
      } catch { }
    }
  }, []);
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
        <div className="garden-decoration bottom-right">🌳</div>        {/* 顶部图标 */}
        <div className="garden-icon">⚡</div>
        
        <h1 className="garden-title">NeuraFlux</h1>
        
        <form className="garden-form">
          <div className="garden-form-group">
            <label htmlFor="username" className="garden-label">邮箱地址</label>
            <input
              type="email"
              className="garden-input"
              placeholder="请输入您的邮箱地址"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
            />
            <div className="garden-input-icon">📧</div>
          </div>
          
          <div className="garden-form-group">
            <label htmlFor="password" className="garden-label">密码</label>
            <input
              type="password"
              className="garden-input"
              placeholder="请输入您的密码"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
            />
            <div className="garden-input-icon">🔒</div>
          </div>
          
          {errorMessage && (
            <div className="garden-error">
              {errorMessage}
            </div>
          )}
          
          <div className="garden-button-group">            <button
              type="button"
              className="garden-button garden-login-btn"
              onClick={handleLogin}            >
              接入神经网络
            </button>
            <button
              type="button"
              className="garden-button garden-register-btn"
              onClick={handleRegister}            >
              注册神经节点
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
