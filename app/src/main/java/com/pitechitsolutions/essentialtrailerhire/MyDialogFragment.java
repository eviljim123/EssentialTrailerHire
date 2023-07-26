package com.pitechitsolutions.essentialtrailerhire;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class MyDialogFragment extends DialogFragment {

    public static MyDialogFragment newInstance() {
        return new MyDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Title")
                .setMessage("Message")
                .setPositiveButton("OK", (dialog, id) -> {
                    // Do something
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // Do something
                });
        return builder.create();
    }
}
