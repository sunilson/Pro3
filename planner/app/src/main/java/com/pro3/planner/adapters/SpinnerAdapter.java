package com.pro3.planner.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.pro3.planner.baseClasses.Category;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by linus_000 on 25.11.2016.
 */

public class SpinnerAdapter extends ArrayAdapter {

    private List<Category> list;

    public SpinnerAdapter(Context context, int resource, List<Category> entries) {
        super(context, resource);
        list = entries;
    }

    @Override
    public void sort(Comparator comparator) {
        Collections.sort(list, comparator);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return list.get(position).getCategoryName();
    }

    public Category getCategory(int position) {
        return list.get(position);
    }


    @Override
    public int getPosition(Object item) {
        return super.getPosition(item);
    }
}
