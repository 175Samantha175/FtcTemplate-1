/*
 * Copyright (c) 2022 Titan Robotics Club (http://www.titanrobotics.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package multiteams;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

import TrcCommonLib.trclib.TrcDbgTrace;
import TrcCommonLib.trclib.TrcDriveBaseOdometry;
import TrcCommonLib.trclib.TrcPidController;
import TrcCommonLib.trclib.TrcPidDrive;
import TrcCommonLib.trclib.TrcPurePursuitDrive;
import TrcCommonLib.trclib.TrcSwerveDriveBase;
import TrcCommonLib.trclib.TrcSwerveModule;
import TrcFtcLib.ftclib.FtcServo;

/**
 * This class creates the RobotDrive subsystem that consists of wheel motors and related objects for driving the
 * robot.
 */
public class SwerveDrive extends RobotDrive
{
    private static final boolean logPoseEvents = false;
    private static final boolean tracePidInfo = false;

    public static final String[] servoNames = {
        RobotParams.HWNAME_LFSTEER_SERVO1, RobotParams.HWNAME_RFSTEER_SERVO1,
        RobotParams.HWNAME_LBSTEER_SERVO1, RobotParams.HWNAME_RBSTEER_SERVO1};
    public double[][] servoPositions = {
        {RobotParams.lfSteerMinus90, RobotParams.lfSteerPlus90},
        {RobotParams.rfSteerMinus90, RobotParams.rfSteerPlus90},
        {RobotParams.lbSteerMinus90, RobotParams.lbSteerPlus90},
        {RobotParams.rbSteerMinus90, RobotParams.rbSteerPlus90}
    };
    //
    // Swerve steering motors and modules.
    //
    public final FtcServo lfSteerServo1, lfSteerServo2, lbSteerServo1, lbSteerServo2;
    public final FtcServo rfSteerServo1, rfSteerServo2, rbSteerServo1, rbSteerServo2;
    public final TrcSwerveModule lfSwerveModule, lbSwerveModule, rfSwerveModule, rbSwerveModule;

    /**
     * Constructor: Create an instance of the object.
     *
     * @param robot specifies the robot object.
     */
    public SwerveDrive(Robot robot)
    {
        super();

        readSteeringCalibrationData();

        lfDriveMotor = createDriveMotor(RobotParams.HWNAME_LFDRIVE_MOTOR, RobotParams.lfDriveInverted);
        lbDriveMotor = createDriveMotor(RobotParams.HWNAME_LBDRIVE_MOTOR, RobotParams.lbDriveInverted);
        rfDriveMotor = createDriveMotor(RobotParams.HWNAME_RFDRIVE_MOTOR, RobotParams.rfDriveInverted);
        rbDriveMotor = createDriveMotor(RobotParams.HWNAME_RBDRIVE_MOTOR, RobotParams.rbDriveInverted);

        lfSteerServo1 = createSteerServo(
            RobotParams.HWNAME_LFSTEER_SERVO1, servoPositions[0][0], servoPositions[0][1],
            RobotParams.lfSteerInverted);
        lfSteerServo2 = createSteerServo(
            RobotParams.HWNAME_LFSTEER_SERVO2, servoPositions[0][0], servoPositions[0][1],
            RobotParams.lfSteerInverted);
        lfSteerServo1.addFollower(lfSteerServo2);

        lbSteerServo1 = createSteerServo(
            RobotParams.HWNAME_LBSTEER_SERVO1, servoPositions[1][0], servoPositions[1][1],
            RobotParams.lbSteerInverted);
        lbSteerServo2 = createSteerServo(
            RobotParams.HWNAME_LBSTEER_SERVO2, servoPositions[1][0], servoPositions[1][1],
            RobotParams.lbSteerInverted);
        lbSteerServo1.addFollower(lbSteerServo2);

        rfSteerServo1 = createSteerServo(
            RobotParams.HWNAME_RFSTEER_SERVO1, servoPositions[2][0], servoPositions[2][1],
            RobotParams.rfSteerInverted);
        rfSteerServo2 = createSteerServo(
            RobotParams.HWNAME_RFSTEER_SERVO2, servoPositions[2][0], servoPositions[2][1],
            RobotParams.rfSteerInverted);
        rfSteerServo1.addFollower(rfSteerServo2);

        rbSteerServo1 = createSteerServo(
            RobotParams.HWNAME_RBSTEER_SERVO1, servoPositions[3][0], servoPositions[3][1],
            RobotParams.rbSteerInverted);
        rbSteerServo2 = createSteerServo(
            RobotParams.HWNAME_RBSTEER_SERVO2, servoPositions[3][0], servoPositions[3][1],
            RobotParams.rbSteerInverted);
        rbSteerServo1.addFollower(rbSteerServo2);

        lfSwerveModule = new TrcSwerveModule("lfSwerveModule", lfDriveMotor, lfSteerServo1);
        lbSwerveModule = new TrcSwerveModule("lbSwerveModule", lbDriveMotor, lbSteerServo1);
        rfSwerveModule = new TrcSwerveModule("rfSwerveModule", rfDriveMotor, rfSteerServo1);
        rbSwerveModule = new TrcSwerveModule("rbSwerveModule", rbDriveMotor, rbSteerServo1);
        lfSwerveModule.setSteeringLimits(RobotParams.steerLowLimit, RobotParams.steerHighLimit);
        lbSwerveModule.setSteeringLimits(RobotParams.steerLowLimit, RobotParams.steerHighLimit);
        rfSwerveModule.setSteeringLimits(RobotParams.steerLowLimit, RobotParams.steerHighLimit);
        rbSwerveModule.setSteeringLimits(RobotParams.steerLowLimit, RobotParams.steerHighLimit);

        driveBase = new TrcSwerveDriveBase(
            lfSwerveModule, lbSwerveModule, rfSwerveModule, rbSwerveModule, gyro,
            RobotParams.driveBaseWidth, RobotParams.driveBaseLength);
        driveBase.setSynchronizeOdometriesEnabled(false);

         if (RobotParams.Preferences.useExternalOdometry)
         {
             //
             // Create the external odometry device that uses the right back encoder port as the X odometry and
             // the left and right front encoder ports as the Y1 and Y2 odometry. Gyro will serve as the angle
             // odometry.
             //
             TrcDriveBaseOdometry driveBaseOdometry = new TrcDriveBaseOdometry(
                 new TrcDriveBaseOdometry.AxisSensor(rbDriveMotor, RobotParams.xOdometryWheelOffset),
                 new TrcDriveBaseOdometry.AxisSensor[] {
                     new TrcDriveBaseOdometry.AxisSensor(lfDriveMotor, RobotParams.yLeftOdometryWheelOffset),
                     new TrcDriveBaseOdometry.AxisSensor(rfDriveMotor, RobotParams.yRightOdometryWheelOffset)},
                 gyro);
             //
             // Set the drive base to use the external odometry device overriding the built-in one.
             //
             driveBase.setDriveBaseOdometry(driveBaseOdometry);
             driveBase.setOdometryScales(
                 RobotParams.xOdometryWheelInchesPerCount, RobotParams.yOdometryWheelInchesPerCount);
         }
         else
         {
             driveBase.setOdometryScales(RobotParams.xPosInchesPerCount, RobotParams.yPosInchesPerCount);
         }

        //
        // Create and initialize PID controllers.
        //
        xPosPidCtrl = new TrcPidController(
            "xPosPidCtrl", RobotParams.xPosPidCoeff, RobotParams.xPosTolerance, driveBase::getXPosition);
        yPosPidCtrl = new TrcPidController(
            "yPosPidCtrl", RobotParams.yPosPidCoeff, RobotParams.yPosTolerance, driveBase::getYPosition);
        turnPidCtrl = new TrcPidController(
            "turnPidCtrl", RobotParams.turnPidCoeff, RobotParams.turnTolerance, driveBase::getHeading);
        turnPidCtrl.setAbsoluteSetPoint(true);
        // FTC robots generally have USB performance issues where the sampling rate of the gyro is not high enough.
        // If the robot turns too fast, PID will cause oscillation. By limiting turn power, the robot turns slower.
        turnPidCtrl.setOutputLimit(RobotParams.turnPowerLimit);

        pidDrive = new TrcPidDrive("pidDrive", driveBase, xPosPidCtrl, yPosPidCtrl, turnPidCtrl);
        // AbsoluteTargetMode eliminates cumulative errors on multi-segment runs because drive base is keeping track
        // of the absolute target position.
        pidDrive.setAbsoluteTargetModeEnabled(true);
        pidDrive.setMsgTracer(robot.globalTracer, logPoseEvents, tracePidInfo);

        purePursuitDrive = new TrcPurePursuitDrive(
            "purePursuitDrive", driveBase,
            RobotParams.ppdFollowingDistance, RobotParams.ppdPosTolerance, RobotParams.ppdTurnTolerance,
            RobotParams.xPosPidCoeff, RobotParams.yPosPidCoeff, RobotParams.turnPidCoeff, RobotParams.velPidCoeff);
        purePursuitDrive.setFastModeEnabled(true);
        purePursuitDrive.setMsgTracer(robot.globalTracer, logPoseEvents, tracePidInfo);
    }   //SwerveDrive

    /**
     * This method creates and configures a steering servo.
     *
     * @param servoName specifies the name of the servo.
     * @param steerMinus90 specifies the logical position of -90 degree.
     * @param steerPlus90 specifies the logical position of +90 degree.
     * @param inverted specifies true if servo direction is reversed, false otherwise.
     * @return created steering servo.
     */
    private FtcServo createSteerServo(String servoName, double steerMinus90, double steerPlus90, boolean inverted)
    {
        FtcServo servo = new FtcServo(servoName);

        servo.setInverted(inverted);
        servo.setPhysicalRange(-90.0, 90.0);
        servo.setLogicalRange(steerMinus90, steerPlus90);

        return servo;
    }   //createSteerServo

    /**
     * This method checks if anti-defense mode is enabled.
     *
     * @return true if anti-defense mode is enabled, false if disabled.
     */
    public boolean isAntiDefenseEnabled()
    {
        return ((TrcSwerveDriveBase) driveBase).isAntiDefenseEnabled();
    }   //isAntiDefenseEnabled

    /**
     * This method enables/disables the anti-defense mode where it puts all swerve wheels into an X-formation.
     * By doing so, it is very difficult for others to push us around.
     *
     * @param owner     specifies the ID string of the caller for checking ownership, can be null if caller is not
     *                  ownership aware.
     * @param enabled   specifies true to enable anti-defense mode, false to disable.
     */
    public void setAntiDefenseEnabled(String owner, boolean enabled)
    {
        if (owner == null || !enabled || driveBase.acquireExclusiveAccess(owner))
        {
            ((TrcSwerveDriveBase) driveBase).setAntiDefenseEnabled(owner, enabled);
            if (!enabled)
            {
                driveBase.releaseExclusiveAccess(owner);
            }
        }
    }   //setAntiDefenseEnabled

    /**
     * This method returns the servo position value for the given wheel index and position index.
     *
     * @param wheelIndex specifies the wheel index.
     * @param posIndex specifies -1 for zero position, 0 for minus90 and 1 for plus90.
     * @return servo position value.
     */
    public double getSteeringServoPosition(int wheelIndex, int posIndex)
    {
        return posIndex == -1?
            (servoPositions[wheelIndex][0] + servoPositions[wheelIndex][1])/2.0:
            servoPositions[wheelIndex][posIndex];
    }   //getSterringServoPosition

    /**
     * This method sets all the swerve steering servos to the selected angle.
     *
     * @param posIndex specifies -1 for zero position, 0 for minus90 and 1 for plus90.
     */
    public void setSteeringServoPosition(int posIndex)
    {
        double pos;

        pos = getSteeringServoPosition(0, posIndex);
        lfSteerServo1.setLogicalPosition(pos);
        lfSteerServo2.setLogicalPosition(pos);
        pos = getSteeringServoPosition(1, posIndex);
        rfSteerServo1.setLogicalPosition(pos);
        rfSteerServo2.setLogicalPosition(pos);
        pos = getSteeringServoPosition(2, posIndex);
        lbSteerServo1.setLogicalPosition(pos);
        lbSteerServo2.setLogicalPosition(pos);
        pos = getSteeringServoPosition(3, posIndex);
        rbSteerServo1.setLogicalPosition(pos);
        rbSteerServo2.setLogicalPosition(pos);
    }  //setSteeringServoPosition

    /**
     * This method saves the calibration data to a file on the Robot Controller.
     */
    public void saveSteeringCalibrationData()
    {
        final String funcName = "saveSteeringCalibrationData";

        try (PrintStream out = new PrintStream(new FileOutputStream(
            RobotParams.teamFolderPath + "/" + RobotParams.STEERING_CALIBRATION_DATA_FILE)))
        {
            for (int i = 0; i < servoNames.length; i++)
            {
                out.printf("%s: %f, %f\n", servoNames[i], servoPositions[i][0], servoPositions[i][1]);
            }
            out.close();
            TrcDbgTrace.getGlobalTracer().traceInfo(funcName, "Saved steering calibration data!");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }   //saveSteeringCalibrationData

    /**
     * This method reads the steering calibration data from a file on the Robot Controller.
     *
     * @throws RuntimeException if file contains invalid data.
     */
    public void readSteeringCalibrationData()
    {
        final String funcName = "readSteeringCalibrationData";
        TrcDbgTrace tracer = TrcDbgTrace.getGlobalTracer();

        try (Scanner in = new Scanner(new FileReader(
            RobotParams.teamFolderPath + "/" + RobotParams.STEERING_CALIBRATION_DATA_FILE)))
        {
            for (int i = 0; i < servoNames.length; i++)
            {
                String line = in.nextLine();
                int colonPos = line.indexOf(':');
                String name = colonPos == -1? null: line.substring(0, colonPos);

                if (name == null || !name.equals(servoNames[i]))
                {
                    throw new RuntimeException("Invalid servo name in line " + line);
                }

                String[] numbers = line.substring(colonPos + 1).split(",", 2);

                for (int j = 0; j < servoPositions[0].length; j++)
                {
                    servoPositions[i][j] = Double.parseDouble(numbers[j]);
                }

                tracer.traceInfo(
                    funcName, "SteeringCalibrationData[%s]: %s", servoNames[i], Arrays.toString(servoPositions[i]));
            }
        }
        catch (FileNotFoundException e)
        {
            tracer.traceWarn(funcName, "Steering calibration data file not found, using built-in defaults.");
        }
    }   //readSteeringCalibrationData

}   //class SwerveDrive
