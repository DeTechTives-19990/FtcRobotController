//** Wait venka dont touch any of this dont worry about shooting i'll finish it during class next week;
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

    private static final double TICKS_PER_REV_SHOOTER = 28.0;  
    private static final double CLOSE_HOOD_POS        = 0.55; //Tune 
    private static final double FAR_HOOD_POS          = 0.45; //Tune

    private static final double POWER_STEP     = 0.01;
    private static final double STICK_DEADZONE = 0.3;

    private static final int MAX_SAMPLES = 10;
    private double[] sampleDist  = new double[MAX_SAMPLES];
    private double[] sampleRpm   = new double[MAX_SAMPLES];
    private double[] sampleHood  = new double[MAX_SAMPLES];
    private String[] sampleMode  = new String[MAX_SAMPLES];  
    private int sampleCount      = 0;
    private boolean lastA        = false;

    private double shooterPower  = 0.0;
    private double currentHoodPos = CLOSE_HOOD_POS;
    private boolean closeMode    = true; 
    @Override
    public void init() {
        shooterLeft = hardwareMap.get(DcMotorEx.class, "shooterLeft");
        hoodedServo = hardwareMap.get(Servo.class,      "hoodedServo");
        limelight   = hardwareMap.get(Limelight3A.class,"limelight");

        shooterLeft.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        shooterLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        limelight.start();
        closeMode = true;
        currentHoodPos = CLOSE_HOOD_POS;
        hoodedServo.setPosition(currentHoodPos);

        telemetry.addLine("ShooterTuner INIT");
        telemetry.addLine("Controls:");
        telemetry.addLine("  Left stick X: adjust shooter power");
        telemetry.addLine("  X: CLOSE hood preset");
        telemetry.addLine("  Y: FAR hood preset");
        telemetry.addLine("  A: record sample (distance, RPM, hood, mode)");
        telemetry.update();
    }

    @Override
    public void loop() {
        if (gamepad1.x) {
            closeMode = true;
            currentHoodPos = CLOSE_HOOD_POS;
        } else if (gamepad1.y) {
            closeMode = false;
            currentHoodPos = FAR_HOOD_POS;
        }

        double lx = gamepad1.left_stick_x;
        if (Math.abs(lx) > STICK_DEADZONE) {
            shooterPower += lx * POWER_STEP;
        }
        shooterPower = Range.clip(shooterPower, 0.0, 1.0);

        shooterLeft.setPower(shooterPower);
        hoodedServo.setPosition(currentHoodPos);

        double ticksPerSec = shooterLeft.getVelocity();
        double rpm = (ticksPerSec * 60.0) / TICKS_PER_REV_SHOOTER;

        double distanceInches = 0.0;
        try {
            distanceInches = Limelightlibray.getDistance(limelight);
        } catch (Exception e) {
            // ignore if LL not happy
        }

        boolean a = gamepad1.a;
        if (a && !lastA && sampleCount < MAX_SAMPLES) {
            sampleDist[sampleCount]  = distanceInches;
            sampleRpm[sampleCount]   = rpm;
            sampleHood[sampleCount]  = currentHoodPos;
            sampleMode[sampleCount]  = closeMode ? "C" : "F";  // C = close hood, F = far hood
            sampleCount++;
        }
        lastA = a;

        telemetry.addLine("=== Live ===");
        telemetry.addData("Power", "%.3f", shooterPower);
        telemetry.addData("RPM",   "%.1f", rpm);
        telemetry.addData("Dist (in)", "%.1f", distanceInches);
        telemetry.addData("HoodPos", "%.3f", currentHoodPos);
        telemetry.addData("Mode", closeMode ? "CLOSE" : "FAR");

        telemetry.addLine("=== Samples (Press A) ===");
        for (int i = 0; i < sampleCount; i++) {
            telemetry.addData(
                    "S" + i,
                    "%s d=%.1f in  rpm=%.1f  hood=%.3f",
                    sampleMode[i], sampleDist[i], sampleRpm[i], sampleHood[i]
            );
        }

        telemetry.update();
    }

    @Override
    public void stop() {
        shooterLeft.setPower(0);
        limelight.stop();
    }
}
