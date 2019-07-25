package com.sunrain.timetablev4.thread;

import android.content.DialogInterface;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.dao.CourseClassroomDao;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.activity.MainActivity;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.utils.CalendarUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.utils.WebUtil;

import java.lang.ref.WeakReference;

import tech.gujin.toast.ToastUtil;

public class DataCheckThread extends Thread {

    private final int mLastVersionCode;
    private final WeakReference<MainActivity> mMainActivityWeakReference;

    public DataCheckThread(MainActivity mainActivity, int lastVersionCode) {
        mMainActivityWeakReference = new WeakReference<>(mainActivity);
        mLastVersionCode = lastVersionCode;
    }

    @Override
    public void run() {
        final MainActivity mainActivity = mMainActivityWeakReference.get();

        if (mLastVersionCode == 0) {
            if (mainActivity == null) {
                ToastUtil.postShow(MyApplication.sContext.getResources().getString(R.string.see_tutorial_in_more), true);
                return;
            }

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showTutorialDialog(mainActivity);
                }
            });
            return;
        }

        // 23 Start joining biweekly, 25 changed biweekly rules
        if ((mLastVersionCode == 23 || mLastVersionCode == 24) && SharedPreUtils.getInt(SharedPreConstants.ALTERNATE_WEEK,
                SharedPreConstants.DEFAULT_DOUBLE_WEEK) == 1) {
            if (mainActivity == null) {
                ToastUtil.postShow(MyApplication.sContext.getResources().getString(R.string.plz_check_one_week_subject_config), true);
                return;
            }

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDoubleWeekDialog(mainActivity);
                }
            });
            return;
        }

        // 学期检查
        long startDateTime = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_START_DATE, 0);
        if (startDateTime == 0) {
            ToastUtil.postShow(MyApplication.sContext.getResources().getString(R.string.set_sem_start_date), true);
            return;
        }

        long endDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_END_DATE, 0);
        if (endDate == 0) {
            ToastUtil.postShow(MyApplication.sContext.getResources().getString(R.string.set_sem_end_date), true);
            return;
        }

        final int week = SharedPreUtils.getInt(SharedPreConstants.DURATION_WEEK, SharedPreConstants.DEFAULT_SEMESTER_WEEK);
        int currentWeek = CalendarUtil.getCurrentWeek();
        if (currentWeek < 0 || currentWeek > week - 1) {
            ToastUtil.postShow(MyApplication.sContext.getResources().getString(R.string.exceed_the_total_number_of_week), true);
            return;
        }

        if (TableDao.existsOutOfWeek(week - 1)) {
            if (mainActivity == null) {
                ToastUtil.postShow(mainActivity.getResources().getString(R.string.more_than_total_week) + week + mainActivity.getResources().getString(R.string.weekly_subject), true);
                return;
            }

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showOutOfWeekDialog(mainActivity, week);
                }
            });
            return;
        }

        // Course inspection
        if (CourseClassroomDao.isDataBaseEmpty()) {
            ToastUtil.postShow(mainActivity.getResources().getString(R.string.plz_add_subject), true);
            return;
        }

        if (TableDao.isDataBaseEmpty()) {
            ToastUtil.postShow(mainActivity.getResources().getString(R.string.plz_add_class_time_subject), true);

            return;
        }

        if (SharedPreUtils.getInt(SharedPreConstants.ALTERNATE_WEEK, SharedPreConstants.DEFAULT_DOUBLE_WEEK) == 0 && TableDao
                .existsDoubleWeek()) {
            ToastUtil.postShow(mainActivity.getResources().getString(R.string.alternate_week_subject_available), true);
        }
    }

    private void showDoubleWeekDialog(final MainActivity mainActivity) {
        new MessageDialog(mainActivity).setMessage(mainActivity.getResources().getString(R.string.alternate_week_function_optimized))
                .setPositiveButton(mainActivity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .hideNegativeButton()
                .show();
    }

    private void showTutorialDialog(final MainActivity mainActivity) {
        new MessageDialog(mainActivity).setMessage(mainActivity.getResources().getString(R.string.i_suggest_you_tutorial))
                .setNegativeButton(mainActivity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(mainActivity.getResources().getString(R.string.view_tutorial), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        WebUtil.gotoWeb(mainActivity, mainActivity.getResources().getString(R.string.tutorial_url));
                    }
                })
                .show();
    }

    private void showOutOfWeekDialog(MainActivity mainActivity, int week) {
        new MessageDialog(mainActivity).setMessage(String.format(mainActivity.getResources().getString(R.string.total_number_of_exceed), week, week))
                .setPositiveButton(mainActivity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .hideNegativeButton()
                .show();
    }
}
