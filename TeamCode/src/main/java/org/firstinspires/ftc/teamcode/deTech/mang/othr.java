package org.firstinspires.ftc.teamcode.deTech.mang;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import static org.firstinspires.ftc.teamcode.deTech.data.*;

public class othr {
    private DcMotorEx othrMotrTurt = null;
    private DcMotorEx othrMotrFlyi = null;
    private DcMotorEx othrMotrIntk = null;
    
    private Servo othrServKick = null;
    private Servo othrServSpin = null;
    private Servo othrServHood = null;

    private double[] othrMotrFlyiEnco = new double[] {0.0, 0.0, 0.0};
    protected OpMode opmo;

    public othr(OpMode opmo) {
        this.opmo = opmo;
    }
    
    private int othrSpin = 0;
    private double othrHood = 0;
    private int othrStep = 0;

    private elap elapObji = new elap();
    
    public void INIT() {
        motrINIT();
        servINIT();
    }

    private void motrINIT() {
        othrMotrTurt = opmo.hardwareMap.get(DcMotorEx.class, "motrTurt");
        othrMotrFlyi = opmo.hardwareMap.get(DcMotorEx.class, "motrFlyi");
        othrMotrIntk = opmo.hardwareMap.get(DcMotorEx.class, "motrIntk");

        othrMotrTurt.setZeroPowerBehavior(dataMotrBrak);
        othrMotrTurt.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        othrMotrTurt.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        
        othrMotrFlyi.setZeroPowerBehavior(dataMotrBrak);
        othrMotrFlyi.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        othrMotrFlyi.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        othrMotrIntk.setDirection(dataMotrRevr);
        othrMotrFlyi.setDirection(dataMotrRevr);
    }

    private void servINIT() {
        othrServKick = opmo.hardwareMap.get(Servo.class, "servKick");
        othrServSpin = opmo.hardwareMap.get(Servo.class, "servSpin");
        othrServHood = opmo.hardwareMap.get(Servo.class, "servHood");
    }

    public void updt(double updtDelt) {
        othrMotrFlyiEnco[1] = othrMotrFlyiEnco[0];
        othrMotrFlyiEnco[0] = othrMotrFlyi.getCurrentPosition();
        othrMotrFlyiEnco[2] = (othrMotrFlyiEnco[0] - othrMotrFlyiEnco[1]) * updtDelt;
        
        if (othrSpin > 2) { othrSpin = 0; }
        othrServSpin.setPosition(dataRoboSpinPosi[othrSpin]);
        othrServHood.setPosition(othrHood);
        othrServKick.setPosition(dataRoboKickPosi[0]);
    }

    public void intk(boolean intkTake) {
        if (intkTake) {
            othrMotrIntk.setPower(1.0);
        } else {
            othrMotrIntk.setPower(0.0);
        }
    }
    
    public void spin() {
        othrSpin += 1;
    }

    public void hood(int hoodPosi) {
        if (hoodPosi == 0) {
            othrHood = dataRoboHoodPosi[0];
        } else if (hoodPosi == 1) {
            othrHood = dataRoboHoodPosi[1];
        } else {
            othrHood = dataRoboHoodPosi[2];
        }
    }
    
    public void kick() {
        elapObji.start(2.7);
        
        while (elapObji.runi()) {
            if (elapObji.geti() > 2.7) { othrServSpin.setPosition(dataRoboSpinPosi[0]);
            } else if (elapObji.geti() > 2.45) { othrServKick.setPosition(dataRoboKickPosi[0]);
            } else if (elapObji.geti() > 2.2) { othrServKick.setPosition(dataRoboKickPosi[1]);
            } else if (elapObji.geti() > 1.6) { othrServSpin.setPosition(dataRoboSpinPosi[2]);;
            } else if (elapObji.geti() > 1.35) { othrServKick.setPosition(dataRoboKickPosi[0]);
            } else if (elapObji.geti() > 1.1) { othrServKick.setPosition(dataRoboKickPosi[1]);
            } else if (elapObji.geti() > 0.5) { othrServSpin.setPosition(dataRoboSpinPosi[1]);;
            } else if (elapObji.geti() > 0.25) { othrServKick.setPosition(dataRoboKickPosi[0]);
            } else if (elapObji.geti() > 0.0) { othrServKick.setPosition(dataRoboKickPosi[1]); }
            
            elapObji.updt();
        }
    }
    
    public void turt(double turtPosi) {
        othrMotrTurt.setPower(turtPosi / 5);
    }
    
    public void fire(int fireWay) {
        if (fireWay != 0) {
            othrMotrFlyi.setVelocity(dataRoboShot[fireWay - 1] - (othrMotrFlyiEnco[2] - dataRoboShot[fireWay - 1]) / dataRoboShotFixi);
        } else {
            othrMotrFlyi.setVelocity(0);
        }
    }
}
