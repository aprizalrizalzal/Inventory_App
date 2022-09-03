package id.sch.smkn1batukliang.inventory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import id.sch.smkn1batukliang.inventory.databinding.ActivitySplahScreenBinding;


@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplahScreenBinding binding = ActivitySplahScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Thread thread = new Thread(() -> {
            long timeSplash = 1_000;
            try {
                Thread.sleep(timeSplash);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
        thread.start();
    }
}