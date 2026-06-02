package com.urbanmicrocad.report.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 基于 HCM 第6版阈值的服务水平（LOS）计算工具。
 * 所有方法为静态纯函数，无状态依赖。
 *
 * <p>HCM 信号交叉口控制延误阈值（秒/车）：
 * <ul>
 *   <li>A: ≤10</li>
 *   <li>B: ≤20</li>
 *   <li>C: ≤35</li>
 *   <li>D: ≤55</li>
 *   <li>E: ≤80</li>
 *   <li>F: &gt;80</li>
 * </ul>
 */
public final class LosCalculator {

    private LosCalculator() {
        // 工具类，禁止实例化
    }

    /**
     * 从交叉口 LOS JSONB 数据计算平均控制延误。
     *
     * <p>期望 intersectionLos 为对象或数组，每个条目包含 "controlDelay" 或 "delay" 数值字段。
     * 若数据为空或无有效条目，返回 0。
     */
    public static double averageDelay(JsonNode intersectionLos) {
        if (intersectionLos == null || intersectionLos.isEmpty()) {
            return 0;
        }
        double total = 0;
        int count = 0;
        if (intersectionLos.isArray()) {
            for (JsonNode entry : intersectionLos) {
                double delay = extractDelay(entry);
                if (delay >= 0) {
                    total += delay;
                    count++;
                }
            }
        } else if (intersectionLos.isObject()) {
            var fields = intersectionLos.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                double delay = extractDelay(entry.getValue());
                if (delay >= 0) {
                    total += delay;
                    count++;
                }
            }
        }
        return count == 0 ? 0 : total / count;
    }

    /**
     * 按 HCM 第6版阈值将平均控制延误映射为 LOS 等级。
     */
    public static String losGrade(double averageDelay) {
        if (averageDelay <= 10) return "A";
        if (averageDelay <= 20) return "B";
        if (averageDelay <= 35) return "C";
        if (averageDelay <= 55) return "D";
        if (averageDelay <= 80) return "E";
        return "F";
    }

    private static double extractDelay(JsonNode entry) {
        if (entry == null || !entry.isObject()) return -1;
        JsonNode delay = entry.has("controlDelay") ? entry.get("controlDelay")
            : entry.has("delay") ? entry.get("delay") : null;
        if (delay != null && delay.isNumber()) {
            return delay.asDouble();
        }
        return -1;
    }
}
