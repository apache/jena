/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.geosparql.implementation.jts;

import org.junit.Test;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequences;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;

import static org.junit.Assert.*;

public class CustomCoordinateSequenceFactoryTest {
    @Test
    public void allocationPreservesMeasuresAndMatchesJtsLayoutLimits() {
        for (int size : new int[] { 0, 2 }) {
            for (int[] layout : new int[][] { { 2, 0 }, { 3, 0 }, { 3, 1 }, { 4, 1 },
                    { 1, 0 }, { 4, 0 }, { 5, 2 } }) {
                CoordinateSequence expected = CoordinateArraySequenceFactory.instance().create(size, layout[0], layout[1]);
                CoordinateSequence actual = new CustomCoordinateSequenceFactory().create(size, layout[0], layout[1]);
                assertLayout(expected, actual);
                if (size > 0 && actual.hasM()) {
                    actual.setOrdinate(0, actual.getDimension() - actual.getMeasures(), 8);
                    assertEquals(8, actual.getM(0), 0);
                    assertTrue(Double.isNaN(actual.getZ(0)));
                }
            }
        }
    }

    @Test
    public void conversionPreservesLayoutsValuesAndIndependence() {
        for (int[] layout : new int[][] { { 2, 0 }, { 3, 0 }, { 3, 1 }, { 4, 1 } }) {
            CoordinateSequence source = new CoordinateArraySequence(2, layout[0], layout[1]);
            for (int i = 0; i < source.size(); i++) {
                source.setOrdinate(i, 0, 100 + i);
                source.setOrdinate(i, 1, 10 + i);
                if (source.hasZ())
                    source.setOrdinate(i, 2, i == 0 ? Double.NaN : 9);
                if (source.hasM())
                    source.setOrdinate(i, source.getDimension() - 1, i == 0 ? Double.NaN : 8);
            }
            CoordinateSequence converted = new CustomCoordinateSequenceFactory().create(source);
            assertLayout(source, converted);
            for (int i = 0; i < source.size(); i++) {
                assertEquals(source.getX(i), converted.getX(i), 0);
                assertEquals(source.getY(i), converted.getY(i), 0);
                assertEquals(source.getZ(i), converted.getZ(i), 0);
                assertEquals(source.getM(i), converted.getM(i), 0);
            }
            converted.setOrdinate(1, CoordinateSequence.X, -1);
            converted.setOrdinate(1, CoordinateSequence.Y, -1);
            assertEquals(101, source.getX(1), 0);
            assertEquals(11, source.getY(1), 0);
            if (source.hasZ()) {
                converted.setOrdinate(1, CoordinateSequence.Z, -1);
                assertEquals(9, source.getZ(1), 0);
            }
            if (source.hasM()) {
                converted.setOrdinate(1, converted.getDimension() - converted.getMeasures(), -1);
                assertEquals(-1, converted.getM(1), 0);
                assertEquals(8, source.getM(1), 0);
            }
        }
    }

    @Test
    public void convertedSequencesSupportGenericOrdinateCopying() {
        for (int[] layout : new int[][] { { 2, 0 }, { 3, 0 }, { 3, 1 }, { 4, 1 } }) {
            CoordinateSequence source = new CoordinateArraySequence(2, layout[0], layout[1]);
            for (int i = 0; i < source.size(); i++) {
                for (int ordinate = 0; ordinate < source.getDimension(); ordinate++) {
                    source.setOrdinate(i, ordinate, i == 0 && ordinate > 1 ? Double.NaN : 10 * i + ordinate);
                }
            }
            CoordinateSequence converted = new CustomCoordinateSequenceFactory().create(source);
            CoordinateSequence standardCopy = new CoordinateArraySequence(2, layout[0], layout[1]);
            CoordinateSequences.copy(converted, 0, standardCopy, 0, source.size());
            CoordinateSequence customCopy = new CustomCoordinateSequenceFactory().create(
                    new CoordinateArraySequence(2, layout[0], layout[1]));
            CoordinateSequences.copy(source, 0, customCopy, 0, source.size());
            for (CoordinateSequence result : new CoordinateSequence[] { converted, standardCopy, customCopy }) {
                assertLayout(source, result);
                for (int i = 0; i < source.size(); i++) {
                    for (int ordinate = 0; ordinate < source.getDimension(); ordinate++) {
                        assertEquals(source.getOrdinate(i, ordinate), result.getOrdinate(i, ordinate), 0);
                    }
                    assertEquals(source.getZ(i), result.getZ(i), 0);
                    assertEquals(source.getM(i), result.getM(i), 0);
                }
            }
        }
    }

    @Test
    public void conversionPreservesEmptyLayouts() {
        for (int[] layout : new int[][] { { 2, 0 }, { 3, 0 }, { 3, 1 }, { 4, 1 } }) {
            CoordinateSequence source = new CoordinateArraySequence(0, layout[0], layout[1]);
            assertLayout(source, new CustomCoordinateSequenceFactory().create(source));
        }
    }

    private static void assertLayout(CoordinateSequence source, CoordinateSequence converted) {
        assertNotSame(source, converted);
        assertEquals(source.size(), converted.size());
        assertEquals(source.getDimension(), converted.getDimension());
        assertEquals(source.getMeasures(), converted.getMeasures());
        assertEquals(source.hasZ(), converted.hasZ());
        assertEquals(source.hasM(), converted.hasM());
    }
}
