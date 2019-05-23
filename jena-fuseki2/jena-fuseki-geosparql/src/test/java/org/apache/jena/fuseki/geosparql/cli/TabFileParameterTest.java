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
package org.apache.jena.fuseki.geosparql.cli;

import com.beust.jcommander.ParameterException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
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
public class TabFileParameterTest {

    public TabFileParameterTest() {
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
     * Test of convert method, of class TabFileParameter.
     */
    @Test
    public void testConvert() {
        System.out.println("convert");
        String value = "test.rdf#test|TAB,test2.rdf";
        TabFileParameter instance = new TabFileParameter();
        List<FileGraphDelimiter> expResult = Arrays.asList(new FileGraphDelimiter(new File("test.rdf"), "test", "TAB"), new FileGraphDelimiter(new File("test2.rdf"), "", "COMMA"));
        List<FileGraphDelimiter> result = instance.convert(value);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of build method, of class TabFileParameter.
     */
    @Test
    public void testBuild() {
        System.out.println("build");
        String value = "test.rdf|SPACE";
        TabFileParameter instance = new TabFileParameter();
        FileGraphDelimiter expResult = new FileGraphDelimiter(new File("test.rdf"), "", "SPACE");
        FileGraphDelimiter result = instance.build(value);

        //System.out.println("Exp: " + expResult);
        //System.out.println("Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TabFileParameter.
     */
    @Test(expected = ParameterException.class)
    public void testValidate() {
        System.out.println("validate");
        String name = "--tab_file";
        String value = "test.csv|COMMA#test";
        TabFileParameter instance = new TabFileParameter();
        instance.validate(name, value);
    }

}
