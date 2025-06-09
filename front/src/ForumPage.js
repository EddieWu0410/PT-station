import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
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
import SearchIcon from "@mui/icons-material/Search";
import PostAddIcon from "@mui/icons-material/PostAdd";
import "./SharedStyles.css";

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

// 论坛文字雨内容
const forumTexts = [
  "讨论", "交流", "分享", "观点", "话题", "回复", "发帖", "社区",
  "对话", "互动", "评论", "建议", "意见", "经验", "心得", "推荐",
  "新手", "老鸟", "攻略", "指南", "教程", "资源", "福利", "活动"
];

export default function ForumPage() {
    const navigate = useNavigate();
    const [posts, setPosts] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [userInfo, setUserInfo] = useState({ avatar_url: '', username: '' });
    const [userPT, setUserPT] = useState({ magic: 0, ratio: 0, upload: 0, download: 0 });

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

        // 获取论坛帖子
        fetch(`${API_BASE_URL}/api/forum`)
            .then(res => {
                // console.log("fetch raw response:", res);
                return res.json();
            })
            .then(data => {
                // console.log("fetch forum data:", data);
                const formattedPosts = data.map(post => ({
                    post_id: post.postid,
                    title: post.posttitle,
                    content: post.postcontent,
                    author_id: post.postuserid,
                    author_name: post.author?.username || '',
                    created_at: new Date(post.posttime).toLocaleString(),
                    reply_count: post.replytime,
                    view_count: post.readtime,
                }));
                setPosts(formattedPosts);
            })
            .catch(() => setPosts([]));
    }, []);
    
    // Handler to perform search based on input value
    const handleSearch = () => {
      fetch(`${API_BASE_URL}/api/search-posts?keyword=${encodeURIComponent(searchQuery)}`)
        .then(res => res.json())
        .then(data => {
          const formattedPosts = data.map(post => ({
            post_id: post.postid,
            title: post.posttitle,
            content: post.postcontent,
            author_id: post.postuserid,
            author_name: post.author?.username || '',
            created_at: new Date(post.posttime).toLocaleString(),
            reply_count: post.replytime,
            view_count: post.readtime,
          }));
          setPosts(formattedPosts);
        })
        .catch(() => setPosts([]));
    };

    return (
        <div className="emerald-home-container">
            {/* 文字雨背景效果 */}
            <div className="forum-text-rain">
                {forumTexts.map((text, index) => (
                    <div key={index} className="text-drop" style={{
                        left: `${(index * 3.5) % 100}%`,
                        animationDelay: `${(index * 0.8) % 10}s`,
                        animationDuration: `${8 + (index % 5)}s`
                    }}>
                        {text}
                    </div>
                ))}
            </div>

            {/* 浮动园林装饰元素 */}
            <div className="floating-garden-elements">
                <div className="garden-element">💬</div>
                <div className="garden-element">📝</div>
                <div className="garden-element">🗨️</div>
                <div className="garden-element">✍️</div>
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
                            className={`emerald-nav-item ${item.label === "论坛" ? "active" : ""}`}
                            data-type={item.type}
                            onClick={() => navigate(item.path)}
                        >
                            {item.icon}
                            <span className="emerald-nav-label">{item.label}</span>
                        </div>
                    ))}
                </nav>

                {/* 论坛内容区域 */}
                <div className="emerald-content-section">
                    <h1 className="emerald-page-title">💬 NeuraFlux论坛</h1>
                    <p style={{ textAlign: 'center', color: '#2d5016', fontSize: '18px', marginBottom: '30px' }}>
                        欢迎来到NeuraFlux社区论坛，这里是知识与思想的碰撞之地
                    </p>
                    
                    {/* 论坛工具栏 */}
                    <div className="forum-toolbar">
                        <div className="search-section">
                            <div className="search-input-container">
                                <SearchIcon className="search-icon" />
                                <input
                                    type="text"
                                    placeholder="搜索论坛话题..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="forum-search-input"
                                />
                            </div>
                            <button onClick={handleSearch} className="forum-search-btn">
                                搜索
                            </button>
                        </div>
                        <button className="new-post-btn" onClick={() => navigate('/publish')}>
                            <PostAddIcon style={{ marginRight: '8px' }} />
                            发布新话题
                        </button>
                    </div>

                    {/* 帖子列表 */}
                    <div className="forum-posts-container">
                        {posts.length > 0 ? (
                            posts.map((post) => (
                                <div
                                    key={post.post_id}
                                    className="forum-post-card"
                                    onClick={() => navigate(`/forum/${post.post_id}`)}
                                >
                                    <div className="post-header">
                                        <div className="post-author-info">
                                            <div className="author-avatar">
                                                <AccountCircleIcon style={{ fontSize: 28, color: '#2d5016' }} />
                                            </div>
                                            <div className="author-details">
                                                <span className="author-name">{post.author_name}</span>
                                                <span className="post-time">{post.created_at}</span>
                                            </div>
                                        </div>
                                        <div className="post-stats">
                                            <span className="stat-item">👁 {post.view_count}</span>
                                            <span className="stat-item">💬 {post.reply_count}</span>
                                        </div>
                                    </div>
                                    
                                    <div className="post-content">
                                        <h3 className="post-title">{post.title}</h3>
                                        <p className="post-preview">{post.content}</p>
                                    </div>
                                    
                                    <div className="post-footer">
                                        <div className="post-tags">
                                            <span className="post-tag">讨论</span>
                                        </div>
                                        <div className="post-actions">
                                            <span className="action-btn">查看详情 →</span>
                                        </div>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className="empty-state">
                                <ForumIcon style={{ fontSize: 64, color: '#90ee90', marginBottom: '16px' }} />
                                <h3 style={{ color: '#2d5016', marginBottom: '8px' }}>暂无帖子</h3>
                                <p style={{ color: '#666', marginBottom: '20px' }}>快来发布第一个话题吧！</p>
                                <button className="new-post-btn" onClick={() => navigate('/publish')}>
                                    <PostAddIcon style={{ marginRight: '8px' }} />
                                    发布话题
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}