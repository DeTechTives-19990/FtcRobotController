@TeleOp(name = "TurretTracking")
public class TurretTracking extends LinearOpMode {

    private GoBildaPinpointDriver odo;
    private DcMotorEx turretMotor;
    private Servo hoodServo;
    
    // Target position (the goal)
    private double targetX = 0;
    private double targetY = 144;
    
    // Turret encoder config
    private double TICKS_PER_REV = 537.7;
    private double GEAR_RATIO = 1.0;
    private double TICKS_PER_TURRET_REV = TICKS_PER_REV * GEAR_RATIO;
    
    // Hood positions
    private double HOOD_CLOSE = 0.3;
    private double HOOD_FAR = 0.7;
    
    // Distance thresholds (inches)
    private double CLOSE_DIST = 48;
    private double FAR_DIST = 96;

    @Override
    public void runOpMode() {
        // Pinpoint odometry setup
        odo = hardwareMap.get(GoBildaPinpointDriver.class, "odo");
        
        // SET THESE TO YOUR ACTUAL POD POSITIONS (mm from robot center)
        odo.setOffsets(-84.0, -168.0);  // x and y offset in mm
        
        // Set encoder directions (depends on how you mounted them)
        odo.setEncoderDirections(
            GoBildaPinpointDriver.EncoderDirection.FORWARD,
            GoBildaPinpointDriver.EncoderDirection.FORWARD
        );
        
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        odo.resetPosAndIMU();
        
        // Turret setup
        turretMotor = hardwareMap.get(DcMotorEx.class, "turret");
        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        
        hoodServo = hardwareMap.get(Servo.class, "hood");

        telemetry.addLine("Make sure turret is centered!");
        telemetry.addLine("Press START when ready");
        telemetry.update();
        
        waitForStart();

        while (opModeIsActive()) {
            odo.update();
            
            // Pinpoint gives mm, convert to inches
            double robotX = odo.getPosX() / 25.4;
            double robotY = odo.getPosY() / 25.4;
            double robotHeading = odo.getHeading();  // radians
            
            // === TURRET AIM ===
            double angleToTarget = Math.atan2(targetY - robotY, targetX - robotX);
            double turretAngle = normalizeAngle(angleToTarget - robotHeading);
            
            int targetTicks = (int) (turretAngle / (2 * Math.PI) * TICKS_PER_TURRET_REV);
            
            turretMotor.setTargetPosition(targetTicks);
            turretMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            turretMotor.setPower(0.5);
            
            // === HOOD ANGLE ===
            double distance = Math.hypot(targetX - robotX, targetY - robotY);
            double hoodPos;
            
            if (distance < CLOSE_DIST) {
                hoodPos = HOOD_CLOSE;
            } else if (distance > FAR_DIST) {
                hoodPos = HOOD_FAR;
            } else {
                double t = (distance - CLOSE_DIST) / (FAR_DIST - CLOSE_DIST);
                hoodPos = HOOD_CLOSE + t * (HOOD_FAR - HOOD_CLOSE);
            }
            
            hoodServo.setPosition(hoodPos);
            
            // === TELEMETRY ===
            telemetry.addData("Robot Pos", "(%.1f, %.1f)", robotX, robotY);
            telemetry.addData("Heading", "%.1f°", Math.toDegrees(robotHeading));
            telemetry.addData("Distance", "%.1f in", distance);
            telemetry.addData("Turret Angle", "%.1f°", Math.toDegrees(turretAngle));
            telemetry.addData("Hood", "%.2f", hoodPos);
            telemetry.update();
        }
    }
    
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}
