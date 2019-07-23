package com.sunrain.timetablev4.ui.activity;

import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.Nullable;
import com.sunrain.timetablev4.BuildConfig;
import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.base.BaseActivity;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.manager.CrashHandler;
import com.sunrain.timetablev4.manager.FragmentChanger;
import com.sunrain.timetablev4.manager.WallpaperManager;
import com.sunrain.timetablev4.thread.DataCheckThread;
import com.sunrain.timetablev4.ui.fragment.CourseFragment;
import com.sunrain.timetablev4.ui.fragment.SettingsFragment;
import com.sunrain.timetablev4.utils.ChannelHelper;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.view.DrawerArrowDrawable;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private FragmentChanger mFragmentChanger;

    private ImageButton mImgBtnSettings;
    private DrawerArrowDrawable mArrow;
    private Bundle mSavedInstanceState;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mImgBtnSettings = findViewById(R.id.imgBtn_settings);
    }

    @Override
    protected void initData(@Nullable Bundle savedInstanceState) {

        CrashHandler.getInstance().init();
        setBackground();
        mSavedInstanceState = savedInstanceState;
        initFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Looper.myQueue().addIdleHandler(new ResumeIdleHandler());
    }

    private void setBackground() {
        WallpaperManager.getInstance().refreshWallpaperInBackground(this);
    }


    private void initFragment() {
        mFragmentChanger = new FragmentChanger(getSupportFragmentManager(), R.id.fl_main);
        if (mSavedInstanceState != null) {
            mFragmentChanger.onRestoreInstanceState(mSavedInstanceState);
        } else {
            mFragmentChanger.showFragment(CourseFragment.class);
        }
    }

    private void setListener() {
        mImgBtnSettings.setOnClickListener(this);
    }

    private void initArrow() {
        mArrow = new DrawerArrowDrawable();
        mArrow.setAnimationListener(new DrawerArrowDrawable.AnimationListener() {
            @Override
            public void onAnimationFinish() {
                mImgBtnSettings.setEnabled(true);
            }
        });
        mImgBtnSettings.setImageDrawable(mArrow);

        // 注意这里的equals条件和changeFragment()方法中的条件相反
        if (mSavedInstanceState != null && SettingsFragment.class.getSimpleName().equals(mFragmentChanger.getLastFragmentName())) {
            mArrow.startArrowAnim();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgBtn_settings) {
            mImgBtnSettings.setEnabled(false);
            changeFragment();
        }
    }

    private void changeFragment() {
        if (CourseFragment.class.getSimpleName().equals(mFragmentChanger.getLastFragmentName())) {
            mFragmentChanger.showFragment(SettingsFragment.class);
            mArrow.startArrowAnim();
        } else {
            mFragmentChanger.showFragment(CourseFragment.class);
            mArrow.startHamburgerAnim();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mFragmentChanger != null) {
            mFragmentChanger.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    private class ResumeIdleHandler implements MessageQueue.IdleHandler {

        @Override
        public boolean queueIdle() {
            initArrow();
            setListener();
            int lastVersionCode = SharedPreUtils.getInt(SharedPreConstants.VERSION_CODE, 0);
            new DataCheckThread(MainActivity.this, lastVersionCode).start();
            if (lastVersionCode != BuildConfig.VERSION_CODE) {
                SharedPreUtils.putInt(SharedPreConstants.VERSION_CODE, BuildConfig.VERSION_CODE);
            }
            return false;
        }
    }
}
