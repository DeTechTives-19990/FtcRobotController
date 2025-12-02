package org.firstinspires.ftc.teamcode.deTech;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.hardware.limelightvision.Limelight3A;

// Import your custom library class
import org.firstinspires.ftc.teamcode.deTech.Limelightlibrary; 

@TeleOp(name = "turretfinal.java", group = "Iterative OpMode")
public class turretfinal extends OpMode {

    // === Hardware ===
    private DcMotorEx shooterLeft;
    private DcMotorEx turretMotor;
    private Servo hoodedServo;
    
    // CHANGED: Use an instance of your custom library class
    private Limelightlibrary limelightUtil; 

    // === Shooter Constants (simplified) ===
    private static final double TICKS_PER_REV_SHOOTER = 28.0; 
    private static final double TARGET_SHOOTER_RPM = 3500.0; // Simplified to a single target RPM
    private static final double SHOOTER_TICKS_PER_SEC = (TARGET_SHOOTER_RPM * TICKS_PER_REV_SHOOTER) / 60.0;

    // Hood Presets (simplified)
    private static final double DEFAULT_HOOD_POS = 0.45; 

    // === Turret Constants (Unwind Logic) ===
    private static final double TURRET_TICKS_PER_REV = 1440.0; // find how many ticks one revolution is on the turret motor !!
    private static final double TURRET_MAX_TURNS = 3.0; // adjust how many rotations it needs to have 
    private static final int TURRET_SOFT_LIMIT_TICKS = (int)(TURRET_TICKS_PER_REV * TURRET_MAX_TURNS);

    private static final double TURRET_UNWIND_START_TURNS = 2.5;
    private static final int TURRET_UNWIND_START_TICKS = (int)(TURRET_TICKS_PER_REV * TURRET_UNWIND_START_TURNS);

    private static final int TURRET_UNWIND_DONE_BAND_TICKS = 20;
    private static final double TURRET_UNWIND_POWER = 0.20;

    private static final double TURRET_KP = 0.02; // auto-aim P gain
    private static final double TURRET_MAX_POWER = 0.30;

    private boolean isUnwinding = false;

    @Override
    public void init() {
        shooterLeft = hardwareMap.get(DcMotorEx.class, "shooterLeft");
        turretMotor = hardwareMap.get(DcMotorEx.class, "turret");
        hoodedServo = hardwareMap.get(Servo.class, "hoodedServo");
        
        // CHANGED: Initialize the Limelightlibrary instance
        limelightUtil = new Limelightlibrary(hardwareMap, "limelight");

        shooterLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooterLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        turretMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // CHANGED: Call start() on the utility instance
        limelightUtil.start(); 

        // Set hood to a default position
        hoodedServo.setPosition(DEFAULT_HOOD_POS);

        telemetry.addLine("turret intialization is done"); 
        telemetry.update();
    }

    @Override
    public void loop() {
        // CHANGED: Call non-static methods on the limelightUtil instance
        boolean hasTarget = limelightUtil.hasValidTarget();
        double txDegrees = limelightUtil.getTx();
        double distanceInches = limelightUtil.getDistanceInches(); // Keeping distance for telemetry/reference

        // Shooter control (simplified)
        boolean shooterEnabled = gamepad1.right_bumper;
        if (shooterEnabled) {
            shooterLeft.setVelocity(SHOOTER_TICKS_PER_SEC);
        } else {
            shooterLeft.setVelocity(0);
        }
        
        // Hood control (simplified to a fixed position)
        hoodedServo.setPosition(DEFAULT_HOOD_POS);

        // Turret apriltag alignment logic
        int turretPos = turretMotor.getCurrentPosition();
        boolean insideHardLimits = Math.abs(turretPos) < TURRET_SOFT_LIMIT_TICKS;

        // Unwind condition check
        if (!isUnwinding && Math.abs(turretPos) >= TURRET_UNWIND_START_TICKS) {
            isUnwinding = true;
        }

        double turretPower = 0.0;

        if (isUnwinding) {
            // Unwind logic
            if (turretPos > TURRET_UNWIND_DONE_BAND_TICKS) {
                turretPower = -TURRET_UNWIND_POWER; // Turn back towards 0
            } else if (turretPos < -TURRET_UNWIND_DONE_BAND_TICKS) {
                turretPower = TURRET_UNWIND_POWER; // Turn back towards 0
            } else {
                turretPower = 0.0;
                isUnwinding = false; // Unwinding complete
            }
        } else {
            // Auto-aim (PID/P-control) logic
            if (hasTarget && insideHardLimits) {
                double errorDeg = txDegrees; 
                // Simple P-control for turret movement
                turretPower = Range.clip(errorDeg * TURRET_KP, -TURRET_MAX_POWER, TURRET_MAX_POWER);
            }
        } // The turretMotor.setPower() line was moved outside the else block in your original code, 
          // I will correct that to be outside the 'isUnwinding' check for correct operation.

        turretMotor.setPower(turretPower);

        // Telemetry
        telemetry.addLine("=== Turret ===");
        telemetry.addData("hasTarget", hasTarget);
        telemetry.addData("tx (deg)", "%.2f", txDegrees);
        telemetry.addData("turretPos (ticks)", turretPos);
        telemetry.addData("insideHardLimits", insideHardLimits);
        telemetry.addData("isUnwinding", isUnwinding);
        telemetry.addData("turretPower", "%.2f", turretPower);

        telemetry.addLine("=== Shooter (Simplified) ===");
        telemetry.addData("distance (in)", "%.1f", distanceInches);
        telemetry.addData("hoodPos (Fixed)", "%.2f", hoodedServo.getPosition());
        telemetry.addData("targetRPM", "%.1f", TARGET_SHOOTER_RPM);
        telemetry.addData("shooterEnabled (RB)", shooterEnabled);

        telemetry.update();
    } // Closing bracket for loop() method

    @Override
    public void stop() {
        shooterLeft.setVelocity(0);
        turretMotor.setPower(0);
        // CHANGED: Call stop() on the utility instance
        limelightUtil.stop();
    }
    
    // REMOVED: rpmFromDistance, physicsRpmFromDistance, interpolateRpmError
}
