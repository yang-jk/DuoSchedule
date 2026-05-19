package com.duoschedule.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.util.Log

class FairMemoryReceiver : IBinder.DeathRecipient {

    companion object {
        private const val TAG = "FairMemoryReceiver"
        private const val ACTION_TRIM = "itgsa.intent.action.TRIM"
        private const val ACTION_KILL = "itgsa.intent.action.KILL"
        private const val BUNDLE_KEY_COMMON = "common"
        private const val BUNDLE_KEY_EXTRA = "extra"
        private const val KEY_NOTIFY_TYPE = "notifyType"
        private const val KEY_NOTIFY_ID = "notifyId"
        private const val KEY_REASON = "reason"
        private const val KEY_ACTION = "action"
        private const val KEY_CALLBACK = "callback"
        private const val KEY_HEAP_ALLOC = "heapAlloc"
        private const val KEY_HEAP_CAPACITY = "heapCapacity"
        private const val KEY_PSS = "pss"
        private const val KEY_PSS_LIMIT = "pssLimit"
        private const val KEY_REPLY = "reply"
        const val TRANSACTION_EXCEPTION_REPLY = IBinder.FIRST_CALL_TRANSACTION

        const val RESULT_SUCCESS = 0
        const val RESULT_FAILURE = 1
    }

    private var mRemote: IBinder? = null
    private var mInitialized = false
    private var mHandler: Handler? = null

    override fun binderDied() {
        synchronized(this) {
            if (mRemote != null) {
                try {
                    mRemote?.unlinkToDeath(this, 0)
                } catch (_: Exception) {
                }
            }
            mRemote = null
        }
    }

    fun initialize(context: Context) {
        synchronized(this) {
            if (mInitialized) return
            val ht = HandlerThread(TAG)
            ht.start()
            mHandler = Handler(ht.looper)
            val filter = IntentFilter(ACTION_TRIM)
            filter.addAction(ACTION_KILL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(mReceiver, filter, null, mHandler, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(mReceiver, filter, null, mHandler)
            }
            mInitialized = true
            Log.i(TAG, "FairMemoryReceiver initialized")
        }
    }

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action != ACTION_TRIM && action != ACTION_KILL) return

            val data = intent.extras ?: return
            val bundle = data.getBundle(BUNDLE_KEY_COMMON) ?: return

            val notifyType = bundle.getInt(KEY_NOTIFY_TYPE)
            val notifyId = bundle.getInt(KEY_NOTIFY_ID)
            val reason = bundle.getString(KEY_REASON)
            val actionType = bundle.getString(KEY_ACTION)
            val callbackBinder: IBinder? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bundle.getBinder(KEY_CALLBACK)
            } else {
                null
            }

            val extraData = data.getBundle(BUNDLE_KEY_EXTRA)
            var heapAlloc = 0
            var heapCapacity = 0
            var pss = 0
            var pssLimit = 0
            if (extraData != null) {
                heapAlloc = extraData.getInt(KEY_HEAP_ALLOC)
                heapCapacity = extraData.getInt(KEY_HEAP_CAPACITY)
                pss = extraData.getInt(KEY_PSS)
                pssLimit = extraData.getInt(KEY_PSS_LIMIT)
            }

            Log.i(TAG, "Received: action=$actionType, type=$notifyType, id=$notifyId, reason=$reason")
            Log.i(TAG, "Memory: heapAlloc=${heapAlloc}KB, heapCapacity=${heapCapacity}KB, pss=${pss}KB, pssLimit=${pssLimit}KB")

            if (callbackBinder != null) {
                handleReceived(notifyType, notifyId, callbackBinder, actionType ?: "trim")
            } else {
                Log.w(TAG, "Callback binder not found in intent extras")
            }
        }
    }

    private fun handleReceived(notifyType: Int, notifyId: Int, callback: IBinder, action: String) {
        if (!checkRemote(callback)) {
            Log.e(TAG, "Failed to link to callback binder")
            return
        }

        when (action) {
            "trim" -> {
                Log.i(TAG, "Handling TRIM: releasing memory caches")
                System.gc()
                val extra = Bundle().apply {
                    putString(KEY_REPLY, "memory_released")
                }
                reply(notifyType, notifyId, RESULT_SUCCESS, extra)
            }
            "kill" -> {
                Log.i(TAG, "Handling KILL: saving data before process termination")
                val extra = Bundle().apply {
                    putString(KEY_REPLY, "data_saved")
                }
                reply(notifyType, notifyId, RESULT_SUCCESS, extra)
            }
            else -> {
                Log.w(TAG, "Unknown action: $action")
                reply(notifyType, notifyId, RESULT_FAILURE, null)
            }
        }
    }

    private fun checkRemote(callback: IBinder): Boolean {
        synchronized(this) {
            if (mRemote == null) {
                try {
                    mRemote = callback
                    mRemote?.linkToDeath(this, 0)
                } catch (_: RemoteException) {
                    mRemote = null
                    return false
                }
            }
        }
        return true
    }

    private fun reply(notifyType: Int, notifyId: Int, result: Int, extra: Bundle?) {
        synchronized(this) {
            val remote = mRemote ?: return
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            try {
                data.writeInt(notifyType)
                data.writeInt(notifyId)
                data.writeInt(result)
                data.writeBundle(extra ?: Bundle())
                remote.transact(TRANSACTION_EXCEPTION_REPLY, data, reply, IBinder.FLAG_ONEWAY)
                reply.readException()
                Log.i(TAG, "Reply sent: notifyType=$notifyType, notifyId=$notifyId, result=$result")
            } catch (e: Exception) {
                Log.e(TAG, "Reply failed", e)
            } finally {
                reply.recycle()
                data.recycle()
            }
        }
    }
}
