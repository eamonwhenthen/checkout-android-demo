package co.whenthen.demo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import co.whenthen.demo.natived.NativeFormActivity;
import co.whenthen.demo.webview.WebViewActivity;


public class MainActivity extends AppCompatActivity {

    private final Context context = MainActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        findViewById(R.id.btn_webview).setOnClickListener(v -> {
            startActivity(new Intent(context, WebViewActivity.class));
        });

        findViewById(R.id.btn_native_form).setOnClickListener(v -> {
            startActivity(new Intent(context, NativeFormActivity.class));
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}