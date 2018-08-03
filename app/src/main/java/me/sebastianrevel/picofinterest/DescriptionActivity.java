package me.sebastianrevel.picofinterest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.File;

import me.sebastianrevel.picofinterest.Models.Pics;

public class DescriptionActivity extends AppCompatActivity {
    EditText descriptionEt;
    ImageView picIv;
    Button uploadBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_description);

        descriptionEt = findViewById(R.id.description);

        picIv = findViewById(R.id.pic_preview);

        //String filePath = getIntent().getStringExtra("pictures");

        Intent intent = getIntent();
        String filepath = new String(intent.getStringExtra("filepath"));
        Log.e("DESC", filepath);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 8; // down sizing image as it throws OutOfMemory Exception for larger images
//        // filepath = filepath.replace("file://", ""); // remove to avoid BitmapFactory.decodeFile return null
//        File imgFile = new File(filepath);
//        if (imgFile.exists()) {
//            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
//            picIv.setImageBitmap(bitmap);

        uploadBtn = findViewById(R.id.upload_post_btn);
        final Pics newPic = (Pics) intent.getExtras().get("pic");

        ParseFile pFile = newPic.getPic();

        Glide.with(this)
                .load(pFile.getUrl())
                .into(picIv);


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picIv.setImageDrawable(null);
                String userDesc = String.valueOf(descriptionEt.getText());

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

}


