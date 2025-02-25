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

import org.opencv.core.Point;

import TrcCommonLib.trclib.TrcDriveBase;
import TrcCommonLib.trclib.TrcGyro;
import TrcCommonLib.trclib.TrcPidController;
import TrcCommonLib.trclib.TrcPidDrive;
import TrcCommonLib.trclib.TrcPose2D;
import TrcCommonLib.trclib.TrcPurePursuitDrive;
import TrcFtcLib.ftclib.FtcBNO055Imu;
import TrcFtcLib.ftclib.FtcDcMotor;

/**
 * This class is intended to be extended by subclasses implementing different robot drive bases.
 */
public class RobotDrive
{
    //
    // Sensors.
    //
    public final FtcBNO055Imu imu;
    public final TrcGyro gyro;
    //
    // Subclass needs to initialize the following variables.
    //
    // Drive motors.
    public FtcDcMotor lfDriveMotor, lbDriveMotor, rfDriveMotor, rbDriveMotor;
    // Drive Base.
    public TrcDriveBase driveBase;
    // PID Coefficients and Controllers.
    public TrcPidController xPosPidCtrl, yPosPidCtrl, turnPidCtrl;
    // Drive Controllers.
    public TrcPidDrive pidDrive;
    public TrcPurePursuitDrive purePursuitDrive;

    /**
     * Constructor: Create an instance of the object.
     */
    public RobotDrive()
    {
        imu = new FtcBNO055Imu(RobotParams.HWNAME_IMU);
        gyro = imu.gyro;
    }   //RobotDrive

    /**
     * This method cancels any PIDDrive operation still in progress.
     */
    public void cancel()
    {
        if (pidDrive.isActive())
        {
            pidDrive.cancel();
        }

        if (purePursuitDrive.isActive())
        {
            purePursuitDrive.cancel();
        }

        driveBase.stop();
    }   //cancel

    /**
     * This method creates and configures a drive motor.
     *
     * @param name specifies the hardware name of the drive motor to be created.
     * @param inverted specifies true to configure the motor inverted, false otherwise.
     * @return created drive motor.
     */
    protected FtcDcMotor createDriveMotor(String name, boolean inverted)
    {
        FtcDcMotor driveMotor = new FtcDcMotor(name);

        driveMotor.motor.setMode(RobotParams.driveMotorMode);
        driveMotor.setBrakeModeEnabled(RobotParams.driveWheelBrakeModeOn);
        driveMotor.setInverted(inverted);

        if (RobotParams.Preferences.useVelocityControl)
        {
            driveMotor.enableVelocityMode(RobotParams.DRIVE_MOTOR_MAX_VELOCITY_PPS);
        }

        return driveMotor;
    }   //createDriveMotor

    /**
     * This method creates a TrcPose2D point in the target path for PurePursuitDrive.
     *
     * @param xTargetLocation specifies the target location in field reference frame.
     * @param yTargetLocation specifies the target location in field reference frame.
     * @param heading specifies the robot end heading.
     * @param tileUnit specifies true if location unit is in floor tile unit, false if in inches unit.
     * @return path point to be used in PurePursuitDrive.
     */
    public TrcPose2D pathPoint(double xTargetLocation, double yTargetLocation, double heading, boolean tileUnit)
    {
        double unitScale = tileUnit? RobotParams.FULL_TILE_INCHES: 1.0;

        return new TrcPose2D(xTargetLocation*unitScale, yTargetLocation*unitScale, heading);
    }   //pathPoint

    /**
     * This method creates a TrcPose2D point in the target path for PurePursuitDrive.
     *
     * @param xTargetLocation specifies the target location in field reference frame.
     * @param yTargetLocation specifies the target location in field reference frame.
     * @param heading specifies the robot end heading.
     * @return path point to be used in PurePursuitDrive.
     */
    public TrcPose2D pathPoint(double xTargetLocation, double yTargetLocation, double heading)
    {
        return pathPoint(xTargetLocation, yTargetLocation, heading, true);
    }   //pathPoint

    /**
     * This method creates a TrcPose2D point in the target path for PurePursuitDrive.
     *
     * @param targetLocation specifies the target location in field reference frame.
     * @param heading specifies the robot end heading.
     * @param tileUnit specifies true if location unit is in floor tile unit, false if in inches unit.
     * @return path point to be used in PurePursuitDrive.
     */
    public TrcPose2D pathPoint(Point targetLocation, double heading, boolean tileUnit)
    {
        return pathPoint(targetLocation.x, targetLocation.y, heading, tileUnit);
    }   //pathPoint

    /**
     * This method creates a TrcPose2D point in the target path for PurePursuitDrive.
     *
     * @param targetLocation specifies the target location in field reference frame.
     * @param heading specifies the robot end heading.
     * @return path point to be used in PurePursuitDrive.
     */
    public TrcPose2D pathPoint(Point targetLocation, double heading)
    {
        return pathPoint(targetLocation.x, targetLocation.y, heading, true);
    }   //pathPoint

    /**
     * This method sets the robot's autonomous starting position according to the autonomous choices.
     *
     * @param autoChoices specifies all the auto choices.
     */
    public void setAutoStartPosition(FtcAuto.AutoChoices autoChoices)
    {
        driveBase.setFieldPosition(
            autoChoices.alliance == FtcAuto.Alliance.RED_ALLIANCE ?
                (autoChoices.startPos == FtcAuto.StartPos.LEFT ?
                    RobotParams.STARTPOS_RED_LEFT : RobotParams.STARTPOS_RED_RIGHT) :
                (autoChoices.startPos == FtcAuto.StartPos.LEFT ?
                    RobotParams.STARTPOS_BLUE_LEFT : RobotParams.STARTPOS_BLUE_RIGHT));
    }   //setAutoStartPosition

    /**
     * This method adjusts the target cell according to the alliance and startPos in autoChoices.
     *
     * @param tileX specifies X tile coordinate for RED LEFT.
     * @param tileY specifies Y tile coordinate for RED LEFT.
     * @param heading specifies heading for RED LEFT.
     * @param autoChoices specifies auto choices.
     * @return adjusted target cell as TrcPose2D.
     */
    public TrcPose2D getAutoTargetCell(double tileX, double tileY, double heading, FtcAuto.AutoChoices autoChoices)
    {
        if (autoChoices.alliance == FtcAuto.Alliance.BLUE_ALLIANCE)
        {
            tileY = -tileY;
            heading = (heading + 180.0) % 360.0;
            if (autoChoices.startPos == FtcAuto.StartPos.LEFT)
            {
                tileX = -tileX;
            }
            else
            {
                heading = -heading;
            }
        }
        else if (autoChoices.startPos == FtcAuto.StartPos.RIGHT)
        {
            tileX = -tileX;
            heading = -heading;
        }

        return new TrcPose2D(tileX, tileY, heading);
    }   //getAutoTargetCell

    /**
     * This method adjusts the target cell according to the alliance and startPos in autoChoices.
     *
     * @param targetPos specifies the target position in tile units.
     * @param autoChoices specifies auto choices.
     * @return adjusted target cell as TrcPose2D.
     */
    public TrcPose2D getAutoTargetCell(TrcPose2D targetPos, FtcAuto.AutoChoices autoChoices)
    {
        return getAutoTargetCell(targetPos.x, targetPos.y, targetPos.angle, autoChoices);
    }   //getAutoTargetCell

    /**
     * This method adjusts the target heading according to the alliance and startPos in autoChoices.
     *
     * @param heading specifies heading for RED LEFT.
     * @param autoChoices specifies auto choices.
     * @return adjusted target heading.
     */
    public double getAutoTargetHeading(double heading, FtcAuto.AutoChoices autoChoices)
    {
        TrcPose2D adjPose = getAutoTargetCell(0.0, 0.0, heading, autoChoices);
        return adjPose.angle;
    }   //getAutoTargetHeading

}   //class RobotDrive
