package org.firstinspires.ftc.teamcode.deTech.mang;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import static org.firstinspires.ftc.teamcode.deTech.data.*;

public class boti {
    private DcMotorEx botiMotrRigtForw;
    private DcMotorEx botiMotrLeftForw;
    private DcMotorEx botiMotrRigtBack;
    private DcMotorEx botiMotrLeftBack;

    private GoBildaPinpointDriver botiOdoi;
    private VoltageSensor botiVolt;
    private IMU botiImui;

    public Pose2D botiPosiNewi;
    public Pose2D botiPosiOldi;
    public Pose2D botiVelc;

    protected LinearOpMode opmo;

    public void INIT() {
        motrINIT();
        imuiINIT();
        odoiINIT();
    }

    public void updt() {
        botiPosiOldi = botiPosiNewi;
        botiPosiNewi = getOdoi();

        fixOdoi();
    }

    private void motrINIT() {
        botiMotrRigtForw = opmo.hardwareMap.get(DcMotorEx.class, "botiMotrRigtForw");
        botiMotrLeftForw = opmo.hardwareMap.get(DcMotorEx.class, "botiMotrLeftForw");
        botiMotrRigtBack = opmo.hardwareMap.get(DcMotorEx.class, "botiMotrRigtBack");
        botiMotrLeftBack = opmo.hardwareMap.get(DcMotorEx.class, "botiMotrLeftBack");

        botiMotrRigtForw.setZeroPowerBehavior(dataMotrBrak);
        botiMotrLeftForw.setZeroPowerBehavior(dataMotrBrak);
        botiMotrRigtBack.setZeroPowerBehavior(dataMotrBrak);
        botiMotrLeftBack.setZeroPowerBehavior(dataMotrBrak);

        // Do a thingy for one of the motors if its flipped?
        // botiMotrRigtForw.setDirection(dataMotrRevr);
    }

    private void imuiINIT() {
        botiImui = opmo.hardwareMap.get(IMU.class, "botiImui");

        IMU.Parameters botImuiParm = new IMU.Parameters(new RevHubOrientationOnRobot(dataReviLogo, dataReviUSBi));

        botiImui.initialize(botImuiParm);
    }

    private void voltINIT() {
        // Channy, idk what avery was doing, but test it tho
        botiVolt = opmo.hardwareMap.voltageSensor.iterator().next();
    }

    private void odoiINIT() {
        botiOdoi = opmo.hardwareMap.get(GoBildaPinpointDriver.class, "botiOdoi");

        // Fix the offsets channy
        botiOdoi.setOffsets(8.5, 1.0, dataUnivDist);
        botiOdoi.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        botiOdoi.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD, GoBildaPinpointDriver.EncoderDirection.REVERSED);

        botiOdoi.resetPosAndIMU();
    }

    // imu

    public double getImu() {
        return botiImui.getRobotYawPitchRollAngles().getYaw(dataUnivAnge);
    }

    // voltage sensor

    public double getVolt() {
        return Math.max(1, botiVolt.getVoltage() / 12.0);
    }

    // odo

    private Pose2D getOdoi() {
        Pose2D getOdoiPose = botiOdoi.getPosition();

        double getOdoiHeadFixi = getOdoiPose.getHeading(dataUnivAnge);

        if (getOdoiHeadFixi > 180) getOdoiHeadFixi -= 360;
        if (getOdoiHeadFixi < -180) getOdoiHeadFixi += 360;

        // Channy, add some minus signs around here. And you might want to flip the += and -= above if it doesn't work
        return new Pose2D(dataUnivDist, getOdoiPose.getX(dataUnivDist), getOdoiPose.getY(dataUnivDist), dataUnivAnge, getOdoiHeadFixi);
    }

    private void fixOdoi() {
        if (Math.abs(botiPosiNewi.getX(dataUnivDist) - botiPosiOldi.getX(dataUnivDist)) < dataUnivAcur) {
            botiPosiNewi = new Pose2D(dataUnivDist, botiPosiOldi.getX(dataUnivDist), botiPosiNewi.getY(dataUnivDist), dataUnivAnge, botiPosiNewi.getHeading(dataUnivAnge));
        }
        if (Math.abs(botiPosiNewi.getY(dataUnivDist) - botiPosiOldi.getY(dataUnivDist)) < dataUnivAcur) {
            botiPosiNewi = new Pose2D(dataUnivDist, botiPosiNewi.getX(dataUnivDist), botiPosiOldi.getY(dataUnivDist), dataUnivAnge, botiPosiNewi.getHeading(dataUnivAnge));
        }
        if (Math.abs(botiPosiNewi.getHeading(dataUnivAnge) - botiPosiOldi.getHeading(dataUnivAnge)) < dataUnivAcur) {
            botiPosiNewi = new Pose2D(dataUnivDist, botiPosiNewi.getX(dataUnivDist), botiPosiNewi.getY(dataUnivDist), dataUnivAnge, botiPosiOldi.getHeading(dataUnivAnge));
        }
    }

    public Pose2D getOdoiPosi() {
        return botiPosiNewi;
    }

    public Pose2D getOdoiVelc() {
        botiVelc = new Pose2D(dataUnivDist, botiOdoi.getVelX(dataUnivDist), botiOdoi.getVelY(dataUnivDist), dataUnivAnge, Range.clip(botiOdoi.getHeadingVelocity(dataUnivAngeUnrm), -179.0, 179.0));

        return botiVelc;
    }

    // drivey time

    public void drivXYWi(double drivX, double drivY, double drivW, double drivPowr) {
        double drivDeno = Math.max(Math.abs(drivX) + Math.abs(drivY) + Math.abs(drivW), 1);

        // channy, u know what to do (fix the minus and plus signs below if the bot is not moving forward)

        double drivRigtForw = (drivX + drivY + drivW) / drivDeno / getVolt() * drivPowr;
        double drivLeftForw = (drivX - drivY - drivW) / drivDeno / getVolt() * drivPowr;
        double drivRigtBack = (drivX - drivY + drivW) / drivDeno / getVolt() * drivPowr;
        double drivLeftBack = (drivX + drivY - drivW) / drivDeno / getVolt() * drivPowr;

        botiMotrRigtForw.setPower(drivRigtForw);
        botiMotrLeftForw.setPower(drivLeftForw);
        botiMotrRigtBack.setPower(drivRigtBack);
        botiMotrLeftBack.setPower(drivLeftBack);
    }
}