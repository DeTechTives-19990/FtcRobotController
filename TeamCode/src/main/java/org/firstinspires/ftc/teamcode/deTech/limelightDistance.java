package org.firstinspires.ftc.teamcode.deTech;

public class LimelightDistance {

    public static final double LIMELIGHT_MOUNT_ANGLE = 30.0; // degrees
    public static final double LIMELIGHT_HEIGHT = placeholder;      // inches
    public static final double TARGET_HEIGHT = placeholder;         // inches

    public static double getDistance(Limelight3A limelight) {
        double ty = limelight.getTy();
        double totalAngle = LIMELIGHT_MOUNT_ANGLE + ty; /// we gotta find the angle of limelight mounted
        double angleRad = Math.toRadians(totalAngle);

        // Distance in SAME UNITS as heights above
        double distance = (TARGET_HEIGHT - LIMELIGHT_HEIGHT) / Math.tan(angleRad);
        return distance;
    }
}
