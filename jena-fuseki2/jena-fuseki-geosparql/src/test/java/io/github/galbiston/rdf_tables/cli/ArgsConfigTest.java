/**
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.galbiston.rdf_tables.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import org.apache.jena.ext.io.github.galbiston.rdf_tables.cli.ArgsConfig;
import org.apache.jena.riot.RDFFormat;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Gerg
 */
public class ArgsConfigTest {

    public ArgsConfigTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getOutputFormat method, of class ArgsConfig.
     */
    @Test
    public void testGetOutputFormat_XML() {
        //System.out"getOutputFormat_XML");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"--format", "xml", "-i", "test.rdf"};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        RDFFormat expResult = RDFFormat.RDFXML;
        RDFFormat result = args.getOutputFormat();

        ////System.out"Exp: " + expResult);
        ////System.out"Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getOutputFormat method, of class ArgsConfig.
     */
    @Test
    public void testGetOutputFormat() {
        //System.out"getOutputFormat_TTL");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"--format", "ttl", "-i", "test.rdf"};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        RDFFormat expResult = RDFFormat.TTL;
        RDFFormat result = args.getOutputFormat();

        ////System.out"Exp: " + expResult);
        ////System.out"Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of isNamedIndividual method, of class ArgsConfig.
     */
    @Test
    public void testIsNamedIndividual() {
        //System.out"getIsNamedIndividual");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"-i", "test.rdf"};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        Boolean expResult = true;
        Boolean result = args.isOwlNamedIndividual();

        ////System.out"Exp: " + expResult);
        ////System.out"Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of isNamedIndividual method, of class ArgsConfig.
     */
    @Test
    public void testIsNamedIndividual_false() {
        //System.out"getIsNamedIndividual_false");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"-i", "test.rdf", "-n", "false"};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        Boolean expResult = false;
        Boolean result = args.isOwlNamedIndividual();

        ////System.out"Exp: " + expResult);
        ////System.out"Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInputDelimiter method, of class ArgsConfig.
     */
    @Test
    public void testGetInputDelimiter_tab() {
        //System.out"getInputDelimiter_tab");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"-i", "test.rdf", "-l", "TAB"};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        String expResult = "TAB";
        String result = args.getInputDelimiter();

        //System.out"Exp: " + expResult);
        //System.out"Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInputDelimiter method, of class ArgsConfig.
     */
    @Test
    public void testGetInputDelimiter_space() {
        //System.out"getInputDelimiter_space");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"-i", "test.rdf", "-l", "SPACE"};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        String expResult = "SPACE";
        String result = args.getInputDelimiter();

        ////System.out"Exp: " + expResult);
        ////System.out"Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInputDelimiter method, of class ArgsConfig.
     */
    @Test
    public void testGetInputDelimiter_comma() {
        //System.out"getInputDelimiter_comma");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"-i", "test.rdf"};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        String expResult = "COMMA";
        String result = args.getInputDelimiter();

        ////System.out"Exp: " + expResult);
        ////System.out"Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInputDelimiter method, of class ArgsConfig.
     */
    @Test
    public void testGetInputDelimiter_comma2() {
        //System.out"getInputDelimiter_comma2");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"-i", "test.rdf", "-l", ","};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        String expResult = ",";
        String result = args.getInputDelimiter();

        ////System.out"Exp: " + expResult);
        ////System.out"Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInputDelimiter method, of class ArgsConfig.
     */
    @Test(expected = ParameterException.class)
    public void testGetInputDelimiter_reserved() {
        //System.out"getInputDelimiter_reserved");
        ArgsConfig args = new ArgsConfig();

        String[] argv = {"-i", "test.rdf", "-l", "|"};
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argv);

        String expResult = ",";
        String result = args.getInputDelimiter();

        ////System.out"Exp: " + expResult);
        ////System.out"Res: " + result);
        assertEquals(expResult, result);
    }

}
