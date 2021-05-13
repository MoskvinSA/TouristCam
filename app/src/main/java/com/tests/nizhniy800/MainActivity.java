package com.tests.nizhniy800;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.soundcloud.android.crop.Crop;
import com.tests.nizhniy800.toolbar.AboutUs;
import com.tests.nizhniy800.toolbar.Dashboard;
import com.tests.nizhniy800.toolbar.Profile;
import com.tests.nizhniy800.toolbar.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import static java.util.Objects.*;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleMap map;
    DrawerLayout drawerLayout;
    private FusedLocationProviderClient mLocationClient;

    public static Button Camera;
    public static Button Zoom_in;
    public static Button Zoom_out;
    public static Button Location;

    // for permission requests
    public static final int REQUEST_PERMISSION = 300;

    // request code for permission requests to the os for image
    public static final int REQUEST_IMAGE = 100;

    private Uri imageUri;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        requireNonNull(mapFragment).getMapAsync(this);

        drawerLayout = findViewById(R.id.drawer_layout);

        Camera   = (Button) findViewById(R.id.camera);
        Zoom_in  = (Button) findViewById(R.id.zoom_in);
        Zoom_out = (Button) findViewById(R.id.zoom_out);
        Location = (Button) findViewById(R.id.location);


        // Разриешения на использование местоположения
        mLocationClient = new FusedLocationProviderClient(this);
        if (  ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrLoc();
            }
        });

        // request permission to write data (images) to the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        // request permission to read data (images) from the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }


        Camera.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                openCameraIntent();
            }
        });

        Zoom_in.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomIn();
                map.moveCamera(cameraUpdate);
                map.setMapType(map.MAP_TYPE_NORMAL);
            }
        });

        Zoom_out.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomOut();
                map.moveCamera(cameraUpdate);
                map.setMapType(map.MAP_TYPE_NORMAL);
            }
        });
    }

    // проверяет, предоставил ли пользователь все необходимые разрешения на чтение, запись и камеру. Если нет, сообщите об этом пользователю и закройте приложение.
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(),"This application needs read, write, and camera permissions to run. Application now closing.",Toast.LENGTH_LONG).show();
                System.exit(0);
            }
        }
    }

    // инициализация карты и включение показа пробок
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setTrafficEnabled(true);
    }

    // открывает камеру для пользователя
    private void openCameraIntent(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        // tell camera where to store the resulting picture
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // start camera, and wait for it to finish
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    // Получаем ответ от камеры и запускаем активити статьи
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("------------", imageUri.toString());
        Intent classifyIntent = new Intent(MainActivity.this, ArticleActivity.class);
        // put image data in extras to send
        classifyIntent.putExtra("resID_uri", imageUri);
        // send other required data
        startActivity(classifyIntent);

    }

    // получение геолокации пользователя
    @SuppressLint("MissingPermission")
    private void getCurrLoc() {
        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Location location = getLastKnownLocation();
                Marker marker = null;
                try {
                    gotoLocation(location.getLatitude(), location.getLongitude());
                    LatLng NizhnyNovgorod= new LatLng(location.getLatitude(), location.getLongitude());
                    map.addMarker(new MarkerOptions().position(NizhnyNovgorod).title("NizhnyNovgorod"));
                    map.moveCamera(CameraUpdateFactory.newLatLng(NizhnyNovgorod));
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    // получение последней геолокации пользователя
    private Location getLastKnownLocation() {
        Location l=null;
        LocationManager mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                l = mLocationManager.getLastKnownLocation(provider);
            }
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    // перемещение камеры на мето геолокации
    private void gotoLocation(double latitude, double longtude)
    {
        LatLng LatLng = new LatLng( latitude,  longtude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng, 16);
        map.moveCamera(cameraUpdate);
        map.setMapType(map.MAP_TYPE_NORMAL);
    }

    /* кнопки toolbar */

    public void ClickMenu(View view){
        openDrawer(drawerLayout);
        Camera.setEnabled(false);
        Location.setEnabled(false);
        Zoom_out.setEnabled(false);
        Zoom_in.setEnabled(false);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo(View view){
        closeDrawer(drawerLayout);

    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickHome(View view){
        recreate();
    }

    public void ClickDashboard(View view){
        redirectActivity(this, Dashboard.class);
    }

    public void ClickAboutUs(View view){
        redirectActivity(this, AboutUs.class);
    }

    public void ClickLogout(View view){
        logout(this);
    }

    public void ClickProfile(View view) { redirectActivity(this, Profile.class);}

    public void ClickSettings(View view) { redirectActivity(this, Settings.class);}

    public static void logout(final Activity activity) {
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finishAffinity();
                System.exit(0);
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void redirectActivity(Activity activity,Class aClass) {
        Intent intent = new Intent(activity, aClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {  }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
