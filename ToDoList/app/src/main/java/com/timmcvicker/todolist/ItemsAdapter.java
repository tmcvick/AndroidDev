package com.timmcvicker.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to extend an ArrayAdapter to be used with a ListItem.
 * @author timmcvicker
 * @see ListItem
 * @see ArrayAdapter
 *
 */
public class ItemsAdapter extends ArrayAdapter<ListItem> {

    /**
     *
     * @param context the context the Adapter will be used in
     * @param listItemArrayList an arraylist of ListItems to be put into a ListView
     */
    public ItemsAdapter(Context context, List<ListItem> listItemArrayList) {
        super(context, 0, listItemArrayList);
    }

    /**
     * This class will convert items in the ItemsAdapter into desired display format
     *
     * @param position selected position that will be returned
     * @param convertView the view that is converted into desired display format
     * @param parent the container view
     * @return the view that results from getting the item at position
     */
    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        ListItem item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_layout, parent, false);
        }
        // Lookup view for data population
        TextView tvTitle = (TextView) convertView.findViewById(R.id.descriptionView);
        TextView tvDate = (TextView) convertView.findViewById(R.id.dateView);
        RatingBar tvPriority = (RatingBar) convertView.findViewById(R.id.ratingBar);

        // Populate the data into the template view using the data object
        tvDate.setText(item.getDueDate());
        tvTitle.setText(item.getTitle());
        tvPriority.setRating(item.getPriority());

        // Return the completed view to render on screen
        return convertView;
    }
}