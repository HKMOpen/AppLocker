package com.balram.locker.utils;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;

import com.balram.locker.view.AppLocker;

import java.util.HashSet;


/**
 * Created by Balram Pandey 12/11/16.
 * hesk edited 2017.
 */
public abstract class Locker {
    public static final int ENABLE_PASSLOCK = 0;
    public static final int DISABLE_PASSLOCK = 1;
    public static final int CHANGE_PASSWORD = 2;
    public static final int UNLOCK_PASSWORD = 3;

    public static final String MESSAGE = "message";
    public static final String TYPE = "type";
    public static final String ICON = "spicon";
    public static final String PASSCODE_SYMBOL = "symbol";
    public static final String INTENT_FUNCTION_CLASS = "str_classname";
    public static final String INTENT_BOOL_CLOSE_CURRENT = "bool_close_x";
    public static final String INTENT_BUNDLE_DATA = "bundle_dat";
    public static final String INTENT_LABEL = "str_labelbut";

    public static final int DEFAULT_TIMEOUT = 0; // 2000ms

    protected int lockTimeOut;
    protected HashSet<String> ignoredActivities;

    public void setTimeout(int timeout) {
        this.lockTimeOut = timeout;
    }

    public Locker() {
        ignoredActivities = new HashSet<String>();
        lockTimeOut = DEFAULT_TIMEOUT;
    }

    public void addIgnoredActivity(Class<?> clazz) {
        String clazzName = clazz.getName();
        this.ignoredActivities.add(clazzName);
    }

    public void removeIgnoredActivity(Class<?> clazz) {
        String clazzName = clazz.getName();
        this.ignoredActivities.remove(clazzName);
    }

    public abstract void enable();

    public abstract void disable();

    public abstract boolean setPasscode(String passcode);

    public abstract boolean checkPasscode(String passcode);

    public abstract boolean isPasscodeSet();

    public static class Builder {
        Intent intent;

        public Builder(Intent g) {
            intent = g;
        }

        public Builder smartLock() {
            int type = AppLocker.getInstance().getAppLock().isPasscodeSet() ? Locker.DISABLE_PASSLOCK : Locker.ENABLE_PASSLOCK;
            intent.putExtra(Locker.TYPE, type);
            return this;
        }

        public Builder changePass() {
            intent.putExtra(Locker.TYPE, Locker.CHANGE_PASSWORD);
            return this;
        }

        public Builder changePass(String message) {
            intent.putExtra(Locker.TYPE, Locker.CHANGE_PASSWORD);
            intent.putExtra(Locker.MESSAGE, message);
            return this;
        }

        public Builder withPressFunc(String label, boolean closex, Class<? extends AppCompatActivity> class_name) {
            intent.putExtra(Locker.INTENT_FUNCTION_CLASS, class_name.getClass().getCanonicalName());
            intent.putExtra(Locker.INTENT_BOOL_CLOSE_CURRENT, closex);
            // intent.putExtra(Locker.INTENT_BUNDLE_DATA, intent_data);
            intent.putExtra(Locker.INTENT_LABEL, label);
            return this;
        }

        public Builder withPressFunc(String label, Class<? extends AppCompatActivity> class_name) {
            intent.putExtra(Locker.INTENT_FUNCTION_CLASS, class_name.getCanonicalName());
            intent.putExtra(Locker.INTENT_BOOL_CLOSE_CURRENT, true);
            // intent.putExtra(Locker.INTENT_BUNDLE_DATA, intent_data);
            intent.putExtra(Locker.INTENT_LABEL, label);
            return this;
        }

        public Builder withIcon(@DrawableRes int resIcon) {
            intent.putExtra(ICON, resIcon);
            return this;
        }

        public Builder withSymbol(String emoji_code) {
            intent.putExtra(PASSCODE_SYMBOL, emoji_code);
            return this;
        }

        public Builder withSymbol(int emoji_code) {
            intent.putExtra(PASSCODE_SYMBOL, getEmojiByUnicode(emoji_code));
            return this;
        }

        private String getEmojiByUnicode(int unicode) {
            return new String(Character.toChars(unicode));
        }

        /**
         * develop lock type
         *
         * @return the int
         */
        public int getType() {
            return intent.getExtras().getInt(Locker.TYPE);
        }

        /**
         * all finishing bundle data
         *
         * @return intent data
         */
        public Intent toBundle() {
            return intent;
        }
    }
}
