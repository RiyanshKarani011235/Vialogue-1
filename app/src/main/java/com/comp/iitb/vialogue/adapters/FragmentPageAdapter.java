package com.comp.iitb.vialogue.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.fragments.CreateVideos;
import com.comp.iitb.vialogue.fragments.CreateVideosMark2;
import com.comp.iitb.vialogue.fragments.DummyFragment;
import com.comp.iitb.vialogue.fragments.Home;
import com.comp.iitb.vialogue.fragments.UserAccount;
import com.comp.iitb.vialogue.fragments.ViewVideos;

/**
 * Created by shubh on 09-01-2017.
 */
//Tab Manager Adapter
public class FragmentPageAdapter extends FragmentPagerAdapter {
    public static final int PAGE_COUNT = 4;
    public static final int HOME = 0;
    public static final int CREATE_PROJECT = 2;
    public static final int VIEW_VIDEOS = 1;
    public static final int USER_ACCOUNT = 3;

    private Context context;
    private FragmentManager mFragmentManager;

    public FragmentPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
        mFragmentManager = fragmentManager;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        //Generate Fragment based on Positions

        switch (position) {
            case HOME:
                return Home.newInstance();
            case VIEW_VIDEOS:
                return ViewVideos.newInstance("")/*DummyFragment.newInstance()*/;
            case CREATE_PROJECT:
                return CreateVideosMark2.newInstance();
//                return CreateVideos.newInstance();
            case USER_ACCOUNT:
                return UserAccount.newInstance();
            default :
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return null;
    }
}