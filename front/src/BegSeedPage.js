import React, { useState, useEffect } from "react";
import HelpIcon from "@mui/icons-material/Help";
import PeopleIcon from "@mui/icons-material/People";
import MonetizationOnIcon from "@mui/icons-material/MonetizationOn";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "./config";
import "./BegSeedPage.css";

export default function BegSeedPage() {
    const navigate = useNavigate();
    const now = new Date();
    const [begSeedList, setBegSeedList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [refreshKey, setRefreshKey] = useState(0); // 用于强制重新渲染
    const [formData, setFormData] = useState({
        info: "",
        reward_magic: "",
        deadline: "",
    });

    // 从后端获取求种列表
    const fetchBegSeedList = async () => {
        setLoading(true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/begseed-list`);
            if (!response.ok) {
                throw new Error(`请求失败，状态码: ${response.status}`);
            }
            const data = await response.json();
            console.log("获取到的求种列表数据:", data);
            
            // 格式化数据以匹配前端期望的格式
            const formattedData = data.map(item => ({
                beg_id: item.beg_id || item.begid || item.id,
                info: item.info || item.description || item.content,
                beg_count: item.beg_count || item.begCount || 1,
                reward_magic: item.reward_magic || item.rewardMagic || item.magic,
                deadline: item.deadline || item.endtime,
                has_match: item.has_match || item.hasMatch || item.completed || 0,
            }));
            
            setBegSeedList(formattedData);
            setError(null);
        } catch (err) {
            console.error('获取求种列表失败:', err);
            setError(err.message);
            // 如果API调用失败，使用默认数据
            setBegSeedList([
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
            ]);
        } finally {
            setLoading(false);
        }
    };

    // 组件挂载时获取数据
    useEffect(() => {
        fetchBegSeedList();
    }, []);

    // 表单输入处理
    const handleFormChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    // 提交新求种任务
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        // 获取用户ID
        const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
        const userId = match ? match[2] : null;
        
        if (!userId) {
            alert("请先登录后再发布求种任务");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/create-begseed`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    userid: userId,
                    info: formData.info,
                    reward_magic: Number(formData.reward_magic),
                    deadline: formData.deadline,
                }),
            });

            if (response.ok) {
                // 成功创建，重新获取列表并强制重新渲染
                setLoading(true); // 显示加载状态
                await fetchBegSeedList();
                setShowForm(false);
                setFormData({ info: "", reward_magic: "", deadline: "" });
                setRefreshKey(prev => prev + 1); // 强制重新渲染
                alert("发布成功！");
            } else {
                const errorData = await response.json();
                alert(`发布失败: ${errorData.message || '未知错误'}`);
            }
        } catch (err) {
            console.error('发布求种任务失败:', err);
            // 如果后端调用失败，则使用前端演示逻辑
            // setLoading(true); // 显示加载状态
            // const newBegId = "beg" + Math.floor(Math.random() * 10000);
            // setBegSeedList([
            //     {
            //         beg_id: newBegId,
            //         info: formData.info,
            //         beg_count: 1,
            //         reward_magic: Number(formData.reward_magic),
            //         deadline: formData.deadline,
            //         has_match: 0,
            //     },
            //     ...begSeedList,
            // ]);
            // setLoading(false); // 隐藏加载状态
            // setShowForm(false);
            // setFormData({ info: "", reward_magic: "", deadline: "" });
            // setRefreshKey(prev => prev + 1); // 强制重新渲染
            // alert("发布成功（前端演示）");
        }
    };

    return (
        <div className="begseed-container">
            <div className="begseed-content">
                <h1 className="begseed-title">
                    <HelpIcon className="begseed-title-icon" style={{ fontSize: 42 }} />
                    求种列表
                </h1>
                
                {/* 加载状态 */}
                {loading && (
                    <div className="begseed-loading">
                        正在加载求种列表...
                    </div>
                )}
                
                {/* 错误状态 */}
                {error && (
                    <div className="begseed-error">
                        加载失败: {error} (已显示默认数据)
                    </div>
                )}
                
                <div className="begseed-publish-section">
                    <button
                        onClick={() => setShowForm(true)}
                        disabled={loading}
                        className="begseed-publish-btn"
                    >
                        发布求种任务
                    </button>
                </div>
                
                {showForm && (
                    <div className="begseed-form-container">
                        <h3 className="begseed-form-title">发布求种任务</h3>
                        <form onSubmit={handleSubmit}>
                            <div className="begseed-form-group">
                                <label className="begseed-form-label">求种信息：</label>
                                <input
                                    type="text"
                                    name="info"
                                    value={formData.info}
                                    onChange={handleFormChange}
                                    required
                                    className="begseed-form-input"
                                    placeholder="请输入您要求种的资源信息"
                                />
                            </div>
                            <div className="begseed-form-group">
                                <label className="begseed-form-label">悬赏魔力值：</label>
                                <input
                                    type="number"
                                    name="reward_magic"
                                    value={formData.reward_magic}
                                    onChange={handleFormChange}
                                    required
                                    min={1}
                                    className="begseed-form-input"
                                    placeholder="请输入悬赏的魔力值"
                                />
                            </div>
                            <div className="begseed-form-group">
                                <label className="begseed-form-label">截止日期：</label>
                                <input
                                    type="datetime-local"
                                    name="deadline"
                                    value={formData.deadline}
                                    onChange={handleFormChange}
                                    required
                                    className="begseed-form-input"
                                />
                            </div>
                            <div className="begseed-form-actions">
                                <button type="submit" className="begseed-form-btn begseed-form-btn-primary">
                                    提交
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setShowForm(false)}
                                    className="begseed-form-btn begseed-form-btn-secondary"
                                >
                                    取消
                                </button>
                            </div>
                        </form>
                    </div>
                )}
                
                <div key={refreshKey} className="begseed-list">
                    {begSeedList.map((beg) => {
                        const isExpired = new Date(beg.deadline) < now || beg.has_match === 1;
                        const isCompleted = beg.has_match === 1;
                        const isActive = !isExpired && !isCompleted;
                        
                        return (
                            <div
                                key={beg.beg_id}
                                className={`begseed-card ${isExpired ? 'begseed-card-expired' : ''}`}
                                onClick={() => navigate(`/begseed/${beg.beg_id}`)}
                            >
                                <div className="begseed-card-content">
                                    <div className="begseed-card-title">
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
                                        <span 
                                            className={`begseed-card-status ${
                                                isCompleted ? 'begseed-status-completed' : 
                                                isExpired ? 'begseed-status-expired' : 
                                                'begseed-status-active'
                                            }`}
                                        >
                                            {isCompleted ? "已完成" : isExpired ? "已过期" : "进行中"}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}