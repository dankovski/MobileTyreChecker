package com.example.mobiletyrechecker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static Bitmap image_side=null;
    private static Bitmap image_front=null;
    private static Uri photoUri=null;
    private Button button_photo;
    private Button button_gallery;
    private TextView text_result_pressure;
    private TextView text_result_tread;
    private TextView text_result_damage;
    private TextView text_result_height;
    private TextView text_result_width;
    private TextView text_result_diameter;
    private TextView text_result_m_s;
    private TextView text_result_dot;
    private TextView text_result_load_index;
    private TextView text_result_date;
    private TextView text_result_speed_index;
    private TextView text_pressure;
    private TextView text_tread;
    private TextView text_damage;
    private TextView text_height;
    private TextView text_width;
    private TextView text_diameter;
    private TextView text_m_s;
    private TextView text_dot;
    private TextView text_load_index;
    private TextView text_date;
    private TextView text_speed_index;
    public ImageView image_view;
    static final int REQUEST_TAKING_PHOTO = 1;
    static final int REQUEST_OPENING_GALLERY= 2;
    private Spinner spinner;
    public enum Choices {SIDE, FRONT};
    Choices choice = Choices.FRONT;
    String[] names= {"Bok opony", "Przód opony"};
    Model model_pressure;
    Model model_tread;
    model_dmg model_damage;
    model_OCR model_ocr;
    String[] decode = {"0", "1", "2", "3", "4", "5", "6", "7",
        "8", "9", "T", "W", "H", "V" };

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_photo = findViewById(R.id.button_photo);
        button_gallery = findViewById(R.id.button_gallery);
        image_view = findViewById(R.id.image_view);
        text_result_damage = findViewById(R.id.text_result_damage);
        text_result_pressure=findViewById(R.id.text_result_pressure);
        text_result_tread = findViewById(R.id.text_result_tread);
        text_result_height=findViewById(R.id.text_result_height);
        text_result_width=findViewById(R.id.text_result_width);
        text_result_diameter=findViewById(R.id.text_result_diameter);
        text_result_m_s=findViewById(R.id.text_result_m_s);
        text_result_dot=findViewById(R.id.text_result_dot);
        text_result_load_index=findViewById(R.id.text_result_load_index);
        text_result_date=findViewById(R.id.text_result_date);
        text_result_speed_index=findViewById(R.id.text_result_speed_index);
        text_damage = findViewById(R.id.text_damage);
        text_pressure=findViewById(R.id.text_pressure);
        text_tread = findViewById(R.id.text_tread);
        text_height=findViewById(R.id.text_height);
        text_width=findViewById(R.id.text_width);
        text_diameter=findViewById(R.id.text_diameter);
        text_m_s=findViewById(R.id.text_m_s);
        text_dot=findViewById(R.id.text_dot);
        text_load_index=findViewById(R.id.text_load_index);
        text_date=findViewById(R.id.text_date);
        text_speed_index=findViewById(R.id.text_speed_index);

        text_result_tread.setVisibility(View.INVISIBLE);


        OpenCVLoader.initDebug();
        try {
            model_pressure =new Model(this, "cisnienie_model_przyc.tflite");
            model_tread =new Model(this, "model_przod.tflite");
            model_damage = new model_dmg(this,"damage01.tflite" );
            model_ocr = new model_OCR(this);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("bbb", "nie mozna wczytac modelu ");
        }

        spinner = (Spinner) findViewById(R.id.spinner);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int id, long position) {
                switch (id){
                    case 1:

                        choice = Choices.FRONT;
                        image_view.setImageBitmap(image_front);

                        text_result_damage.setVisibility(View.INVISIBLE);
                        text_result_pressure.setVisibility(View.INVISIBLE);
                        text_result_tread.setVisibility(View.VISIBLE);
                        text_result_height.setVisibility(View.INVISIBLE);
                        text_result_width.setVisibility(View.INVISIBLE);
                        text_result_diameter.setVisibility(View.INVISIBLE);
                        text_result_m_s.setVisibility(View.INVISIBLE);
                        text_result_dot.setVisibility(View.INVISIBLE);
                        text_result_load_index.setVisibility(View.INVISIBLE);
                        text_result_date.setVisibility(View.INVISIBLE);
                        text_result_speed_index.setVisibility(View.INVISIBLE);
                        text_damage.setVisibility(View.INVISIBLE);
                        text_pressure.setVisibility(View.INVISIBLE);
                        text_tread.setVisibility(View.VISIBLE);
                        text_height.setVisibility(View.INVISIBLE);
                        text_width.setVisibility(View.INVISIBLE);
                        text_diameter.setVisibility(View.INVISIBLE);
                        text_m_s.setVisibility(View.INVISIBLE);
                        text_dot.setVisibility(View.INVISIBLE);
                        text_load_index.setVisibility(View.INVISIBLE);
                        text_date.setVisibility(View.INVISIBLE);
                        text_speed_index.setVisibility(View.INVISIBLE);


                        break;
                    case 0:
                        choice = Choices.SIDE;
                        image_view.setImageBitmap(image_side);



                        text_result_damage.setVisibility(View.VISIBLE);
                        text_result_pressure.setVisibility(View.VISIBLE);
                        text_result_tread.setVisibility(View.INVISIBLE);
                        text_result_height.setVisibility(View.VISIBLE);
                        text_result_width.setVisibility(View.VISIBLE);
                        text_result_diameter.setVisibility(View.VISIBLE);
                        text_result_m_s.setVisibility(View.VISIBLE);
                        text_result_dot.setVisibility(View.VISIBLE);
                        text_result_load_index.setVisibility(View.VISIBLE);
                        text_result_date.setVisibility(View.VISIBLE);
                        text_result_speed_index.setVisibility(View.VISIBLE);
                        text_damage.setVisibility(View.VISIBLE);
                        text_pressure.setVisibility(View.VISIBLE);
                        text_tread.setVisibility(View.INVISIBLE);
                        text_height.setVisibility(View.VISIBLE);
                        text_width.setVisibility(View.VISIBLE);
                        text_diameter.setVisibility(View.VISIBLE);
                        text_m_s.setVisibility(View.VISIBLE);
                        text_dot.setVisibility(View.VISIBLE);
                        text_load_index.setVisibility(View.VISIBLE);
                        text_date.setVisibility(View.VISIBLE);
                        text_speed_index.setVisibility(View.VISIBLE);

                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });


        button_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) ||
                        (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                    String[] permission = new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
                   ActivityCompat.requestPermissions(MainActivity.this,permission, REQUEST_TAKING_PHOTO);
                }
                else {
                   openCamera();
                }
            }
        });

        button_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
         if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) ||
                        (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                    String[] permission = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
                    ActivityCompat.requestPermissions(MainActivity.this,permission, REQUEST_OPENING_GALLERY);
                }
                else {
                    openGallery();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_TAKING_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                }
                break;
            case REQUEST_OPENING_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }
                break;
        }
    }

    void openGallery(){

        Intent openGalleryIntent= new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGalleryIntent.setType("image/*");

        try {
            startActivityForResult(openGalleryIntent, REQUEST_OPENING_GALLERY);
        } catch (ActivityNotFoundException e) {
        }
    }

    void openCamera(){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File photo_file = new File(storageDir+"/photo_temp.jpg");
            Uri imgUri = FileProvider.getUriForFile(MainActivity.this,
                    "com.example.mobiletyrechecker.fileprovider", photo_file);
            photoUri=imgUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
            startActivityForResult(intent, REQUEST_TAKING_PHOTO);}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_TAKING_PHOTO && resultCode==RESULT_OK) {
            Uri dest_uri=Uri.fromFile(new File(getCacheDir(), "cropped"));
            Crop.of(photoUri, dest_uri).start(this);
        }
        else if (requestCode==REQUEST_OPENING_GALLERY && resultCode==RESULT_OK) {
            photoUri=data.getData();
            Uri dest_uri=Uri.fromFile(new File(getCacheDir(), "cropped"));
                Crop.of(photoUri, dest_uri).start(this);
        }
        else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            photoUri = Crop.getOutput(data);
            switch(choice) {
                case FRONT:
                    try {
                        image_front = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    image_view.setImageBitmap(image_front);
                    break;

                case SIDE:
                    try {
                        image_side = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    image_view.setImageBitmap(image_side);
                    break;
            }
            createResultDialog();
        }
    }

    public void createResultDialog(){
        AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(this);
        final AlertDialog dialog;
        final View poputView = getLayoutInflater().inflate(R.layout.popup, null);
        Button button = poputView.findViewById(R.id.button);
        dialogBuilder.setView(poputView);
        dialog=dialogBuilder.create();
        dialog.show();
final TextView text = poputView.findViewById(R.id.textView);
dialog.setCanceledOnTouchOutside(false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        text.setText("Trwa obliczanie...");

                switch(choice) {
                    case FRONT:
                        if(image_front!=null) {
                            float[] temp_float = model_tread.get_output(image_front);
                            Log.d("bbb", Arrays.toString(temp_float));
                            if (temp_float[0] > temp_float[1]) {
                                text_result_tread.setText("Dobra wysokość");
                                text_result_tread.setTextColor(Color.GREEN);
                            } else {
                                text_result_tread.setText("Zbyt niski");
                                text_result_tread.setTextColor(Color.RED);
                            }
                        }
                        break;

                    case SIDE:
                        if(image_side!=null) {
                            long start = System.currentTimeMillis();
                            List<Object[]> result = new ArrayList<>();
                            result=model_ocr.OCR(image_side);
                            image_view.setImageBitmap(image_side);
                            text_result_dot.setText("Nie wykryto");
                            text_result_m_s.setText("Nie wykryto");
                            text_result_date.setText("Nie wykryto");
                            text_result_width.setText("Nie wykryto");
                            text_result_height.setText("Nie wykryto");
                            text_result_diameter.setText("Nie wykryto");
                            text_result_load_index.setText("Nie wykryto");
                            text_result_speed_index.setText("Nie wykryto");
                            int height=0;
                            int width=0;

                            for(int i=0; i<result.size(); i++){
                                String str = "";
                                for(int k=1; k<result.get(i).length; k++){
                                    str=str+decode[(int)(result.get(i)[k])];
                                }
                                Log.d("bbb", result.get(i)[0]+" : "+str);
                                switch ((int)result.get(i)[0]){
                                    case 0:
                                        text_result_dot.setText("Wykryto");
                                        break;
                                    case 1:
                                        text_result_m_s.setText("Wykryto");
                                        break;
                                    case 4:
                                        if(str.length()==4){
                                            try{
                                                int week = Integer.parseInt(str.substring(0,2));
                                                int year = Integer.parseInt(str.substring(2,4));
                                                if(week>0 && week<=4){
                                                    text_result_date.setText(str+" (Styczeń, 20"+year+")");
                                                }
                                                else if(week>4 && week<=8){
                                                    text_result_date.setText(str+" (Luty, 20"+year+")");
                                                }
                                                else if(week>8 && week<=13){
                                                    text_result_date.setText(str+" (Marzec, 20"+year+")");
                                                }
                                                else if(week>13 && week<=17){
                                                    text_result_date.setText(str+" (Kwiecień, 20"+year+")");
                                                }
                                                else if(week>17 && week<=21){
                                                    text_result_date.setText(str+" (Maj, 20"+year+")");
                                                }
                                                else if(week>21 && week<=26){
                                                    text_result_date.setText(str+" (Czerwiec, 20"+year+")");
                                                }
                                                else if(week>26 && week<=30){
                                                    text_result_date.setText(str+" (Lipiec, 20"+year+")");
                                                }
                                                else if(week>30 && week<=34){
                                                    text_result_date.setText(str+" (Sierpień, 20"+year+")");
                                                }
                                                else if(week>34 && week<=39){
                                                    text_result_date.setText(str+" (Wrzesień, 20"+year+")");
                                                }
                                                else if(week>39 && week<=43){
                                                    text_result_date.setText(str+" (Październik, 20"+year+")");
                                                }
                                                else if(week>43 && week<=47){
                                                    text_result_date.setText(str+" (Listopad, 20"+year+")");
                                                }
                                                else if(week>47){
                                                    text_result_date.setText(str+" (Grudzień, 20"+year+")");
                                                }


                                            }
                                            catch(NumberFormatException e){
                                            }

                                        }
                                        break;
                                    case 5:
                                        if(str.length()==3){
                                            try{

                                                width=Integer.parseInt(str);
                                                text_result_width.setText(str+" "+"("+str+" mm)");
                                                if(height!=0){
                                                    text_result_height.setText(height+" ("+(width*height/100.0)+" mm)");
                                                }
                                            }
                                            catch(NumberFormatException e){
                                            }



                                            }
                                        break;
                                    case 6:
                                        if(str.length()==2){
                                            try{
                                                height=Integer.parseInt(str);
                                                text_result_height.setText(str);
                                                if(width!=0){
                                                    text_result_height.setText(str+" ("+(width*height/100.0)+" mm)");
                                                }

                                            }
                                            catch(NumberFormatException e){
                                            }
                                            }
                                        break;
                                    case 7:
                                        if(str.length()==2){

                                            try{
                                                int diameter = Integer.parseInt(str);
                                                if(diameter==22 || diameter==23 || diameter==24){
                                                    text_result_diameter.setText(str+" ("+str+" cale)");
                                                }
                                                else{
                                                    text_result_diameter.setText(str+" ("+str+" cali)");
                                                }
                                            }
                                            catch(NumberFormatException e){
                                            }



                                        }
                                        break;
                                    case 8:
                                        int load_index=0;
                                        String speed_index="";
                                        if(str.length()==3){
                                            try{
                                            load_index=Integer.parseInt(str.substring(0,2));
                                            text_result_load_index.setText(String.valueOf(load_index));}
                                            catch(NumberFormatException e){
                                            }
                                            speed_index=str.substring(2,3);

                                        }
                                        else if(str.length()==4){
                                            try{
                                                load_index=Integer.parseInt(str.substring(0,3));
                                                text_result_load_index.setText(String.valueOf(load_index));}
                                            catch(NumberFormatException e){
                                            }
                                            speed_index=str.substring(3,4);
                                        }
                                        if(!speed_index.isEmpty()){
                                        if(speed_index.charAt(0) == 'T'){
                                            text_result_speed_index.setText(speed_index+" (190 km/h)");
                                        }
                                        else if(speed_index.charAt(0) == 'H'){
                                            text_result_speed_index.setText(speed_index+" (210 km/h)");
                                        }
                                        else if(speed_index.charAt(0) == 'V'){
                                            text_result_speed_index.setText(speed_index+" (240 km/h)");
                                        }
                                        else if(speed_index.charAt(0) == 'W'){
                                            text_result_speed_index.setText(speed_index+" (270 km/h)");
                                        }}

                                            break;
                                }
                            }

                            float[] temp_float = model_pressure.get_output(image_side);
                            if (temp_float[0] < temp_float[1]) {
                                text_result_pressure.setText("Prawidłowy poziom");
                                text_result_pressure.setTextColor(Color.GREEN);
                            } else {
                                text_result_pressure.setText("Niskie ciśnienie");
                                text_result_pressure.setTextColor(Color.RED);
                            }

                            image_side= model_damage.get_output(image_side);

                            image_view.setImageBitmap(image_side);

                            switch(model_damage.damage_number){
                                case 0:
                                    text_result_damage.setText("Brak");
                                    text_result_damage.setTextColor(Color.GREEN);
                                    break;
                                case 1:
                                    text_result_damage.setText("1 uszkodzenie");
                                    text_result_damage.setTextColor(Color.RED);
                                    break;
                                case 2:
                                    text_result_damage.setText("2 uszkodzenia");
                                    text_result_damage.setTextColor(Color.RED);
                                    break;
                                case 3:
                                    text_result_damage.setText("3 uszkodzenia");
                                    text_result_damage.setTextColor(Color.RED);
                                    break;
                                case 4:
                                    text_result_damage.setText("4 uszkodzenia");
                                    text_result_damage.setTextColor(Color.RED);
                                    break;
                                case 5:
                                    text_result_damage.setText("5 uszkodzen");
                                    text_result_damage.setTextColor(Color.RED);
                                    break;
                                case 6:
                                    text_result_damage.setText("6 uszkodzen");
                                    text_result_damage.setTextColor(Color.RED);
                                    break;
                                case 7:
                                    text_result_damage.setText("7 uszkodzen");
                                    text_result_damage.setTextColor(Color.RED);
                                    break;
                            }
                        }
                        break;
                }
         dialog.dismiss();

            }
        });
    }

}