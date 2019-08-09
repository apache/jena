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
package org.apache.jena.fuseki.geosparql;

import com.beust.jcommander.JCommander;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.fuseki.geosparql.cli.ArgsConfig;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class EmptyTest {

    private static GeosparqlServer SERVER;

    public EmptyTest() {
    }

    @BeforeClass
    public static void setUpClass() throws DatasetException, SpatialIndexException {
        String[] args = {"-u"};

        ArgsConfig argsConfig = new ArgsConfig();
        JCommander.newBuilder()
                .addObject(argsConfig)
                .build()
                .parse(args);

        //Setup dataset
        Dataset dataset = DatasetOperations.setup(argsConfig);

        //Configure server
        SERVER = new GeosparqlServer(argsConfig.getPort(), argsConfig.getDatsetName(), argsConfig.isLoopbackOnly(), dataset, argsConfig.isUpdateAllowed());
        SERVER.start();

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

        UpdateRequest updateRequest = UpdateFactory.create(update);
        UpdateProcessor updateProcessor = UpdateExecutionFactory.createRemote(updateRequest, SERVER.getLocalServiceURL());
        updateProcessor.execute();

        System.out.println("Server: " + SERVER.getLocalServiceURL());
    }

    @AfterClass
    public static void tearDownClass() {
        SERVER.shutdown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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
        List<Resource> result = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.sparqlService(SERVER.getLocalServiceURL(), query)) {
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
    }

}
