package org.firstinspires.ftc.teamcode.deTech;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import static org.firstinspires.ftc.teamcode.deTech.data.*;
import org.firstinspires.ftc.teamcode.deTech.mang.*;

@TeleOp(name="opmo", group="Iterative OpMode")
@Disabled
public class opmo extends OpMode {
    private ElapsedTime time = new ElapsedTime();

    private boti botiObji;

    @Override
    public void init() {
        telemetry.addData("Stat", "INIT");
        botiObji.INIT();
    }

    @Override
    public void loop() {
        botiObji.drivXYWi(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, 1.0 - gamepad1.right_trigger);
        botiObji.updt();

        telemetry.addData("Stat", "Loop");
        telemetry.addData("Odoi", botiObji.getOdoiPosi().getX(dataUnivDist) + " " + botiObji.getOdoiPosi().getY(dataUnivDist) + " " + botiObji.getOdoiPosi().getHeading(dataUnivAnge));
    }

    @Override
    public void stop() {
        telemetry.addData("Stat", "Stop");
    }
}