package com.basselm_lailam_mohammedb.secondchance;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class CreateListingActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {


    Button btn_create, btn_gallery, btn_camera;
    TextInputEditText et_name, et_desc, et_phone, et_price;
    Uri imgUri;
    TextView tv_upload_photo;
    ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);

        // change the title of the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create Listing");
        }

        btn_create = findViewById(R.id.btn_create);
        btn_camera = findViewById(R.id.btn_camera);
        btn_gallery = findViewById(R.id.btn_gallery);
        et_name = findViewById(R.id.et_name);
        et_desc = findViewById(R.id.et_desc);
        et_phone = findViewById(R.id.et_phone);
        et_price = findViewById(R.id.et_price);
        tv_upload_photo = findViewById(R.id.tv_upload_photo);

        btn_create.setOnClickListener(this);
        btn_camera.setOnClickListener(this);
        btn_gallery.setOnClickListener(this);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleCreateButton();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        et_name.addTextChangedListener(textWatcher);
        et_price.addTextChangedListener(textWatcher);
        et_phone.addTextChangedListener(textWatcher);
        et_desc.addTextChangedListener(textWatcher);

        et_name.setOnFocusChangeListener(this);
        et_price.setOnFocusChangeListener(this);
        et_phone.setOnFocusChangeListener(this);
        et_desc.setOnFocusChangeListener(this);

        handleCreateButton();
        setupGalleryLauncher();
    }

    public void setupGalleryLauncher() {
        galleryLauncher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        setImgUri(uri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
    }

    public void launchGallery() {
        galleryLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void setImgUri(Uri uri) {
        imgUri = uri;
        photoUploadedUIFeedback();
    }

    public void createListing() {
        String name = et_name.getText().toString().trim();
        String desc = et_desc.getText().toString().trim();
        String phone = et_phone.getText().toString().trim();
        double price = Double.parseDouble(et_price.getText().toString());

        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("desc", desc);
        item.put("phone", phone);
        item.put("price", price);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items")
                .add(item)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String id = documentReference.getId();
                        if(imgUri != null) {

                            // upload image to firebase storage
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference().child("images/" + id);
                            storageRef.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Intent intent = new Intent(CreateListingActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }

                    }
                });
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_create) {
            createListing();
        }

        if (view.getId() == R.id.btn_camera) {

        }

        if (view.getId() == R.id.btn_gallery) {
            launchGallery();
        }
    }

    public void handleCreateButton() {
        String text1 = et_name.getText().toString().trim();
        String text2 = et_phone.getText().toString().trim();
        String text3 = et_desc.getText().toString().trim();
        String text4 = et_price.getText().toString().trim();


        boolean enabled = !text1.isEmpty() && !text2.isEmpty() && !text3.isEmpty() && !text4.isEmpty();
        btn_create.setEnabled(enabled);

        if(enabled) {
            btn_create.getBackground().setColorFilter(null);
        }
        else {
            btn_create.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {

        TextInputEditText et = (TextInputEditText) view;

        if (!hasFocus && et.getText().toString().isEmpty()) {
            et.setError("Field cannot be empty");
        }
    }

    public void photoUploadedUIFeedback() {
        tv_upload_photo.setTextColor(getResources().getColor(R.color.green));
        tv_upload_photo.setText("Photo uploaded!");
        btn_gallery.setVisibility(View.INVISIBLE);
        btn_camera.setVisibility(View.INVISIBLE);
    }
}















