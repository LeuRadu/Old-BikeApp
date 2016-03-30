package com.leuradu.android.bikeapp.utils;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.leuradu.android.bikeapp.R;
import com.leuradu.android.bikeapp.model.CustomMenuItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by radu on 28.03.2016.
 */
public class MenuListAdapter extends ArrayAdapter<CustomMenuItem> {

    private static final String TAG = "MenuListAdapter";

    private ArrayList<CustomMenuItem> mItems;
    private LayoutInflater mInflater;

    public MenuListAdapter(Context context, int resource, ArrayList<CustomMenuItem> objects) {
        super(context, resource, objects);
        mItems = objects;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CustomMenuItem item = mItems.get(position);
        MenuItemHolder holder;
        View leftView = null;
        TextView label;
        int type = item.getType();

        if (convertView == null) {
            if (type == CustomMenuItem.TYPE_HEADER) {
                convertView = mInflater.inflate(R.layout.menu_list_header, parent, false);
                label = (TextView) convertView.findViewById(R.id.menu_item_label);
            } else if (type == CustomMenuItem.TYPE_ITEM){
                convertView = mInflater.inflate(R.layout.menu_list_item, parent, false);
                leftView = convertView.findViewById(R.id.menu_item_icon);
                label = (TextView) convertView.findViewById(R.id.menu_item_label);
            } else if (type == CustomMenuItem.TYPE_CHECKBOX){
                Log.d(TAG, "type is checkbox!");
                convertView = mInflater.inflate(R.layout.menu_list_checkbox, parent, false);
                leftView = convertView.findViewById(R.id.menu_item_checkbox);
                label = (TextView) convertView.findViewById(R.id.menu_item_label);
            } else { //if (type == CustomMenuItem.TYPE_INFO) {
                convertView = mInflater.inflate(R.layout.menu_list_info, parent, false);
                label = (TextView) convertView.findViewById(R.id.menu_item_label);
            }

            holder = new MenuItemHolder();
            holder.leftView = leftView;
            holder.labelView = label;

            convertView.setTag(holder);
        } else {
            holder = (MenuItemHolder) convertView.getTag();
        }

        holder.labelView.setText(item.getLabel());
        switch (type) {
            case CustomMenuItem.TYPE_ITEM:
                ((ImageView)holder.leftView).setImageDrawable(item.getIcon());
                break;
        }

        return convertView;
    }

//    Returns the type of view that getView will return for that position
    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    @Override
    public int getViewTypeCount() {
        return mItems.size();
    }

    private static class MenuItemHolder{
        private View leftView;
        private TextView labelView;
    }
}
