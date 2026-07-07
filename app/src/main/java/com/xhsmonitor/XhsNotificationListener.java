package com.xhsmonitor;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class XhsNotificationListener extends NotificationListenerService {

    private static final String TAG = "XhsMonitor";
    private static final String TARGET_PKG = "com.xingin.xhs";
    private static final String MONITOR_FILE = "/sdcard/xhs_monitor/trigger";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private final Set<String> processedKeys = new HashSet<>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        if (!TARGET_PKG.equals(pkg)) return;

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        CharSequence titleSeq = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence textSeq = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence bigTextSeq = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

        String title = titleSeq != null ? titleSeq.toString() : "";
        String text = textSeq != null ? textSeq.toString() : "";
        String bigText = bigTextSeq != null ? bigTextSeq.toString() : "";

        if (title.isEmpty() && text.isEmpty() && bigText.isEmpty()) return;

        String content = title + " " + text + " " + bigText;
        Log.i(TAG, "onNotificationPosted: " + title + " | " + text);

        // 只关心评论和 @ 通知
        if (!isCommentOrMention(content)) return;

        String key = sbn.getKey();
        String now = dateFormat.format(new Date());
        if (processedKeys.contains(key)) {
            Log.i(TAG, "已处理，跳过: " + key);
            return;
        }
        processedKeys.add(key);

        writeTriggerFile(now, title, text, bigText, key);
    }

    private boolean isCommentOrMention(String content) {
        String lower = content.toLowerCase();
        return lower.contains("评论")
                || lower.contains("@")
                || lower.contains("赞了")
                || lower.contains("收藏了")
                || lower.contains("关注了")
                || lower.contains("回复了");
    }

    private void writeTriggerFile(String time, String title, String text, String bigText, String key) {
        File dir = new File("/sdcard/xhs_monitor");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "trigger");

        String payload = "time=" + time + "\n"
                + "title=" + safe(title) + "\n"
                + "text=" + safe(text) + "\n"
                + "bigText=" + safe(bigText) + "\n"
                + "key=" + key + "\n";

        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            fos.write(payload.getBytes("UTF-8"));
            fos.flush();
            Log.i(TAG, "trigger file written: " + payload);
        } catch (Exception e) {
            Log.e(TAG, "write trigger failed", e);
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace("\r", " ").trim();
    }
}
