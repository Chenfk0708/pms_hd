package com.jeez.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 * @author Jeez
 */
@Data
public class PageResult<T> {

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = size != null && size > 0 ? (total + size - 1) / size : 0L;
    }

    public static <T> PageResult<T> of(List<T> records, Long total, Long current, Long size) {
        return new PageResult<>(records, total, current, size);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(null, 0L, 1L, 10L);
    }
}