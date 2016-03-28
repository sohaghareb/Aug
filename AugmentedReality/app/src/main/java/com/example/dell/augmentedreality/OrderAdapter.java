package com.example.dell.augmentedreality;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dell on 08/03/2016.
 */
public class OrderAdapter extends BaseAdapter {
    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    Activity activity;
    int owner;
    LayoutInflater inflater;
     Order[]list;
    public OrderAdapter(Activity activity,Order[] list,int o){
        this.activity=activity;
        this.list=list;
        owner=o;
        inflater=activity.getLayoutInflater();
    }
    @Override
    public int getCount() {
        return list.length;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            convertView = inflater.inflate(R.layout.order_raw, null);
        }
        TextView name = (TextView) convertView.findViewById(R.id.order_name);
        TextView price = (TextView) convertView.findViewById(R.id.order_price );
        Order x=list[position];
        name.setText(x.getName());
        price.setText(x.getPrice()+"LE");
        TextView available=(TextView)convertView.findViewById(R.id.available);
        TextView requests=(TextView)convertView.findViewById(R.id.requests);
        //Button avorreq=(Button)convertView.findViewById(R.id.requestOrAv);
        if(x.getAvailable()==1){
            available.setText("Available");
            available.setTextColor(Color.parseColor("#ff0a1551"));
        }
        else{
            available.setText("Not Available");
            available.setTextColor(Color.RED);
        }
        requests.setText(x.getRequests()+"Requests");


        return convertView;
    }
}
