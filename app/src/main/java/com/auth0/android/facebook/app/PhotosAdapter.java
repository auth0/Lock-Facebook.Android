package com.auth0.android.facebook.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PhotosAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> photos;

    public PhotosAdapter(Context context, List<String> photos) {
        this.context = context;
        this.photos = photos;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        }
        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        Glide.with(context).load(photos.get(position)).into(image);
        return convertView;
    }
}
