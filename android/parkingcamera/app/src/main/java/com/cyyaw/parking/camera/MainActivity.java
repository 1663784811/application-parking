package com.cyyaw.parking.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Home page: full-bleed landscape camera preview rendered to a SurfaceView
 * via the Camera2 API. The preview is letterboxed to the camera's aspect
 * ratio so it is never distorted. Bottom bar includes camera selector,
 * network status, and settings shortcut.
 */
public class MainActivity extends BaseActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1440;

    private AspectRatioSurfaceView cameraSurface;
    private CameraManager cameraManager;
    private String cameraId;

    private Size previewSize;        // chosen once, stable
    private boolean surfaceConfigured = false;

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private boolean surfaceReady = false;
    private boolean opening = false;

    // License-plate recognition (HyperLPR3, onnxruntime)
    private static final int LPR_MAX_FPS = 3;              // 识别节流
    private ImageReader lprReader;                         // YUV_420_888 帧
    private LprRecognizer lpr;
    private HandlerThread lprThread;
    private Handler lprHandler;
    private boolean lprBusy = false;
    private long lastLprAt = 0;
    private TextView plateResult;
    private PlateOverlayView plateOverlay;

    // 车牌回调上报：去重 + 冷却
    private static final float LPR_REPORT_MIN_CONF = 0.85f;   // 置信度低于此不上报
    private static final long LPR_REPORT_COOLDOWN_MS = 5000;  // 同一车牌 5 秒内只报一次
    private String lastReportedPlate = null;
    private long lastReportAt = 0;

    // Bottom bar views
    private ImageView cameraSwitchIcon;
    private TextView networkStatus;
    private ImageView settingsIcon;

    // Settings panel views
    private View settingsOverlay;
    private View settingsPanel;
    private ImageView closePanel;
    private TextView bottomCameraId;
    private TextView bottomBarrierId;

    // MQTT settings
    private View mqttSettingsRow;
    private TextView mqttSummary;

    // License-plate recognition callback URL
    private View lprCallbackRow;
    private TextView lprCallbackSummary;

    // Basic info: timestamp toggle / camera ID / barrier ID
    private View basicInfoRow;
    private TextView basicInfoSummary;

    // Signaling server address
    private View signalingServerRow;
    private TextView signalingServerSummary;

    private static final String PREFS_NAME = "parking_settings";
    private static final String KEY_MQTT_ADDRESS = "mqtt_address";
    private static final String KEY_MQTT_USERNAME = "mqtt_username";
    private static final String KEY_MQTT_PASSWORD = "mqtt_password";
    private static final String KEY_MQTT_CLIENT_ID = "mqtt_client_id";
    private static final String KEY_LPR_CALLBACK = "lpr_callback";
    private static final String KEY_TIMESTAMP_VISIBLE = "timestamp_visible";
    private static final String KEY_CAMERA_ID = "camera_id";
    private static final String KEY_BARRIER_ID = "barrier_id";
    private static final String KEY_SIGNALING_SERVER = "signaling_server";

    // Camera IDs for the spinner
    private List<String> allCameraIds = new ArrayList<>();

    // Panel animation state
    private boolean isPanelOpen = false;

    // Time display
    private TextView timeDisplay;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
    private boolean timeVisible = true;

    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            surfaceReady = true;
            configureSurface(holder);
            openCamera();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            // (Re)create the preview session once the surface has its final size.
            startPreview();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            surfaceReady = false;
            closePreviewSession();
        }
    };

    private final CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            opening = false;
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            opening = false;
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            opening = false;
            camera.close();
            cameraDevice = null;
            toast("Camera error: " + error);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraSurface = findViewById(R.id.cameraSurface);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Initialize bottom bar views
        cameraSwitchIcon = findViewById(R.id.cameraSwitchIcon);
        networkStatus = findViewById(R.id.networkStatus);
        settingsIcon = findViewById(R.id.settingsIcon);

        // Initialize settings panel views
        settingsOverlay = findViewById(R.id.settingsOverlay);
        settingsPanel = findViewById(R.id.settingsPanel);
        closePanel = findViewById(R.id.closePanel);
        bottomCameraId = findViewById(R.id.bottomCameraId);
        bottomBarrierId = findViewById(R.id.bottomBarrierId);
        mqttSettingsRow = findViewById(R.id.mqttSettingsRow);
        mqttSummary = findViewById(R.id.mqttSummary);
        lprCallbackRow = findViewById(R.id.lprCallbackRow);
        lprCallbackSummary = findViewById(R.id.lprCallbackSummary);
        basicInfoRow = findViewById(R.id.basicInfoRow);
        basicInfoSummary = findViewById(R.id.basicInfoSummary);
        signalingServerRow = findViewById(R.id.signalingServerRow);
        signalingServerSummary = findViewById(R.id.signalingServerSummary);

        // Initialize time display
        timeDisplay = findViewById(R.id.timeDisplay);

        // Initialize LPR UI + background engine
        plateResult = findViewById(R.id.plateResult);
        plateOverlay = findViewById(R.id.plateOverlay);
        startLprEngine();

        // Restore persisted basic-info state into the bottom bar + overlay
        SharedPreferences bootPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        bottomCameraId.setText(bootPrefs.getString(KEY_CAMERA_ID, "CAM-001"));
        bottomBarrierId.setText(bootPrefs.getString(KEY_BARRIER_ID, "B-001"));
        timeVisible = bootPrefs.getBoolean(KEY_TIMESTAMP_VISIBLE, true);
        timeDisplay.setVisibility(timeVisible ? View.VISIBLE : View.GONE);

        // Setup camera switch
        setupCameraSwitch();

        // Setup network monitoring
        setupNetworkMonitor();

        // Setup settings panel
        setupSettingsPanel();

        // Start time updater
        startTimeUpdater();

        cameraId = pickBackCameraId();
        cameraSurface.getHolder().addCallback(surfaceCallback);
    }

    /**
     * Populate camera list and set up the camera switch icon to cycle through cameras.
     */
    private void setupCameraSwitch() {
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                allCameraIds.add(id);
            }
        } catch (CameraAccessException e) {
            toast("Cannot access camera list");
        }

        if (allCameraIds.isEmpty()) return;

        // Click to cycle to next camera
        cameraSwitchIcon.setOnClickListener(v -> {
            if (allCameraIds.size() <= 1) {
                toast("只有一个摄像头可用");
                return;
            }
            int currentIndex = allCameraIds.indexOf(cameraId);
            int nextIndex = (currentIndex + 1) % allCameraIds.size();
            String newId = allCameraIds.get(nextIndex);
            if (!newId.equals(cameraId)) {
                cameraId = newId;
                surfaceConfigured = false;
                previewSize = null;
                closeCamera();
                openCamera();
            }
        });
    }

    /**
     * Monitor network connectivity and update the status indicator.
     */
    private void setupNetworkMonitor() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        // Initial check
        updateNetworkStatus(cm);

        // Listen for changes
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> updateNetworkStatus(cm));
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> updateNetworkStatus(cm));
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull NetworkCapabilities capabilities) {
                runOnUiThread(() -> updateNetworkStatus(cm));
            }
        });
    }

    private void updateNetworkStatus(ConnectivityManager cm) {
        Network activeNetwork = cm.getActiveNetwork();
        NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
        boolean connected = caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        Drawable dot = ContextCompat.getDrawable(this, R.drawable.ic_network_dot);
        if (dot != null) {
            dot = dot.mutate();
            dot.setTint(connected ? 0xFF66FF66 : 0xFFFF4444);
            networkStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(dot, null, null, null);
        }
        networkStatus.setText(connected ? "网络已连接" : "网络未连接");
    }

    // ───── Settings sliding panel ─────

    private void setupSettingsPanel() {
        // Settings icon opens the panel
        settingsIcon.setOnClickListener(v -> openSettingsPanel());

        // Close button inside the panel
        closePanel.setOnClickListener(v -> closeSettingsPanel());

        // Tapping the dimming overlay also closes the panel
        settingsOverlay.setOnClickListener(v -> closeSettingsPanel());

        // MQTT settings row opens the config dialog
        mqttSettingsRow.setOnClickListener(v -> showMqttSettingsDialog());
        updateMqttSummary();

        // License-plate callback row opens the config dialog
        lprCallbackRow.setOnClickListener(v -> showLprCallbackDialog());
        updateLprCallbackSummary();

        // Basic info row opens the config dialog
        basicInfoRow.setOnClickListener(v -> showBasicInfoDialog());
        updateBasicInfoSummary();

        // Signaling server row opens the config dialog
        signalingServerRow.setOnClickListener(v -> showSignalingServerDialog());
        updateSignalingServerSummary();
    }

    // ───── MQTT settings ─────

    private void updateMqttSummary() {
        String address = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_MQTT_ADDRESS, "");
        mqttSummary.setText(address.isEmpty() ? "未配置" : address);
    }

    private void showMqttSettingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_mqtt_settings);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialog.setCanceledOnTouchOutside(true);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        EditText addressInput = dialog.findViewById(R.id.dlgMqttAddressInput);
        EditText usernameInput = dialog.findViewById(R.id.dlgMqttUsernameInput);
        EditText passwordInput = dialog.findViewById(R.id.dlgMqttPasswordInput);
        EditText clientIdInput = dialog.findViewById(R.id.dlgMqttClientIdInput);

        addressInput.setText(prefs.getString(KEY_MQTT_ADDRESS, "tcp://192.168.1.100:1883"));
        usernameInput.setText(prefs.getString(KEY_MQTT_USERNAME, ""));
        passwordInput.setText(prefs.getString(KEY_MQTT_PASSWORD, ""));
        clientIdInput.setText(prefs.getString(KEY_MQTT_CLIENT_ID, ""));

        dialog.findViewById(R.id.dlgMqttSave).setOnClickListener(b -> {
            prefs.edit()
                    .putString(KEY_MQTT_ADDRESS, addressInput.getText().toString().trim())
                    .putString(KEY_MQTT_USERNAME, usernameInput.getText().toString().trim())
                    .putString(KEY_MQTT_PASSWORD, passwordInput.getText().toString().trim())
                    .putString(KEY_MQTT_CLIENT_ID, clientIdInput.getText().toString().trim())
                    .apply();
            updateMqttSummary();
            toast("已保存");
            dialog.dismiss();
        });

        dialog.show();
    }

    // ───── License-plate recognition callback ─────

    private void updateLprCallbackSummary() {
        String url = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_LPR_CALLBACK, "");
        lprCallbackSummary.setText(url.isEmpty() ? "未配置" : url);
    }

    private void showLprCallbackDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_lpr_callback);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialog.setCanceledOnTouchOutside(true);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        EditText callbackInput = dialog.findViewById(R.id.dlgLprCallbackInput);
        callbackInput.setText(prefs.getString(KEY_LPR_CALLBACK, "http://192.168.1.200:8080/api/plate"));
        dialog.findViewById(R.id.dlgLprSave).setOnClickListener(b -> {
            prefs.edit()
                    .putString(KEY_LPR_CALLBACK, callbackInput.getText().toString().trim())
                    .apply();
            updateLprCallbackSummary();
            toast("已保存");
            dialog.dismiss();
        });
        dialog.show();
    }

    // ───── Basic info: timestamp / camera ID / barrier ID ─────

    private void updateBasicInfoSummary() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cameraIdVal = prefs.getString(KEY_CAMERA_ID, "CAM-001");
        String barrierIdVal = prefs.getString(KEY_BARRIER_ID, "B-001");
        basicInfoSummary.setText(cameraIdVal + " · " + barrierIdVal);
    }

    private void showBasicInfoDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_basic_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialog.setCanceledOnTouchOutside(true);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        androidx.appcompat.widget.SwitchCompat timestampSw =
                dialog.findViewById(R.id.dlgTimestampSwitch);
        EditText cameraIdInput = dialog.findViewById(R.id.dlgCameraIdInput);
        EditText barrierIdDlg = dialog.findViewById(R.id.dlgBarrierIdInput);

        timestampSw.setChecked(prefs.getBoolean(KEY_TIMESTAMP_VISIBLE, true));
        cameraIdInput.setText(prefs.getString(KEY_CAMERA_ID, "CAM-001"));
        barrierIdDlg.setText(prefs.getString(KEY_BARRIER_ID, "B-001"));

        dialog.findViewById(R.id.dlgBasicSave).setOnClickListener(b -> {
            boolean checked = timestampSw.isChecked();
            String camId = cameraIdInput.getText().toString().trim();
            String barId = barrierIdDlg.getText().toString().trim();
            if (camId.isEmpty()) camId = "CAM-001";
            if (barId.isEmpty()) barId = "B-001";
            prefs.edit()
                    .putBoolean(KEY_TIMESTAMP_VISIBLE, checked)
                    .putString(KEY_CAMERA_ID, camId)
                    .putString(KEY_BARRIER_ID, barId)
                    .apply();
            timeVisible = checked;
            timeDisplay.setVisibility(checked ? View.VISIBLE : View.GONE);
            bottomCameraId.setText(camId);
            bottomBarrierId.setText(barId);
            updateBasicInfoSummary();
            toast("已保存");
            dialog.dismiss();
        });
        dialog.show();
    }

    // ───── Signaling server ─────

    private void updateSignalingServerSummary() {
        String server = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_SIGNALING_SERVER, "");
        signalingServerSummary.setText(server.isEmpty() ? "未配置" : server);
    }

    private void showSignalingServerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_signaling_server);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialog.setCanceledOnTouchOutside(true);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        EditText serverInput = dialog.findViewById(R.id.dlgSignalingServerInput);
        serverInput.setText(prefs.getString(KEY_SIGNALING_SERVER, "ws://192.168.1.100:8080"));
        dialog.findViewById(R.id.dlgSignalingSave).setOnClickListener(b -> {
            prefs.edit()
                    .putString(KEY_SIGNALING_SERVER, serverInput.getText().toString().trim())
                    .apply();
            updateSignalingServerSummary();
            toast("已保存");
            dialog.dismiss();
        });
        dialog.show();
    }

    private void openSettingsPanel() {
        if (isPanelOpen) return;
        isPanelOpen = true;

        // Make overlay and panel visible (panel starts off-screen via translationX in XML)
        settingsOverlay.setVisibility(View.VISIBLE);
        settingsPanel.setVisibility(View.VISIBLE);

        // Animate overlay fade-in
        settingsOverlay.setAlpha(0f);
        settingsOverlay.animate()
                .alpha(1f)
                .setDuration(250)
                .start();

        // Animate panel slide-in from right
        settingsPanel.setTranslationX(settingsPanel.getWidth());
        settingsPanel.animate()
                .translationX(0f)
                .setDuration(300)
                .start();
    }

    private void closeSettingsPanel() {
        if (!isPanelOpen) return;
        isPanelOpen = false;

        // Animate overlay fade-out
        settingsOverlay.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> settingsOverlay.setVisibility(View.GONE))
                .start();

        // Animate panel slide-out to right
        settingsPanel.animate()
                .translationX(settingsPanel.getWidth())
                .setDuration(250)
                .withEndAction(() -> {
                    settingsPanel.setVisibility(View.GONE);
                    settingsPanel.setTranslationX(settingsPanel.getWidth());
                })
                .start();
    }

    /**
     * Called from the back button press to close the panel if it's open.
     */
    @Override
    public void onBackPressed() {
        if (isPanelOpen) {
            closeSettingsPanel();
        } else {
            super.onBackPressed();
        }
    }

    private void startTimeUpdater() {
        final Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (timeVisible) {
                    timeDisplay.setText(timeFormat.format(new Date()));
                }
                timeHandler.postDelayed(this, 1000);
            }
        };
        tick.run();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSurface.getHolder().removeCallback(surfaceCallback);
        closeCamera();
        if (lpr != null) {
            lpr.close();
            lpr = null;
        }
        if (lprThread != null) {
            lprThread.quitSafely();
            lprThread = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                toast("Camera permission denied");
            }
        }
    }

    private String pickBackCameraId() {
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics cs = cameraManager.getCameraCharacteristics(id);
                Integer facing = cs.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id;
                }
            }
            String[] ids = cameraManager.getCameraIdList();
            return ids.length > 0 ? ids[0] : null;
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /** Pick the supported preview size whose aspect ratio best matches the (landscape) screen. */
    private Size choosePreviewSize() {
        if (cameraId == null) {
            return null;
        }
        try {
            CameraCharacteristics cs = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map =
                    cs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                return null;
            }
            Size[] raw = map.getOutputSizes(SurfaceHolder.class);
            if (raw == null || raw.length == 0) {
                raw = map.getOutputSizes(ImageFormat.PRIVATE);
            }
            if (raw == null || raw.length == 0) {
                return null;
            }

            WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds();
            int sw = Math.max(bounds.width(), bounds.height());
            int sh = Math.min(bounds.width(), bounds.height());
            double targetRatio = (double) sw / sh;

            // Compare aspect ratios in landscape-normalized space, but keep the
            // ORIGINAL Size object: setFixedSize() must receive a size the camera
            // actually advertises as a supported output, or session config fails.
            Size best = null;
            double bestDiff = 0;
            long bestArea = 0;
            for (Size s : raw) {
                int ow = s.getWidth();
                int oh = s.getHeight();
                int cw = Math.max(ow, oh); // landscape-normalized for comparison
                int ch = Math.min(ow, oh);
                if (cw > MAX_PREVIEW_WIDTH || ch > MAX_PREVIEW_HEIGHT) {
                    continue;
                }
                double ratio = (double) cw / ch;
                double diff = Math.abs(ratio - targetRatio);
                long area = (long) cw * ch;
                if (best == null
                        || diff < bestDiff - 1e-9
                        || (Math.abs(diff - bestDiff) < 1e-9 && area > bestArea)) {
                    bestDiff = diff;
                    bestArea = area;
                    best = s; // original, guaranteed-supported size
                }
            }
            return best != null ? best : raw[0];
        } catch (CameraAccessException e) {
            return null;
        }
    }

    /** Apply the chosen preview size to the surface (once). Letterboxes the view to match. */
    private void configureSurface(@NonNull SurfaceHolder holder) {
        if (surfaceConfigured) {
            return;
        }
        if (previewSize == null) {
            previewSize = choosePreviewSize();
        }
        if (previewSize != null) {
            cameraSurface.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            holder.setFixedSize(previewSize.getWidth(), previewSize.getHeight());
        }
        surfaceConfigured = true;
    }

    private void openCamera() {
        if (cameraId == null) {
            toast("No camera available");
            return;
        }
        if (!surfaceReady || cameraDevice != null || opening) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        try {
            opening = true;
            cameraManager.openCamera(cameraId, cameraCallback, null);
        } catch (CameraAccessException | SecurityException e) {
            opening = false;
            toast("Cannot open camera: " + e.getMessage());
        }
    }

    private void startPreview() {
        if (cameraDevice == null || !surfaceReady) {
            return;
        }
        Surface surface = cameraSurface.getHolder().getSurface();
        if (surface == null || !surface.isValid()) {
            return;
        }
        try {
            closePreviewSession();
            List<Surface> targets = new ArrayList<>();
            targets.add(surface);
            if (lprReader == null && previewSize != null) {
                lprReader = ImageReader.newInstance(previewSize.getWidth(),
                        previewSize.getHeight(), ImageFormat.YUV_420_888, 2);
                lprReader.setOnImageAvailableListener(lprFrameListener, lprHandler);
            }
            if (lprReader != null) {
                targets.add(lprReader.getSurface());
            }
            CaptureRequest.Builder builder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (Surface t : targets) {
                builder.addTarget(t);
            }
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            cameraDevice.createCaptureSession(
                    targets,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                session.setRepeatingRequest(builder.build(), null, null);
                            } catch (CameraAccessException e) {
                                toast("Preview start failed");
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            toast("Camera preview setup failed");
                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            toast("Camera session error");
        }
    }

    private void closePreviewSession() {
        if (captureSession != null) {
            try {
                captureSession.stopRepeating();
            } catch (CameraAccessException ignored) {
            }
            captureSession.close();
            captureSession = null;
        }
    }

    private void closeCamera() {
        closePreviewSession();
        opening = false;
        if (lprReader != null) {
            lprReader.close();
            lprReader = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    // ───── License-plate recognition ─────

    private void startLprEngine() {
        lprThread = new HandlerThread("lpr-recognizer");
        lprThread.start();
        lprHandler = new Handler(lprThread.getLooper());
        lprHandler.post(() -> {
            try {
                lpr = new LprRecognizer(MainActivity.this);
                Log.i("Lpr", "HyperLPR3 engine ready");
            } catch (Exception e) {
                Log.e("Lpr", "init failed", e);
                runOnUiThread(() -> toast("车牌识别引擎初始化失败"));
            }
        });
    }

    private final ImageReader.OnImageAvailableListener lprFrameListener = reader -> {
        if (lprBusy || lpr == null) {
            return; // 上一帧还在推理，直接丢帧
        }
        long now = System.currentTimeMillis();
        if (now - lastLprAt < 1000 / LPR_MAX_FPS) {
            return;
        }
        lastLprAt = now;
        Image image = reader.acquireLatestImage();
        if (image == null) {
            return;
        }
        lprBusy = true;
        try {
            Bitmap bmp = yuvToBitmap(image);
            if (bmp == null) {
                return;
            }
            java.util.List<LprRecognizer.PlateResult> results = lpr.recognize(bmp);
            showLprResults(results);
            reportPlateIfNeeded(results);
            bmp.recycle();
        } catch (Exception e) {
            Log.e("Lpr", "recognize failed", e);
        } finally {
            image.close();
            lprBusy = false;
        }
    };

    /** YUV_420_888 -> ARGB_8888。 */
    private Bitmap yuvToBitmap(Image image) {
        int w = image.getWidth();
        int h = image.getHeight();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yPlane = planes[0].getBuffer();
        ByteBuffer uPlane = planes[1].getBuffer();
        ByteBuffer vPlane = planes[2].getBuffer();
        int yRowStride = planes[0].getRowStride();
        int uvRowStride = planes[1].getRowStride();
        int uvPixelStride = planes[1].getPixelStride();

        int[] argb = new int[w * h];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int y = (yPlane.get(j * yRowStride + i) & 0xFF) - 16;
                if (y < 0) y = 0;
                int uv = (j >> 1) * uvRowStride + (i >> 1) * uvPixelStride;
                int u = (uPlane.get(uv) & 0xFF) - 128;
                int v = (vPlane.get(uv) & 0xFF) - 128;

                int r = (int) (1.164f * y + 1.596f * v);
                int g = (int) (1.164f * y - 0.392f * u - 0.813f * v);
                int b = (int) (1.164f * y + 2.017f * u);
                r = clamp255(r);
                g = clamp255(g);
                b = clamp255(b);
                argb[j * w + i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
        return Bitmap.createBitmap(argb, w, h, Bitmap.Config.ARGB_8888);
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }

    /** 在 UI 线程展示识别结果（取置信度最高者），并画框。 */
    private void showLprResults(java.util.List<LprRecognizer.PlateResult> results) {
        runOnUiThread(() -> {
            if (results == null || results.isEmpty()) {
                plateResult.setText("未识别到车牌");
                plateOverlay.clear();
                return;
            }
            LprRecognizer.PlateResult best = null;
            for (LprRecognizer.PlateResult r : results) {
                if (best == null || r.confidence > best.confidence) {
                    best = r;
                }
            }
            if (best == null) {
                return;
            }
            String typeName = plateTypeName(best.plateType);
            plateResult.setText(String.format(Locale.getDefault(),
                    "%s  %.2f  %s", best.plate, best.confidence, typeName));

            // 帧坐标 -> 屏幕坐标：SurfaceView 按预览比例居中，换算偏移+缩放。
            // 识别帧与 SurfaceView 显示的是同一路 buffer（同为 previewSize），方向一致，无需旋转。
            int vw = cameraSurface.getWidth();
            int vh = cameraSurface.getHeight();
            int fw = previewSize != null ? previewSize.getWidth() : vw;
            int fh = previewSize != null ? previewSize.getHeight() : vh;
            float sx = (float) vw / fw;
            float sy = (float) vh / fh;
            plateOverlay.setResults(results,
                    cameraSurface.getLeft(), cameraSurface.getTop(), sx, sy);
        });
    }

    /**
     * 把识别到的最高置信度车牌 POST 到配置的回调地址（已在 LPR 后台线程调用）。
     * 未配置地址 / 置信度不足 / 同一车牌冷却期内 都直接跳过。
     */
    private void reportPlateIfNeeded(java.util.List<LprRecognizer.PlateResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        String callback = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_LPR_CALLBACK, "");
        if (callback.trim().isEmpty()) {
            return;
        }
        LprRecognizer.PlateResult best = null;
        for (LprRecognizer.PlateResult r : results) {
            if (best == null || r.confidence > best.confidence) {
                best = r;
            }
        }
        if (best == null || best.confidence < LPR_REPORT_MIN_CONF) {
            return;
        }
        long now = System.currentTimeMillis();
        if (best.plate.equals(lastReportedPlate)
                && now - lastReportAt < LPR_REPORT_COOLDOWN_MS) {
            return; // 同一车牌冷却期内
        }

        final String plate = best.plate;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final String cameraId = prefs.getString(KEY_CAMERA_ID, "CAM-001");
        final String barrierId = prefs.getString(KEY_BARRIER_ID, "B-001");

        final String payload;
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("plate", plate);
            json.put("confidence", (double) best.confidence);
            json.put("plateType", best.plateType);
            json.put("cameraId", cameraId);
            json.put("barrierId", barrierId);
            json.put("timestamp", System.currentTimeMillis());
            payload = json.toString();
        } catch (Exception e) {
            Log.e("Lpr", "build payload failed", e);
            return;
        }

        lprHandler.post(() -> {
            boolean ok = postJson(callback, payload);
            if (ok) {
                lastReportedPlate = plate;
                lastReportAt = System.currentTimeMillis();
                Log.i("Lpr", "reported plate " + plate);
            } else {
                Log.w("Lpr", "report plate failed: " + callback);
            }
        });
    }

    /** 发送 JSON 到回调地址，返回是否成功。 */
    private boolean postJson(String urlStr, String json) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(body.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }
            int code = conn.getResponseCode();
            return code >= 200 && code < 300;
        } catch (Exception e) {
            Log.w("Lpr", "postJson error", e);
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String plateTypeName(int t) {
        switch (t) {
            case LprRecognizer.PLATE_BLUE: return "蓝牌";
            case LprRecognizer.PLATE_YELLOW_SINGLE: return "黄牌单层";
            case LprRecognizer.PLATE_YELLOW_DOUBLE: return "黄牌双层";
            case LprRecognizer.PLATE_WHITE_SINGLE: return "白牌";
            case LprRecognizer.PLATE_GREEN: return "绿牌";
            case LprRecognizer.PLATE_BLACK_HK_MACAO: return "港澳黑牌";
            case LprRecognizer.PLATE_HK_SINGLE: return "香港单层";
            case LprRecognizer.PLATE_HK_DOUBLE: return "香港双层";
            case LprRecognizer.PLATE_MACAO_SINGLE: return "澳门单层";
            case LprRecognizer.PLATE_MACAO_DOUBLE: return "澳门双层";
            default: return "未知";
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
