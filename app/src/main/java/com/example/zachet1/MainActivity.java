package com.example.zachet1;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    int id = 1;
    TextView picTitle,album,number;
    ImageView preview,image;
    String url = "https://jsonplaceholder.typicode.com/photos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        album = findViewById(R.id.album);
        number = findViewById(R.id.number);
        picTitle = findViewById(R.id.picTitle);
        preview = findViewById(R.id.preview);
        image = findViewById(R.id.image);
        try {
            open();
        } catch (Exception e){
            Toast.makeText(MainActivity.this,"Пожалуйста, сохраните данные",Toast.LENGTH_LONG).show();
        }

    }

    public void open(){
        open_text(album,"album.txt");
        open_text(number,"number.txt");
        open_text(picTitle,"picTitle.txt");
        open_pic(preview,"preview.png");
        open_pic(image,"image.png");
    }

    public void open_pic(ImageView i, String name){
        FileInputStream fin = null;
        try {
            fin = openFileInput(name);
            Drawable drawable = Drawable.createFromStream(fin,name);
            i.setImageDrawable(drawable);
        }
        catch(IOException ex) {
        }
        finally{
            try{
                if(fin!=null)
                    fin.close();
            }
            catch(IOException ex){
            }
        }
    }

    public void open_text(TextView t, String name){
        FileInputStream fin = null;
        try {
            fin = openFileInput(name);
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            String text = new String (bytes);
            t.setText(text);
        }
        catch(IOException ex) {
        }
        finally{
            try{
                if(fin!=null)
                    fin.close();
            }
            catch(IOException ex){
            }
        }
    }

    public void save(View view){
        save_text(album,"album.txt");
        save_text(number,"number.txt");
        save_text(picTitle,"picTitle.txt");
        save_pic(preview,"preview.png");
        save_pic(image,"image.png");
    }

    public void save_text(TextView t, String name){
        FileOutputStream fos = null;
        try {
            String text = t.getText().toString();
            fos = openFileOutput(name, MODE_PRIVATE);
            fos.write(text.getBytes());
            Toast.makeText(this, "Файл сохранен", Toast.LENGTH_SHORT).show();
        }
        catch(IOException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally{
            try{
                if(fos!=null)
                    fos.close();
            }
            catch(IOException ex){
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void save_pic(ImageView i,String name){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(name, MODE_PRIVATE);
            BitmapDrawable draw = (BitmapDrawable) i.getDrawable();
            Bitmap bitmap = draw.getBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG,100, fos);
            Toast.makeText(this, "Файл сохранен", Toast.LENGTH_SHORT).show();
        }
        catch(IOException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){}
        finally{
            try{
                if(fos!=null)
                    fos.close();
            }
            catch(IOException ex){
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetDataFromURL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONArray obj = new JSONArray(result);
                if (id == 0){
                    id = (int) (Math.random() * 5000);
                }
                id = id - 1;
                JSONObject element = obj.getJSONObject(id);
                System.out.println(element);
                album.setText(element.getString("albumId"));
                number.setText(element.getString("id"));
                picTitle.setText(element.getString("title"));
                try {
                    Glide.with(preview.getContext())
                            .load(element.getString("thumbnailUrl")+".png")
                            .into(preview);
                } catch (Exception e){
                    try {
                        Glide.with(preview.getContext())
                                .load(element.getString("thumbnailUrl")+".jpg")
                                .into(preview);
                    }catch (Exception ex){
                        Toast.makeText(MainActivity.this,"Bad image",Toast.LENGTH_LONG);
                    }
                }
                try {
                    Glide.with(image.getContext())
                            .load(element.getString("url")+".png")
                            .into(image);
                } catch (Exception e){
                    try {
                        Glide.with(image.getContext())
                                .load(element.getString("url")+".jpg")
                                .into(image);
                    }catch (Exception ex){
                        Toast.makeText(MainActivity.this,"Bad image",Toast.LENGTH_LONG);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setId1(View view){
        id = 1;
        new GetDataFromURL().execute(url);
    }
    public void setId2(View view){
        id = 2;
        new GetDataFromURL().execute(url);
    }
    public void setId3(View view){
        id = 3;
        new GetDataFromURL().execute(url);
    }
    public void setId4(View view){
        id = 4;
        new GetDataFromURL().execute(url);
    }
    public void setId5(View view){
        id = 5;
        new GetDataFromURL().execute(url);
    }
    public void setIdRand(View view){
        id = 0;
        new GetDataFromURL().execute(url);
    }
}