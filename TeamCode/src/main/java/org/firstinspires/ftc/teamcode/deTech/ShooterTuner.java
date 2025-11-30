package org.firstinspires.ftc.teamcode.deTech;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "ShooterTuner", group = "Tuning")
public class ShooterTuner extends OpMode {

    private DcMotorEx shooterLeft  = null;
    private Servo hoodedServo      = null;
    private Limelight3A limelight  = null;   // use your wrapper / device

    // Current values we’re tuning
    private double shooterPower = 0.0;   // we’ll turn this into RPM readback
    private double hoodPos      = 0.40;  // starting hood position

    // Step sizes per loop
    private static final double POWER_STEP = 0.01;
    private static final double HOOD_STEP  = 0.005;
    private static final double STICK_DEADZONE = 0.3;

    private static final double TICKS_PER_REV = 28.0;

    // Simple data logging
    private static final int MAX_SAMPLES = 10;
    private double[] sampleDist  = new double[MAX_SAMPLES];
    private double[] sampleRpm   = new double[MAX_SAMPLES];
    private double[] sampleHood  = new double[MAX_SAMPLES];
    private int sampleCount = 0;
    private boolean lastA = false;

    @Override
    public void init() {
        shooterLeft  = hardwareMap.get(DcMotorEx.class, "shooterLeft");
        hoodedServo  = hardwareMap.get(Servo.class,      "hoodedServo");
        limelight    = hardwareMap.get(Limelight3A.class,"limelight");

        shooterLeft.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        shooterLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        hoodedServo.setPosition(hoodPos);

        // If your Limelight needs this:
        // limelight.start();
        // limelight.setPipeline(0);

        telemetry.addLine("ShooterHoodTuner INIT");
        telemetry.addLine("Left stick X  shooter power");
        telemetry.addLine("Right stick X hoodedservo position");
        telemetry.addLine("Press A to record (distance, RPM, hood)");
        telemetry.update();
    }

    @Override
    public void loop() {
        double lx = gamepad1.left_stick_x;
        if (Math.abs(lx) > STICK_DEADZONE) {
            shooterPower += lx * POWER_STEP;
        }
        shooterPower = Range.clip(shooterPower, 0.0, 1.0);

        // === Adjust hood position with right stick X, I know its kinda wobbly but like when u move the stick to the right like move it tothe opposite direction for like split second it will stop
      // so the purpose of this is to gain data about what hoood positino at what distance is accurate, so i can design accurate physics-based function on like
      //so i can design the accurate function that bases off these data;
      // do 3 sample for closed hood position
      // so our hood position has two positions -> Far hood means like shooting from far (far distance shooting closes the hood) -> this is so that when u shoot from the far, it lasunches into the backbaord so it doenst fall out;
      // our hood position's close_hood is like when shooting from close distance, when we have to open the angle so the ball goes in. 
      
        double rx = gamepad1.right_stick_x;
        if (Math.abs(rx) > STICK_DEADZONE) {
            hoodPos += rx * HOOD_STEP;
        }
        hoodPos = Range.clip(hoodPos, 0.0, 1.0);

        // Apply to hardware
        shooterLeft.setPower(shooterPower);
        hoodedServo.setPosition(hoodPos);

        double ticksPerSec = shooterLeft.getVelocity();
        double rpm = (ticksPerSec * 60.0) / TICKS_PER_REV;
//Venka this is the distance function you have to import limelightDistance class
        double distance = 0.0;
        try {
            distance = limelight.getDistance();   // your method
        } catch (Exception e) {
        }

        // === Log sample when A is pressed (on rising edge) ===
        boolean a = gamepad1.a;
        if (a && !lastA && sampleCount < MAX_SAMPLES) {
            sampleDist[sampleCount] = distance;
            sampleRpm[sampleCount]  = rpm;
            sampleHood[sampleCount] = hoodPos;
            sampleCount++;
        }
        lastA = a;

        // Telemetry
        telemetry.addLine("=== Live Values ===");
        telemetry.addData("Power", "%.3f", shooterPower);
        telemetry.addData("RPM",   "%.1f", rpm);
        telemetry.addData("Hood",  "%.3f", hoodPos);
        telemetry.addData("Dist",  "%.2f", distance);

        telemetry.addLine("=== Samples (press A to save) ===");
        for (int i = 0; i < sampleCount; i++) {
            telemetry.addData(
                    "S" + i,
                    "d=%.2f  rpm=%.1f  hood=%.3f",
                    sampleDist[i], sampleRpm[i], sampleHood[i]
            );
        }
        telemetry.update();
    }

    @Override
    public void stop() {
        shooterLeft.setPower(0);
        // limelight.stop(); // if you have this
    }
}
