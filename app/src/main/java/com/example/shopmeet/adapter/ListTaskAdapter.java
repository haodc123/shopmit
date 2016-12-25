package com.example.shopmeet.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shopmeet.R;
import com.example.shopmeet.fragments.Frg_Chat;
import com.example.shopmeet.fragments.Frg_Group;
import com.example.shopmeet.fragments.Frg_Task_Detail;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ListTaskAdapter extends BaseAdapter {
	private Context context;
    private List<ListTaskData> taskItems;

    public ListTaskAdapter(Context context, List<ListTaskData> items) {
        this.context = context;
        this.taskItems = items;
    }
 
    @Override
    public int getCount() {
        return taskItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return taskItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
        final int iPosition = position;
		final ListTaskData m = taskItems.get(position);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            convertView = mInflater.inflate(R.layout.row_task, null);

            TextView row_task_created = (TextView) convertView.findViewById(R.id.row_task_created);
            TextView row_task_content = (TextView) convertView.findViewById(R.id.row_task_content);
            TextView row_task_require_person = (TextView) convertView.findViewById(R.id.row_task_require_person);
            TextView row_task_deadline = (TextView) convertView.findViewById(R.id.row_task_deadline);
            TextView row_task_status = (TextView) convertView.findViewById(R.id.row_task_status);

        row_task_created.setText(m.getTask_create_at());
        row_task_content.setText(m.getTask_content());
        row_task_require_person.setText(m.getTask_require_person_fname()+" "+m.getTask_require_person_lname());
        row_task_deadline.setText(m.getTask_deadline());
        setCurrentStatus(row_task_status, m.getTask_status());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                goTaskDetail(m.getTask_id(), m.getTask_staff_id());
                }
            });

            return convertView;
	}

    public void setCurrentStatus(TextView tv_task_status, int status) {
        switch (status) {
            case ListTaskData.TASK_STATUS_OUT_OF_DATE:
                tv_task_status.setText(context.getResources().getString(R.string.status_ofd));
                break;
            case ListTaskData.TASK_STATUS_COMPLETED:
                tv_task_status.setText(context.getResources().getString(R.string.status_completed));
                break;
            case ListTaskData.TASK_STATUS_UNCOMPLETED:
                tv_task_status.setText(context.getResources().getString(R.string.status_uncompleted));
                break;
            default:
                tv_task_status.setText(context.getResources().getString(R.string.status_waiting));
                break;
        }
    }

    public void goTaskDetail(String task_id, String task_staff_id) {
        Frg_Task_Detail frgTkd = new Frg_Task_Detail();

        Bundle b = new Bundle();
        b.putString("task_id", task_id);
        b.putString("task_staff_id", task_staff_id);
        frgTkd.setArguments(b);

        ((Activity)context).getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frgTkd, Constants.TAG_FRG_TASK_DETAIL)
                .addToBackStack(null)
                .commit();

        Variables.curFrg = Constants.TAG_FRG_TASK_DETAIL;
    }

	public static class ListTaskData {
        public final static int TASK_STATUS_COMPLETED = 0;
        public final static int TASK_STATUS_WAITING = 1;
        public final static int TASK_STATUS_UNCOMPLETED = 2;
        public final static int TASK_STATUS_OUT_OF_DATE = 3;

    	private String task_staff_id, task_id, task_content, task_require_person_id, task_require_person_fname, task_require_person_lname,
                task_create_at, task_deadline;
        private int task_status;

        public ListTaskData() {
        }
     
        public ListTaskData(String task_id, String task_staff_id, String task_content, String task_require_person_id, String task_require_person_fname,
                            String task_require_person_lname, String task_create_at, String task_deadline,
                            int task_status) {
            this.task_id = task_id;
            this.task_staff_id = task_staff_id;
            this.task_content = task_content;
            this.task_require_person_fname = task_require_person_fname;
            this.task_require_person_lname = task_require_person_lname;
            this.task_create_at = task_create_at;
            this.task_deadline = task_deadline;
            this.task_require_person_id = task_require_person_id;
            this.task_status = task_status;
        }

        public String getTask_staff_id() {
            return task_staff_id;
        }

        public void setTask_staff_id(String task_staff_id) {
            this.task_staff_id = task_staff_id;
        }

        public String getTask_id() {
            return task_id;
        }

        public void setTask_id(String task_id) {
            this.task_id = task_id;
        }

        public String getTask_content() {
            return task_content;
        }

        public void setTask_content(String task_content) {
            this.task_content = task_content;
        }

        public String getTask_require_person_fname() {
            return task_require_person_fname;
        }

        public void setTask_require_person_fname(String task_require_person_fname) {
            this.task_require_person_fname = task_require_person_fname;
        }

        public String getTask_require_person_lname() {
            return task_require_person_lname;
        }

        public void setTask_require_person_lname(String task_require_person_lname) {
            this.task_require_person_lname = task_require_person_lname;
        }

        public String getTask_create_at() {
            return task_create_at;
        }

        public void setTask_create_at(String task_create_at) {
            this.task_create_at = task_create_at;
        }

        public String getTask_deadline() {
            return task_deadline;
        }

        public void setTask_deadline(String task_deadline) {
            this.task_deadline = task_deadline;
        }

        public String getTask_require_person_id() {
            return task_require_person_id;
        }

        public void setTask_require_person_id(String task_require_person_id) {
            this.task_require_person_id = task_require_person_id;
        }

        public int getTask_status() {
            return task_status;
        }

        public void setTask_status(int task_status) {
            this.task_status = task_status;
        }
    }
}
