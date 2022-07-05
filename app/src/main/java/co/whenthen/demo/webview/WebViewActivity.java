package co.whenthen.demo.webview;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import co.whenthen.demo.Constants;
import co.whenthen.demo.R;
import co.whenthen.demo.databinding.ActivityMainBinding;

public class WebViewActivity extends AppCompatActivity implements WebViewEventHandler {

    private ActivityMainBinding binding;
    private TextView paymentText;
    private WebView webView;

    private final String TAG = "MainActivity";
    private final String clientToken = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        setSupportActionBar(findViewById(R.id.toolbar));

        webView = findViewById(R.id.webview);
        paymentText = findViewById(R.id.paymentText);

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

        String url = Constants.WEBVIEW_SDK_URL +
                 "?apiKey="+ Constants.CLIENT_TOKEN
                + "&amount=" +amount
                + "&currencyCode=" + Constants.CURRENCY_CODE
                + "&flowId=" + Constants.FLOW_ID
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
     * !! Method signature cannot be changed !!
     * @param eventType "error" | "paymentComplete"
     * NOTE: API errors will be sent as paymentComplete event with an {errors: [], data: object}
     * @param payload JSONString
     */
    @Override
    @JavascriptInterface
    public void handleEvent(String eventType, String payload) {
        Log.d(TAG, "handleCheckoutEvent= eventType: "+ eventType + " \n payload: "+ payload );
        runOnUiThread(() -> {
            paymentText.setText("eventType: "+ eventType + "\n\n payload: "+ payload);
        });
    }
}