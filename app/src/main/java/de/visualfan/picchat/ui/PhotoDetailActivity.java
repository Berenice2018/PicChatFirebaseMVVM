package de.visualfan.picchat.ui;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.visualfan.picchat.DataRepository;
import de.visualfan.picchat.R;
import de.visualfan.picchat.databinding.ActivityPhotoDetailBinding;

/**
 * Created by Berenice on 30.01.18.
 */

public class PhotoDetailActivity extends Activity{
    private final static String TAG = "PhotoDetailActivity";

    private ActivityPhotoDetailBinding mBinding;
    private String photoUrl = "";
    private String authorName;


    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

    View.OnClickListener btnDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Deleting");

            // delete message from realtime DB
            Query photoUrlQuery = ref.child("messages").orderByChild("photoUrl").equalTo(photoUrl);
            photoUrlQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                        appleSnapshot.getRef().removeValue();
                    }

                    // delete picture file from Firebase storage
                    StorageReference pictureRef =  FirebaseStorage.getInstance()
                            .getReferenceFromUrl(photoUrl);
                    pictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            Log.w(TAG, "onSuccess: deleted file");
                            Toast.makeText(mBinding.getRoot().getContext(),
                                    "Message deleted.",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // an error occurred!
                            Log.d(TAG, "onFailure: did not delete file");
                            Toast.makeText(mBinding.getRoot().getContext(),
                                    "Picture could not be deleted",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled. Could not delete", databaseError.toException());
                }
            });

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_photo_detail);

        Intent intent = getIntent();
        photoUrl = intent.getStringExtra("photoUrlExtra");
        authorName = intent.getStringExtra("userNameExtra");
        String description = intent.getStringExtra("descriptionExtra");

        Log.d("PhotoDetailActivity", "INTENT: " + authorName + description + photoUrl);
        if(photoUrl != null && !photoUrl.isEmpty())
            Glide.with(this)
                    .load(photoUrl)
                    .into(mBinding.imgvPhotoLarge);

        mBinding.tvAuthor.setText(authorName);
        mBinding.tvPhotoDescription.setText(description);

        // show delete button if the current user is the author of the message
        String user = DataRepository.getInstance().getUserName();
        if(user != null){
            if(user.equals(authorName)){
                mBinding.btnDelete.setVisibility(View.VISIBLE);
                mBinding.btnDelete.setOnClickListener(btnDeleteClickListener);
            }
        }

    }


}
