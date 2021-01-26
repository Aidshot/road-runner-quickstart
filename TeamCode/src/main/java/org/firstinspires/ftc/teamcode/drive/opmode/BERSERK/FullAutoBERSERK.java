package org.firstinspires.ftc.teamcode.drive.opmode.BERSERK;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.AngularVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.MinVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.openftc.easyopencv.*;

import java.util.Arrays;

import static com.arcrobotics.ftclib.vision.UGContourRingPipeline.*;
import static com.arcrobotics.ftclib.vision.UGContourRingPipeline.Height.*;

@Autonomous(group = "BERSERK")
public class FullAutoBERSERK extends LinearOpMode {

    private static final int CAMERA_WIDTH = 320; // width  of wanted camera resolution
    private static final int CAMERA_HEIGHT = 240; // height of wanted camera resolution
    private static final int HORIZON = 50; // horizon value to tune
    private static final boolean DEBUG = false; // if debug is wanted, change to true
    private static final boolean USING_WEBCAM = true; // change to true if using webcam
    private static final String WEBCAM_NAME = "webcam1"; // insert webcam name from configuration if using webcam

    FtcDashboard dashboard = FtcDashboard.getInstance();
    TelemetryPacket packet = new TelemetryPacket();

    private UGContourRingPipeline pipeline;
    private OpenCvCamera camera;

    enum State {
        FOUR,
        ONE,
        ZERO
    }

    @Override
    public void runOpMode() throws InterruptedException {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        HardwareBERSERK robot    = new HardwareBERSERK();
        robot.init(hardwareMap);

        double shooter_target_velo = 1800;
        double launch_angle = 0.173;
        double kicker_out = 0.7;
        double kicker_in = 0.2;
        double wobble_close = 0.45;
        double wobble_open = 1;
        double wobble_up = 0.6;
        double wobble_down = 0.16;
        long shootWait = 350;

        double webcam_right = 0.3;

        robot.webcam_servo.setPosition(webcam_right);

        int cameraMonitorViewId = this
                .hardwareMap
                .appContext
                .getResources().getIdentifier(
                        "cameraMonitorViewId",
                        "id",
                        hardwareMap.appContext.getPackageName()
                );
        if (USING_WEBCAM) {
            camera = OpenCvCameraFactory
                    .getInstance()
                    .createWebcam(hardwareMap.get(WebcamName.class, WEBCAM_NAME), cameraMonitorViewId);
        } else {
            camera = OpenCvCameraFactory
                    .getInstance()
                    .createInternalCamera2(OpenCvInternalCamera2.CameraDirection.BACK, cameraMonitorViewId);
        }

        camera.setPipeline(pipeline = new UGContourRingPipeline(telemetry, DEBUG));
        Config.setCAMERA_WIDTH(CAMERA_WIDTH);
        Config.setHORIZON(HORIZON);
        camera.openCameraDeviceAsync(() -> camera.startStreaming(CAMERA_WIDTH, CAMERA_HEIGHT, OpenCvCameraRotation.UPRIGHT));
        FtcDashboard.getInstance().startCameraStream(camera, 0);

        State state = State.ZERO;

        while (!isStarted()) {
            switch (pipeline.getHeight()) {
                case ZERO:
                    state = State.ZERO;
                    break;
                case ONE:
                    state = State.ONE;
                    break;
                case FOUR:
                    state = State.FOUR;
                    break;
            }

        }

        waitForStart();

        robot.kicker.setPosition(kicker_out);
        Pose2d startPose = new Pose2d(-63.0,50, Math.toRadians(0.0));
        drive.setPoseEstimate(startPose);

        if (isStopRequested()) return;

        //   A AUTO TRAJECTORIES   //



        //   B AUTO TRAJECTORIES   //
        //SHOOT POSITION
        Trajectory B1 = drive.trajectoryBuilder(startPose)
                .splineTo(new Vector2d(-25.0, 55.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    ((DcMotorEx) robot.shooter1).setVelocity(shooter_target_velo);
                    // ((DcMotorEx) robot.shooter2).setVelocity(((DcMotorEx) robot.shooter1).getVelocity());
                })
                .splineTo(new Vector2d(-3.0, 38.0), Math.toRadians(0.0))
                .build();

        //WOBBLE B POSITION
        Trajectory B2 = drive.trajectoryBuilder(B1.end())
                .splineToLinearHeading(new Pose2d(15.0, 38.0, Math.toRadians(-90.0)), Math.toRadians(0.0))
                .build();

        //MOVE TOWARDS STACK
        Trajectory B3 = drive.trajectoryBuilder(B2.end(),true)
                .splineToLinearHeading(new Pose2d(-19.0, 38.0, Math.toRadians(180.0)), Math.toRadians(180.0))
                .build();

        //SHOOT POSITION
        Trajectory B4 = drive.trajectoryBuilder(B3.end(),true)
                .splineToLinearHeading( new Pose2d(-3.0, 39.0, Math.toRadians(-2.0)), Math.toRadians(180))
                .build();

        //PARK
        Trajectory B5 = drive.trajectoryBuilder(B4.end())
                .forward(9)
                .build();



        //   C AUTO TRAJECTORIES   //
        //SHOOT POSITION
        Trajectory C1 = drive.trajectoryBuilder(startPose)
                .splineTo(new Vector2d(-25.0, 55.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    ((DcMotorEx) robot.shooter1).setVelocity(shooter_target_velo);
                    //  ((DcMotorEx) robot.shooter2).setVelocity(((DcMotorEx) robot.shooter1).getVelocity());
                })
                .splineTo(new Vector2d(-3.0, 38.0), Math.toRadians(-2.0))
                .build();

        //WOBBLE C POSITION
        Trajectory C2 = drive.trajectoryBuilder(C1.end())
                .splineToConstantHeading(new Vector2d(55.0, 42.0), Math.toRadians(0.0))
                .build();

        //MOVE TOWARDS STACK
        Trajectory C3 = drive.trajectoryBuilder(C2.end(),true)
                .splineToLinearHeading(new Pose2d(0.0, 35.0, Math.toRadians(180.0)), Math.toRadians(180.0))
                .build();

        //RAM STACK (Fast Constraints)
        Trajectory C4 = drive.trajectoryBuilder(C3.end())
                .forward(12,
                        new MinVelocityConstraint(Arrays.asList(
                                new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
                                new MecanumVelocityConstraint(80, DriveConstants.TRACK_WIDTH)
                        )
                        ), new ProfileAccelerationConstraint(80))
                //.splineTo(new Vector2d (-12.0,35.0), Math.toRadians(180))
                .build();

        //INTAKE STACK (Slow Constraints)
        Trajectory C5 = drive.trajectoryBuilder(C4.end())
                .forward(8,
                        new MinVelocityConstraint(Arrays.asList(
                                new AngularVelocityConstraint(DriveConstants.MAX_ANG_VEL),
                                new MecanumVelocityConstraint(5, DriveConstants.TRACK_WIDTH)
                        )
                        ), new ProfileAccelerationConstraint(DriveConstants.MAX_ACCEL))
                //.splineTo(new Vector2d (-18,35), Math.toRadians(180.0))
                //.splineTo(new Vector2d (-16,35), Math.toRadians(180.0))
                //.splineTo(new Vector2d (-20,35), Math.toRadians(180.0))
                .build();

        //SHOOT POSITION
        Trajectory C6 = drive.trajectoryBuilder(C5.end(),true)
                .splineToLinearHeading( new Pose2d(-3.0, 38.0, Math.toRadians(-3.0)), Math.toRadians(180))
                .build();

        //PARK
        Trajectory C7 = drive.trajectoryBuilder(C6.end())
                .forward(5)
                .build();

        // AUTO CASE STATEMENT

        switch (state) {
            case ZERO:
                telemetry.addData("Stack:", "ZERO");
                telemetry.update();


                PoseStorage.currentPose = drive.getPoseEstimate();
                break;
            case ONE:


                // B AUTO //
                telemetry.addData("Stack:", "ONE");
                telemetry.update();

                //SET SERVOS
                robot.wobble_lift.setPosition(wobble_up);
                robot.wobble_claw.setPosition(wobble_close);
                robot.flap.setPosition(launch_angle);

                //SHOOT POSITION
                drive.followTrajectory(B1);

                //SHOOT x 3
                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);

                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);

                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_out);

                //TURN OFF SHOOTER
                ((DcMotorEx) robot.shooter1).setVelocity(0);
                ((DcMotorEx) robot.shooter2).setVelocity(0);

                //WOBBLE C POSITION
                drive.followTrajectory(B2);

                //DROP WOBBLE 1
                robot.wobble_lift.setPosition(wobble_down);
                sleep(700);
                robot.wobble_claw.setPosition(wobble_open);
                sleep(200);

                //MOVE TOWARDS STACK
                robot.intake.setPower(0.8);
                robot.feeder_turn.setPower(1);
                drive.followTrajectory(B3);

                robot.wobble_lift.setPosition(wobble_up);

                //SHOOT POSITION
                drive.followTrajectory(B4);

                ((DcMotorEx) robot.shooter1).setVelocity(shooter_target_velo);
                // ((DcMotorEx) robot.shooter2).setVelocity(((DcMotorEx) robot.shooter1).getVelocity());

                //SHOOT X 1
                sleep(3000);

                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_out);

                //TURN OFF INTAKE AND SHOOTER
                robot.intake.setPower(0);
                robot.feeder_turn.setPower(0);
                ((DcMotorEx) robot.shooter1).setVelocity(0);
                ((DcMotorEx) robot.shooter2).setVelocity(0);

                //PARK
                drive.followTrajectory(B5);

                PoseStorage.currentPose = drive.getPoseEstimate();
                break;


            case FOUR:

                // C AUTO //
                telemetry.addData("Stack:", "FOUR");
                telemetry.update();
                //SET SERVOS
                robot.wobble_lift.setPosition(wobble_up);
                robot.wobble_claw.setPosition(wobble_close);
                robot.flap.setPosition(launch_angle);

                //SHOOT POSITION
                drive.followTrajectory(C1);

                //SHOOT x 3
                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);

                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);

                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_out);

                //TURN OFF SHOOTER
                ((DcMotorEx) robot.shooter1).setVelocity(0);
                ((DcMotorEx) robot.shooter2).setVelocity(0);

                //WOBBLE C POSITION
                drive.followTrajectory(C2);

                //DROP WOBBLE 1
                robot.wobble_lift.setPosition(wobble_down);
                sleep(400);
                robot.wobble_claw.setPosition(wobble_open);
                sleep(200);

                //MOVE TOWARDS STACK
                drive.followTrajectory(C3);

                robot.wobble_lift.setPosition(wobble_up);

                robot.intake.setPower(0.8);
                robot.feeder_turn.setPower(1);

                //RAM STACK
                drive.followTrajectory(C4);

                sleep(500);

                //INTAKE STACK
                drive.followTrajectory(C5);

                ((DcMotorEx) robot.shooter1).setVelocity(shooter_target_velo);
                //((DcMotorEx) robot.shooter2).setVelocity(((DcMotorEx) robot.shooter1).getVelocity());

                //SHOOT POSITION
                drive.followTrajectory(C6);

                sleep(3000);

                //SHOOT X 3
                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);

                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);

                robot.kicker.setPosition(kicker_out);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_in);
                sleep(shootWait);
                robot.kicker.setPosition(kicker_out);

                //TURN OFF INTAKE AND SHOOTER
                robot.intake.setPower(0);
                robot.feeder_turn.setPower(0);
                ((DcMotorEx) robot.shooter1).setVelocity(0);
                ((DcMotorEx) robot.shooter2).setVelocity(0);

                //PARK
                drive.followTrajectory(C7);
                PoseStorage.currentPose = drive.getPoseEstimate();
                break;

        }
    }
}