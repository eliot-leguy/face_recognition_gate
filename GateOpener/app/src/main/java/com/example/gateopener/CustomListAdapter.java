package com.example.gateopener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {

    private List<Users> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomListAdapter(Context aContext, List<Users> listData){
        this.context = aContext;
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);

    }

    //@Override
    public int getCount(){
        return listData.size();
    }

    //@Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    //@Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null) {
            convertView = layoutInflater.inflate(R.layout.list_item_layout,null);
            holder = new ViewHolder();
            holder.faceView = (ImageView) convertView.findViewById(R.id.imageView_face);
            holder.nameView = (TextView) convertView.findViewById(R.id.textView_name);
            convertView.setTag(holder);
        } else {
            holder =(ViewHolder) convertView.getTag();
        }
        Users users = this.listData.get(position);
        holder.nameView.setText(users.getUserName());

        setImage(holder, users.getImagePath());

        return convertView;
    }

    Bitmap userImage(String imagePath){
        return BitmapFactory.decodeFile(imagePath);
    }

    static class ViewHolder {
        ImageView faceView;
        TextView nameView;
    }

    public void setImage(ViewHolder holder, String path) {
        //int targetW = holder.faceView.getWidth();
        //int targetH = holder.faceView.getHeight();

        int targetW = 110;
        int targetH = 90;

        Log.d("Tag-", String.valueOf(targetW));
        Log.d("Tag-", String.valueOf(targetH));

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        holder.faceView.setImageBitmap(bitmap);
    }
}
