package frc.robot;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.apriltag.AprilTagDetector;
import edu.wpi.first.cameraserver.CameraServer;

import java.util.HashSet;

public class AprilTagsPipeline {
    
    static void aprilTagVisionProc() {
        var camera = CameraServer.startAutomaticCapture();
        var outputWidth = 640;
        var outputHeight = 480;
    
        camera.setResolution(outputWidth, outputHeight);
        var cvSink = CameraServer.getVideo();
        var outputStream = CameraServer.putVideo("Camera Stream", outputWidth, outputHeight);
    
        var mat = new Mat();
        var grayScaleMat = new Mat();
    
        var point0 = new Point();
        var point1 = new Point();
        var point2 = new Point();
        var point3 = new Point();
        var frameCenter = new Point();
        var blue = new Scalar(0, 0, 255);
        var red = new Scalar(255, 0, 0);
    
        var aprilTagDetector = new AprilTagDetector();
    
        // https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/apriltag/AprilTagDetector.Config.html
        var config = aprilTagDetector.getConfig();
        config.quadSigma = 0.8f; // Gaussian Blur
        aprilTagDetector.setConfig(config);
    
        // https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/apriltag/AprilTagDetector.QuadThresholdParameters.html
        var quadThresholdParameters = aprilTagDetector.getQuadThresholdParameters();
        quadThresholdParameters.minClusterPixels = 250;
        quadThresholdParameters.criticalAngle *= 5;
        quadThresholdParameters.maxLineFitMSE *= 1.5;
        aprilTagDetector.setQuadThresholdParameters(quadThresholdParameters);
      
        aprilTagDetector.addFamily("tag16h5");
    
        while (!Thread.interrupted()) {
          if (cvSink.grabFrame(mat) == 0) {
            outputStream.notifyError(cvSink.getError());
            continue;
          }
          
          Imgproc.cvtColor(mat, grayScaleMat, Imgproc.COLOR_RGB2GRAY);
          var results = aprilTagDetector.detect(grayScaleMat);
          var resultIds = new HashSet<>();
    
          for (var result: results) {
            point0.x = result.getCornerX(0);
            point1.x = result.getCornerX(1);
            point2.x = result.getCornerX(2);
            point3.x = result.getCornerX(3);
    
            point0.y = result.getCornerY(0);
            point1.y = result.getCornerY(1);
            point2.y = result.getCornerY(2);
            point3.y = result.getCornerY(3);
    
            frameCenter.x = result.getCenterX();
            frameCenter.y = result.getCenterY();
    
            resultIds.add(result.getId());
    
            Imgproc.line(mat, point0, point1, blue, 3);
            Imgproc.line(mat, point1, point2, blue, 3);
            Imgproc.line(mat, point2, point3, blue, 3);
            Imgproc.line(mat, point3, point0, blue, 3);
            Imgproc.putText(mat, String.valueOf(result.getId()), frameCenter, Imgproc.FONT_HERSHEY_COMPLEX, 5, red, 10);
          }
          outputStream.putFrame(mat);
        }
        aprilTagDetector.close();
      }

}
