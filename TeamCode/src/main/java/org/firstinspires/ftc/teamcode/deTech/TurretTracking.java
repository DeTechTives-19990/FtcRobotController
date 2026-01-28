@TeleOp(name = "TurretTracking")
public class TurretTracking extends LinearOpMode {

    private DcMotorEx turretMotor;
    private Servo hoodServo;
    private Odometry odometry;
    
    // Target position (the goal)
    private double targetX = 0;
    private double targetY = 144;
    
    // Turret encoder config
    private double TICKS_PER_REV = 537.7;  // adjust for your motor
    private double GEAR_RATIO = 1.0;       // turret gear ratio
    private double TICKS_PER_TURRET_REV = TICKS_PER_REV * GEAR_RATIO;
    
    // Hood positions (tune these!)
    private double HOOD_CLOSE = 0.3;
    private double HOOD_FAR = 0.7;
    
    // Distance thresholds (inches)
    private double CLOSE_DIST = 48;
    private double FAR_DIST = 96;

    @Override
    public void runOpMode() {
        // Turret motor setup
        turretMotor = hardwareMap.get(DcMotorEx.class, "turret");
        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);  // IMPORTANT: zero it with turret centered!
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        
        hoodServo = hardwareMap.get(Servo.class, "hood");
        
        odometry = new Odometry(hardwareMap);

        telemetry.addLine("Make sure turret is centered!");
        telemetry.addLine("Press START when ready");
        telemetry.update();
        
        waitForStart();

        while (opModeIsActive()) {
            odometry.update();
            
            double robotX = odometry.getX();
            double robotY = odometry.getY();
            double robotHeading = odometry.getHeading();
            
            // === TURRET AIM ===
            double angleToTarget = Math.atan2(targetY - robotY, targetX - robotX);
            double turretAngle = normalizeAngle(angleToTarget - robotHeading);
            
            // Maps -π to π directly to tick range (auto unwinds!)
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
            telemetry.addData("Robot Heading", "%.1f°", Math.toDegrees(robotHeading));
            telemetry.addData("Distance to Target", "%.1f in", distance);
            telemetry.addData("Turret Angle", "%.1f°", Math.toDegrees(turretAngle));
            telemetry.addData("Turret Ticks", "%d", targetTicks);
            telemetry.addData("Hood Position", "%.2f", hoodPos);
            telemetry.update();
        }
    }
    
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
}
