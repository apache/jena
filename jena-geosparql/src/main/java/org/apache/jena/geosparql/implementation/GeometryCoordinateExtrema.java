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

import java.util.function.DoubleBinaryOperator;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;

/**
 * Finds coordinate extrema in the input geometry's SRS axis order.
 * X and Y denote the first and second SRS dimensions, respectively.
 */
final class GeometryCoordinateExtrema {
    private GeometryCoordinateExtrema() {
    }

    static double maxX(GeometryWrapper geometry) {
        return extreme(geometry, 0, Math::max);
    }

    static double maxY(GeometryWrapper geometry) {
        return extreme(geometry, 1, Math::max);
    }

    static double maxZ(GeometryWrapper geometry) {
        return zExtreme(geometry, Math::max);
    }

    static double minX(GeometryWrapper geometry) {
        return extreme(geometry, 0, Math::min);
    }

    static double minY(GeometryWrapper geometry) {
        return extreme(geometry, 1, Math::min);
    }

    static double minZ(GeometryWrapper geometry) {
        return zExtreme(geometry, Math::min);
    }

    private static double extreme(GeometryWrapper geometry, int ordinate,
            DoubleBinaryOperator accumulator) {
        return extreme(geometry.getParsingGeometry(), ordinate, accumulator, false);
    }

    private static double zExtreme(GeometryWrapper geometry, DoubleBinaryOperator accumulator) {
        return extreme(geometry.getParsingGeometry(), 2, accumulator, true);
    }

    private static double extreme(Geometry geometry, int ordinate,
            DoubleBinaryOperator accumulator, boolean skipNaN) {
        ExtremaFilter filter = new ExtremaFilter(ordinate, accumulator, skipNaN);
        geometry.apply(filter);
        return filter.result();
    }

    private static final class ExtremaFilter implements CoordinateSequenceFilter {
        private final int ordinate;
        private final DoubleBinaryOperator accumulator;
        private final boolean skipNaN;
        private double result;
        private boolean found;

        private ExtremaFilter(int ordinate, DoubleBinaryOperator accumulator, boolean skipNaN) {
            this.ordinate = ordinate;
            this.accumulator = accumulator;
            this.skipNaN = skipNaN;
        }

        @Override
        public void filter(CoordinateSequence sequence, int index) {
            if (skipNaN && !sequence.hasZ()) {
                return;
            }
            double value = skipNaN ? sequence.getZ(index) : sequence.getOrdinate(index, ordinate);
            if (Double.isInfinite(value) || (!skipNaN && Double.isNaN(value))) {
                throw new IllegalStateException("Geometry has a non-finite ordinate at ordinate index " + ordinate);
            }
            if (skipNaN && Double.isNaN(value)) {
                return;
            }
            result = found ? accumulator.applyAsDouble(result, value) : value;
            found = true;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean isGeometryChanged() {
            return false;
        }

        private double result() {
            if (!found) {
                throw new IllegalStateException("Geometry has no eligible ordinate");
            }
            return result;
        }
    }
}
