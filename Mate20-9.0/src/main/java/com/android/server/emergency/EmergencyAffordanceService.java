package com.android.server.emergency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.SystemService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EmergencyAffordanceService extends SystemService {
    private static final int CELL_INFO_STATE_CHANGED = 2;
    private static final String EMERGENCY_SIM_INSERTED_SETTING = "emergency_sim_inserted_before";
    private static final int INITIALIZE_STATE = 1;
    private static final int NUM_SCANS_UNTIL_ABORT = 4;
    private static final int SUBSCRIPTION_CHANGED = 3;
    private static final String TAG = "EmergencyAffordanceService";
    private BroadcastReceiver mAirplaneModeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) == 0) {
                EmergencyAffordanceService.this.startScanning();
                EmergencyAffordanceService.this.requestCellScan();
            }
        }
    };
    private final Context mContext;
    private boolean mEmergencyAffordanceNeeded;
    private final ArrayList<Integer> mEmergencyCallMccNumbers;
    /* access modifiers changed from: private */
    public MyHandler mHandler;
    private final Object mLock = new Object();
    private boolean mNetworkNeedsEmergencyAffordance;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCellInfoChanged(List<CellInfo> list) {
            if (!EmergencyAffordanceService.this.isEmergencyAffordanceNeeded()) {
                EmergencyAffordanceService.this.requestCellScan();
            }
        }

        public void onCellLocationChanged(CellLocation location) {
            if (!EmergencyAffordanceService.this.isEmergencyAffordanceNeeded()) {
                EmergencyAffordanceService.this.requestCellScan();
            }
        }
    };
    private int mScansCompleted;
    private boolean mSimNeedsEmergencyAffordance;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionChangedListener = new SubscriptionManager.OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            EmergencyAffordanceService.this.mHandler.obtainMessage(3).sendToTarget();
        }
    };
    private SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;
    private boolean mVoiceCapable;

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    EmergencyAffordanceService.this.handleInitializeState();
                    return;
                case 2:
                    boolean unused = EmergencyAffordanceService.this.handleUpdateCellInfo();
                    return;
                case 3:
                    boolean unused2 = EmergencyAffordanceService.this.handleUpdateSimSubscriptionInfo();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void requestCellScan() {
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    public EmergencyAffordanceService(Context context) {
        super(context);
        this.mContext = context;
        int[] numbers = context.getResources().getIntArray(17236007);
        this.mEmergencyCallMccNumbers = new ArrayList<>(numbers.length);
        for (int valueOf : numbers) {
            this.mEmergencyCallMccNumbers.add(Integer.valueOf(valueOf));
        }
    }

    private void updateEmergencyAffordanceNeeded() {
        synchronized (this.mLock) {
            this.mEmergencyAffordanceNeeded = this.mVoiceCapable && (this.mSimNeedsEmergencyAffordance || this.mNetworkNeedsEmergencyAffordance);
            Settings.Global.putInt(this.mContext.getContentResolver(), "emergency_affordance_needed", this.mEmergencyAffordanceNeeded ? 1 : 0);
            if (this.mEmergencyAffordanceNeeded) {
                stopScanning();
            }
        }
    }

    private void stopScanning() {
        synchronized (this.mLock) {
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            this.mScansCompleted = 0;
        }
    }

    /* access modifiers changed from: private */
    public boolean isEmergencyAffordanceNeeded() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEmergencyAffordanceNeeded;
        }
        return z;
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == 600) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService(TelephonyManager.class);
            this.mVoiceCapable = this.mTelephonyManager.isVoiceCapable();
            if (!this.mVoiceCapable) {
                updateEmergencyAffordanceNeeded();
                return;
            }
            this.mSubscriptionManager = SubscriptionManager.from(this.mContext);
            HandlerThread thread = new HandlerThread(TAG);
            thread.start();
            this.mHandler = new MyHandler(thread.getLooper());
            this.mHandler.obtainMessage(1).sendToTarget();
            startScanning();
            this.mContext.registerReceiver(this.mAirplaneModeReceiver, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
            this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionChangedListener);
        }
    }

    /* access modifiers changed from: private */
    public void startScanning() {
        this.mTelephonyManager.listen(this.mPhoneStateListener, 1040);
    }

    /* access modifiers changed from: private */
    public void handleInitializeState() {
        if (!handleUpdateSimSubscriptionInfo() && !handleUpdateCellInfo()) {
            updateEmergencyAffordanceNeeded();
        }
    }

    /* access modifiers changed from: private */
    public boolean handleUpdateSimSubscriptionInfo() {
        boolean neededNow = simNeededAffordanceBefore();
        List<SubscriptionInfo> activeSubscriptionInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            setSimNeedsEmergencyAffordance(neededNow);
            return neededNow;
        }
        Iterator<SubscriptionInfo> it = activeSubscriptionInfoList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            SubscriptionInfo info = it.next();
            int mcc = info.getMcc();
            if (mccRequiresEmergencyAffordance(mcc)) {
                neededNow = true;
                break;
            }
            if (!(mcc == 0 || mcc == Integer.MAX_VALUE)) {
                neededNow = false;
            }
            String simOperator = this.mTelephonyManager.getSimOperator(info.getSubscriptionId());
            int mcc2 = 0;
            if (simOperator != null && simOperator.length() >= 3) {
                try {
                    mcc2 = Integer.parseInt(simOperator.substring(0, 3));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid mcc, should be numeric mcc.");
                }
            }
            if (mcc2 != 0) {
                if (mccRequiresEmergencyAffordance(mcc2)) {
                    neededNow = true;
                    break;
                }
                neededNow = false;
            }
        }
        setSimNeedsEmergencyAffordance(neededNow);
        return neededNow;
    }

    private void setSimNeedsEmergencyAffordance(boolean simNeedsEmergencyAffordance) {
        if (simNeededAffordanceBefore() != simNeedsEmergencyAffordance) {
            Settings.Global.putInt(this.mContext.getContentResolver(), EMERGENCY_SIM_INSERTED_SETTING, simNeedsEmergencyAffordance);
        }
        if (simNeedsEmergencyAffordance != this.mSimNeedsEmergencyAffordance) {
            this.mSimNeedsEmergencyAffordance = simNeedsEmergencyAffordance;
            updateEmergencyAffordanceNeeded();
        }
    }

    private boolean simNeededAffordanceBefore() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), EMERGENCY_SIM_INSERTED_SETTING, 0) != 0;
    }

    /* access modifiers changed from: private */
    public boolean handleUpdateCellInfo() {
        List<CellInfo> cellInfos = this.mTelephonyManager.getAllCellInfo();
        if (cellInfos == null) {
            return false;
        }
        boolean stopScanningAfterScan = false;
        for (CellInfo cellInfo : cellInfos) {
            int mcc = 0;
            if (cellInfo instanceof CellInfoGsm) {
                mcc = ((CellInfoGsm) cellInfo).getCellIdentity().getMcc();
            } else if (cellInfo instanceof CellInfoLte) {
                mcc = ((CellInfoLte) cellInfo).getCellIdentity().getMcc();
            } else if (cellInfo instanceof CellInfoWcdma) {
                mcc = ((CellInfoWcdma) cellInfo).getCellIdentity().getMcc();
            }
            if (mccRequiresEmergencyAffordance(mcc)) {
                setNetworkNeedsEmergencyAffordance(true);
                return true;
            } else if (!(mcc == 0 || mcc == Integer.MAX_VALUE)) {
                stopScanningAfterScan = true;
            }
        }
        if (stopScanningAfterScan) {
            stopScanning();
        } else {
            onCellScanFinishedUnsuccessful();
        }
        setNetworkNeedsEmergencyAffordance(false);
        return false;
    }

    private void setNetworkNeedsEmergencyAffordance(boolean needsAffordance) {
        synchronized (this.mLock) {
            this.mNetworkNeedsEmergencyAffordance = needsAffordance;
            updateEmergencyAffordanceNeeded();
        }
    }

    private void onCellScanFinishedUnsuccessful() {
        synchronized (this.mLock) {
            this.mScansCompleted++;
            if (this.mScansCompleted >= 4) {
                stopScanning();
            }
        }
    }

    private boolean mccRequiresEmergencyAffordance(int mcc) {
        return this.mEmergencyCallMccNumbers.contains(Integer.valueOf(mcc));
    }
}
