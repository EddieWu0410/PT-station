import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "./config";
import "./AppealPage.css";

// PDF预览组件
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

    return <a href={url} target="_blank" rel="noopener noreferrer" className="file-download-link">下载附件</a>;
}

export default function MigrationPage() {
    const [migrations, setMigrations] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [grantedUploadInput, setGrantedUploadInput] = useState(0);

    // Helper to load migrations list
    const fetchMigrations = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${API_BASE_URL}/api/migrations`);
            if (!res.ok) throw new Error(`请求失败，状态码 ${res.status}`);
            const data = await res.json();
            const formatted = data.map(item => ({
                migration_id: item.profileurl,
                user_id: item.user.userid,
                application_url: item.applicationurl,
                pending_magic: Number(item.magictogive),
                granted_magic: Number(item.magicgived),
                pending_uploaded: Number(item.uploadtogive),
                granted_uploaded: Number(item.uploadgived),
                approved: item.exampass ? 1 : 0
            }));
            setMigrations(formatted);
            if (formatted.length > 0) setSelectedId(formatted[0].migration_id);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMigrations();
    }, []);

    // 获取当前选中的迁移申请
    const selectedMigration = migrations.find(m => m.migration_id === selectedId) || {};

    // 每次切换迁移申请时，重置输入框为0或当前已迁移量
    useEffect(() => {
        // 审核通过后，输入框应为已迁移量，否则为0
        if (selectedMigration.approved === 1) {
            setGrantedUploadInput(selectedMigration.granted_uploaded || 0);
        } else {
            setGrantedUploadInput(0);
        }
    }, [selectedId, migrations, selectedMigration.approved, selectedMigration.granted_uploaded]);

    // 审核通过
    const handleApprove = async () => {
        const max = selectedMigration.pending_uploaded || 0;
        let value = Number(grantedUploadInput);
        if (isNaN(value) || value < 0) value = 0;
        if (value > max) value = max;

        try {
            const res = await fetch(`${API_BASE_URL}/api/migrations-approve`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    migration_id: selectedMigration.migration_id,
                    granted_uploaded: value
                })
            });
            if (!res.ok) throw new Error(`请求失败，状态码 ${res.status}`);
            alert("已通过迁移");
            await fetchMigrations();
        } catch (err) {
            alert(`操作失败：${err.message}`);
        }
    };

    // 审核不通过
    const handleReject = async () => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/migrations-reject`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    migration_id: selectedMigration.migration_id,
                    granted_uploaded: 0
                })
            });
            if (!res.ok) throw new Error(`请求失败，状态码 ${res.status}`);
            alert("已拒绝迁移");
            await fetchMigrations();
        } catch (err) {
            alert(`操作失败：${err.message}`);
        }
    };

    if (loading) return <div className="appeal-loading">加载中...</div>;
    if (error) return <div className="appeal-error">加载失败：{error}</div>;

    return (
        <div className="appeal-page-container">
            {/* 侧栏 */}
            <div className="appeal-sidebar">
                <h3 className="appeal-sidebar-title">迁移列表</h3>
                <div className="appeal-list-container">
                    {migrations.map(m => (
                        <div
                            key={m.migration_id}
                            onClick={() => setSelectedId(m.migration_id)}
                            className={`appeal-list-item ${selectedId === m.migration_id ? 'selected' : ''
                                } ${m.approved === 1 ? 'approved' : 'pending'
                                }`}
                        >
                            {m.migration_id}
                            <span className={`appeal-status-label ${m.approved === 1 ? 'approved' : 'pending'}`}>
                                {m.approved === 1 ? "已审核" : "未审核"}
                            </span>
                        </div>
                    ))}
                </div>
            </div>
            {/* 迁移详情 */}
            <div className="appeal-main-content">
                <h2 className="appeal-detail-title">迁移详情</h2>
                <div className="appeal-detail-card">
                    <div className="appeal-detail-item">
                        <b>迁移ID：</b>{selectedMigration.migration_id}
                    </div>
                    <div className="appeal-detail-item">
                        <b>用户ID：</b>{selectedMigration.user_id}
                    </div>
                    <div className="appeal-detail-item">
                        <b>申请文件：</b>
                        <FileViewer url={selectedMigration.application_url} />
                    </div>
                    <div className="appeal-detail-item">
                        <b>待迁移上传量：</b>{selectedMigration.pending_uploaded}，
                        <b>已迁移上传量：</b>
                        {selectedMigration.approved === 1 ? (
                            <span>{selectedMigration.granted_uploaded}</span>
                        ) : (
                            <input
                                type="number"
                                min={0}
                                max={selectedMigration.pending_uploaded}
                                value={grantedUploadInput}
                                onChange={e => {
                                    let val = Number(e.target.value);
                                    if (isNaN(val) || val < 0) val = 0;
                                    if (val > selectedMigration.pending_uploaded) val = selectedMigration.pending_uploaded;
                                    setGrantedUploadInput(val);
                                }}
                                style={{
                                    width: 100,
                                    marginLeft: 8,
                                    padding: "8px 12px",
                                    borderRadius: 8,
                                    border: "2px solid rgba(144, 238, 144, 0.3)",
                                    background: "rgba(240,255,240,0.5)",
                                    fontSize: 16,
                                    fontFamily: "Lora, serif",
                                    outline: "none",
                                    transition: "all 0.3s"
                                }}
                                disabled={selectedMigration.approved === 1}
                                className="appeal-form-input"
                            />
                        )}
                    </div>
                </div>
                {/* 审核按钮 */}
                <div className="appeal-buttons-container">
                    <button
                        className="appeal-btn appeal-btn-approve"
                        disabled={selectedMigration.approved === 1}
                        onClick={handleApprove}
                    >
                        通过
                    </button>
                    <button
                        className="appeal-btn appeal-btn-reject"
                        disabled={selectedMigration.approved === 1}
                        onClick={handleReject}
                    >
                        不通过
                    </button>
                </div>
            </div>
        </div>
    );
}