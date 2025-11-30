package org.firstinspires.ftc.teamcode.deTech.mang;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import static org.firstinspires.ftc.teamcode.deTech.data.*;

public class othr {
    private DcMotorEx othrMotrIntk;
    private DcMotorEx othrMotrTurt;

    private DcMotorEx othrMotrFlyiRigt;
    private DcMotorEx othrMotrFlyiLeft;

    private Servo othrServSpin;
    private Servo othrServHusk;
    private Servo othrServFlip;
    private Servo othrServFlyi;

    private double[] othrMotrFlyiRigtEnco = new double[] {0.0, 0.0, 0.0};
    private double[] othrMotrFlyiLeftEnco = new double[] {0.0, 0.0, 0.0};

    private boti botiObji;

    protected LinearOpMode opmo;

    public void INIT() {
        motrINIT();
        servINIT();
    }

    private void motrINIT() {
        othrMotrIntk = opmo.hardwareMap.get(DcMotorEx.class, "othrMotrIntk");
        othrMotrTurt = opmo.hardwareMap.get(DcMotorEx.class, "othrMotrTurt");

        othrMotrFlyiRigt = opmo.hardwareMap.get(DcMotorEx.class, "othrMotrFlyiRigt");
        othrMotrFlyiLeft = opmo.hardwareMap.get(DcMotorEx.class, "othrMotrFlyiLeft");

        othrMotrFlyiRigt.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        othrMotrFlyiLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        othrMotrFlyiRigt.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        othrMotrFlyiLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //.setDirection(dataMotrRevr);
    }

    private void servINIT() {
        othrServSpin = opmo.hardwareMap.get(Servo.class, "othrServSpin");
        othrServHusk = opmo.hardwareMap.get(Servo.class, "othrServHusk");
        othrServFlip = opmo.hardwareMap.get(Servo.class, "othrServFlip");
        othrServFlyi = opmo.hardwareMap.get(Servo.class, "othrServFlyi");
    }

    public void updt(double updtDelt) {
        othrMotrFlyiRigtEnco[1] = othrMotrFlyiRigtEnco[0];
        othrMotrFlyiRigtEnco[0] = othrMotrFlyiRigt.getCurrentPosition();
        othrMotrFlyiRigtEnco[2] = (othrMotrFlyiRigtEnco[0] - othrMotrFlyiRigtEnco[1]) * updtDelt;

        othrMotrFlyiLeftEnco[1] = othrMotrFlyiLeftEnco[0];
        othrMotrFlyiLeftEnco[0] = othrMotrFlyiLeft.getCurrentPosition();
        othrMotrFlyiLeftEnco[2] = (othrMotrFlyiLeftEnco[0] - othrMotrFlyiLeftEnco[1]) * updtDelt;
    }

    public void shot(boolean shotFire) {
        if (shotFire) {
            othrMotrFlyiRigt.setVelocity((dataRoboShot - (othrMotrFlyiRigtEnco[2] - dataRoboShot) / dataRoboShotFixi) / botiObji.getVolt());
            othrMotrFlyiLeft.setVelocity((dataRoboShot - (othrMotrFlyiLeftEnco[2] - dataRoboShot) / dataRoboShotFixi) / botiObji.getVolt());
        } else {
            othrMotrFlyiRigt.setVelocity(0);
            othrMotrFlyiLeft.setVelocity(0);
        }
    }

}