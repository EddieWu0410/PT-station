import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { API_BASE_URL } from "./config";

// 求种任务示例数据（作为后备数据）
const begSeedList = [
    {
        beg_id: "beg001",
        info: "求《三体》高清资源",
        beg_count: 5,
        reward_magic: 100,
        deadline: "2025-06-10T23:59:59",
        has_match: 0,
    },
    {
        beg_id: "beg002",
        info: "求《灌篮高手》国语配音版",
        beg_count: 3,
        reward_magic: 50,
        deadline: "2024-05-01T23:59:59",
        has_match: 1,
    },
    {
        beg_id: "beg003",
        info: "求《黑暗之魂3》PC版种子",
        beg_count: 2,
        reward_magic: 80,
        deadline: "2024-04-01T23:59:59",
        has_match: 0,
    },
];

// SubmitSeed表示例数据
const submitSeedList = [
    { beg_id: "beg001", seed_id: "seed001", votes: 3 },
    { beg_id: "beg001", seed_id: "seed002", votes: 1 },
    { beg_id: "beg002", seed_id: "seed003", votes: 2 },
];

// 种子信息映射
const seedInfoMap = {
    seed001: { title: "三体 1080P 蓝光", subtitle: "高码率无水印" },
    seed002: { title: "三体 720P", subtitle: "清晰版" },
    seed003: { title: "灌篮高手 国语配音", subtitle: "全剧集" },
};

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
            
            
            // 格式化数据以匹配前端期望的格式
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
            // 如果API调用失败，使用默认数据
            const fallbackBeg = begSeedList.find((b) => b.beg_id === begid);
            setBeg(fallbackBeg || null);
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
            console.log('获取到的种子提交数据:', data);
            
            // 新的数据结构：数组，每个元素包含seed对象和votes字段
            const submissions = Array.isArray(data) ? data : [];
            
            // 格式化种子数据
            const formattedSeeds = submissions.map(item => ({
                seed_id: item.seed?.seedid || item.seedid,
                beg_id: begid,
                votes: item.votes || 0, // 每个种子单独的投票数
                title: item.seed?.title || item.title || "未知标题",
                subtitle: item.seed?.subtitle || item.subtitle || "无简介",
                seedsize: item.seed?.seedsize || item.seedsize,
                downloadtimes: item.seed?.downloadtimes || item.downloadtimes || 0,
                url: item.seed?.url || item.url,
                user: item.seed?.user || item.user
            }));
            
            // 构建种子信息映射
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
            // 如果API调用失败，使用默认数据
            const fallbackSeeds = submitSeedList.filter((s) => s.beg_id === begid);
            setSeeds(fallbackSeeds);
            setSeedInfoMap(seedInfoMap);
        }
    };

    // 组件挂载时获取数据
    useEffect(() => {
        fetchBegSeedDetail();
        fetchSubmittedSeeds();
    }, [begid]);

    // 加载状态
    if (loading) {
        return (
            <div className="container">
                <div style={{ textAlign: "center", margin: "40px 0", color: "#666" }}>
                    正在加载求种详情...
                </div>
            </div>
        );
    }

    // 未找到求种信息
    if (!beg) {
        return (
            <div className="container">
                <div style={{ padding: 40, textAlign: "center", color: "#666" }}>
                    未找到该求种信息
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
            // 获取用户ID
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
            
            // 格式化种子数据
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

    // 投票功能（发送到后端）
    const handleVote = async (seed_id) => {
        try {
            // 获取用户ID
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
                // 投票成功，重新获取数据以更新投票计数
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
            // 如果后端调用失败，更新本地状态作为后备
            setSeeds((prev) =>
                prev.map((s) => 
                    s.seed_id === seed_id ? { ...s, votes: s.votes + 1 } : s
                )
            );
            alert("投票成功（前端演示）");
        }
    };

    // 上传表单处理
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
            // 获取用户ID
            const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
            const userId = match ? match[2] : null;
            
            if (!userId) {
                alert("请先登录后再提交种子");
                return;
            }
            // console.log('提交种子数据:', {
            //     userid: userId,
            //     begid: begid,
            //     seedid: formData.selectedSeedId,
            // });

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
                // 提交成功，重新获取所有数据以刷新页面
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
            // 如果后端调用失败，使用前端演示逻辑
            const newSeedId = "seed" + Math.floor(Math.random() * 10000);
            
            // 从用户种子列表中找到选中种子的信息
            const selectedSeed = userSeeds.find(seed => seed.seedid === formData.selectedSeedId);
            
            setSeeds((prev) => [
                ...prev,
                {
                    beg_id: begid,
                    seed_id: newSeedId,
                    votes: 0,
                    title: selectedSeed?.title || "未知标题",
                    subtitle: selectedSeed?.subtitle || "无简介",
                    seedsize: selectedSeed?.seedsize,
                    downloadtimes: selectedSeed?.downloadtimes || 0,
                    url: selectedSeed?.url,
                    user: { username: "当前用户" }
                },
            ]);
            
            setSeedInfoMap(prev => ({
                ...prev,
                [newSeedId]: {
                    title: selectedSeed?.title || "未知标题",
                    subtitle: selectedSeed?.subtitle || "无简介",
                }
            }));
            
            setShowForm(false);
            setFormData({ selectedSeedId: "" });
            setUserSeeds([]);
            alert("提交成功（前端演示）");
        }
    };

    return (
        <div className="container">
            <h1 style={{ margin: "24px 0 32px 0", color: "#1976d2" }}>
                求种详情
            </h1>
            
            {/* 错误状态 */}
            {error && (
                <div style={{ 
                    textAlign: "center", 
                    margin: "20px 0", 
                    padding: "10px", 
                    background: "#ffebee", 
                    color: "#c62828", 
                    borderRadius: "4px" 
                }}>
                    加载失败: {error} (已显示默认数据)
                </div>
            )}
            
            <div
                style={{
                    background: "#e3f7e7",
                    border: "1.5px solid #b2d8ea",
                    borderRadius: 12,
                    padding: 24,
                    maxWidth: 600,
                    margin: "0 auto 32px auto",
                    boxShadow: "0 2px 8px #e0e7ff",
                }}
            >
                <div style={{ fontWeight: 600, fontSize: 20, marginBottom: 12 }}>
                    {beg.info}
                </div>
                <div>求种人数：{beg.beg_count}</div>
                <div>悬赏魔力值：{beg.reward_magic}</div>
                <div>截止时间：{new Date(beg.deadline).toLocaleString()}</div>
                <div>
                    状态：
                    {isFinished
                        ? "已完成"
                        : isExpired
                            ? "已过期"
                            : "进行中"}
                </div>
            </div>

            <h2 style={{ margin: "24px 0 12px 0" }}>已提交种子</h2>
            <table className="movie-table" style={{ maxWidth: 1000, margin: "0 auto" }}>
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
                            <td colSpan={7} style={{ textAlign: "center" }}>暂无提交的种子</td>
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
                                            style={{
                                                background: "#1976d2",
                                                color: "#fff",
                                                border: "none",
                                                borderRadius: 6,
                                                padding: "6px 18px",
                                                fontWeight: 500,
                                                cursor: "pointer",
                                                transition: "background 0.2s",
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

            {/* 显示总投票数 */}
            {seeds.length > 0 && (
                <div style={{ textAlign: "center", margin: "16px 0", color: "#666" }}>
                    总投票数: {seeds.reduce((total, seed) => total + (seed.votes || 0), 0)}
                </div>
            )}

            {isActive && (
                <div style={{ margin: "32px 0", textAlign: "center" }}>
                    <button
                        onClick={() => {
                            setShowForm(true);
                            fetchUserSeeds();
                        }}
                        style={{
                            fontSize: 18,
                            padding: "12px 36px",
                            background: "linear-gradient(90deg, #42a5f5 0%, #1976d2 100%)",
                            color: "#fff",
                            border: "none",
                            borderRadius: 8,
                            fontWeight: 600,
                            boxShadow: "0 2px 8px #b2d8ea",
                            cursor: "pointer",
                            transition: "background 0.2s",
                        }}
                    >
                        提交种子
                    </button>
                </div>
            )}

            {showForm && isActive && (
                <div
                    style={{
                        background: "#fff",
                        border: "1.5px solid #b2d8ea",
                        borderRadius: 12,
                        padding: 24,
                        maxWidth: 480,
                        margin: "0 auto",
                        boxShadow: "0 2px 8px #e0e7ff",
                    }}
                >
                    <h3 style={{ color: "#1976d2", marginBottom: 18 }}>选择种子</h3>
                    
                    {/* 加载用户种子状态 */}
                    {loadingUserSeeds && (
                        <div style={{ textAlign: "center", margin: "16px 0", color: "#666" }}>
                            正在加载您的种子列表...
                        </div>
                    )}
                    
                    {/* 选择已有种子 */}
                    {userSeeds.length > 0 ? (
                        <div style={{ marginBottom: 24 }}>
                            <div style={{ marginBottom: 16 }}>
                                <label style={{ display: "inline-block", width: 80, fontWeight: 500 }}>选择种子：</label>
                                <select
                                    name="selectedSeedId"
                                    value={formData.selectedSeedId}
                                    onChange={handleFormChange}
                                    style={{
                                        padding: "8px 12px",
                                        borderRadius: 6,
                                        border: "1px solid #b2d8ea",
                                        width: 300,
                                        background: "#fff",
                                        fontSize: 14,
                                    }}
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
                            <div style={{ 
                                textAlign: "center", 
                                margin: "20px 0", 
                                padding: "16px",
                                background: "#fff3cd",
                                color: "#856404",
                                border: "1px solid #ffeaa7",
                                borderRadius: 6
                            }}>
                                您还没有上传过种子，无法参与悬赏
                            </div>
                        )
                    )}
                    
                    <form onSubmit={handleSubmitSeed}>
                        <div style={{ marginTop: 18 }}>
                            <button
                                type="submit"
                                disabled={!formData.selectedSeedId}
                                style={{
                                    background: formData.selectedSeedId ? "#1976d2" : "#b0b0b0",
                                    color: "#fff",
                                    border: "none",
                                    borderRadius: 6,
                                    padding: "8px 28px",
                                    fontWeight: 500,
                                    fontSize: 16,
                                    marginRight: 18,
                                    cursor: formData.selectedSeedId ? "pointer" : "not-allowed",
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
                                style={{
                                    background: "#b0b0b0",
                                    color: "#fff",
                                    border: "none",
                                    borderRadius: 6,
                                    padding: "8px 28px",
                                    fontWeight: 500,
                                    fontSize: 16,
                                    cursor: "pointer",
                                }}
                            >
                                取消
                            </button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
}