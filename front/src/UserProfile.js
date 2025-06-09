import React, { useState, useEffect } from "react";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import PersonIcon from "@mui/icons-material/Person";
import EmailIcon from "@mui/icons-material/Email";
import SchoolIcon from "@mui/icons-material/School";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import CloudDownloadIcon from "@mui/icons-material/CloudDownload";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import FavoriteIcon from "@mui/icons-material/Favorite";
import EmptyIcon from "@mui/icons-material/Inbox";
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import MenuItem from '@mui/material/MenuItem';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "./config";
import "./UserProfile.css";

export default function UserProfile() {
  const navigate = useNavigate();
  const [userInfo, setUserInfo] = useState({
    avatar_url: "",
    username: "示例用户",
    email: "user@example.com",
    invitetimes: "",
    school: "",
    account_status: "",
    gender: "",
  });
  const [tempUserInfo, setTempUserInfo] = useState({ ...userInfo });
  const [userSeeds, setUserSeeds] = useState([]);
  const [userFavorites, setUserFavorites] = useState([]);
  const [userStats, setUserStats] = useState({
    magic: 0,
    upload: 0,
    viptime: 0,
    ratio: 0,
  });

  // 邀请相关
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteStatus, setInviteStatus] = useState('');

  // 兑换相关
  const [exchangeType, setExchangeType] = useState('uploaded');
  const [exchangeMagic, setExchangeMagic] = useState('');
  const [exchangeResult, setExchangeResult] = useState(0);

  // 兑换比例
  const exchangeRate = { uploaded: 0.1, downloaded: 0.1, vip_downloads: 100 };

  // 用户申诉相关
  const [appealOpen, setAppealOpen] = useState(false);
  const [appealTitle, setAppealTitle] = useState('');
  const [appealFile, setAppealFile] = useState(null);

  // 账号迁移相关
  const [migrationOpen, setMigrationOpen] = useState(false);
  const [migrationUpload, setMigrationUpload] = useState('');
  const [migrationEmail, setMigrationEmail] = useState('');
  const [migrationPassword, setMigrationPassword] = useState('');
  const [migrationStatus, setMigrationStatus] = useState('');

  // 兑换结果计算
  React.useEffect(() => {
    if (!exchangeMagic || isNaN(exchangeMagic)) {
      setExchangeResult(0);
      return;
    }
    setExchangeResult(Number(exchangeMagic) / exchangeRate[exchangeType]);
  }, [exchangeMagic, exchangeType]);

  // 获取用户信息
  useEffect(() => {
    const fetchUserInfo = async () => {
      const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
      const userid = match ? match[2] : null;
      if (!userid) return;
      try {
        const res = await fetch(`${API_BASE_URL}/api/user-profile?userid=${userid}`);
        if (res.ok) {
          const data = await res.json();
          setUserInfo(data);
          setTempUserInfo(data);
        }
      } catch (err) {
        console.error("获取用户信息失败", err);
      }
    };
    fetchUserInfo();
  }, []);

  useEffect(() => {
    const fetchUserSeeds = async () => {
      const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
      const userid = match ? match[2] : null;
      if (!userid) return;
      try {
        const res = await fetch(`${API_BASE_URL}/api/user-seeds?userid=${userid}`);
        if (res.ok) {
          const data = await res.json();
          setUserSeeds(data);
        }
      } catch (err) {
        console.error("获取种子列表失败", err);
      }
    };
    fetchUserSeeds();
  }, []);

  useEffect(() => {
    const fetchUserFavorites = async () => {
      const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
      const userid = match ? match[2] : null;
      if (!userid) return;
      try {
        const res = await fetch(`${API_BASE_URL}/api/user-favorites?userid=${userid}`);
        if (res.ok) {
          const data = await res.json();
          // console.log("获取收藏种子列表:", data);
          setUserFavorites(data);
        }
      } catch (err) {
        console.error("获取收藏种子列表失败", err);
      }
    };
    fetchUserFavorites();
  }, []);

  useEffect(() => {
    const fetchUserStats = async () => {
      const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
      const userid = match ? match[2] : null;
      if (!userid) return;
      try {
        const res = await fetch(`${API_BASE_URL}/api/user-stats?userid=${userid}`);
        if (res.ok) {
          const data = await res.json();
          setUserStats(data);
        }
      } catch (err) {
        console.error("获取活跃度信息失败", err);
      }
    };
    fetchUserStats();
  }, []);

  const handleInputChange = (field, value) => {
    setTempUserInfo({ ...tempUserInfo, [field]: value });
  };

  const handleSave = async () => {
    if (tempUserInfo.gender === "男") {
      tempUserInfo.gender = "m";
    } else if (tempUserInfo.gender === "女") {
      tempUserInfo.gender = "f";
    }
    setUserInfo({ ...tempUserInfo });

    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const userid = match ? match[2] : null;
    try {
      const res = await fetch(`${API_BASE_URL}/api/change-profile`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userid, ...tempUserInfo }),
      });
      if (res.ok) {
        alert("信息已保存！");
      } else {
        alert("保存失败，请重试。");
      }
    } catch (err) {
      alert("保存失败，请检查网络连接。");
      console.error("保存用户信息失败", err);
    }
  };

  const handleAvatarClick = () => {
    const avatarUrl = prompt("请输入头像的URL：");
    if (avatarUrl) {
      setTempUserInfo({ ...tempUserInfo, avatar_url: avatarUrl });
    }
  };

  // 邀请
  const handleInvite = async () => {
    if (!inviteEmail) {
      setInviteStatus("请输入邀请邮箱");
      return;
    }
    // 获取userid
    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const userid = match ? match[2] : null;
    if (!userid) {
      setInviteStatus("未获取到用户ID");
      return;
    }
    if (userInfo.invite_left <= 0) {
      setInviteStatus("邀请次数已用完");
      return;
    }
    try {
      const res = await fetch(`${API_BASE_URL}/api/invite`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userid, invite_email: inviteEmail }),
      });
      if (res.ok) {
        const data = await res.json();
        setInviteStatus("邀请成功");
        // 更新剩余次数
        const left = data.invite_left !== undefined ? data.invite_left : userInfo.invite_left - 1;
        setUserInfo(prev => ({ ...prev, invite_left: left }));
        setTempUserInfo(prev => ({ ...prev, invite_left: left }));
        setInviteEmail('');
      } else {
        const errorText = await res.text();
        setInviteStatus("邀请失败：" + errorText);
      }
    } catch (err) {
      console.error("邀请失败", err);
      setInviteStatus("邀请失败，请检查网络");
    }
  };

  // 兑换魔力值
  const handleExchange = async () => {
    const magic = Number(exchangeMagic);
    if (!magic || isNaN(magic) || magic <= 0) return;
    if (magic > userStats.magic) {
      alert("魔力值不足！");
      return;
    }

    // 检查兑换结果是否为整数
    const calculatedExchangeResult = magic / exchangeRate[exchangeType];
    if (!Number.isInteger(calculatedExchangeResult)) {
      alert("兑换结果必须为整数，请调整魔力值！");
      return;
    }

    // 获取userid
    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const userid = match ? match[2] : null;
    if (!userid) {
      alert("未获取到用户ID");
      return;
    }

    console.log("兑换请求参数:", { userid, magic, exchangeType, exchangeResult: calculatedExchangeResult });
    try {
      // 发送兑换请求到后端
      const res = await fetch(`${API_BASE_URL}/api/exchange`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userid,
          magic,
          exchangeType,
          exchangeResult: calculatedExchangeResult
        }),
      });
      // console.log("兑换请求结果:", res);
      if (res.ok) {
        // 兑换成功后重新获取用户数据
        const statsRes = await fetch(`${API_BASE_URL}/api/user-stats?userid=${userid}`);
        if (statsRes.ok) {
          const updatedStats = await statsRes.json();
          setUserStats(updatedStats);
        }
        setExchangeMagic('');
        alert("兑换成功！");
      } else {
        const errorText = await res.text();
        alert("兑换失败：" + errorText);
      }
    } catch (err) {
      console.error("兑换失败", err);
      alert("兑换失败，请检查网络");
    }
  };
  // 删除种子
  const handleDeleteSeed = (seedid) => {
    setUserSeeds(userSeeds.filter((s) => s.seedid !== seedid));
  };

  // 取消收藏
  const handleRemoveFavorite = async (seedid) => {
    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const userid = match ? match[2] : null;
    if (!userid) {
      alert('未获取到用户ID');
      return;
    }
    try {
      const res = await fetch(`${API_BASE_URL}/api/remove-favorite`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userid, seedid }),
      }); if (res.ok) {
        setUserFavorites(userFavorites.filter((s) => (s.seedid || s.seed_id) !== seedid));
        alert('已取消收藏');
      } else {
        alert('取消收藏失败，请重试');
      }
    } catch (err) {
      console.error('取消收藏失败', err);
      alert('取消收藏失败，请检查网络');
    }
  };

  // 申诉提交逻辑
  const handleAppealSubmit = async () => {
    if (!appealTitle || !appealFile) return;
    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const userid = match ? match[2] : null;
    if (!userid) {
      alert('未获取到用户ID');
      return;
    }

    const formData = new FormData();
    formData.append('userid', userid);
    formData.append('content', appealTitle);
    formData.append('file', appealFile);
    try {
      const res = await fetch(`${API_BASE_URL}/api/submit-appeal`, {
        method: 'POST',
        body: formData,
      });
      if (res.ok) {
        alert('申诉已提交');
        setAppealOpen(false);
        setAppealTitle('');
        setAppealFile(null);
      } else {
        const errorText = await res.text();
        alert('申诉失败：' + errorText);
      }
    } catch (err) {
      console.error('申诉失败', err);
      alert('申诉失败，请检查网络');
    }
  };

  // 账号迁移
  const handleMigrationSubmit = async () => {
    if (!appealFile) {
      setMigrationStatus('请选择PDF文件');
      return;
    }
    if (!migrationUpload || isNaN(migrationUpload) || Number(migrationUpload) <= 0) {
      setMigrationStatus('请输入有效的待发放上传量');
      return;
    }

    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const currentUserId = match ? match[2] : null;
    if (!currentUserId) {
      setMigrationStatus('未获取到当前用户ID');
      return;
    }

    try {
      const formData = new FormData();
      formData.append('userid', currentUserId);
      formData.append('file', appealFile);
      formData.append('uploadtogive', migrationUpload);

      const res = await fetch(`${API_BASE_URL}/api/migrate-account`, {
        method: 'POST',
        body: formData,
      });

      if (res.ok) {
        setMigrationStatus('账号迁移申请已提交，请等待管理员审核');
        setTimeout(() => {
          setMigrationOpen(false);
          setAppealFile(null);
          setMigrationUpload('');
          setMigrationStatus('');
        }, 2000);
      } else {
        const errorText = await res.text();
        setMigrationStatus('迁移失败：' + errorText);
      }
    } catch (err) {
      console.error('账号迁移失败', err);
      setMigrationStatus('迁移失败，请检查网络');
    }
  };  return (
    <div className="user-profile-container">
      <div className="profile-grid">
        {/* 用户基本信息卡片 */}
        <div className="profile-card user-info-card">
          <div className="user-avatar-section">
            <div className="avatar-container" onClick={handleAvatarClick}>
              {tempUserInfo.avatar_url ? (
                <img
                  src={tempUserInfo.avatar_url}
                  alt="用户头像"
                  className="user-avatar"
                />
              ) : (
                <AccountCircleIcon style={{ fontSize: 120, color: '#1a237e' }} />
              )}
              <div className="avatar-overlay">
                <span>点击更换头像</span>
              </div>
            </div>
            <h2 className="user-title">用户个人资料</h2>
          </div>

          <div className="user-form">
            <div className="form-group">
              <label className="form-label">
                <PersonIcon style={{ fontSize: 16, marginRight: 4 }} />
                用户名
              </label>
              <input
                className="form-input"
                value={tempUserInfo.username}
                onChange={(e) => handleInputChange("username", e.target.value)}
                placeholder="请输入用户名"
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                <EmailIcon style={{ fontSize: 16, marginRight: 4 }} />
                邮箱
              </label>
              <input
                className="form-input"
                value={tempUserInfo.email}
                readOnly
                style={{ cursor: 'not-allowed' }}
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                <SchoolIcon style={{ fontSize: 16, marginRight: 4 }} />
                学校
              </label>
              <input
                className="form-input"
                value={tempUserInfo.school}
                onChange={(e) => handleInputChange("school", e.target.value)}
                placeholder="请输入学校名称"
              />
            </div>

            <div className="form-group">
              <label className="form-label">账号状态</label>
              <div className="status-indicator">
                <input
                  className="form-input"
                  value={tempUserInfo.account_status === 1 || tempUserInfo.account_status === "1" ? "封禁" : "正常"}
                  readOnly
                />
                <div className={`status-dot ${tempUserInfo.account_status === 1 || tempUserInfo.account_status === "1" ? 'banned' : 'active'}`}></div>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">性别</label>
              <select
                className="form-input"
                value={tempUserInfo.gender}
                onChange={(e) => handleInputChange("gender", e.target.value)}
              >
                <option value="">请选择性别</option>
                <option value="m">男性</option>
                <option value="f">女性</option>
              </select>
            </div>

            {/* 邀请功能 */}
            <div className="invite-section">
              <label className="form-label">邀请功能</label>
              <div className="invite-form">
                <input
                  className="form-input invite-input"
                  type="email"
                  placeholder="输入邀请邮箱"
                  value={inviteEmail}
                  onChange={e => setInviteEmail(e.target.value)}
                  disabled={Number(tempUserInfo.invite_left) === 0}
                />
                <button
                  className="btn btn-primary btn-small"
                  onClick={handleInvite}
                  disabled={Number(tempUserInfo.invite_left) === 0 || !inviteEmail}
                >
                  邀请
                </button>
              </div>
              <div className="invite-counter">
                剩余邀请次数：{tempUserInfo.invite_left || "0"}
              </div>
              {inviteStatus && (
                <div className="invite-status">{inviteStatus}</div>
              )}
            </div>

            <div className="btn-group">
              <button className="btn btn-primary" onClick={handleSave}>
                保存信息
              </button>
              <button className="btn btn-danger" onClick={() => setAppealOpen(true)}>
                用户申诉
              </button>
              <button className="btn btn-warning" onClick={() => setMigrationOpen(true)}>
                账号迁移
              </button>            </div>
          </div>
        </div>

        {/* 活跃度卡片 */}
        <div className="profile-card activity-card">
          <h3 className="activity-title">
            <AutoAwesomeIcon style={{ fontSize: 24, marginRight: 8 }} />
            活跃度统计
          </h3>
          
          <div className="activity-content">
            {/* 魔力值兑换 */}
            <div className="magic-exchange">
              <div className="stat-item">
                <span className="stat-label">当前魔力值</span>
                <span className="stat-value magic">{userStats.magic}</span>
              </div>
              
              <div className="exchange-form">
                <input
                  className="form-input exchange-input"
                  type="number"
                  placeholder="输入兑换魔力值"
                  value={exchangeMagic}
                  onChange={e => setExchangeMagic(e.target.value)}
                />
                <select
                  className="form-input exchange-input"
                  value={exchangeType}
                  onChange={e => setExchangeType(e.target.value)}
                >
                  <option value="uploaded">上传量（增加）</option>
                  <option value="downloaded">下载量（减少）</option>
                  <option value="vip_downloads">VIP下载次数（增加）</option>
                </select>
                <button
                  className="btn btn-primary btn-small"
                  onClick={handleExchange}
                  disabled={
                    !exchangeMagic ||
                    isNaN(exchangeMagic) ||
                    Number(exchangeMagic) <= 0 ||
                    Number(exchangeMagic) > userStats.magic ||
                    !Number.isInteger(exchangeResult)
                  }
                >
                  兑换
                </button>
              </div>
              
              {exchangeMagic && (
                <div className="exchange-result">
                  可兑换：{exchangeResult} {exchangeType === 'vip_downloads' ? '次' : 'MB'}
                  {!Number.isInteger(exchangeResult) && exchangeResult > 0 && (
                    <span style={{ color: '#e53935', marginLeft: 8 }}>
                      (结果必须为整数)
                    </span>
                  )}
                </div>
              )}
            </div>

            {/* 统计数据 */}
            <div className="stats-grid">
              <div className="stat-item">
                <span className="stat-label">
                  <CloudUploadIcon style={{ fontSize: 16, marginRight: 4 }} />
                  上传量
                </span>
                <span className="stat-value upload">
                  {(userStats.upload / 1000000)?.toFixed(2)} MB
                </span>
              </div>
              
              <div className="stat-item">
                <span className="stat-label">
                  <CloudDownloadIcon style={{ fontSize: 16, marginRight: 4 }} />
                  下载量
                </span>
                <span className="stat-value download">
                  {(userStats.download / 1000000)?.toFixed(2)} MB
                </span>
              </div>
              
              <div className="stat-item">
                <span className="stat-label">上传/下载比</span>
                <span className="stat-value ratio">
                  {userStats.download === 0 ? "∞" : (userStats.upload / userStats.download).toFixed(2)}
                </span>
              </div>
              
              <div className="stat-item">
                <span className="stat-label">VIP下载次数</span>
                <span className="stat-value vip">{userStats.viptime}</span>
              </div>
            </div>
          </div>
        </div>        {/* 个人上传种子列表 */}
        <div className="profile-card seeds-card">
          <h3 className="list-title">
            <CloudUploadIcon style={{ fontSize: 24, marginRight: 8 }} />
            个人上传种子列表
          </h3>
          
          <div className="list-container">
            {userSeeds.length === 0 ? (
              <div className="empty-state">
                <EmptyIcon className="empty-icon" />
                <span>暂无上传种子</span>
              </div>
            ) : (
              <ul className="seeds-list">
                {userSeeds.map((seed, idx) => (
                  <li
                    key={seed.seedid || idx}
                    className="seed-item"
                    onClick={e => {
                      if (e.target.classList.contains('delete-btn')) return;
                      navigate(`/torrent/${seed.seed_id}`);
                    }}
                  >
                    <span className="seed-title">{seed.title}</span>
                    <span className="seed-tags">{seed.tags}</span>
                    <span className="seed-stats">人气: {seed.downloadtimes}</span>
                    <div className="seed-actions">
                      <button
                        className="btn btn-danger btn-small delete-btn"
                        onClick={async e => {
                          e.stopPropagation();
                          const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
                          const userid = match ? match[2] : null;
                          if (!userid) {
                            alert('未获取到用户ID');
                            return;
                          }
                          try {
                            const res = await fetch(`${API_BASE_URL}/api/delete-seed`, {
                              method: 'POST',
                              headers: { 'Content-Type': 'application/json' },
                              body: JSON.stringify({ seed_id: seed.seed_id, userid }),
                            });
                            if (res.ok) {
                              setUserSeeds(userSeeds.filter((s, i) => (s.seed_id || i) !== (seed.seed_id || idx)));
                            } else {
                              alert('删除失败，请重试');
                            }
                          } catch (err) {
                            alert('删除失败，请检查网络');
                          }
                        }}
                      >
                        删除
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* 个人收藏种子列表 */}
        <div className="profile-card favorites-card">
          <h3 className="list-title">
            <FavoriteIcon style={{ fontSize: 24, marginRight: 8 }} />
            个人收藏种子列表
          </h3>
          
          <div className="list-container">
            {userFavorites.length === 0 ? (
              <div className="empty-state">
                <FavoriteIcon className="empty-icon" />
                <span>暂无收藏种子</span>
              </div>
            ) : (
              <ul className="seeds-list">
                {userFavorites.map((seed, idx) => (
                  <li
                    key={seed.seedid || idx}
                    className="seed-item"
                    onClick={e => {
                      if (e.target.classList.contains('remove-favorite-btn')) return;
                      navigate(`/torrent/${seed.seedid || seed.seed_id}`);
                    }}
                  >
                    <span className="seed-title">{seed.seed.title}</span>
                    <span className="seed-tags">{seed.seed.tags}</span>
                    <span className="seed-stats">人气: {seed.seed.downloadtimes}</span>
                    <div className="seed-actions">
                      <button
                        className="btn btn-warning btn-small remove-favorite-btn"
                        onClick={e => {
                          e.stopPropagation();
                          handleRemoveFavorite(seed.seedid || seed.seed_id);
                        }}
                      >
                        取消收藏
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>      
      {/* 申诉弹窗 */}
      <Dialog open={appealOpen} onClose={() => setAppealOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle style={{ background: '#f8faff', color: '#1a237e', fontWeight: 600 }}>
          提交申诉
        </DialogTitle>
        <DialogContent style={{ padding: '24px', background: '#ffffff' }}>
          <div style={{ marginBottom: 20 }}>
            <TextField
              label="申诉主题"
              fullWidth
              value={appealTitle}
              onChange={e => setAppealTitle(e.target.value)}
              variant="outlined"
              style={{ marginBottom: 16 }}
            />
          </div>
          <div>
            <input
              type="file"
              accept=".pdf"
              onChange={e => {
                const file = e.target.files[0];
                if (file && file.type !== 'application/pdf') {
                  alert('请选择PDF文件');
                  e.target.value = '';
                  setAppealFile(null);
                } else {
                  setAppealFile(file);
                }
              }}
              style={{ 
                marginTop: 8,
                padding: '12px',
                border: '2px dashed #e0e7ff',
                borderRadius: '8px',
                width: '100%',
                background: '#f8faff'
              }}
            />
            <div style={{ 
              fontSize: 12, 
              color: '#666', 
              marginTop: 8,
              padding: '8px 12px',
              background: '#f0f4ff',
              borderRadius: '6px'
            }}>
              请选择PDF文件（最大100MB）
            </div>
          </div>
        </DialogContent>
        <DialogActions style={{ padding: '16px 24px', background: '#f8faff' }}>
          <Button 
            onClick={handleAppealSubmit} 
            variant="contained" 
            disabled={!appealTitle || !appealFile}
            style={{
              background: (!appealTitle || !appealFile) ? '#ccc' : 'linear-gradient(135deg, #1a237e 0%, #3f51b5 100%)',
              color: 'white',
              fontWeight: 600
            }}
          >
            提交申诉
          </Button>
          <Button 
            onClick={() => setAppealOpen(false)} 
            variant="outlined"
            style={{ color: '#1a237e', borderColor: '#1a237e' }}
          >
            取消
          </Button>
        </DialogActions>
      </Dialog>

      {/* 账号迁移弹窗 */}
      <Dialog open={migrationOpen} onClose={() => setMigrationOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle style={{ background: '#f8faff', color: '#1a237e', fontWeight: 600 }}>
          账号迁移
        </DialogTitle>
        <DialogContent style={{ padding: '24px', background: '#ffffff' }}>
          <div style={{ marginBottom: 20 }}>
            <TextField
              label="待发放上传量"
              type="number"
              fullWidth
              value={migrationUpload}
              onChange={e => setMigrationUpload(e.target.value)}
              variant="outlined"
              inputProps={{ min: 1 }}
              style={{ marginBottom: 16 }}
            />
          </div>
          <div>
            <input
              type="file"
              accept=".pdf"
              onChange={e => {
                const file = e.target.files[0];
                if (file && file.type !== 'application/pdf') {
                  alert('请选择PDF文件');
                  e.target.value = '';
                  setAppealFile(null);
                } else {
                  setAppealFile(file);
                }
              }}
              style={{ 
                marginTop: 8,
                padding: '12px',
                border: '2px dashed #e0e7ff',
                borderRadius: '8px',
                width: '100%',
                background: '#f8faff'
              }}
            />
            <div style={{ 
              fontSize: 12, 
              color: '#666', 
              marginTop: 8,
              padding: '8px 12px',
              background: '#f0f4ff',
              borderRadius: '6px'
            }}>
              请选择PDF文件（最大10MB）
            </div>
          </div>
          {migrationStatus && (
            <div style={{ 
              color: migrationStatus.includes('成功') ? '#43a047' : '#e53935', 
              fontSize: 14, 
              marginTop: 16,
              padding: '12px',
              borderRadius: '8px',
              background: migrationStatus.includes('成功') ? 'rgba(67, 160, 71, 0.1)' : 'rgba(229, 57, 53, 0.1)',
              border: `1px solid ${migrationStatus.includes('成功') ? 'rgba(67, 160, 71, 0.3)' : 'rgba(229, 57, 53, 0.3)'}`
            }}>
              {migrationStatus}
            </div>
          )}
        </DialogContent>
        <DialogActions style={{ padding: '16px 24px', background: '#f8faff' }}>
          <Button 
            onClick={handleMigrationSubmit} 
            variant="contained"
            style={{
              background: 'linear-gradient(135deg, #ff9800 0%, #ffa726 100%)',
              color: 'white',
              fontWeight: 600
            }}
          >
            提交迁移
          </Button>
          <Button 
            onClick={() => setMigrationOpen(false)} 
            variant="outlined"
            style={{ color: '#1a237e', borderColor: '#1a237e' }}
          >
            取消
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}