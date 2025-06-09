import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "./config";

// 简单PDF预览组件
function FileViewer({ url }) {
    if (!url) return <div>无附件</div>;
    if (url.endsWith(".pdf")) {
        return (
            <iframe
                src={url}
                title="PDF预览"
                width="100%"
                height="400px"
                style={{ border: "1px solid #ccc", borderRadius: 8 }}
            />
        );
    }
    // 这里只做PDF示例，实际可扩展为DOC等
    return <a href={url} target="_blank" rel="noopener noreferrer">下载附件</a>;
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

    if (loading) return <div>加载中...</div>;
    if (error) return <div>加载失败：{error}</div>;

    return (
        <div style={{ display: "flex", minHeight: "100vh", background: "#f7faff" }}>
            {/* 侧栏 */}
            <div style={{ width: 180, background: "#fff", borderRight: "1px solid #e0e7ff", padding: 0 }}>
                <h3 style={{ textAlign: "center", padding: "18px 0 0 0", color: "#1976d2" }}>迁移列表</h3>
                <div style={{ display: "flex", flexDirection: "column", gap: 12, marginTop: 18 }}>
                    {migrations.map(m => (
                        <div
                            key={m.migration_id}
                            onClick={() => setSelectedId(m.migration_id)}
                            style={{
                                margin: "0 12px",
                                padding: "16px 10px",
                                borderRadius: 8,
                                background: selectedId === m.migration_id ? "#e3f2fd" : "#fff",
                                border: `2px solid ${m.approved === 1 ? "#43a047" : "#e53935"}`,
                                color: m.approved === 1 ? "#43a047" : "#e53935",
                                fontWeight: 600,
                                cursor: "pointer",
                                boxShadow: selectedId === m.migration_id ? "0 2px 8px #b2d8ea" : "none",
                                transition: "all 0.2s"
                            }}
                        >
                            {m.migration_id}
                            <span style={{
                                float: "right",
                                fontSize: 12,
                                color: m.approved === 1 ? "#43a047" : "#e53935"
                            }}>
                                {m.approved === 1 ? "已审核" : "未审核"}
                            </span>
                        </div>
                    ))}
                </div>
            </div>
            {/* 迁移详情 */}
            <div style={{ flex: 1, padding: "40px 48px" }}>
                <h2 style={{ marginBottom: 24, color: "#1976d2" }}>迁移详情</h2>
                <div style={{ background: "#fff", borderRadius: 12, padding: 32, boxShadow: "0 2px 8px #e0e7ff", marginBottom: 32 }}>
                    <div style={{ marginBottom: 18 }}>
                        <b>迁移ID：</b>{selectedMigration.migration_id}
                    </div>
                    <div style={{ marginBottom: 18 }}>
                        <b>用户ID：</b>{selectedMigration.user_id}
                    </div>
                    <div style={{ marginBottom: 18 }}>
                        <b>申请文件：</b>
                        <FileViewer url={selectedMigration.application_url} />
                    </div>
                    <div style={{ marginBottom: 18 }}>
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
                                    padding: "4px 8px",
                                    borderRadius: 4,
                                    border: "1px solid #bdbdbd"
                                }}
                                disabled={selectedMigration.approved === 1}
                            />
                        )}
                    </div>
                </div>
                {/* 审核按钮 */}
                <div style={{ display: "flex", gap: 32, justifyContent: "center" }}>
                    <button
                        style={{
                            background: selectedMigration.approved === 1 ? "#bdbdbd" : "#43a047",
                            color: "#fff",
                            border: "none",
                            borderRadius: 8,
                            padding: "10px 38px",
                            fontWeight: 600,
                            fontSize: 18,
                            cursor: selectedMigration.approved === 1 ? "not-allowed" : "pointer"
                        }}
                        disabled={selectedMigration.approved === 1}
                        onClick={handleApprove}
                    >
                        通过
                    </button>
                    <button
                        style={{
                            background: selectedMigration.approved === 1 ? "#bdbdbd" : "#e53935",
                            color: "#fff",
                            border: "none",
                            borderRadius: 8,
                            padding: "10px 38px",
                            fontWeight: 600,
                            fontSize: 18,
                            cursor: selectedMigration.approved === 1 ? "not-allowed" : "pointer"
                        }}
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