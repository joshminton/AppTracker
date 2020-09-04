package com.jminton.apptracker;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LimitsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LimitsFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TrackingService trackingService;
    SeekBar seekBar;
    TextView txtTarget, txtUsage, txtExposition, txtComment;
    long averageUsage;

    public LimitsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LimitsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LimitsFragment newInstance(String param1, String param2) {
        LimitsFragment fragment = new LimitsFragment();
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        trackingService = ((MainActivity) getActivity()).getTrackingService();
        seekBar = (SeekBar) getView().findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        txtTarget = getView().findViewById(R.id.txtCurrentTarget);
        txtUsage = getView().findViewById(R.id.txtUsageTime);
        txtExposition = getView().findViewById(R.id.txtTargetExposition);
        txtComment = getView().findViewById(R.id.txtTargetComment);

        ExtendedFloatingActionButton btnConfirm = getActivity().findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        Button btnBack = getActivity().findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        averageUsage = trackingService.trackedAppsAverageUsageLastWeek();

        seekBar.setProgress((int) (( 1f - ((float) trackingService.getQuota() / (float) (averageUsage / 1000 / 60))) * 100));

        Log.d("Debug:", trackingService.getQuota() + " " + averageUsage);

        txtUsage.setText(TimeConverter.millsToHoursMinutesSecondsVerbose(averageUsage));
        txtExposition.setText(getText(R.string.app_name) + " will show your progress towards this target on your screen.");

        seekBarUpdate(seekBar.getProgress());

    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            seekBarUpdate(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    private void seekBarUpdate(int progress) {

        progress -= 50;

        txtTarget.setText(TimeConverter.millsToHoursMinutesSecondsVerbose((long) (averageUsage * ((double) (100 - progress) / 100))));
        if (progress < 0) {
            txtComment.setText("That's more than you normally use them!");
        } else if (progress < 33) {
            txtComment.setText("That's a reduction of " + progress + "%...");
        } else if (progress < 66) {
            txtComment.setText("That's a reduction of " + progress + "%.");
        } else {
            txtComment.setText("That's a reduction of " + progress + "%!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_limits, container, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConfirm:
                onClickConfirm();
                break;
            case R.id.btnBack:
                onClickBack();
                break;
        }
    }

    private void onClickConfirm(){
        trackingService.setQuota((long) (averageUsage * ((double) (100 - (seekBar.getProgress() - 50)) / 100)));
//        Toast.makeText(this.getContext(), " " + trackingService.getQuota(), Toast.LENGTH_SHORT).show();
        ((MainActivity) getActivity()).setupDone();
    }

    private void onClickBack(){
        ((MainActivity) getActivity()).doAppsSetup();
    }
}