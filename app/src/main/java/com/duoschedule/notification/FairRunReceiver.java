package com.duoschedule.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.duoschedule.util.AppLog;

/**
 * OPPO 公平运行内存机制适配接收器。
 * 监听系统内存预警(TRIM)和查杀(KILL)广播，及时释放内存或保存现场数据。
 */
public class FairRunReceiver implements IBinder.DeathRecipient {

    private static final String TAG = "FairRunReceiver";

    private static final String ACTION_TRIM = "itgsa.intent.action.TRIM";
    private static final String ACTION_KILL = "itgsa.intent.action.KILL";

    private static final String BUNDLE_KEY_COMMON = "common";
    private static final String BUNDLE_KEY_EXTRA = "extra";
    private static final String BUNDLE_KEY_NOTIFY_TYPE = "notifyType";
    private static final String BUNDLE_KEY_NOTIFY_ID = "notifyId";
    private static final String BUNDLE_KEY_REASON = "reason";
    private static final String BUNDLE_KEY_ACTION = "action";
    private static final String BUNDLE_KEY_CALLBACK = "callback";
    private static final String BUNDLE_KEY_HEAP_ALLOC = "heapAlloc";
    private static final String BUNDLE_KEY_HEAP_CAPACITY = "heapCapacity";
    private static final String BUNDLE_KEY_PSS = "pss";
    private static final String BUNDLE_KEY_PSS_LIMIT = "pssLimit";

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILURE = 1;

    private static final int TRANSACTION_EXCEPTION_REPLY = IBinder.FIRST_CALL_TRANSACTION;

    private IBinder mRemote;
    private boolean mInitialized;
    private Handler mHandler;
    private Context mAppContext;

    private MemoryCleanupCallback cleanupCallback;

    public interface MemoryCleanupCallback {
        void onTrimMemory(int notifyType);
        boolean onSaveState();
    }

    private FairRunReceiver() {}

    public static FairRunReceiver getInstance() {
        return Instance.INSTANCE;
    }

    private static class Instance {
        private static final FairRunReceiver INSTANCE = new FairRunReceiver();
    }

    public void setCleanupCallback(MemoryCleanupCallback callback) {
        this.cleanupCallback = callback;
    }

    public void initialize(Context context) {
        synchronized (this) {
            if (!mInitialized) {
                mAppContext = context.getApplicationContext();
                HandlerThread ht = new HandlerThread(TAG);
                ht.start();
                mHandler = new Handler(ht.getLooper());

                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_TRIM);
                filter.addAction(ACTION_KILL);

                ContextCompat.registerReceiver(context, mReceiver, filter, null, mHandler, ContextCompat.RECEIVER_EXPORTED);

                mInitialized = true;
                AppLog.i(TAG, "公平运行接收器已初始化");
            }
        }
    }

    @Override
    public void binderDied() {
        synchronized (this) {
            if (mRemote != null) {
                try {
                    mRemote.unlinkToDeath(this, 0);
                } catch (Exception ignore) {}
            }
            mRemote = null;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            Bundle data = intent.getExtras();
            if (data == null) return;

            Bundle common = data.getBundle(BUNDLE_KEY_COMMON);
            if (common == null) return;

            int notifyType = common.getInt(BUNDLE_KEY_NOTIFY_TYPE);
            int notifyId = common.getInt(BUNDLE_KEY_NOTIFY_ID);
            String reason = common.getString(BUNDLE_KEY_REASON, "");
            String actionType = common.getString(BUNDLE_KEY_ACTION, "");
            IBinder callbackBinder = common.getBinder(BUNDLE_KEY_CALLBACK);

            Bundle extraData = data.getBundle(BUNDLE_KEY_EXTRA);
            int heapAlloc = extraData != null ? extraData.getInt(BUNDLE_KEY_HEAP_ALLOC) : 0;
            int heapCapacity = extraData != null ? extraData.getInt(BUNDLE_KEY_HEAP_CAPACITY) : 0;
            int pss = extraData != null ? extraData.getInt(BUNDLE_KEY_PSS) : 0;
            int pssLimit = extraData != null ? extraData.getInt(BUNDLE_KEY_PSS_LIMIT) : 0;

            AppLog.w(TAG, "收到公平运行广播: action=" + actionType
                    + ", notifyType=" + notifyType
                    + ", reason=" + reason
                    + ", pss=" + pss + "KB/" + pssLimit + "KB"
                    + ", heap=" + heapAlloc + "KB/" + heapCapacity + "KB");

            if (ACTION_TRIM.equals(action)) {
                handleTrim(notifyType, notifyId, callbackBinder, pss, pssLimit, heapAlloc, heapCapacity);
            } else if (ACTION_KILL.equals(action)) {
                handleKill(notifyType, notifyId, callbackBinder, pss, pssLimit, heapAlloc, heapCapacity);
            }
        }
    };

    private void handleTrim(int notifyType, int notifyId, IBinder callback,
                            int pss, int pssLimit, int heapAlloc, int heapCapacity) {
        AppLog.i(TAG, "处理内存预警: 释放内存");

        try {
            // 通知应用释放内存
            if (cleanupCallback != null) {
                cleanupCallback.onTrimMemory(notifyType);
            }

            // 系统级内存释放
            System.gc();

            // 回调系统
            replyToSystem(notifyType, notifyId, callback, RESULT_SUCCESS, "内存已释放");
        } catch (Exception e) {
            Log.e(TAG, "处理TRIM失败", e);
            replyToSystem(notifyType, notifyId, callback, RESULT_FAILURE, "处理失败: " + e.getMessage());
        }
    }

    private void handleKill(int notifyType, int notifyId, IBinder callback,
                            int pss, int pssLimit, int heapAlloc, int heapCapacity) {
        AppLog.w(TAG, "处理查杀预警: 保存现场数据");

        try {
            boolean saved = false;
            if (cleanupCallback != null) {
                saved = cleanupCallback.onSaveState();
            }

            int result = saved ? RESULT_SUCCESS : RESULT_FAILURE;
            String message = saved ? "数据已保存" : "保存失败";
            replyToSystem(notifyType, notifyId, callback, result, message);
        } catch (Exception e) {
            Log.e(TAG, "处理KILL失败", e);
            replyToSystem(notifyType, notifyId, callback, RESULT_FAILURE, "处理失败: " + e.getMessage());
        }
    }

    private void replyToSystem(int notifyType, int notifyId, IBinder callback,
                               int result, String message) {
        if (callback == null) {
            AppLog.w(TAG, "回调Binder为空，无法回复系统");
            return;
        }

        if (!checkRemote(callback)) {
            AppLog.w(TAG, "无法连接到系统回调Binder");
            return;
        }

        Bundle extra = new Bundle();
        extra.putString("reply", message);
        reply(notifyType, notifyId, result, extra);
    }

    private boolean checkRemote(IBinder callback) {
        synchronized (this) {
            if (mRemote == null) {
                try {
                    mRemote = callback;
                    mRemote.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    mRemote = null;
                    return false;
                }
            }
        }
        return true;
    }

    public void reply(int notifyType, int notifyId, int result, Bundle extra) {
        synchronized (this) {
            IBinder remote = mRemote;
            if (remote != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInt(notifyType);
                    data.writeInt(notifyId);
                    data.writeInt(result);
                    if (extra == null) {
                        extra = new Bundle();
                    }
                    data.writeBundle(extra);
                    remote.transact(TRANSACTION_EXCEPTION_REPLY, data, reply, IBinder.FLAG_ONEWAY);
                    reply.readException();
                    AppLog.i(TAG, "已回复系统: notifyType=" + notifyType
                            + ", notifyId=" + notifyId + ", result=" + result);
                } catch (Exception e) {
                    Log.e(TAG, "回复系统失败", e);
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
        }
    }
}
