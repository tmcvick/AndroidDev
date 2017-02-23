package com.timmcvicker.todolist;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * Data object that represents an item in the todolist
 * Implements serializable and parcelable in order to pass it in Intents
 */
public class ListItem implements Serializable, Parcelable {
    /**
     * holds the short description of the task
     */
    private String title;

    /**
     * range[1..5]
     * holds the priority of the item
     */
    private int priority;

    /**
     * holds the date that the item is due.
     * format: mm/dd
     */
    private String dueDate;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public ListItem(String title, int priority, String dueDate) {
        this.dueDate = dueDate;
        this.priority = priority;
        this.title = title;
    }

    public ListItem() {
        dueDate = "";
        priority = 0;
        title = "";
    }

    /**
     * a comparator that will order ListItems by date ascending
     */
    public static Comparator<ListItem> ListItemDateComparator = new Comparator<ListItem>() {

        public int compare(ListItem item1, ListItem item2) {
            String dueDate1 = item1.getDueDate();
            String dueDate2 = item2.getDueDate();

            if (dueDate1.length() > 1 && dueDate1.substring(0, 1).equals("0")) {
                dueDate1 = dueDate1.split("0")[1];
            }

            if (dueDate2.length() > 1 && dueDate2.substring(0, 1).equals("0")) {
                dueDate2 = dueDate2.split("0")[1];
            }

            //ascending order
            return dueDate1.compareTo(dueDate2);

            //descending order
            //return dueDate2.compareTo(dueDate1);
        }};

    /**
     * a comparator that will order ListItems by priority descending. ties are broken by due date ascending
     */
    public static Comparator<ListItem> ListItemPriorityComparator = new Comparator<ListItem>() {

        public int compare(ListItem item1, ListItem item2) {

            int priority1 = item1.getPriority();
            int priority2 = item2.getPriority();

	   /*For ascending order*/
            // return priority1-priority2;

	   /*For descending order*/
            int diff = priority2-priority1;
            if (diff == 0) {
                String dueDate1 = item1.getDueDate();
                String dueDate2 = item2.getDueDate();
                if (dueDate1.length() > 1 && dueDate1.substring(0, 1).equals("0")) {
                    dueDate1 = dueDate1.split("0")[1];
                }

                if (dueDate2.length() > 1 && dueDate2.substring(0, 1).equals("0")) {
                    dueDate2 = dueDate2.split("0")[1];
                }

                //ascending order
                return dueDate1.compareTo(dueDate2);
            }

            return diff;
        }};

    @Override
    public String toString() {
        return title + " " + priority + " " + dueDate;
    }


    /**
     * constructor for use with Parcelable
     * @param in Parcel that will be converted to ListItem
     */
    protected ListItem(Parcel in) {
        title = in.readString();
        priority = in.readInt();
        dueDate = in.readString();
    }

    @Override
    /**
     * returns 0, necessary override for parcelable
     */
    public int describeContents() {
        return 0;
    }

    @Override
    /**
     * converts the ListItem to a Parcel dest
     * @param dest the parcel to convert to
     * @param flags not used
     */
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(priority);
        dest.writeString(dueDate);
    }

    @SuppressWarnings("unused")
    /**
     * necessary overrides for Parcelable
     */
    public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
        @Override
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };
}