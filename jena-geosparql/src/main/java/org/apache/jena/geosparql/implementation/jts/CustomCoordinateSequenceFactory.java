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
package org.apache.jena.geosparql.implementation.jts;

import java.io.Serializable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

/**
 *
 *
 */
public class CustomCoordinateSequenceFactory implements CoordinateSequenceFactory, Serializable {

    @Override
    public CoordinateSequence create(Coordinate[] coordinates) {
        return new CustomCoordinateSequence(coordinates);
    }

    @Override
    public CoordinateSequence create(CoordinateSequence coordSeq) {
        CustomCoordinateSequence copyCoordSeq;

        if (coordSeq == null) {
            copyCoordSeq = new CustomCoordinateSequence();
        } else if (coordSeq instanceof CustomCoordinateSequence) {
            CustomCoordinateSequence customCoordSeq = (CustomCoordinateSequence) coordSeq;
            copyCoordSeq = customCoordSeq.copy();
        } else {
            copyCoordSeq = new CustomCoordinateSequence(coordSeq.toCoordinateArray());
        }

        return copyCoordSeq;
    }

    @Override
    public CoordinateSequence create(int size, int dimension) {
        return new CustomCoordinateSequence(size, dimension);
    }

}
