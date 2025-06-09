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

// 电影地区标签
const areaTabs = [
    { label: "华语电影", value: "chinese" },
    { label: "港台电影", value: "hk_tw" },
    { label: "欧美电影", value: "western" },
    { label: "日韩电影", value: "jp_kr" },
    { label: "其他电影", value: "others" }
];

export default function MoviePage() {
    const navigate = useNavigate();
    const [searchText, setSearchText] = React.useState('');
    const [userInfo, setUserInfo] = useState({ avatar_url: '', username: '' });
    const [userPT, setUserPT] = useState({ magic: 0, ratio: 0, upload: 0, download: 0 });
    const [activeTab, setActiveTab] = React.useState(0);
    const [movieList, setMovieList] = React.useState([]);

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

    // 每个tab对应的电影类型
    const movieTypesList = [
        ["华语电影（大陆）", "欧美电影", "日韩电影", "港台电影", "其他"], // 大陆
        ["港台动作", "港台爱情", "港台喜剧", "港台其他"], // 港台
        ["欧美动作", "欧美科幻", "欧美剧情", "欧美其他"], // 欧美
        ["日韩动画", "日韩爱情", "日韩其他"], // 日韩
        ["其他类型1", "其他类型2"] // 其他
    ];
    const movieTypes = movieTypesList[activeTab] || [];

    React.useEffect(() => {
        const area = areaTabs[activeTab].label;
        fetch(`${API_BASE_URL}/api/get-seed-list-by-tag?tag=${encodeURIComponent(area)}`)
            .then(res => res.json())
            .then(data => {
                setMovieList(data);
            })
            .catch(() => setMovieList([]));
    }, [activeTab]);

    // 搜索按钮处理
    const handleSearch = () => {
        const area = areaTabs[activeTab].label;
        fetch(`${API_BASE_URL}/api/search-seeds?tag=${encodeURIComponent(area)}&keyword=${encodeURIComponent(searchText)}`)
            .then(res => res.json())
            .then(data => {
                setMovieList(data);
            })
            .catch(() => setMovieList([]));
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
                            魔力值: <span className="emerald-stat-value">12,345</span>
                        </span>
                        <span className="emerald-stat-item">
                            分享率: <span className="emerald-stat-value">2.56</span>
                        </span>
                        <span className="emerald-stat-item">
                            上传: <span className="emerald-stat-value">100GB</span>
                        </span>
                        <span className="emerald-stat-item">
                            下载: <span className="emerald-stat-value">50GB</span>
                        </span>
                    </div>
                </div>

                {/* NeuraFlux导航栏 */}
                <nav className="emerald-nav-bar">
                    {navItems.map((item) => (
                        <div
                            key={item.label}
                            className={`emerald-nav-item ${item.label === "电影" ? "active" : ""}`}
                            data-type={item.type}
                            onClick={() => navigate(item.path)}
                        >
                            {item.icon}
                            <span className="emerald-nav-label">{item.label}</span>
                        </div>
                    ))}
                </nav>

                {/* 电影内容区域 */}
                <div className="emerald-content-section">
                    <h1 className="emerald-page-title">🎬 电影资源</h1>
                    <p style={{ textAlign: 'center', color: '#2d5016', fontSize: '18px' }}>
                        欢迎来到NeuraFlux电影频道，这里有最新最热门的电影资源
                    </p>
                </div>
            </div>
        </div>
    );
}
