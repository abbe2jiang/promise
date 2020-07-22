package org.dj.we.controller;

import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class Pagination<T> {
    private int page;
    private int size = 10;
    private long total;
    private List<T> data;


    public static <T> Pagination<T> of(List<T> data, Pageable pageable, long total) {
        Pagination pagination = new Pagination<T>();
        pagination.page = pageable.getPageNumber()+1;
        pagination.size = pageable.getPageSize();
        pagination.data = data;
        pagination.total = total;
        return pagination;
    }

    public <E> Pagination<E>  updateData(List<E> data){
        Pagination pagination = new Pagination<E>();
        pagination.page = page;
        pagination.size = size;
        pagination.data = data;
        pagination.total = total;
        return pagination;
    }
}
