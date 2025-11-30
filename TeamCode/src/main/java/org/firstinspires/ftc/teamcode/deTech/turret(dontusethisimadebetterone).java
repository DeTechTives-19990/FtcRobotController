package org.firstinspires.ftc.teamcode.deTech;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.deTech.vision.Limelight3A;
import org.firstinspires.ftc.teamcode.deTech.Limelight3A;
import org.firstinspires.ftc.teamcode.deTech.LimelightDistance;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;




// Your Limelight class – adjust package if needed

@TeleOp(name = "turret.java", group = "Iterative OpMode")
public class turret extends OpMode {

    private DcMotorEx shooterLeft  = null;
  //  private DcMotorEx shooterRight = null;
    private Servo hoodedServo      = null;
    private Limelight3A limelight  = null;   // works like DcMotor

    // ====== CONSTANTS TO TUNE ======

    // Encoder ticks per *output shaft* rev of your shooter motor
    // Example: goBILDA 435RPM Yellow Jacket: 28 * 13.7 ≈ 384
    private static final double TICKS_PER_REV = placeholder;   // TODO: set correctly

    // Distance range (meters) you care about
    // we dont know the distance yet so; palceholder
    private static final double MIN_DIST_M = placeholder;        // close shot
    private static final double MAX_DIST_M = placeholder;        // far shot

    // Shooter RPM at close / far (find these on the field)
    private static final double MIN_RPM = 2400;          // at MIN_DIST_M (check and tune) 
    private static final double MAX_RPM = 4200;          // at MAX_DIST_M (check and tune)

    // Hood servo positions (0–1) at close / far
    private static final double MIN_HOOD_POS = 0.30;     // at MIN_DIST_M (check and tune)
    private static final double MAX_HOOD_POS = 0.60;     // at MAX_DIST_M (Check and tune before)

    @Override
    public void init() {
        telemetry.addData("Status", "Init start");

        // --- Map hardware ---
        shooterLeft  = hardwareMap.get(DcMotorEx.class, "shooterLeft");
     //   shooterRight = hardwareMap.get(DcMotorEx.class, "shooterRight");
        hoodedServo  = hardwareMap.get(Servo.class,      "hoodedServo");
        limelight    = hardwareMap.get(Limelight3A.class, "limelight");

        shooterLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    //    shooterRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        shooterLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    //    shooterRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        shooterLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      //  shooterRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
//IDK I forgot which one has to spin counterclockwise -> tune tmr
        shooterRight.setDirection(DcMotor.Direction.REVERSE);

        hoodedServo.setPosition(MIN_HOOD_POS);

        // If your Limelight needs any extra init, do it here
        // e.g. limelight.setPipeline(0); limelight.ledOn(); etc.
        limelight.start();
      // testing blue alliance april tag first
        limelight.setPipeline(0)
        telemetry.addData("Status", "Init complete");
        telemetry.update();
    }

    @Override
    public void loop() {
        // 1) Get distance from Limelight in meters
        double distanceMeters = limelight.getDistance();   // your method in Limelight3A
        
        // 2) Clamp distance into calibrated range
        double d = Range.clip(distanceMeters, MIN_DIST_M, MAX_DIST_M);

        // 3) Interpolation factor from 0 (close) to 1 (far)
        double t = (d - MIN_DIST_M) / (MAX_DIST_M - MIN_DIST_M);

        // 4) Distance → RPM
        double targetRpm = MIN_RPM + t * (MAX_RPM - MIN_RPM);

        // 5) RPM → ticks per second
        double targetTicksPerSec = (targetRpm * TICKS_PER_REV) / 60.0;

        // 6) Distance → hood servo position
        double targetHoodPos = MIN_HOOD_POS + t * (MAX_HOOD_POS - MIN_HOOD_POS);
        targetHoodPos = Range.clip(targetHoodPos, 0.0, 1.0);

        // 7) Only spin shooter while RB is held
        boolean enableShooter = gamepad1.right_bumper;

        if (enableShooter) {
            shooterLeft.setVelocity(targetTicksPerSec);
       //     shooterRight.setVelocity(targetTicksPerSec);
            hoodedServo.setPosition(targetHoodPos);
        } else {
            shooterLeft.setVelocity(0);
       //     shooterRight.setVelocity(0);
        }

        telemetry.addData("LL distance (m)", distanceMeters);
        telemetry.addData("Clamped d", d);
        telemetry.addData("t (0–1)", t);
        telemetry.addData("Target RPM", targetRpm);
        telemetry.addData("Ticks/sec", targetTicksPerSec);
        telemetry.addData("Hood pos", targetHoodPos);
        telemetry.addData("Shooter ON", enableShooter);
        telemetry.update();
    }

    @Override
    public void stop() {
        shooterLeft.setVelocity(0);
    //    shooterRight.setVelocity(0);
        limelight.stop();
        telemetry.addData("Status", "Stopped");
        telemetry.update();
    }
}
