import React from 'react';
import { useParams } from 'react-router-dom';
import './App.css';
import './SharedStyles.css';
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
    const downloadUrl = `${API_BASE_URL}/api/get-torrent?userId=${encodeURIComponent(userId)}&torrentId=${encodeURIComponent(torrentId)}`;
    fetch(downloadUrl)
      .then(response => {
        if (!response.ok) {
          throw new Error('下载失败');
        }
        return response.blob();
      })
      .then(blob => {
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

  if (loading) return (
    <div className="emerald-home-container">
      <div className="emerald-content">
        <div className="emerald-loading-text">
          加载中...
        </div>
      </div>
    </div>
  );
  if (error) return (
    <div className="emerald-home-container">
      <div className="emerald-content">
        <div className="emerald-error-text">
          加载失败: {error}
        </div>
      </div>
    </div>
  );
  if (!detail) return (
    <div className="emerald-home-container">
      <div className="emerald-content">
        <div className="emerald-empty-text">
          未找到详情
        </div>
      </div>
    </div>
  );

  return (
    <div className="emerald-home-container">
      <div className="emerald-content">
        <div
          className="emerald-content-section"
          style={{
            maxWidth: 420,
            margin: "80px auto 0 auto",
            background: "rgba(255,255,255,0.96)",
            borderRadius: 18,
            boxShadow: "0 4px 32px 0 rgba(60,120,60,0.10)",
            padding: "36px 32px 32px 32px",
            border: "1px solid #e0e8e0"
          }}
        >
          <div
            style={{
              fontFamily: "'Lora', serif",
              fontWeight: 700,
              fontSize: 26,
              color: "#2d5016",
              letterSpacing: 1,
              marginBottom: 24,
              textAlign: "center"
            }}
          >
            种子详情
          </div>
          <div style={{
            marginBottom: 24,
            background: "#f7faf7",
            borderRadius: 12,
            padding: "24px 20px",
            boxShadow: "0 1px 4px 0 rgba(60,120,60,0.04)",
            border: "1px solid #e5eee5"
          }}>
            <div style={{
              fontSize: 18,
              fontWeight: 600,
              marginBottom: 10,
              color: "#234d20",
              fontFamily: "'Lora', serif",
              wordBreak: "break-all"
            }}>
              标题：{detail.title || `种子${torrentId}`}
            </div>
            <div style={{
              fontSize: 15,
              color: "#4a7c59",
              fontFamily: "'Lora', serif",
              lineHeight: 1.7,
              wordBreak: "break-all"
            }}>
              简介：{detail.description || `这是种子${torrentId}的详细信息。`}
            </div>
          </div>
          <div style={{
            display: "flex",
            gap: 20,
            marginTop: 10,
            justifyContent: "center"
          }}>
            <button
              className="emerald-btn"
              style={{
                minWidth: 90,
                padding: "10px 0",
                fontSize: "15px",
                borderRadius: "16px",
                background: "linear-gradient(90deg, #4caf50 0%, #81c784 100%)",
                color: "#fff",
                fontWeight: 600,
                border: "none",
                boxShadow: "0 2px 8px rgba(76,175,80,0.10)",
                fontFamily: "'Lora', serif",
                cursor: "pointer",
                transition: "all 0.2s"
              }}
              onClick={handleClick}
            >
              下载
            </button>
            <button
              className="emerald-btn"
              style={{
                minWidth: 90,
                padding: "10px 0",
                fontSize: "15px",
                borderRadius: "16px",
                background: isFavorite
                  ? "linear-gradient(90deg, #ffd54f 0%, #ffb300 100%)"
                  : "linear-gradient(90deg, #e0e0e0 0%, #bdbdbd 100%)",
                color: isFavorite ? "#fff" : "#444",
                fontWeight: 600,
                border: "none",
                boxShadow: isFavorite ? "0 2px 8px #ffe082" : "0 2px 8px #e0e7ff",
                fontFamily: "'Lora', serif",
                cursor: "pointer",
                transition: "all 0.2s"
              }}
              onClick={handleFavorite}
            >
              {isFavorite ? "已收藏" : "收藏"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}