import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './SharedStyles.css';
import { API_BASE_URL } from "./config";
import HomeIcon from "@mui/icons-material/Home";
import MovieIcon from "@mui/icons-material/Movie";
import TvIcon from "@mui/icons-material/Tv";
import MusicNoteIcon from "@mui/icons-material/MusicNote";
import AnimationIcon from "@mui/icons-material/Animation";
import SportsEsportsIcon from "@mui/icons-material/SportsEsports";
import SportsMartialArtsIcon from "@mui/icons-material/SportsMartialArts";
import PersonIcon from "@mui/icons-material/Person";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import ForumIcon from "@mui/icons-material/Forum";
import HelpIcon from "@mui/icons-material/Help";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import PublishIcon from "@mui/icons-material/Publish";
import CategoryIcon from "@mui/icons-material/Category";
import TitleIcon from "@mui/icons-material/Title";
import DescriptionIcon from "@mui/icons-material/Description";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";

const navItems = [
  { label: "首页", icon: <HomeIcon className="emerald-nav-icon" />, path: "/home", type: "home" },
  { label: "电影", icon: <MovieIcon className="emerald-nav-icon" />, path: "/movie", type: "movie" },
  { label: "剧集", icon: <TvIcon className="emerald-nav-icon" />, path: "/tv", type: "tv" },
  { label: "音乐", icon: <MusicNoteIcon className="emerald-nav-icon" />, path: "/music", type: "music" },
  { label: "动漫", icon: <AnimationIcon className="emerald-nav-icon" />, path: "/anime", type: "anime" },
  { label: "游戏", icon: <SportsEsportsIcon className="emerald-nav-icon" />, path: "/game", type: "game" },
  { label: "体育", icon: <SportsMartialArtsIcon className="emerald-nav-icon" />, path: "/sport", type: "sport" },
  { label: "资料", icon: <PersonIcon className="emerald-nav-icon" />, path: "/info", type: "info" },
  { label: "论坛", icon: <ForumIcon className="emerald-nav-icon" />, path: "/forum", type: "forum" },
  { label: "发布", icon: <AccountCircleIcon className="emerald-nav-icon" />, path: "/publish", type: "publish" },
  { label: "求种", icon: <HelpIcon className="emerald-nav-icon" />, path: "/begseed", type: "help" },
];

// 发布页面专用文字雨内容
const publishTexts = [
  "分享", "上传", "种子", "资源", "贡献", "精品", "原创", "高清",
  "无损", "蓝光", "1080P", "4K", "HDR", "珍藏", "首发", "独家"
];

const PublishPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    type: '',
    torrentFile: '',
    title: '',
    subtitle: ''
  });
  const [subType, setSubType] = useState('');
  const [userInfo, setUserInfo] = useState({ avatar_url: '', username: '' });  const [userPT, setUserPT] = useState({ magic: 0, ratio: 0, upload: 0, download: 0 });
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [isDragOver, setIsDragOver] = useState(false);

  useEffect(() => {
    // 获取用户信息
    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const userId = match ? match[2] : null;
    if (userId) {
      fetch(`${API_BASE_URL}/api/get-userpt?userid=${encodeURIComponent(userId)}`)
        .then(res => res.json())
        .then(data => {
          setUserInfo({ avatar_url: data.user.avatar_url, username: data.user.username });
          setUserPT({
            magic: data.magic_value || data.magic || 0,
            ratio: data.share_ratio || data.share || 0,
            upload: data.upload_amount || data.upload || 0,
            download: data.download_amount || data.download || 0,
          });
        })
        .catch(err => console.error('Fetching user profile failed', err));
    }
  }, []);

  const typeOptions = {
    '电影': ['大陆', '港台', '欧美', '日韩'],
    '剧集': ['国产电视剧', '港剧', '欧美剧', '日韩剧'],
    '音乐': ['古典音乐', '流行音乐', '摇滚', '电子音乐', '说唱'],
    '动漫': ['国创', '日漫', '欧美动漫', '韩漫'],
    '游戏': ['PC', '主机', '移动', '掌机', '视频'],
    '体育': ['篮球', '足球', '羽毛球', '排球', '电竞'],
    '资料': ['出版物', '学习教程', '素材模板', '演讲交流', '日常娱乐'],
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    validateAndSetFile(file);
  };

  const validateAndSetFile = (file) => {
    if (file && file.name.split('.').pop().toLowerCase() !== 'torrent') {
      alert('仅能上传.torrent类型文件');
      return false;
    } else if (file) {
      setFormData({ ...formData, torrentFile: file });
      return true;
    }
    return false;
  };

  // 拖拽上传功能
  const handleDragEnter = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    // 只有当鼠标真正离开拖拽区域时才设置为false
    if (!e.currentTarget.contains(e.relatedTarget)) {
      setIsDragOver(false);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      const file = files[0];
      if (validateAndSetFile(file)) {
        // 如果文件验证成功，清空文件输入框并重新设置
        const fileInput = document.getElementById('torrentFile');
        if (fileInput) {
          // 创建一个新的FileList对象来模拟文件选择
          const dt = new DataTransfer();
          dt.items.add(file);
          fileInput.files = dt.files;
        }
      }
    }
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const userId = match ? match[2] : null;

    if (!formData.torrentFile) {
      alert('请上传.torrent文件');
      return;
    }

    setIsUploading(true);
    setUploadProgress(0);

    // 模拟上传进度
    const progressInterval = setInterval(() => {
      setUploadProgress(prev => {
        if (prev >= 90) {
          clearInterval(progressInterval);
          return prev;
        }
        return prev + Math.random() * 15;
      });
    }, 200);

    const data = new FormData();
    data.append('userid', userId);
    data.append('title', formData.title);
    data.append('tag', subType);
    data.append('file', formData.torrentFile);
    data.append('subtitle', formData.subtitle);

    try {
      const response = await fetch(`${API_BASE_URL}/api/save-torrent`, {
        method: 'POST',
        body: data,
      });
      
      clearInterval(progressInterval);
      setUploadProgress(100);
      
      if (response.ok) {
        setTimeout(() => {
          alert('上传成功！');
          setIsUploading(false);
          setUploadProgress(0);
          // 重置表单
          setFormData({
            type: '',
            torrentFile: '',
            title: '',
            subtitle: ''
          });
          setSubType('');
        }, 500);
      } else {
        setIsUploading(false);
        setUploadProgress(0);
        alert('上传失败');
      }
    } catch (err) {
      clearInterval(progressInterval);
      setIsUploading(false);
      setUploadProgress(0);
      alert('网络错误');
    }
  };
  return (
    <div className="emerald-home-container">
      {/* 发布页面专用文字雨 */}
      <div className="publish-text-rain">
        {publishTexts.map((text, index) => (
          <div key={index} className="text-drop" style={{
            left: `${(index * 4) % 100}%`,
            animationDelay: `${(index * 0.9) % 12}s`,
            animationDuration: `${7 + (index % 6)}s`,
            color: index % 3 === 0 ? '#90ee90' : index % 3 === 1 ? '#2d5016' : '#4a7c59'
          }}>
            {text}
          </div>
        ))}
      </div>

      {/* 浮动园林装饰元素 */}
      <div className="floating-garden-elements">
        <div className="garden-element">📤</div>
        <div className="garden-element">🌟</div>
        <div className="garden-element">💎</div>
        <div className="garden-element">🎯</div>
      </div>

      <div className="emerald-content">
        {/* NeuraFlux用户栏 */}
        <div className="emerald-user-bar">
          <div className="emerald-user-avatar" onClick={() => navigate('/user')}>
            {userInfo.avatar_url ? (
              <img src={userInfo.avatar_url} alt="用户头像" style={{ width: 38, height: 38, borderRadius: '50%', objectFit: 'cover' }} />
            ) : (
              <AccountCircleIcon style={{ fontSize: 38, color: 'white' }} />
            )}
          </div>
          <div className="emerald-brand-section">
            <div className="emerald-brand-icon">⚡</div>
            <div className="emerald-user-label">NeuraFlux</div>
          </div>
          <div className="emerald-user-stats">
            <span className="emerald-stat-item">
              魔力值: <span className="emerald-stat-value">{userPT.magic}</span>
            </span>
            <span className="emerald-stat-item">
              分享率: <span className="emerald-stat-value">{userPT.ratio}</span>
            </span>
            <span className="emerald-stat-item">
              上传: <span className="emerald-stat-value">{userPT.upload}GB</span>
            </span>
            <span className="emerald-stat-item">
              下载: <span className="emerald-stat-value">{userPT.download}GB</span>
            </span>
          </div>
        </div>

        {/* NeuraFlux导航栏 */}
        <nav className="emerald-nav-bar">
          {navItems.map((item) => (
            <div
              key={item.label}
              className={`emerald-nav-item ${item.label === "发布" ? "active" : ""}`}
              data-type={item.type}
              onClick={() => navigate(item.path)}
            >
              {item.icon}
              <span className="emerald-nav-label">{item.label}</span>
            </div>
          ))}
        </nav>

        {/* 发布页面内容 */}
        <div className="emerald-content-section">
          <h1 className="emerald-page-title">
            <PublishIcon style={{ marginRight: '15px', fontSize: '36px', color: '#2d5016' }} />
            🌟 NeuraFlux种子发布
          </h1>
          <p style={{ textAlign: 'center', color: '#2d5016', fontSize: '18px', marginBottom: '40px', fontStyle: 'italic' }}>
            分享优质资源，成就精彩社区 • 每一次上传都是对知识传承的贡献
          </p>

          {/* 发布表单 */}
          <div className="publish-form-container">
            <form onSubmit={handleSubmit} className="publish-form-advanced">
              {/* 资源类型选择 */}
              <div className="form-section">
                <div className="section-header">
                  <CategoryIcon style={{ color: '#2d5016', marginRight: '10px' }} />
                  <h3>资源分类</h3>
                </div>
                <div className="form-grid">
                  <div className="form-field">
                    <label htmlFor="type">主要类型</label>
                    <div className="select-wrapper">
                      <select 
                        name="type" 
                        id="type" 
                        value={formData.type} 
                        onChange={e => { handleChange(e); setSubType(''); }} 
                        required
                        className="modern-select"
                      >
                        <option value="">请选择资源类型</option>
                        <option value="电影">🎬 电影</option>
                        <option value="剧集">📺 剧集</option>
                        <option value="音乐">🎵 音乐</option>
                        <option value="动漫">🎨 动漫</option>
                        <option value="游戏">🎮 游戏</option>
                        <option value="体育">⚽ 体育</option>
                        <option value="资料">📚 资料</option>
                      </select>
                    </div>
                  </div>

                  {formData.type && typeOptions[formData.type] && (
                    <div className="form-field">
                      <label htmlFor="subtype">具体分类</label>
                      <div className="select-wrapper">
                        <select 
                          name="subtype" 
                          id="subtype" 
                          value={subType} 
                          onChange={e => setSubType(e.target.value)} 
                          required
                          className="modern-select"
                        >
                          <option value="">请选择具体分类</option>
                          {typeOptions[formData.type].map(opt => (
                            <option key={opt} value={opt}>{opt}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* 种子文件上传 */}
              <div className="form-section">
                <div className="section-header">
                  <CloudUploadIcon style={{ color: '#2d5016', marginRight: '10px' }} />
                  <h3>种子文件</h3>
                </div>                <div 
                  className={`file-upload-area ${isDragOver ? 'drag-over' : ''}`}
                  onDragEnter={handleDragEnter}
                  onDragLeave={handleDragLeave}
                  onDragOver={handleDragOver}
                  onDrop={handleDrop}
                >
                  <input
                    type="file"
                    id="torrentFile"
                    name="torrentFile"
                    onChange={handleFileChange}
                    required
                    accept=".torrent"
                    className="file-input-hidden"
                  />
                  <label htmlFor="torrentFile" className="file-upload-label">
                    <CloudUploadIcon 
                      style={{ 
                        fontSize: '48px', 
                        color: isDragOver ? '#2d5016' : '#90ee90', 
                        marginBottom: '10px',
                        transition: 'all 0.3s ease'
                      }} 
                    />
                    <div className="upload-text">
                      <span className="upload-main-text">
                        {isDragOver ? 
                          '松开鼠标完成上传' : 
                          (formData.torrentFile ? formData.torrentFile.name : '点击选择或拖拽.torrent文件')
                        }
                      </span>
                      <span className="upload-sub-text">
                        {isDragOver ? 
                          '🎯 即将上传文件到NeuraFlux' :
                          '✨ 支持拖拽上传 • 仅接受.torrent格式文件'
                        }
                      </span>
                    </div>
                  </label>
                </div>
              </div>

              {/* 标题和描述 */}
              <div className="form-section">
                <div className="section-header">
                  <TitleIcon style={{ color: '#2d5016', marginRight: '10px' }} />
                  <h3>资源信息</h3>
                </div>
                <div className="form-field">
                  <label htmlFor="title">资源标题</label>
                  <input
                    type="text"
                    id="title"
                    name="title"
                    value={formData.title}
                    onChange={handleChange}
                    required
                    className="modern-input"
                    placeholder="请输入简洁明确的资源标题..."
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="subtitle">
                    <DescriptionIcon style={{ marginRight: '5px', fontSize: '18px' }} />
                    资源简介
                  </label>
                  <textarea
                    id="subtitle"
                    name="subtitle"
                    value={formData.subtitle}
                    onChange={handleChange}
                    className="modern-textarea"
                    rows="4"
                    placeholder="详细描述资源内容、质量、特色等信息..."
                  />
                </div>
              </div>

              {/* 上传进度条 */}
              {isUploading && (
                <div className="upload-progress-section">
                  <div className="progress-header">
                    <span>正在上传资源...</span>
                    <span>{Math.round(uploadProgress)}%</span>
                  </div>
                  <div className="progress-bar">
                    <div 
                      className="progress-fill" 
                      style={{ width: `${uploadProgress}%` }}
                    />
                  </div>
                </div>
              )}

              {/* 提交按钮 */}
              <div className="form-submit-section">
                <button 
                  type="submit" 
                  className="publish-submit-btn"
                  disabled={isUploading}
                >
                  {isUploading ? (
                    <>
                      <div className="loading-spinner" />
                      上传中...
                    </>
                  ) : (
                    <>
                      <CheckCircleIcon style={{ marginRight: '8px', fontSize: '20px' }} />
                      发布资源
                    </>
                  )}
                </button>
                <div className="submit-tips">
                  💡 请确保上传的资源合法合规，感谢您的贡献！
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PublishPage;