package co.whenthen.demo.custom;

import static co.whenthen.demo.custom.PaymentsUtil.CENTS_IN_A_UNIT;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.os.Bundle;

import co.whenthen.demo.AuthorizePaymentMutation;
import co.whenthen.demo.Constants;
import co.whenthen.demo.R;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import co.whenthen.demo.TokenisePaymentMethodMutation;
import co.whenthen.demo.type.AuthorisedPaymentInput;
import co.whenthen.demo.type.GooglePayInput;
import co.whenthen.demo.type.PaymentCardInput;
import co.whenthen.demo.type.PaymentMethodDtoInput;
import co.whenthen.demo.type.PaymentMethodInput;
import co.whenthen.demo.type.PaymentStatus;
import co.whenthen.demo.type.ThreeDSecureDtoInput;
import co.whenthen.demo.type.TokenInput;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private PaymentsClient googlePayClient;
    private final int LOAD_PAYMENT_DATA_REQUEST_CODE = 104;
    private View googlePayButton;
    private Button payByCardButton;

    private final String TAG = "MainActivity";
    private long amount = 0;

    private ProgressDialog loadingDialog;

    // Handle potential conflict from calling loadPaymentData.
    ActivityResultLauncher<IntentSenderRequest> resolvePaymentForResult = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                switch (result.getResultCode()) {
                    case Activity.RESULT_OK:
                        Intent resultData = result.getData();
                        if (resultData != null) {
                            PaymentData paymentData = PaymentData.getFromIntent(result.getData());
                            if (paymentData != null) {
                                handlePaymentSuccess(paymentData);
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        break;
                }
            });

    /**
     * Callback definition, where AuthorizePaymentResponse is handled for all payment types
     */
    ApolloCall.Callback<AuthorizePaymentMutation.Data> authorizePaymentCallback = new ApolloCall.Callback<AuthorizePaymentMutation.Data>() {

        @Override
        public void onResponse(@NonNull Response<AuthorizePaymentMutation.Data> response) {
            Log.i(TAG, "onResponse: " + response.toString());
            runOnUiThread(() -> {

                loadingDialog.dismiss();
                //Handle request error.
                if(response.hasErrors()){
                    Toast.makeText(getBaseContext(), response.getErrors().get(0).getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                //Show payment status
                AuthorizePaymentMutation.AuthorizePayment payment = Objects.requireNonNull(response.getData()).authorizePayment();
                Toast.makeText(getBaseContext(), payment.status().rawValue(), Toast.LENGTH_LONG).show();
            });
        }

        @Override
        public void onFailure(@NonNull ApolloException e) {
            Log.e(TAG, "onFailure: " + e );
            loadingDialog.dismiss();
            runOnUiThread(() -> Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show());
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checkout);
        setSupportActionBar(findViewById(R.id.toolbar));

        googlePayClient = PaymentsUtil.createPaymentsClient(this.getBaseContext());

        googlePayButton = findViewById(R.id.googlePayButton);
        googlePayButton.setOnClickListener(this::requestGooglePayment);
        setGooglePayAvailable();

        payByCardButton = findViewById(R.id.pay_button);
        payByCardButton.setOnClickListener(this::requestCardPayment);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Processing your payment");
    }

    /**
     * If isReadyToPay returned {@code true}, show the button and hide the "checking" text.
     * Otherwise, notify the user that Google Pay is not available. Please adjust to fit in with
     * your current user flow. You are not required to explicitly let the user know if isReadyToPay
     * returns {@code false}.
     */
    private void setGooglePayAvailable(){

        JSONObject isReadyToPayRequestJson = PaymentsUtil.getIsReadyToPayRequest();

        Log.e("isReadyToPay", String.valueOf(isReadyToPayRequestJson));

        IsReadyToPayRequest isReadytoPay = IsReadyToPayRequest.fromJson(String.valueOf(isReadyToPayRequestJson));

        Task<Boolean> paymentTask = googlePayClient.isReadyToPay(isReadytoPay);
        paymentTask.addOnCompleteListener(this, (completeTask) -> {
            if (completeTask.isSuccessful()) {
                googlePayButton.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, R.string.google_pay_status_unavailable, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestCardPayment(View v){

        //Build cardInput
        String name = ((EditText) findViewById(R.id.name_input)).getText().toString();
        String card = ((EditText) findViewById(R.id.card_input)).getText().toString().trim();
        String month = ((EditText) findViewById(R.id.month_input)).getText().toString();
        String year = ((EditText) findViewById(R.id.year_input)).getText().toString();
        String cvv = ((EditText) findViewById(R.id.cvv_input)).getText().toString();
        String inputAmount = ((EditText) findViewById(R.id.amount_input)).getText().toString();
        amount = Long.parseLong(inputAmount) * 100;

        Input<String> cvvInput = Input.optional(cvv);

        Input<PaymentCardInput> cardInput = Input.optional(PaymentCardInput.builder()
                                                .name(name).number(card)
                                                .expMonth(Integer.parseInt(month))
                                                .expYear(Integer.parseInt(year))
                                                .cvcInput(cvvInput).build());
        //Showing loading indicator to block UI interaction
        loadingDialog.show();

        /**
         * Tokenize the card
         */
        try{

            TokenInput tokenInput = TokenInput.builder().paymentMethod(PaymentMethodInput.builder()
                            .cardInput(cardInput).build()).build();
            GraphQLClient.getInstance().mutate(TokenisePaymentMethodMutation.builder()
                    .data(tokenInput)
                    .build()).enqueue(new ApolloCall.Callback<TokenisePaymentMethodMutation.Data>() {
                @Override
                public void onResponse(@NonNull Response<TokenisePaymentMethodMutation.Data> response) {
                    if(response.hasErrors()){
                        runOnUiThread(() ->{
                            loadingDialog.dismiss();
                            Toast.makeText(getBaseContext(), R.string.card_tokenization_error, Toast.LENGTH_LONG).show();
                        });
                    }


                    authorizePayment(response.getData().tokenisePaymentMethod().token(), amount);
                    Log.e(TAG, "tokenizePaymentMethod onResponse: " + response);
                }

                @Override
                public void onFailure(@NonNull ApolloException e) {
                    Log.e(TAG, "tokenizePaymentMethod onFailure: ", e);
                    runOnUiThread(() ->{
                        loadingDialog.dismiss();
                        Toast.makeText(getBaseContext(), R.string.card_tokenization_error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }catch (ApolloException e){
            Log.e(TAG, "tokenizePaymentMethod: ", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void requestGooglePayment(View v){

        amount = Long.parseLong(((EditText) findViewById(R.id.amount_input)).getText().toString());
        if(amount <=1 ){
            Toast.makeText(getBaseContext(), R.string.amount_error, Toast.LENGTH_LONG).show();
            return;
        }
        amount = amount * 100; //Long.parseLong(PaymentsUtil.centsToString(amount));


            final JSONObject paymentRequestJson = PaymentsUtil.getPaymentDataRequest(amount);
            if (paymentRequestJson == null) {
                Log.e("RequestPayment", "Can't fetch payment data request");
                return;
            }

            final PaymentDataRequest paymentRequest = PaymentDataRequest.fromJson(paymentRequestJson.toString());

            final Task<PaymentData> task = googlePayClient.loadPaymentData(paymentRequest);

            task.addOnCompleteListener(completedTask -> {
                //loadingDialog.show();
                if (completedTask.isSuccessful()) {
                    handlePaymentSuccess(completedTask.getResult());
                } else {
                    Exception exception = completedTask.getException();
                    if (exception instanceof ResolvableApiException) {
                        PendingIntent resolution = ((ResolvableApiException) exception).getResolution();
                        resolvePaymentForResult.launch(new IntentSenderRequest.Builder(resolution).build());

                    } else if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        handleError(apiException.getStatusCode(), apiException.getMessage());
                        Log.e(TAG, "addOnCompleteListener: " + apiException.getMessage(), apiException);

                    } else {
                           handleError(CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                                   " exception when trying to deliver the task result to an activity!");
                           Log.i(TAG, "addOnCompleteListener: " + exception.getMessage());
                    }
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode holds the value of any constant from CommonStatusCode or one of the
     *               WalletConstants.ERROR_CODE_* constants.
     * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/
     * WalletConstants#constant-summary">Wallet Constants Library</a>
     */
    private void handleError(int statusCode, @Nullable String message) {
        Log.e("loadPaymentData failed",
                String.format(Locale.getDefault(), "Error code: %d, Message: %s", statusCode, message));
    }


    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see <a href="https://developers.google.com/pay/api/android/reference/
     * object#PaymentData">PaymentData</a>
     */
    private void handlePaymentSuccess(PaymentData paymentData) {
        final String paymentInfo = paymentData.toJson();

        loadingDialog.show();

        Log.i(TAG, "handlePaymentSuccess: paymentInfo: " + paymentInfo);

        JSONObject tokenizationData = null;
        try {
            tokenizationData = new JSONObject(paymentInfo)
                    .getJSONObject("paymentMethodData")
                    .getJSONObject("tokenizationData");

            //send token to WT via authorizePaymentGraphQL API
            authorizePayment(tokenizationData, amount);
        } catch (JSONException e) {
            Log.e("handlePaymentSuccess", "Error: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Authorize a Payment using GooglePay token
     * @param googlePayToken
     */
    private void authorizePayment(JSONObject googlePayToken, long amount) throws JSONException {

        PaymentMethodDtoInput paymentMethodDtoInput =  PaymentMethodDtoInput.builder()
                .type(Constants.PAYMENT_METHOD_GOOGLE_PAY)
                .walletToken(googlePayToken.getString("token"))
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
             GraphQLClient.getInstance()
                            .mutate(AuthorizePaymentMutation.builder()
                                    .authorisePayment(input)
                                    .build())
                    .enqueue(authorizePaymentCallback);
        }catch (ApolloException e){
            Log.e(TAG, "authorizePayment: ", e);
        }

    }

    /**
     * Authorize Payment using a card token
     * @param cardToken
     * @param amount in microunit
     * TODO: Handle 3DS
     */
    private void authorizePayment(String cardToken, long amount){

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

        try{
            GraphQLClient.getInstance()
                    .mutate(AuthorizePaymentMutation.builder()
                            .authorisePayment(input)
                            .build())
                    .enqueue(authorizePaymentCallback);
        }catch (ApolloException e){
            Log.e(TAG, "authorizePayment: ", e);
        }
    }


}