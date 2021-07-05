package com.cyberark.identity.util;

data class ResponseHandler<out T>(val status: ResponseStatus, val data: T?, val message: String?) {

    companion object {
        fun <T> success(data: T?): ResponseHandler<T> {
            return ResponseHandler(ResponseStatus.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): ResponseHandler<T> {
            return ResponseHandler(ResponseStatus.ERROR, data, msg)
        }

        fun <T> loading(data: T?): ResponseHandler<T> {
            return ResponseHandler(ResponseStatus.LOADING, data, null)
        }
    }
}