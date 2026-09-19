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

public final class CentroidTestData {
    public static final String EMPTY_GML_POLYGON_EPSG_4979 =
            "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4979\"/>";
    public static final String GML_POINT_Z_EPSG_4979 =
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml/3.2\" srsName=\"http://www.opengis.net/def/crs/EPSG/0/4979\"><gml:pos>10 100 7</gml:pos></gml:Point>";

    private CentroidTestData() {
    }
}
