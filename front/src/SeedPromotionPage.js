import React, { useEffect, useState } from "react";
import { API_BASE_URL } from "./config";
import "./AppealPage.css";

export default function SeedPromotionPage() {
    const [seeds, setSeeds] = useState([]);
    const [currentTime, setCurrentTime] = useState("");
    const [loading, setLoading] = useState(true);

    // 时间戳转datetime-local字符串
    const tsToDatetimeLocal = (ts) => {
        if (!ts) return "";
        const d = new Date(ts);
        const pad = (n) => n.toString().padStart(2, "0");
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    };

    // 加载所有种子和促销信息
    useEffect(() => {
        const now = new Date();
        const pad = (n) => n.toString().padStart(2, "0");
        const localISOTime = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
        setCurrentTime(localISOTime);

        async function fetchData() {
            setLoading(true);
            try {
                // 获取所有种子
                const seedsRes = await fetch(`${API_BASE_URL}/api/all-seeds`);
                if (!seedsRes.ok) throw new Error("获取种子列表失败");
                const seedsData = await seedsRes.json();

                // 获取所有促销信息
                const promoRes = await fetch(`${API_BASE_URL}/api/all-seed-promotions`);
                if (!promoRes.ok) throw new Error("获取促销信息失败");
                const promoData = await promoRes.json();

                // 构建促销信息映射
                const promoMap = {};
                promoData.forEach(p => {
                    const seedid = p.seed?.seedid;
                    promoMap[seedid] = {
                        start_time: p.startTime ? tsToDatetimeLocal(p.startTime) : "",
                        end_time: p.endTime ? tsToDatetimeLocal(p.endTime) : "",
                        discount: typeof p.discount === "number" ? p.discount : 1,
                    };
                });

                // 合并数据
                const mergedSeeds = seedsData.map(seed => ({
                    seed_id: seed.seedid,
                    title: seed.title,
                    tags: seed.seedtag,
                    popularity: seed.downloadtimes,
                    promotion: promoMap[seed.seedid] || {
                        start_time: "",
                        end_time: "",
                        discount: 1,
                    },
                }));

                setSeeds(mergedSeeds);
            } catch (err) {
                setSeeds([]);
                alert(err.message || "加载失败");
            } finally {
                setLoading(false);
            }
        }

        fetchData();
    }, []);

    // 输入框变更处理
    const handlePromotionChange = (seedId, field, value) => {
        setSeeds((prev) =>
            prev.map((s) =>
                s.seed_id === seedId
                    ? {
                        ...s,
                        promotion: {
                            ...s.promotion,
                            [field]: value,
                        },
                    }
                    : s
            )
        );
    };

    // 结束时间校验
    const isEndTimeInvalid = (start, end) => {
        return start && end && end < start;
    };

    // 提交促销设置
    const handleStartPromotion = async (seed) => {
        const { start_time, end_time, discount } = seed.promotion;
        try {
            const res = await fetch(`${API_BASE_URL}/api/set-seed-promotion`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    seed_id: seed.seed_id,
                    start_time,
                    end_time,
                    discount,
                }),
            });
            if (!res.ok) {
                const errData = await res.json();
                throw new Error(errData.message || "促销设置失败");
            }
            alert(`已为「${seed.title}」开启促销！`);
        } catch (err) {
            alert(err.message || "促销设置失败");
        }
    };

    return (
        <div className="appeal-page-container">
            {/* 侧栏 */}
            <div className="appeal-sidebar">
                <h3 className="appeal-sidebar-title">种子列表</h3>
                <div className="appeal-list-container">
                    {seeds.map(seed => (
                        <div
                            key={seed.seed_id}
                            className="appeal-list-item"
                            style={{ cursor: "default" }}
                        >
                            {seed.title}
                            <span className="appeal-status-label approved">
                                {seed.promotion && seed.promotion.start_time && seed.promotion.end_time ? "有促销" : "无促销"}
                            </span>
                        </div>
                    ))}
                </div>
            </div>
            {/* 促销详情 */}
            <div className="appeal-main-content">
                <h2 className="appeal-detail-title">促销管理</h2>
                <div className="appeal-detail-card" style={{ overflowX: "auto", padding: 0 }}>
                    {loading ? (
                        <div className="appeal-loading" style={{ minHeight: 300 }}>正在加载...</div>
                    ) : (
                        <table style={{
                            width: "100%",
                            borderCollapse: "collapse",
                            background: "transparent",
                            borderRadius: 25,
                            overflow: "hidden",
                            margin: 0
                        }}>
                            <thead>
                                <tr style={{
                                    background: "linear-gradient(90deg, #f0fff0 0%, #98fb98 100%)",
                                    color: "#2d5016",
                                    fontWeight: 700,
                                    fontFamily: "'Lora', serif",
                                    fontSize: 16
                                }}>
                                    <th style={{ padding: "18px 12px" }}>标题</th>
                                    <th style={{ padding: "18px 12px" }}>标签</th>
                                    <th style={{ padding: "18px 12px" }}>热度</th>
                                    <th style={{ padding: "18px 12px" }}>促销开始时间</th>
                                    <th style={{ padding: "18px 12px" }}>促销结束时间</th>
                                    <th style={{ padding: "18px 12px" }}>促销倍率</th>
                                    <th style={{ padding: "18px 12px" }}>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                {seeds.map((seed, idx) => {
                                    const { start_time, end_time, discount } = seed.promotion;
                                    const endTimeInvalid = isEndTimeInvalid(start_time, end_time);
                                    const canStartPromotion = start_time && end_time && !endTimeInvalid && discount >= 1;
                                    return (
                                        <tr key={seed.seed_id} style={{
                                            background: idx % 2 === 0 ? "rgba(240,255,240,0.7)" : "rgba(255,255,255,0.95)",
                                            fontSize: 15,
                                            color: "#2d5016"
                                        }}>
                                            <td style={{ padding: "16px 12px", fontWeight: 600 }}>{seed.title}</td>
                                            <td style={{ padding: "16px 12px" }}>{seed.tags}</td>
                                            <td style={{ padding: "16px 12px" }}>{seed.popularity}</td>
                                            <td style={{ padding: "16px 12px" }}>
                                                <input
                                                    type="datetime-local"
                                                    value={start_time}
                                                    min={currentTime}
                                                    onChange={(e) =>
                                                        handlePromotionChange(seed.seed_id, "start_time", e.target.value)
                                                    }
                                                    className="appeal-form-input"
                                                    style={{
                                                        width: 180,
                                                        background: "rgba(240,255,240,0.5)",
                                                        border: "2px solid rgba(144, 238, 144, 0.3)",
                                                        borderRadius: 8,
                                                        padding: "8px 12px",
                                                        fontSize: 15,
                                                        fontFamily: "Lora, serif"
                                                    }}
                                                />
                                            </td>
                                            <td style={{ padding: "16px 12px" }}>
                                                <input
                                                    type="datetime-local"
                                                    value={end_time}
                                                    min={start_time || currentTime}
                                                    onChange={(e) =>
                                                        handlePromotionChange(seed.seed_id, "end_time", e.target.value)
                                                    }
                                                    className="appeal-form-input"
                                                    style={{
                                                        width: 180,
                                                        background: "rgba(240,255,240,0.5)",
                                                        border: endTimeInvalid ? "2px solid #e53935" : "2px solid rgba(144, 238, 144, 0.3)",
                                                        borderRadius: 8,
                                                        padding: "8px 12px",
                                                        fontSize: 15,
                                                        fontFamily: "Lora, serif"
                                                    }}
                                                />
                                                {endTimeInvalid && (
                                                    <div style={{ color: "#e53935", fontSize: 12, marginTop: 4 }}>
                                                        结束时间不能早于开始时间
                                                    </div>
                                                )}
                                            </td>
                                            <td style={{ padding: "16px 12px" }}>
                                                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                                                    <button
                                                        className="appeal-btn"
                                                        style={{
                                                            width: 32,
                                                            height: 32,
                                                            padding: 0,
                                                            minWidth: 0,
                                                            borderRadius: 8,
                                                            fontSize: 18,
                                                            background: "linear-gradient(135deg, #f0fff0 0%, #98fb98 100%)",
                                                            color: "#2d5016",
                                                            border: "1.5px solid #90ee90",
                                                            fontWeight: 700,
                                                            boxShadow: "none"
                                                        }}
                                                        onClick={() =>
                                                            discount > 1 &&
                                                            handlePromotionChange(seed.seed_id, "discount", discount - 1)
                                                        }
                                                        disabled={discount <= 1}
                                                    >-</button>
                                                    <span style={{ minWidth: 28, textAlign: "center", fontWeight: 700 }}>{discount}</span>
                                                    <button
                                                        className="appeal-btn"
                                                        style={{
                                                            width: 32,
                                                            height: 32,
                                                            padding: 0,
                                                            minWidth: 0,
                                                            borderRadius: 8,
                                                            fontSize: 18,
                                                            background: "linear-gradient(135deg, #f0fff0 0%, #98fb98 100%)",
                                                            color: "#2d5016",
                                                            border: "1.5px solid #90ee90",
                                                            fontWeight: 700,
                                                            boxShadow: "none"
                                                        }}
                                                        onClick={() =>
                                                            handlePromotionChange(seed.seed_id, "discount", discount + 1)
                                                        }
                                                    >+</button>
                                                </div>
                                            </td>
                                            <td style={{ padding: "16px 12px" }}>
                                                <button
                                                    className="appeal-btn appeal-btn-approve"
                                                    style={{
                                                        padding: "8px 22px",
                                                        fontSize: 15,
                                                        borderRadius: 10,
                                                        fontWeight: 700,
                                                        minWidth: 0
                                                    }}
                                                    disabled={!canStartPromotion}
                                                    onClick={() => handleStartPromotion(seed)}
                                                >
                                                    开启促销
                                                </button>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </div>
    );
}