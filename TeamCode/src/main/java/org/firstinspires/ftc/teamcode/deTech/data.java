package org.firstinspires.ftc.teamcode.deTech;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.LogoFacingDirection;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot.UsbFacingDirection;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class data {
    public static DcMotor.Direction dataMotrForw = DcMotor.Direction.FORWARD;
    public static DcMotor.Direction dataMotrRevr = DcMotor.Direction.REVERSE;

    public static final DcMotor.ZeroPowerBehavior dataMotrBrak = DcMotor.ZeroPowerBehavior.BRAKE;
    public static final DcMotor.ZeroPowerBehavior dataMotrFloa = DcMotor.ZeroPowerBehavior.FLOAT;

    public static LogoFacingDirection dataReviLogo = LogoFacingDirection.LEFT;
    public static UsbFacingDirection dataReviUSBi = UsbFacingDirection.UP;

    public static UnnormalizedAngleUnit dataUnivAngeUnrm = UnnormalizedAngleUnit.DEGREES;
    public static DistanceUnit dataUnivDist = DistanceUnit.INCH;
    public static AngleUnit dataUnivAnge = AngleUnit.DEGREES;

    public static final double dataWhelDiam = 3.5;
    public static final double dataWhelCirc = dataWhelDiam * Math.PI;

    public static final double dataRoboShot = 1800.0;
    public static final double dataRoboShotFixi = 7.25;

    public static final double dataUnivAcur = 0.0001;
    public static final double dataRoboOffi = 90.0;
}