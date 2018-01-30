package de.visualfan.picchat.data;

import android.support.annotation.Nullable;

import de.visualfan.picchat.model.Message;

/**
 * Created by Berenice on 19.01.18.
 */

public class PhotoMessageEntity implements Message {

    private String description;
    private String userName;
    private String photoUrl;

    public PhotoMessageEntity(){};

    public PhotoMessageEntity(String description, String userName, @Nullable String photoUrl) {
        this.description = description;
        this.userName = userName;
        this.photoUrl = photoUrl;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String text) {
        this.description = text;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(String name) {
        this.userName = name;
    }

    @Override
    public String getPhotoUrl() {
        return photoUrl;
    }

    @Override
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
