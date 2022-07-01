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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import static org.apache.jena.geosparql.configuration.GeoSPARQLConfig.PRECISION_MODEL_SCALE_FACTOR;

/**
 *
 *
 */
public class CustomGeometryFactory implements Serializable {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(PRECISION_MODEL_SCALE_FACTOR), 0, new CustomCoordinateSequenceFactory());

    public static final GeometryFactory theInstance() {
        return GEOMETRY_FACTORY;
    }

}
