package com.call_blocke.a_repository.unit;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

class FixEncodingInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        MediaType oldMediaType = MediaType.parse(Objects.requireNonNull(response.header("Content-Type")));
        // update only charset in mediatype
        MediaType newMediaType = MediaType.parse(oldMediaType.type()+"/"+oldMediaType.subtype()+"; charset=utf-8");
        // update body
        ResponseBody newResponseBody = ResponseBody.create(newMediaType, response.body().bytes());

        return response.newBuilder()
                .removeHeader("Content-Type")
                .addHeader("Content-Type", newMediaType.toString())
                .body(newResponseBody)
                .build();
    }
}
