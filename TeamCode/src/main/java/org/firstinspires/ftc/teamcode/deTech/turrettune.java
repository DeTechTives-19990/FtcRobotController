package org.firstinspires.ftc.teamcode.deTech;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "turrettune.java", group = "Test")
public class turrettune extends OpMode {

    private DcMotorEx shooterLeft;
    private DcMotorEx shooterRight;
    private Servo hoodedServo;

    // values you’re tweaking
    private double shooterPower = 0.0;
    private double hoodPos = 0.5;

    // how fast it changes when you push the stick
    private static final double POWER_STEP = 0.01;
    private static final double SERVO_STEP = 0.01;

    // TODO: set this right for your shooter motor
    // e.g. goBILDA 435RPM Yellow Jacket ≈ 384.0
    private static final double TICKS_PER_REV = 384.0;

    @Override
    public void init() {
        shooterLeft  = hardwareMap.get(DcMotorEx.class, "shooterLeft");
        shooterRight = hardwareMap.get(DcMotorEx.class, "shooterRight");
        hoodedServo  = hardwareMap.get(Servo.class,    "hoodedServo");

        shooterLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        // so both wheels spin same direction
        shooterRight.setDirection(DcMotor.Direction.REVERSE);

        hoodPos = 0.5;
        hoodedServo.setPosition(hoodPos);

        telemetry.addLine("Simple shooter/servo RPM tuner ready");
        telemetry.update();
    }

    @Override
    public void loop() {
        // ==== LEFT STICK X -> shooter power ====
        double lsx = gamepad1.left_stick_x;

        if (lsx > 0.1) {                 // push right -> increase power
            shooterPower += POWER_STEP;
        } else if (lsx < -0.1) {         // push left -> decrease power
            shooterPower -= POWER_STEP;
        }
        shooterPower = Range.clip(shooterPower, -1.0, 1.0);

        // ==== RIGHT STICK X -> servo position ====
        double rsx = gamepad1.right_stick_x;

        if (rsx > 0.1) {                 // push right -> increase servo pos
            hoodPos += SERVO_STEP;
        } else if (rsx < -0.1) {         // push left -> decrease servo pos
            hoodPos -= SERVO_STEP;
        }
        hoodPos = Range.clip(hoodPos, 0.0, 1.0);

        // apply to hardware
        shooterLeft.setPower(shooterPower);
        shooterRight.setPower(shooterPower);
        hoodedServo.setPosition(hoodPos);

        // ==== read RPM from encoder ====
        double velTicksPerSec = shooterLeft.getVelocity();              // ticks/sec
        double rpm = (velTicksPerSec * 60.0) / TICKS_PER_REV;          // convert to RPM

        // telemetry
        telemetry.addLine("=== Simple Shooter/Servo Tuner ===");
        telemetry.addData("Shooter Power", "%.3f", shooterPower);
        telemetry.addData("Shooter RPM", "%.1f", rpm);
        telemetry.addData("Hood Servo Pos", "%.3f", hoodPos);
        telemetry.addLine("LS X: power +/-");
        telemetry.addLine("RS X: servo +/-");
        telemetry.update();
    }

    @Override
    public void stop() {
        shooterLeft.setPower(0);
        shooterRight.setPower(0);
    }
}
