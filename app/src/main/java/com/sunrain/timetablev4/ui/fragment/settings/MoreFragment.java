package com.sunrain.timetablev4.ui.fragment.settings;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.client.android.CaptureActivity;
import com.sunrain.timetablev4.BuildConfig;
import com.sunrain.timetablev4.R;
import com.sunrain.timetablev4.application.MyApplication;
import com.sunrain.timetablev4.base.BaseFragment;
import com.sunrain.timetablev4.bean.ClassBean;
import com.sunrain.timetablev4.dao.CourseClassroomDao;
import com.sunrain.timetablev4.dao.TableDao;
import com.sunrain.timetablev4.manager.WallpaperManager;
import com.sunrain.timetablev4.manager.permission.PermissionManager;
import com.sunrain.timetablev4.thread.input_course.InputCourseAnalysisThread;
import com.sunrain.timetablev4.thread.input_course.InputCourseSaveThread;
import com.sunrain.timetablev4.ui.activity.CropActivity;
import com.sunrain.timetablev4.ui.dialog.DonationDialog;
import com.sunrain.timetablev4.ui.dialog.InputCourseDialog;
import com.sunrain.timetablev4.ui.dialog.MessageDialog;
import com.sunrain.timetablev4.ui.dialog.ShareClassDialog;
import com.sunrain.timetablev4.utils.ClipboardUtil;
import com.sunrain.timetablev4.utils.WebUtil;
import com.sunrain.timetablev4.view.table.TableData;

import java.util.List;

import tech.gujin.toast.ToastUtil;

public class MoreFragment extends BaseFragment implements View.OnClickListener, PermissionManager.OnRequestPermissionsListener, View
        .OnLongClickListener {

    private final int REQUEST_BACKGROUND_PICK_IMG = 1;
    private final int REQUEST_INPUT_COURSE = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final int REQUEST_BACKGROUND_CROP_IMG = 3;

    private final int REQUEST_PERMISSION_BACKGROUND = 1;
    private final int REQUEST_PERMISSION_INPUT_COURSE = 2;
    private final int REQUEST_PERMISSION_SAVE_QR_CODE = 3;

    private PermissionManager mPermissionManager;

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    protected void initView(View view, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mPermissionManager = PermissionManager.Factory.get(this, this);
        setListener();
    }

    private void setListener() {
        View view = getView();
        view.findViewById(R.id.btn_background).setOnClickListener(this);
        view.findViewById(R.id.btn_tutorial).setOnClickListener(this);
        view.findViewById(R.id.btn_input_course).setOnClickListener(this);
        view.findViewById(R.id.btn_share_course).setOnClickListener(this);
        view.findViewById(R.id.btn_clear_course).setOnClickListener(this);
        view.findViewById(R.id.btn_github).setOnClickListener(this);
        view.findViewById(R.id.btn_version).setOnClickListener(this);
        view.findViewById(R.id.btn_praise).setOnClickListener(this);
        view.findViewById(R.id.btn_donation).setOnClickListener(this);
        view.findViewById(R.id.btn_feedback).setOnClickListener(this);

        view.findViewById(R.id.btn_donation).setOnLongClickListener(this);
        view.findViewById(R.id.btn_feedback).setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_donation:
                showDonationDialog();
                break;
            case R.id.btn_praise:
                goPraise();
                break;
            case R.id.btn_background:
                checkBackGroundPermission();
                break;
            case R.id.btn_share_course:
                checkSaveQrCodePermission();
                break;
            case R.id.btn_input_course:
                checkInputCoursePermission();
                break;
            case R.id.btn_clear_course:
                showClearCourseDialog();
                break;
            case R.id.btn_version:
                ToastUtil.show(BuildConfig.VERSION_NAME);
                break;
            case R.id.btn_github:
                WebUtil.gotoWeb(mActivity, getString(R.string.repo_url));
                break;
            case R.id.btn_feedback:
                showEmailDialog();
                break;
            case R.id.btn_tutorial:
                WebUtil.gotoWeb(mActivity, getString(R.string.tutorial_url));
                break;
        }
    }

    private void showEmailDialog() {
        String message = getString(R.string.dialog_feedback, BuildConfig.VERSION_NAME);
        MessageDialog messageDialog = new MessageDialog(mActivity).setMessage(message);
        messageDialog.setPositiveButton(getString(R.string.copy_email_address), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ClipboardUtil.writeToClipboard(getString(R.string.mailbox), getString(R.string.email_id));
                ToastUtil.show(getString(R.string.copied));
            }
        });

        final Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + getString(R.string.email_id)));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_welcome));
        intent.putExtra(Intent.EXTRA_TEXT, getFeedBackInfo());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(MyApplication.sContext.getPackageManager()) != null) {
            messageDialog.setNegativeButton(getString(R.string.open_the_mail_app), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(intent);
                }
            });
        } else {
            messageDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        messageDialog.setTextGravity(Gravity.START).show();
    }

    private String getFeedBackInfo() {
        return getString(R.string.version_) + BuildConfig.VERSION_NAME + "\n\n";
    }

    private void showClearCourseDialog() {
        new MessageDialog(mActivity).setMessage(getString(R.string.clear_all_data)).setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton(getString(R.string.empty), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                TableDao.clearInBackground();
                CourseClassroomDao.clearInBackground();
                TableData.getInstance().setContentChange();
                ToastUtil.show(getString(R.string.cleard));
            }
        }).show();
    }

    private void checkTableDataValid() {
        if (TableDao.isDataBaseEmpty()) {
            ToastUtil.show(getString(R.string.empty_class_schedule));
            return;
        }
        new ShareClassDialog(mActivity).show();
    }

    private void checkInputCoursePermission() {
        mPermissionManager
                .checkPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_INPUT_COURSE, 0, R.string.permission_camera_message);
    }

    private void checkSaveQrCodePermission() {
        mPermissionManager
                .checkPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_SAVE_QR_CODE, 0, R.string
                        .permission_write_message_qr_code);
    }

    private void goPraise() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + mActivity.getPackageName()));
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            ToastUtil.show(getString(R.string.market_load_failed));
        }
    }

    private void showDonationDialog() {
        new DonationDialog(mActivity).show();
    }

    @Override
    public void onPermissionGranted(int requestCode) {
        if (requestCode == REQUEST_PERMISSION_BACKGROUND) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                    REQUEST_BACKGROUND_PICK_IMG);
        } else if (requestCode == REQUEST_PERMISSION_INPUT_COURSE) {
            startActivityForResult(new Intent(mActivity, CaptureActivity.class), REQUEST_INPUT_COURSE);
        } else if (requestCode == REQUEST_PERMISSION_SAVE_QR_CODE) {
            checkTableDataValid();
        }
    }

    @Override
    public void onPermissionDenied(int requestCode, boolean neverAskAgainChecked) {
        if (requestCode == REQUEST_PERMISSION_BACKGROUND) {
            ToastUtil.show(R.string.permission_read_fail_background);
        } else if (requestCode == REQUEST_PERMISSION_INPUT_COURSE) {
            ToastUtil.show(R.string.permission_camera_fail);
        } else if (requestCode == REQUEST_PERMISSION_SAVE_QR_CODE) {
            ToastUtil.show(R.string.permission_write_fail_qr_code);
        }
    }

    public void importCourseFinished() {
        ToastUtil.show(getString(R.string.successfully_import));
        TableData.getInstance().setContentChange();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BACKGROUND_PICK_IMG && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(mActivity, CropActivity.class);
            intent.putExtra("imageUrl", data.getData());
            startActivityForResult(intent, REQUEST_BACKGROUND_CROP_IMG);
        } else if (requestCode == REQUEST_BACKGROUND_CROP_IMG && resultCode == Activity.RESULT_OK) {
            WallpaperManager.getInstance().refreshWallpaperInBackground(getActivity());
        } else if (requestCode == REQUEST_INPUT_COURSE && resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra("result");
            if (TextUtils.isEmpty(result)) {
                ToastUtil.show(getString(R.string.import_failed));
                return;
            }
            new InputCourseAnalysisThread(this, result).start();
        }
    }

    public void showImportClassDialog(final List<ClassBean> list) {
        new InputCourseDialog(mActivity, list).setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton(getString(R.string.import_), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ToastUtil.show(getString(R.string.importing));
                new InputCourseSaveThread(MoreFragment.this, list).start();
            }
        }).show();
    }

    private void checkBackGroundPermission() {
        mPermissionManager
                .checkPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_BACKGROUND, 0, R.string.permission_read_message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.btn_donation:
                ToastUtil.show(getString(R.string.msg_1), true);
                return true;
            case R.id.btn_feedback:
                ToastUtil.show(getString(R.string.msg_2));
                return true;
        }
        return false;
    }
}
