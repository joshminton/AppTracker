package com.jminton.apptracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppsFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TrackingService trackingService;

    RecyclerView lstApps;
    RecyclerView.LayoutManager layoutManager;
    AppsAdapter lstAppsAdapter;
    HashMap<String, TrackedApp> apps;

    public AppsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AppsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AppsFragment newInstance(String param1, String param2) {
        AppsFragment fragment = new AppsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAdvance = getActivity().findViewById(R.id.btnAdvance);
        btnAdvance.setOnClickListener(this);

        lstApps = (RecyclerView) getView().findViewById(R.id.lstApps);
        layoutManager = new LinearLayoutManager(getContext());
        lstApps.setLayoutManager(layoutManager);

        apps = new HashMap<>();

        trackingService = ((MainActivity) getActivity()).getTrackingService();

        lstAppsAdapter = new AppsAdapter(apps, trackingService, this);
        lstApps.setAdapter(lstAppsAdapter);

        onClickRefresh();
        updateUsage();

    }

    //https://developer.android.com/training/animation/reveal-or-hide-view
    protected void updateUsage(){

        int numSelected = 0;

        for(TrackedApp tApp : apps.values()){
            if(tApp.isTracked()){
                numSelected++;
            }
        }

        final CardView cardView = getView().findViewById(R.id.boxWeekUsage);

        if(numSelected == 0){

            int cx = cardView.getWidth() / 2;
            int cy = cardView.getHeight() / 2;

            // get the initial radius for the clipping circle
            float initialRadius = (float) Math.hypot(cx, cy);

            // create the animation (the final radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(cardView, cx, cy, initialRadius, 0f);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    cardView.setVisibility(View.GONE);
                }
            });

            // start the animation
            anim.start();

        } else {

            //https://developer.android.com/training/animation/reveal-or-hide-view
            if(cardView.getVisibility() == View.GONE){
                // get the center for the clipping circle
                int cx = cardView.getWidth() / 2;
                int cy = cardView.getHeight() / 2;

                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);

                // create the animator for this view (the start radius is zero)
                Animator anim = ViewAnimationUtils.createCircularReveal(cardView, cx, cy, 0f, finalRadius);

                // make the view visible and start the animation
                cardView.setVisibility(View.VISIBLE);
                anim.start();
            }
            TextView txtWeekUsage = getActivity().findViewById(R.id.txtWeekUsage);
            String useMessage = TimeConverter.millsToHoursMinutesSecondsVerbose(trackingService.trackedAppsAverageUsageLastWeek());

            String startText = "You've used these apps an average of ";
            String endText = " a day in total in the last two weeks.";
            SpannableString str = new SpannableString(startText + useMessage + endText);
            str.setSpan(new StyleSpan(Typeface.BOLD), startText.length(), startText.length() + useMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            txtWeekUsage.setText(str);
        }
    }

    public void onClickRefresh(){
        if(trackingService == null){
            Log.d("Uh oh", "It's null!");
        } else {
            trackingService.refreshUsageStats();
            apps.clear();
            apps.putAll(trackingService.getTrackedAppsData());
            Log.d("Length", "" + apps.size());
            Objects.requireNonNull(lstApps.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdvance:
                Log.d("Trying", "This");
                ((MainActivity) getActivity()).doLimitsSetup();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}