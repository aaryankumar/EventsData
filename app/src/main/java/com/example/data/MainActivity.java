package com.example.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.net.URI;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.widget.Toast.*;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {



    EditText dheading,ddesc,dvenue,dlink;
    TextView ddate;
    Button btn;
    CircleImageView dimg;
    DatabaseReference data;

    Model model;
    long maxid=3;
    int k=0;

    private static final int IMAGE_PICK_CODE=1000;
    private static final int PERMISSION_CODE=1001;
    private StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        dheading=findViewById(R.id.dhead);
        ddesc=findViewById(R.id.ddetail);
        ddate=findViewById(R.id.ddate);
        dvenue=findViewById(R.id.dvenue);
        dlink=findViewById(R.id.dlink);
        dimg=findViewById(R.id.image);
        btn=findViewById(R.id.btn);
        model=new Model();
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
//        Calendar c=Calendar.getInstance();
//        String currentDateString;



        ddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  DialogFragment datePicker=new
                showDatePickerDialog();
            }
        });





        data=FirebaseDatabase.getInstance().getReference();

        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    maxid=(dataSnapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





        dimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


           Intent galleryIntent=new Intent();
          galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,IMAGE_PICK_CODE);




            }
        });










        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                model.setHeading(dheading.getText().toString().trim());
                model.setDesc(ddesc.getText().toString().trim());
                model.setDate(ddate.getText().toString().trim());
                model.setVenue(dvenue.getText().toString().trim());
                model.setLink(dlink.getText().toString().trim());

                data.child(String.valueOf(max())).setValue(model);
                k=0;
                //data.push().setValue(model);

            }
        });



    }

    public long max(){

        if(k==0){
            maxid=maxid+1;
            k=1;
        }
        return maxid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==IMAGE_PICK_CODE && resultCode==RESULT_OK && data.getData()!=null){

            Uri ImageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

           // Picasso.get().load(ImageUri).into(dimg);
        }

        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){

                Uri resultUri=result.getUri();

                final  StorageReference filePath= userProfileImageRef.child(String.valueOf(maxid+1)+".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                Uri downloadurl=uri;
                                String imageurl=downloadurl.toString();
                                model.setImage(imageurl);
                                final DatabaseReference data=FirebaseDatabase.getInstance().getReference();
                                data.child(String.valueOf(max())).setValue(model)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
//
                                                            DatabaseReference data1=FirebaseDatabase.getInstance().getReference();
                                                            data1.child(String.valueOf(maxid)).addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                                                    String retrieveImage=dataSnapshot.child("image").getValue().toString();
                                                                    Picasso.get().load(retrieveImage).into(dimg);

                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                            Toast.makeText(MainActivity.this,"uploaded", LENGTH_LONG).show();

                                                        }
                                                        else {

                                                            String message=task.getException().toString();
                                                            Toast.makeText(MainActivity.this,"error:"+message, LENGTH_LONG).show();
                                                        }

                                            }
                                        });

                            }
                        });
                    }
                });
//
            }
        }
    }


    private void showDatePickerDialog(){

        DatePickerDialog datePickerDialog=new DatePickerDialog(
                this,this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }





    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        String selected=dayOfMonth+"/"+month+"/"+year;
        ddate.setText(selected);

    }
}
