package com.tests.nizhniy800;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ArticleActivity extends AppCompatActivity {

    public TextView title;
    private ImageView selected_image;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        // Initialize imageView
        selected_image = (ImageView) findViewById(R.id.selected_image);
        title = (TextView) findViewById(R.id.textView);
        title.setText("Loading...");
        Uri uri = (Uri)getIntent().getParcelableExtra("resID_uri");
        String message = null;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            selected_image.setImageBitmap(bitmap);

            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "image.jpg");
            try {
                FileOutputStream outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
                outStream.flush();
                outStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        message = SendFile();

    }

    private String SendFile()
    {
        String URL = "http://192.168.0.113:8080/server/server";
        try {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("task_file", file.getName(), RequestBody.create(MediaType.parse("image/jpg"), file))
                    .build();

            final Request request = new Request.Builder()
                    .url(URL)
                    .post(requestBody)
                    .build();

            final OkHttpClient client = new OkHttpClient();

            Thread thread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        Response response = client.newCall(request).execute();
                        String message = response.body().string();
                        Log.e("MES", message);

                        Bundle arguments = getIntent().getExtras();
                        title.setText(message);

                    } catch (Exception e) {
                        Log.e("TAG", e.getMessage());
                    }
                }
            });
            thread.start();


        } catch (Exception e) {
            Log.e("TAG", "Other Error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return "404";
    }


}