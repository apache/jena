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
package io.github.galbiston.rdf_tables.datatypes;

import static org.junit.Assert.assertEquals;

import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.DatatypeController;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.*;

/**
 *
 * @author Greg Albiston
 */
public class DatatypeControllerTest {

    public DatatypeControllerTest() {
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
     * Test of createLiteral DateTime method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralDate() {
        //System.out.println("extractLiteralDate");
        String dateTime = "2001-10-26";
        String dataTypeURI = XSDBaseNumericType.XSDdate.getURI();

        Literal expResult = ResourceFactory.createTypedLiteral(dateTime, XSDBaseNumericType.XSDdate);

        Literal result = DatatypeController.createLiteral(dateTime, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral DateTime method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralDateTime() {
        //System.out.println("extractLiteralDateTime");
        String dateTime = "2001-10-26T21:32:52";
        String dataTypeURI = XSDBaseNumericType.XSDdateTime.getURI();

        Literal expResult = ResourceFactory.createTypedLiteral(dateTime, XSDBaseNumericType.XSDdateTime);

        Literal result = DatatypeController.createLiteral(dateTime, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral Time method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralTime() {
        //System.out.println("extractLiteralTime");
        String time = "21:32:52";
        String dataTypeURI = XSDBaseNumericType.XSDtime.getURI();

        Literal expResult = ResourceFactory.createTypedLiteral(time, XSDBaseNumericType.XSDtime);

        Literal result = DatatypeController.createLiteral(time, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral Duration method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralDuration() {
        //System.out.println("extractLiteralDuration");
        String duration = "PT1004199059S";
        String dataTypeURI = XSDBaseNumericType.XSDduration.getURI();

        Literal expResult = ResourceFactory.createTypedLiteral(duration, XSDBaseNumericType.XSDduration);

        Literal result = DatatypeController.createLiteral(duration, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral Double method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralDouble() {
        //System.out.println("extractLiteralDouble");
        String data = "4.2";
        String dataTypeURI = XSDBaseNumericType.XSDdouble.getURI();

        Double doubleValue = Double.parseDouble(data);
        Literal expResult = ResourceFactory.createTypedLiteral(doubleValue);

        Literal result = DatatypeController.createLiteral(data, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral Decimal method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralDecimal() {
        //System.out.println("extractLiteralDecimal");
        String data = "4.2";
        String dataTypeURI = XSDBaseNumericType.XSDdecimal.getURI();

        Literal expResult = ResourceFactory.createTypedLiteral(data, XSDBaseNumericType.XSDdecimal);

        Literal result = DatatypeController.createLiteral(data, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral Integer method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralInteger() {
        //System.out.println("extractLiteralInteger");
        String data = "3";
        String dataTypeURI = XSDBaseNumericType.XSDinteger.getURI();

        Literal expResult = ResourceFactory.createTypedLiteral(data, XSDBaseNumericType.XSDinteger);

        Literal result = DatatypeController.createLiteral(data, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral Int method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralInt() {
        //System.out.println("extractLiteralInt");
        String data = "3";
        String dataTypeURI = XSDBaseNumericType.XSDint.getURI();

        int integerValue = Integer.parseInt(data);
        Literal expResult = ResourceFactory.createTypedLiteral(integerValue);

        Literal result = DatatypeController.createLiteral(data, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral Positive Int method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralPositiveInt() {
        //System.out.println("extractLiteralPositiveInt");
        String data = "3";
        String dataTypeURI = XSDBaseNumericType.XSDpositiveInteger.getURI();

        Literal expResult = ResourceFactory.createTypedLiteral(data, XSDBaseNumericType.XSDpositiveInteger);

        Literal result = DatatypeController.createLiteral(data, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral Boolean method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralBoolean() {
        //System.out.println("extractLiteralBoolean");
        String data = "true";
        String dataTypeURI = XSDBaseNumericType.XSDboolean.getURI();

        boolean booleanValue = Boolean.valueOf(data);
        Literal expResult = ResourceFactory.createTypedLiteral(booleanValue);

        Literal result = DatatypeController.createLiteral(data, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of createLiteral String method, of class DataTypeExtract.
     */
    @Test
    public void testExtractLiteralString() {
        //System.out.println("extractLiteralString");
        String data = "Blah Blah";
        String dataTypeURI = XSDBaseNumericType.XSDstring.getURI();

        Literal expResult = ResourceFactory.createTypedLiteral(data);

        Literal result = DatatypeController.createLiteral(data, dataTypeURI);
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }
}
