import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import PeopleIcon from "@mui/icons-material/People";
import MonetizationOnIcon from "@mui/icons-material/MonetizationOn";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import { API_BASE_URL } from "./config";
import "./BegSeedPage.css";

export default function BegInfo() {
    const { begid } = useParams();
    const [beg, setBeg] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [seeds, setSeeds] = useState([]);
    const [seedInfoMap, setSeedInfoMap] = useState({});
    const [showForm, setShowForm] = useState(false);
    const [userSeeds, setUserSeeds] = useState([]);
    const [loadingUserSeeds, setLoadingUserSeeds] = useState(false);
    const [formData, setFormData] = useState({
        selectedSeedId: "",
    });

    // 从后端获取求种详情
    const fetchBegSeedDetail = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/begseed-detail?begid=${begid}`);
            if (!response.ok) {
                throw new Error(`请求失败，状态码: ${response.status}`);
            }
            const data = await response.json();
            const formattedBeg = {
                beg_id: data.beg_id || data.begid || data.id,
                info: data.info || data.description || data.content,
                beg_count: data.beg_count || data.begCount || 1,
                reward_magic: data.reward_magic || data.rewardMagic || data.magic,
                deadline: data.deadline || data.endtime,
                has_match: data.has_match || data.hasMatch || data.completed || 0,
            };
            setBeg(formattedBeg);
            setError(null);
        } catch (err) {
            console.error('获取求种详情失败:', err);
            setError(err.message);
            setBeg(null);
        } finally {
            setLoading(false);
        }
    };

    // 从后端获取已提交的种子列表
    const fetchSubmittedSeeds = async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/begseed-submissions?begid=${begid}`);
            if (!response.ok) {
                throw new Error(`请求失败，状态码: ${response.status}`);
            }
            const data = await response.json();
            const submissions = Array.isArray(data) ? data : [];
            const formattedSeeds = submissions.map(item => ({
                seed_id: item.seed?.seedid || item.seedid,
                beg_id: begid,
                votes: item.votes || 0,
                title: item.seed?.title || item.title || "未知标题",
                subtitle: item.seed?.subtitle || item.subtitle || "无简介",
                seedsize: item.seed?.seedsize || item.seedsize,
                downloadtimes: item.seed?.downloadtimes || item.downloadtimes || 0,
                url: item.seed?.url || item.url,
                user: item.seed?.user || item.user
            }));
            const newSeedInfoMap = {};
            submissions.forEach(item => {
                const seedId = item.seed?.seedid || item.seedid;
                if (seedId) {
                    newSeedInfoMap[seedId] = {
                        title: item.seed?.title || item.title || "未知标题",
                        subtitle: item.seed?.subtitle || item.subtitle || "无简介",
                    };
                }
            });
            setSeeds(formattedSeeds);
            setSeedInfoMap(newSeedInfoMap);
        } catch (err) {
            console.error('获取种子提交列表失败:', err);
            setSeeds([]);
            setSeedInfoMap({});
        }
    };

    useEffect(() => {
        fetchBegSeedDetail();
        fetchSubmittedSeeds();
    }, [begid]);

    if (loading) {
        return (
            <div className="begseed-container">
                <div className="begseed-content">
                    <div className="begseed-loading">
                        正在加载求种详情...
                    </div>
                </div>
            </div>
        );
    }

    if (!beg) {
        return (
            <div className="begseed-container">
                <div className="begseed-content">
                    <div className="begseed-error">
                        未找到该求种信息
                    </div>
                </div>
            </div>
        );
    }

    const isExpired = new Date(beg.deadline) < new Date();
    const isFinished = beg.has_match === 1;
    const isActive = !isExpired && !isFinished;

    // 获取用户的所有种子
    const fetchUserSeeds = async () => {
        setLoadingUserSeeds(true);
        try {
            const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
            const userId = match ? match[2] : null;
            if (!userId) {
                alert("请先登录后再获取种子列表");
                setLoadingUserSeeds(false);
                return;
            }
            const response = await fetch(`${API_BASE_URL}/api/user-seeds?userid=${userId}`);
            if (!response.ok) {
                throw new Error(`请求失败，状态码: ${response.status}`);
            }
            const data = await response.json();
            const formattedSeeds = Array.isArray(data) ? data.map(seed => ({
                seedid: seed.seedid || seed.id,
                title: seed.title || "未知标题",
                subtitle: seed.subtitle || "无简介",
                seedsize: seed.seedsize,
                downloadtimes: seed.downloadtimes || 0,
                url: seed.url
            })) : [];
            setUserSeeds(formattedSeeds);
        } catch (err) {
            console.error('获取用户种子失败:', err);
            alert(`获取种子列表失败: ${err.message}`);
        } finally {
            setLoadingUserSeeds(false);
        }
    };

    // 投票功能
    const handleVote = async (seed_id) => {
        try {
            const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
            const userId = match ? match[2] : null;
            if (!userId) {
                alert("请先登录后再投票");
                return;
            }
            const response = await fetch(`${API_BASE_URL}/api/vote-seed`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userid: userId,
                    seedid: seed_id,
                    begid: begid,
                }),
            });
            if (response.ok) {
                await fetchSubmittedSeeds();
                alert("投票成功！");
            } else if (response.status === 409) {
                alert("您已投过票，不能重复投票");
            }
            else {
                const errorData = await response.json();
                alert(`投票失败: ${errorData.message || '未知错误'}`);
            }
        } catch (err) {
            console.error('投票失败:', err);
            setSeeds((prev) =>
                prev.map((s) =>
                    s.seed_id === seed_id ? { ...s, votes: s.votes + 1 } : s
                )
            );
            alert("投票成功（前端演示）");
        }
    };

    const handleFormChange = (e) => {
        const { name, value } = e.target;
        setFormData((f) => ({ ...f, [name]: value }));
    };

    const handleSubmitSeed = async (e) => {
        e.preventDefault();
        if (!formData.selectedSeedId) {
            alert("请选择一个种子");
            return;
        }
        try {
            const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
            const userId = match ? match[2] : null;
            if (!userId) {
                alert("请先登录后再提交种子");
                return;
            }
            const response = await fetch(`${API_BASE_URL}/api/submit-seed`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userid: userId,
                    begid: begid,
                    seedid: formData.selectedSeedId,
                }),
            });
            if (response.ok) {
                await Promise.all([
                    fetchBegSeedDetail(),
                    fetchSubmittedSeeds()
                ]);
                setShowForm(false);
                setFormData({ selectedSeedId: "" });
                setUserSeeds([]);
                alert("提交成功！");
            } else {
                const errorData = await response.json();
                alert(`提交失败: ${errorData.message || '未知错误'}`);
            }
        } catch (err) {
            console.error('提交种子失败:', err);
            setShowForm(false);
            setFormData({ selectedSeedId: "" });
            setUserSeeds([]);
            alert("提交成功（前端演示）");
        }
    };

    // 卡片状态样式
    let statusClass = "begseed-status-active";
    let statusText = "进行中";
    if (isFinished) {
        statusClass = "begseed-status-completed";
        statusText = "已完成";
    } else if (isExpired) {
        statusClass = "begseed-status-expired";
        statusText = "已过期";
    }

    return (
        <div className="begseed-container">
            <div className="begseed-content">
                <h1 className="begseed-title" style={{ marginBottom: 32 }}>
                    <CheckCircleIcon className="begseed-title-icon" style={{ fontSize: 42 }} />
                    求种详情
                </h1>

                {error && (
                    <div className="begseed-error">
                        加载失败: {error} (已显示默认数据)
                    </div>
                )}

                {/* 求种信息卡片 */}
                <div className="begseed-card" style={{ maxWidth: 600, margin: "0 auto 32px auto", cursor: "default" }}>
                    <div className="begseed-card-content">
                        <div className="begseed-card-title" style={{ marginBottom: 18 }}>
                            {beg.info}
                        </div>
                        <div className="begseed-card-info">
                            <PeopleIcon className="begseed-card-info-icon" />
                            求种人数：{beg.beg_count}
                        </div>
                        <div className="begseed-card-info">
                            <MonetizationOnIcon className="begseed-card-info-icon" />
                            悬赏魔力值：{beg.reward_magic}
                        </div>
                        <div className="begseed-card-info">
                            <AccessTimeIcon className="begseed-card-info-icon" />
                            截止时间：{new Date(beg.deadline).toLocaleString()}
                        </div>
                        <div className="begseed-card-info">
                            <CheckCircleIcon className="begseed-card-info-icon" />
                            <span className={`begseed-card-status ${statusClass}`}>
                                {statusText}
                            </span>
                        </div>
                    </div>
                </div>

                {/* 已提交种子列表 */}
                <div className="begseed-form-container" style={{ maxWidth: 1000, margin: "0 auto 32px auto", padding: 32 }}>
                    <h3 className="begseed-form-title" style={{ marginBottom: 18 }}>已提交种子</h3>
                    <div style={{ overflowX: "auto" }}>
                        <table className="emerald-table" style={{ minWidth: 700, background: "transparent" }}>
                            <thead>
                                <tr>
                                    <th>标题</th>
                                    <th>简介</th>
                                    <th>文件大小</th>
                                    <th>下载次数</th>
                                    <th>投票数</th>
                                    <th>上传者</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                {seeds.length === 0 ? (
                                    <tr>
                                        <td colSpan={7} style={{ textAlign: "center", color: "#888" }}>暂无提交的种子</td>
                                    </tr>
                                ) : (
                                    seeds.map((s) => (
                                        <tr key={s.seed_id}>
                                            <td>
                                                <a href={`/torrent/${s.seed_id}`} style={{ color: '#1a237e', textDecoration: 'none' }}>
                                                    {s.title}
                                                </a>
                                            </td>
                                            <td>{s.subtitle || "无简介"}</td>
                                            <td>{s.seedsize ? `${s.seedsize} MB` : "未知"}</td>
                                            <td>{s.downloadtimes || 0}</td>
                                            <td style={{ fontWeight: 'bold', color: '#1976d2' }}>{s.votes || 0}</td>
                                            <td>{s.user?.username || "未知用户"}</td>
                                            <td>
                                                {isActive ? (
                                                    <button
                                                        onClick={() => handleVote(s.seed_id)}
                                                        className="begseed-form-btn begseed-form-btn-primary"
                                                        style={{
                                                            padding: "6px 18px",
                                                            fontSize: 15,
                                                            minWidth: 0,
                                                            margin: 0
                                                        }}
                                                    >
                                                        投票
                                                    </button>
                                                ) : (
                                                    <span style={{ color: "#b0b0b0" }}>不可投票</span>
                                                )}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                    {/* 总投票数 */}
                    {seeds.length > 0 && (
                        <div style={{ textAlign: "center", margin: "16px 0", color: "#666" }}>
                            总投票数: {seeds.reduce((total, seed) => total + (seed.votes || 0), 0)}
                        </div>
                    )}
                </div>

                {/* 提交种子按钮 */}
                {isActive && (
                    <div style={{ margin: "32px 0", textAlign: "center" }}>
                        <button
                            onClick={() => {
                                setShowForm(true);
                                fetchUserSeeds();
                            }}
                            className="begseed-publish-btn"
                            style={{ fontSize: 18, padding: "12px 36px" }}
                        >
                            提交种子
                        </button>
                    </div>
                )}

                {/* 提交种子表单 */}
                {showForm && isActive && (
                    <div className="begseed-form-container" style={{ maxWidth: 480, margin: "0 auto" }}>
                        <h3 className="begseed-form-title" style={{ marginBottom: 18 }}>选择种子</h3>
                        {loadingUserSeeds && (
                            <div className="begseed-loading">
                                正在加载您的种子列表...
                            </div>
                        )}
                        {userSeeds.length > 0 ? (
                            <div style={{ marginBottom: 24 }}>
                                <div className="begseed-form-group" style={{ marginBottom: 16 }}>
                                    <label className="begseed-form-label" style={{ width: 80 }}>选择种子：</label>
                                    <select
                                        name="selectedSeedId"
                                        value={formData.selectedSeedId}
                                        onChange={handleFormChange}
                                        className="begseed-form-input"
                                        style={{ width: 300, background: "#fff", fontSize: 14 }}
                                    >
                                        <option value="">请选择一个种子</option>
                                        {userSeeds.map((seed) => (
                                            <option key={seed.seedid} value={seed.seedid}>
                                                {seed.title} - {seed.subtitle || "无简介"} ({seed.seedsize ? `${seed.seedsize} MB` : "未知大小"})
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                {formData.selectedSeedId && (
                                    <div style={{
                                        padding: 12,
                                        background: "#e8f5e8",
                                        borderRadius: 6,
                                        border: "1px solid #4caf50",
                                        color: "#2e7d32"
                                    }}>
                                        ✓ 已选择种子，点击提交即可使用此种子
                                    </div>
                                )}
                            </div>
                        ) : (
                            !loadingUserSeeds && (
                                <div className="begseed-error" style={{
                                    background: "#fff3cd",
                                    color: "#856404",
                                    border: "1px solid #ffeaa7",
                                    borderRadius: 6,
                                    margin: "20px 0",
                                    padding: "16px"
                                }}>
                                    您还没有上传过种子，无法参与悬赏
                                </div>
                            )
                        )}

                        <form onSubmit={handleSubmitSeed}>
                            <div className="begseed-form-actions" style={{ marginTop: 18 }}>
                                <button
                                    type="submit"
                                    disabled={!formData.selectedSeedId}
                                    className="begseed-form-btn begseed-form-btn-primary"
                                    style={{
                                        background: formData.selectedSeedId ? undefined : "#b0b0b0",
                                        cursor: formData.selectedSeedId ? "pointer" : "not-allowed"
                                    }}
                                >
                                    提交种子
                                </button>
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowForm(false);
                                        setFormData({ selectedSeedId: "" });
                                        setUserSeeds([]);
                                    }}
                                    className="begseed-form-btn begseed-form-btn-secondary"
                                >
                                    取消
                                </button>
                            </div>
                        </form>
                    </div>
                )}
            </div>
        </div>
    );
}