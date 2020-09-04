package com.jminton.apptracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

import androidx.fragment.app.DialogFragment;

public class EntryTaskDialog extends DialogFragment {

    public interface EntryTaskDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    EntryTaskDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (EntryTaskDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(this.toString()
                    + " must implement EntryTaskDialogListener");
        }
    }




    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        Random random = new Random();
        final StringBuilder entryText = new StringBuilder();

        int numBlocks = 0;

        for(int y = 0; y < numBlocks; y++){
            for(int x = 0; x < 6; x++){
                entryText.append(String.valueOf(random.nextInt(10)));
            }

            if(y < (numBlocks - 1)){
                entryText.append(" ");
            }
        }


        random.nextInt(10);

        final View view = inflater.inflate(R.layout.entry_task_dialog, null);
        ((TextView) view.findViewById(R.id.entryText)).setText(entryText.toString(), TextView.BufferType.NORMAL);

        //https://developer.android.com/guide/topics/ui/dialogs
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle("Enter the numbers as displayed to proceed.")
                // Add action buttons
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        if(((EditText) view.findViewById(R.id.editEntry)).getText().toString().equals(entryText.toString())){
                            listener.onDialogPositiveClick(EntryTaskDialog.this);
                        } else {
                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                            builder.setMessage("Incorrect code");
                            // add the buttons
                            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        EntryTaskDialog.this.getDialog().cancel();
                        listener.onDialogNegativeClick(EntryTaskDialog.this);
                    }
                });

        return builder.create();
    }

}
