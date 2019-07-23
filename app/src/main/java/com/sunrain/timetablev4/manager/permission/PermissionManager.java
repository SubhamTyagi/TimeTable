package com.sunrain.timetablev4.manager.permission;

import android.app.Activity;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

public interface PermissionManager {


    void checkPermission(String[] permissions, int requestCode, @StringRes int hintMessageId, @StringRes int messageId);

    /**
     * @param hintMessage If the permission has been denied, the reminder copy is popped up.
     * @param message     Prompt to the user before applying permission to the system
     */
    void checkPermission(String[] permissions, int requestCode, String hintMessage, String message);

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    interface OnRequestPermissionsListener {

        void onPermissionGranted(int requestCode);

        void onPermissionDenied(int requestCode, boolean neverAskAgainChecked);
    }

    class Factory {

        public static PermissionManager get(Activity activity, PermissionManager.OnRequestPermissionsListener listener) {
            return new ActivityPermissionManager(activity, listener);
        }

        public static PermissionManager get(Fragment fragment, PermissionManager.OnRequestPermissionsListener listener) {
            return new FragmentPermissionManager(fragment, listener);
        }
    }
}