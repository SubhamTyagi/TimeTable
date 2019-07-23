package com.sunrain.timetablev4.thread;

import android.content.DialogInterface;

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
                ToastUtil.postShow("Please see the tutorial in more", true);
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
        if ((mLastVersionCode == 23 || mLastVersionCode == 24) && SharedPreUtils.getInt(SharedPreConstants.DOUBLE_WEEK,
                SharedPreConstants.DEFAULT_DOUBLE_WEEK) == 1) {
            if (mainActivity == null) {
                ToastUtil.postShow("Please check the one-week course configuration", true);
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
            ToastUtil.postShow("Please set the semester start date", true);
            return;
        }

        long endDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_END_DATE, 0);
        if (endDate == 0) {
            ToastUtil.postShow("Please set the semester end date", true);
            return;
        }

        final int week = SharedPreUtils.getInt(SharedPreConstants.SEMESTER_WEEK, SharedPreConstants.DEFAULT_SEMESTER_WEEK);
        int currentWeek = CalendarUtil.getCurrentWeek();
        if (currentWeek < 0 || currentWeek > week - 1) {
            ToastUtil.postShow("The current number of weeks has exceeded the total number of weeks in the semester", true);
            return;
        }

        if (TableDao.existsOutOfWeek(week - 1)) {
            if (mainActivity == null) {
                ToastUtil.postShow("There are more than the total number of weeks in the semester" + week + "周的课程", true);
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
            ToastUtil.postShow("Please add a course", true);
            return;
        }

        if (TableDao.isDataBaseEmpty()) {
            ToastUtil.postShow("Please add class time to the course", true);
            return;
        }

        if (SharedPreUtils.getInt(SharedPreConstants.DOUBLE_WEEK, SharedPreConstants.DEFAULT_DOUBLE_WEEK) == 0 && TableDao
                .existsDoubleWeek()) {
            ToastUtil.postShow("There are biweekly courses, but single and double week functions are not enabled", true);
        }
    }

    private void showDoubleWeekDialog(final MainActivity mainActivity) {
        new MessageDialog(mainActivity).setMessage("The single and double week functions have been optimized, and the rules for one week have changed. Please adjust the one-week course in the class schedule in time.")
                .setPositiveButton("I know", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .hideNegativeButton()
                .show();
    }

    private void showTutorialDialog(final MainActivity mainActivity) {
        new MessageDialog(mainActivity).setMessage("I suggest you check out the tutorial first.\n" +
                "Or check back later in more.")
                .setNegativeButton("shut down", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("View tutorial", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        WebUtil.gotoWeb(mainActivity, "http://timetable.gujin.tech/tutorial.html");
                    }
                })
                .show();
    }

    private void showOutOfWeekDialog(MainActivity mainActivity, int week) {
        new MessageDialog(mainActivity).setMessage("The total number of weeks in the current semester is" + week + "Week, there is more than class time" + week + "Weekly course, please pay attention to the processing.")
                .setPositiveButton("I know", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .hideNegativeButton()
                .show();
    }
}
