package com.example.shopmeet.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListGroupMessAdapter.ListGroupMessData;
import com.example.shopmeet.adapter.ListMessAdapter;
import com.example.shopmeet.adapter.ListMessAdapter.ListMemberMessData;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.view.MyExpandableListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by UserPC on 6/10/2016.
 */
public class Frg_Mess extends Fragment {

    // Group
    private MyExpandableListView lv_mess_list_group;
    private List<ListGroupMessData> groupmessList;
    private ListGroupMessAdapter groupmessAdapter;
    // Members
    private MyExpandableListView lv_mess_list_members;
    private List<ListMemberMessData> mbmessList;
    private ListMessAdapter mbmessAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_mess, container, false);

        setInitView(v);
        setData();

        return v;
    }

    private void setInitView(View v) {
        // TODO Auto-generated method stub

        lv_mess_list_group = (MyExpandableListView)v.findViewById(R.id.lv_mess_list_group);

        lv_mess_list_members = (MyExpandableListView)v.findViewById(R.id.lv_mess_list_members);

    }
    public void setData() {
        // TODO Auto-generated method stub
        if (groupmessList == null)
            groupmessList = new ArrayList<ListGroupMessData>();
        if (groupmessAdapter == null)
            groupmessAdapter = new ListGroupMessAdapter(getActivity(), groupmessList);
        lv_mess_list_group.setExpanded(true);
        lv_mess_list_group.setAdapter(groupmessAdapter);

        if (mbmessList == null)
            mbmessList = new ArrayList<ListMemberMessData>();
        if (mbmessAdapter == null)
            mbmessAdapter = new ListMessAdapter(getActivity(), mbmessList);
        lv_mess_list_members.setExpanded(true);
        lv_mess_list_members.setAdapter(mbmessAdapter);
    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Variables.curFrg = Constants.TAG_FRG_MESS;
        MainActivity.setIndicator(Variables.curFrg);
        getListGroupMess();
        getListMemberMess();
    }
    private void getListMemberMess() {
        // TODO Auto-generated method stub
        mbmessList.clear();
        ListMemberMessData mb1 = new ListMemberMessData("member id 1", "member name 1", "http://e-space.vn/files/14.jpg", "ab@gmail.com",
                "last msg 1", "id_lastmess 1", "05:50", 0);
        ListMemberMessData mb2 = new ListMemberMessData("member id 2", "member name 2", "http://e-space.vn/files/17.jpg", "cd@gmail.com",
                "last msg 2", "id_lastmess 2", "07:50", 1);
        mbmessList.add(mb1);
        mbmessList.add(mb2);
        mbmessAdapter.notifyDataSetChanged();
    }

    private void getListGroupMess() {
        // TODO Auto-generated method stub
        groupmessList.clear();
        ListGroupMessData g1 = new ListGroupMessData("group id 1", "shop name 1", "last_msg 1", "id_last_mess 1",
                "iduser_last_mess 1","17:34", 1);
        ListGroupMessData g2 = new ListGroupMessData("group id 2", "shop name 2", "last_msg 2", "id_last_mess 2",
                "iduser_last_mess 2","14:34", 0);
        groupmessList.add(g1);
        groupmessList.add(g2);
        groupmessAdapter.notifyDataSetChanged();
    }


}
