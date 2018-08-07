package me.sebastianrevel.picofinterest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
//import android.service.autofill.SaveCallback;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mukesh.image_processing.ImageProcessor;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;

import me.sebastianrevel.picofinterest.Models.Pics;

public class DescriptionActivity extends AppCompatActivity {
    EditText descriptionEt;
    ImageView picIv;
    Button uploadBtn;
    ImageView ivrarrow, ivlarrow;
    Bitmap[] images;
    ParseFile current;
    private int i;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_description);

        descriptionEt = findViewById(R.id.description);

        picIv = findViewById(R.id.pic_preview);

        ivlarrow = findViewById(R.id.ivlarrow);
        ivrarrow = findViewById(R.id.ivrarrow);


        //String filePath = getIntent().getStringExtra("pictures");

        Intent intent = getIntent();
        //String filepath = new String(intent.getStringExtra("filepath"));
        //Log.e("DESC", filepath);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 8; // down sizing image as it throws OutOfMemory Exception for larger images
//        // filepath = filepath.replace("file://", ""); // remove to avoid BitmapFactory.decodeFile return null
//        File imgFile = new File(filepath);
//        if (imgFile.exists()) {
//            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
//            picIv.setImageBitmap(bitmap);

        uploadBtn = findViewById(R.id.upload_post_btn);
        final Pics newPic = (Pics) intent.getExtras().get("pic");
        //Bitmap bm = (Bitmap) intent.getExtras().get("bitmap"); // get bitmap passed in intent
        Bitmap bm = MainActivity.bitm;
        // now make parsefile using bitmap but first apply filter (just for test)
        ImageProcessor imageProcessor = new ImageProcessor();

        // want to make an array with the original image and two filtered versions of the image
        Bitmap bm1 = imageProcessor.doGreyScale(bm); // this applies the greyscale filter to the image, by taking in and returning a bitmap
        Bitmap bm2 = imageProcessor.createSepiaToningEffect(bm, 2, 12.0, 32.0, 69.0); // this applies a filter to make the picture "blue"
        images = new Bitmap[3];
        images[0] = bm;
        images[1] = bm1;
        images[2] = bm2;

        //bm = imageProcessor.doGreyScale(bm); // this applies the greyscale filter to the image, by taking in and returning a bitmap
        //bm = imageProcessor.createSepiaToningEffect(bm, 2, 12.0, 32.0, 69.0); // this applies a filter to make the picture "blue"

        final ParseFile pile = conversionBitmapParseFile(bm); // this is the normal bitmap parsefile
        i = 0;
        //ParseFile pFile = newPic.getPic();
        //ParseFile pf = newPic.getPic();
        pile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(null == e){
                    Glide.with(DescriptionActivity.this)
                            //.load(pFile.getUrl())
                            .load(pile.getUrl())
                            .into(picIv);
                } else {

                    e.printStackTrace();

                }
            }
        });
        current = pile;

        ivlarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(true);
            }
        });

        ivrarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter(false);
            }
        });
//
//        Glide.with(this)
//                //.load(pFile.getUrl())
//                .load(pf.getUrl())
//                .into(picIv);


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picIv.setImageDrawable(null);
                String userDesc = String.valueOf(descriptionEt.getText());

                newPic.setPic(current);
                newPic.setDesc(userDesc);
                newPic.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("DESCACTIVITY", "added description");
                        Toast.makeText(getApplicationContext(), "Image added", Toast.LENGTH_SHORT);

                        try {
                            MainActivity.setScore();
                        } catch (ParseException exception) {
                            exception.printStackTrace();
                        }
                    }
                });
                finish();
            }
        });


    }

    public void filter(boolean left){
        if(left){ // if left arrow clicked
            i--;
            if(i < 0)
                i = 2;
        }
        else{ // if left arrow clicked
            i++;
            if(i > 2)
                i = 0;
        }

        final ParseFile pf = conversionBitmapParseFile(images[i]); // this is the current bitmap
        //ParseFile pFile = newPic.getPic();
        //ParseFile pf = newPic.getPic();
        pf.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(null == e){
                    Glide.with(DescriptionActivity.this)
                            .load(pf.getUrl())
                            .into(picIv);
                } else {

                    e.printStackTrace();

                }
            }
        });
        current = pf;
    }

    public ParseFile conversionBitmapParseFile(Bitmap imageBitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        byte[] imageByte = byteArrayOutputStream.toByteArray();

        ParseFile parseFile = new ParseFile("image_file.png", imageByte);

        return parseFile;
    }

}


