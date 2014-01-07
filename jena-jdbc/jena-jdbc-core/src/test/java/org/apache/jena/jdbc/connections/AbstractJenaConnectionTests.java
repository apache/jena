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
package org.apache.jena.jdbc.connections;

import java.net.MalformedURLException ;
import java.net.URL ;
import java.sql.* ;
import java.sql.ResultSet ;
import java.util.HashMap ;
import java.util.Properties ;

import org.apache.jena.jdbc.JdbcCompatibility ;
import org.apache.jena.jdbc.postprocessing.ResultsEcho ;
import org.apache.jena.jdbc.preprocessing.Echo ;
import org.apache.jena.jdbc.results.AskResults ;
import org.apache.jena.jdbc.results.SelectResults ;
import org.apache.jena.jdbc.results.TripleIteratorResults ;
import org.apache.jena.jdbc.results.metadata.AskResultsMetadata ;
import org.apache.jena.jdbc.results.metadata.TripleResultsMetadata ;
import org.apache.jena.jdbc.utils.TestUtils ;
import org.junit.Assert ;
import org.junit.Assume ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

/**
 * Abstract tests for {@link JenaConnection} implementations
 * 
 */
public abstract class AbstractJenaConnectionTests {

    static {
        ARQ.init();
    }

    /**
     * Method which derived test classes must implement to provide a connection
     * to an empty database for testing
     * 
     * @return Connection
     * @throws SQLException
     */
    protected abstract JenaConnection getConnection() throws SQLException;

    /**
     * Method which derived test classes must implement to provide a connection
     * to a database constructed from the given dataset for testing
     * 
     * @return Connection
     * @throws SQLException
     */
    protected abstract JenaConnection getConnection(Dataset ds) throws SQLException;

    /**
     * Method which indicates whether a named graph is being used as the default
     * graph since some tests need to know this in order to adjust SPARQL
     * Updates issued appropriately
     * <p>
     * By default assumed to be false, override if you need to make it true for
     * your connection
     * </p>
     * 
     * @return Whether a named graph is used as the default graph
     */
    protected boolean usesNamedGraphAsDefault() {
        return false;
    }

    /**
     * Method which indicates whether the connection being tested supports query
     * timeouts
     * 
     * @return True if query timeouts are supported
     */
    protected boolean supportsTimeouts() {
        return true;
    }

    /**
     * Method which returns the name of the default graph when a named graph is
     * being used as the default graph
     * 
     * @return Named Graph being used as the default graph
     * @throws SQLException
     *             Thrown if this feature is not being used
     */
    protected String getDefaultGraphName() throws SQLException {
        throw new SQLException(
                "Named Default Graph not used by these tests, please override getDefaultGraphName() if your connection uses this feature");
    }

    /**
     * Create and close a connection to an empty database
     * 
     * @throws SQLException
     */
    @Test
    public void connection_create_close_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Create and close a connection to an explicitly provided empty database
     * 
     * @throws SQLException
     */
    @Test
    public void connection_create_close_02() throws SQLException {
        JenaConnection conn = this.getConnection(DatasetFactory.createMem());
        Assert.assertFalse(conn.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Retrieve and close a statement
     * 
     * @throws SQLException
     */
    @Test
    public void connection_get_statement_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        Assert.assertNotNull(stmt);
        Assert.assertFalse(stmt.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Trying to retrieve a statement from a closed connection is an error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_get_statement_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();
        Assert.assertTrue(conn.isClosed());

        // Trying to create a statement after the connection is closed is an
        // error
        conn.createStatement();
    }

    /**
     * Trying to use a statement from a closed connection is an error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_get_statement_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        Assert.assertNotNull(stmt);
        Assert.assertFalse(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());

        // Trying to use a statement after the connection is closed is an error
        stmt.execute("SELECT * { ?s ?p ?o }");
    }

    /**
     * Runs a SELECT query on an empty database and checks it returns empty
     * results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_select_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkSelectMetadata(rset, 3);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(stmt.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a SELECT query on a non-empty database and checks it returns
     * non-empty results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_select_02() throws SQLException {
        // Prepare a dataset
        Dataset ds = DatasetFactory.createMem();
        ds.asDatasetGraph().add(
                new Quad(NodeFactory.createURI("http://example/graph"), NodeFactory.createURI("http://example/subject"),
                        NodeFactory.createURI("http://example/predicate"), NodeFactory.createURI("http://example/object")));

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { GRAPH ?g { ?s ?p ?o } }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 4);

        // Should have a row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertEquals(1, rset.getRow());

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a SELECT query on a non-empty database and checks it returns
     * non-empty results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_select_03() throws SQLException {
        // Prepare a dataset
        Dataset ds = DatasetFactory.createMem();
        ds.asDatasetGraph().add(
                new Quad(NodeFactory.createURI("http://example/graph"), NodeFactory.createURI("http://example/subject"),
                        NodeFactory.createURI("http://example/predicate"), NodeFactory.createURI("http://example/object")));

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 1);

        // Should have a row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertEquals(1, rset.getRow());

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a SELECT query on a non-empty database and checks it returns
     * non-empty results. Uses high compatibility level to ensure that column
     * type detection doesn't consume the first row.
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_select_04() throws SQLException {
        // Prepare a dataset
        Dataset ds = DatasetFactory.createMem();
        ds.asDatasetGraph().add(
                new Quad(NodeFactory.createURI("http://example/graph"), NodeFactory.createURI("http://example/subject"),
                        NodeFactory.createURI("http://example/predicate"), NodeFactory.createURI("http://example/object")));

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        conn.setJdbcCompatibilityLevel(JdbcCompatibility.HIGH);
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { GRAPH ?g { ?s ?p ?o } }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 4);

        // Should have a row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertEquals(1, rset.getRow());

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests use of prepared statements
     * 
     * @throws SQLException
     * @throws MalformedURLException
     */
    @Test
    public void connection_prepared_statement_select_01() throws SQLException, MalformedURLException {
        // Prepare a dataset
        Dataset ds = DatasetFactory.createMem();
        ds.asDatasetGraph().add(
                new Quad(NodeFactory.createURI("http://example/graph"), NodeFactory.createURI("http://example/subject"),
                        NodeFactory.createURI("http://example/predicate"), NodeFactory.createURI("http://example/object")));

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        conn.setJdbcCompatibilityLevel(JdbcCompatibility.HIGH);
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { GRAPH ? { ?s ?p ?o } }");
        ParameterMetaData metadata = stmt.getParameterMetaData();
        Assert.assertEquals(1, metadata.getParameterCount());
        stmt.setURL(1, new URL("http://example/graph"));

        ResultSet rset = stmt.executeQuery();
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 3);

        // Should have a row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertEquals(1, rset.getRow());

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests use of prepared statements
     * 
     * @throws SQLException
     * @throws MalformedURLException
     */
    @Test
    public void connection_prepared_statement_select_02() throws SQLException, MalformedURLException {
        // Prepare a dataset
        Dataset ds = DatasetFactory.createMem();
        ds.asDatasetGraph().add(
                new Quad(NodeFactory.createURI("http://example/graph"), NodeFactory.createURI("http://example/subject"),
                        NodeFactory.createURI("http://example/predicate"), NodeFactory.createLiteral("value")));

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        conn.setJdbcCompatibilityLevel(JdbcCompatibility.HIGH);
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { GRAPH ?g { ?s ?p ? } }");
        ParameterMetaData metadata = stmt.getParameterMetaData();
        Assert.assertEquals(1, metadata.getParameterCount());
        stmt.setString(1, "value");

        ResultSet rset = stmt.executeQuery();
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 3);

        // Should have a row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertEquals(1, rset.getRow());

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests use of prepared statements
     * 
     * @throws SQLException
     * @throws MalformedURLException
     */
    @Test
    public void connection_prepared_statement_select_03() throws SQLException, MalformedURLException {
        // Prepare a dataset
        Dataset ds = DatasetFactory.createMem();
        ds.asDatasetGraph().add(
                new Quad(NodeFactory.createURI("http://example/graph"), NodeFactory.createURI("http://example/subject"),
                        NodeFactory.createURI("http://example/predicate"), NodeFactory.createLiteral("value")));

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        conn.setJdbcCompatibilityLevel(JdbcCompatibility.HIGH);
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { GRAPH ?g { ?s ?p ? } }");
        ParameterMetaData metadata = stmt.getParameterMetaData();
        Assert.assertEquals(1, metadata.getParameterCount());
        stmt.setNString(1, "value");

        ResultSet rset = stmt.executeQuery();
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 3);

        // Should have a row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertEquals(1, rset.getRow());

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

   

    /**
     * Runs a SELECT query on a non-empty database with max rows set and checks
     * that the appropriate number of rows are returned
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_select_max_rows_01() throws SQLException {
        // Prepare a dataset
        Dataset ds = TestUtils.generateDataset(3, 10, false);

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();
        stmt.setMaxRows(10);
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { GRAPH ?g { ?s ?p ?o } }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 4);

        // Check expected number of rows
        int count = 0;
        while (rset.next()) {
            count++;
        }
        Assert.assertEquals(10, count);

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a SELECT query on a non-empty database with max rows set and checks
     * that the appropriate number of rows are returned
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_select_max_rows_02() throws SQLException {
        // Prepare a dataset
        Dataset ds = TestUtils.generateDataset(3, 10, false);

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();

        // Set max rows to 10, note that if the query specifies a lower limit
        // then that is respected
        stmt.setMaxRows(10);
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { GRAPH ?g { ?s ?p ?o } } LIMIT 1");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 4);

        // Check expected number of rows
        int count = 0;
        while (rset.next()) {
            count++;
        }
        Assert.assertEquals(1, count);

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a SELECT query on a non-empty database with max rows set and checks
     * that the appropriate number of rows are returned
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_select_max_rows_03() throws SQLException {
        // Prepare a dataset
        Dataset ds = TestUtils.generateDataset(3, 10, false);

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();

        // Set max rows to 20, note that if the query specifies a higher limit
        // then the max rows are still enforced
        stmt.setMaxRows(10);
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { GRAPH ?g { ?s ?p ?o } } LIMIT 50");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Check result set metadata
        checkSelectMetadata(rset, 4);

        // Check expected number of rows
        int count = 0;
        while (rset.next()) {
            count++;
        }
        Assert.assertEquals(10, count);

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a SELECT query on a non-empty database with timeout
     * 
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test(expected = SQLException.class)
    public void connection_statement_query_select_timeout_01() throws SQLException, InterruptedException {
        Assume.assumeTrue(this.supportsTimeouts());

        // Prepare a dataset
        Dataset ds = TestUtils.generateDataset(1, 1000, true);

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();

        // Set timeout to 1 second
        stmt.setQueryTimeout(1);
        try {
            ResultSet rset = stmt.executeQuery("SELECT * WHERE { "
                    + (this.usesNamedGraphAsDefault() ? "GRAPH <" + this.getDefaultGraphName() + "> {" : "")
                    + " ?a ?b ?c . ?d ?e ?f . ?g ?h ?i " + (this.usesNamedGraphAsDefault() ? "}" : "") + "}");

            // Note that we have to start iterating otherwise the query doesn't
            // get executed and the timeout will never apply
            while (rset.next()) {
                Thread.sleep(100);
            }

            rset.close();
            Assert.fail("Expected a query timeout");
        } finally {
            // Close things
            stmt.close();
            conn.close();
        }
    }

    /**
     * Runs a SELECT query on a non-empty database with timeout
     * 
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test(expected = SQLException.class)
    public void connection_statement_query_select_timeout_02() throws SQLException, InterruptedException {
        Assume.assumeTrue(this.supportsTimeouts());

        // Prepare a dataset
        Dataset ds = TestUtils.generateDataset(1, 1000, true);

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        // Set timeout to 1 second
        stmt.setQueryTimeout(1);
        try {
            ResultSet rset = stmt.executeQuery("SELECT * WHERE { "
                    + (this.usesNamedGraphAsDefault() ? "GRAPH <" + this.getDefaultGraphName() + "> {" : "")
                    + " ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . " + (this.usesNamedGraphAsDefault() ? "}" : "") + "}");

            // Note that we have to start iterating otherwise the query doesn't
            // get executed and the timeout will never apply
            while (rset.next()) {
                Thread.sleep(100);
            }

            rset.close();
            Assert.fail("Expected a query timeout");
        } finally {
            // Close things
            stmt.close();
            conn.close();
        }
    }

    /**
     * Runs a SELECT query on a non-empty database with timeout
     * 
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test(expected = SQLException.class)
    public void connection_statement_query_construct_timeout_01() throws SQLException, InterruptedException {
        Assume.assumeTrue(this.supportsTimeouts());

        // Prepare a dataset
        Dataset ds = TestUtils.generateDataset(1, 1000, true);

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();

        // Set timeout to 1 second
        stmt.setQueryTimeout(1);
        try {
            ResultSet rset = stmt.executeQuery("CONSTRUCT { ?s ?p ?o } WHERE { "
                    + (this.usesNamedGraphAsDefault() ? "GRAPH <" + this.getDefaultGraphName() + "> {" : "")
                    + " FILTER(<http://jena.hpl.hp.com/ARQ/function#wait>(1500)) " + (this.usesNamedGraphAsDefault() ? "}" : "") + "}");

            // Note that we have to start iterating otherwise the query doesn't
            // get executed and the timeout will never apply
            while (rset.next()) {
                Thread.sleep(100);
            }

            rset.close();
            Assert.fail("Expected a query timeout");
        } finally {
            // Close things
            stmt.close();
            conn.close();
        }
    }

    /**
     * Runs a SELECT query on a non-empty database with timeout
     * 
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test(expected = SQLException.class)
    public void connection_statement_query_construct_timeout_02() throws SQLException, InterruptedException {
        Assume.assumeTrue(this.supportsTimeouts());

        // Prepare a dataset
        Dataset ds = TestUtils.generateDataset(1, 1000, true);

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        // Set timeout to 1 second
        stmt.setQueryTimeout(1);
        try {
            ResultSet rset = stmt.executeQuery("CONSTRUCT { ?s ?p ?o } WHERE { "
                    + (this.usesNamedGraphAsDefault() ? "GRAPH <" + this.getDefaultGraphName() + "> {" : "")
                    + " FILTER(<http://jena.hpl.hp.com/ARQ/function#wait>(1500)) " + (this.usesNamedGraphAsDefault() ? "}" : "") + "}");

            // Note that we have to start iterating otherwise the query doesn't
            // get executed and the timeout will never apply
            while (rset.next()) {
                Thread.sleep(100);
            }

            rset.close();
            Assert.fail("Expected a query timeout");
        } finally {
            // Close things
            stmt.close();
            conn.close();
        }
    }

    protected void checkSelectMetadata(ResultSet results, int numColumns) throws SQLException {
        ResultSetMetaData metadata = results.getMetaData();
        Assert.assertEquals(numColumns, metadata.getColumnCount());
        for (int i = 1; i <= numColumns; i++) {
            Assert.assertEquals(String.class.getCanonicalName(), metadata.getColumnClassName(i));
            Assert.assertEquals(Node.class.getCanonicalName(), metadata.getColumnTypeName(i));
            Assert.assertEquals(Types.NVARCHAR, metadata.getColumnType(i));
        }
    }

    /**
     * Runs an ASK query on an empty database and checks it returns true
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_ask_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("ASK WHERE { }");
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkAskMetadata(rset);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.getBoolean(1));

        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    protected void checkAskMetadata(ResultSet results) throws SQLException {
        ResultSetMetaData metadata = results.getMetaData();
        Assert.assertEquals(1, metadata.getColumnCount());
        Assert.assertEquals(Boolean.class.getCanonicalName(), metadata.getColumnClassName(1));
        Assert.assertEquals(Node.class.getCanonicalName(), metadata.getColumnTypeName(1));
        Assert.assertEquals(Types.BOOLEAN, metadata.getColumnType(1));
    }

    /**
     * Runs a CONSTRUCT query on an empty database and checks it returns empty
     * results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_construct_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("CONSTRUCT WHERE { ?s ?p ?o }");
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkConstructDescribeMetadata(rset);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a CONSTRUCT query on a non-empty database and checks it returns
     * non-empty results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_construct_02() throws SQLException {
        // Prepare a dataset
        Dataset ds = DatasetFactory.createMem();
        ds.asDatasetGraph().add(
                new Quad(NodeFactory.createURI("http://example/graph"), NodeFactory.createURI("http://example/subject"),
                        NodeFactory.createURI("http://example/predicate"), NodeFactory.createURI("http://example/object")));

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Should have a row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertEquals(1, rset.getRow());

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a CONSTRUCT query on an empty database and checks it returns empty
     * results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_describe_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("DESCRIBE <http://subject>");
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkConstructDescribeMetadata(rset);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a CONSTRUCT query on a non-empty database and checks it returns
     * non-empty results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_query_describe_02() throws SQLException {
        // Prepare a dataset
        Dataset ds = DatasetFactory.createMem();
        ds.asDatasetGraph().add(
                new Quad(NodeFactory.createURI("http://example/graph"), NodeFactory.createURI("http://example/subject"),
                        NodeFactory.createURI("http://example/predicate"), NodeFactory.createURI("http://example/object")));

        // Work with the connection
        JenaConnection conn = this.getConnection(ds);
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("DESCRIBE ?s WHERE { GRAPH ?g { ?s ?p ?o } }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Should have a row
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.isFirst());
        Assert.assertEquals(1, rset.getRow());

        // Should be no further rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(rset.isClosed());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    protected void checkConstructDescribeMetadata(ResultSet results) throws SQLException {
        // Check result set metadata
        ResultSetMetaData metadata = results.getMetaData();
        Assert.assertEquals(3, metadata.getColumnCount());
        for (int i = 1; i <= TripleResultsMetadata.NUM_COLUMNS; i++) {
            Assert.assertEquals(String.class.getCanonicalName(), metadata.getColumnClassName(i));
            Assert.assertEquals(Node.class.getCanonicalName(), metadata.getColumnTypeName(i));
            Assert.assertEquals(Types.NVARCHAR, metadata.getColumnType(i));
        }
    }

    /**
     * Does a basic read transaction
     * 
     * @throws SQLException
     */
    @Test
    public void connection_transactions_01() throws SQLException {
        // Set up connection
        JenaConnection conn = this.getConnection(DatasetFactory.createMem());
        Assume.assumeNotNull(conn.getMetaData());
        Assume.assumeTrue(conn.getMetaData().supportsTransactions());
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        conn.setAutoCommit(true);

        // Make a read operation
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Should have no rows
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Does a basic write transaction
     * 
     * @throws SQLException
     */
    @Test
    public void connection_transactions_02() throws SQLException {
        // Set up connection
        JenaConnection conn = this.getConnection(DatasetFactory.createMem());
        Assume.assumeNotNull(conn.getMetaData());
        Assume.assumeTrue(conn.getMetaData().supportsTransactions());
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        conn.setAutoCommit(true);
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);

        // Make a write operation
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT DATA { <http://x> <http://y> <http://z> }");

        // Make a subsequent read, with auto-commit we should see some data
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Should have one row
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.next());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Does a basic write transaction without auto-commit and then commits it
     * 
     * @throws SQLException
     */
    @Test
    public void connection_transactions_03() throws SQLException {
        // Set up connection
        JenaConnection conn = this.getConnection(DatasetFactory.createMem());
        Assume.assumeNotNull(conn.getMetaData());
        Assume.assumeTrue(conn.getMetaData().supportsTransactions());
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        conn.setAutoCommit(false);
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);

        // Make a write operation
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT DATA { <http://x> <http://y> <http://z> }");

        // Make a subsequent read, with auto-commit we should see some data
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Should have one row
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.next());
        rset.close();
        Assert.assertTrue(rset.isClosed());

        // Commit the transaction
        conn.commit();

        // Check we still can read the data
        rset = stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Should have one row
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.next());
        rset.close();
        Assert.assertTrue(rset.isClosed());

        // Close things
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Does a basic write transaction without auto-commit and then rolls it back
     * 
     * @throws SQLException
     */
    @Test
    public void connection_transactions_04() throws SQLException {
        // Set up connection
        JenaConnection conn = this.getConnection(DatasetFactory.createMem());
        Assume.assumeNotNull(conn.getMetaData());
        Assume.assumeTrue(conn.getMetaData().supportsTransactions());
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        conn.setAutoCommit(false);
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);

        // Make a write operation
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT DATA { <http://x> <http://y> <http://z> }");

        // Make a subsequent read, with auto-commit we should see some data
        ResultSet rset = stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Should have one row
        Assert.assertTrue(rset.next());
        Assert.assertFalse(rset.next());
        rset.close();
        Assert.assertTrue(rset.isClosed());

        // Rollback the transaction
        conn.rollback();

        // Check we can no longer read the data
        rset = stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        Assert.assertNotNull(rset);
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());

        // Should have no rows
        Assert.assertFalse(rset.next());
        rset.close();
        Assert.assertTrue(rset.isClosed());

        // Close things
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests error cases for transactions
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_transaction_bad_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Trying to commit a transaction on a closed connection should be an
        // error
        conn.commit();
    }

    /**
     * Tests error cases for transactions
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_transaction_bad_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Trying to commit a transaction on a closed connection should be an
        // error
        conn.rollback();
    }

    /**
     * Test error cases for creating statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_statement_bad_creation_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.createStatement();
    }

    /**
     * Test error cases for creating statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_statement_bad_creation_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Test error cases for creating statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_statement_bad_creation_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }

    /**
     * Test error cases for creating statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_statement_bad_creation_04() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            // Creating a SCOLL_SENSITIVE statement is not supported
            conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } finally {
            conn.close();
        }
    }

    /**
     * Test error cases for creating statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_statement_bad_creation_05() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            // Creating a CONCUR_UPDATABLE statement is not supported
            conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } finally {
            conn.close();
        }
    }

    /**
     * Test error cases for creating prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_prepared_statement_bad_creation_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }");
    }

    /**
     * Test error cases for creating prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_prepared_statement_bad_creation_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }", 1);
    }

    /**
     * Test error cases for creating prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_prepared_statement_bad_creation_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }", new int[0]);
    }

    /**
     * Test error cases for creating prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_prepared_statement_bad_creation_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }", new String[0]);
    }

    /**
     * Test error cases for creating prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_prepared_statement_bad_creation_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Test error cases for creating prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_prepared_statement_bad_creation_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Creating a statement after closing should be an error
        conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT);
    }

    /**
     * Test error cases for creating prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_prepared_statement_bad_creation_07() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            // Creating a statement with SCROLL_SENSITIVE is not supported
            conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } finally {
            conn.close();
        }
    }

    /**
     * Test error cases for creating prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_prepared_statement_bad_creation_08() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            // Creating a statement with CONCUR_UPDATABLE is not supported
            conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } finally {
            conn.close();
        }
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        // Create and execute a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(1, batchResults.length);

        // Expect first result set returned to be SELECT results
        Assert.assertEquals(batchResults[0], Statement.SUCCESS_NO_INFO);
        ResultSet rset = stmt.getResultSet();
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkSelectMetadata(rset, 3);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(stmt.isClosed());

        // Expect there to be no further results
        Assert.assertFalse(stmt.getMoreResults());
        Assert.assertTrue(rset.isClosed()); // Should cause previous result set
                                            // to be closed

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        // Create and execute a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        stmt.addBatch("ASK WHERE { }");
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(2, batchResults.length);

        // Expect first result set returned to be SELECT results
        Assert.assertEquals(batchResults[0], Statement.SUCCESS_NO_INFO);
        ResultSet rset = stmt.getResultSet();
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkSelectMetadata(rset, 3);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(stmt.isClosed());

        // Expect there to be a further ASK result set
        Assert.assertEquals(batchResults[1], Statement.SUCCESS_NO_INFO);
        Assert.assertTrue(stmt.getMoreResults());
        Assert.assertTrue(rset.isClosed()); // Should close the previous result
                                            // set
        rset = stmt.getResultSet();
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkAskMetadata(rset);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertTrue(rset.next());
        Assert.assertTrue(rset.getBoolean(AskResultsMetadata.COLUMN_LABEL_ASK));
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);

        // This test needs scroll insensitive result sets as otherwise the first
        // SELECT may see data from the update if the underlying connection does
        // not have suitable transaction isolation
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        // Create and execute a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        if (this.usesNamedGraphAsDefault()) {
            stmt.addBatch("INSERT DATA { GRAPH <" + this.getDefaultGraphName() + "> { <http://x> <http://y> <http://z> } }");
        } else {
            stmt.addBatch("INSERT DATA { <http://x> <http://y> <http://z> }");
        }
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(3, batchResults.length);

        // Expect first result set returned to be SELECT results
        Assert.assertEquals(batchResults[0], Statement.SUCCESS_NO_INFO);
        ResultSet rset = stmt.getResultSet();
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkSelectMetadata(rset, 3);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());
        Assert.assertFalse(stmt.isClosed());

        // Next results should be for the update so should return null for the
        // result set
        Assert.assertTrue(batchResults[1] >= 0);
        Assert.assertTrue(stmt.getMoreResults());
        rset = stmt.getResultSet();
        Assert.assertNull(rset);

        // Expect there to be a further SELECT result set
        Assert.assertEquals(batchResults[2], Statement.SUCCESS_NO_INFO);
        Assert.assertTrue(stmt.getMoreResults());
        rset = stmt.getResultSet();
        Assert.assertNotNull(rset);

        // Check result set metadata
        checkSelectMetadata(rset, 3);

        // Check result set
        Assert.assertFalse(rset.isClosed());
        Assert.assertTrue(rset.isBeforeFirst());
        Assert.assertTrue(rset.next()); // Should now be a row because previous
                                        // command in batch inserted data
        Assert.assertFalse(rset.next());
        Assert.assertTrue(rset.isAfterLast());

        // Close things
        rset.close();
        Assert.assertTrue(rset.isClosed());
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        // Create and execute a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        stmt.addBatch("ASK WHERE { }");
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(2, batchResults.length);
        Assert.assertEquals(batchResults[0], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[1], Statement.SUCCESS_NO_INFO);

        // Closing the statement should clean up any open result sets even if we
        // haven't retrieved them yet
        stmt.close();
        Assert.assertTrue(stmt.isClosed());

        // Clean up
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        // Create and execute a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        stmt.addBatch("ASK WHERE { }");
        stmt.addBatch("CONSTRUCT WHERE { ?s ?p ?o }");
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(3, batchResults.length);
        Assert.assertEquals(batchResults[0], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[1], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[2], Statement.SUCCESS_NO_INFO);

        // Get first result set
        ResultSet rset = stmt.getResultSet();
        Assert.assertTrue(rset instanceof SelectResults);
        checkSelectMetadata(rset, 3);

        // Grab the next result set leaving this one open
        Assert.assertTrue(stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT));
        Assert.assertFalse(rset.isClosed());
        ResultSet rset2 = stmt.getResultSet();
        Assert.assertTrue(rset2 instanceof AskResults);
        checkAskMetadata(rset2);

        // Closing the statement should clean up any open result sets even if we
        // haven't retrieved them yet or have previously moved past them but
        // left them open
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        Assert.assertTrue(rset.isClosed());
        Assert.assertTrue(rset2.isClosed());

        // Clean up
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        // Create and execute a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        stmt.addBatch("ASK WHERE { }");
        stmt.addBatch("CONSTRUCT WHERE { ?s ?p ?o }");
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(3, batchResults.length);
        Assert.assertEquals(batchResults[0], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[1], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[2], Statement.SUCCESS_NO_INFO);

        // Get first result set
        ResultSet rset = stmt.getResultSet();
        Assert.assertTrue(rset instanceof SelectResults);
        checkSelectMetadata(rset, 3);

        // Close this and grab the next result set
        Assert.assertTrue(stmt.getMoreResults(Statement.CLOSE_CURRENT_RESULT));
        Assert.assertTrue(rset.isClosed());
        ResultSet rset2 = stmt.getResultSet();
        Assert.assertTrue(rset2 instanceof AskResults);
        checkAskMetadata(rset2);

        // Closing the statement should clean up any open result sets even if we
        // haven't retrieved them yet or have previously moved past them but
        // left them open
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        Assert.assertTrue(rset2.isClosed());

        // Clean up
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_07() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        // Create and execute a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        stmt.addBatch("ASK WHERE { }");
        stmt.addBatch("CONSTRUCT WHERE { ?s ?p ?o }");
        stmt.addBatch("DESCRIBE <http://example>");
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(4, batchResults.length);
        Assert.assertEquals(batchResults[0], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[1], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[2], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[3], Statement.SUCCESS_NO_INFO);

        // Get first result set
        ResultSet rset = stmt.getResultSet();
        Assert.assertTrue(rset instanceof SelectResults);
        checkSelectMetadata(rset, 3);

        // Leave open and grab the next result set
        Assert.assertTrue(stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT));
        Assert.assertFalse(rset.isClosed());
        ResultSet rset2 = stmt.getResultSet();
        Assert.assertTrue(rset2 instanceof AskResults);
        checkAskMetadata(rset2);

        // Leave open and grab the next result set
        Assert.assertTrue(stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT));
        Assert.assertFalse(rset.isClosed());
        Assert.assertFalse(rset2.isClosed());
        ResultSet rset3 = stmt.getResultSet();
        Assert.assertTrue(rset3 instanceof TripleIteratorResults);
        checkConstructDescribeMetadata(rset3);

        // Grab next result set closing all previous
        Assert.assertTrue(stmt.getMoreResults(Statement.CLOSE_ALL_RESULTS));
        Assert.assertTrue(rset.isClosed());
        Assert.assertTrue(rset2.isClosed());
        Assert.assertTrue(rset3.isClosed());
        ResultSet rset4 = stmt.getResultSet();
        Assert.assertFalse(rset4.isClosed());
        checkConstructDescribeMetadata(rset4);

        // Closing the statement should clean up any open result sets even if we
        // haven't retrieved them yet or have previously moved past them but
        // left them open
        stmt.close();
        Assert.assertTrue(stmt.isClosed());
        Assert.assertTrue(rset.isClosed());
        Assert.assertTrue(rset2.isClosed());

        // Clean up
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_08() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        // Create a a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        stmt.addBatch("ASK WHERE { }");
        stmt.addBatch("CONSTRUCT WHERE { ?s ?p ?o }");
        stmt.addBatch("DESCRIBE <http://example>");

        // Clear the batch
        stmt.clearBatch();

        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(0, batchResults.length);

        stmt.close();
        conn.close();
    }

    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_statement_batch_09() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        // Create and execute a batch
        stmt.addBatch("SELECT * WHERE { ?s ?p ?o }");
        stmt.addBatch("ASK WHERE { }");
        stmt.addBatch("CONSTRUCT WHERE { ?s ?p ?o }");
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(3, batchResults.length);
        Assert.assertEquals(batchResults[0], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[1], Statement.SUCCESS_NO_INFO);
        Assert.assertEquals(batchResults[2], Statement.SUCCESS_NO_INFO);

        // Get first result set
        ResultSet rset = stmt.getResultSet();
        Assert.assertTrue(rset instanceof SelectResults);
        checkSelectMetadata(rset, 3);

        try {
            // Not a valid option for what to do with previous results
            stmt.getMoreResults(-1);
        } finally {
            stmt.close();
            conn.close();
        }
    }
    
    /**
     * Runs a batch of operations and checks the results results
     * 
     * @throws SQLException
     */
    @Test
    public void connection_statement_batch_10() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt = conn.createStatement();

        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(0, batchResults.length);
        Assert.assertFalse(stmt.getMoreResults());

        stmt.close();
        conn.close();
    }
    
    /**
     * Tests using batches with prepared statements
     * @throws SQLException
     * @throws MalformedURLException 
     */
    @Test
    public void connection_prepared_statement_batch_01() throws SQLException, MalformedURLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");
        
        for (int i = 1; i <= 5; i++) {
            stmt.setURL(1, new URL("http://example/" + i));
            stmt.addBatch();
        }
        
        int[] batchResults = stmt.executeBatch();
        Assert.assertEquals(5, batchResults.length);
        
        // Expect all to be SELECT results
        ResultSet rset = stmt.getResultSet();
        checkSelectMetadata(rset, 2);
        while (stmt.getMoreResults()) {
            rset = stmt.getResultSet();
            checkSelectMetadata(rset, 2);
        }
        
        stmt.close();
        conn.close();
    }

    /**
     * Tests pre-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_pre_processors_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Adding a null pre-processor has no effect
        conn.addPreProcessor(null);
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Similarly so does removing a null pre-processor
        conn.removePreProcessor(null);
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Inserting a null pre-processor also has no effect
        conn.insertPreProcessor(0, null);
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        conn.close();
    }

    /**
     * Tests pre-processor management operations
     * 
     * @throws SQLException
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void connection_pre_processors_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        Echo echo = new Echo();

        // Inserting at zero index should be safe
        conn.insertPreProcessor(0, echo);
        Assert.assertTrue(conn.getPreProcessors().hasNext());

        // Inserting at some random index will cause an error
        try {
            conn.insertPreProcessor(50, echo);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests pre-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_pre_processors_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Add a pre-processor
        Echo echo = new Echo();
        conn.addPreProcessor(echo);
        Assert.assertTrue(conn.getPreProcessors().hasNext());

        // Remove it
        conn.removePreProcessor(echo);
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        conn.close();
    }

    /**
     * Tests pre-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_pre_processors_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Add a pre-processor
        Echo echo = new Echo();
        conn.addPreProcessor(echo);
        Assert.assertTrue(conn.getPreProcessors().hasNext());

        // Remove it
        conn.removePreProcessor(0);
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        conn.close();
    }

    /**
     * Tests pre-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_pre_processors_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Add a pre-processor
        Echo echo = new Echo();
        conn.addPreProcessor(echo);
        Assert.assertTrue(conn.getPreProcessors().hasNext());

        // Remove all
        conn.clearPreProcessors();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        conn.close();
    }

    /**
     * Tests pre-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_pre_processors_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Add a pre-processor
        Echo echo = new Echo();
        conn.addPreProcessor(echo);
        Assert.assertTrue(conn.getPreProcessors().hasNext());

        // Apply the pre-processor
        String input = "SELECT * WHERE { ?s ?p ?o }";
        String output = conn.applyPreProcessors(input);
        Assert.assertEquals(input, output);

        conn.close();
    }

    /**
     * Tests pre-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_pre_processors_07() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Add a pre-processor
        Echo echo = new Echo();
        conn.addPreProcessor(echo);
        Assert.assertTrue(conn.getPreProcessors().hasNext());

        // Apply the pre-processor
        Query input = QueryFactory.create("SELECT * WHERE { ?s ?p ?o }");
        Query output = conn.applyPreProcessors(input);
        Assert.assertEquals(input, output);

        conn.close();
    }

    /**
     * Tests pre-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_pre_processors_08() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPreProcessors().hasNext());

        // Add a pre-processor
        Echo echo = new Echo();
        conn.addPreProcessor(echo);
        Assert.assertTrue(conn.getPreProcessors().hasNext());

        // Apply the pre-processor
        UpdateRequest input = UpdateFactory.create("DELETE WHERE { ?s ?p ?o }");
        UpdateRequest output = conn.applyPreProcessors(input);
        Assert.assertEquals(input, output);

        conn.close();
    }
    
    /**
     * Tests post-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_post_processors_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        // Adding a null post-processor has no effect
        conn.addPostProcessor(null);
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        // Similarly so does removing a null post-processor
        conn.removePostProcessor(null);
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        // Inserting a null post-processor also has no effect
        conn.insertPostProcessor(0, null);
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        conn.close();
    }

    /**
     * Tests post-processor management operations
     * 
     * @throws SQLException
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void connection_post_processors_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        ResultsEcho echo = new ResultsEcho();

        // Inserting at zero index should be safe
        conn.insertPostProcessor(0, echo);
        Assert.assertTrue(conn.getPostProcessors().hasNext());

        // Inserting at some random index will cause an error
        try {
            conn.insertPostProcessor(50, echo);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests post-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_post_processors_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        // Add a post-processor
        ResultsEcho echo = new ResultsEcho();
        conn.addPostProcessor(echo);
        Assert.assertTrue(conn.getPostProcessors().hasNext());

        // Remove it
        conn.removePostProcessor(echo);
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        conn.close();
    }

    /**
     * Tests post-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_post_processors_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        // Add a post-processor
        ResultsEcho echo = new ResultsEcho();
        conn.addPostProcessor(echo);
        Assert.assertTrue(conn.getPostProcessors().hasNext());

        // Remove it
        conn.removePostProcessor(0);
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        conn.close();
    }

    /**
     * Tests post-processor management operations
     * 
     * @throws SQLException
     */
    @Test
    public void connection_post_processors_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        // Add a post-processor
        ResultsEcho echo = new ResultsEcho();
        conn.addPostProcessor(echo);
        Assert.assertTrue(conn.getPostProcessors().hasNext());

        // Remove all
        conn.clearPostProcessors();
        Assert.assertFalse(conn.getPostProcessors().hasNext());

        conn.close();
    }

    /**
     * Tests error cases trying to set invalid options
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_option_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            // Can't change catalog
            conn.setCatalog("test");
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases trying to set invalid options
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_bad_option_02() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            // Invalid holdability setting
            conn.setHoldability(-1);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases trying to set invalid options
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_bad_option_03() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            // Invalid transaction isolation setting
            conn.setTransactionIsolation(-1);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases around savepoints which are unsupported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_savepoints_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.setSavepoint();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases around savepoints which are unsupported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_savepoints_02() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.setSavepoint("test");
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases around savepoints which are unsupported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_savepoints_03() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.rollback(null);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases around savepoints which are unsupported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_savepoints_04() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.releaseSavepoint(null);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases around type maps which are unsupported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_type_map_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.setTypeMap(new HashMap<String, Class<?>>());
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases around type maps which are unsupported
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_type_map_02() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.getTypeMap();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported call functionality
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_call_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.prepareCall("test");
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported call functionality
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_call_02() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.prepareCall("test", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported call functionality
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_call_03() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.prepareCall("test", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported native sql functionality
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_native_sql_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.nativeSQL("test");
        } finally {
            conn.close();
        }
    }

    /**
     * Tests usage of client info
     * 
     * @throws SQLException
     */
    @Test
    public void connection_client_info_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        // Check initially empty
        Properties ps = conn.getClientInfo();
        Assert.assertNotNull(ps);
        Assert.assertEquals(0, ps.size());

        // Add a value and check it
        conn.setClientInfo("key", "value");
        ps = conn.getClientInfo();
        Assert.assertNotNull(ps);
        Assert.assertEquals(1, ps.size());
        Assert.assertEquals("value", conn.getClientInfo("key"));

        // Replace values with a new set and check
        Properties props = new Properties();
        props.put("a", 1);
        props.put("b", 2);
        conn.setClientInfo(props);
        ps = conn.getClientInfo();
        Assert.assertNotNull(ps);
        Assert.assertEquals(2, ps.size());

        conn.close();
    }

    /**
     * Tests usage of client info
     * 
     * @throws SQLException
     */
    @Test
    public void connection_client_info_02() throws SQLException {
        JenaConnection conn = this.getConnection();

        // Add and retrieve a key
        conn.setClientInfo("key", "value");
        String value = conn.getClientInfo("key");
        Assert.assertEquals("value", value);

        // Non-existent key should give null
        Assert.assertNull(conn.getClientInfo("test"));

        conn.close();
    }

    /**
     * Check catalog retrieval
     * 
     * @throws SQLException
     */
    @Test
    public void connection_catalog_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        Assert.assertNotNull(conn.getCatalog());

        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void connection_warnings_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        Assert.assertNull(conn.getWarnings());

        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void connection_warnings_02() throws SQLException {
        JenaConnection conn = this.getConnection();

        Assert.assertNull(conn.getWarnings());
        conn.setWarning("Test");
        Assert.assertNotNull(conn.getWarnings());
        conn.clearWarnings();
        Assert.assertNull(conn.getWarnings());

        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void connection_warnings_03() throws SQLException {
        JenaConnection conn = this.getConnection();

        Assert.assertNull(conn.getWarnings());
        conn.setWarning("Test", new Exception());
        Assert.assertNotNull(conn.getWarnings());
        conn.clearWarnings();
        Assert.assertNull(conn.getWarnings());

        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void connection_warnings_04() throws SQLException {
        JenaConnection conn = this.getConnection();

        Assert.assertNull(conn.getWarnings());
        conn.setWarning(new SQLWarning());
        Assert.assertNotNull(conn.getWarnings());
        conn.clearWarnings();
        Assert.assertNull(conn.getWarnings());

        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void connection_warnings_05() throws SQLException {
        JenaConnection conn = this.getConnection();

        Assert.assertNull(conn.getWarnings());
        conn.setWarning("A");
        Assert.assertNotNull(conn.getWarnings());
        conn.setWarning("B");
        Assert.assertNotNull(conn.getWarnings());
        Assert.assertNotNull(conn.getWarnings().getNextWarning());
        conn.clearWarnings();
        Assert.assertNull(conn.getWarnings());

        conn.close();
    }

    /**
     * Tests error cases for unsupported wrapper features
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_wrapper_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.isWrapperFor(JenaConnection.class);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported wrapper features
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_wrapper_02() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.unwrap(JenaConnection.class);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported create operations
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_create_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.createArrayOf("test", new Object[0]);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported create operations
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_create_02() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.createBlob();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported create operations
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_create_03() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.createClob();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported create operations
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_create_04() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.createNClob();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported create operations
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_create_05() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.createSQLXML();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported create operations
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void connection_bad_create_06() throws SQLException {
        JenaConnection conn = this.getConnection();

        try {
            conn.createStruct("test", new Object[0]);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests connection validity
     * 
     * @throws SQLException
     */
    @Test
    public void connection_validity_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        Assert.assertTrue(conn.isValid(0));
        conn.close();
        Assert.assertFalse(conn.isValid(0));
    }

    /**
     * Tests read only settings
     * 
     * @throws SQLException
     */
    @Test
    public void connection_read_only_01() throws SQLException {
        JenaConnection conn = this.getConnection();

        Assert.assertFalse(conn.isReadOnly());
        conn.setReadOnly(true);
        Assert.assertTrue(conn.isReadOnly());
        conn.close();
    }

    /**
     * Tests read only settings
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void connection_read_only_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        conn.close();

        // Error to set on a closed connection
        conn.setReadOnly(true);
    }
}
