package org.dj.we.controller;

import lombok.Data;

@Data
public class Response<T> {

    public static <T> Response<T> succeed(T data) {
        return new Response(true, data, null);
    }

    public static <T> Response<T> fail(String error) {
        return new Response(false, null, error);
    }

    private boolean success;
    private T data;
    private String error;

    private Response(boolean success, T data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }
}
