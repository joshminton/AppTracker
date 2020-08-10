package com.jminton.apptracker;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

//taken from official Android RecyclerView tutorial
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.MyViewHolder> {
    private HashMap<String, TrackedApp> listApps;
    private TrackingService trackingService;
    private AppsFragment appsFragment;
    ArrayList<TrackedApp> arrayListApps;
    private int numSelected = 0;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public CardView listLayout;
        public TextView appName;
        public TextView appCode;
        public ImageView icon;
        public CheckBox checkBox;
        public MyViewHolder(CardView v) {
            super(v);
            listLayout = v;
            appName = v.findViewById(R.id.listAppName);
            appCode = v.findViewById(R.id.listAppCode);
            icon = v.findViewById(R.id.listIcon);
            checkBox = v.findViewById(R.id.checkBox);
            v.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }

        //https://blog.oziomaogbe.com/2017/10/18/android-handling-checkbox-state-in-recycler-views.html
        @Override
        public void onClick(View v) {
            Log.d("CLICK!", "CLICK!");
            int adapterPosition = getAdapterPosition();
            TrackedApp tApp = arrayListApps.get(adapterPosition);
            if (!tApp.isTracked()) {
                checkBox.setChecked(true);
                listApps.get(tApp.getPackageName()).setTracked(true);
                tApp.setTracked(true);
                listLayout.setCardElevation(10);
                listLayout.getBackground().setColorFilter(v.getResources().getColor(R.color.selectedTint), PorterDuff.Mode.SRC_ATOP);
                numSelected++;
            }
            else  {
                checkBox.setChecked(false);
                listApps.get(tApp.getPackageName()).setTracked(false);
                tApp.setTracked(false);
                listLayout.setCardElevation(1);
                listLayout.getBackground().clearColorFilter();
                numSelected--;
            }
            Log.d("YEEEE", "HAAAAA");
            trackingService.saveTrackedApps();
            appsFragment.updateUsage();
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AppsAdapter(HashMap<String, TrackedApp> listApps, TrackingService trackingService, AppsFragment appsFragment) {
        this.listApps = listApps;
        this.trackingService = trackingService;
        this.appsFragment = appsFragment;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AppsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        arrayListApps = new ArrayList<>(listApps.values());
        Collections.sort(arrayListApps, new TrackedAppComparator());

        holder.appName.setText(arrayListApps.get(position).getName());
        holder.appCode.setText("Used " + TimeConverter.millsToHoursMinutesSecondsVerbose((long) arrayListApps.get(position).getAvgUsageLastWeek()) + " per day");

        holder.icon.setImageDrawable(arrayListApps.get(position).getIcon());

        if(arrayListApps.get(position).isTracked()){
            holder.checkBox.setChecked(true);
            holder.listLayout.setCardElevation(10);
            holder.listLayout.getBackground().setColorFilter(holder.listLayout.getResources().getColor(R.color.selectedTint), PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.checkBox.setChecked(false);
            holder.listLayout.setCardElevation(1);
            holder.listLayout.getBackground().clearColorFilter();
        }


//        holder.appCode.setText()
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return listApps.size();
    }
}
