package co.whenthen.demo.natived;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.http.HttpRequest;
import com.apollographql.apollo3.api.http.HttpResponse;
import com.apollographql.apollo3.network.http.HttpInterceptor;
import com.apollographql.apollo3.network.http.HttpInterceptorChain;

import java.io.IOException;

import co.whenthen.demo.Constants;
import kotlin.coroutines.Continuation;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Apollo  {

    private ApolloClient instance = null;

    public ApolloClient getInstance(Context context){
        if(instance != null){
            return  instance;
        }

        instance = new ApolloClient.Builder()
                        .httpServerUrl(Constants.API_ENDPOINT)
                        .addHttpInterceptor(new AuthorizationInterceptor())
                        .build();
        return instance;
    }

    private static class AuthorizationInterceptor implements HttpInterceptor {

        @Nullable
        @Override
        public Object intercept(@NonNull HttpRequest httpRequest,
                                @NonNull HttpInterceptorChain httpInterceptorChain,
                                @NonNull Continuation<? super HttpResponse> continuation) {

            return httpInterceptorChain.proceed(httpRequest.newBuilder()
                    .addHeader("Authorization", Constants.CLIENT_TOKEN)
                    .build(), continuation);
        }

        @Override
        public void dispose() {

        }
    }

}
