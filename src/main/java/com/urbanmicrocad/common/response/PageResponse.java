package com.urbanmicrocad.common.response;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.function.Function;

/**
 * 通用分页响应体。使用 records 字段名，前端 PageResponse 兼容 records/content/items。
 *
 * @param <T> 数据项类型
 */
public record PageResponse<T>(
    List<T> records,
    long total,
    int page,
    int size
) {
    /**
     * 从 MyBatis-Plus Page 转换，同时映射实体到 DTO。
     *
     * @param mpPage MyBatis-Plus 分页结果
     * @param mapper 实体到 DTO 的映射函数
     * @param <R>    实体类型
     * @param <T>    DTO 类型
     */
    public static <R, T> PageResponse<T> from(Page<R> mpPage, Function<R, T> mapper) {
        List<T> mapped = mpPage.getRecords().stream().map(mapper).toList();
        return new PageResponse<>(
            mapped,
            mpPage.getTotal(),
            (int) mpPage.getCurrent(),
            (int) mpPage.getSize()
        );
    }
}
