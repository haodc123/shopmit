package com.example.shopmeet.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.widget.TextView;
import android.widget.DatePicker;
import android.app.Dialog;
import java.util.Calendar;

import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by UserPC on 7/13/2016.
 */
public class MyDatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, MainActivity.LocalRtcListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //Use the current date as the default date in the date picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        //Create a new DatePickerDialog instance and return it
        /*
            DatePickerDialog Public Constructors - Here we uses first one
            public DatePickerDialog (Context context, DatePickerDialog.OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth)
            public DatePickerDialog (Context context, int theme, DatePickerDialog.OnDateSetListener listener, int year, int monthOfYear, int dayOfMonth)
         */
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, this, year, month, day);
        dpd.getDatePicker().setCalendarViewShown(false);
        dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        return dpd;
    }
    public void onDateSet(DatePicker view, int year, int month, int day) {
    }

    public void onLocalReceive_private_message(JSONObject data) {
    }
    public void onLocalReceive_group_message(JSONObject data) {
    }
    @Override
    public void onLocalNetworkChange() {
    }
    public void onLocalOn_user_online(JSONObject data) {
    }
}