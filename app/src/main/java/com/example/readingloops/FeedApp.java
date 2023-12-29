package com.example.readingloops;

public class FeedApp {

   // private int profileIcon;
    private String profileIcon;
    private String postImage;

    private String title;
    private String message;

    public FeedApp(String profileIcon, String postImage, String title, String message) {
        this.profileIcon = profileIcon;
        this.postImage = postImage;
        this.title = title;
        this.message = message;
    }

    public String getProfileIcon() {
        return profileIcon;
    }

    public void setProfileIcon(String profileIcon) {
        this.profileIcon = profileIcon;
    }



    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}