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
        double distance = (29.5 - 12.25) / Math.tan(angleRad);
        return distance;

        //try this if it doenst work:
    //     NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
    // NetworkTableEntry ty = table.getEntry("ty");
    // double targetOffsetAngle_Vertical = ty.getDouble(0.0);

    // // how many degrees back is your limelight rotated from perfectly vertical?
    // double limelightMountAngleDegrees = 25.0; 

    // // distance from the center of the Limelight lens to the floor
    // double limelightLensHeightInches = 20.0; 

    // // distance from the target to the floor
    // double goalHeightInches = 60.0; 

    // double angleToGoalDegrees = limelightMountAngleDegrees + targetOffsetAngle_Vertical;
    // double angleToGoalRadians = angleToGoalDegrees * (3.14159 / 180.0);

    // //calculate distance
    // double distanceFromLimelightToGoalInches = (goalHeightInches - limelightLensHeightInches) / Math.tan(angleToGoalRadians)
    }
}
