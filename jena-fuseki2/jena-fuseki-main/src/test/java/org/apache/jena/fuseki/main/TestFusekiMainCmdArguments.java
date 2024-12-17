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
package org.apache.jena.fuseki.main;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.cmd.CmdException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.cmds.ServerArgs;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.riot.SysRIOT;

/**
 * NOTE: we will randomise the port (--port=0) on all happy paths in order to avoid conflict with existing runs.
 */
public class TestFusekiMainCmdArguments {

    private static String level = null;
    private static File jettyConfigFile;
    private static String jettyConfigFilename;

    @BeforeAll public static void beforeClass() throws IOException {
        // This is not reset by each running server.
        FusekiLogging.setLogging();
        level = LogCtl.getLevel(Fuseki.serverLog);
        LogCtl.setLevel(Fuseki.serverLog, "WARN");

        // Create a fake Jetty config file just to avoid "File Not Found" error
        jettyConfigFile = Files.createTempFile("jetty-config",".xml").toFile();
        jettyConfigFilename = jettyConfigFile.getAbsolutePath();
    }

    @AfterAll public static void afterClass() {
        if ( level != null )
            LogCtl.setLevel(Fuseki.serverLog, level);
        jettyConfigFile.delete();
    }

    private FusekiServer server = null;
    @AfterEach public void after() {
        if ( server != null )
            server.stop();
    }

    // Test the initial settings in ServerArgs for customisation
    // control features that are not argument values.
    @Test
    public void argDefaults() {
        ServerArgs serverArgs = new ServerArgs();
        assertFalse(serverArgs.allowEmpty, "Wrong default setting: allowEmpty");
        assertFalse(serverArgs.bypassStdArgs, "Wrong default setting: bypassStdArgs");
    }

    @Test
    public void test_empty() {
        // given
        List<String> arguments = List.of("--port=0", "--empty");
        // when
        buildServer(buildCmdLineArguments(arguments));
        // then
        assertNotNull(server);
    }

    @Test
    public void test_localhost() {
        // given
        List<String> arguments = List.of("--port=0", "--localhost", "--mem", "/dataset");
        // when
        buildServer(buildCmdLineArguments(arguments));
        // then
        assertNotNull(server);
    }

    @Test
    public void test_contextpath_1() {
        int port = WebLib.choosePort();
        // given
        List<String> arguments = List.of("--port="+port, "--mem", "--contextpath=/ABC", "/path");
        // when
        buildServer(buildCmdLineArguments(arguments));
        // then
        assertNotNull(server);
        assertEquals(port, server.getHttpPort());
    }

    @Test
    public void test_contextpath_2() {
        // given
        List<String> arguments = List.of("--port=0", "--mem", "--contextpath=ABC", "/path");
        // when
        buildServer(buildCmdLineArguments(arguments));
        // then
        assertNotNull(server);
    }

    @Test
    public void test_contextpath_3() {
        // given
        List<String> arguments = List.of("--port=0", "--mem", "--contextpath=/", "/path");
        // when
        buildServer(buildCmdLineArguments(arguments));
        // then
        assertNotNull(server);
    }

    @Test
    public void test_empty_but_named() {
        // given
        List<String> arguments = List.of("--port=0", "--empty", "/dataset");
        String expectedMessage = "Dataset name provided but 'no dataset' flag given";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_contextpath() {
        // given
        List<String> arguments = List.of("--mem", "--contextpath=ABC/", "/path");
        String expectedMessage = "Path base must not end with \"/\": 'ABC/'";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_noDataSetProvided() {
        // given
        List<String> arguments = List.of("/dataset");
        String expectedMessage = "No dataset or configuration specified on the command line";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_noArguments_emptyString() {
        // given
        String emptyString = "";
        String expectedMessage = "No dataset or configuration specified on the command line";
        // when
        CmdException actual = assertThrows(CmdException.class, ()-> buildServer(emptyString));

        // then
        assertEquals(expectedMessage, actual.getMessage(), "Expecting correct message");
    }

    @Test
    public void test_error_noArguments_null() {
        // given
        String nullString = null;
        String expectedMessage = "No dataset or configuration specified on the command line";
        // when
        CmdException actual = assertThrows(CmdException.class, ()-> buildServer(nullString));
        // then
        assertEquals(expectedMessage, actual.getMessage(), "Expecting correct message");
    }

    @Test
    public void test_error_noArguments_emptyList() {
        // given
        List<String> arguments = emptyList();
        String expectedMessage = "No dataset or configuration specified on the command line";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_tooManyDataSetsProvided() {
        // given
        List<String> arguments =
                List.of("--mem", "--file=file", "--dataset=dataset", "--tdb=file", "--memtdb=file", "--config=file");
        String expectedMessage = "Multiple ways providing a dataset. Only one of --mem, --file, --loc or --conf";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_allowEmpty_withAdditionalDataset() {
        // given
        List<String> arguments = List.of("--mem", "--empty");
        String expectedMessage = "Dataset provided but 'no dataset' flag given";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_configFileWithDataset() {
        // given
        List<String> arguments = List.of("--config=file", "/dataset");
        String expectedMessage = "Can't have both a configuration file and a service name";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_configFileWithRDFS() {
        // given
        List<String> arguments = List.of("--config=file", "--rdfs=file");
        String expectedMessage = "Need to define RDFS setup in the configuration file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_missingServiceName() {
        // given
        List<String> arguments = List.of("--mem");
        String expectedMessage = "Missing service name";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_multipleServiceNames() {
        // given
        List<String> arguments = List.of("--mem", "/path1", "/path2");
        String expectedMessage = "Multiple dataset path names given";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_configFileAndUpdate() {
        // given
        List<String> arguments = List.of("--config=file", "--update");
        String expectedMessage =
                "--update and a configuration file does not make sense (control using the configuration file only)";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_configFileMissingFile() {
        // given
        List<String> arguments = List.of("--config=file");
        String expectedMessage = "File not found: file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_configFile_directory() {
        // given
        List<String> arguments = List.of("--config=testing");
        String expectedMessage = "Is a directory: testing";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_invalidPort() {
        // given
        List<String> arguments = List.of("--mem", "--port=ERROR", "/path");
        String expectedMessage = "port : bad port number: 'ERROR'";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_jettyConfigFileAndPort() {
        // given
        List<String> arguments = List.of("--mem", "--jetty=" + jettyConfigFilename, "--port=99", "/path");
        String expectedMessage = "Cannot specify the port and also provide a Jetty configuration file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_jettyConfigFileAndLocalHost() {
        // given
        List<String> arguments = List.of("--mem", "--jetty=" + jettyConfigFilename, "--localhost", "/path");
        String expectedMessage = "Cannot specify 'localhost' and also provide a Jetty configuration file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_argConfigFile_MissingFile() {
        // given
        List<String> arguments = List.of("--file=file", "/dataset");
        String expectedMessage = "File not found: file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_argConfigFile_UnrecognisedFileType() {
        // given
        List<String> arguments = List.of("--file=testing/Config/cors.properties", "/dataset");
        String expectedMessage = "Cannot guess language for file: testing/Config/cors.properties";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_argConfigFile_wrongFormat() {
        // given
        List<String> arguments = List.of("--file=testing/Config/invalid.ttl", "/dataset");
        String expectedMessage = "Failed to load file: testing/Config/invalid.ttl";
        // when, then
        LogCtl.withLevel(SysRIOT.getLogger(), "fatal",
                         ()-> testForCmdException(arguments, expectedMessage)
                         );
    }

    @Test
    public void test_error_missingRDFSFile() {
        // given
        List<String> arguments = List.of("--mem", "--rdfs=missing_file", "/dataset");
        String expectedMessage = "No such file for RDFS: missing_file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_missingSparqlerFile() {
        // given
        List<String> arguments = List.of("--sparqler=missing_file");
        String expectedMessage = "File area not found: missing_file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_incorrectContextPath() {
        // given
        List<String> arguments = List.of("--mem", "--contextPath=wrongPath/", "/dataset");
        String expectedMessage = "Path base must not end with \"/\": 'wrongPath/'";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_argBase_missingFile() {
        // given
        List<String> arguments = List.of("--mem", "--base=missing_file", "/dataset");
        String expectedMessage = "File area not found: missing_file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_passwdFile_withJetty() {
        // given
        List<String> arguments = List.of("--mem", "--passwd=missing_file", "--jetty=" + jettyConfigFilename, "/dataset");
        String expectedMessage = "Can't specify a password file and also provide a Jetty configuration file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_httpsConfig() {
        // given
        List<String> arguments = List.of("--mem", "--httpsPort=12345", "/dataset");
        String expectedMessage = "https port given but not certificate details via --https";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_httpsConfig_withJetty() {
        // given
        List<String> arguments = List.of("--mem", "--httpsPort=12345", "--https=something", "--jetty=" + jettyConfigFilename, "/dataset");
        String expectedMessage = "Can't specify \"https\" and also provide a Jetty configuration file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_auth_withJetty() {
        // given
        List<String> arguments = List.of("--mem", "--auth=12345", "--jetty=" + jettyConfigFilename, "/dataset");
        String expectedMessage = "Can't specify authentication and also provide a Jetty configuration file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_jetty_missingFile() {
        // given
        List<String> arguments = List.of("--mem", "--jetty=missing_file", "/dataset");
        String expectedMessage = "Jetty config file not found: missing_file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_error_corsConfig_missingFile() {
        // given
        List<String> arguments = List.of("--mem", "--CORS=file", "--localhost", "/path");
        String expectedMessage = "CORS config file not found: file";
        // when, then
        testForCmdException(arguments, expectedMessage);
    }

    @Test
    public void test_happy_corsConfig() {
        // given
        List<String> arguments = List.of("--port=0", "--mem", "--CORS=testing/Config/cors.properties", "--localhost", "/path");
        // when
        buildServer(buildCmdLineArguments(arguments));
        // then
        assertNotNull(server);
    }

    private void testForCmdException(List<String> arguments, String expectedMessage) {
        // when
        CmdException actual = assertThrows(CmdException.class, ()->buildServer(buildCmdLineArguments(arguments)));
        // then
        assertEquals(expectedMessage, actual.getMessage(), "Expecting correct message");
    }

    private static String[] buildCmdLineArguments(List<String> listArgs) {
        return listArgs.toArray(new String[0]);
    }

    // Build and set the server
    private void buildServer(String... cmdline) {
        if ( server != null )
            fail("Bad test - a server has already been created");
        server = FusekiMain.build(cmdline);
        server.start();
    }
}
