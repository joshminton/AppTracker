package com.jminton.apptracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.judemanutd.autostarter.AutoStartPermissionHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button btnBack = getActivity().findViewById(R.id.btnSettings);
        btnBack.setOnClickListener(this);

        getActivity().findViewById(R.id.btnDokiGuide).setOnClickListener(this);
        getActivity().findViewById(R.id.btnAutoStart).setOnClickListener(this);


//        if(AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(getActivity().getBaseContext())){
//            getActivity().findViewById(R.id.deviceWarningBox).setVisibility(View.VISIBLE);
//        } else {
//            getActivity().findViewById(R.id.deviceWarningBox).setVisibility(View.INVISIBLE);
//        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSettings:
                onClickSettings();
                break;
            case R.id.btnDokiGuide:
                onClickDokiGuide();
                break;
            case R.id.btnAutoStart:
                onClickAutoStart();
                break;

        }
    }

    public void onClickSettings(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Are you sure?");
        builder.setMessage("It's much better to set a target and then stick to it!");

        // add the buttons
        builder.setNegativeButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EntryTaskDialog ent = new EntryTaskDialog();
                ent.show(getChildFragmentManager(), "entryTask");
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onClickDokiGuide(){
        (new DokiDialog()).show(getChildFragmentManager(), null);
    }

    public void onClickAutoStart(){
        if(AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(getContext())){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Auto Start settings");
            builder.setMessage("Please select " + R.string.app_name +
                    " on the next screen and enable Auto Start.");
            // add the buttons
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AutoStartPermissionHelper.getInstance().getAutoStartPermission(getContext());
                }
            });
            builder.setNegativeButton("Cancel", null);
            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Auto Start is not needed for your device!");
            // add the buttons
            builder.setPositiveButton("Okay!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public void onDialogPositiveClick(DialogFragment dialog) {
        ((MainActivity) getActivity()).doAppsSetup();

    }

    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}