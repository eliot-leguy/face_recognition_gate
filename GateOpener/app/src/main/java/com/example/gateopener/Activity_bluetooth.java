package com.example.gateopener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Activity_bluetooth extends AppCompatActivity {

    static OutputStream outputStream = null;
    static InputStream inputStream = null;
    SendImageThread sendUsers = null;
    receiveImageThread receiveUsers = null;
    BluetoothDevice device = null;
    ProgressBar progressBar;
    TextView textView = null;
    BluetoothSocket socket = null;
    List<String> UserName = null;
    Handler mHandler;
    boolean usersDled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        textView = (TextView) this.findViewById(R.id.textView_synchBltUser);

        mHandler = new Handler();

        Intent intent = getIntent();
        device = intent.getExtras().getParcelable("device");

        UserName = new ArrayList<>();

        if(connect2()){ // If the connection happen, synchronize, else close the activity
            getUsersFromGateOpener();
            Log.d("oui", "allo");
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setContentView(R.layout.activity_bluetooth);

        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        textView = (TextView) this.findViewById(R.id.textView_synchBltUser);

        if (usersDled) {
            Log.d("oui", "Resume");
            sendUsersToGateOpener();
        }

        }

    @Override
    protected void onStop() {
        super.onStop();

        if(sendUsers != null){
            sendUsers.interrupt();
            Log.d("oui", "Interrupting sendUsers Thread");
        }
        if(receiveUsers != null) {
            receiveUsers.interrupt();
            Log.d("oui", "Interrupting receiveUsers Thread");

        }

    }

    protected void sendUsersToGateOpener() {
        DataInputStream inStream = new DataInputStream(inputStream);
        int bytes = 0;
        byte[] buffer = new byte[1024];
        String msg = "-";
        try {
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
            bytes = inStream.read(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }

        int i =0;
        while(buffer[i] != 0){
            i++;
        }
        byte[] bName = new byte[i];
        for(i=0; i< bName.length; i++){
            bName[i] = buffer[i];
        }

        String data = new String(bName);
        List<String> nameOfUsersToSend = new ArrayList<>();
        List<String> pathOfUsersToSend = new ArrayList<>();
        StringBuilder temp = new StringBuilder();
        for(i=0;i<data.length();i++){
            if (data.charAt(i) == '_') {
                nameOfUsersToSend.add(temp.toString());
                Log.d("oui", "User to Send : " + temp.toString());
                temp = new StringBuilder();
            } else {
                temp.append(data.charAt(i));
            }
        }

        if(nameOfUsersToSend.size() > 0) {

            File[] users = getLocalImages();
            for (File user : users) {
                String path = user.getAbsolutePath();
                String name = user.getName();
                name = name.substring(name.lastIndexOf("/") + 1, name.lastIndexOf("-"));

                for (int j = 0; j < nameOfUsersToSend.size(); j++) {
                    if (name.equals(nameOfUsersToSend.get(j))) {
                        pathOfUsersToSend.add(path);
                        break;
                    }
                }
            }

            sendUsers = new SendImageThread(nameOfUsersToSend, pathOfUsersToSend, progressBar);
            sendUsers.start();
        } else {
            close();
        }
    }


    protected void close() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.Synchronisation_Successful)
                .setMessage(R.string.Synchronisation_Successful_txt)
                .setPositiveButton(R.string.button_understand, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (inputStream != null) {
                            try {inputStream.close();} catch (Exception e) {}
                        }

                        if (outputStream != null) {
                            try {outputStream.close();} catch (Exception e) {}
                        }

                        if (socket != null) {
                            try {socket.close();} catch (Exception e) {}
                        }

                        finish();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (inputStream != null) {
                            try {inputStream.close();} catch (Exception e) {}
                        }

                        if (outputStream != null) {
                            try {outputStream.close();} catch (Exception e) {}
                        }

                        if (socket != null) {
                            try {socket.close();} catch (Exception e) {}
                        }

                        finish();
                    }
                })
                .create().show();
    }


    protected boolean connect2() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }

        boolean ok = true;
        UUID uuid = device.getUuids()[0].getUuid();
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,R.string.toast3,Toast.LENGTH_SHORT).show();
        }
        try {
            socket.connect();
            Log.d("oui", "connected1");
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

        } catch (IOException e){
            e.printStackTrace();
            try {
                Log.d("oui", "trying fallback");

                socket =(BluetoothSocket) device.getClass().getMethod("createRFcommSocket", new Class[] {int.class}).invoke(device,1);
                assert socket != null;
                socket.connect();
                Log.d("oui", "connected2");
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();

            } catch (IOException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ioException) {
                ioException.printStackTrace();
                Toast.makeText(this,R.string.toast3,Toast.LENGTH_SHORT).show();
                ok = false;
            }
        }
        return ok;
    }

    protected void getUsersFromGateOpener() {

        int nbUsersToDl = matchUsers(getLocalImages());
        Log.d("oui", String.valueOf(nbUsersToDl));

        receiveUsers = new receiveImageThread(inputStream, progressBar,nbUsersToDl);
        receiveUsers.start();
    }

    protected File[] getLocalImages(){
        File directory = new File(this.getApplicationInfo().dataDir +"/app_imageDir/");

        return directory.listFiles();
    }

    @NonNull
    private byte[] getImage(String path, String name){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b_image = baos.toByteArray();
        Log.d("oui", "Name = " + name); // A retirer
        Log.d("oui", "Size = " + b_image.length);

        return b_image;
    }

    private int getImageHeight(String path){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return bitmap.getHeight();
    }

    private int getImageWidth(String path){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return bitmap.getWidth();
    }

    public class SendImageThread extends Thread{

        String name;
        byte[] b_image;
        int imageHeight;
        int imageWidth;
        Context context = null;
        ProgressBar progressBar;
        List<String> nameOfUsersToSend;
        List<String> pathOfUsersToSend;


        SendImageThread(List<String> nameOfUsersToSend, List<String> pathOfUsersToSend, ProgressBar progressBar){
            this.nameOfUsersToSend = nameOfUsersToSend;
            this.pathOfUsersToSend = pathOfUsersToSend;
            this.progressBar = progressBar;
        }

        @Override
        public void run() {
            super.run();

            for (int i = 0; i < nameOfUsersToSend.size(); i++){
                this.b_image = getImage(pathOfUsersToSend.get(i), nameOfUsersToSend.get(i));
                this.imageHeight = getImageHeight(pathOfUsersToSend.get(i));
                this.imageWidth = getImageWidth(pathOfUsersToSend.get(i));
                this.name = nameOfUsersToSend.get(i);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(name);
                    }
                });

                if(Thread.interrupted()){
                    return;
                }

                try {
                    String size = "_" + this.b_image.length + "_" + name + "_" + this.imageHeight + "_" + this.imageWidth + "_";
                    outputStream.write(size.getBytes(StandardCharsets.UTF_8));
                    outputStream.write(b_image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DataInputStream inStream = new DataInputStream(inputStream);
                int percentage = 0;
                String percentageStr = "";
                int loop = 0;
                while(percentage < 100){ //read form the inputStream
                    loop += 1;

                    if(Thread.interrupted()){
                        return;
                    }

                    int bytes = 0;
                    byte[] buffer = new byte[1];

                    try {
                        bytes = inStream.read(buffer);
                        if(String.valueOf(buffer[0]).equals("95")){
                            percentage = Integer.parseInt(percentageStr);
                            percentageStr = "";
                        } else {
                            percentageStr += String.valueOf(Integer.parseInt(String.valueOf(buffer[0]))-48);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    float finalPercentage = percentage;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress((int) finalPercentage);
                        }
                    });

                    Log.d("oui", "Percentage :" + String.valueOf(percentage));
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    close();
                }
            });

        }
    }

    protected int matchUsers(File[] images){

        DataInputStream inStream = new DataInputStream(inputStream);
        int bytes = 0;
        byte[] buffer = new byte[1];
        int nbUsers = 0;

        if(images != null){
            StringBuilder msg = new StringBuilder();
            String name = null;
            for (File image : images) {
                name = image.getName();
                name = name.substring(name.lastIndexOf("/")+1, name.lastIndexOf("-"));
                msg.append("*").append(name);
            }
            msg.append("*");


            try {
                outputStream.write(msg.toString().getBytes(StandardCharsets.UTF_8));

                bytes = inStream.read(buffer);

                String nbUsersStr = String.valueOf(buffer[0]);
                nbUsers = Integer.parseInt(nbUsersStr) - 48;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return nbUsers;
    }

    public class receiveImageThread extends Thread{

        InputStream inputStream = null;
        ProgressBar progressBar;
        Boolean infoOk = false;
        int nbUsersToDl;


        receiveImageThread(InputStream inputStream, ProgressBar progressBar, int nbUsersToDl){
            this.inputStream = inputStream;
            this.progressBar = progressBar;
            this.nbUsersToDl = nbUsersToDl;
        }

        @Override
        public void run(){
            super.run();
            DataInputStream inStream = new DataInputStream(inputStream);
            String name = null;
            int size = 0;

            for(int i=0; i < nbUsersToDl; i++){
                infoOk = false;
                Log.d("oui", "Beginning the receiving");

                if(Thread.interrupted()){
                    return;
                }

                byte[] buffer = new byte[1024];
                int byteNo;
                try {
                    Log.d("oui", "Receive Name And Size");
                    while(!infoOk){
                        name = getUserName(inStream);
                        size = getSize(inStream);
                    }
                    String finalName = name;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(finalName);
                            UserName.add(finalName);
                        }
                    });
                    //Request the image transmission
                    String msg = String.valueOf('3');
                    outputStream.write(msg.getBytes(StandardCharsets.UTF_8));

                    buffer = new byte[size];
                    float percentage;
                    float percentageOld = 0;

                    byteNo = inStream.read(buffer);
                    if (byteNo != -1) {
                        int byteNo2 = byteNo;
                        int bufferSize = size;
                        while(byteNo2 != bufferSize){

                            if(Thread.interrupted()){
                                return;
                            }

                            bufferSize = bufferSize - byteNo2;
                            byteNo2 = inStream.read(buffer,byteNo,bufferSize);

                            percentage =  ((float)byteNo / (float)size)*100;
                            if(percentage != percentageOld){
                                percentage = Math.round(percentage);
                                float finalPercentage = percentage;
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress((int) finalPercentage);
                                    }
                                });

                                Log.d("oui", "Percentage " + percentage);

                                percentageOld = percentage;
                            }
                            if(byteNo2 == -1){
                                Log.d("oui", "error");
                                break;
                            }
                            byteNo = byteNo+byteNo2;
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("oui", "ended");
                ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                Bitmap bp=BitmapFactory.decodeStream(bis); //decode stream to a bitmap image

                try {
                    saveImage(createImageFile(name), bp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            usersDled = true;
            Intent viewUserIntent = new Intent(Activity_bluetooth.this, Activity_viewUsers.class);
            startActivity(viewUserIntent);
        }

        public String getUserName(@NonNull DataInputStream inStream){
            byte[] buffer = new byte[1024];
            int byteNo;
            String name = null;

            try {
                String msg = String.valueOf('1');
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                Log.d("oui", "Requesting name :");
                byteNo = inStream.read(buffer);
                // Get the length of the string stocked in the bytes buffer
                int i =0;
                while(buffer[i] != 0){
                    i++;
                }
                byte[] bName = new byte[i];
                for(i=0; i< bName.length; i++){
                    bName[i] = buffer[i];
                }

                name = new String(bName);
                Log.d("oui", "Name = " + name);
        } catch (IOException e) {
                e.printStackTrace();
            }
            return name;
        }

        public int getSize(@NonNull DataInputStream inStream){
            byte[] buffer = new byte[1024];
            int byteNo;
            int i = 0;

            try {
                String msg = String.valueOf('2');
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                Log.d("oui", "Requesting size :");
                byteNo = inStream.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringBuilder sizeStr = new StringBuilder();
            while(buffer[i] != 0){
                sizeStr.append(buffer[i] - 48); // -48 because that's what gives the number we want
                i++;
            }
            //String first = String.valueOf(sizeStr.charAt(0));
            if(sizeStr.length() == 0){ // Pourquoi un "-" ?
                infoOk = false;
                Log.d("oui","Pb size");
                return 0;
            }
            else {
                Log.d("oui", "Size = " + sizeStr);
                infoOk = true;
            }

            return Integer.parseInt(sizeStr.toString());
        }

        @NonNull
        private String createImageFile(String name) throws IOException {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // Path to /data/data/myapp/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File image = File.createTempFile(
                    name + "-",  /* prefix */
                    ".jpg",         /* suffix */
                    directory      /* directory */
            );
            return image.getAbsolutePath();
        }

        private void saveImage(String path, @NonNull Bitmap img) {
            OutputStream fOut;
            File file = new File(path);
            try {
                fOut = new FileOutputStream(file);
                img.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}