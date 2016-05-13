/* 
 * Copyright (c) 2016 RobotsByTheC. All rights reserved.
 *
 * Open Source Software - may be modified and shared by FRC teams. The code must
 * be accompanied by the BSD license file in the root directory of the project.
 */
package org.usfirst.frc.team2084.CMonster2016.vision;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Ben Wolsieffer
 */
public class HistoryBufferTest {

    private final HistoryBuffer buffer = new HistoryBuffer(5, 1);

    private static final double[][] VALUES =
            { { 1, 0 }, { 1.5, 1 }, { 2.05, 2 }, { 2.4, 3 }, { 2.999, 4 }, { 3.6, 5 }, { 4.0, 6 } };

    public HistoryBufferTest() {
        for (double[] val : VALUES) {
            buffer.newValue(val[0], val[1]);
        }
    }

    @Test
    public void testLowerOutOfBounds() {
        assertEquals(6, buffer.getValue(6), 0.001);
    }

    @Test
    public void testUpperOutOfBounds() {
        assertEquals(2, buffer.getValue(0), 0.001);
    }

    @Test
    public void testExactValue() {
        assertEquals(3, buffer.getValue(2.4), 0.001);
    }

    @Test
    public void testInterpolatedValue() {
        assertThat(buffer.getValue(3.3), both(greaterThan(4.0)).and(lessThan(5.0)));
    }

    @Test
    public void testUpperInterpolatedValue() {
        assertThat(buffer.getValue(3.9), both(greaterThan(5.0)).and(lessThan(6.0)));
    }
}
