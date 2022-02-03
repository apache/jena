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

package org.apache.jena.geosparql;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.atlas.logging.LogCtlJUL;
import org.apache.jena.geosparql.assembler.GeoAssembler;
import org.apache.jena.geosparql.assembler.VocabGeoSPARQL;
import org.apache.jena.geosparql.configuration.GeoSPARQLConfig;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitGeoSPARQL implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        // SIS uses JUL for logging.
        LogCtlJUL.routeJULtoSLF4J();

        GeometryDatatype.registerDatatypes();
        // Logs "SIS_DATA is not set"
        GeoSPARQLConfig.loadFunctions();
        Assembler assembler = new GeoAssembler();
        AssemblerUtils.registerDataset(VocabGeoSPARQL.tGeoDataset,    assembler);
        AssemblerUtils.registerDataset(VocabGeoSPARQL.tGeoDatasetAlt, assembler);
    }

    @Override
    public void stop() {}

    @Override
    public int level() { return 100; }
}
