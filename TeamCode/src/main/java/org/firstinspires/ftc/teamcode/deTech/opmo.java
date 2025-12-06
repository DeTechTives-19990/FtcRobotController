package org.firstinspires.ftc.teamcode.deTech;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.Servo;

import static org.firstinspires.ftc.teamcode.deTech.data.*;
import org.firstinspires.ftc.teamcode.deTech.mang.boti;
import org.firstinspires.ftc.teamcode.deTech.mang.*;

@TeleOp(name="opmo", group="Iterative OpMode")
public class opmo extends OpMode {
    private ElapsedTime time = new ElapsedTime();
    private double[] timeData = new double[] {0.0, 0.0, 0.0, 0.0};

    private boti botiObji;
    private othr othrObji;

    boolean[] bttiPast = new boolean[] {false, false};

    @Override
    public void init() {
        telemetry.addData("Stat", "INIT");
        
        botiObji = new boti(this);
        othrObji = new othr(this);

        botiObji.INIT();
        othrObji.INIT();
    }

    @Override
    public void loop() {
        timeData[1] = timeData[0];
        timeData[0] = (double) time.nanoseconds() / 1000000000;
        timeData[2] = timeData[0] - timeData[1];
        timeData[3] = 1 / timeData[2];
        
        botiObji.updt();
        othrObji.updt(timeData[3]);

        botiObji.drivXYWi(
                gamepad1.left_stick_x,
                gamepad1.left_stick_y,
                gamepad1.right_stick_x,
                1.0 - gamepad1.right_trigger);

        othrObji.intk(gamepad1.a);
        othrObji.turt(gamepad2.left_stick_x);

        if (gamepad2.dpad_up) { othrObji.hood(2);
        } else if (gamepad2.dpad_down) { othrObji.hood(1);
        } else { othrObji.hood(0); }
        
        if (gamepad2.y && bttiPast[0] == false) { othrObji.kick(); bttiPast[0] = true; }
        if (gamepad2.y == false) { bttiPast[0] = false; }
        
        if (gamepad2.b && bttiPast[1] == false) { othrObji.spin(); bttiPast[1] = true; }
        if (gamepad2.b == false) { bttiPast[1] = false; }
        
        if (gamepad2.dpad_up) { othrObji.hood(1); othrObji.fire(1);
        } else if (gamepad2.dpad_down) { othrObji.hood(0); othrObji.fire(2);
        } else { othrObji.fire(0); }
        
        telemetry.addData("Stat", "Loop");
        telemetry.addData("Data", timeData[3]);
        telemetry.update();
    }

    @Override
    public void stop() {
        telemetry.addData("Stat", "Stop");
    }
}
