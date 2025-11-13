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

package org.apache.jena.fuseki.mod.exectracker;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.IntStream;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.mod.exectracker.ExecTrackerService;
import org.apache.jena.fuseki.mod.exectracker.FMod_ExecTracker;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.tracker.TaskEventBroker;
import org.apache.jena.sparql.exec.tracker.TaskEventHistory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.G;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Test cases that interact with the spatial indexer web UI via Selenium.
 *
 * This class is currently set to "ignore" because it requires local browser.
 * Although, a headless Chrome should be started automatically,
 * this step turns out to not yet work reliable across all environments.
 */
@Disabled
public class TestFMod_ExecTracker {
    private WebDriver driver;
    private JavascriptExecutor js;

    private DatasetGraph dsg;
    private Node graphName1 = NodeFactory.createURI("http://www.example.org/graph1");

    /** Create a model with 1000 triples. */
    static Graph createTestGraph() {
        Graph graph = GraphFactory.createDefaultGraph();
        IntStream.range(0, 1000)
            .mapToObj(i -> NodeFactory.createURI("http://www.example.org/r" + i))
            .forEach(node -> graph.add(node, node, node));
        return graph;
    }

    @BeforeEach
    public void setUp() throws IOException {
        dsg = DatasetGraphFactory.create();
        IntStream.range(0, 1000)
            .mapToObj(i -> NodeFactory.createURI("http://www.example.org/x" + i))
            .map(n -> Triple.create(n, n, n))
            .forEach(dsg.getDefaultGraph()::add);

        TaskEventBroker tracker = TaskEventBroker.getOrCreate(dsg.getContext());
        TaskEventHistory history = TaskEventHistory.getOrCreate(dsg.getContext());
        history.connect(tracker);

        G.addInto(dsg.getDefaultGraph(), createTestGraph());
        // setupTestData(dsg);

        Context endpointCxt = Context.create();
        ExecTrackerService.setAllowAbort(endpointCxt, true);

        String[] argv = new String[] { "--empty" };
        FusekiServer server = FusekiMain.builder(argv)
            .add("test", dsg)
            .registerOperation(FMod_ExecTracker.getOperation(), new ExecTrackerService())
            .addEndpoint("test", "tracker", FMod_ExecTracker.getOperation(), null, endpointCxt)
            .build();

        server.start();
        int port = server.getPort();
        String serverURL = "http://localhost:" + port + "/";
        String siteUrl = serverURL + "test/update-spatial";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // use "new" headless mode (better support)
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriverManager.chromedriver().setup(); // Automatically downloads and sets path
        driver = new ChromeDriver(options);
        driver.get(siteUrl);
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        driver = null;
    }

    /**
     * Test that first clicks the "apply" button on HTML page to update the spatial index.
     * Then, the test removes a graph from the dataset and clicks the "clean" button which
     * should remove all entries from the index for which there is no corresponding graph in the dataset.
     * @throws InterruptedException
     */
    @Test
    public void testIndexAndCleanButtons() throws InterruptedException {
        if (false) {
            // Index the test data (default graph and 1 named graph).
            WebElement button = driver.findElement(By.id("apply-action"));
            button.click();
            awaitEvent();
    //        Assert.assertEquals(1, spatialIndex.query(queryEnvelope, Quad.defaultGraphIRI).size());
    //        Assert.assertEquals(2, spatialIndex.query(queryEnvelope, graphName1).size());
            clearLastEvent();

            // Remove the named graph and update the index.
            dsg.removeGraph(graphName1);
            WebElement cleanButton = driver.findElement(By.id("clean-action"));
            cleanButton.click();
            awaitEvent();

            //
            clearLastEvent();
        }

        Thread.sleep(600_000);
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
}
