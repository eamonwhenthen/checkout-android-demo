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
    private final String bearerToken = "sk_test_uOSmCSROUVr1nTjASfZf5U6POCCR2Toi";
    private final String clientToken = "sk_test_f39ZtDHRJ1Fj0gFTw2Ws8yHR5dxLDM5U";
    private final String flowID = "1acbb1d4-caa0-4c83-be94-61564f113fd7";
    private final String sdkUrl = "https://iridescent-zabaione-621b97.netlify.app";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        webView = (WebView) findViewById(R.id.webview);
        paymentResponse = (TextView) findViewById(R.id.paymentResponse);

        webView.setWebChromeClient(new WebChromeClient() {
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.clearCache(true);
        webView.getSettings().setDomStorageEnabled(true);

        //Generate random and amount
        String amount = String.valueOf(new Random().nextInt(1000));

        //add javascript callback function for webView to call.
        webView.addJavascriptInterface(this, "checkoutBridge");

        loadCheckoutSDK(webView, amount);
    }

    private void loadCheckoutSDK(WebView webView, String amount) {

        //DropInOptions checkout = new DropInOptions( clientToken, amount, BuildConfig. CURRENCY,);
        String url = sdkUrl +
                 "?apiKey="+clientToken
                + "&amount=" +amount
                + "&currencyCode=" +CURRENCY
                + "&flowId=" + flowID;
        webView.loadUrl(url);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    @JavascriptInterface
    public void handleEvent(String eventType, String payload) {
        Log.d(TAG, "handleCheckoutEvent= eventType: "+ eventType + " \n payload: "+ payload );
        //Toast.makeText(this, "eventType: "+ eventType + "\n\n payload: "+ payload, Toast.LENGTH_LONG ).show();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                paymentResponse.setText("eventType: "+ eventType + "\n\n payload: "+ payload);
            }
        });

    }
}