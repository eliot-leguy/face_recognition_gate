package com.example.gateopener;

import android.util.Log;

public class Users {

    private String userName;
    private String pathImage;

    public Users(String userPath){
        this.pathImage = userPath;
        this.userName = userPath.substring(userPath.lastIndexOf("/")+1, userPath.lastIndexOf("-"));
        Log.d("UsersName-",userName);
        Log.d("UsersPath-",pathImage);
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName){
        this.userName = userName.substring(userName.lastIndexOf("/")+1, userName.lastIndexOf("-"));
    }

    public String getImagePath() {
        return pathImage;
    }

    public void setImagePath(String userPath) {
        this.pathImage = userPath;
    }
}
