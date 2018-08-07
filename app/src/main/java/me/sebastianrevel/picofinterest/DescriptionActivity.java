package me.sebastianrevel.picofinterest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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
import com.parse.GetDataCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;

import me.sebastianrevel.picofinterest.Models.Pics;

import static android.graphics.Color.*;

public class DescriptionActivity extends AppCompatActivity {
    EditText descriptionEt;
    ImageView picIv;
    Button uploadBtn;
    ImageView ivrarrow, ivlarrow;
    Bitmap bm;
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
        /*
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
        */
        final ParseFile pf = newPic.getPic();
        /*
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
//      */
        /*
        Glide.with(this)
                //.load(pFile.getUrl())
                .load(pf.getUrl())
                .into(picIv);
        */
        loadImages(pf, picIv);

        //final ImageProcessor imageProcessor = new ImageProcessor();
        ivrarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bitmap bm1 = imageProcessor.doGreyScale(bm); // this applies the greyscale filter to the image, by taking in and returning a bitmap
                //picIv.setImageBitmap(imageProcessor.doGreyScale(bm));
                picIv.setImageBitmap(toGrayscale(bm));
            }
        });

        ivlarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picIv.setImageBitmap(createSepiaToningEffect(bm, 2, 12.0, 32.0, 69.0));
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picIv.setImageDrawable(null);
                String userDesc = String.valueOf(descriptionEt.getText());

                //newPic.setPic(current);
                newPic.setPic(pf);
                newPic.setDesc(userDesc);
                newPic.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("DESCACTIVITY", "added description");
                        Toast.makeText(getApplicationContext(), "Image added", Toast.LENGTH_SHORT);

                    }
                });
                finish();
            }
        });


    }

    private void loadImages(final ParseFile thumbnail, final ImageView img) {

        if (thumbnail != null) {
            thumbnail.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bm = bmp;
                        img.setImageBitmap(bmp);
                    } else {
                    }
                }
            });
        } else {
            //img.setImageResource(R.drawable.menu);
        }
    }// load image

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

        imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);

        byte[] imageByte = byteArrayOutputStream.toByteArray();

        ParseFile parseFile = new ParseFile("image_file.png", imageByte);

        return parseFile;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Bitmap createSepiaToningEffect(Bitmap originalImage, int depth, double red, double green,
                                          double blue) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, originalImage.getConfig());
        final double GS_RED = 0.3;
        final double GS_GREEN = 0.59;
        final double GS_BLUE = 0.11;
        int A, R, G, B;
        int pixel;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixel = originalImage.getPixel(x, y);
                A = alpha(pixel);
                R = red(pixel);
                G = green(pixel);
                B = blue(pixel);
                B = G = R = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                R += (depth * red);
                if (R > 255) {
                    R = 255;
                }
                G += (depth * green);
                if (G > 255) {
                    G = 255;
                }
                B += (depth * blue);
                if (B > 255) {
                    B = 255;
                }
                bmOut.setPixel(x, y, argb(A, R, G, B));
            }
        }
        return bmOut;
    }

}


