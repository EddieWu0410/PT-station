import React from 'react';
import { useParams } from 'react-router-dom';
import './App.css';
import { API_BASE_URL } from "./config";

export default function TorrentDetailPage() {
  const { torrentId } = useParams();
  const [detail, setDetail] = React.useState(null);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState(null);
  const [isFavorite, setIsFavorite] = React.useState(false);
  const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
  const userId = match ? match[2] : null;

  // 下载种子
  const handleClick = () => {
    // 构造下载 URL，包含 userId 和 torrentId 参数
    console.log(torrentId)
    const downloadUrl = `${API_BASE_URL}/api/get-torrent?userId=${encodeURIComponent(userId)}&torrentId=${encodeURIComponent(torrentId)}`;

    // 发起 GET 请求下载文件
    fetch(downloadUrl)
      .then(response => {
        if (!response.ok) {
          throw new Error('下载失败');
        }
        return response.blob();
      })
      .then(blob => {
        // 创建下载链接并触发下载
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `torrent-${torrentId}.torrent`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      })
      .catch(error => {
        console.error('下载错误:', error);
        alert('下载失败: ' + error.message);
      });
  };

  // 收藏种子
  const handleFavorite = () => {
    fetch(`${API_BASE_URL}/api/add-favorite`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        userid: userId,
        seedid: torrentId
      })
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('收藏失败');
        }
        return response.json();
      })
      .then(data => {
        setIsFavorite(true);
        alert("收藏成功！");
      })
      .catch(error => {
        console.error('收藏错误:', error);
        alert('收藏失败: ' + error.message);
      });
  };

  React.useEffect(() => {
    setLoading(true);
    setError(null);
    fetch(`${API_BASE_URL}/api/torrent-detail?id=${encodeURIComponent(torrentId)}`)
      .then(res => {
        if (!res.ok) throw new Error('网络错误');
        return res.json();
      })
      .then(data => {
        setDetail(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, [torrentId]);

  if (loading) return <div className="container"><h1>加载中...</h1></div>;
  if (error) return <div className="container"><h1>加载失败: {error}</h1></div>;
  if (!detail) return <div className="container"><h1>未找到详情</h1></div>;

  return (
    <div className="container" style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "100vh" }}>
      <div
        style={{
          background: "#fff",
          borderRadius: 16,
          boxShadow: "0 4px 24px #e0e7ff",
          padding: "36px 48px",
          maxWidth: 540,
          width: "100%",
          marginTop: 48,
        }}
      >
        <h1 style={{ color: "#1976d2", fontWeight: 700, marginBottom: 24, fontSize: 28, letterSpacing: 1 }}>种子详情页</h1>
        <div style={{ marginBottom: 18 }}>
          <div style={{ fontSize: 20, fontWeight: 600, marginBottom: 8, color: "#222" }}>
            标题：{detail.title || `种子${torrentId}`}
          </div>
          <div style={{ fontSize: 16, color: "#555", marginBottom: 8 }}>
            简介：{detail.description || `这是种子${torrentId}的详细信息。`}
          </div>
        </div>
        <div style={{ display: "flex", gap: 24, marginTop: 32, justifyContent: "center" }}>
          <button
            style={{
              padding: "10px 32px",
              fontSize: "16px",
              cursor: "pointer",
              background: "linear-gradient(90deg, #42a5f5 0%, #1976d2 100%)",
              color: "#fff",
              border: "none",
              borderRadius: "8px",
              fontWeight: 600,
              boxShadow: "0 2px 8px #b2d8ea",
              transition: "background 0.2s",
            }}
            onClick={handleClick}
          >
            下载
          </button>
          <button
            style={{
              padding: "10px 32px",
              fontSize: "16px",
              cursor: "pointer",
              background: isFavorite
                ? "linear-gradient(90deg, #ffb74d 0%, #ff9800 100%)"
                : "linear-gradient(90deg, #f0f0f0 0%, #bdbdbd 100%)",
              color: isFavorite ? "#fff" : "#333",
              border: "none",
              borderRadius: "8px",
              fontWeight: 600,
              boxShadow: isFavorite ? "0 2px 8px #ffe0b2" : "0 2px 8px #e0e7ff",
              transition: "background 0.2s",
            }}
            onClick={handleFavorite}
          >
            {isFavorite ? "已收藏" : "收藏"}
          </button>
        </div>
      </div>
    </div>
  );
}
