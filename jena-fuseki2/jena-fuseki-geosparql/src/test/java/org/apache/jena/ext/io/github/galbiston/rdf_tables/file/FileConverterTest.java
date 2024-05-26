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
package org.apache.jena.ext.io.github.galbiston.rdf_tables.file;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.DatatypeController;
import org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.PrefixController;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.*;

/**
 *
 * @author Greg Albiston
 */
public class FileConverterTest {

    private static Model testModel;

    public FileConverterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        URL u = FileConverterTest.class.getClassLoader().getResource("TestData.csv");
        // These URLs have the file name as an encoded string.
        String fn = IRILib.decodeHex(u.getFile());
        File inputFile = new File(fn);
        testModel = ModelFactory.createDefaultModel();
        DatatypeController.addPrefixDatatypeURI("wkt", "http://www.opengis.net/ont/geosparql#wktLiteral");
        PrefixController.addPrefix("other", "http://example.org/other#");
        FileConverter.writeToModel(inputFile, testModel);
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
     * Test of Age method, of class FileConverter. Check int datatype.
     */
    @Test
    public void testAge() {
        //System.out.println("Age");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonA");
        Property predicate = ResourceFactory.createProperty("http://example.org#age");
        Literal object = ResourceFactory.createTypedLiteral(21);
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of ShoeSize method, of class FileConverter. Check integer datatype
     * and local property URI.
     */
    @Test
    public void testShoeSize() {
        //System.out.println("ShoeSize");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonA");
        Property predicate = ResourceFactory.createProperty("http://example2.org/ont#shoeSize");
        Literal object = ResourceFactory.createTypedLiteral("8", XSDDatatype.XSDinteger);
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Income method, of class FileConverter. Check double datatype.
     */
    @Test
    public void testIncome() {
        //System.out.println("Income");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonA");
        Property predicate = ResourceFactory.createProperty("http://example.org#income");
        Literal object = ResourceFactory.createTypedLiteral(12000.01);
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Time method, of class FileConverter. Check time datatype.
     */
    @Test
    public void testTime() {
        //System.out.println("Time");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonA");
        Property predicate = ResourceFactory.createProperty("http://example.org#start");
        Literal object = ResourceFactory.createTypedLiteral("11:00:12", XSDBaseNumericType.XSDtime);
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Period method, of class FileConverter. Check duration datatype.
     */
    @Test
    public void testPeriod() {
        //System.out.println("Period");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonA");
        Property predicate = ResourceFactory.createProperty("http://example.org#period");
        Literal object = ResourceFactory.createTypedLiteral("PT130S", XSDBaseNumericType.XSDduration);
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Appointment method, of class FileConverter. Check dateTime
     * datatype.
     */
    @Test
    public void testAppointment() {
        //System.out.println("Appointment");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonA");
        Property predicate = ResourceFactory.createProperty("http://example.org#appointment");
        Literal object = ResourceFactory.createTypedLiteral("2001-10-26T21:32:52+02:00", XSDBaseNumericType.XSDdateTime);
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Birthday method, of class FileConverter. Check date datatype.
     */
    @Test
    public void testBirthday() {
        //System.out.println("Birthday");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonA");
        Property predicate = ResourceFactory.createProperty("http://example.org#birthday");
        Literal object = ResourceFactory.createTypedLiteral("2001-10-26", XSDBaseNumericType.XSDdate);
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Driving Licence method, of class FileConverter. Check boolean
     * datatype.
     */
    @Test
    public void testDrivingLicence() {
        //System.out.println("DrivingLicence");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonB");
        Property predicate = ResourceFactory.createProperty("http://example.org#drivingLicence");
        Literal object = ResourceFactory.createTypedLiteral(true);
        Statement s = ResourceFactory.createStatement(subject, predicate, object);

        Literal r = testModel.getProperty(subject, predicate).getLiteral();
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Person method, of class FileConverter. Check rdf:type creation.
     */
    @Test
    public void testPerson() {
        //System.out.println("Person");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonB");
        Property predicate = RDF.type;
        Resource object = ResourceFactory.createResource("http://example.org#Person");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);

        boolean result = testModel.contains(s);
        boolean expResult = true;

        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Name method, of class FileConverter. Check string datatype.
     */
    @Test
    public void testName() {
        //System.out.println("Name");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonC");
        Property predicate = ResourceFactory.createProperty("http://example.org#firstname");
        Literal object = ResourceFactory.createTypedLiteral("Peter");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Label method, of class FileConverter. Check rdfs:label creation.
     */
    @Test
    public void testLabel() {
        //System.out.println("Label");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonD");
        Property predicate = RDFS.label;
        Literal object = ResourceFactory.createTypedLiteral("PersonD");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Vehicle method, of class FileConverter. Check resource datatype.
     */
    @Test
    public void testVehicle() {
        //System.out.println("Vehicle");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonE");
        Property predicate = ResourceFactory.createProperty("http://example.org#vehicle");
        Resource object = ResourceFactory.createResource("http://example.org#VehicleE");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Vehicle2 method, of class FileConverter. Check local URI.
     */
    @Test
    public void testVehicle2() {
        //System.out.println("Vehicle2");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonD");
        Property predicate = ResourceFactory.createProperty("http://example.org#vehicle");
        Resource object = ResourceFactory.createResource("http://example2.org/ont#VehicleD");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Vehicle3 method, of class FileConverter. Check repeated property
     * URI.
     */
    @Test
    public void testVehicle3() {
        //System.out.println("Vehicle3");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonE");
        Property predicate = ResourceFactory.createProperty("http://example.org#vehicle");
        Resource object = ResourceFactory.createResource("http://example.org#VehicleE");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);

        Resource object2 = ResourceFactory.createResource("http://example.org#VehicleF");
        Statement s2 = ResourceFactory.createStatement(subject, predicate, object2);

        boolean result = testModel.contains(s) && testModel.contains(s2);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Vehicle method, of class FileConverter. Check prefix for class
     * type.
     */
    @Test
    public void testVehicle4() {
        //System.out.println("Vehicle4");

        Resource subject = ResourceFactory.createResource("http://example.org#VehicleF");
        Property predicate = RDF.type;
        Resource object = ResourceFactory.createResource("http://example.org/other#Vehicle");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);

        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Missing Filed method, of class FileConverter. Check missing
     * field.
     */
    @Test
    public void testMissingField() {
        //System.out.println("MissingField");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonE");
        Property predicate = ResourceFactory.createProperty("http://example.org#name");
        Literal object = ResourceFactory.createTypedLiteral("");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);
        boolean result = testModel.contains(s);
        boolean expResult = false;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of WKT method, of class FileConverter. Check user datatype.
     */
    @Test
    public void testUserDatatype() {
        //System.out.println("userDatatype");

        Resource subject = ResourceFactory.createResource("http://example.org#VehicleA");
        Property predicate = ResourceFactory.createProperty("http://example.org#position");
        String result = testModel.getProperty(subject, predicate).getLiteral().getLexicalForm();

        String expResult = ResourceFactory.createTypedLiteral("POINT(10 10)", new BaseDatatype("http://www.opengis.net/ont/geosparql#wktLiteral")).getLexicalForm();

        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of prefix datatype method, of class FileConverter. Check prefix for
     * class type.
     */
    @Test
    public void testPrefixDatatype() {
        //System.out.println("prefixDatatype");

        Resource subject = ResourceFactory.createResource("http://example.org#VehicleA");
        Property predicate = ResourceFactory.createProperty("http://example.org#positionAccuracy");
        String result = testModel.getProperty(subject, predicate).getLiteral().getLexicalForm();

        String expResult = ResourceFactory.createTypedLiteral("5.1", new BaseDatatype("http://example.org/other#accuracy")).getLexicalForm();

        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of prefix datatype using datatype URI lookup method, of class
     * FileConverter. Check prefix for class type.
     */
    @Test
    public void testPrefixDatatype2() {
        //System.out.println("prefixDatatype2");

        Resource subject = ResourceFactory.createResource("http://example.org#VehicleA");
        Property predicate = ResourceFactory.createProperty("http://example.org#positionAccuracy");
        Literal object = ResourceFactory.createTypedLiteral("5.1", DatatypeController.lookupDatatype("http://example.org/other#accuracy"));
        Statement s = ResourceFactory.createStatement(subject, predicate, object);

        boolean result = testModel.contains(s);
        boolean expResult = true;

        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of InvertProperty method, of class FileConverter. Check inverting
     * the property. type.
     */
    @Test
    public void testInvertProperty() {
        //System.out.println("InvertProperty");

        Resource subject = ResourceFactory.createResource("http://example.org#HouseA");
        Property predicate = ResourceFactory.createProperty("http://example.org#hasOwner");
        Resource object = ResourceFactory.createResource("http://example.org#PersonA");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);

        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Object created for single arg column header. type.
     */
    @Test
    public void testDefaultObject() {
        //System.out.println("DefaultObject");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonA");
        Property predicate = ResourceFactory.createProperty("http://example.org#hasFriend");
        Resource object = ResourceFactory.createResource("http://example.org#PersonB");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);

        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

    /**
     * Test of Object created for single arg column header. type.
     */
    @Test
    public void testPositionObject() {
        //System.out.println("Position");

        Resource subject = ResourceFactory.createResource("http://example.org#PersonB");
        Property predicate = ResourceFactory.createProperty("http://example.org#hasPet");
        Resource object = ResourceFactory.createResource("http://example.org#PetB");
        Statement s = ResourceFactory.createStatement(subject, predicate, object);

        boolean result = testModel.contains(s);
        boolean expResult = true;
        ////System.out.println("Exp: " + expResult + " Res: " + result);
        assertEquals(expResult, result);
    }

}
