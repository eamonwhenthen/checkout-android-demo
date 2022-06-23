package co.whenthen.demo.natived;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.os.Bundle;
import co.whenthen.demo.R;
import android.webkit.WebView;
import android.widget.TextView;
import co.whenthen.demo.databinding.ActivityMainBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;

import org.json.JSONObject;

import java.util.Optional;

public class NativeFormActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private TextView paymentText;
    private WebView webView;
    private PaymentsClient googlePayClient;
    private final int LOAD_PAYMENT_DATA_REQUEST_CODE = 102;

    private final String CURRENCY = "EUR";
    private final String LANG = "en";
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_native);

        setSupportActionBar(findViewById(R.id.toolbar));

        googlePayClient = PaymentsUtil.createPaymentsClient(this);

        IsReadyToPayRequest isReadyToPayRequest = PaymentsUtil.getIsReadyToPayRequest();

        Log.e("isReadyToPay", isReadyToPayRequest.toJson());


        Task<Boolean> paymentTask = googlePayClient.isReadyToPay(isReadyToPayRequest);
        paymentTask.addOnCompleteListener(this, (completeTask) -> {
            showGooglePayButton(completeTask.isSuccessful());
            Log.e("isReadyToPay", "completeTask: " + completeTask.isSuccessful());
        });

       try{
           findViewById(R.id.gogolepay_button).setOnClickListener(v -> {
               final Optional<JSONObject> paymentRequestJson = PaymentsUtil.getPaymentDataRequest(12345);
               if (paymentRequestJson == null) {
                   Log.e("RequestPayment", "Can't fetch payment data request");
                   return;
               }
               final PaymentDataRequest paymentRequest = PaymentDataRequest.fromJson(paymentRequestJson.toString());

               if (paymentRequest != null) {
                   AutoResolveHelper.resolveTask(
                           googlePayClient.loadPaymentData(paymentRequest),
                           this,
                           LOAD_PAYMENT_DATA_REQUEST_CODE
                   );
               }

           });
       }catch (Exception e){
           Log.e(TAG, "onCreate: {}",  e);
           throw new RuntimeException(e.getMessage());
       }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: "+ data);
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {

                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        //handlePaymentSuccess(paymentData);
                        Log.i(TAG, paymentData.toJson().toString());

                        break;

                    case Activity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        //handleError(status.getStatusCode());
                        Log.i(TAG, status.getStatusMessage());
                        break;
                }

                // Re-enables the Google Pay payment button.
                //googlePayButton.setClickable(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showGooglePayButton(boolean isReadyToPay){
        if(isReadyToPay){

        }else{

        }
    }



}