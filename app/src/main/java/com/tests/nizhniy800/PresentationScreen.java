package com.tests.nizhniy800;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tests.nizhniy800.ui.login.LoginActivity;

public class PresentationScreen extends AppCompatActivity {

    SharedPreferences prefs = null;

    private ImageView imageViewLogo;
    private static int SPLASH_TIME_OUT = 2000;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        prefs = getSharedPreferences("com..alexiv.itmm.app_itmm", MODE_PRIVATE);
        setContentView(R.layout.activity_presentation_screen);
       // imageViewLogo = (ImageView) findViewById(R.id.logo);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, NotificationService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        am.cancel(pi);
        int minutes =1 ;
        if (minutes > 0) {
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + minutes*60*1000,
                    minutes*60*1000, pi);
        }

        LinearLayout layout = (LinearLayout) findViewById(R.id.layoutSplashScreen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Initialize Firebase Auth
                if (mFirebaseUser == null) {
                    startActivity(new Intent(PresentationScreen.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(PresentationScreen.this, "Hello, my God", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(PresentationScreen.this, MainActivity.class));
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void addImages(int image , ImageView destino){}

    @Override
    protected void onPause (){
        super.onPause();
        overridePendingTransition(R.anim.activity_close_bottom,0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_close_bottom,0);
    }

}

