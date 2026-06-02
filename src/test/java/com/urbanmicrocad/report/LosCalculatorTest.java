package com.urbanmicrocad.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urbanmicrocad.report.service.LosCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("LosCalculator 测试")
class LosCalculatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("averageDelay")
    class AverageDelay {

        @Test
        @DisplayName("null 数据返回 0")
        void nullData_returnsZero() {
            assertThat(LosCalculator.averageDelay(null)).isEqualTo(0);
        }

        @Test
        @DisplayName("空对象返回 0")
        void emptyObject_returnsZero() {
            assertThat(LosCalculator.averageDelay(objectMapper.createObjectNode())).isEqualTo(0);
        }

        @Test
        @DisplayName("数组格式 — 计算加权平均")
        void arrayFormat_computesAverage() {
            ArrayNode arr = objectMapper.createArrayNode();
            arr.add(entry("A", 8.0));
            arr.add(entry("C", 30.0));
            double result = LosCalculator.averageDelay(arr);
            assertThat(result).isCloseTo(19.0, within(0.001));
        }

        @Test
        @DisplayName("对象格式 — 计算加权平均")
        void objectFormat_computesAverage() {
            ObjectNode obj = objectMapper.createObjectNode();
            obj.set("intersection1", entry("A", 5.0));
            obj.set("intersection2", entry("D", 45.0));
            double result = LosCalculator.averageDelay(obj);
            assertThat(result).isCloseTo(25.0, within(0.001));
        }

        @Test
        @DisplayName("使用 delay 字段名")
        void usesDelayFieldName() {
            ArrayNode arr = objectMapper.createArrayNode();
            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("delay", 15.0);
            arr.add(entry);
            assertThat(LosCalculator.averageDelay(arr)).isCloseTo(15.0, within(0.001));
        }

        private ObjectNode entry(String los, double delay) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("los", los);
            node.put("controlDelay", delay);
            return node;
        }
    }

    @Nested
    @DisplayName("losGrade")
    class LosGrade {

        @Test
        @DisplayName("A: ≤10s")
        void gradeA() {
            assertThat(LosCalculator.losGrade(0)).isEqualTo("A");
            assertThat(LosCalculator.losGrade(10)).isEqualTo("A");
        }

        @Test
        @DisplayName("B: ≤20s")
        void gradeB() {
            assertThat(LosCalculator.losGrade(15)).isEqualTo("B");
            assertThat(LosCalculator.losGrade(20)).isEqualTo("B");
        }

        @Test
        @DisplayName("C: ≤35s")
        void gradeC() {
            assertThat(LosCalculator.losGrade(25)).isEqualTo("C");
            assertThat(LosCalculator.losGrade(35)).isEqualTo("C");
        }

        @Test
        @DisplayName("D: ≤55s")
        void gradeD() {
            assertThat(LosCalculator.losGrade(40)).isEqualTo("D");
            assertThat(LosCalculator.losGrade(55)).isEqualTo("D");
        }

        @Test
        @DisplayName("E: ≤80s")
        void gradeE() {
            assertThat(LosCalculator.losGrade(60)).isEqualTo("E");
            assertThat(LosCalculator.losGrade(80)).isEqualTo("E");
        }

        @Test
        @DisplayName("F: >80s")
        void gradeF() {
            assertThat(LosCalculator.losGrade(81)).isEqualTo("F");
            assertThat(LosCalculator.losGrade(200)).isEqualTo("F");
        }
    }
}
