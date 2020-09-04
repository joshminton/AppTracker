package com.jminton.apptracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import dev.doubledot.doki.views.DokiContentView;

public class DokiDialog extends DialogFragment {
    //https://developer.android.com/guide/topics/ui/dialogs

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dokiCustomView = inflater.inflate(R.layout.activity_doki, null);

        DokiContentView dCV = dokiCustomView.findViewById(R.id.doki_content);

        if(dCV != null){
            dCV.loadContent(Build.MANUFACTURER.toLowerCase());
            dCV.setButtonsVisibility(false);
            dCV.setBackgroundColor(getResources().getColor(R.color.themeCompatBackground));
            dCV.setPrimaryTextColor(getResources().getColor(R.color.themeCompatTextColour));
            dCV.setSecondaryTextColor(getResources().getColor(R.color.themeCompatTextColour));
            dCV.setButtonsTextColor(getResources().getColor(R.color.colorPrimary));
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dokiCustomView)
                // Add action buttons
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }
}