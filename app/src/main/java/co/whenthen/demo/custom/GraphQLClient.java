package co.whenthen.demo.custom;


import android.content.Context;

import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.interceptor.ApolloInterceptor;
import com.apollographql.apollo.interceptor.ApolloInterceptorChain;
import com.apollographql.apollo.request.RequestHeaders;

import java.util.UUID;
import java.util.concurrent.Executor;
import co.whenthen.demo.Constants;

/**
 * GraphQL Client to execute request against WhenThen API
 */
public class GraphQLClient {

    private static ApolloClient instance = null;

    public static ApolloClient getInstance(){
        if(instance != null){
            return  instance;
        }

        instance =  ApolloClient.builder()
                        .serverUrl(Constants.API_ENDPOINT)
                        .addApplicationInterceptor(new AuthorizationInterceptor())

                        .build();
        return instance;
    }

    private static class AuthorizationInterceptor implements ApolloInterceptor {

        @Override
        public void interceptAsync(@NonNull InterceptorRequest request,
                                   @NonNull ApolloInterceptorChain chain,
                                   @NonNull Executor dispatcher,
                                   @NonNull CallBack callBack) {

            /**
             * Add HTTP headers: CLIENT_TOKEN should never be hardcoded, but generated from a server-side request
             */
            RequestHeaders headers = RequestHeaders.builder()
                    .addHeader("Authorization", "Bearer " + Constants.CLIENT_TOKEN)
                    .addHeader("X-Idempotency-Key", UUID.randomUUID().toString()).build();

            chain.proceedAsync(request.toBuilder().requestHeaders(headers).build(),
                                dispatcher, callBack);
        }

        @Override
        public void dispose() {

        }
    }

}
