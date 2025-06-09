import React, { useEffect, useState } from "react";
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
import "./HomePage.css";

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

export default function HomePage() {
    const navigate = useNavigate();
    const [recommendSeeds, setRecommendSeeds] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // 获取当前登录用户ID
        const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
        const userId = match ? match[2] : null;

        if (!userId) {
            setRecommendSeeds([]);
            setLoading(false);
            return;
        }

        setLoading(true);
        fetch("http://10.126.59.25:5000/recommend", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ user_id: userId })
        })
            .then(res => res.json())
            .then(data => {
                setRecommendSeeds(Array.isArray(data.recommend) ? data.recommend : []);
                setLoading(false);
            })
            .catch(() => {
                setRecommendSeeds([]);
                setLoading(false);
            });
    }, []);

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
                            className={`emerald-nav-item ${item.label === "首页" ? "active" : ""}`}
                            data-type={item.type}
                            onClick={() => navigate(item.path)}
                        >
                            {item.icon}
                            <span className="emerald-nav-label">{item.label}</span>
                        </div>
                    ))}
                </nav>

                {/* NeuraFlux种子列表 */}
                <div className="emerald-table-section">
                    <table className="emerald-table">
                        <thead>
                            <tr>
                                <th>分类标签</th>
                                <th>标题</th>
                                <th>发布者</th>
                                <th>大小</th>
                                <th>热度</th>
                                <th>折扣倍率</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr>
                                    <td colSpan={6} style={{ textAlign: "center", color: "#888" }}>正在加载推荐种子...</td>
                                </tr>
                            ) : recommendSeeds.length === 0 ? (
                                <tr>
                                    <td colSpan={6} style={{ textAlign: "center", color: "#888" }}>暂无推荐数据</td>
                                </tr>
                            ) : (
                                recommendSeeds.map((seed) => (
                                    <tr key={seed.seed_id}>
                                        <td>{seed.tags}</td>
                                        <td>
                                            <a href={`/torrent/${seed.seed_id}`}>
                                                {seed.title}
                                            </a>
                                        </td>
                                        <td>{seed.username}</td>
                                        <td>{seed.size}</td>
                                        <td>{seed.popularity}</td>
                                        <td>{seed.discount == null ? 1 : seed.discount}</td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}