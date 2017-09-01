package com.tuankhai.travelassistants.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuankhai.travelassistants.R;
import com.tuankhai.travelassistants.webservice.DTO.SliderPlaceDTO;

import java.util.ArrayList;

/**
 * Created by Khai on 01/09/2017.
 */

public class SliderPlaceAdapter extends PagerAdapter {
    private Activity context;
    private ArrayList<Bitmap> arrImage;
    private SliderPlaceDTO.Place[] arrPlace;

    public SliderPlaceAdapter(FragmentActivity context, ArrayList<Bitmap> arrImgSliderPlace, SliderPlaceDTO.Place[] arrSliderPlace) {
        this.context = context;
        this.arrImage = arrImgSliderPlace;
        this.arrPlace = arrSliderPlace;
    }

    @Override
    public int getCount() {
        return arrImage.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup view, int position, Object object) {
        view.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        if (arrImage.size() == 0) return null;
        View item = LayoutInflater.from(context).inflate(R.layout.item_slider_place, null);
        ImageView imageView = item.findViewById(R.id.img_item_slider_place);
        TextView textView = item.findViewById(R.id.txt_item_name_slider_place);
        textView.setText(arrPlace[position].long_name);
        imageView.setImageBitmap(arrImage.get(position));
        view.addView(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return item;
    }

//    public void addItem() {
//        mSize++;
//        notifyDataSetChanged();
//    }
//
//    public void removeItem() {
//        mSize--;
//        mSize = mSize < 0 ? 0 : mSize;
//
//        notifyDataSetChanged();
//    }
}
