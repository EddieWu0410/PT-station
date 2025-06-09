import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "./config";
import "./AdminPage.css";

// 示例数据
const initialConfig = {
    FarmNumber: 3,
    FakeTime: 3,
    BegVote: 3,
    CheatTime: 5,
};

export default function AdminPage() {
    const navigate = useNavigate();
    const [config, setConfig] = useState(initialConfig);
    // state for users fetched from backend
    const [cheatUsers, setCheatUsers] = useState([]);
    const [suspiciousUsers, setSuspiciousUsers] = useState([]);

    // helper to get admin userId from cookie
    const getUserIdFromCookie = () => {
        const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
        return match ? match[2] : null;
    };

    // fetch cheat users list from backend
    const fetchCheatUsers = async () => {
        const adminId = getUserIdFromCookie();
        if (!adminId) return;
        try {
            const res = await fetch(`${API_BASE_URL}/api/admin/cheat-users?userid=${adminId}`);
            if (!res.ok) throw new Error('获取作弊用户失败');
            const data = await res.json();
            setCheatUsers(data);
        } catch (err) {
            console.error(err);
        }
    };

    // fetch suspicious users list from backend
    const fetchSuspiciousUsers = async () => {
        const adminId = getUserIdFromCookie();
        if (!adminId) return;
        try {
            const res = await fetch(`${API_BASE_URL}/api/admin/suspicious-users?userid=${adminId}`);
            if (!res.ok) throw new Error('获取可疑用户失败');
            const data = await res.json();
            setSuspiciousUsers(data);
        } catch (err) {
            console.error(err);
        }
    };

    const handleBan = (user) => {
        const adminId = getUserIdFromCookie();
        if (!adminId) {
            alert('无法获取用户ID');
            return;
        }
        fetch(`${API_BASE_URL}/api/admin/ban-user`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userid: user.userid }),
        })
            .then((res) => { if (!res.ok) throw new Error('Network response was not ok'); return res.json(); })
            .then(() => {
                // 重新获取用户列表，触发页面重新渲染
                fetchSuspiciousUsers();
                fetchCheatUsers();
            })
            .catch((err) => console.error('封禁用户失败:', err));
    }

    // 解封作弊用户
    const handleUnban = (user) => {
        const adminId = getUserIdFromCookie();
        if (!adminId) {
            alert('无法获取用户ID');
            return;
        }
        fetch(`${API_BASE_URL}/api/admin/unban-user`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userid: user.userid }),
        })
            .then((res) => { if (!res.ok) throw new Error('Network response was not ok'); return res.json(); })
            .then(() => {
                // 重新获取用户列表，触发页面重新渲染
                fetchCheatUsers();
                fetchSuspiciousUsers();
            })
            .catch((err) => console.error('解封用户失败:', err));
    };

    // 初始化时向后端请求系统参数及用户列表
    useEffect(() => {
        const match = document.cookie.match('(^|;)\\s*userId=([^;]+)');
        const userId = match ? match[2] : null;
        // console.log("User ID from cookie:", userId);
        if (userId) {
            // fetch config
            fetch(`${API_BASE_URL}/api/admin/config?userid=${userId}`)
                .then((res) => {
                    if (!res.ok) throw new Error('Network response was not ok');
                    return res.json();
                })
                .then((data) => {
                    // console.log("Fetched system config:", data);
                    setConfig(data);
                })
                .catch((err) => console.error('获取系统参数失败:', err));

            // 初始获取用户列表
            fetchCheatUsers();
            fetchSuspiciousUsers();
        }
    }, []);

    return (
        <div className="admin-page-container">
            <div className="admin-main-content">
                <h1 className="admin-title">管理员页面</h1>
                {/* 参数设置 */}
                <div className="admin-config-card">
                    <span className="admin-config-label">系统参数：</span>
                    <span className="admin-config-item">FarmNumber: {config.FarmNumber}</span>
                    <span className="admin-config-item">FakeTime: {config.FakeTime}</span>
                    <span className="admin-config-item">BegVote: {config.BegVote}</span>
                    <span className="admin-config-item">CheatTime: {config.CheatTime}</span>
                </div>
                {/* 作弊用户 */}
                <div className="admin-section">
                    <h2 className="admin-section-title cheat">作弊用户</h2>
                    <div className="admin-table-container">
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>邮箱</th>
                                    <th>用户名</th>
                                    <th>账户状态</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                {cheatUsers.map((u) => (
                                    <tr key={u.userid}>
                                        <td>{u.email}</td>
                                        <td>{u.username}</td>
                                        <td className="status-banned">
                                            封禁
                                        </td>
                                        <td>
                                            <button
                                                className="admin-btn admin-btn-unban"
                                                onClick={() => handleUnban(u)}
                                            >
                                                解封
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
                {/* 可疑用户 */}
                <div className="admin-section">
                    <h2 className="admin-section-title suspicious">可疑用户</h2>
                    <div className="admin-table-container">
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>邮箱</th>
                                    <th>用户名</th>
                                    <th>账户状态</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                {suspiciousUsers.map((u) => (
                                    <tr key={u.user_id}>
                                        <td>{u.email}</td>
                                        <td>{u.username}</td>
                                        <td className={u.account_status === 1 ? "status-banned" : "status-normal"}>
                                            {u.account_status === 1 ? "封禁" : "正常"}
                                        </td>
                                        <td>
                                            <button
                                                className="admin-btn admin-btn-ban"
                                                onClick={() => handleBan(u)}
                                            >
                                                封禁
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
                {/* 跳转按钮 */}
                <div className="admin-nav-buttons">
                    <button
                        className="admin-nav-btn appeal"
                        onClick={() => navigate("/appeal-review")}
                    >
                        用户申诉
                    </button>
                    <button
                        className="admin-nav-btn migration"
                        onClick={() => navigate("/migration-review")}
                    >
                        用户迁移
                    </button>
                    <button
                        className="admin-nav-btn promotion"
                        onClick={() => navigate("/seed-promotion")}
                    >
                        促销管理
                    </button>
                </div>
            </div>
        </div>
    );
}