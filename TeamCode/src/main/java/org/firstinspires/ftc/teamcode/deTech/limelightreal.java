package org.firstinspires.ftc.teamcode.deTech.mang;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import static org.firstinspires.ftc.teamcode.deTech.data.*;

public class limelightreal {

    private final Limelightlibrary limelightLib;
    private final DcMotorEx turretMotor;
    private final DcMotorEx flywheelMotor;
    private final Servo hoodServo; // Renamed to servFlyi in your original class, using hoodServo for clarity in context

    private final PidController turretPID;

    private static final double[] TURRET_PID_CONSTANTS = {0.02, 0.0001, 0.001}; 
    private static final double TX_TOLERANCE_DEG = 1.0; 
    private static final double MAX_TURN_POWER = 0.5;

    private static final double TURRET_GEAR_RATIO = 1.0; // <<< ADJUST YOUR ACTUAL GEAR RATIO
    private static final double MOTOR_TICKS_PER_REV = 28.0; // <<< ADJUST YOUR ACTUAL MOTOR TICKS
    public static final double TURRET_TICKS_PER_OUTPUT_REV = MOTOR_TICKS_PER_REV * TURRET_GEAR_RATIO; 
    public static final double TURRET_ROTATION_LIMIT = 3.0; 
    public static final int TURRET_MAX_TICK_LIMIT = (int) (TURRET_ROTATION_LIMIT * TURRET_TICKS_PER_OUTPUT_REV);
    
    private boolean isTrackingEnabled = false;
    private boolean isRollingBack = false; 
    private final OpMode opmo;


    public limelightreal(OpMode opmo, String turretName, String flywheelName, String hoodName, String limelightDeviceName) {
        this.opmo = opmo;
        
        turretMotor = opmo.hardwareMap.get(DcMotorEx.class, "motrTurt");
        flywheelMotor = opmo.hardwareMap.get(DcMotorEx.class, "motrFlyi");
        hoodServo = opmo.hardwareMap.get(Servo.class, servHood);
        limelightLib = new Limelightlibrary(opmo.hardwareMap, "limelight");

        turretMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER); 

        flywheelMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        flywheelMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        
        // 3. Initialize Controller
        turretPID = new PidController(TURRET_PID_CONSTANTS);
    }

    public void initTracking() {
        limelightLib.start();
    }
  
    public void stopTracking() {
        limelightLib.stop();
        turretMotor.setPower(0.0);
        flywheelMotor.setVelocity(0.0);
    }
    public void update() {
        if (isRollingBack) {
            runRollbackSequence(); 
        } else if (isTrackingEnabled) {
            runTurretTracking();
            autoAimServosAndVelocity();
        }
    }


    public void toggleTracking(boolean enable) {
        isTrackingEnabled = enable;
        if (!enable) {
            turretMotor.setPower(0.0);
            flywheelMotor.setVelocity(0.0);
        } else {
            turretPID.reset(); 
        }
    }
    
 
    public void manualTurretControl(double joystickInput) {
        if (isRollingBack) {
            return; 
        }
        
        if (Math.abs(joystickInput) > 0.05) {
            isTrackingEnabled = false;
            turretMotor.setPower(joystickInput * 0.4); 
        } else if (!isTrackingEnabled) {
            turretMotor.setPower(0.0);
        }
    }
    
    private void setFlywheelVelocity(int velocityIndex) {
        if (velocityIndex != -1) {
             flywheelMotor.setVelocity(dataRoboShot[velocityIndex]);
        } else {
            flywheelMotor.setVelocity(0);
        }
    }

    private void runRollbackSequence() {
        int currentPos = turretMotor.getCurrentPosition();
        toggleTracking(false); 

        if (Math.abs(currentPos) < 50) { 
            isRollingBack = false;
            turretMotor.setPower(0.0);
            return;
        }

        if (currentPos > 0) {
            turretMotor.setPower(-0.8); 
        } else {
            turretMotor.setPower(0.8); 
        }
    }


    private boolean runTurretTracking() {
        // 0. Turret Limit Check
        int currentPos = turretMotor.getCurrentPosition();
        if (Math.abs(currentPos) >= TURRET_MAX_TICK_LIMIT) {
            isRollingBack = true;
            return false;
        }
        
        // 1. Limelight Check
        if (!limelightLib.hasValidTarget()) {
            turretMotor.setPower(0.0); 
            return false;
        }

        double tx = limelightLib.getTx();
        
        if (Math.abs(tx) < TX_TOLERANCE_DEG) {
            turretMotor.setPower(0.0);
            turretPID.reset(); 
            return true;
        }

        double motorPower = turretPID.calculate(0.0, tx);
        motorPower = Range.clip(motorPower, -MAX_TURN_POWER, MAX_TURN_POWER);
        turretMotor.setPower(motorPower);
        
        return true;
    }
    
    private void autoAimServosAndVelocity() {
        if (!limelightLib.hasValidTarget()) return;
        
        double distance = limelightLib.getDistanceInches();
        
        if (distance > 100.0) { // Far Shot
            hoodServo.setPosition(dataRoboHoodPosi[0]); 
            setFlywheelVelocity(0); 
        } else if (distance > 70.0) { // Medium Shot
            hoodServo.setPosition(dataRoboHoodPosi[1]); 
            setFlywheelVelocity(1); 
        } else if (distance > 50.0) { // Short Shot
            hoodServo.setPosition(dataRoboHoodPosi[2]);
            setFlywheelVelocity(2);
        } else {
            flywheelMotor.setVelocity(0); 
        }
    }
  
    public boolean isTrackingActive() {
        return isTrackingEnabled && !isRollingBack;
    }
    
    public double getCurrentTx() {
        return limelightLib.getTx();
    }
    
    public double getCurrentDistance() {
        return limelightLib.getDistanceInches();
    }

    private class PidController {
        private final double Kp, Ki, Kd;
        private double integralSum = 0;
        private double lastError = 0;
        private final ElapsedTime timer = new ElapsedTime();
        private static final double INTEGRAL_MAX_ERROR = 5.0; 

        public PidController(double[] constants) {
            this.Kp = constants[0];
            this.Ki = constants[1];
            this.Kd = constants[2];
        }

        public double calculate(double target, double current) {
            double error = target - current;
            double deltaT = timer.seconds();
            timer.reset();
            
            double pTerm = Kp * error;

            if (Math.abs(error) < INTEGRAL_MAX_ERROR) { 
                integralSum += error * deltaT;
            } else {
                integralSum = 0;
            }
            integralSum = Range.clip(integralSum, -100.0, 100.0);
            double iTerm = Ki * integralSum;
            
            double derivative = (error - lastError) / deltaT;
            double dTerm = Kd * derivative;
            
            lastError = error;
            return pTerm + iTerm + dTerm;
        }
        
        public void reset() {
            integralSum = 0;
            lastError = 0;
            timer.reset();
        }
    }
}
