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

package teamcode;

import org.opencv.imgproc.Imgproc;
import org.openftc.apriltag.AprilTagDetectorJNI;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;

import TrcCommonLib.trclib.TrcDbgTrace;
import TrcCommonLib.trclib.TrcHomographyMapper;
import TrcCommonLib.trclib.TrcOpenCvColorBlobPipeline;
import TrcFtcLib.ftclib.FtcEocvAprilTagPipeline;
import TrcFtcLib.ftclib.FtcEocvColorBlobPipeline;
import TrcFtcLib.ftclib.FtcEocvDetector;

/**
 * This class implements EOCV Vision that provides the capability to detect AprilTag or color blobs and return their
 * detected info.
 */
public class EocvVision extends FtcEocvDetector
{
    private static final int colorConversion = Imgproc.COLOR_RGBA2RGB;
    private static final double[] colorThresholdsRedBlob = {100.0, 255.0, 0.0, 100.0, 0.0, 60.0};
    private static final double[] colorThresholdsBlueBlob = {0.0, 60.0, 0.0, 100.0, 100, 255.0};
    private static final double[] colorThresholdsYellowBlob = {128.0, 255.0, 128.0, 255.0, 0.0, 120.0};

    public enum ObjectType
    {
        APRIL_TAG, RED_BLOB, BLUE_BLOB, YELLOW_BLOB, NONE;

        static ObjectType nextObjectType(ObjectType objType)
        {
            ObjectType nextObjType;

            switch (objType)
            {
                case APRIL_TAG:
                    nextObjType = RED_BLOB;
                    break;

                case RED_BLOB:
                    nextObjType = BLUE_BLOB;
                    break;

                case BLUE_BLOB:
                    nextObjType = YELLOW_BLOB;
                    break;

                case YELLOW_BLOB:
                    nextObjType = NONE;
                    break;

                default:
                case NONE:
                    nextObjType = APRIL_TAG;
                    break;
            }

            return nextObjType;
        }   //nextObjectType

    }   //enum ObjectType

    private final TrcDbgTrace tracer;
    private final FtcEocvAprilTagPipeline aprilTagPipeline;
    private final FtcEocvColorBlobPipeline redBlobPipeline;
    private final FtcEocvColorBlobPipeline blueBlobPipeline;
    private final FtcEocvColorBlobPipeline yellowBlobPipeline;
    private ObjectType objectType = null;

    /**
     * Constructor: Create an instance of the object.
     *
     * @param instanceName specifies the instance name.
     * @param imageWidth specifies the camera image width.
     * @param imageHeight specifies the camera image height.
     * @param cameraRect specifies the homography camera pixel rectangle, can be null if not provided.
     * @param worldRect specifies the homography world coordinate rectangle, can be null if not provided.
     * @param openCvCam specifies the OpenCV camera object.
     * @param cameraRotation specifies the camera orientation.
     * @param tracer specifies the tracer for trace info, null if none provided.
     */
    public EocvVision(
        String instanceName, int imageWidth, int imageHeight,
        TrcHomographyMapper.Rectangle cameraRect, TrcHomographyMapper.Rectangle worldRect,
        OpenCvCamera openCvCam, OpenCvCameraRotation cameraRotation, TrcDbgTrace tracer)
    {
        super(instanceName, imageWidth, imageHeight, cameraRect, worldRect, openCvCam, cameraRotation, tracer);

        this.tracer = tracer;
        TrcOpenCvColorBlobPipeline.FilterContourParams redBlobFilterContourParams =
            new TrcOpenCvColorBlobPipeline.FilterContourParams()
                .setMinArea(10000.0)
                .setMinPerimeter(200.0)
                .setWidthRange(100.0, 1000.0)
                .setHeightRange(100.0, 1000.0)
                .setSolidityRange(0.0, 100.0)
                .setVerticesRange(0.0, 1000.0)
                .setAspectRatioRange(0.0, 1000.0);
        TrcOpenCvColorBlobPipeline.FilterContourParams blueBlobFilterContourParams =
            new TrcOpenCvColorBlobPipeline.FilterContourParams()
                .setMinArea(10000.0)
                .setMinPerimeter(200.0)
                .setWidthRange(100.0, 1000.0)
                .setHeightRange(100.0, 1000.0)
                .setSolidityRange(0.0, 100.0)
                .setVerticesRange(0.0, 1000.0)
                .setAspectRatioRange(0.0, 1000.0);
        TrcOpenCvColorBlobPipeline.FilterContourParams yellowBlobFilterContourParams =
            new TrcOpenCvColorBlobPipeline.FilterContourParams()
                .setMinArea(5000.0)
                .setMinPerimeter(500.0)
                .setWidthRange(100.0, 1000.0)
                .setHeightRange(250.0, 10000.0)
                .setSolidityRange(0.0, 100.0)
                .setVerticesRange(0.0, 1000.0)
                .setAspectRatioRange(0.0, 1000.0);

        aprilTagPipeline = new FtcEocvAprilTagPipeline(
            AprilTagDetectorJNI.TagFamily.TAG_36h11, RobotParams.APRILTAG_SIZE,
            RobotParams.WEBCAM_FX, RobotParams.WEBCAM_FY, RobotParams.WEBCAM_CX, RobotParams.WEBCAM_CY, tracer);
        aprilTagPipeline.setVideoOutput(0, true);
        redBlobPipeline = new FtcEocvColorBlobPipeline(
            "redBlobPipeline", colorConversion, colorThresholdsRedBlob, redBlobFilterContourParams, tracer);
        redBlobPipeline.setVideoOutput(0, true);
        blueBlobPipeline = new FtcEocvColorBlobPipeline(
            "blueBlobPipeline", colorConversion, colorThresholdsBlueBlob, blueBlobFilterContourParams, tracer);
        blueBlobPipeline.setVideoOutput(0, true);
        yellowBlobPipeline = new FtcEocvColorBlobPipeline(
            "yellowBlobPipeliine", colorConversion, colorThresholdsYellowBlob, yellowBlobFilterContourParams, tracer);
        yellowBlobPipeline.setVideoOutput(0, true);
        // Set default pipeline and enable it.
        setDetectObjectType(ObjectType.APRIL_TAG);
    }   //EocvVision

    /**
     * This method updates the pipeline to detect the currently selected object type.
     */
    private void updatePipeline()
    {
        if (tracer != null)
        {
            tracer.traceInfo("updatePipeline", "objType=%s", objectType);
        }

        switch (objectType)
        {
            case APRIL_TAG:
                setPipeline(aprilTagPipeline);
                break;

            case RED_BLOB:
                setPipeline(redBlobPipeline);
                break;

            case BLUE_BLOB:
                setPipeline(blueBlobPipeline);
                break;

            case YELLOW_BLOB:
                setPipeline(yellowBlobPipeline);
                break;

            case NONE:
                setPipeline(null);
                break;
        }
    }   //updatePipeline

    /**
     * This method sets the object type to detect.
     *
     * @param objType specifies the object type to detect.
     */
    public void setDetectObjectType(ObjectType objType)
    {
        objectType = objType;
        updatePipeline();
    }   //setDetectObjectType

    /**
     * This method sets the detect object type to the next type.
     */
    public void setNextObjectType()
    {
        setDetectObjectType(ObjectType.nextObjectType(objectType));
    }   //setNextObjectType

    /**
     * This method returns the selected detect object type.
     *
     * @return selected detect object type.
     */
    public ObjectType getDetectObjectType()
    {
        return objectType;
    }   //getDetectObjectType

    /**
     * This method cycles to the next intermediate mat of the pipeline as the video output mat.
     */
    public void setNextVideoOutput()
    {
        getPipeline().setNextVideoOutput(true);
    }   //setNextVideoOutput

}   //class EocvVision
