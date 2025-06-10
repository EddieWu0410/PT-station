import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
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


export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<Navigate to="/login" replace />} />

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