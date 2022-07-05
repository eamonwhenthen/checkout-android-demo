package co.whenthen.demo.custom;


import android.util.Log;

import androidx.annotation.NonNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.interceptor.ApolloInterceptor;
import com.apollographql.apollo.interceptor.ApolloInterceptorChain;
import com.apollographql.apollo.request.RequestHeaders;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

import co.whenthen.demo.AuthorizePaymentMutation;
import co.whenthen.demo.Constants;
import co.whenthen.demo.R;
import co.whenthen.demo.TokenisePaymentMethodMutation;
import co.whenthen.demo.type.AuthorisedPaymentInput;
import co.whenthen.demo.type.GooglePayInput;
import co.whenthen.demo.type.PaymentCardInput;
import co.whenthen.demo.type.PaymentMethodDtoInput;
import co.whenthen.demo.type.PaymentMethodInput;
import co.whenthen.demo.type.ThreeDSecureDtoInput;
import co.whenthen.demo.type.TokenInput;

/**
 * GraphQL Client to execute request against WhenThen API
 */
public class GraphQLClient {

    private static final String TAG = "GraphQLClient";

    private static ApolloClient client = null;
    private static GraphQLClient instance = null;
    private CheckoutActivity activity = null;

    private static ApolloClient getClient(){
        if(client != null){
            return client;
        }

        client = ApolloClient.builder()
                        .serverUrl(Constants.API_ENDPOINT)
                        .addApplicationInterceptor(new AuthorizationInterceptor())
                        .build();
        return client;
    }

    public static GraphQLClient getInstance(CheckoutActivity activity){
        if(instance != null){
            return  instance;
        }
        instance = new GraphQLClient();
        instance.activity = activity;
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

    /**
     * Authorize a Payment using GooglePay token
     * @param googlePayToken
     */
    public void authorizePayment(ApolloCall.Callback<AuthorizePaymentMutation.Data> authorizePaymentCallback, String googlePayToken, long amount){

        PaymentMethodDtoInput paymentMethodDtoInput =  PaymentMethodDtoInput.builder()
                .type(Constants.PAYMENT_METHOD_GOOGLE_PAY)
                .walletToken(googlePayToken)
                .googlePay(GooglePayInput.builder().transactionId(UUID.randomUUID().toString()).build())
                .build();

        AuthorisedPaymentInput input = AuthorisedPaymentInput.builder()
                .orderId(UUID.randomUUID().toString())
                .flowId(Constants.FLOW_ID)
                .currencyCode(Constants.CURRENCY_CODE)
                .amount(amount)
                .paymentMethod(paymentMethodDtoInput)
                .build();

        try{
            getClient().mutate(AuthorizePaymentMutation.builder()
                            .authorisePayment(input)
                            .build())
                    .enqueue(authorizePaymentCallback);
        }catch (ApolloException e){
            Log.e(TAG, "authorizePayment: ", e);
        }
    }

    /**
     * Authorize Payment using a Card token
     * @param authorizePaymentCallback
     * @param cardInput
     * @param amount
     *  TODO: Handle 3DS
     */
    public void authorizePayment(ApolloCall.Callback<AuthorizePaymentMutation.Data> authorizePaymentCallback, Input<PaymentCardInput> cardInput, long amount){

        //Tokenize the card
        TokenInput tokenInput = TokenInput.builder().paymentMethod(PaymentMethodInput.builder()
                .cardInput(cardInput).build()).build();
        getClient().mutate(TokenisePaymentMethodMutation.builder()
                .data(tokenInput)
                .build()).enqueue(new ApolloCall.Callback<TokenisePaymentMethodMutation.Data>() {
            @Override
            public void onResponse(@NonNull Response<TokenisePaymentMethodMutation.Data> tokeniseResponse) {
                if(tokeniseResponse.hasErrors()){
                    activity.showResult(activity.getString(R.string.card_tokenization_error));
                    return;
                }

                String cardToken = Objects.requireNonNull(tokeniseResponse.getData()).tokenisePaymentMethod().token();

                Log.d(TAG, "cardToken: " + cardToken);
                PaymentMethodDtoInput paymentMethodDtoInput =  PaymentMethodDtoInput.builder()
                        .type(Constants.PAYMENT_METHOD_CARD)
                        .token(cardToken)
                        .build();

                AuthorisedPaymentInput input = AuthorisedPaymentInput.builder()
                        .orderId(UUID.randomUUID().toString())
                        .flowId(Constants.FLOW_ID)
                        .currencyCode(Constants.CURRENCY_CODE)
                        .amount(amount)
                        .paymentMethod(paymentMethodDtoInput)
                        .perform3DSecure(ThreeDSecureDtoInput.builder().redirectUrl("someUrl.com").build())
                        .build();

                getClient().mutate(AuthorizePaymentMutation.builder()
                                .authorisePayment(input)
                                .build())
                        .enqueue(authorizePaymentCallback);
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                Log.e(TAG, "tokenizePaymentMethod onFailure: ", e);
                activity.showResult(activity.getString(R.string.card_tokenization_error));
            }
        });
    }
}
