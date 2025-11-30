// this is insanely complex venka dont worry about all of this 


package org.firstinspires.ftc.teamcode.deTech;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import org.firstinspires.ftc.teamcode.deTech.Limelightlibrary;

@TeleOp(name = "turret", group = "Iterative OpMode")
public class turret extends OpMode {

    // === Hardware ===
    private DcMotorEx shooterLeft;
    private DcMotorEx turretMotor;
    private Servo hoodedServo;
    private Limelight3A limelight;

    // === Shooter encoder ===
    private static final double TICKS_PER_REV_SHOOTER = 28.0; // bare motor

    // Hood presets
    private static final double CLOSE_HOOD_POS = 0.55; // close/mid shots
    private static final double FAR_HOOD_POS   = 0.35; // long shots

    // distance where we switch hood mode
    private static final double HOOD_SWITCH_DIST_INCHES = 90.0; // all placeholder we dont know yet

    private static final double GRAVITY_IN_PER_S2       = 386.09;
    private static final double SHOOTER_HEIGHT_IN       = 18.0;  // TODO measure
    private static final double TARGET_HEIGHT_IN        = 29.5;  // TODO measure
    private static final double SHOOTER_ANGLE_CLOSE_DEG = 55.0;  // TODO tune
    private static final double SHOOTER_ANGLE_FAR_DEG   = 35.0;  // TODO tune
    private static final double WHEEL_RADIUS_IN         = 2.0;   // TODO measure
    private static final double GEAR_RATIO_MOTOR_TO_WHEEL = 1.0; // motorRPM * ratio = wheelRPM

    private static final double MIN_SHOOTER_RPM         = 1500.0; // all placeholder 
    private static final double MAX_SHOOTER_RPM         = 5000.0; //all placeholder

    // === Hybrid calibration arrays (placeholders) ===
    // Replace these with real values after tuning.
    private static final double[] CLOSE_SAMPLE_DIST_IN  = {40.0, 60.0, 80.0}; // i gotta put all data points acquired by the shootertune code
    private static final double[] CLOSE_SAMPLE_RPM      = {2600.0, 2800.0, 3000.0};
    private static final double[] FAR_SAMPLE_DIST_IN    = {80.0, 110.0, 140.0}; 
    private static final double[] FAR_SAMPLE_RPM        = {3200.0, 3600.0, 4000.0};

    private static final double TURRET_TICKS_PER_REV = 1440.0; // find how many ticks one revolution is on the turret motor !!
    private static final double TURRET_MAX_TURNS     = 3.0; // adjust how many rotations it needs to have 
    private static final int    TURRET_SOFT_LIMIT_TICKS = (int)(TURRET_TICKS_PER_REV * TURRET_MAX_TURNS);

    private static final double TURRET_UNWIND_START_TURNS = 2.5;
    private static final int    TURRET_UNWIND_START_TICKS = (int)(TURRET_TICKS_PER_REV * TURRET_UNWIND_START_TURNS);

    private static final int    TURRET_UNWIND_DONE_BAND_TICKS = 20;
    private static final double TURRET_UNWIND_POWER           = 0.20;

    private static final double TURRET_KP        = 0.02; // auto-aim P gain
    private static final double TURRET_MAX_POWER = 0.30;

    private boolean isUnwinding = false;

    @Override
    public void init() {
        shooterLeft = hardwareMap.get(DcMotorEx.class, "shooterLeft");
        turretMotor = hardwareMap.get(DcMotorEx.class, "turret");
        hoodedServo = hardwareMap.get(Servo.class,      "hoodedServo");
        limelight   = hardwareMap.get(Limelight3A.class,"limelight");

        shooterLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooterLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        turretMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        limelight.start();

        hoodedServo.setPosition(CLOSE_HOOD_POS);

        telemetry.addLine("turret intialization is done"); 
        telemetry.update();
    }

    @Override
    public void loop() {
        boolean hasTarget     = Limelightlibrary.hasTarget(limelight);
        double txDegrees      = Limelightlibrary.getTx(limelight);
        double distanceInches = Limelightlibrary.getDistance(limelight);

        // Choose hood mode
        boolean useCloseHood = (distanceInches <= HOOD_SWITCH_DIST_INCHES);
        if (useCloseHood) {
            hoodedServo.setPosition(CLOSE_HOOD_POS);
        } else {
            hoodedServo.setPosition(FAR_HOOD_POS);
        }

        // Shooter RPM from hybrid model
        double targetRpm = rpmFromDistance(distanceInches, useCloseHood);
        double targetTicksPerSec = (targetRpm * TICKS_PER_REV_SHOOTER) / 60.0;

        boolean shooterEnabled = gamepad1.right_bumper;
        if (shooterEnabled) {
            shooterLeft.setVelocity(targetTicksPerSec);
        } else {
            shooterLeft.setVelocity(0);
        }

        // Turret apriltag alignment logic
        int turretPos = turretMotor.getCurrentPosition();
        boolean insideHardLimits = Math.abs(turretPos) < TURRET_SOFT_LIMIT_TICKS;

        if (!isUnwinding && Math.abs(turretPos) >= TURRET_UNWIND_START_TICKS) {
            isUnwinding = true;
        }

        double turretPower = 0.0;

        if (isUnwinding) {
            if (turretPos > TURRET_UNWIND_DONE_BAND_TICKS) {
                turretPower = -TURRET_UNWIND_POWER;
            } else if (turretPos < -TURRET_UNWIND_DONE_BAND_TICKS) {
                turretPower = TURRET_UNWIND_POWER;
            } else {
                turretPower = 0.0;
                isUnwinding = false;
            }
        } else {

            if (hasTarget && insideHardLimits) {
                double errorDeg = txDegrees; 
                turretPower = Range.clip(errorDeg * TURRET_KP, -TURRET_MAX_POWER, TURRET_MAX_POWER);
            }

        turretMotor.setPower(turretPower);

        // Telemetry
        telemetry.addLine("=== Turret ===");
        telemetry.addData("hasTarget", hasTarget);
        telemetry.addData("tx (deg)", "%.2f", txDegrees);
        telemetry.addData("turretPos (ticks)", turretPos);
        telemetry.addData("insideHardLimits", insideHardLimits);
        telemetry.addData("isUnwinding", isUnwinding);
        telemetry.addData("turretPower", "%.2f", turretPower);

        telemetry.addLine("=== Shooter ===");
        telemetry.addData("distance (in)", "%.1f", distanceInches);
        telemetry.addData("useCloseHood", useCloseHood);
        telemetry.addData("hoodPos", "%.2f", hoodedServo.getPosition());
        telemetry.addData("targetRPM", "%.1f", targetRpm);
        telemetry.addData("shooterEnabled (RB)", shooterEnabled);

        telemetry.update();
    }

    @Override
    public void stop() {
        shooterLeft.setVelocity(0);
        turretMotor.setPower(0);
        limelight.stop();
    }

// this interpolates based on our 6 array data and physics -> dont worry about it 
    private double rpmFromDistance(double distanceInches, boolean closeMode) {
        double baseRpm    = physicsRpmFromDistance(distanceInches, closeMode);
        double correction = interpolateRpmError(distanceInches, closeMode);
        double finalRpm   = baseRpm + correction;
        return Range.clip(finalRpm, MIN_SHOOTER_RPM, MAX_SHOOTER_RPM);
    }

    private double physicsRpmFromDistance(double distanceInches, boolean closeMode) {
        if (distanceInches <= 1.0) {
            return MIN_SHOOTER_RPM;
        }

        double angleDeg = closeMode ? SHOOTER_ANGLE_CLOSE_DEG : SHOOTER_ANGLE_FAR_DEG;
        double theta    = Math.toRadians(angleDeg);

        double d      = distanceInches;
        double deltaH = TARGET_HEIGHT_IN - SHOOTER_HEIGHT_IN;

        double cosT = Math.cos(theta);
        double tanT = Math.tan(theta);

        double denom = 2.0 * cosT * cosT * (d * tanT - deltaH);
        if (denom <= 0.0) {
            return closeMode ? (MIN_SHOOTER_RPM + MAX_SHOOTER_RPM) / 2.0 : MAX_SHOOTER_RPM;
        }

        double vSquared = (GRAVITY_IN_PER_S2 * d * d) / denom;
        if (vSquared <= 0.0) {
            return MIN_SHOOTER_RPM;
        }

        double v = Math.sqrt(vSquared); // in/s

        if (WHEEL_RADIUS_IN <= 0.0) {
            return MIN_SHOOTER_RPM;
        }

        double omegaRadPerSec = v / WHEEL_RADIUS_IN;
        double wheelRpm = omegaRadPerSec * 60.0 / (2.0 * Math.PI);
        double motorRpm = wheelRpm * GEAR_RATIO_MOTOR_TO_WHEEL;
        return Range.clip(motorRpm, MIN_SHOOTER_RPM, MAX_SHOOTER_RPM);
    }
// All of this is interpolation within the physics-based function dont worry about it venka
    private double interpolateRpmError(double distanceInches, boolean closeMode) {
        double[] distArray;
        double[] measuredArray;

        if (closeMode) {
            distArray     = CLOSE_SAMPLE_DIST_IN;
            measuredArray = CLOSE_SAMPLE_RPM;
        } else {
            distArray     = FAR_SAMPLE_DIST_IN;
            measuredArray = FAR_SAMPLE_RPM;
        }

        int n = distArray.length;
        if (n == 0) return 0.0;

        if (n == 1) {
            double physicsAtD = physicsRpmFromDistance(distArray[0], closeMode);
            return measuredArray[0] - physicsAtD;
        }

        if (distanceInches <= distArray[0]) {
            distanceInches = distArray[0];
        } else if (distanceInches >= distArray[n - 1]) {
            distanceInches = distArray[n - 1];
        }

        int i;
        for (i = 0; i < n - 1; i++) {
            if (distanceInches <= distArray[i + 1]) break;
        }

        double d0 = distArray[i];
        double d1 = distArray[i + 1];

        double measured0 = measuredArray[i];
        double measured1 = measuredArray[i + 1];

        double physics0 = physicsRpmFromDistance(d0, closeMode);
        double physics1 = physicsRpmFromDistance(d1, closeMode);

        double error0 = measured0 - physics0;
        double error1 = measured1 - physics1;

        double t = (d1 - d0) > 0.0 ? (distanceInches - d0) / (d1 - d0) : 0.0;
        t = Range.clip(t, 0.0, 1.0);

        return error0 + t * (error1 - error0);
    }
}
