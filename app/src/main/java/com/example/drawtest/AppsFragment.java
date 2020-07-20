package com.example.drawtest;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

        FloatingActionButton refreshBtn = getActivity().findViewById(R.id.refresh);
        refreshBtn.setOnClickListener(this);

        lstApps = (RecyclerView) getView().findViewById(R.id.lstApps);
        layoutManager = new LinearLayoutManager(getContext());
        lstApps.setLayoutManager(layoutManager);

        apps = new HashMap<>();

        trackingService = ((MainActivity) getActivity()).getTrackingService();

        lstAppsAdapter = new AppsAdapter(apps, trackingService);
        lstApps.setAdapter(lstAppsAdapter);

        onClickRefresh();

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
            case R.id.refresh:
                onClickRefresh();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onClickRefresh();
    }
}