//Main
package com.example.butterflyz;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.butterflyz.R;
import com.example.butterflyz.SplashActivity;


public class MainActivity extends AppCompatActivity {
    private static final long REDIRECT_DELAY = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VideoView videoView = findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_screen));
        videoView.start();
        videoView.setOnPreparedListener(mp -> mp.setLooping(true));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                startActivity(intent);
                finish();
            }
        }, REDIRECT_DELAY);
    }
}
