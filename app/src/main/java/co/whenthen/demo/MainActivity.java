package co.whenthen.demo;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import co.whenthen.demo.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CheckoutBridgeHandler{

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private TextView paymentResponse;
    private WebView webView;

    private final String CURRENCY = "EUR";
    private final String LANG = "en";
    private final String TAG = "MainActivity";
    private final String clientToken = "sk_test_f39ZtDHRJ1Fj0gFTw2Ws8yHR5dxLDM5U";
    private final String flowID = "1acbb1d4-caa0-4c83-be94-61564f113fd7";
    private final String sdkUrl = "https://checkout-hosted.whenthen.com/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        webView = findViewById(R.id.webview);
        paymentResponse = findViewById(R.id.paymentResponse);

        webView.setWebChromeClient(new WebChromeClient() {
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.clearCache(true);
        webView.getSettings().setDomStorageEnabled(true);

        //Generate random amount
        String amount = String.valueOf(new Random().nextInt(1000));

        //add javascript callback function for the webView to call.
        webView.addJavascriptInterface(this, "checkoutBridge");

        loadCheckoutSDK(webView, amount);
    }

    private void loadCheckoutSDK(WebView webView, String amount) {

        //Define SDK properties here
        JSONObject theme = new JSONObject();
        JSONArray apms = new JSONArray();

        try {
            apms.put(new JSONObject().put("type", "klarna"));
            theme.put("colors", new JSONObject().put("border", "blue"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = sdkUrl +
                 "?apiKey="+clientToken
                + "&amount=" +amount
                + "&currencyCode=" +CURRENCY
                + "&flowId=" + flowID
                + "&alternativePaymentMethods=" + apms
                + "&theme=" + theme;

        webView.loadUrl(url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /**
     *
     * @param eventType "error" | "paymentComplete"
     * @param payload JSONString
     */
    @Override
    @JavascriptInterface
    public void handleEvent(String eventType, String payload) {
        Log.d(TAG, "handleCheckoutEvent= eventType: "+ eventType + " \n payload: "+ payload );
        runOnUiThread(() -> {
            paymentResponse.setText("eventType: "+ eventType + "\n\n payload: "+ payload);
        });
    }
}