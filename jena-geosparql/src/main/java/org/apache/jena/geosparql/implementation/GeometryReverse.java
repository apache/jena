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
package org.apache.jena.geosparql.implementation;

import org.apache.jena.geosparql.implementation.registry.SRSRegistry;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;

/**
 *
 *
 */
public class GeometryReverse {

    /**
     * Checks the spatial reference system URI for y,x and reverses the supplied
     * geometry coordinates.
     *
     * @param geometry
     * @param srsURI
     * @return Geometry in x,y coordinate order.
     */
    public static final Geometry check(Geometry geometry, String srsURI) {

        Boolean isAxisXY = SRSRegistry.getAxisXY(srsURI);
        return check(geometry, isAxisXY);
    }

    /**
     * Checks the spatial reference system URI for y,x and reverses the supplied
     * geometry coordinates.
     *
     * @param geometry
     * @param srsInfo
     * @return Geometry in x,y coordinate order.
     */
    public static final Geometry check(Geometry geometry, SRSInfo srsInfo) {
        return check(geometry, srsInfo.isAxisXY());
    }

    /**
     * Checks the spatial reference system URI for y,x and reverses the supplied
     * geometry coordinates.
     *
     * @param geometry
     * @param isAxisXY
     * @return Geometry in x,y coordinate order.
     */
    public static final Geometry check(Geometry geometry, Boolean isAxisXY) {

        Geometry finalGeometry;
        if (isAxisXY) {
            finalGeometry = geometry;
        } else {
            finalGeometry = reverseGeometry(geometry);
        }
        return finalGeometry;
    }

    /**
     * Swaps X and Y in a copy of the supplied geometry, preserving Z, M,
     * coordinate layouts, and collection structure. The input is not modified.
     *
     * @param geometry
     * @return Geometry with X and Y exchanged.
     */
    public static Geometry reverseGeometry(Geometry geometry) {
        Geometry reversed = geometry.copy();
        reversed.apply(new CoordinateSequenceFilter() {
            @Override
            public void filter(CoordinateSequence sequence, int index) {
                double x = sequence.getX(index);
                sequence.setOrdinate(index, 0, sequence.getY(index));
                sequence.setOrdinate(index, 1, x);
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public boolean isGeometryChanged() {
                return true;
            }
        });
        return reversed;
    }
}
