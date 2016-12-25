package com.example.shopmeet;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.shopmeet.fragments.Frg_Mess;
import com.example.shopmeet.fragments.Frg_Personal;
import com.example.shopmeet.globals.Constants;
import com.example.shopmeet.globals.Functions;
import com.example.shopmeet.globals.Variables;

public class MainActivity extends Activity implements Frg_Personal.FrgListener {

    // Action bar
    private TextView tvTitle;
    private ImageView imgAdd, imgSearch;
    // Icon tab + indicator
    private ImageView img_tab_per, img_tab_mess, img_tab_history, img_tab_task, img_tab_menu2;
    public static ImageView img_tab_per_idc, img_tab_mess_idc, img_tab_history_idc, img_tab_task_idc, img_tab_menu2_idc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setInitView();
        setInitFragments();
    }

    private void setInitFragments() {
        // TODO Auto-generated method stub
        if (findViewById(R.id.fragment_container) != null) {

            Frg_Personal personalFragment = new Frg_Personal();
            personalFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, personalFragment, Constants.TAG_FRG_PER).commit();

            Variables.curFrg = Constants.TAG_FRG_PER;
            setIndicator(Variables.curFrg);

        }
    }
    public void setInitView() {
        setCustomActionBar("Member 28");
        img_tab_per = (ImageView)findViewById(R.id.img_tab_per);
        img_tab_mess = (ImageView)findViewById(R.id.img_tab_mess);
        img_tab_history = (ImageView)findViewById(R.id.img_tab_history);
        img_tab_task = (ImageView)findViewById(R.id.img_tab_task);
        img_tab_menu2 = (ImageView)findViewById(R.id.img_tab_menu2);
        img_tab_per_idc = (ImageView)findViewById(R.id.img_tab_per_idc);
        img_tab_mess_idc = (ImageView)findViewById(R.id.img_tab_mess_idc);
        img_tab_history_idc = (ImageView)findViewById(R.id.img_tab_history_idc);
        img_tab_task_idc = (ImageView)findViewById(R.id.img_tab_task_idc);
        img_tab_menu2_idc = (ImageView)findViewById(R.id.img_tab_menu2_idc);
        img_tab_per.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_PER);
            }
        });
        img_tab_mess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_MESS);
            }
        });
        img_tab_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_HIS);
            }
        });
        img_tab_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_TASK);
            }
        });
        img_tab_menu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTab(Constants.TAG_FRG_MENU2);
            }
        });
    }
    public void openTab(String tag) {
        switch (tag) {
            case Constants.TAG_FRG_MESS:
                if (Variables.curFrg == Constants.TAG_FRG_MESS) {
                    // if current fragment is Frg_Mess, do nothing
                } else {
                    getFragmentManager().popBackStack();
                    Frg_Mess stFrg = new Frg_Mess();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, stFrg, Constants.TAG_FRG_MESS)
                            .addToBackStack(null)
                            .commit();

                    Variables.curFrg = Constants.TAG_FRG_MESS;
                    setIndicator(Variables.curFrg);
                }
                break;
            case Constants.TAG_FRG_HIS:

                break;
            case Constants.TAG_FRG_TASK:

                break;
            case Constants.TAG_FRG_MENU2:

                break;
            default:
                if (Variables.curFrg == Constants.TAG_FRG_PER) {
                    // if current fragment is Frg_Personal, do nothing
                } else {
                    getFragmentManager().popBackStack();
                    Frg_Personal stFrg = new Frg_Personal();
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, stFrg, Constants.TAG_FRG_PER)
                            .addToBackStack(null)
                            .commit();

                    Variables.curFrg = Constants.TAG_FRG_PER;
                    setIndicator(Variables.curFrg);
                }
                break;
        }
    }
    private void setCustomActionBar(String title) {
        // TODO Auto-generated method stub
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
        getActionBar().setCustomView(R.layout.actionbar_main);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        imgAdd = (ImageView)findViewById(R.id.imgAdd);
        imgSearch = (ImageView)findViewById(R.id.imgSearch);
    	
    	/*getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4183d7")));
    	getActionBar().setTitle(title);
    	getActionBar().setHomeButtonEnabled(false);
    	getActionBar().setDisplayHomeAsUpEnabled(false);*/
    }

    public static void setIndicator(String tag) {
        switch (tag) {
            case Constants.TAG_FRG_MESS:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.VISIBLE);
                img_tab_history_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.INVISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
            case Constants.TAG_FRG_HIS:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_history_idc.setVisibility(View.VISIBLE);
                img_tab_task_idc.setVisibility(View.INVISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
            case Constants.TAG_FRG_TASK:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_history_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.VISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
            case Constants.TAG_FRG_MENU2:
                img_tab_per_idc.setVisibility(View.INVISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_history_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.INVISIBLE);
                img_tab_menu2_idc.setVisibility(View.VISIBLE);
                break;
            default:
                img_tab_per_idc.setVisibility(View.VISIBLE);
                img_tab_mess_idc.setVisibility(View.INVISIBLE);
                img_tab_history_idc.setVisibility(View.INVISIBLE);
                img_tab_task_idc.setVisibility(View.INVISIBLE);
                img_tab_menu2_idc.setVisibility(View.INVISIBLE);
                break;
        }
    }


    @Override
    public void onFrgEvent(int value) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom()) ) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

    @SuppressWarnings("static-access")
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            // get Back
            try {
                Thread.currentThread().sleep(100);
                getFragmentManager().popBackStack();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        /*MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
 
        searchView.setIconified(true);
 
        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView searchImg = (ImageView) searchView.findViewById(searchImgId);
        searchImg.setImageResource(R.drawable.ic_search);
 
        int searchDeleteImgId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView searchDeleteImg = (ImageView) searchView.findViewById(searchDeleteImgId);
        searchDeleteImg.setImageResource(R.drawable.ic_del);
 
        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.LTGRAY);
        searchEditText.setHint("Search Here");
 
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
 
            @Override
            public boolean onQueryTextSubmit(String s) {
                Functions.toastString("Enter keyword", getBaseContext());
                return true;
            }
 
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });*/
 
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
}
