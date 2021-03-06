package huawei.android.hardware.fingerprint;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintService;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FingerprintManagerEx {
    private static final int CODE_DISABLE_FINGERPRINT_VIEW = 1114;
    private static final int CODE_ENABLE_FINGERPRINT_VIEW = 1115;
    private static final int CODE_GET_FINGERPRINT_LIST_ENROLLED = 1118;
    private static final int CODE_GET_HARDWARE_POSITION = 1110;
    private static final int CODE_GET_HARDWARE_TYPE = 1109;
    private static final int CODE_GET_HIGHLIGHT_SPOT_RADIUS = 1122;
    private static final int CODE_GET_HOVER_SUPPORT = 1113;
    private static final int CODE_GET_TOKEN_LEN = 1103;
    private static final int CODE_IS_FINGERPRINT_HARDWARE_DETECTED = 1119;
    private static final int CODE_IS_FP_NEED_CALIBRATE = 1101;
    private static final int CODE_IS_SUPPORT_DUAL_FINGERPRINT = 1120;
    private static final int CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION = 1116;
    private static final int CODE_NOTIFY_OPTICAL_CAPTURE = 1111;
    private static final int CODE_REMOVE_FINGERPRINT = 1107;
    private static final int CODE_REMOVE_MASK_AND_SHOW_CANCEL = 1117;
    private static final int CODE_SET_CALIBRATE_MODE = 1102;
    private static final int CODE_SET_FINGERPRINT_MASK_VIEW = 1104;
    private static final int CODE_SET_HOVER_SWITCH = 1112;
    private static final int CODE_SHOW_FINGERPRINT_BUTTON = 1106;
    private static final int CODE_SHOW_FINGERPRINT_VIEW = 1105;
    private static final int CODE_SUSPEND_AUTHENTICATE = 1108;
    private static final int CODE_SUSPEND_ENROLL = 1123;
    private static final int CODE_UDFINGERPRINT_SPOTCOLOR = 1124;
    private static final String DESCRIPTOR_FINGERPRINT_SERVICE = "android.hardware.fingerprint.IFingerprintService";
    private static final int FINGERPRINT_BACK_ULTRASONIC = 0;
    private static final int FINGERPRINT_FRONT_ULTRASONIC = 1;
    private static final int FINGERPRINT_HARDWARE_OPTICAL = 1;
    private static final int FINGERPRINT_HARDWARE_OUTSCREEN = 0;
    private static final int FINGERPRINT_HARDWARE_ULTRASONIC = 2;
    private static final int FINGERPRINT_NOT_ULTRASONIC = -1;
    private static final int FINGERPRINT_SLIDE_ULTRASONIC = 3;
    private static final int FINGERPRINT_UNDER_DISPLAY_ULTRASONIC = 2;
    private static final int FLAG_FINGERPRINT_LOCATION_BACK = 1;
    private static final int FLAG_FINGERPRINT_LOCATION_FRONT = 2;
    private static final int FLAG_FINGERPRINT_LOCATION_SLIDE = 8;
    private static final int FLAG_FINGERPRINT_LOCATION_UNDER_DISPLAY = 4;
    private static final int FLAG_FINGERPRINT_POSITION_MASK = 65535;
    private static final int FLAG_FINGERPRINT_TYPE_CAPACITANCE = 1;
    private static final int FLAG_FINGERPRINT_TYPE_MASK = 15;
    private static final int FLAG_FINGERPRINT_TYPE_OPTICAL = 2;
    private static final int FLAG_FINGERPRINT_TYPE_ULTRASONIC = 3;
    private static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
    private static final int HOVER_HARDWARE_NOT_SUPPORT = 0;
    private static final int HOVER_HARDWARE_SUPPORT = 1;
    private static final int INVALID_VALUE = -1;
    private static final String TAG = "FingerprintManagerEx";
    private static int mDetailsType = -1;
    private static HashMap<Integer, Integer> mHardwareInfo = new HashMap<>();
    private static int[] mPosition = {-1, -1, -1, -1};
    private static int mType = -1;
    private Context mContext;
    private IFingerprintService mService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));

    public FingerprintManagerEx(Context context) {
        this.mContext = context;
    }

    public int getRemainingNum() {
        if (this.mService != null) {
            try {
                return this.mService.getRemainingNum();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in getRemainingNum: ", e);
            }
        }
        return -1;
    }

    public long getRemainingTime() {
        if (this.mService != null) {
            try {
                return this.mService.getRemainingTime();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in getRemainingTime: ", e);
            }
        }
        return 0;
    }

    public static boolean isFpNeedCalibrate() {
        boolean z = false;
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 0 || !FRONT_FINGERPRINT_NAVIGATION) {
            return false;
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        int result = -1;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(1101, _data, _reply, 0);
                _reply.readException();
                result = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "isFpNeedCalibrate result: " + result);
        if (result == 1) {
            z = true;
        }
        return z;
    }

    public static void setCalibrateMode(int mode) {
        Log.d(TAG, "setCalibrateMode: " + mode);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeInt(mode);
                b.transact(CODE_SET_CALIBRATE_MODE, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static int getTokenLen() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        int len = -1;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_GET_TOKEN_LEN, _data, _reply, 0);
                _reply.readException();
                len = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "getTokenLen len: " + len);
        return len;
    }

    public static void setFingerprintMaskView(Bundle bundle) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeBundle(bundle);
                b.transact(CODE_SET_FINGERPRINT_MASK_VIEW, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void showFingerprintView() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_SHOW_FINGERPRINT_VIEW, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void showSuspensionButton(int centerX, int centerY) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeInt(centerX);
                _data.writeInt(centerY);
                b.transact(CODE_SHOW_FINGERPRINT_BUTTON, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void removeFingerView() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_REMOVE_FINGERPRINT, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void suspendAuthentication(int status) {
        if (!hasFingerprintInScreen()) {
            Log.w(TAG, "do not have UD device suspend invalid");
        } else {
            Log.d(TAG, "suspendAuthentication: " + status);
        }
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeInt(status);
                b.transact(CODE_SUSPEND_AUTHENTICATE, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static int suspendEnroll(int status) {
        int result = -1;
        if (!hasFingerprintInScreen()) {
            Log.w(TAG, "do not have UD device suspend invalid");
            return -1;
        }
        Log.d(TAG, "suspendEnroll: " + status);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeInt(status);
                b.transact(CODE_SUSPEND_ENROLL, _data, _reply, 0);
                _reply.readException();
                result = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return result;
    }

    public static int getHardwareType() {
        int type = mType;
        if (type != -1) {
            return type;
        }
        mHardwareInfo = getHardwareInfo();
        if (mHardwareInfo.isEmpty()) {
            int type2 = SystemProperties.getInt("persist.sys.fingerprint.hardwareType", -1);
            Log.d(TAG, "use SystemProperties type :" + type2);
            return type2;
        }
        if (mHardwareInfo.containsKey(4)) {
            int physical = Integer.parseInt(mHardwareInfo.get(4).toString());
            if (physical == 2) {
                type = 1;
            } else if (physical == 3) {
                type = 2;
            }
            Log.d(TAG, "LOCATION_UNDER_DISPLAY :" + physical);
        } else {
            type = 0;
        }
        mType = type;
        Log.d(TAG, "type:" + type);
        return type;
    }

    public static int getUltrasonicFingerprintType() {
        int type = -1;
        if (mHardwareInfo.isEmpty()) {
            mHardwareInfo = getHardwareInfo();
        }
        if (mHardwareInfo.containsKey(2)) {
            if (mHardwareInfo.get(2).intValue() == 3) {
                type = 1;
            }
        } else if (mHardwareInfo.containsKey(1)) {
            if (mHardwareInfo.get(1).intValue() == 3) {
                type = 0;
            }
        } else if (mHardwareInfo.containsKey(4)) {
            if (mHardwareInfo.get(4).intValue() == 3) {
                type = 2;
            }
        } else if (mHardwareInfo.containsKey(8) && mHardwareInfo.get(8).intValue() == 3) {
            type = 3;
        }
        Log.d(TAG, "getUltrasonicFingerprintType :" + type);
        return type;
    }

    public static boolean isSupportDualFingerprint() {
        Log.d(TAG, "isSupportDualFingerprint called.");
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_IS_SUPPORT_DUAL_FINGERPRINT, _data, _reply, 0);
                _reply.readException();
                boolean isSupportDualFp = _reply.readBoolean();
                Log.d(TAG, "isSupportDualFingerprint is: " + isSupportDualFp);
                _reply.recycle();
                _data.recycle();
                return isSupportDualFp;
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return false;
    }

    public List<Fingerprint> getEnrolledFingerprints(int targetDevice) {
        return getEnrolledFingerprints(targetDevice, UserHandle.myUserId());
    }

    public List<Fingerprint> getEnrolledFingerprints(int targetDevice, int userId) {
        List<Fingerprint> fingerprints = new ArrayList<>();
        String opPackageName = this.mContext.getOpPackageName();
        if (opPackageName == null || "".equals(opPackageName)) {
            Log.d(TAG, "calling opPackageName is invalid");
            return fingerprints;
        }
        Log.d(TAG, "getEnrolledFingerprints calling package: " + opPackageName + " targetDevice: " + targetDevice);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeInt(targetDevice);
                _data.writeString(opPackageName);
                _data.writeInt(userId);
                b.transact(CODE_GET_FINGERPRINT_LIST_ENROLLED, _data, _reply, 0);
                _reply.readException();
                _reply.readTypedList(fingerprints, Fingerprint.CREATOR);
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return fingerprints;
    }

    public boolean hasEnrolledFingerprints(int targetDevice) {
        String opPackageName = this.mContext.getOpPackageName();
        boolean z = false;
        if (opPackageName == null || "".equals(opPackageName)) {
            Log.d(TAG, "calling opPackageName is invalid");
            return false;
        }
        Log.d(TAG, "hasEnrolledFingerprints calling package: " + opPackageName + " targetDevice: " + targetDevice);
        if (getEnrolledFingerprints(targetDevice).size() > 0) {
            z = true;
        }
        return z;
    }

    public boolean isHardwareDetected(int targetDevice) {
        String opPackageName = this.mContext.getOpPackageName();
        if (opPackageName == null || "".equals(opPackageName)) {
            Log.d(TAG, "calling opPackageName is invalid");
            return false;
        }
        Log.d(TAG, "isHardwareDetected calling package: " + opPackageName + " targetDevice: " + targetDevice);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeInt(targetDevice);
                _data.writeString(opPackageName);
                b.transact(CODE_IS_FINGERPRINT_HARDWARE_DETECTED, _data, _reply, 0);
                _reply.readException();
                boolean isHardwareDetected = _reply.readBoolean();
                Log.d(TAG, "isHardwareDetected is: " + isHardwareDetected);
                _reply.recycle();
                _data.recycle();
                return isHardwareDetected;
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return false;
    }

    private static HashMap<Integer, Integer> getHardwareInfo() {
        HashMap<Integer, Integer> hardwareInfo = new HashMap<>();
        int typeDetails = getHardwareTypeDetailsFromHal();
        Log.d(TAG, "typeDetails:" + typeDetails);
        if (typeDetails != -1) {
            int offset = -1;
            if ((typeDetails & 1) != 0) {
                offset = -1 + 1;
                int physicalType = (typeDetails >> ((offset * 4) + 8)) & 15;
                hardwareInfo.put(1, Integer.valueOf(physicalType));
                Log.d(TAG, "LOCATION_BACK physicalType :" + physicalType);
            }
            if ((typeDetails & 2) != 0) {
                offset++;
                int physicalType2 = (typeDetails >> ((offset * 4) + 8)) & 15;
                hardwareInfo.put(2, Integer.valueOf(physicalType2));
                Log.d(TAG, "LOCATION_FRONT physicalType :" + physicalType2);
            }
            if ((typeDetails & 4) != 0) {
                offset++;
                int physicalType3 = (typeDetails >> ((offset * 4) + 8)) & 15;
                hardwareInfo.put(4, Integer.valueOf(physicalType3));
                Log.d(TAG, "LOCATION_UNDER_DISPLAY physicalType :" + physicalType3);
            }
            if ((typeDetails & 8) != 0) {
                int physicalType4 = (typeDetails >> (((offset + 1) * 4) + 8)) & 15;
                hardwareInfo.put(8, Integer.valueOf(physicalType4));
                Log.d(TAG, "LOCATION_SLIDE physicalType :" + physicalType4);
            }
        }
        return hardwareInfo;
    }

    private static int getHardwareTypeDetailsFromHal() {
        Log.d(TAG, "getHardwareType  mDetailsType:" + mDetailsType);
        int type = mDetailsType;
        if (type != -1) {
            return type;
        }
        int type2 = -1;
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_GET_HARDWARE_TYPE, _data, _reply, 0);
                _reply.readException();
                type2 = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "getHardwareType from Hal: " + type2);
        mDetailsType = type2;
        return type2;
    }

    public static boolean hasFingerprintInScreen() {
        int hardHardwareType = getHardwareType();
        return (hardHardwareType == 0 || hardHardwareType == -1) ? false : true;
    }

    private static int[] getHardwarePositionFromHal() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        int[] position = {-1, -1, -1, -1};
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_GET_HARDWARE_POSITION, _data, _reply, 0);
                _reply.readException();
                position[0] = _reply.readInt();
                position[1] = _reply.readInt();
                position[2] = _reply.readInt();
                position[3] = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return position;
    }

    public static Rect getFingerprintRect() {
        int[] position = getHardwarePosition();
        return new Rect(position[0], position[1], position[2], position[3]);
    }

    public static int[] getHardwarePosition() {
        StringBuilder sb = new StringBuilder();
        sb.append("getHardwarePosition mPosition[0] ");
        int i = 0;
        sb.append(mPosition[0]);
        Log.d(TAG, sb.toString());
        int[] position = mPosition;
        if (position[0] != -1) {
            return position;
        }
        int[] pxPosition = getHardwarePositionFromHal();
        while (true) {
            int i2 = i;
            if (i2 < 4) {
                Log.d(TAG, "from hal after covert: " + pxPosition[i2]);
                i = i2 + 1;
            } else {
                mPosition = pxPosition;
                return pxPosition;
            }
        }
    }

    public static int getUDFingerprintSpotColor() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        int spotColor = 0;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_UDFINGERPRINT_SPOTCOLOR, _data, _reply, 0);
                _reply.readException();
                spotColor = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return spotColor;
    }

    public static void notifyCaptureOpticalImage() {
        if (getHardwareType() != 1) {
            Log.d(TAG, "not Optical sensor notifyCapture failed");
            return;
        }
        Log.d(TAG, "notifyCaptureOpticalImage");
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_NOTIFY_OPTICAL_CAPTURE, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void setHoverEventSwitch(int enabled) {
        Log.d(TAG, "setHoverEventSwitch: " + enabled);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeInt(enabled);
                b.transact(CODE_SET_HOVER_SWITCH, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static boolean isHoverEventSupport() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        int type = -1;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_GET_HOVER_SUPPORT, _data, _reply, 0);
                _reply.readException();
                type = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        Log.d(TAG, "isHoverEventSupport from Hal: " + type);
        if (type == -1) {
            type = SystemProperties.getInt("persist.sys.fingerprint.hoverSupport", 0);
            Log.d(TAG, "isHoverEventSupport use SystemProperties type :" + type);
        }
        return type == 1;
    }

    public void disableFingerprintView(boolean hasAnimation) {
        Log.d(TAG, "disableFingerprintView: " + hasAnimation);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeBoolean(hasAnimation);
                b.transact(CODE_DISABLE_FINGERPRINT_VIEW, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public void enableFingerprintView(boolean hasAnimation, int initStatus) {
        Log.d(TAG, "enableFingerprintView: hasAnimation =" + hasAnimation + ",initStatus = " + initStatus);
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                _data.writeBoolean(hasAnimation);
                _data.writeInt(initStatus);
                b.transact(CODE_ENABLE_FINGERPRINT_VIEW, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void keepMaskShowAfterAuthentication() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_KEEP_MASK_SHOW_AFTER_AUTHENTICATION, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static void removeMaskAndShowButton() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_REMOVE_MASK_AND_SHOW_CANCEL, _data, _reply, 0);
                _reply.readException();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
    }

    public static int getHighLightspotRadius() {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        IBinder b = ServiceManager.getService("fingerprint");
        int radius = -1;
        if (b != null) {
            try {
                _data.writeInterfaceToken(DESCRIPTOR_FINGERPRINT_SERVICE);
                b.transact(CODE_GET_HIGHLIGHT_SPOT_RADIUS, _data, _reply, 0);
                _reply.readException();
                radius = _reply.readInt();
            } catch (RemoteException localRemoteException) {
                localRemoteException.printStackTrace();
            } catch (Throwable th) {
                _reply.recycle();
                _data.recycle();
                throw th;
            }
        }
        _reply.recycle();
        _data.recycle();
        return radius;
    }
}
