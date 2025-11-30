package org.firstinspires.ftc.teamcode.deTech;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;


public class Limelightlibrary {

    // 🔹 Change these for YOUR robot
    public static final double LIMELIGHT_MOUNT_ANGLE_DEG = 25.0; // angle above horizontal
    public static final double LIMELIGHT_LENS_HEIGHT_IN  = 12.25; 
    public static final double ARILTAG_HEIGHT          = 29.50; 

    private final Limelight3A limelight;

    public LimelightHelper(HardwareMap hardwareMap, String deviceName) {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
    }

    /** Call once in init() */
    public void start() {
        limelight.start();
    }

    public void stop() {
        limelight.stop();
    }

    public void setPipeline(int index) {
        limelight.pipelineSwitch(index);
    }

    public LLResult getLatestResult() {
        return limelight.getLatestResult();
    }

    public boolean hasValidTarget() {
        LLResult result = limelight.getLatestResult();
        return result != null && result.isValid();
    }

    public double getTx() {
        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid()) {
            return result.getTx();
        }
        return 0.0;
    }

    public double getTy() {
        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid()) {
            return result.getTy();
        }
        return 0.0;
    }

    public double getTa() {
        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid()) {
            return result.getTa();
        }
        return 0.0;
    }

    public double getDistanceInches() {
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) {
            return 0.0;
        }

        double ty = result.getTy(); 
        double totalAngleDeg = LIMELIGHT_MOUNT_ANGLE_DEG + ty;
        double angleRad = Math.toRadians(totalAngleDeg);

        double deltaHeight = APRILTAG_HEIGHT - LIMELIGHT_LENS_HEIGHT_IN;
        return deltaHeight / Math.tan(angleRad);
    }

    /** Same as above, but in meters. */
    public double getDistanceMeters() {
        return getDistanceInches() * 0.0254;
    }
}
