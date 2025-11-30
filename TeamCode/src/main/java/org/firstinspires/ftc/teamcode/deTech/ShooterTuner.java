package org.firstinspires.ftc.teamcode.deTech;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import org.firstinspires.ftc.teamcode.deTech.Limelightlibrary;

@TeleOp(name = "ShooterTuner", group = "Tuning")
public class ShooterTuner extends OpMode {

    private DcMotorEx shooterLeft  = null;
    private Servo hoodedServo      = null;
    private Limelight3A limelight  = null;   

    private double shooterPower = 0.0;
    private double hoodPos      = 0.50;
    
    private static final double POWER_STEP = 0.01;
    private static final double HOOD_STEP  = 0.005;
    private static final double STICK_DEADZONE = 0.3;

    private static final double TICKS_PER_REV = 28.0;

    private static final int MAX_SAMPLES = 10;
    private double[] sampleDist  = new double[MAX_SAMPLES];
    private double[] sampleRpm   = new double[MAX_SAMPLES];
    private double[] sampleHood  = new double[MAX_SAMPLES];
    private int sampleCount = 0;
    private boolean lastA = false;

    @Override
    public void init() {
        shooterLeft  = hardwareMap.get(DcMotorEx.class, "shooterLeft");
        hoodedServo  = hardwareMap.get(Servo.class, "hoodedServo");
        limelight    = hardwareMap.get(Limelight3A.class,"limelight");

        shooterLeft.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        shooterLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        hoodedServo.setPosition(hoodPos);

        // Optional: start limelight, set pipeline
        limelight.start();
        // limelight.pipelineSwitch(0);

        telemetry.addLine("ShooterTuner INIT");
        telemetry.addLine("Left stick X  -> shooter power");
        telemetry.addLine("Right stick X -> hood position");
        telemetry.addLine("Press A to record (distance, RPM, hood)");
        telemetry.update();
    }

    @Override
    public void loop() {
        //Adjust shooter power with left stick X 
        double lx = gamepad1.left_stick_x;
        if (Math.abs(lx) > STICK_DEADZONE) {
            shooterPower += lx * POWER_STEP;
        }
        shooterPower = Range.clip(shooterPower, 0.0, 1.0);

        //adjust hood position with right stick X
        double rx = gamepad1.right_stick_x;
        if (Math.abs(rx) > STICK_DEADZONE) {
            hoodPos += rx * HOOD_STEP;
        }
        hoodPos = Range.clip(hoodPos, 0.0, 1.0);

        shooterLeft.setPower(shooterPower);
        hoodedServo.setPosition(hoodPos);

        // Compute RPM from encoder velocity
        double ticksPerSec = shooterLeft.getVelocity();
        double rpm = (ticksPerSec * 60.0) / TICKS_PER_REV;

        double distance = 0.0;
        try {
            if (limelight != null) {
                distance = Limelightlibrary.getDistance(limelight);
            }
        } catch (Exception e) {
        }

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

 //       telemetry.addLine("=== Samples (press A to save) ===");
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
        limelight.stop(); 
        hoodedServo.setPosition(0.5);// if you have this
    }
}
