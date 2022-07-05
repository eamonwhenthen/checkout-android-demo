package co.whenthen.demo.custom;


import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.os.Bundle;

import co.whenthen.demo.AuthorizePaymentMutation;
import co.whenthen.demo.R;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import co.whenthen.demo.type.PaymentCardInput;
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

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private PaymentsClient googlePayClient;
    private View googlePayButton;
    private ProgressDialog loadingDialog;
    private EditText amountInput;

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
                                handleApprovedGooglePay(paymentData);
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
            //Handle request error.
            if(response.hasErrors()){
                showResult(response.getErrors().get(0).getMessage());
                return;
            }
            //Handle and show payment result
            AuthorizePaymentMutation.AuthorizePayment payment = Objects.requireNonNull(response.getData()).authorizePayment();
            showResult(payment.status().rawValue());
        }
        @Override
        public void onFailure(@NonNull ApolloException e) {
            Log.e(TAG, "onFailure: " + e );
            showResult(e.getMessage());
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_checkout);
        setSupportActionBar(findViewById(R.id.toolbar));

        googlePayClient = PaymentsUtil.createPaymentsClient(this.getBaseContext());

        amountInput = findViewById(R.id.amount_input);

        googlePayButton = findViewById(R.id.googlePayButton);
        googlePayButton.setOnClickListener(this::requestGooglePayment);

        Button payByCardButton = findViewById(R.id.pay_button);
        payByCardButton.setOnClickListener(this::requestCardPayment);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Processing your payment");

        setGooglePayAvailable();
    }

    /**
     * Use runOnUiThread to show response
     */
    public void showResult(String message){
        runOnUiThread(() -> {
            loadingDialog.dismiss();
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    /**
     * If isReadyToPay returned {@code true}, show the button and hide the "checking" text.
     * Otherwise, notify the user that Google Pay is not available.
     */
    private void setGooglePayAvailable(){
        JSONObject isReadyToPayRequestJson = PaymentsUtil.getIsReadyToPayRequest();
        IsReadyToPayRequest isReadyToPay = IsReadyToPayRequest.fromJson(String.valueOf(isReadyToPayRequestJson));
        Task<Boolean> paymentTask = googlePayClient.isReadyToPay(isReadyToPay);
        paymentTask.addOnCompleteListener(completeTask-> {
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

        Input<String> cvvInput = Input.optional(cvv);

        Input<PaymentCardInput> cardInput = Input.optional(PaymentCardInput.builder()
                                                .name(name).number(card)
                                                .expMonth(Integer.parseInt(month))
                                                .expYear(Integer.parseInt(year))
                                                .cvcInput(cvvInput).build());
        //Showing loading indicator to block UI interaction
        loadingDialog.show();
        GraphQLClient.getInstance(this).authorizePayment(authorizePaymentCallback, cardInput, getAmount());
    }

    private void requestGooglePayment(View v){

            final JSONObject paymentRequestJson = PaymentsUtil.getPaymentDataRequest(getAmount());
            if (paymentRequestJson == null) {
                Log.e("RequestPayment", "Can't fetch payment data request");
                return;
            }

            final PaymentDataRequest paymentRequest = PaymentDataRequest.fromJson(paymentRequestJson.toString());
            final Task<PaymentData> task = googlePayClient.loadPaymentData(paymentRequest);

            task.addOnCompleteListener(completedTask -> {
                if (completedTask.isSuccessful()) {
                    handleApprovedGooglePay(completedTask.getResult());
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
    private void handleApprovedGooglePay(PaymentData paymentData) {
        final String paymentInfo = paymentData.toJson();

        loadingDialog.show();
        Log.i(TAG, "handlePaymentSuccess: paymentInfo: " + paymentInfo);
        JSONObject tokenizationData = null;
        try {
            String token = new JSONObject(paymentInfo)
                    .getJSONObject("paymentMethodData")
                    .getJSONObject("tokenizationData")
                    .get("token").toString();

            //Use GooglePay token to authorize a payment.
            GraphQLClient.getInstance(this).authorizePayment(authorizePaymentCallback, token, getAmount());
        } catch (JSONException e) {
            Log.e("handlePaymentSuccess", "Error: " + e);
            e.printStackTrace();
        }
    }

    /**
     * Get amount in minor-unit (cents).
     * @return long
     */
    private long getAmount(){
        return Long.parseLong(amountInput.getText().toString()) * 100;
    }


}