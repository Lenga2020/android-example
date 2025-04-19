package com.lena.android.model.phone;

import android.content.Context;

import androidx.annotation.NonNull;

import com.lena.android.R;
import com.lena.android.model.Phone;
import com.lena.android.utils.VerifyUtil;

import java.util.HashMap;

public class Samsung extends Phone {
    private static final HashMap<String, String> SAMSUNG_MODEL_MAP = new HashMap<>();

    static {
        // Galaxy S 系列
        SAMSUNG_MODEL_MAP.put("SM-G9910", "Galaxy S21");
        SAMSUNG_MODEL_MAP.put("SM-G9960", "Galaxy S21+");
        SAMSUNG_MODEL_MAP.put("SM-G9980", "Galaxy S21 Ultra");
        SAMSUNG_MODEL_MAP.put("SM-G9730", "Galaxy S10");
        SAMSUNG_MODEL_MAP.put("SM-G9700", "Galaxy S10e");

        // Galaxy A 系列
        SAMSUNG_MODEL_MAP.put("SM-A5460", "Galaxy A54");
        SAMSUNG_MODEL_MAP.put("SM-A5260", "Galaxy A52");
        SAMSUNG_MODEL_MAP.put("SM-A3360", "Galaxy A33");

        // Galaxy Note 系列
        SAMSUNG_MODEL_MAP.put("SM-N9750", "Galaxy Note10+");
        SAMSUNG_MODEL_MAP.put("SM-N9860", "Galaxy Note20 Ultra");

        // Galaxy Tab 系列
        SAMSUNG_MODEL_MAP.put("SM-T870", "Galaxy Tab S7");
        SAMSUNG_MODEL_MAP.put("SM-T970", "Galaxy Tab S7+");

        // Galaxy Z Fold / Flip 系列
        SAMSUNG_MODEL_MAP.put("SM-F9360", "Galaxy Z Fold4");
        SAMSUNG_MODEL_MAP.put("SM-F9460", "Galaxy Z Fold5");
        SAMSUNG_MODEL_MAP.put("SM-F7210", "Galaxy Z Flip4");
    }

    public Samsung(@NonNull Context context) {
        super(context);
    }

    @Override
    public String getPhoneModel() {
        final String model = android.os.Build.MODEL.toUpperCase();
        final String phonePublishModel = SAMSUNG_MODEL_MAP.get(model);
        return VerifyUtil.aNotEmptyString(phonePublishModel)? phonePublishModel: super.getPhoneModel();
    }

    @Override
    protected int setPhoneLogo() {
        return R.drawable.app_phone_samsung;
    }
}
