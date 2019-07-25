package com.sunrain.timetablev4.ui.fragment.settings;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.adapter.course_management.ClassTimeAdapter;
import com.sunrain.timetablev4.adapter.course_management.CourseClassroomAdapter;
import com.sunrain.timetablev4.base.BaseFragment;
import com.sunrain.timetablev4.bean.CourseClassroomBean;
import com.sunrain.timetablev4.constants.SharedPreConstants;
import com.sunrain.timetablev4.dao.CourseClassroomDao;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.ui.dialog.CourseClassroomEditDialog;
import com.sunrain.timetablev4.ui.dialog.CourseClassroomLongClickDialog;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.ui.fragment.SettingsFragment;
import com.sunrain.timetablev4.utils.DensityUtil;
import com.sunrain.timetablev4.utils.SharedPreUtils;
import com.sunrain.timetablev4.utils.SystemUiUtil;
import com.sunrain.timetablev4.view.table.TableData;

import java.util.List;

import tech.gujin.toast.ToastUtil;

public class CourseManagementFragment extends BaseFragment implements ViewTreeObserver.OnGlobalLayoutListener, View.OnClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private View mRootView;
    private ListView mLvCourseClassroom;
    private ListView mLvClassTime;
    private EditText mEtCourse;
    private EditText mEtClassroom;
    private List<CourseClassroomBean> mCourseClassroomList;
    private CourseClassroomAdapter mCourseClassroomAdapter;
    private ClassTimeAdapter mClassTimeAdapter;
    private int mSmoothOffset;
    private boolean needContentRefresh;
    private boolean needLayoutRefresh;
    private OnContentChangedListener mOnContentChangedListener;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_course_management, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
        mRootView = view.findViewById(R.id.ll_root);
        mLvCourseClassroom = view.findViewById(R.id.lv_course_classroom);
        mLvClassTime = view.findViewById(R.id.lv_class_time);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        initCourseClassroomListView();
        initClassTimeListView();
        setListener();
        mSmoothOffset = DensityUtil.dip2Px(60);
        mOnContentChangedListener = new OnContentChangedListener();
        TableData.getInstance().registerOnTableDataChangedListener(mOnContentChangedListener);
    }

    private void initCourseClassroomListView() {
        mCourseClassroomList = CourseClassroomDao.getAll();
        mCourseClassroomAdapter = new CourseClassroomAdapter(mCourseClassroomList);
        mLvCourseClassroom.setAdapter(mCourseClassroomAdapter);

        View view = View.inflate(mActivity, R.layout.footer_course_classroom_listview, null);
        mEtCourse = view.findViewById(R.id.et_course);
        mEtClassroom = view.findViewById(R.id.et_classroom);
        view.findViewById(R.id.btn_add_course_classroom).setOnClickListener(this);
        mLvCourseClassroom.addFooterView(view, null, false);
    }

    private void initClassTimeListView() {
        mClassTimeAdapter = new ClassTimeAdapter(mActivity, mLvClassTime);
        mClassTimeAdapter.setDoubleWeekEnabled(SharedPreUtils.getInt(SharedPreConstants.ALTERNATE_WEEK, SharedPreConstants
                .DEFAULT_DOUBLE_WEEK) == 1);
        mLvClassTime.setAdapter(mClassTimeAdapter);

        View view = View.inflate(mActivity, R.layout.footer_class_time_listview, null);
        view.findViewById(R.id.btn_add_class_time).setOnClickListener(this);
        mLvClassTime.addFooterView(view, null, false);
    }

    private void setListener() {
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mLvCourseClassroom.setOnItemClickListener(this);
        mLvCourseClassroom.setOnItemLongClickListener(this);
    }

    @Override
    public void onGlobalLayout() {
        // 软键盘隐藏时 重新设置全屏模式
        Rect r = new Rect();
        mRootView.getWindowVisibleDisplayFrame(r);
        int screenHeight = mRootView.getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;
        if (keypadHeight < screenHeight * 0.15) {
            if (mActivity != null) {
                SystemUiUtil.setSystemUi(mActivity.getWindow().getDecorView());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_course_classroom:
                checkValid();
                break;
            case R.id.btn_add_class_time:
                mClassTimeAdapter.showAddDialog();
                break;
        }
    }

    private void checkValid() {

        if (isSemesterInvalid()) {
            return;
        }

        String course = mEtCourse.getText().toString();
        String classroom = mEtClassroom.getText().toString();

        if (TextUtils.isEmpty(course)) {
            ToastUtil.show(getResources().getString(R.string.subject_name_cant_empty));
            return;
        }

        if (TextUtils.isEmpty(classroom)) {
            ToastUtil.show(getResources().getString(R.string.class_loc_cant_empty));
            return;
        }

        CourseClassroomBean bean = new CourseClassroomBean(course, classroom);
        if (CourseClassroomDao.exists(bean)) {
            ToastUtil.show(getResources().getString(R.string.already_have_the_same_entry));
            return;
        }

        save(bean);
    }

    private boolean isSemesterInvalid() {
        long startDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_START_DATE, 0);
        long endDate = SharedPreUtils.getLong(SharedPreConstants.SEMESTER_END_DATE, 0);
        if (startDate == 0 || endDate == 0) {
            new MessageDialog(mActivity).setMessage(getResources().getString(R.string.set_sem_date_first))
                    .setNegativeButton(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getResources().getString(R.string.go_to_set), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            SettingsFragment settingsFragment = (SettingsFragment) getParentFragment();
                            if (settingsFragment != null) {
                                settingsFragment.showSemesterFragment();
                            } else {
                                ToastUtil.show(getResources().getString(R.string.jump_failed_plz_set_it_manually));
                            }
                        }
                    })
                    .show();
            return true;
        }
        return false;
    }

    private void save(CourseClassroomBean bean) {
        CourseClassroomDao.insertInBackground(bean);
        mCourseClassroomList.add(bean);
        mCourseClassroomAdapter.setClickPosition(mCourseClassroomAdapter.getCount() - 1);
        mCourseClassroomAdapter.notifyDataSetChanged();
        mClassTimeAdapter.setCourseClassroom(bean);
        if (mLvClassTime.getVisibility() == View.INVISIBLE) {
            mLvClassTime.setVisibility(View.VISIBLE);
        }

        mLvCourseClassroom.smoothScrollByOffset(mSmoothOffset);

        mEtCourse.setText("");
        mEtClassroom.setText("");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mCourseClassroomAdapter.getClickPosition() == position) {
            return;
        }

        CourseClassroomBean classroomBean = mCourseClassroomAdapter.getItem(position);
        mClassTimeAdapter.setCourseClassroom(classroomBean);
        mCourseClassroomAdapter.setClickPosition(position);
        mCourseClassroomAdapter.notifyDataSetChanged();
        if (mLvClassTime.getVisibility() == View.INVISIBLE) {
            mLvClassTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.lv_course_classroom:
                if (position == mCourseClassroomAdapter.getCount()) {
                    return false;
                }
                showCourseClassroomLongClickDialog(mCourseClassroomAdapter.getItem(position));
                return true;
        }
        return false;
    }

    private void showCourseClassroomLongClickDialog(final CourseClassroomBean bean) {
        new CourseClassroomLongClickDialog(mActivity, bean, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (which == 0) {//edit
                    showCourseClassroomEditDialog(bean);
                } else if (which == 1) {//delete
                    showDeleteCourseClassroomDialog(bean);
                }
            }
        }).show();
    }

    private void showCourseClassroomEditDialog(final CourseClassroomBean bean) {
        mEtCourse.clearFocus();
        mEtClassroom.clearFocus();

        final CourseClassroomEditDialog editDialog = new CourseClassroomEditDialog(mActivity, bean)
                .setNegativeButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        editDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final String course = editDialog.getCourse();
                final String classroom = editDialog.getClassroom();

                if (TextUtils.isEmpty(course)) {
                    ToastUtil.show(getResources().getString(R.string.subject_name_cant_empty));
                    return;
                }

                if (TextUtils.isEmpty(classroom)) {
                    ToastUtil.show(R.string.class_loc_cant_empty);
                    return;
                }

                if (course.equals(bean.course) && classroom.equals(bean.classroom)) {
                    dialog.dismiss();
                    return;
                }

                CourseClassroomBean newBean = new CourseClassroomBean(course, classroom);

                if (CourseClassroomDao.exists(newBean)) {
                    ToastUtil.show(getResources().getString(R.string.already_have_the_saame_entry));
                } else {
                    dialog.dismiss();
                    updateCourseClassroom(bean, newBean);
                }

            }
        }).show();
    }

    private void updateCourseClassroom(CourseClassroomBean oldBean, CourseClassroomBean newBean) {
        CourseClassroomDao.update(oldBean, newBean);
        TableDao.update(oldBean, newBean);
        int index = mCourseClassroomList.indexOf(oldBean);
        mCourseClassroomList.remove(index);
        mCourseClassroomList.add(index, newBean);
        mCourseClassroomAdapter.notifyDataSetChanged();
        TableData.getInstance().setContentChange();
    }

    private void showDeleteCourseClassroomDialog(final CourseClassroomBean bean) {
        new MessageDialog(mActivity).setMessage(getString(R.string.delete) + bean.course + " " + bean.classroom + getResources().getString(R.string.subject_q))
                .setNegativeButton(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        CourseClassroomDao.deleteInBackground(bean);
                        TableDao.deleteInBackground(bean);
                        mCourseClassroomList.remove(bean);
                        mCourseClassroomAdapter.notifyDataSetChanged();
                        mLvClassTime.setVisibility(View.INVISIBLE);
                        TableData.getInstance().setContentChange();
                    }
                })
                .show();
    }

    private class OnContentChangedListener implements TableData.OnTableDataChangedListener {

        @Override
        public void onContentChange() {
            needContentRefresh = true;
        }

        @Override
        public void onLayoutChange() {
            needLayoutRefresh = true;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            return;
        }

        if (needContentRefresh) {
            needContentRefresh = false;
            mCourseClassroomList.clear();
            mCourseClassroomList.addAll(CourseClassroomDao.getAll());
            mCourseClassroomAdapter.setClickPosition(-1);
            mCourseClassroomAdapter.notifyDataSetChanged();
            mLvClassTime.setVisibility(View.INVISIBLE);
            mClassTimeAdapter.setDoubleWeekEnabled(SharedPreUtils.getInt(SharedPreConstants.ALTERNATE_WEEK, SharedPreConstants
                    .DEFAULT_DOUBLE_WEEK) == 1);
            mClassTimeAdapter.setDialogNull();
        }

        if (needLayoutRefresh) {
            needLayoutRefresh = false;
            mClassTimeAdapter.setDialogNull();
        }
    }

    @Override
    public void onDestroyView() {
        TableData.getInstance().unregisterOnTableDataChangedListener(mOnContentChangedListener);
        super.onDestroyView();
    }
}
