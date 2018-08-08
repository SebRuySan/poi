package me.sebastianrevel.picofinterest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
//import android.service.autofill.SaveCallback;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
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
    Bitmap current;
    Bitmap[] images;
    boolean rightclicked, leftclicked;
    //ParseFile current;
    private int i;
    Bitmap b1, b2, b3, b4;


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
        current = bm;
        rightclicked = false;
        leftclicked = false;
//        b3 = toGrayscale(bm);
       // b4 = changeBitmapColor(bm,  Color.CYAN, Color.BLACK); // aquamarine filter

        //final ImageProcessor imageProcessor = new ImageProcessor();
        ivrarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(leftclicked){
                    current = bm;
                    picIv.setImageBitmap(current);
                    leftclicked = false;
                }
                //Bitmap bm1 = imageProcessor.doGreyScale(bm); // this applies the greyscale filter to the image, by taking in and returning a bitmap
                //picIv.setImageBitmap(imageProcessor.doGreyScale(bm));
                rightclicked = true;
                //current = toGrayscale(bm);
                //picIv.setImageBitmap(current);
                picIv.setImageBitmap(b3);
            }
        });

        ivlarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rightclicked){
                    current = bm;
                    picIv.setImageBitmap(current);
                    rightclicked = false;
                }
        //        picIv.setImageBitmap(createSepiaToningEffect(bm, 2, 12.0, 32.0, 69.0));
                /*
                leftclicked = true;
                current = changeBitmapColor(bm,  0xFFFFFFFF, 0x0000FFFF ); // aqua filter
                current = changeBitmapColor(bm,  0xFFFFFFFF, 0x000000FF ); // blue filter
                picIv.setImageBitmap(current);
                */
                //current = bm;
                //current = changeBitmapColor(bm,  Color.CYAN, Color.BLACK); // aquamarine filter
                //current = applyShadingFilter(bm, Color.GREEN); // green filter
                //current = doInvert(bm);
                //picIv.setImageBitmap(current);
                picIv.setImageBitmap(b2);
            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picIv.setImageDrawable(null);
                String userDesc = String.valueOf(descriptionEt.getText());

                final ParseFile pfile = bittobytetoparse(current);
                pfile.saveInBackground();

                //newPic.setPic(current);
                newPic.setPic(pfile);
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

    private void loadImages(final ParseFile thumbnail, final ImageView img) {

        if (thumbnail != null) {
            thumbnail.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bm = bmp;
                        img.setImageBitmap(bmp);
                        b3 = toGrayscale(bm);
                        b4 = changeBitmapColor(bm,  Color.CYAN, Color.BLACK); // aquamarine filter
                        /*new Thread() {
                            public void run() {
                                b2 = doInvert(bm);
                            }
                        }.start();*/
                        b2 = doInvert(bm);
                    } else {
                    }
                }
            });
        } else {
            //img.setImageResource(R.drawable.menu);
        }
    }// load image

    /*
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
    } */

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

    public static Bitmap changeBitmapColor(Bitmap sourceBitmap, int color, int add)
    {
        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(),true);
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(color, add);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, paint);
        return resultBitmap;
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

    public static Bitmap applyShadingFilter(Bitmap source, int shadingColor) {
        // get image size
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int index = 0;
        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // AND
                pixels[index] &= shadingColor;
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

    public static Bitmap doInvert(Bitmap src) {
        // create new bitmap with the same settings as source bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // color info
        int A, R, G, B;
        int pixelColor;
        // image size
        int height = src.getHeight();
        int width = src.getWidth();

        // scan through every pixel
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                // get one pixel
                pixelColor = src.getPixel(x, y);
                // saving alpha channel
                A = Color.alpha(pixelColor);
                // inverting byte for each R/G/B channel
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);
                // set newly-inverted pixel to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final bitmap
        return bmOut;
    }

    // this function will hopefully replace the function above
    public ParseFile bittobytetoparse(Bitmap imageBitmap) {

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, blob);
        byte[] bitmapdata = blob.toByteArray();
        final ParseFile imageFile = new ParseFile("image.png", bitmapdata);
        return imageFile;

    }

}


