/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import static org.usfirst.frc.team2084.CMonster2016.vision.HighGoalProcessor.IMAGE_SIZE;
import static org.usfirst.frc.team2084.CMonster2016.vision.Utils.ratioToScore;
import static org.usfirst.frc.team2084.CMonster2016.vision.VisionParameters.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.usfirst.frc.team2084.CMonster2016.vision.Utils.Color;

/**
 * An object that represents a potential target. It runs a number of tests to
 * determine if it could possibly be a goal and how well it matches.
 *
 * @author Ben Wolsieffer
 */
public class Target implements Comparable<Target> {

    public static final double[][] CAMERA_MATRIX =
            { { 591.6959855477264, 0.0, 319.5 }, { 0.0, 611.9796964144819, 239.5 }, { 0.0, 0.0, 1.0 } };

    public static final double[] DISTORTION_MATRIX = {
            0.08164997482366798,
            -0.5661217323881856,
            -0.010531884927443303,
            -0.004479451330784382,
            1.0088761872766687 };
    private static final Mat CAMERA_MAT = matFrom2DArray(CAMERA_MATRIX);

    private static final MatOfDouble DISTORTION_MAT = new MatOfDouble(DISTORTION_MATRIX);

    private static final MatOfDouble SIGN_NORMALIZATION_MATRIX = new MatOfDouble(1, -1, 1);

    public static final Scalar TARGET_COLOR = Color.GREEN;
    public static final Scalar VALID_TARGET_COLOR = Color.BLUE;
    public static final Scalar INVALID_TARGET_COLOR = Color.WHITE;
    public static final int DRAW_THICKNESS = 2;
    public static final NumberFormat SCORE_FORMAT = new DecimalFormat("#0.00");
    public static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#0.00");

    public static final double TARGET_WIDTH = 20.0 / 12.0;
    public static final double TARGET_HEIGHT = 12.0 / 12.0;
    public static final double ARM_LENGTH = 38.5 / 12.0;
    public static final double CAMERA_Y_OFFSET = -3.375 / 12.0;
    public static final double CAMERA_X_OFFSET = 10.25 / 12.0;
    public static final double ARM_PIVOT_Y_OFFSET = 11.0 / 12.0;
    public static final double ARM_PIVOT_Z_OFFSET = 13.5 / 12.0;

    /**
     * The ideal aspect ratio for the static (vertical) target.
     */
    public static final double TARGET_ASPECT_RATIO = TARGET_WIDTH / TARGET_HEIGHT;

    public static final MatOfPoint3f OBJECT_POINTS = new MatOfPoint3f(
            new Point3(-TARGET_WIDTH / 2, -TARGET_HEIGHT / 2, 0), new Point3(TARGET_WIDTH / 2, -TARGET_HEIGHT / 2, 0),
            new Point3(-TARGET_WIDTH / 2, TARGET_HEIGHT / 2, 0), new Point3(TARGET_WIDTH / 2, TARGET_HEIGHT / 2, 0));

    /**
     * The minimum aspect ratio score a blob can have to be considered a target.
     */
    public static final double MIN_ASPECT_RATIO_SCORE = 0;
    public static final double MIN_RECTANGULARITY_WIDTH_SCORE = 0;
    public static final double MIN_RECTANGULARITY_HEIGHT_SCORE = 0;

    public static final double MAX_Y_ANGLE = Math.toRadians(60);
    public static final double MAX_X_ANGLE = Math.toRadians(60);
    public static final double MAX_Z_ANGLE = Math.toRadians(25);

    private final MatOfPoint contour;

    private MatOfPoint2f corners;

    private static class Validator {

        public final BooleanSupplier function;
        public final String name;

        public Validator(BooleanSupplier function, String name) {
            this.function = function;
            this.name = name;
        }

    }

    private Validator[] validators = {
            new Validator(this::validateZAngle, "Z Angle"),
            new Validator(this::validateXAngle, "X Angle"),
            new Validator(this::validateDistance, "Distance"),
            new Validator(this::validateAspectRatio, "Aspect Ratio"),
            new Validator(this::validateRectangularityHeight, "Rectangularity Height"),
            new Validator(this::validateRectangularityWidth, "Rectangularity Width") };
    private DoubleSupplier[] scores = { this::scoreAngle };

    /**
     * The score of this target.
     */
    private double score = -1;

    /**
     * Stores whether or not the target meets the minimum score requirements.
     */
    private boolean valid = true;
    private boolean validArea = true;

    private Point topLeft;
    private Point topRight;
    private Point bottomLeft;
    private Point bottomRight;

    private Point center;
    private Point3 position;
    private MatOfDouble rotation = new MatOfDouble();

    private double widthTop;
    private double widthBottom;
    private double width;
    private double heightLeft;
    private double heightRight;
    private double height;

    private double area;

    private double distance;

    private double xGoalAngle;
    private double yGoalAngle;

    /**
     * Creates a new possible target based on the specified blob and calculates
     * its score.
     *
     * @param p the shape of the possible target
     */
    public Target(MatOfPoint contour, Mat grayImage) {
        // Simplify contour to make the corner finding algorithm work better
        MatOfPoint2f fContour = new MatOfPoint2f();
        contour.convertTo(fContour, CvType.CV_32F);
        Imgproc.approxPolyDP(fContour, fContour, VisionParameters.getGoalApproxPolyEpsilon(), true);
        fContour.convertTo(contour, CvType.CV_32S);

        this.contour = contour;

        // Check area, and don't do any calculations if it is not valid
        if (validArea = validateArea()) {

            // Find a bounding rectangle
            RotatedRect rect = Imgproc.minAreaRect(fContour);

            Point[] rectPoints = new Point[4];
            rect.points(rectPoints);

            for (int j = 0; j < rectPoints.length; j++) {
                Point rectPoint = rectPoints[j];

                double minDistance = Double.MAX_VALUE;
                Point point = null;

                for (int i = 0; i < contour.rows(); i++) {
                    Point contourPoint = new Point(contour.get(i, 0));
                    double dist = distance(rectPoint, contourPoint);
                    if (dist < minDistance) {
                        minDistance = dist;
                        point = contourPoint;
                    }
                }

                rectPoints[j] = point;
            }
            MatOfPoint2f rectMat = new MatOfPoint2f(rectPoints);
            Imgproc.cornerSubPix(grayImage, rectMat, new Size(4, 10), new Size(-1, -1),
                    new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, 30, 0.1));
            rectPoints = rectMat.toArray();

            SortedMap<Double, List<Point>> x = new TreeMap<>();
            Arrays.stream(rectPoints).forEach((p) -> {
                List<Point> points;
                if ((points = x.get(p.x)) == null) {
                    x.put(p.x, points = new LinkedList<>());
                }
                points.add(p);
            });

            int i = 0;
            for (Iterator<List<Point>> it = x.values().iterator(); it.hasNext();) {
                List<Point> s = it.next();

                for (Point p : s) {
                    switch (i) {
                    case 0:
                        topLeft = p;
                    break;
                    case 1:
                        bottomLeft = p;
                    break;
                    case 2:
                        topRight = p;
                    break;
                    case 3:
                        bottomRight = p;
                    }
                    i++;
                }
            }

            // Organize corners
            if (topLeft.y > bottomLeft.y) {
                Point p = bottomLeft;
                bottomLeft = topLeft;
                topLeft = p;
            }

            if (topRight.y > bottomRight.y) {
                Point p = bottomRight;
                bottomRight = topRight;
                topRight = p;
            }

            // Create corners for centroid calculation
            corners = new MatOfPoint2f(rectPoints);

            // Calculate center
            Moments moments = Imgproc.moments(corners);
            center = new Point(moments.m10 / moments.m00, moments.m01 / moments.m00);

            // Put the points in the correct order for solvePNP
            rectPoints[0] = topLeft;
            rectPoints[1] = topRight;
            rectPoints[2] = bottomLeft;
            rectPoints[3] = bottomRight;
            // Recreate corners in the new order
            corners = new MatOfPoint2f(rectPoints);

            widthTop = distance(topLeft, topRight);
            widthBottom = distance(bottomLeft, bottomRight);
            width = (widthTop + widthBottom) / 2.0;
            heightLeft = distance(topLeft, bottomLeft);
            heightRight = distance(topRight, bottomRight);
            height = (heightLeft + heightRight) / 2.0;

            Mat tvec = new Mat();

            // Calculate target's location
            Calib3d.solvePnP(OBJECT_POINTS, corners, CAMERA_MAT, DISTORTION_MAT, rotation, tvec, false, Calib3d.CV_P3P);

            // =======================================
            // Position and Orientation Transformation
            // =======================================

            double armAngle = VisionResults.getArmAngle();

            // Flip y axis to point upward
            Core.multiply(tvec, SIGN_NORMALIZATION_MATRIX, tvec);

            // Shift origin to arm pivot point, on the robot's centerline
            CoordinateMath.translate(tvec, CAMERA_X_OFFSET, CAMERA_Y_OFFSET, ARM_LENGTH);

            // Align axes with ground
            CoordinateMath.rotateX(tvec, -armAngle);
            Core.add(rotation, new MatOfDouble(armAngle, 0, 0), rotation);

            // Shift origin to robot center of rotation
            CoordinateMath.translate(tvec, 0, ARM_PIVOT_Y_OFFSET, -ARM_PIVOT_Z_OFFSET);

            double xPosFeet = tvec.get(0, 0)[0];
            double yPosFeet = tvec.get(1, 0)[0];
            double zPosFeet = tvec.get(2, 0)[0];

            // double pixelsToFeet = TARGET_WIDTH / width;

            // distance = (TARGET_WIDTH * HighGoalProcessor.IMAGE_SIZE.width
            // / (2 * width ** Math.tan(VisionParameters.getFOVAngle() / 2)));
            // double xPosFeet = (center.x - (HighGoalProcessor.IMAGE_SIZE.width
            // / 2)) * pixelsToFeet;
            // double yPosFeet = -(center.y -
            // (HighGoalProcessor.IMAGE_SIZE.height / 2)) * pixelsToFeet;

            distance = Math.sqrt(xPosFeet * xPosFeet + zPosFeet * zPosFeet);

            position = new Point3(xPosFeet, yPosFeet, zPosFeet);

            xGoalAngle = Math.atan(xPosFeet / zPosFeet);
            yGoalAngle = Math.atan(yPosFeet / zPosFeet);

            validate();
            score = calculateScore();
        } else {
            valid = false;
        }
    }

    private static double distance(Point p1, Point p2) {
        double x = p1.x - p2.x;
        double y = p1.y - p2.y;
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Gets the score of this target.
     *
     * @return the target's score
     */
    public double getScore() {
        return score;
    }

    public void draw(Mat image, double imageHeading) {
        draw(image, true, imageHeading);
    }

    public void draw(Mat image, boolean text, double imageHeading) {
        Scalar drawColor = isValid() ? VALID_TARGET_COLOR : INVALID_TARGET_COLOR;
        if (text) {
            drawColor = TARGET_COLOR;
        }

        Imgproc.line(image, topLeft, topRight, drawColor, DRAW_THICKNESS);
        Imgproc.line(image, topRight, bottomRight, drawColor, DRAW_THICKNESS);
        Imgproc.line(image, bottomRight, bottomLeft, drawColor, DRAW_THICKNESS);
        Imgproc.line(image, bottomLeft, topLeft, drawColor, DRAW_THICKNESS);

        Imgproc.circle(image, center, 5, drawColor);

        if (isValid()) {
            Utils.drawText(image, "score: " + SCORE_FORMAT.format(score), center.x - 50, center.y + 20, 1, Color.RED);
        } else {
            Utils.drawText(image, "failed: " + failedValidator, center.x - 50, center.y + 20, 1, Color.RED);
        }

        if (text) {
            Utils.drawText(image, " rotation: " + NUMBER_FORMAT.format(Math.toDegrees(xGoalAngle)) + " deg", 0,
                    IMAGE_SIZE.height - 85);
            Utils.drawText(image, "distance: " + NUMBER_FORMAT.format(distance) + " ft", 0, IMAGE_SIZE.height - 65);
            Utils.drawText(image, "        x: " + NUMBER_FORMAT.format(position.x) + " ft", 0, IMAGE_SIZE.height - 45);
            Utils.drawText(image, "        y: " + NUMBER_FORMAT.format(position.y) + " ft", 0, IMAGE_SIZE.height - 25);
            Utils.drawText(image, "        z: " + NUMBER_FORMAT.format(position.z) + " ft", 0, IMAGE_SIZE.height - 5);

            double textX = IMAGE_SIZE.width - 250;

            double angleX = Math.toDegrees(rotation.get(0, 0)[0]);
            double angleY = Math.toDegrees(rotation.get(1, 0)[0]);
            double angleZ = Math.toDegrees(rotation.get(2, 0)[0]);

            Utils.drawText(image, "heading: " + NUMBER_FORMAT.format(Math.toDegrees(imageHeading)) + " deg", textX,
                    IMAGE_SIZE.height - 65);
            Utils.drawText(image, "x angle: " + NUMBER_FORMAT.format(angleX) + " deg", textX, IMAGE_SIZE.height - 45);
            Utils.drawText(image, "y angle: " + NUMBER_FORMAT.format(angleY) + " deg", textX, IMAGE_SIZE.height - 25);
            Utils.drawText(image, "z angle: " + NUMBER_FORMAT.format(angleZ) + " deg", textX, IMAGE_SIZE.height - 5);
        }

    }

    private String failedValidator = "";

    private boolean validate() {
        for (Validator validator : validators) {
            if (!validator.function.getAsBoolean()) {
                failedValidator = validator.name;
                return valid = false;
            }
        }
        return valid = true;
    }

    public String getFailedValidator() {
        return failedValidator;
    }

    private boolean validateArea() {
        return (area = Imgproc.contourArea(contour)) > VisionParameters.getGoalMinBlobArea();
    }

    private boolean validateXAngle() {
        return Math.abs(rotation.get(0, 0)[0]) < MAX_X_ANGLE;
    }

    /**
     * This isn't reliable enough to use.
     */
    private boolean validateYAngle() {
        return Math.abs(rotation.get(1, 0)[0]) < MAX_X_ANGLE;
    }

    private boolean validateZAngle() {
        return Math.abs(rotation.get(2, 0)[0]) < MAX_Z_ANGLE;
    }

    private boolean validateDistance() {
        return distance < 30 && distance > 2;
    }

    /**
     * Calculates this target's score. If the target is not valid, it returns 0.
     * This is called in the constructor.
     *
     * @return this target's score
     */
    private double calculateScore() {
        double totalScore = 0;
        for (DoubleSupplier score : scores) {
            double scoreVal = score.getAsDouble();
            if (!Double.isFinite(scoreVal)) {
                valid = false;
                return Double.NaN;
            } else {
                totalScore += scoreVal;
            }
        }
        totalScore /= scores.length;

        return totalScore;
    }

    /**
     * Calculate the aspect ratio score for this target. This is calculated by
     * dividing the target's ratio by the target's ideal ratio. This ratio is
     * converted to a score using {@link Utils#ratioToScore(double)}.
     *
     * @return this target's rectangularity score
     */
    private double scoreAspectRatio() {
        double ratio = width / height;
        double ideal = TARGET_ASPECT_RATIO;
        double lScore = ratioToScore(ratio / ideal);
        if (lScore < getGoalMinAspectRatioScore()) {
            lScore = Double.NaN;
        }

        return lScore;
    }

    private boolean validateAspectRatio() {
        return Double.isFinite(scoreAspectRatio());
    }

    private double scoreRectangularityWidth() {
        double lScore = ratioToScore(widthTop / widthBottom);

        if (lScore < getGoalMinRectangularityWidthScore()) {
            lScore = Double.NaN;
        }
        return lScore;
    }

    private boolean validateRectangularityWidth() {
        return Double.isFinite(scoreRectangularityWidth());
    }

    private double scoreRectangularityHeight() {
        double lScore = ratioToScore(heightLeft / heightRight);

        if (lScore < getGoalMinRectangularityHeightScore()) {
            lScore = Double.NaN;
        }
        return lScore;
    }

    private boolean validateRectangularityHeight() {
        return Double.isFinite(scoreRectangularityHeight());
    }

    private double scoreSkew() {
        double horizontalWidth = topRight.x - topRight.x;
        if (horizontalWidth != 0) {
            double angle = Math.atan((topLeft.y - topRight.y) / horizontalWidth);
            return ratioToScore((angle / (Math.PI / 2)) + 1);
        } else {
            return 0;
        }
    }

    private double scoreAngle() {
        return 3 - Math.abs(xGoalAngle);
    }

    public MatOfPoint getContour() {
        return contour;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isAreaValid() {
        return validArea;
    }

    /**
     * @return the area
     */
    public double getArea() {
        return area;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @return the angle the robot needs to rotate to aim at the goal
     */
    public double getGoalXAngle() {
        return xGoalAngle;
    }

    /**
     * @return the angle the shooter needs to move to aim at the goal
     */
    public double getGoalYAngle() {
        return yGoalAngle;
    }

    @Override
    public int compareTo(Target o) {
        if (score < o.score) {
            return -1;
        } else if (score > o.score) {
            return 1;
        } else {
            return 0;
        }
    }

    private static Mat matFrom2DArray(double[][] array) {
        Mat mat = new Mat(array.length, array[0].length, CvType.CV_64F);

        for (int r = 0; r < array.length; r++) {
            mat.put(r, 0, array[r]);
        }
        return mat;
    }
}