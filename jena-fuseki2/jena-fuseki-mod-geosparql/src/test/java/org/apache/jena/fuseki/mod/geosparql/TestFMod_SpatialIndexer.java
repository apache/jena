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

package org.apache.jena.fuseki.mod.geosparql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator.GeometryType;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexLib;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexPerGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.locationtech.jts.geom.Envelope;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Test cases that interact with the spatial indexer web UI via Selenium.
 *
 * This class is currently set to "disabled" because it requires a local browser.
 * Although, a headless Chrome should be started automatically,
 * this step turns out to not yet work reliable across all environments.
 */
@Disabled
public class TestFMod_SpatialIndexer {
    private WebDriver driver;
    private JavascriptExecutor js;

    private static Envelope queryEnvelope = new Envelope(-180, 180, -90, 90);
    private DatasetGraph dsg;
    private SpatialIndex spatialIndex;
    private Node graphName1 = NodeFactory.createURI("http://www.example.org/graph1");

    @BeforeEach
    public void setUp() throws IOException, SpatialIndexException {
        dsg = DatasetGraphFactory.create();
        setupTestData(dsg);

        spatialIndex = SpatialIndexLib.buildSpatialIndex(dsg);

        String[] argv = new String[] { "--empty" };

        FusekiServer server = FusekiMain.builder(argv)
            .add("test", dsg)
            .registerOperation(FMod_SpatialIndexer.spatialIndexerOperation, new SpatialIndexerService())
            .addEndpoint("test", "spatial-indexer", FMod_SpatialIndexer.spatialIndexerOperation)
            .build();
        server.start();
        int port = server.getPort();
        String serverURL = "http://localhost:" + port + "/";
        String siteUrl = serverURL + "test/spatial-indexer";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // use "new" headless mode (better support)
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriverManager.chromedriver().setup(); // Automatically downloads and sets path
        driver = new ChromeDriver(options);
        driver.get(siteUrl);
        js = (JavascriptExecutor) driver;
    }

    private void setupTestData(DatasetGraph dsg) {
        // Fill the graph with a few geometries; spatial index construction will derive the SRS from them.
        Envelope envelope = new Envelope(-175, 175, -85, 85);

        Map<GeometryType, Number> conf = new HashMap<>();
        conf.put(GeometryType.POINT, 1);

        // Generate geometries into the default graph and a named graph
        GeometryGenerator.generateGraph(dsg.getDefaultGraph(), envelope, conf);

        conf.put(GeometryType.POLYGON, 1);
        GeometryGenerator.generateGraph(dsg.getGraph(graphName1), envelope, conf);
    }

    /**
     * Test that first clicks the "apply" button on HTML page to update the spatial index.
     * Then, the test removes a graph from the dataset and clicks the "clean" button which
     * should remove all entries from the index for which there is no corresponding graph in the dataset.
     */
    @Test
    public void testIndexAndCleanButtons() {
        // Index the test data (default graph and 1 named graph).
        WebElement button = driver.findElement(By.id("apply-action"));
        button.click();
        awaitEvent();
        assertEquals(1, spatialIndex.query(queryEnvelope, Quad.defaultGraphIRI).size());
        assertEquals(2, spatialIndex.query(queryEnvelope, graphName1).size());
        clearLastEvent();

        // Remove the named graph and update the index.
        dsg.removeGraph(graphName1);
        WebElement cleanButton = driver.findElement(By.id("clean-action"));
        cleanButton.click();
        awaitEvent();
        SpatialIndexPerGraph newIndex = (SpatialIndexPerGraph)SpatialIndexLib.getSpatialIndex(dsg.getContext());

        // Check the number of graphs in the spatial index; the treeMap includes the default graph.
        long numGraphsInIndex = newIndex.getIndex().getTreeMap().keySet().size();
        long numGraphsInDataset;
        try (Stream<?> s = Iter.asStream(dsg.listGraphNodes())) {
            numGraphsInDataset = s.count() + 1; // Add one for the default graph.
        }
        assertEquals(numGraphsInIndex, numGraphsInDataset);
        clearLastEvent();
    }

    private void awaitEvent() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(d -> {
            Object status = js.executeScript("return window.lastEvent;");
            return status != null;
        });
    }

    private void clearLastEvent() {
        js.executeScript("window.lastEvent = null");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
