import React, { useState, useEffect } from "react";
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
import "./SharedStyles.css";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "./config";

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

// 动漫地区标签
const areaTabs = [
  { label: "国创动漫", value: "chinese" },
  { label: "日系动漫", value: "japanese" },
  { label: "欧美动漫", value: "western" },
  { label: "韩系动漫", value: "korean" },
  { label: "其他动漫", value: "others" }
];

export default function AnimePage() {
  const navigate = useNavigate();
  const [searchText, setSearchText] = useState('');
  const [userInfo, setUserInfo] = useState({ avatar_url: '', username: '' });
  const [userPT, setUserPT] = useState({ magic: 0, ratio: 0, upload: 0, download: 0 });
  const [activeTab, setActiveTab] = useState(0);
  const [animeList, setAnimeList] = useState([]);

  useEffect(() => {
    const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
    const userId = match ? match[2] : null;
    if (!userId) return;
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
  }, []);
  useEffect(() => {
    // 根据选中的标签获取动漫列表
    const area = areaTabs[activeTab].label;
    fetch(`${API_BASE_URL}/api/get-seed-list-by-tag?tag=${encodeURIComponent(area)}`)
      .then(res => res.json())
      .then(data => setAnimeList(data))
      .catch(() => setAnimeList([]));
  }, [activeTab]);

  // 搜索按钮处理
  const handleSearch = () => {
    const area = areaTabs[activeTab].label;
    fetch(`${API_BASE_URL}/api/search-seeds?tag=${encodeURIComponent(area)}&keyword=${encodeURIComponent(searchText)}`)
      .then(res => res.json())
      .then(data => {
        console.log('搜索返回数据:', data);
        setAnimeList(data);
      })
      .catch(() => setAnimeList([]));
  };

  const animeTypesList = [
    ["华语动漫", "国产3D", "国产2D", "华语其他"], // 国创动漫
    ["日漫热血", "日漫治愈", "日漫搞笑", "日漫其他"], // 日系动漫
    ["欧美冒险", "欧美科幻", "欧美喜剧", "欧美其他"], // 欧美动漫
    ["韩漫爱情", "韩漫奇幻", "韩漫其他"], // 韩系动漫
    ["独立动画", "短篇动画", "其他类型"] // 其他动漫
  ];
  const animeTypes = animeTypesList[activeTab] || [];
  return (
    <div className="emerald-home-container">
      {/* 流星雨背景效果 */}
      <div className="meteor-shower">
        <div className="meteor">💫</div>
        <div className="meteor">⭐</div>
        <div className="meteor">✨</div>
        <div className="meteor">🌟</div>
        <div className="meteor">💫</div>
        <div className="meteor">⭐</div>
        <div className="meteor">✨</div>
        <div className="meteor">🌟</div>
        <div className="meteor">💫</div>
        <div className="meteor">⭐</div>
      </div>

      {/* 浮动园林装饰元素 */}
      <div className="floating-garden-elements">
        <div className="garden-element">🌿</div>
        <div className="garden-element">🦋</div>
        <div className="garden-element">🌺</div>
        <div className="garden-element">🌸</div>
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
              className={`emerald-nav-item ${item.label === "动漫" ? "active" : ""}`}
              data-type={item.type}
              onClick={() => navigate(item.path)}
            >
              {item.icon}
              <span className="emerald-nav-label">{item.label}</span>
            </div>
          ))}
        </nav>

        {/* 动漫内容区域 */}
        <div className="emerald-content-section">
          <h1 className="emerald-page-title">🎌 动漫资源</h1>
          <p style={{ textAlign: 'center', color: '#2d5016', fontSize: '18px', marginBottom: '30px' }}>
            欢迎来到NeuraFlux动漫频道，这里有最新最热门的动漫资源
          </p>
          
          {/* 搜索栏 */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'center', 
            marginBottom: '30px',
            gap: '15px'
          }}>
            <input
              type="text"
              placeholder="搜索动漫资源..."
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              style={{
                padding: '12px 20px',
                borderRadius: '20px',
                border: '2px solid rgba(144, 238, 144, 0.3)',
                background: 'rgba(240, 255, 240, 0.5)',
                fontSize: '16px',
                width: '300px',
                fontFamily: 'Lora, serif'
              }}
            />
            <button
              onClick={handleSearch}
              style={{
                padding: '12px 24px',
                borderRadius: '20px',
                border: 'none',
                background: 'linear-gradient(135deg, #2d5016 0%, #90ee90 100%)',
                color: 'white',
                fontSize: '16px',
                cursor: 'pointer',
                fontFamily: 'Lora, serif'
              }}
            >
              搜索
            </button>
          </div>

          {/* 地区分类标签 */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'center', 
            marginBottom: '30px',
            gap: '15px',
            flexWrap: 'wrap'
          }}>
            {areaTabs.map((tab, index) => (
              <button
                key={tab.value}
                onClick={() => setActiveTab(index)}
                style={{
                  padding: '10px 20px',
                  borderRadius: '15px',
                  border: '2px solid rgba(144, 238, 144, 0.3)',
                  background: activeTab === index 
                    ? 'linear-gradient(135deg, #90ee90 0%, #2d5016 100%)' 
                    : 'rgba(240, 255, 240, 0.3)',
                  color: activeTab === index ? 'white' : '#2d5016',
                  fontSize: '14px',
                  cursor: 'pointer',
                  fontFamily: 'Lora, serif',
                  transition: 'all 0.3s ease'
                }}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* 动漫列表 */}
          <div className="emerald-table-section">
            <table className="emerald-table">
              <thead>
                <tr>
                  <th>动漫类型</th>
                  <th>标题</th>
                  <th>发布者</th>
                  <th>大小</th>
                  <th>热度</th>
                  <th>折扣倍率</th>
                </tr>
              </thead>
              <tbody>
                {animeList.length > 0 ? (
                  animeList.map((item, index) => (
                    <tr key={item.id || index}>
                      <td>{item.seedtag}</td>
                      <td>
                        <a href={`/torrent/${item.seedid}`}>
                          {item.title}
                        </a>
                      </td>
                      <td>{item.username}</td>
                      <td>{item.seedsize}</td>
                      <td>{item.downloadtimes}</td>
                      <td>{item.discount == null ? 1 : item.discount}</td>
                    </tr>
                  ))
                ) : (
                  animeTypes.map((type, index) => (
                    <tr key={type}>
                      <td>{type}</td>
                      <td>
                        <a href={`/torrent/${type}`}>
                          种子{index + 1}
                        </a>
                      </td>
                      <td>发布者{index + 1}</td>
                      <td>--</td>
                      <td>--</td>
                      <td>1</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

// Pagination组件暂时不使用
function Pagination() {
  const [page, setPage] = React.useState(3);
  const total = 5;
  return (
    <div className="pagination">
      <button onClick={() => setPage(p => Math.max(1, p - 1))} disabled={page === 1}>上一页</button>
      <span className="page-num">{page}/{total}</span>
      <button onClick={() => setPage(p => Math.min(total, p + 1))} disabled={page === total}>下一页</button>
      <span className="page-info">第 <b>{page}</b> 页</span>
    </div>
  );
}