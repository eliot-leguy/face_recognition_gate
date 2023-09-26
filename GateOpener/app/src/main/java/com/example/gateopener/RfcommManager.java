package com.example.gateopener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.transition.Transition;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.UUID;

public class RfcommManager{
    Context context;
    BluetoothDevice device;
    static OutputStream outputStream = null;
    static InputStream inputStream = null;

    public RfcommManager(Context context){
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public void setDevice(BluetoothDevice device){
        this.device = device;
        Log.d("oui", device.getName());
    }

    protected void connect2() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }

        UUID uuid = device.getUuids()[0].getUuid();
        BluetoothSocket socket = null;
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,R.string.toast3,Toast.LENGTH_SHORT).show();
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
                socket.connect();
                Log.d("oui", "connected2");
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();

            } catch (IOException | InvocationTargetException | IllegalAccessException | NoSuchMethodException ioException) {
                ioException.printStackTrace();
                Toast.makeText(context,R.string.toast3,Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void synchronize() {
        try {
            getImages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void getImages() throws IOException {
        File directory = new File(context.getApplicationInfo().dataDir +"/app_imageDir/");
        File[] images = directory.listFiles();

        if(images != null) {
            for(int i=0;i < images.length;i++) {
                String imageName = images[i].getName();
                Log.d("oui", imageName);
                sendImage(images[i].getAbsolutePath(), imageName);
            }
        } else {
            Log.d("oui", "Pb files");
        }
    }

    private void sendImage(String path, String name){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b_image = baos.toByteArray();
        Log.d("oui", String.valueOf(b_image.length));

        SynchThread synchUsers = new SynchThread(name, b_image, context);
        synchUsers.start();
    }

    public static class SynchThread extends Thread{

        String name = null;
        byte[] b_image = null;
        Context context = null;

        SynchThread(String name, byte[] b_image, Context context){
            this.name = name;
            this.b_image = b_image;
            this.context = context;
        }

        @Override
        public void run() {
            super.run();
            try {
                String size = "_" + String.valueOf(this.b_image.length) + "_" + name + "_";
                outputStream.write(size.getBytes(StandardCharsets.UTF_8));
                outputStream.write(b_image);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(context, MainActivity.class);

            int percentage = 0;
            while(percentage < 100){ //read form the inputStream
                try {
                    sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DataInputStream inStream = new DataInputStream(inputStream);
                int bytes = 0;
                byte[] buffer = new byte[1];

                try {
                    bytes = inStream.read(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                percentage = buffer[0];

                intent.putExtra("percentage", percentage);
                context.startActivity(intent);

                Log.d("oui", "Percentage :" + String.valueOf(percentage));
            }


        }
    }

    protected void echo(){
        String msg = "-truc8";
        Log.d("oui", "Sending : " + String.valueOf(msg));
        try {
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //read form the inputStream
        DataInputStream inStream = new DataInputStream(inputStream);
        int bytes = 0;
        byte[] buffer = new byte[msg.length()-1];

        try {
            bytes = inStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //String readMessage = new String(buffer[3], StandardCharsets.UTF_8);
        int readMessage = buffer[0];

        Log.d("oui", "Receiving :");
        Log.d("oui", String.valueOf(readMessage));
    }
}
