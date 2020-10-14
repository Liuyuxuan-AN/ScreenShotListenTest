package com.example.screenshotlistentest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.screenshotlistentest.Item;
import com.example.screenshotlistentest.R;

import java.util.List;

/**
 *    author : 刘雨轩
 *    e-mail : 1262610086@qq.com
 *    date   : 2020/10/10
 *    desc   :自定义适配器，用于HelpActivity中的ListView
 */
public class ItemAdapter extends ArrayAdapter {
    private final int resourceId;

    public ItemAdapter(Context context, int textResourceId, List<Item> objects){
        super(context,textResourceId,objects);
        resourceId = textResourceId;
    }

    //在进行适配时自动运行
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Item item = (Item)getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,null);
        ImageView imageView = (ImageView)view.findViewById(R.id.item_image_view);
        TextView textView = (TextView)view.findViewById(R.id.item_text_view);
        imageView.setImageResource(item.getImageId());
        textView.setText(item.getS1());
        return view;
    }
}
