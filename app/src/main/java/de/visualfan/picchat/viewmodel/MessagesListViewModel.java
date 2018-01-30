package de.visualfan.picchat.viewmodel;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import de.visualfan.picchat.data.FirebaseQueryLiveData;
import de.visualfan.picchat.data.PhotoMessageEntity;

/**
 * Created by Berenice on 20.01.18.
 */

public class MessagesListViewModel extends ViewModel {
    private static String TAG = "## ListViewModel";

    private static final DatabaseReference DB_REFERENCE =
            FirebaseDatabase.getInstance().getReference().child("messages");

    private List<PhotoMessageEntity> mList = new ArrayList<>();

    @NonNull
    public LiveData<List<PhotoMessageEntity>> getMessageListLiveData(){
        FirebaseQueryLiveData mLiveData = new FirebaseQueryLiveData(DB_REFERENCE); // is LiveData<DataSnapshot>
        // MediatorLiveData observes other LiveData objects and reacts on their emissions.
        LiveData<List<PhotoMessageEntity>> mMessageLiveData =
                Transformations.map(mLiveData, new Deserializer());
        Log.d(TAG, "getMessageListLiveData(), data deserialized");
        return mMessageLiveData;
    }

    private class Deserializer implements Function<DataSnapshot, List<PhotoMessageEntity>>{

        @Override
        public List<PhotoMessageEntity> apply(DataSnapshot dataSnapshot) {
            mList.clear();
            for(DataSnapshot snap : dataSnapshot.getChildren()){
                PhotoMessageEntity msg = snap.getValue(PhotoMessageEntity.class);
                mList.add(msg);
            }
            Log.d(TAG, "apply(), list size = " + mList.size());
            return mList;
        }
    }


    /**
     * Member vars for ComposerFragment
     */
    private String mPhotoUrlString = "";
    private final MutableLiveData<Boolean> pictureUploadIsSuccessful = new MutableLiveData<>();
    private final MutableLiveData<Boolean> messageUploadIsSuccessful = new MutableLiveData<>();


    /**
     * for ComposerFragment
     */

    public void setPhotoUrl(String photoUrl){
        mPhotoUrlString = photoUrl;
    }

    public String getPhotoUrl(){
        return mPhotoUrlString;
    }


    public MutableLiveData<Boolean> getPictureUploadIsSuccessful(){
        return pictureUploadIsSuccessful;
    }

    public MutableLiveData<Boolean> getMessageUploadIsSuccessful(){
        return messageUploadIsSuccessful;
    }



    public void createAndSendPhotoMessageToDataBase(String userName, String descriptionText, String photoUrl){
        PhotoMessageEntity picMessage = new PhotoMessageEntity(descriptionText, userName, photoUrl);

        // push the new message to Firebase
        Task uploadTask = FirebaseDatabase.getInstance()
                .getReference()
                .child("messages")
                .push()
                .setValue(picMessage);
        uploadTask.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                messageUploadIsSuccessful.setValue(true);
            }
        });
    }


    public void uploadPicture(Intent intentData){
        Uri selectedImgUri = intentData.getData();
        StorageReference photoReference = FirebaseStorage.getInstance()
                .getReference().child("chat_photos")
                .child(selectedImgUri.getLastPathSegment()
        );

        UploadTask uploadTask = photoReference.putFile(selectedImgUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mPhotoUrlString = String.valueOf(taskSnapshot.getDownloadUrl());
                pictureUploadIsSuccessful.setValue(true);
            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pictureUploadIsSuccessful.setValue(false);
            }
        });
    }

}


