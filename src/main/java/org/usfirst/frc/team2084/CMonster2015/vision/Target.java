/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2015.vision;

import static org.usfirst.frc.team2084.CMonster2015.vision.ScoreUtils.ratioToScore;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
 * An object that represents a potential target. It runs a number of tests to
 * determine if it could possibly be a goal and how well it matches.
 *
 * @author Ben Wolsieffer
 */
public class Target implements Comparable<Target> {

    private static final Scalar DRAW_COLOR = new Scalar(255, 255, 255);
    private static final int DRAW_THICKNESS = 3;

    /**
     * The number of tests that produce a score. Used for calculating the
     * average score.
     */
    private static final int NUM_SCORES = 1;

    public static final double TARGET_WIDTH = 20;
    public static final double TARGET_HEIGHT = 14;

    /**
     * The ideal aspect ratio for the static (vertical) target.
     */
    public static final double TARGET_ASPECT_RATIO = TARGET_WIDTH / TARGET_HEIGHT;

    /**
     * The minimum aspect ratio score a blob can have to be considered a target.
     */
    public static double MIN_ASPECT_RATIO_SCORE = 10;

    private final MatOfPoint contour;

    private MatOfPoint corners;

    /**
     * The score of this target.
     */
    private double score = -1;
    /**
     * Stores whether or not the target meets the minimum score requirements.
     */
    private boolean valid = true;

    private Point topLeft;
    private Point topRight;
    private Point bottomLeft;
    private Point bottomRight;

    private Point center;

    private double width;
    private double height;

    private double area;

    private double distance;

    /**
     * Creates a new possible target based on the specified blob and calculates
     * its score.
     *
     * @param p the shape of the possible target
     */
    public Target(MatOfPoint contour) {
        // Simplify contour to make the corner finding algorithm work better
        MatOfPoint2f fContour = new MatOfPoint2f();
        contour.convertTo(fContour, CvType.CV_32F);
        Imgproc.approxPolyDP(fContour, fContour, VisionParameters.getApproxPolyEpsilon(), true);
        fContour.convertTo(contour, CvType.CV_32S);

        this.contour = contour;

        // Validate target
        if (validateArea()) {

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
            corners = new MatOfPoint(rectPoints);

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

            Moments moments = Imgproc.moments(corners);
            center = new Point(moments.m10 / moments.m00, moments.m01 / moments.m00);

            width = (distance(topLeft, topRight) + distance(bottomLeft, bottomLeft)) / 2.0;
            height = (distance(topLeft, bottomLeft) + distance(topRight, bottomRight)) / 2.0;

            score = calculateScore();

            distance = (TARGET_WIDTH * HighGoalProcessor.IMAGE_SIZE.width
                    / (2 * width * Math.tan(HighGoalProcessor.FOV_ANGLE / 2))) / 12;
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

    public void draw(Mat image) {
        Imgproc.line(image, topLeft, topRight, DRAW_COLOR, DRAW_THICKNESS);
        Imgproc.line(image, topRight, bottomRight, DRAW_COLOR, DRAW_THICKNESS);
        Imgproc.line(image, bottomRight, bottomLeft, DRAW_COLOR, DRAW_THICKNESS);
        Imgproc.line(image, bottomLeft, topLeft, DRAW_COLOR, DRAW_THICKNESS);

        Imgproc.circle(image, center, 5, DRAW_COLOR);

        Imgproc.putText(image, "distance: " + distance, new Point(0, HighGoalProcessor.IMAGE_SIZE.height), Core.FONT_HERSHEY_PLAIN, 1, DRAW_COLOR);
    }

    private boolean validateArea() {
        return (area = Imgproc.contourArea(contour)) > VisionParameters.getMinBlobArea();
    }

    /**
     * Calculates this target's score. If the target is not valid, it returns 0.
     * This is called in the constructor.
     *
     * @return this target's score
     */
    private double calculateScore() {
        double lScore = (scoreAspectRatio()) / NUM_SCORES;
        return isValid() ? lScore : 0;
    }

    /**
     * Calculate the aspect ratio score for this target. This is calculated by
     * dividing the target's ratio by the target's ideal ratio. This ratio is
     * converted to a score using {@link ScoreUtils#ratioToScore(double)}.
     *
     * @return this target's rectangularity score
     */
    private double scoreAspectRatio() {
        double ratio = width / height;
        double ideal = TARGET_ASPECT_RATIO;
        double lScore = ratioToScore(ratio / ideal);
        if (lScore < MIN_ASPECT_RATIO_SCORE) {
            valid = false;
        }
        return lScore;
    }

    public MatOfPoint getContour() {
        return contour;
    }

    public boolean isValid() {
        return valid;
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

    @Override
    public int compareTo(Target o) {
        return (int) (score - o.score);
    }
}