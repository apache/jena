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
package org.apache.jena.geosparql.spatial;

import org.apache.jena.geosparql.spatial.index.v1.SpatialIndexAdapterV1;
import org.apache.jena.geosparql.spatial.index.v1.SpatialIndexV1;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.Ignore;

@SuppressWarnings("removal")
@Ignore /** These tests reveal issues with the first version of the spatial index which did not use per-graph indexes. */
public class TestSpatialIndexGraphLookupV1
    extends AbstractSpatialIndexGraphLookpTest
{
    @Override
    protected SpatialIndex buildSpatialIndex(DatasetGraph dsg, String srsUri) throws SpatialIndexException {
        SpatialIndexV1 v1 = SpatialIndexV1.buildSpatialIndex(DatasetFactory.wrap(dsg), srsUri);
        return new SpatialIndexAdapterV1(v1);
    }
}
