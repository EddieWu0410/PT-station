import React, { useEffect, useState } from "react";
import { API_BASE_URL } from "./config";

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
        <div style={{ padding: 40, maxWidth: 900, margin: "0 auto" }}>
            <h1 style={{ textAlign: "center", marginBottom: 32 }}>种子促销管理</h1>
            {loading ? (
                <div style={{ textAlign: "center", color: "#666", margin: 40 }}>正在加载...</div>
            ) : (
                <table style={{ width: "100%", background: "#fff", borderRadius: 10, boxShadow: "0 2px 8px #e0e7ff" }}>
                    <thead>
                        <tr style={{ background: "#f5f5f5" }}>
                            <th>标题</th>
                            <th>标签</th>
                            <th>热度</th>
                            <th>促销开始时间</th>
                            <th>促销结束时间</th>
                            <th>促销倍率</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        {seeds.map((seed) => {
                            const { start_time, end_time, discount } = seed.promotion;
                            const endTimeInvalid = isEndTimeInvalid(start_time, end_time);
                            const canStartPromotion = start_time && end_time && !endTimeInvalid && discount >= 1;
                            return (
                                <tr key={seed.seed_id}>
                                    <td>{seed.title}</td>
                                    <td>{seed.tags}</td>
                                    <td>{seed.popularity}</td>
                                    <td>
                                        <input
                                            type="datetime-local"
                                            value={start_time}
                                            min={currentTime}
                                            onChange={(e) =>
                                                handlePromotionChange(seed.seed_id, "start_time", e.target.value)
                                            }
                                        />
                                    </td>
                                    <td>
                                        <input
                                            type="datetime-local"
                                            value={end_time}
                                            min={start_time || currentTime}
                                            onChange={(e) =>
                                                handlePromotionChange(seed.seed_id, "end_time", e.target.value)
                                            }
                                            style={endTimeInvalid ? { border: "1.5px solid #e53935" } : {}}
                                        />
                                        {endTimeInvalid && (
                                            <div style={{ color: "#e53935", fontSize: 12 }}>
                                                结束时间不能早于开始时间
                                            </div>
                                        )}
                                    </td>
                                    <td>
                                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                                            <button
                                                style={{ width: 28, height: 28, fontSize: 18, borderRadius: 4, border: "1px solid #ccc", background: "#f5f5f5", cursor: discount > 1 ? "pointer" : "not-allowed" }}
                                                onClick={() =>
                                                    discount > 1 &&
                                                    handlePromotionChange(seed.seed_id, "discount", discount - 1)
                                                }
                                                disabled={discount <= 1}
                                            >-</button>
                                            <span style={{ minWidth: 24, textAlign: "center" }}>{discount}</span>
                                            <button
                                                style={{ width: 28, height: 28, fontSize: 18, borderRadius: 4, border: "1px solid #ccc", background: "#f5f5f5", cursor: "pointer" }}
                                                onClick={() =>
                                                    handlePromotionChange(seed.seed_id, "discount", discount + 1)
                                                }
                                            >+</button>
                                        </div>
                                    </td>
                                    <td>
                                        <button
                                            style={{
                                                background: canStartPromotion ? "#1976d2" : "#ccc",
                                                color: "#fff",
                                                border: "none",
                                                borderRadius: 6,
                                                padding: "4px 16px",
                                                cursor: canStartPromotion ? "pointer" : "not-allowed",
                                                fontWeight: 600,
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
    );
}