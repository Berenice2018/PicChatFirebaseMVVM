package de.visualfan.picchat.model;

/**
 * Created by administrator on 23.01.18.
 */

public interface Message {

    public String getDescription();

    public void setDescription(String text);

    public String getUserName();

    public void setUserName(String name);

    public String getPhotoUrl();

    public void setPhotoUrl(String photoUrl);
}
