package com.example.gateopener;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Activity_viewUsers extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);
        Button button_save = (Button) this.findViewById(R.id.button_save);

        List<Users> image_details = getListData();
        final ListView listView = (ListView) findViewById(R.id.listView_Users);
        CustomListAdapter adapter= new CustomListAdapter(this, image_details);
        listView.setAdapter(adapter);

        //When ths user clicks on the ListItem
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?>a, View view, int position, long id){
                Object obj = listView.getItemAtPosition(position);
                Users user = (Users) obj;

                deleteUser(user.getImagePath(), image_details, adapter, position);
            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private List<Users> getListData() {
        List<Users> list = new ArrayList<Users>();
        File[] images = getImages();
        String path = getApplicationInfo().dataDir + "/app_imageDir/";

        if(images != null) {
            for(int i=0;i < images.length;i++) {
                String name = images[i].getName();
                list.add(new Users(path + name));
            }
        } else {
            Toast.makeText(Activity_viewUsers.this,"No",Toast.LENGTH_LONG).show();
        }


        return list;
    }

    private File[] getImages() {
        String path = getApplicationInfo().dataDir + "/app_imageDir/";
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files.length==0){
            Toast.makeText(Activity_viewUsers.this,R.string.noUsers,Toast.LENGTH_LONG).show();
            Activity_viewUsers.this.finish();
        }
        return files;
    }

    private void deleteUser(String path, List<Users> image_details, CustomListAdapter adapter ,int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_viewUsers.this);

        builder.setMessage(R.string.dialogMessage1);

        builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                File fdelete = new File(path);
                if(fdelete.exists()){
                    if (fdelete.delete()){
                        Toast.makeText(Activity_viewUsers.this,R.string.userDeleted,Toast.LENGTH_SHORT).show();

                        image_details.remove(position);
                        adapter.notifyDataSetChanged();
                        //Activity_viewUsers.this.finish();
                    } else {
                        Toast.makeText(Activity_viewUsers.this,R.string.userNotDeleted,Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}