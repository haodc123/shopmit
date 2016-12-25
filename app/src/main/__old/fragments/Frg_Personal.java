package com.example.shopmeet.fragments;

import java.util.ArrayList;
import java.util.List;

import com.example.shopmeet.MainActivity;
import com.example.shopmeet.R;
import com.example.shopmeet.adapter.ListGroupAdapter;
import com.example.shopmeet.adapter.ListGroupAdapter.ListGroupData;
import com.example.shopmeet.adapter.ListMemberAdapter;
import com.example.shopmeet.adapter.ListMemberAdapter.ListMemberData;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Variables;
import com.example.shopmeet.view.MyExpandableListView;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by UserPC on 6/7/2016.
 */
public class Frg_Personal extends Fragment {

    // Profile
    private ImageView img_personal_profile_arrow, img_personal_avatar;
    private TextView tv_personal_name, tv_personal_email;
    private LinearLayout ll_personal_profile;
    // Group
    private ImageView img_personal_group_arrow;
    private TextView tv_personal_group_title;
    private MyExpandableListView lv_personal_list_group;
    private List<ListGroupData> groupList;
    private ListGroupAdapter groupAdapter;
    // Members
    private ImageView img_personal_members_arrow;
    private TextView tv_personal_members_title;
    private MyExpandableListView lv_personal_list_members;
    private List<ListMemberData> mbList;
    private ListMemberAdapter mbAdapter;

    private int isProfileExpanded = 1;
    private int isGroupExpanded = 1;
    private int isMemberExpanded = 1;

    FrgListener mCallback;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frg_personal, container, false);

        setInitView(v);
        setData();
        
        return v;
    }

    private void setInitView(View v) {
        // TODO Auto-generated method stub
        img_personal_profile_arrow = (ImageView)v.findViewById(R.id.img_personal_profile_arrow);
        img_personal_avatar = (ImageView)v.findViewById(R.id.img_personal_avatar);
        tv_personal_name = (TextView)v.findViewById(R.id.tv_personal_name);
        tv_personal_email = (TextView)v.findViewById(R.id.tv_personal_email);
        ll_personal_profile = (LinearLayout)v.findViewById(R.id.ll_personal_profile);

        img_personal_group_arrow = (ImageView)v.findViewById(R.id.img_personal_group_arrow);
        tv_personal_group_title = (TextView)v.findViewById(R.id.tv_personal_group_title);
        lv_personal_list_group = (MyExpandableListView)v.findViewById(R.id.lv_personal_list_group);

        img_personal_members_arrow = (ImageView)v.findViewById(R.id.img_personal_members_arrow);
        tv_personal_members_title = (TextView)v.findViewById(R.id.tv_personal_members_title);
        lv_personal_list_members = (MyExpandableListView)v.findViewById(R.id.lv_personal_list_members);

        img_personal_profile_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpandabilityContent("profile");
            }
        });
        img_personal_group_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpandabilityContent("group");
            }
        });
        img_personal_members_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpandabilityContent("members");
            }
        });
    }
    public void setExpandabilityContent(String part) {
        LinearLayout.LayoutParams loPar = null;
        switch (part) {
            case "profile":
                if (isProfileExpanded == 1) { // is expanding, do close
                    loPar = new LinearLayout.LayoutParams(0, 0);
                    ll_personal_profile.setLayoutParams(loPar);
                    ll_personal_profile.setVisibility(View.INVISIBLE);
                    isProfileExpanded = 0;
                    img_personal_profile_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_left));
                } else { // open
                    loPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    ll_personal_profile.setLayoutParams(loPar);
                    ll_personal_profile.setVisibility(View.VISIBLE);
                    isProfileExpanded = 1;
                    img_personal_profile_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_down));
                }
                break;
            case "group":
                if (isGroupExpanded == 1) { // is expanding, do close
                    loPar = new LinearLayout.LayoutParams(0, 0);
                    lv_personal_list_group.setLayoutParams(loPar);
                    lv_personal_list_group.setVisibility(View.INVISIBLE);
                    lv_personal_list_group.setExpanded(false);
                    isGroupExpanded = 0;
                    img_personal_group_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_left));
                } else { // open
                    loPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lv_personal_list_group.setLayoutParams(loPar);
                    lv_personal_list_group.setVisibility(View.VISIBLE);
                    lv_personal_list_group.setExpanded(true);
                    isGroupExpanded = 1;
                    img_personal_group_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_down));
                }
                break;
            case "members":
                if (isMemberExpanded == 1) { // is expanding, do close
                    loPar = new LinearLayout.LayoutParams(0, 0);
                    lv_personal_list_members.setLayoutParams(loPar);
                    lv_personal_list_members.setVisibility(View.INVISIBLE);
                    lv_personal_list_members.setExpanded(false);
                    isMemberExpanded = 0;
                    img_personal_members_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_left));
                } else { // open
                    loPar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lv_personal_list_members.setLayoutParams(loPar);
                    lv_personal_list_members.setVisibility(View.VISIBLE);
                    lv_personal_list_members.setExpanded(true);
                    isMemberExpanded = 1;
                    img_personal_members_arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_arrow_down));
                }
                break;
        }
    }
    public void setData() {
		// TODO Auto-generated method stub
		if (groupList == null)
			groupList = new ArrayList<ListGroupData>();
		if (groupAdapter == null)
			groupAdapter = new ListGroupAdapter(getActivity(), groupList);
		lv_personal_list_group.setExpanded(true);
		lv_personal_list_group.setAdapter(groupAdapter);
		
		if (mbList == null)
			mbList = new ArrayList<ListMemberData>();
		if (mbAdapter == null)
			mbAdapter = new ListMemberAdapter(getActivity(), mbList);
		lv_personal_list_members.setExpanded(true);
		lv_personal_list_members.setAdapter(mbAdapter);
	}
    @Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        Variables.curFrg = Constants.TAG_FRG_PER;
        MainActivity.setIndicator(Variables.curFrg);
		getListGroup();
		getListMembers();
	}
    private void getListMembers() {
		// TODO Auto-generated method stub
    	mbList.clear();
    	ListMemberData mb1 = new ListMemberData("member id 1", "member name 1", "http://e-space.vn/files/14.jpg", "ab@gmail.com", "3 months ago");
    	ListMemberData mb2 = new ListMemberData("member id 2", "member name 2", "http://e-space.vn/files/17.jpg", "cd@gmail.com", "4 months ago");
        ListMemberData mb3 = new ListMemberData("member id 3", "member name 3", "http://e-space.vn/files/16.jpg", "ef@gmail.com", "5 months ago");
    	mbList.add(mb1);
    	mbList.add(mb2);
        mbList.add(mb3);
		mbAdapter.notifyDataSetChanged();
	}

	private void getListGroup() {
		// TODO Auto-generated method stub
    	groupList.clear();
    	ListGroupData g1 = new ListGroupData("group id 1", "shop name 1", 5);
    	ListGroupData g2 = new ListGroupData("group id 2", "shop name 2", 3);
    	groupList.add(g1);
    	groupList.add(g2);
		groupAdapter.notifyDataSetChanged();
	}
    
    
	public interface FrgListener {
        public void onFrgEvent(int value);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (FrgListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Frg_PersonalListener");
        }
    }


}
