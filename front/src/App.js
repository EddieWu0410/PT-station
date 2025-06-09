import React from "react";
import { BrowserRouter as Router, Routes, Route, useNavigate, Link, Navigate } from "react-router-dom";
import HomeIcon from "@mui/icons-material/Home";
import MovieIcon from "@mui/icons-material/Movie";
import EmailIcon from "@mui/icons-material/Email";
import MusicNoteIcon from "@mui/icons-material/MusicNote";
import EmojiPeopleIcon from "@mui/icons-material/EmojiPeople";
import SportsEsportsIcon from "@mui/icons-material/SportsEsports";
import SportsMartialArtsIcon from "@mui/icons-material/SportsMartialArts";
import PersonIcon from "@mui/icons-material/Person";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import ForumIcon from "@mui/icons-material/Forum";
import HelpIcon from "@mui/icons-material/Help";
import "./App.css";
import MoviePage from "./MoviePage";
import TVPage from "./TVPage";
import MusicPage from "./MusicPage";
import AnimePage from "./AnimePage";
import GamePage from "./GamePage";
import SportPage from "./SportPage";
import InfoPage from "./InfoPage";
import UserProfile from "./UserProfile";
import PublishPage from "./PublishPage";
import TorrentDetailPage from './TorrentDetailPage';
import ForumPage from "./ForumPage";
import PostDetailPage from "./PostDetailPage";
import LoginPage from './LoginPage';
import RegisterPage from './RegisterPage';
import RequireAuth from './RequireAuth';
import AdminPage from './AdminPage';
import AppealPage from './AppealPage';
import MigrationPage from './MigrationPage';
import BegSeedPage from "./BegSeedPage";
import BegInfo from "./BegInfo";
import SeedPromotionPage from "./SeedPromotionPage";
import HomePage from "./HomePage";


const navItems = [
  { label: "电影", icon: <MovieIcon />, path: "/movie" },
  { label: "剧集", icon: <EmailIcon />, path: "/tv" },
  { label: "音乐", icon: <MusicNoteIcon />, path: "/music" },
  { label: "动漫", icon: <EmojiPeopleIcon />, path: "/anime" },
  { label: "游戏", icon: <SportsEsportsIcon />, path: "/game" },
  { label: "体育", icon: <SportsMartialArtsIcon />, path: "/sport" },
  { label: "资料", icon: <PersonIcon />, path: "/info" },
  { label: "论坛", icon: <ForumIcon />, path: "/forum" },
  { label: "发布", icon: <AccountCircleIcon />, path: "/publish" },
  { label: "求种", icon: <HelpIcon />, path: "/begseed" },
];

function Home() {
  const navigate = useNavigate();
  return (
    <div className="container">
      {/* 顶部空白与电影界面一致 */}
      <div style={{ height: 80 }} />
      {/* 用户栏 */}
      <div className="user-bar" style={{ position: 'fixed', top: 18, right: 42, zIndex: 100, display: 'flex', alignItems: 'center', background: '#e0f3ff', borderRadius: 12, padding: '6px 18px', boxShadow: '0 2px 8px #b2d8ea', minWidth: 320, minHeight: 48, width: 420 }}>
        <div style={{ cursor: 'pointer', marginRight: 16 }} onClick={() => navigate('/user')}>
          <AccountCircleIcon style={{ fontSize: 38, color: '#1a237e', background: '#e0f3ff', borderRadius: '50%' }} />
        </div>
        <div style={{ color: '#222', fontWeight: 500, marginRight: 24 }}>用户栏</div>
        <div style={{ display: 'flex', gap: 28, flex: 1, justifyContent: 'flex-end', alignItems: 'center' }}>
          <span style={{ color: '#1976d2', fontWeight: 500 }}>魔力值: <b>12345</b></span>
          <span style={{ color: '#1976d2', fontWeight: 500 }}>分享率: <b>2.56</b></span>
          <span style={{ color: '#1976d2', fontWeight: 500 }}>上传量: <b>100GB</b></span>
          <span style={{ color: '#1976d2', fontWeight: 500 }}>下载量: <b>50GB</b></span>
        </div>
      </div>
      <div style={{ height: 32 }} />
      <nav className="nav-bar card">
        {navItems.map((item) => (
          <div
            key={item.label}
            className={"nav-item"}
            onClick={() => navigate(item.path)}
          >
            {item.icon}
            <span>{item.label}</span>
          </div>
        ))}
      </nav>
      <div className="search-section card">
        <input className="search-input" placeholder="输入搜索关键词" />
        <button className="search-btn">
          <span role="img" aria-label="search">🔍</span>
        </button>
      </div>
      <div className="table-section card">
        <table className="movie-table">
          <thead>
            <tr>
              <th>类型</th>
              <th>标题</th>
              <th>发布者</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>电影</td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>剧集</td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>音乐</td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>动漫</td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>游戏</td>
              <td></td>
              <td></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div style={{ height: 32 }} />
      <Pagination />
    </div>
  );
}

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

function Page({ label }) {
  return (
    <div style={{ padding: 40, fontSize: 32 }}>
      {label} 页面（可自定义内容）
      <br />
      <Link to="/">返回首页</Link>
    </div>
  );
}

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<Navigate to="/login" replace />} />
        {/* Protected routes */}
        <Route element={<RequireAuth />}>
          <Route path="/home" element={<HomePage />} />
          <Route path="/movie" element={<MoviePage />} />
          <Route path="/tv" element={<TVPage />} />
          <Route path="/music" element={<MusicPage />} />
          <Route path="/anime" element={<AnimePage />} />
          <Route path="/game" element={<GamePage />} />
          <Route path="/sport" element={<SportPage />} />
          <Route path="/forum" element={<ForumPage />} />
          <Route path="/forum/:postId" element={<PostDetailPage />} />
          <Route path="/info" element={<InfoPage />} />
          <Route path="/user" element={<UserProfile />} />
          <Route path="/publish" element={<PublishPage />} />
          <Route path="/torrent/:torrentId" element={<TorrentDetailPage />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="/appeal-review" element={<AppealPage />} />
          <Route path="/migration-review" element={<MigrationPage />} />
          <Route path="/seed-promotion" element={<SeedPromotionPage />} />
          <Route path="/begseed" element={<BegSeedPage />} />
          <Route path="/begseed/:begid" element={<BegInfo />} />
        </Route>
      </Routes>
    </Router>
  );
}