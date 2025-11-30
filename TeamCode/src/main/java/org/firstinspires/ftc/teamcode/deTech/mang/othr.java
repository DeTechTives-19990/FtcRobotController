package org.firstinspires.ftc.teamcode.deTech.mang;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import static org.firstinspires.ftc.teamcode.deTech.data.*;

public class othr {
    private DcMotorEx othrMotrTurt;
    private DcMotorEx othrMotrFlyi;
    private DcMotorEx othrMotrIntk;
    
    private Servo othrServKick;
    private Servo othrServSpin;
    private Servo othrServFlyi;

    private double[] othrMotrFlyiEnco = new double[] {0.0, 0.0, 0.0};
    protected LinearOpMode opmo;
    private boti botiObji;

    public void INIT() {
        motrINIT();
        servINIT();
    }

    private void motrINIT() {
        othrMotrTurt = opmo.hardwareMap.get(DcMotorEx.class, "motrTurt");
        othrMotrFlyi = opmo.hardwareMap.get(DcMotorEx.class, "motrFlyi");
        othrMotrIntk = opmo.hardwareMap.get(DcMotorEx.class, "motrIntk");

        othrMotrFlyi.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        othrMotrFlyi.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //.setDirection(dataMotrRevr);
    }

    private void servINIT() {
        othrServKick = opmo.hardwareMap.get(Servo.class, "servKck");
        othrServSpin = opmo.hardwareMap.get(Servo.class, "servSpin");
        othrServFlyi = opmo.hardwareMap.get(Servo.class, "servFlyi");
    }

    public void updt(double updtDelt) {
        othrMotrFlyiEnco[1] = othrMotrFlyiEnco[0];
        othrMotrFlyiEnco[0] = othrMotrFlyi.getCurrentPosition();
        othrMotrFlyiEnco[2] = (othrMotrFlyiEnco[0] - othrMotrFlyiEnco[1]) * updtDelt;
    }

    public void shot(boolean shotFire) {
        if (shotFire) {
            othrMotrFlyi.setVelocity((dataRoboShot - (othrMotrFlyiEnco[2] - dataRoboShot) / dataRoboShotFixi) / botiObji.getVolt());
        } else {
            othrMotrFlyi.setVelocity(0);
        }
    }
}
