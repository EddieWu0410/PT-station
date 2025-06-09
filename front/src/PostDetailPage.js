import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
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
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import ReplyIcon from "@mui/icons-material/Reply";
import VisibilityIcon from "@mui/icons-material/Visibility";
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

// 论坛详情页文字雨内容
const forumDetailTexts = [
  "讨论", "回复", "交流", "观点", "见解", "思考", "分享", "互动",
  "对话", "评论", "深度", "专业", "洞察", "分析", "探讨", "解答"
];

export default function PostDetailPage() {
    const { postId } = useParams();
    const navigate = useNavigate();
    const [post, setPost] = useState(null);
    const [replies, setReplies] = useState([]);
    const [newReply, setNewReply] = useState('');
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
    }, []);

    // function to load post details and its replies
    const fetchDetail = () => {
        fetch(`${API_BASE_URL}/api/forum-detail?postid=${postId}`)
            .then(res => res.json())
            .then(data => {
                console.log("Fetched post detail:", data);
                const p = data.post || data;
                const formattedPost = {
                    post_id: p.postid,
                    title: p.posttitle,
                    content: p.postcontent,
                    author_id: p.postuserid,
                    author_name: p.author?.username || '',
                    created_at: new Date(p.posttime).toLocaleString(),
                    reply_count: p.replytime,
                    view_count: p.readtime,
                };
                const formattedReplies = (data.replies || []).map(r => ({
                    reply_id: r.replyid,
                    post_id: r.postid || postId,
                    content: r.content,
                    author_id: r.authorid,
                    author_name: r.author?.username || '',
                    created_at: new Date(r.createdAt).toLocaleString(),
                }));
                setPost(formattedPost);
                setReplies(formattedReplies);
            })
            .catch(err => console.error(err));
    };

    useEffect(() => {
        fetchDetail();
    }, [postId]);

    // post a new reply to backend
    const handleReply = () => {
        const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
        const userId = match ? match[2] : null;
        if (!userId) {
            alert('请先登录后再回复');
            return;
        }
        fetch(`${API_BASE_URL}/api/forum-reply`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ postid: postId, replycontent: newReply, replyuserid: userId }),
        })
            .then(res => res.json())
            .then(() => {
                setNewReply('');
                fetchDetail();
            })
            .catch(err => console.error(err));
    };

    if (!post) return (
        <div className="emerald-home-container">
            <div className="emerald-content">
                <div style={{ 
                    textAlign: 'center', 
                    padding: '100px 20px', 
                    color: '#2d5016', 
                    fontSize: '18px',
                    background: 'rgba(255, 255, 255, 0.95)',
                    borderRadius: '25px',
                    margin: '50px auto',
                    maxWidth: '600px'
                }}>
                    <ForumIcon style={{ fontSize: 64, marginBottom: '20px', color: '#90ee90' }} />
                    <div>加载帖子详情中...</div>
                </div>
            </div>
        </div>
    );

    return (
        <div className="emerald-home-container">
            {/* 文字雨背景效果 */}
            <div className="forum-text-rain">
                {forumDetailTexts.map((text, index) => (
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
                <div className="garden-element">💭</div>
                <div className="garden-element">📖</div>
                <div className="garden-element">💡</div>
                <div className="garden-element">✨</div>
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

                {/* 返回按钮 */}
                <div className="post-detail-header">
                    <button className="back-to-forum-btn" onClick={() => navigate('/forum')}>
                        <ArrowBackIcon style={{ marginRight: '8px', fontSize: '20px' }} />
                        返回论坛
                    </button>
                </div>

                {/* 帖子详情内容区域 */}
                <div className="emerald-content-section">
                    {/* 原帖内容 */}
                    <div className="post-detail-main">
                        <div className="post-detail-header-info">
                            <div className="post-author-section">
                                <div className="post-author-avatar">
                                    <AccountCircleIcon style={{ fontSize: 48, color: '#2d5016' }} />
                                </div>
                                <div className="post-author-details">
                                    <h2 className="post-author-name">{post.author_name}</h2>
                                    <span className="post-publish-time">{post.created_at}</span>
                                </div>
                            </div>
                            <div className="post-stats-section">
                                <div className="stat-badge">
                                    <VisibilityIcon style={{ fontSize: '18px', marginRight: '4px' }} />
                                    <span>{post.view_count} 浏览</span>
                                </div>
                                <div className="stat-badge">
                                    <ReplyIcon style={{ fontSize: '18px', marginRight: '4px' }} />
                                    <span>{post.reply_count} 回复</span>
                                </div>
                            </div>
                        </div>
                        
                        <div className="post-content-section">
                            <h1 className="post-detail-title">{post.title}</h1>
                            <div className="post-detail-content">
                                {post.content}
                            </div>
                        </div>
                    </div>

                    {/* 回复列表 */}
                    <div className="replies-section">
                        <h3 className="replies-title">
                            <ReplyIcon style={{ marginRight: '8px', color: '#2d5016' }} />
                            全部回复 ({replies.length})
                        </h3>
                        
                        {replies.length > 0 ? (
                            <div className="replies-list">
                                {replies.map((reply, index) => (
                                    <div key={reply.reply_id} className="reply-card">
                                        <div className="reply-index">#{index + 1}</div>
                                        <div className="reply-content-wrapper">
                                            <div className="reply-author-info">
                                                <AccountCircleIcon style={{ fontSize: 24, color: '#2d5016', marginRight: '8px' }} />
                                                <span className="reply-author-name">{reply.author_name}</span>
                                                <span className="reply-time">{reply.created_at}</span>
                                            </div>
                                            <div className="reply-content">{reply.content}</div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="no-replies">
                                <ForumIcon style={{ fontSize: 48, color: '#90ee90', marginBottom: '16px' }} />
                                <p>还没有人回复，快来抢沙发吧！</p>
                            </div>
                        )}
                    </div>

                    {/* 回复输入框 */}
                    <div className="reply-input-section">
                        <h3 className="reply-input-title">发表回复</h3>
                        <div className="reply-input-wrapper">
                            <textarea
                                placeholder="写下你的想法和观点..."
                                value={newReply}
                                onChange={e => setNewReply(e.target.value)}
                                className="reply-textarea"
                                rows="4"
                            />
                            <div className="reply-actions">
                                <div className="reply-tips">
                                    💡 支持理性讨论，拒绝恶意灌水
                                </div>
                                <button onClick={handleReply} className="submit-reply-btn">
                                    <ReplyIcon style={{ marginRight: '6px', fontSize: '18px' }} />
                                    发布回复
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}