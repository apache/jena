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

package org.apache.jena.fuseki.geosparql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.geosparql.cli.ArgsConfig;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class EmptyTest {

    // A server per test, on a fresh port.
    private GeosparqlServer server;

    @BeforeEach
    public void setUp() throws DatasetException, SpatialIndexException {
        int port = WebLib.choosePort();
        String[] args = {"-u", "--port", Integer.toString(port)};

        ArgsConfig argsConfig = new ArgsConfig();
        JCommander.newBuilder()
                .addObject(argsConfig)
                .build()
                .parse(args);

        //Setup dataset
        Dataset dataset = DatasetOperations.setup(argsConfig);

        //Configure server
        server = new GeosparqlServer(argsConfig.getPort(), argsConfig.getDatsetName(), argsConfig.isLoopbackOnly(), dataset, argsConfig.isUpdateAllowed());
        server.start();

        //Add data
        String update = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                + "\n"
                + "INSERT DATA{"
                + "<http://example.org/Geometry#LineStringA> geo:hasSerialization \"LINESTRING(0 0, 10 10)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>;"
                + " a geo:Geometry ;"
                + " a geo:SpatialObject ."
                + "<http://example.org/Geometry#LineStringB> geo:hasSerialization \"LINESTRING(0 5, 10 5)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>;"
                + " a geo:Geometry ;"
                + " a geo:SpatialObject ."
                + "<http://example.org/Geometry#PointC> geo:hasSerialization \"POINT(5 5)\"^^<http://www.opengis.net/ont/geosparql#wktLiteral>;"
                + " a geo:Geometry ;"
                + " a geo:SpatialObject ."
                + "}";

        Runnable r = ()->{
            UpdateRequest updateRequest = UpdateFactory.create(update);
            UpdateExecution updateProcessor = UpdateExecution.service(server.getLocalServiceURL()).update(updateRequest).build();
            updateProcessor.execute();
        } ;
        try {
            Helper.run(r);
        } catch (QueryExceptionHTTP | HttpException ex) {
            server.shutdown();
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            server.shutdown();
        } catch (Throwable th) {}
    }

    /**
     * Test of empty dataset.
     */
    @Test
    public void testEmpty() {
        String query = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                + "\n"
                + "SELECT ?obj\n"
                + "WHERE{\n"
                + "    <http://example.org/Geometry#LineStringA> geo:sfCrosses ?obj .\n"
                + "}ORDER by ?obj";
        Runnable r = ()->{
            List<Resource> result = new ArrayList<>();
            try (QueryExecution qe = QueryExecution.service(server.getLocalServiceURL()).query(query).build()) {
                ResultSet rs = qe.execSelect();

                while (rs.hasNext()) {
                    QuerySolution qs = rs.nextSolution();
                    Resource obj = qs.getResource("obj");
                    result.add(obj);
                }

                //ResultSetFormatter.outputAsTSV(rs);
            }

            List<Resource> expResult = new ArrayList<>();
            expResult.add(ResourceFactory.createResource("http://example.org/Geometry#LineStringB"));

            assertEquals(expResult, result);
        };
        Helper.run(r);
    }
}
