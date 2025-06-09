import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "./config";
import "./AppealPage.css";

// State for appeals fetched from backend
export default function AppealPage() {
    const [appeals, setAppeals] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Helper to load appeals
    const fetchAppeals = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${API_BASE_URL}/api/appeals`);
            if (!res.ok) throw new Error(`请求失败，状态码 ${res.status}`);
            const data = await res.json();
            // console.log("Fetched appeals:", data);
            setAppeals(data);
            if (data.length > 0) setSelectedId(data[0].appealid);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAppeals();
    }, []);

    if (loading) return <div className="appeal-loading">加载中...</div>;
    if (error) return <div className="appeal-error">加载失败：{error}</div>;
    const selectedAppeal = appeals.find(a => a.appealid === selectedId) || {};

    // Approve selected appeal and refresh
    const handleApprove = async () => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/appeals-approve`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ appealid: selectedAppeal.appealid })
            });
            if (!res.ok) throw new Error(`请求失败，状态码 ${res.status}`);
            alert("已通过申诉");
            await fetchAppeals();
        } catch (err) {
            alert(`操作失败：${err.message}`);
        }
    };
    // Reject selected appeal and refresh
    const handleReject = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/api/appeals-reject`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ appealid: selectedAppeal.appealid })
        });
        if (!res.ok) throw new Error(`请求失败，状态码 ${res.status}`);
        alert("已拒绝申诉");
        await fetchAppeals();
      } catch (err) {
        alert(`操作失败：${err.message}`);
      }
    };

    return (
        <div className="appeal-page-container">
            {/* 侧栏 */}
            <div className="appeal-sidebar">
                <h3 className="appeal-sidebar-title">申诉列表</h3>
                <div className="appeal-list-container">
                    {appeals.map(a => (
                        <div
                            key={a.appealid}
                            onClick={() => setSelectedId(a.appealid)}
                            className={`appeal-list-item ${
                                selectedId === a.appealid ? 'selected' : ''
                            } ${
                                a.status === 1 || a.status === 2 ? 'approved' : 'pending'
                            }`}
                        >
                            {a.user.username}
                            <span className={`appeal-status-label ${
                                a.status === 1 || a.status === 2 ? 'approved' : 'pending'
                            }`}>
                                {a.status === 1 || a.status === 2 ? "已审核" : "未审核"}
                            </span>
                        </div>
                    ))}
                </div>
            </div>
            {/* 申诉详情 */}
            <div className="appeal-main-content">
                <h2 className="appeal-detail-title">申诉详情</h2>
                <div className="appeal-detail-card">
                    <div className="appeal-detail-item">
                        <b>申诉ID：</b>{selectedAppeal.appealid}
                    </div>
                    <div className="appeal-detail-item">
                        <b>用户ID：</b>{selectedAppeal.user.userid}
                    </div>
                    <div className="appeal-detail-item">
                        <b>申诉内容：</b>{selectedAppeal.content}
                    </div>
                    <div className="appeal-detail-item">
                        <b>申诉文件：</b>
                        <FileViewer url={selectedAppeal.fileURL} />
                    </div>
                </div>
                {/* 审核按钮 */}
                <div className="appeal-buttons-container">
                    <button
                        className={`appeal-btn appeal-btn-approve`}
                        disabled={selectedAppeal.status === 1}
                        onClick={handleApprove}
                    >
                        通过
                    </button>
                    <button
                        className={`appeal-btn appeal-btn-reject`}
                        disabled={selectedAppeal.status === 1}
                        onClick={handleReject}
                    >
                        不通过
                    </button>
                </div>
            </div>
        </div>
    );
}

// 简单PDF预览组件
function FileViewer({ url }) {
    if (!url) return <div>无附件</div>;
    if (url.endsWith(".pdf")) {
        return (
            <div className="file-viewer-container">
                <iframe
                    src={url}
                    title="PDF预览"
                    className="file-viewer-iframe"
                />
            </div>
        );
    }
    // 这里只做PDF示例，实际可扩展为DOC等
    return <a href={url} target="_blank" rel="noopener noreferrer" className="file-download-link">下载附件</a>;
}