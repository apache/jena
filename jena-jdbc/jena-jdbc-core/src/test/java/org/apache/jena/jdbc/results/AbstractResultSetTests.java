/**
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

package org.apache.jena.jdbc.results;

import java.io.InputStream ;
import java.io.Reader ;
import java.math.BigDecimal ;
import java.sql.* ;
import java.util.Calendar ;
import java.util.HashMap ;

import org.apache.jena.jdbc.JdbcCompatibility ;
import org.apache.jena.jdbc.results.metadata.AskResultsMetadata ;
import org.apache.jena.jdbc.results.metadata.TripleResultsMetadata ;
import org.junit.* ;

import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.vocabulary.XSD ;

/**
 * Abstract tests for Jena JDBC {@link ResultSet} implementations, these tests
 * assume that implementations use the {@link JdbcCompatibility#HIGH}
 * compatibility level
 * 
 */
public abstract class AbstractResultSetTests {

    static {
        ARQ.init();
    }

    private static Dataset empty, ds;

    /**
     * Sets up the datasets used for the tests
     */
    @BeforeClass
    public static void globalSetup() {
        // Empty dataset
        if (empty == null) {
            empty = DatasetFactory.createMem();
        }

        // Build a dataset that has one of every type we expect to
        // commonly see
        if (ds == null) {
            ds = DatasetFactory.createMem();

            // Create model and our RDF terms
            Model m = ModelFactory.createDefaultModel();
            Resource subjUri = m.createResource("http://example/subject");
            Resource subjBlank = m.createResource();
            Property predUri = m.createProperty("http://example/predicate");
            Resource objUri = m.createResource("http://example/object");
            Resource objBlank = m.createResource();
            Literal objSimpleLiteral = m.createLiteral("simple");
            Literal objLangLiteral = m.createLiteral("simple", "en");
            Literal objBoolean = m.createTypedLiteral(true);
            Literal objByte = m.createTypedLiteral(Byte.toString((byte) 123), XSDDatatype.XSDbyte);
            Literal objDateTime = m.createTypedLiteral(Calendar.getInstance());
            Literal objDate = m.createTypedLiteral(NodeFactoryExtra.dateToNode(Calendar.getInstance()).getLiteralLexicalForm(),
                    XSDDatatype.XSDdate);
            Literal objTime = m.createTypedLiteral(NodeFactoryExtra.timeToNode(Calendar.getInstance()).getLiteralLexicalForm(),
                    XSDDatatype.XSDtime);
            Literal objChar = m.createTypedLiteral('a');
            Literal objDecimal = m.createTypedLiteral(new BigDecimal(123.4));
            Literal objDouble = m.createTypedLiteral(123.4d);
            Literal objFloat = m.createTypedLiteral(123.4f);
            Literal objInteger = m.createTypedLiteral(1234);
            Literal objLong = m.createTypedLiteral(1234l);
            Literal objShort = m.createTypedLiteral(Short.toString((short) 123), XSDDatatype.XSDshort);
            Literal objString = m.createTypedLiteral("typed");
            Literal objCustom = m.createTypedLiteral("custom",
                    TypeMapper.getInstance().getSafeTypeByName("http://example/customType"));

            m.add(new Statement[] {
                    // Simple triples with URIs and Blank Nodes only
                    m.createStatement(subjUri, predUri, objUri),
                    m.createStatement(subjUri, predUri, objBlank),
                    m.createStatement(subjBlank, predUri, objUri),
                    m.createStatement(subjBlank, predUri, objBlank),
                    // Simple triples with simple literals as objects
                    m.createStatement(subjUri, predUri, objSimpleLiteral),
                    m.createStatement(subjUri, predUri, objLangLiteral),
                    // Triples with typed literals as objects
                    m.createStatement(subjUri, predUri, objBoolean), m.createStatement(subjUri, predUri, objByte),
                    m.createStatement(subjUri, predUri, objDateTime), m.createStatement(subjUri, predUri, objDate),
                    m.createStatement(subjUri, predUri, objTime), m.createStatement(subjUri, predUri, objChar),
                    m.createStatement(subjUri, predUri, objDecimal), m.createStatement(subjUri, predUri, objDouble),
                    m.createStatement(subjUri, predUri, objFloat), m.createStatement(subjUri, predUri, objInteger),
                    m.createStatement(subjUri, predUri, objLong), m.createStatement(subjUri, predUri, objShort),
                    m.createStatement(subjUri, predUri, objString), m.createStatement(subjUri, predUri, objCustom) });
            ds.setDefaultModel(m);
        }
    }

    /**
     * Cleans up the datasets used for tests
     */
    @AfterClass
    public static void globalTeardown() {
        ds.close();
        ds = null;
        empty.close();
        empty = null;
    }

    protected abstract ResultSet createResults(Dataset ds, String query) throws SQLException;

    protected abstract ResultSet createResults(Dataset ds, String query, int resultSetType) throws SQLException;

    protected String getIntegerTypeUri() {
        return XSD.xint.toString();
    }

    protected String getByteTypeUri() {
        return XSD.xbyte.toString();
    }

    protected String getShortTypeUri() {
        return XSD.xshort.toString();
    }

    protected String getLongTypeUri() {
        return XSD.xlong.toString();
    }

    /**
     * Test ASK results with a true result
     * 
     * @throws SQLException
     */
    @Test
    public void results_ask_true() throws SQLException {
        ResultSet rset = this.createResults(AbstractResultSetTests.empty, "ASK { }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Try to move to the result row
        Assert.assertTrue(rset.next());

        // Check the boolean return value
        Assert.assertTrue(rset.getBoolean(AskResultsMetadata.COLUMN_LABEL_ASK));

        // Check no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());

        // Close and clean up
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Test ASK results with a false result
     * 
     * @throws SQLException
     */
    @Test
    public void results_ask_false() throws SQLException {
        ResultSet rset = this.createResults(AbstractResultSetTests.empty, "ASK { FILTER(false) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Try to move to the result row
        Assert.assertTrue(rset.next());

        // Check the boolean return value
        Assert.assertFalse(rset.getBoolean(AskResultsMetadata.COLUMN_LABEL_ASK));

        // Check no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());

        // Close and clean up
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT (STR(?o) AS ?str) { ?s ?p ?o . FILTER(!ISBLANK(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to obtain appropriately typed values
        while (rset.next()) {
            Object obj = rset.getObject("str");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof String);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(ISNUMERIC(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to obtain appropriately typed values
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Long || obj instanceof Integer || obj instanceof Short || obj instanceof Byte
                    || obj instanceof BigDecimal || obj instanceof Double || obj instanceof Float);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_03() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.dateTime.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Timestamp);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_04() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.time.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Time);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_05() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.date.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Date);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }
    
    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_06() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xboolean.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Boolean);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }
    
    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_07() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getLongTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Long);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }
    
    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_08() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xdouble.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Double);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }
    
    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_09() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xfloat.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Float);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }
    
    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_10() throws SQLException {
        Assume.assumeTrue(this.getShortTypeUri().equals(XSD.xshort.getURI()));
        
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getShortTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Integer);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }
    
    /**
     * Tests that SELECT result values are appropriately marshalled when using
     * getObject()
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_objects_11() throws SQLException {
        Assume.assumeTrue(this.getByteTypeUri().equals(XSD.xbyte.getURI()));
        
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getByteTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Object obj = rset.getObject("o");
            Assert.assertNotNull(obj);
            Assert.assertFalse(rset.wasNull());
            Assert.assertTrue(obj instanceof Byte);
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_strings_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT (STR(?o) AS ?str) { ?s ?p ?o . FILTER(!ISBLANK(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("str"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_strings_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT (STR(?o) AS ?str) { ?s ?p ?o . FILTER(!ISBLANK(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getNString("str"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_strings_03() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT (STR(?o) AS ?str) { ?s ?p ?o . FILTER(!ISBLANK(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_strings_04() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT (STR(?o) AS ?str) { ?s ?p ?o . FILTER(!ISBLANK(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getNString(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_strings_01() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?str } WHERE { ?s ?p ?o . FILTER(!ISBLANK(?o)) . BIND(STR(?o) AS ?str) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_strings_02() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?str } WHERE { ?s ?p ?o . FILTER(!ISBLANK(?o)) . BIND(STR(?o) AS ?str) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getNString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_strings_03() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?str } WHERE { ?s ?p ?o . FILTER(!ISBLANK(?o)) . BIND(STR(?o) AS ?str) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_strings_04() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?str } WHERE { ?s ?p ?o . FILTER(!ISBLANK(?o)) . BIND(STR(?o) AS ?str) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getNString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_urls_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?s { ?s ?p ?o . FILTER(ISURI(?s)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal URLs OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getURL("s"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_urls_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?s { ?s ?p ?o . FILTER(ISURI(?s)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal URLs OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getURL(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_urls_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(ISURI(?s)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal URLs OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getURL(TripleResultsMetadata.COLUMN_LABEL_SUBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to strings OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_urls_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(ISURI(?s)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal URLs OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getURL(TripleResultsMetadata.COLUMN_INDEX_SUBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to numerics OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_numeric_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(ISNUMERIC(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal big decimal OK
        BigDecimal zero = new BigDecimal(0);
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            // Use decimal since all numeric types should be promotable to
            // decimal
            Assert.assertFalse(rset.getBigDecimal("o").equals(zero));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to numerics OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_numeric_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(ISNUMERIC(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal big decimal OK
        BigDecimal zero = new BigDecimal(0);
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            // Use decimal since all numeric types should be promotable to
            // decimal
            Assert.assertFalse(rset.getBigDecimal(1).equals(zero));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to numerics OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_numerics_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(ISNUMERIC(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal big decimal OK
        BigDecimal zero = new BigDecimal(0);
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            // Use decimal since all numeric types should be promotable to
            // decimal
            Assert.assertFalse(rset.getBigDecimal(TripleResultsMetadata.COLUMN_LABEL_OBJECT).equals(zero));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to numerics OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_numerics_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(ISNUMERIC(?o)) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal big decimal OK
        BigDecimal zero = new BigDecimal(0);
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            // Use decimal since all numeric types should be promotable to
            // decimal
            Assert.assertFalse(rset.getBigDecimal(TripleResultsMetadata.COLUMN_INDEX_OBJECT).equals(zero));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to integers OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_integers_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getIntegerTypeUri()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal integers OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertFalse(0 == rset.getInt("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to integers OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_integers_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getIntegerTypeUri()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal integers OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertFalse(0 == rset.getInt(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to integers OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_integers_01() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getIntegerTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal integers OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(0 == rset.getInt(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to integers OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_integers_02() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getIntegerTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal integers OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(0 == rset.getInt(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to bytes OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_bytes_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getByteTypeUri()
                + "> && ?o <= 255) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal bytes OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertFalse((byte) 0 == rset.getByte("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to bytes OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_bytes_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getByteTypeUri()
                + "> && ?o <= 255) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal bytes OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertFalse((byte) 0 == rset.getByte(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to bytes OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_bytes_01() throws SQLException {
        ResultSet rset = this
                .createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getByteTypeUri()
                        + "> && ?o <= 255) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal bytes OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse((byte) 0 == rset.getByte(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to bytes OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_bytes_02() throws SQLException {
        ResultSet rset = this
                .createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getByteTypeUri()
                        + "> && ?o <= 255) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal bytes OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse((byte) 0 == rset.getByte(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to floats OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_floats_01() throws SQLException {
        ResultSet rset = this
                .createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xfloat.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal floats OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertFalse(0f == rset.getFloat("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to floats OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_floats_02() throws SQLException {
        ResultSet rset = this
                .createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xfloat.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal floats OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertFalse(0f == rset.getFloat(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to floats OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_floats_01() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xfloat.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal floats OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(0f == rset.getFloat(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to floats OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_floats_02() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xfloat.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal floats OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(0f == rset.getFloat(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to doubles OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_doubles_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xdouble.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal doubles OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertFalse(0d == rset.getDouble("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to doubles OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_doubles_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xdouble.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal doubles OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertFalse(0d == rset.getDouble(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to doubles OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_doubles_01() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xdouble.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal doubles OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(0d == rset.getDouble(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to doubles OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_doubles_02() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.xdouble.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal doubles OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(0d == rset.getDouble(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to longs OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_longs_01() throws SQLException {
        ResultSet rset = this
                .createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getLongTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal longs OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertFalse(0l == rset.getLong("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to longs OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_longs_02() throws SQLException {
        ResultSet rset = this
                .createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getLongTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal longs OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertFalse(0l == rset.getLong(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to longs OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_longs_01() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getLongTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal longs OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(0l == rset.getLong(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to longs OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_longs_02() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getLongTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal longs OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(0l == rset.getLong(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to shorts OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_shorts_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getShortTypeUri()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal shorts OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertFalse((short) 0 == rset.getShort("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to shorts OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_shorts_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getShortTypeUri()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal shorts OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertFalse((short) 0 == rset.getShort(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to shorts OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_shorts_01() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getShortTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal shorts OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse((short) 0 == rset.getShort(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to shorts OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_shorts_02() throws SQLException {
        ResultSet rset = this.createResults(ds,
                "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + this.getShortTypeUri() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal shorts OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse((short) 0 == rset.getShort(TripleResultsMetadata.COLUMN_INDEX_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to boolean OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_boolean_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "PREFIX xsd: <" + XSD.getURI()
                + "> SELECT ?bool { ?s ?p ?o . BIND(xsd:boolean(?o) AS ?bool) . }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal big decimal OK
        while (rset.next()) {
            boolean b = rset.getBoolean("bool");
            if (!rset.wasNull()) {
                Assert.assertTrue(b || !b);
                Assert.assertFalse(rset.wasNull());
            } else {
                Assert.assertTrue(rset.wasNull());
            }
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to numerics OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_boolean_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "PREFIX xsd: <" + XSD.getURI()
                + "> SELECT ?bool { ?s ?p ?o . BIND(xsd:boolean(?o) AS ?bool) . }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal big decimal OK
        while (rset.next()) {
            boolean b = rset.getBoolean(1);
            if (!rset.wasNull()) {
                Assert.assertTrue(b || !b);
                Assert.assertFalse(rset.wasNull());
            } else {
                Assert.assertTrue(rset.wasNull());
            }
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_dates_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.dateTime.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertNotNull(rset.getDate("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_dates_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.dateTime.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertNotNull(rset.getTime("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_dates_03() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.dateTime.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString("o"));
            Assert.assertNotNull(rset.getTimestamp("o"));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_dates_04() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.dateTime.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertNotNull(rset.getDate(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_dates_05() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.dateTime.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertNotNull(rset.getTime(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that SELECT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_dates_06() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT ?o { ?s ?p ?o . FILTER(DATATYPE(?o) = <" + XSD.dateTime.toString()
                + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(1));
            Assert.assertNotNull(rset.getTimestamp(1));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_dates_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <"
                + XSD.dateTime.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertNotNull(rset.getDate(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_dates_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <"
                + XSD.dateTime.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertNotNull(rset.getTime(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests that CONSTRUCT result values can be marshalled to dates OK
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_dates_03() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER(DATATYPE(?o) = <"
                + XSD.dateTime.toString() + ">) }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Check all rows allow us to marshal strings OK
        while (rset.next()) {
            Assert.assertNotNull(rset.getString(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertNotNull(rset.getTimestamp(TripleResultsMetadata.COLUMN_LABEL_OBJECT));
            Assert.assertFalse(rset.wasNull());
        }

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_FORWARD_ONLY);
        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Expect exactly one row we can move to
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.isAfterLast());
        Assert.assertTrue(rset.isLast());
        Assert.assertFalse(rset.next());

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_FORWARD_ONLY);
        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Expect exactly one row we can move to
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.isAfterLast());
        Assert.assertTrue(rset.isLast());
        Assert.assertFalse(rset.next());

        // Attempting to move backwards in a forwards only result set should
        // result in an error
        try {
            rset.beforeFirst();
            Assert.fail("Should not be permitted to move backwards in a FORWARD_ONLY result set");
        } catch (SQLException e) {
            // Expected
        } finally {

            Assert.assertTrue(rset.isAfterLast());
            rset.close();
            Assert.assertTrue(rset.isClosed());
        }
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_03() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_FORWARD_ONLY);
        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to last row
        Assert.assertTrue(rset.last());
        Assert.assertFalse(rset.isAfterLast());
        Assert.assertTrue(rset.isLast());
        Assert.assertFalse(rset.next());

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_04() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_FORWARD_ONLY);
        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to after last row
        rset.afterLast();
        Assert.assertTrue(rset.isAfterLast());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_05() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Expect exactly one row we can move to
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.isAfterLast());
        Assert.assertTrue(rset.isLast());
        Assert.assertFalse(rset.next());

        // Attempting to move backwards in a scrollable result set should result
        // set should be fine
        rset.beforeFirst();
        Assert.assertTrue(rset.isBeforeFirst());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_06() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 2", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to first row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertFalse(rset.isLast());

        // Then can move forwards to second row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isLast());

        // Can move backwards to previous row
        Assert.assertTrue(rset.previous());
        Assert.assertTrue(rset.isFirst());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_07() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 5", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move to various absolute rows
        Assert.assertTrue(rset.absolute(1));
        Assert.assertTrue(rset.absolute(1));
        Assert.assertTrue(rset.absolute(3));
        Assert.assertTrue(rset.absolute(2));
        Assert.assertTrue(rset.absolute(4));
        Assert.assertTrue(rset.absolute(5));
        Assert.assertTrue(rset.absolute(-1));
        Assert.assertTrue(rset.absolute(-2));
        Assert.assertTrue(rset.absolute(-3));
        Assert.assertTrue(rset.absolute(-4));
        Assert.assertTrue(rset.absolute(-5));

        // 0 is treated as moving to before first
        Assert.assertFalse(rset.absolute(0));
        Assert.assertTrue(rset.isBeforeFirst());

        // 1 is treated as moving to first row
        Assert.assertTrue(rset.absolute(1));
        Assert.assertTrue(rset.isFirst());

        // -1 is treated as moving to last row
        Assert.assertTrue(rset.absolute(-1));
        Assert.assertTrue(rset.isLast());

        // Moving to a row beyond end positions us after last and returns false
        Assert.assertFalse(rset.absolute(6));
        Assert.assertTrue(rset.isAfterLast());

        // Moving to a row before start positions us before first and returns
        // false
        Assert.assertFalse(rset.absolute(-6));
        Assert.assertTrue(rset.isBeforeFirst());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_08() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 2", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to after last row
        rset.afterLast();
        Assert.assertTrue(rset.isAfterLast());

        // Can move backwards to last row
        Assert.assertTrue(rset.last());
        Assert.assertTrue(rset.isLast());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_select_movement_09() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * { ?s ?p ?o . } LIMIT 2", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to after last row
        rset.afterLast();
        Assert.assertTrue(rset.isAfterLast());

        // Moving backwards more rows than possible with relative movement
        // should place us before first row
        Assert.assertFalse(rset.relative(-4));
        Assert.assertTrue(rset.isBeforeFirst());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through CONSTRUCT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_FORWARD_ONLY);
        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Expect exactly one row we can move to
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.isAfterLast());
        Assert.assertTrue(rset.isLast());
        Assert.assertFalse(rset.next());

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through CONSTRUCT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_FORWARD_ONLY);
        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Expect exactly one row we can move to
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.isAfterLast());
        Assert.assertTrue(rset.isLast());
        Assert.assertFalse(rset.next());

        // Attempting to move backwards in a forwards only result set should
        // result in an error
        try {
            rset.beforeFirst();
            Assert.fail("Should not be permitted to move backwards in a FORWARD_ONLY result set");
        } catch (SQLException e) {
            // Expected
        } finally {

            Assert.assertTrue(rset.isAfterLast());
            rset.close();
            Assert.assertTrue(rset.isClosed());
        }
    }

    /**
     * Tests movement through CONSTRUCT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_03() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_FORWARD_ONLY);
        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to last row
        Assert.assertTrue(rset.last());
        Assert.assertFalse(rset.isAfterLast());
        Assert.assertTrue(rset.isLast());
        Assert.assertFalse(rset.next());

        Assert.assertTrue(rset.isAfterLast());
        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through CONSTRUCT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_04() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_FORWARD_ONLY);
        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to after last row
        rset.afterLast();
        Assert.assertTrue(rset.isAfterLast());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through CONSTRUCT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_05() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 1", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Expect exactly one row we can move to
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.isAfterLast());
        Assert.assertTrue(rset.isLast());
        Assert.assertFalse(rset.next());

        // Attempting to move backwards in a scrollable result set should result
        // set should be fine
        rset.beforeFirst();
        Assert.assertTrue(rset.isBeforeFirst());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through CONSTRUCT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_06() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 2", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to first row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertFalse(rset.isLast());

        // Then can move forwards to second row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isLast());

        // Can move backwards to previous row
        Assert.assertTrue(rset.previous());
        Assert.assertTrue(rset.isFirst());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through CONSTRUCT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_07() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 5", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move to various absolute rows
        Assert.assertTrue(rset.absolute(1));
        Assert.assertTrue(rset.absolute(1));
        Assert.assertTrue(rset.absolute(3));
        Assert.assertTrue(rset.absolute(2));
        Assert.assertTrue(rset.absolute(4));
        Assert.assertTrue(rset.absolute(5));
        Assert.assertTrue(rset.absolute(-1));
        Assert.assertTrue(rset.absolute(-2));
        Assert.assertTrue(rset.absolute(-3));
        Assert.assertTrue(rset.absolute(-4));
        Assert.assertTrue(rset.absolute(-5));

        // 0 is treated as moving to before first
        Assert.assertFalse(rset.absolute(0));
        Assert.assertTrue(rset.isBeforeFirst());

        // 1 is treated as moving to first row
        Assert.assertTrue(rset.absolute(1));
        Assert.assertTrue(rset.isFirst());

        // -1 is treated as moving to last row
        Assert.assertTrue(rset.absolute(-1));
        Assert.assertTrue(rset.isLast());

        // Moving to a row beyond end positions us after last and returns false
        Assert.assertFalse(rset.absolute(6));
        Assert.assertTrue(rset.isAfterLast());

        // Moving to a row before start positions us before first and returns
        // false
        Assert.assertFalse(rset.absolute(-6));
        Assert.assertTrue(rset.isBeforeFirst());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through CONSTRUCT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_08() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 2", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to after last row
        rset.afterLast();
        Assert.assertTrue(rset.isAfterLast());

        // Can move backwards to last row
        Assert.assertTrue(rset.last());
        Assert.assertTrue(rset.isLast());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Tests movement through SELECT results
     * 
     * @throws SQLException
     */
    @Test
    public void results_construct_movement_09() throws SQLException {
        ResultSet rset = this.createResults(ds, "CONSTRUCT WHERE { ?s ?p ?o . } LIMIT 2", ResultSet.TYPE_SCROLL_INSENSITIVE);
        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, rset.getType());
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.isLast());

        // Can move forwards to after last row
        rset.afterLast();
        Assert.assertTrue(rset.isAfterLast());

        // Moving backwards more rows than possible with relative movement
        // should place us before first row
        Assert.assertFalse(rset.relative(-4));
        Assert.assertTrue(rset.isBeforeFirst());

        rset.close();
        Assert.assertTrue(rset.isClosed());
    }

    /**
     * Test error cases for creating results
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void results_bad_creation_01() throws SQLException {
        new AskResults(null, true, false);
    }

    /**
     * Test error cases for unsupported wrapper features
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_wrapper_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.isWrapperFor(JenaResultSet.class);
        } finally {
            rset.close();
        }
    }

    /**
     * Test error cases for unsupported wrapper features
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_wrapper_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.unwrap(JenaResultSet.class);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.cancelRowUpdates();
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.deleteRow();
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_03() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.insertRow();
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_04() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.moveToCurrentRow();
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_05() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.moveToInsertRow();
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test
    public void results_bad_updates_06() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");
        Assert.assertFalse(rset.rowDeleted());
        rset.close();
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test
    public void results_bad_updates_07() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");
        Assert.assertFalse(rset.rowInserted());
        rset.close();
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test
    public void results_bad_updates_08() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");
        Assert.assertFalse(rset.rowUpdated());
        rset.close();
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_09() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateArray(1, (Array) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_10() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateArray("s", (Array) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_11() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateAsciiStream(1, (InputStream) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_12() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateAsciiStream("s", (InputStream) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_13() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateAsciiStream(1, (InputStream) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_14() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateAsciiStream("s", (InputStream) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_15() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateAsciiStream(1, (InputStream) null, 0l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_16() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateAsciiStream("s", (InputStream) null, 0l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_18() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBigDecimal(1, (BigDecimal) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_17() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBigDecimal("s", (BigDecimal) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_19() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBinaryStream(1, (InputStream) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_20() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBinaryStream("s", (InputStream) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_21() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBinaryStream(1, (InputStream) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_22() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBinaryStream("s", (InputStream) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_23() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBinaryStream(1, (InputStream) null, 0l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_24() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBinaryStream("s", (InputStream) null, 0l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_25() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBlob(1, (Blob) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_26() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBlob("s", (Blob) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_27() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBlob(1, (InputStream) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_28() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBlob("s", (InputStream) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_29() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBlob(1, (InputStream) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_30() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBlob("s", (InputStream) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_31() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBoolean(1, true);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_32() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBoolean("s", true);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_33() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateByte(1, (byte) 123);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_34() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateByte("s", (byte) 123);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_35() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBytes(1, new byte[0]);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_36() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateBytes("s", new byte[0]);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_37() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateCharacterStream(1, (Reader) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_38() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateCharacterStream("s", (Reader) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_39() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateCharacterStream(1, (Reader) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_40() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateCharacterStream("s", (Reader) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_41() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateCharacterStream(1, (Reader) null, 0l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_42() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateCharacterStream("s", (Reader) null, 0l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_43() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateClob(1, (Clob) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_44() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateClob("s", (Clob) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_45() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateClob(1, (Reader) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_46() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateClob("s", (Reader) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_47() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateClob(1, (Reader) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_48() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateClob("s", (Reader) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_49() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateDate(1, (Date) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_50() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateDate("s", (Date) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_51() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateDouble(1, 123d);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_52() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateDouble("s", 123d);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_53() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateFloat(1, 123f);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_54() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateFloat("s", 123f);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_55() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateInt(1, 123);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_56() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateInt("s", 123);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_57() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateLong(1, 123l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_58() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateLong("s", 123l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_59() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNCharacterStream(1, (Reader) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_60() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNCharacterStream("s", (Reader) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_61() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNCharacterStream(1, (Reader) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_62() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNCharacterStream("s", (Reader) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_63() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNCharacterStream(1, (Reader) null, 0l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_64() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNCharacterStream("s", (Reader) null, 0l);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_65() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNClob(1, (NClob) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_66() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNClob("s", (NClob) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_67() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNClob(1, (Reader) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_68() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNClob("s", (Reader) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_69() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNClob(1, (Reader) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_70() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNClob("s", (Reader) null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_71() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNull(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_72() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNull("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_73() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateObject(1, null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_74() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateObject("s", null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_75() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateObject(1, null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_76() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateObject("s", null, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_77() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateRef(1, (Ref) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_78() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateRef("s", (Ref) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_79() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateRowId(1, (RowId) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_80() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateRowId("s", (RowId) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_81() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateSQLXML(1, (SQLXML) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_82() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateSQLXML("s", (SQLXML) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_83() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateShort(1, (short) 123);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_84() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateShort("s", (short) 123);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_85() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateString(1, "");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_86() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateString("s", "");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_87() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateTime(1, (Time) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_88() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateTime("s", (Time) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_89() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateTimestamp(1, (Timestamp) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_90() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateTimestamp("s", (Timestamp) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_91() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateRow();
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_92() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNString(1, "");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around trying to update the result set which is not
     * supported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_bad_updates_93() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.updateNString("s", "");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getArray(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_02() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getArray("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_03() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getAsciiStream(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_04() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getAsciiStream("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @SuppressWarnings("deprecation")
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_05() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBigDecimal(1, 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @SuppressWarnings("deprecation")
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_06() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBigDecimal("s", 0);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_07() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBinaryStream(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_08() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBinaryStream("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_09() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBlob(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_10() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBlob("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_11() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBinaryStream(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_12() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBinaryStream("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_13() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBytes(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_14() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getBytes("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_15() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getCharacterStream(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_16() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getCharacterStream("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_17() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getClob(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_18() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getClob("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_19() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getNCharacterStream(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_20() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getNCharacterStream("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_21() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getNClob(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_22() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getNClob("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_23() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getObject(1, new HashMap<String, Class<?>>());
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_24() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getObject("s", new HashMap<String, Class<?>>());
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_25() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getRef(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_26() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getRef("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_27() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getRowId(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_28() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getRowId("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_29() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getSQLXML(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_30() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getSQLXML("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_31() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getTime(1, (Calendar) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_32() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getTime("s", (Calendar) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_33() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getTimestamp(1, (Calendar) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_34() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getTimestamp("s", (Calendar) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @SuppressWarnings("deprecation")
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_35() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getUnicodeStream(1);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @SuppressWarnings("deprecation")
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_36() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getUnicodeStream("s");
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_37() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getDate(1, (Calendar) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Tests error cases around unsupported getters
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void results_getters_unsupported_38() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        try {
            rset.getDate("s", (Calendar) null);
        } finally {
            rset.close();
        }
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void results_warnings_01() throws SQLException {
        ResultSet rset = this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        Assert.assertNull(rset.getWarnings());

        rset.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void results_warnings_02() throws SQLException {
        JenaResultSet rset = (JenaResultSet) this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        Assert.assertNull(rset.getWarnings());
        rset.setWarning("Test");
        Assert.assertNotNull(rset.getWarnings());
        rset.clearWarnings();
        Assert.assertNull(rset.getWarnings());

        rset.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void results_warnings_03() throws SQLException {
        JenaResultSet rset = (JenaResultSet) this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        Assert.assertNull(rset.getWarnings());
        rset.setWarning("Test", new Exception());
        Assert.assertNotNull(rset.getWarnings());
        rset.clearWarnings();
        Assert.assertNull(rset.getWarnings());

        rset.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void results_warnings_04() throws SQLException {
        JenaResultSet rset = (JenaResultSet) this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        Assert.assertNull(rset.getWarnings());
        rset.setWarning(new SQLWarning());
        Assert.assertNotNull(rset.getWarnings());
        rset.clearWarnings();
        Assert.assertNull(rset.getWarnings());

        rset.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void results_warnings_05() throws SQLException {
        JenaResultSet rset = (JenaResultSet) this.createResults(ds, "SELECT * WHERE { ?s ?p ?o }");

        Assert.assertNull(rset.getWarnings());
        rset.setWarning("A");
        Assert.assertNotNull(rset.getWarnings());
        rset.setWarning("B");
        Assert.assertNotNull(rset.getWarnings());
        Assert.assertNotNull(rset.getWarnings().getNextWarning());
        rset.clearWarnings();
        Assert.assertNull(rset.getWarnings());

        rset.close();
    }
}
