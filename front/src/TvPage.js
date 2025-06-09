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
import { useNavigate } from "react-router-dom";
import "./SharedStyles.css";
import { API_BASE_URL } from "./config";

// 导航栏
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

// 剧集地区标签
const areaTabs = [
    { label: "华语剧集", value: "chinese" },
    { label: "港台剧集", value: "hk_tw" },
    { label: "欧美剧集", value: "western" },
    { label: "日韩剧集", value: "jp_kr" },
    { label: "其他剧集", value: "others" }
];

export default function TvPage() {
    const navigate = useNavigate();
    const [searchText, setSearchText] = React.useState('');
    const [userInfo, setUserInfo] = useState({ avatar_url: '', username: '' });
    const [userPT, setUserPT] = useState({ magic: 0, ratio: 0, upload: 0, download: 0 });
    const [activeTab, setActiveTab] = React.useState(0);
    const [tvList, setTvList] = React.useState([]);

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

    // 每个tab对应的剧集类型
    const tvTypesList = [
        ["华语电视剧", "华语综艺", "华语纪录片", "华语其他"], // 华语
        ["港台电视剧", "港台综艺", "港台其他"], // 港台
        ["欧美电视剧", "欧美综艺", "欧美纪录片", "欧美其他"], // 欧美
        ["日韩电视剧", "日韩综艺", "日韩其他"], // 日韩
        ["其他类型1", "其他类型2"] // 其他
    ];
    const tvTypes = tvTypesList[activeTab] || [];

    React.useEffect(() => {
        const area = areaTabs[activeTab].label;
        fetch(`${API_BASE_URL}/api/get-seed-list-by-tag?tag=${encodeURIComponent(area)}`)
            .then(res => res.json())
            .then(data => {
                setTvList(data);
            })
            .catch(() => setTvList([]));
    }, [activeTab]);

    // 搜索按钮处理
    const handleSearch = () => {
        const area = areaTabs[activeTab].label;
        fetch(`${API_BASE_URL}/api/search-seeds?tag=${encodeURIComponent(area)}&keyword=${encodeURIComponent(searchText)}`)
            .then(res => res.json())
            .then(data => {
                setTvList(data);
            })
            .catch(() => setTvList([]));
    };

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
                        <AccountCircleIcon style={{ fontSize: 38, color: 'white' }} />
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
                            className={`emerald-nav-item ${item.label === "剧集" ? "active" : ""}`}
                            data-type={item.type}
                            onClick={() => navigate(item.path)}
                        >
                            {item.icon}
                            <span className="emerald-nav-label">{item.label}</span>
                        </div>
                    ))}
                </nav>

                {/* 剧集内容区域 */}
                <div className="emerald-content-section">
                    <h1 className="emerald-page-title">📺 剧集资源</h1>
                    <p style={{ textAlign: 'center', color: '#2d5016', fontSize: '18px', marginBottom: '30px' }}>
                        欢迎来到NeuraFlux剧集频道，这里有最新最热门的电视剧、综艺、纪录片资源
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
                            placeholder="搜索剧集资源..."
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

                    {/* 剧集列表 */}
                    <div className="emerald-table-section">
                        <table className="emerald-table">
                            <thead>
                                <tr>
                                    <th>分类标签</th>
                                    <th>剧集标题</th>
                                    <th>热门指数</th>
                                    <th>发布者</th>
                                </tr>
                            </thead>
                            <tbody>
                                {tvList.length > 0 ? (
                                    tvList.map((tv, index) => (
                                        <tr key={tv.id || index}>
                                            <td>{tv.tags || areaTabs[activeTab].label}</td>
                                            <td>
                                                <a href={`/torrent/${tv.id || index}`}>
                                                    {tv.title || `剧集资源 ${index + 1}`}
                                                </a>
                                            </td>
                                            <td>{tv.popularity || Math.floor(Math.random() * 100) + 1}</td>
                                            <td>{tv.user?.username || '匿名用户'}</td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colspan="4" style={{ 
                                            textAlign: 'center', 
                                            color: '#666', 
                                            fontStyle: 'italic',
                                            padding: '40px 20px'
                                        }}>
                                            暂无{areaTabs[activeTab].label}资源，请稍后再试
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
