package com.balram.locker.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.balram.locker.R;
import com.balram.locker.main.AppLockerActivity;
import com.balram.locker.utils.Encryptor;
import com.balram.locker.utils.Locker;

import io.github.rockerhieu.emojiconize.Emojiconize;

import static com.balram.locker.utils.Locker.INTENT_BOOL_CLOSE_CURRENT;
import static com.balram.locker.utils.Locker.INTENT_BUNDLE_DATA;

public class LockActivity extends AppLockerActivity {
    public static final String TAG = "LockActivity";

    private int type = -1;
    private String oldPasscode = null;

    protected EditText codeField1 = null;
    protected EditText codeField2 = null;
    protected EditText codeField3 = null;
    protected EditText codeField4 = null;
    protected InputFilter[] filters = null;
    protected TextView tvMessage = null;
    protected Activity mActivity;
    private String[] code = new String[4];
    boolean erase_bool = true;
    @DrawableRes
    private int icon_special = -1;
    private String special_char;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.page_passcode);
        mActivity = this;
        tvMessage = (TextView) findViewById(R.id.tv_message);
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String message = extras.getString(Locker.MESSAGE);
            if (message != null) {
                tvMessage.setText(message);
            }
            type = extras.getInt(Locker.TYPE, -1);
            erase_bool = extras.getString(Locker.INTENT_FUNCTION_CLASS, "").isEmpty();
            icon_special = extras.getInt(Locker.ICON, -1);
            special_char = extras.getString(Locker.PASSCODE_SYMBOL, "");
        }

        filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(1);
        // filters[1] = numberFilter;

        codeField1 = (EditText) findViewById(R.id.passcode_1);
        setupEditText(codeField1);

        codeField2 = (EditText) findViewById(R.id.passcode_2);
        setupEditText(codeField2);

        codeField3 = (EditText) findViewById(R.id.passcode_3);
        setupEditText(codeField3);

        codeField4 = (EditText) findViewById(R.id.passcode_4);
        setupEditText(codeField4);

        // setup the keyboard
        ((Button) findViewById(R.id.button0)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button1)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button2)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button3)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button4)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button5)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button6)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button7)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button8)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button9)).setOnClickListener(btnListener);
        ((Button) findViewById(R.id.button_clear))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearFields();
                    }
                });

        if (icon_special != -1) {
            ((ImageView) findViewById(R.id.top_icon)).setImageDrawable(ContextCompat.getDrawable(this, icon_special));
        }

        Button button_erase_tp = (Button) findViewById(R.id.button_erase);
        if (erase_bool) {
            button_erase_tp
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onDeleteKey();
                        }
                    });
        } else {
            button_erase_tp.setText(extras.getString(Locker.INTENT_LABEL, "x"));
            button_erase_tp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Class<?> object = Class.forName(extras.getString(Locker.INTENT_FUNCTION_CLASS));
                        boolean close_x = extras.getBoolean(INTENT_BOOL_CLOSE_CURRENT, true);
                        openIntent(object, close_x, extras.getBundle(INTENT_BUNDLE_DATA));
                    } catch (ClassNotFoundException e) {
                        Log.i(TAG, "please locate correct appcompat activity for intent openings");
                    }
                }
            });
        }


        overridePendingTransition(R.anim.slide_up, R.anim.zero);

        switch (type) {

            case Locker.DISABLE_PASSLOCK:
                this.setTitle("Disable Pin");
                break;
            case Locker.ENABLE_PASSLOCK:
                this.setTitle("Enable Pin");
                break;
            case Locker.CHANGE_PASSWORD:
                this.setTitle("Change Pin");
                break;
            case Locker.UNLOCK_PASSWORD:
                this.setTitle("Unlock Pin");
                break;
        }
    }

    public int getType() {
        return type;
    }

    protected void onPasscodeInputed() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < code.length; i++) {
            if (code[i] != null)
                result.append(code[i]);
        }
        String passLock = result.toString();
        codeField1.setText("");
        codeField2.setText("");
        codeField3.setText("");
        codeField4.setText("");
        codeField1.requestFocus();

        switch (type) {

            case Locker.DISABLE_PASSLOCK:
                if (AppLocker.getInstance().getAppLock().checkPasscode(passLock)) {
                    setResult(RESULT_OK);
                    AppLocker.getInstance().getAppLock().setPasscode(null);
                    finish();
                } else {
                    onPasscodeError();
                }
                break;

            case Locker.ENABLE_PASSLOCK:
                if (oldPasscode == null) {
                    tvMessage.setText(R.string.reenter_passcode);
                    oldPasscode = passLock;
                } else {
                    if (passLock.equals(oldPasscode)) {
                        setResult(RESULT_OK);
                        AppLocker.getInstance().getAppLock()
                                .setPasscode(passLock);
                        finish();
                    } else {
                        oldPasscode = null;
                        tvMessage.setText(R.string.enter_passcode);
                        onPasscodeError();
                    }
                }
                break;

            case Locker.CHANGE_PASSWORD:
                if (AppLocker.getInstance().getAppLock().checkPasscode(passLock)) {
                    tvMessage.setText(R.string.enter_passcode);
                    type = Locker.ENABLE_PASSLOCK;
                } else {
                    onPasscodeError();
                }
                break;

            case Locker.UNLOCK_PASSWORD:
                if (AppLocker.getInstance().getAppLock().checkPasscode(passLock)) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    onPasscodeError();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (type == Locker.UNLOCK_PASSWORD) {
            // back to home screen
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    protected void setupEditText(EditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
        editText.setFilters(filters);
        editText.setOnTouchListener(touchListener);
        Emojiconize.view(editText).go();
        // editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            onDeleteKey();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void openIntent(Class intent_class, boolean close_x, @Nullable Bundle dat) {
        Intent intent = new Intent(this, intent_class);
        if (dat != null) {
            intent.putExtras(dat);
        }
        this.startActivity(intent);
        if (close_x) {
            finish();
        }
    }

    private void onDeleteKey() {
        if (codeField1.isFocused()) {

        } else if (codeField2.isFocused()) {
            codeField1.requestFocus();
            codeField1.setText("");
        } else if (codeField3.isFocused()) {
            codeField2.requestFocus();
            codeField2.setText("");
        } else if (codeField4.isFocused()) {
            codeField3.requestFocus();
            codeField3.setText("");
        }
    }

    private OnClickListener btnListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int currentValue = -1;
            int id = view.getId();
            if (id == R.id.button0) {
                currentValue = 0;
            } else if (id == R.id.button1) {
                currentValue = 1;
            } else if (id == R.id.button2) {
                currentValue = 2;
            } else if (id == R.id.button3) {
                currentValue = 3;
            } else if (id == R.id.button4) {
                currentValue = 4;
            } else if (id == R.id.button5) {
                currentValue = 5;
            } else if (id == R.id.button6) {
                currentValue = 6;
            } else if (id == R.id.button7) {
                currentValue = 7;
            } else if (id == R.id.button8) {
                currentValue = 8;
            } else if (id == R.id.button9) {
                currentValue = 9;
            } else {
            }

            // set the value and move the focus
            String internal_st = String.valueOf(currentValue);
            String currentValueString = special_char.isEmpty() ? "*" : special_char;
            if (codeField1.isFocused()) {
                codeField1.setText(currentValueString);
                code[0] = internal_st;
                codeField2.requestFocus();
                codeField2.setText("");
            } else if (codeField2.isFocused()) {
                codeField2.setText(currentValueString);
                code[1] = internal_st;
                codeField3.requestFocus();
                codeField3.setText("");
            } else if (codeField3.isFocused()) {
                codeField3.setText(currentValueString);
                code[2] = internal_st;
                codeField4.requestFocus();
                codeField4.setText("");
            } else if (codeField4.isFocused()) {
                codeField4.setText(currentValueString);
                code[3] = internal_st;
            }

            if (!passFilled()) {
                onPasscodeInputed();
            }
        }
    };

    private boolean passFilled() {
        return codeField1.getText().toString().isEmpty() ||
                codeField3.getText().toString().isEmpty() ||
                codeField2.getText().toString().isEmpty() ||
                codeField4.getText().toString().isEmpty();
    }

    protected void onPasscodeError() {
        Encryptor.snackPeak(mActivity, getString(R.string.passcode_wrong));

        Thread thread = new Thread() {
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(
                        LockActivity.this, R.anim.shake);
                findViewById(R.id.ll_applock).startAnimation(animation);
                codeField1.setText("");
                codeField2.setText("");
                codeField3.setText("");
                codeField4.setText("");
                codeField1.requestFocus();
            }
        };
        runOnUiThread(thread);
    }

    private InputFilter numberFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source.length() > 1) {
                return "";
            }

            if (source.length() == 0) // erase
            {
                return null;
            }

            try {
                int number = Integer.parseInt(source.toString());
                if ((number >= 0) && (number <= 9))
                    return String.valueOf(number);
                else
                    return "";
            } catch (NumberFormatException e) {
                return "";
            }
        }
    };

    private OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.performClick();
            clearFields();
            return false;
        }
    };

    private void clearFields() {
        codeField1.setText("");
        codeField2.setText("");
        codeField3.setText("");
        codeField4.setText("");
        codeField1.postDelayed(new Runnable() {

            @Override
            public void run() {
                codeField1.requestFocus();
            }
        }, 200);
    }
}