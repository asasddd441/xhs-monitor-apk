package com.xhsmonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "XhsMonitor";
    private static final String LISTENER_CLASS = "com.xhsmonitor/.XhsNotificationListener";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.openSettings);
        TextView status = findViewById(R.id.status);

        btn.setOnClickListener(v -> openNotificationSettings());

        updateStatus(status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((TextView) findViewById(R.id.status)).setText(buildStatusText());
    }

    private void openNotificationSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "open settings failed", e);
        }
    }

    private void updateStatus(TextView status) {
        status.setText(buildStatusText());
    }

    private String buildStatusText() {
        boolean enabled = isListenerEnabled();
        return "监听服务状态: " + (enabled ? "已开启" : "未开启") + "\n\n"
                + "请点击下方按钮打开通知使用权设置，找到「小红书通知监听」并开启。";
    }

    private boolean isListenerEnabled() {
        ComponentName cn = new ComponentName(this, XhsNotificationListener.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (TextUtils.isEmpty(flat)) return false;
        return flat.contains(cn.flattenToString());
    }
}
