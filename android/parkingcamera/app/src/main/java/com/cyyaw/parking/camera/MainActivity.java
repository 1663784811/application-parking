package com.cyyaw.parking.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

    // Bottom bar views
    private ImageView cameraSwitchIcon;
    private TextView networkStatus;
    private ImageView settingsIcon;

    // Settings panel views
    private View settingsOverlay;
    private View settingsPanel;
    private ImageView closePanel;
    private EditText deviceIdInput;
    private TextView bottomCameraId;
    private EditText barrierIdInput;
    private TextView bottomBarrierId;

    // Camera IDs for the spinner
    private List<String> allCameraIds = new ArrayList<>();

    // Panel animation state
    private boolean isPanelOpen = false;

    // Time display
    private TextView timeDisplay;
    private androidx.appcompat.widget.SwitchCompat timestampSwitch;
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
        deviceIdInput = findViewById(R.id.deviceIdInput);
        bottomCameraId = findViewById(R.id.bottomCameraId);

        // Initialize time display
        timeDisplay = findViewById(R.id.timeDisplay);
        timestampSwitch = findViewById(R.id.timestampSwitch);

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

        // Timestamp switch controls time display visibility
        timestampSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timeVisible = isChecked;
            timeDisplay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Sync camera ID input to bottom bar display
        deviceIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                bottomCameraId.setText(s.length() > 0 ? s.toString() : "CAM-001");
            }
        });
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
            CaptureRequest.Builder builder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            cameraDevice.createCaptureSession(
                    Collections.singletonList(surface),
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
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
