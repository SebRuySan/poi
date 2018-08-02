package me.sebastianrevel.picofinterest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class DescriptionAcitivity extends AppCompatActivity{
    EditText descriptionEt;
    ImageView picIv;
    Button uploadBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_description);

        descriptionEt = findViewById(R.id.description);

        picIv = findViewById(R.id.pic_preview);

        uploadBtn = findViewById(R.id.upload_post_btn);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

}
