/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.implementation.great_circle;

import java.util.Objects;
import org.apache.jena.geosparql.configuration.SrsException;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;

/**
 *
 *
 */
public class CoordinatePair {

    private final Coordinate coord1;
    private final Coordinate coord2;
    private final boolean equal;

    public CoordinatePair(Coordinate coord1, Coordinate coord2) {
        this.coord1 = coord1;
        this.coord2 = coord2;
        this.equal = coord1.equals2D(coord2);
    }

    public Coordinate getCoord1() {
        return coord1;
    }

    public Coordinate getCoord2() {
        return coord2;
    }

    public boolean isEqual() {
        return equal;
    }

    @Override
    public String toString() {
        return "CoordinatePair{" + "coord1=" + coord1 + ", coord2=" + coord2 + ", equal=" + equal + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.coord1);
        hash = 97 * hash + Objects.hashCode(this.coord2);
        hash = 97 * hash + (this.equal ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CoordinatePair other = (CoordinatePair) obj;
        if (this.equal != other.equal) {
            return false;
        }
        if (!Objects.equals(this.coord1, other.coord1)) {
            return false;
        }
        return Objects.equals(this.coord2, other.coord2);
    }

    public static final CoordinatePair findNearestPair(GeometryWrapper sourceGeometry, GeometryWrapper targetGeometry) throws SrsException {

        //Both GeoemtryWrappers should be same SRS andn Geographic.
        SRSInfo sourceSRSInfo = sourceGeometry.getSrsInfo();
        SRSInfo targetSRSInfo = targetGeometry.getSrsInfo();
        if (!(sourceSRSInfo.isGeographic() && targetSRSInfo.isGeographic()) || !(sourceSRSInfo.getSrsURI().equals(targetSRSInfo.getSrsURI()))) {
            throw new SrsException("Expected same Geographic SRS for GeometryWrappers. " + sourceGeometry + " : " + targetGeometry);
        }

        //Find nearest points.
        Point point1 = null;
        Point point2 = null;

        Geometry sourceXYGeometry = sourceGeometry.getXYGeometry();
        Geometry targetXYGeometry = targetGeometry.getXYGeometry();

        //Check whether only dealing with Point geometries.
        if (sourceXYGeometry instanceof Point) {
            point1 = (Point) sourceXYGeometry;
        }
        if (targetXYGeometry instanceof Point) {
            point2 = (Point) targetXYGeometry;
        }

        //Exit if both are points.
        if (point1 != null && point2 != null) {
            return new CoordinatePair(point1.getCoordinate(), point2.getCoordinate());
        }

        //Both same SRS so same domain range.
        Envelope sourceEnvelope = sourceGeometry.getEnvelope();
        Envelope targetEnvelope = targetGeometry.getEnvelope();
        double domainRange = sourceSRSInfo.getDomainRangeX();
        double halfRange = domainRange / 2;

        double diff = targetEnvelope.getMaxX() - sourceEnvelope.getMinX();

        Geometry adjustedSource;
        Geometry adjustedTarget;
        if (diff > halfRange) {
            //Difference is greater than positive half range, then translate source by the range.
            adjustedSource = sourceGeometry.translateXYGeometry();
            adjustedTarget = targetXYGeometry;
        } else if (diff < -halfRange) {
            //Difference is less than negative half range, then translate target by the range.
            adjustedSource = sourceXYGeometry;
            adjustedTarget = targetGeometry.translateXYGeometry();
        } else {
            //Difference is between the ranges so don't translate.
            adjustedSource = sourceXYGeometry;
            adjustedTarget = targetXYGeometry;
        }

        DistanceOp distanceOp = new DistanceOp(adjustedSource, adjustedTarget);

        Coordinate[] nearest = distanceOp.nearestPoints();
        return new CoordinatePair(nearest[0], nearest[1]);
    }

}
