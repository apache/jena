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

package org.apache.jena.jdbc.statements;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.Calendar;

import org.apache.jena.iri.IRIFactory;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;

/**
 * Tests for statement, note many tests are included at a higher level in the
 * {@link AbstractJenaStatementTests} and this includes tests only for statement
 * specific stuff that has to be tested within this package
 * 
 */
public abstract class AbstractJenaStatementTests {

    /**
     * Method that derived classes must implement to provide a connection to an
     * empty dataset for testing
     * 
     * @return Connection
     * @throws SQLException
     */
    protected abstract JenaConnection getConnection() throws SQLException;

    /**
     * Test error case when trying to create statement with null connection
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_creation_01() throws SQLException {
        new DatasetStatement(null);
    }

    /**
     * Tests error cases for unsupported wrapper features
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void statement_bad_wrapper_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.isWrapperFor(JenaStatement.class);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for unsupported wrapper features
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void statement_bad_wrapper_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.unwrap(JenaStatement.class);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error case for unsupported cancel feature
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void statement_bad_cancel_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.cancel();
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void statement_warnings_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertNull(stmt.getWarnings());

        stmt.close();
        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void statement_warnings_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaStatement stmt = (JenaStatement) conn.createStatement();

        Assert.assertNull(stmt.getWarnings());
        stmt.setWarning("Test");
        Assert.assertNotNull(stmt.getWarnings());
        stmt.clearWarnings();
        Assert.assertNull(stmt.getWarnings());

        stmt.close();
        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void statement_warnings_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaStatement stmt = (JenaStatement) conn.createStatement();

        Assert.assertNull(stmt.getWarnings());
        stmt.setWarning("Test", new Exception());
        Assert.assertNotNull(stmt.getWarnings());
        stmt.clearWarnings();
        Assert.assertNull(stmt.getWarnings());

        stmt.close();
        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void statement_warnings_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaStatement stmt = (JenaStatement) conn.createStatement();

        Assert.assertNull(stmt.getWarnings());
        stmt.setWarning(new SQLWarning());
        Assert.assertNotNull(stmt.getWarnings());
        stmt.clearWarnings();
        Assert.assertNull(stmt.getWarnings());

        stmt.close();
        conn.close();
    }

    /**
     * Check warnings usage
     * 
     * @throws SQLException
     */
    @Test
    public void statement_warnings_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaStatement stmt = (JenaStatement) conn.createStatement();

        Assert.assertNull(stmt.getWarnings());
        stmt.setWarning("A");
        Assert.assertNotNull(stmt.getWarnings());
        stmt.setWarning("B");
        Assert.assertNotNull(stmt.getWarnings());
        Assert.assertNotNull(stmt.getWarnings().getNextWarning());
        stmt.clearWarnings();
        Assert.assertNull(stmt.getWarnings());

        stmt.close();
        conn.close();
    }

    /**
     * Tests for fetch direction settings
     * 
     * @throws SQLException
     */
    @Test
    public void statement_fetch_direction_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertEquals(ResultSet.FETCH_FORWARD, stmt.getFetchDirection());
        conn.close();
    }

    /**
     * Tests for fetch direction settings
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void statement_fetch_direction_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            // Only FETCH_FORWARD is supported
            stmt.setFetchDirection(ResultSet.FETCH_REVERSE);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.execute("SELECT * WHERE { ?s ?p ?o }");
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.execute("SELECT * WHERE { ?s ?p ?o }", 0);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.execute("SELECT * WHERE { ?s ?p ?o }", new int[0]);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.execute("SELECT * WHERE { ?s ?p ?o }", new String[0]);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.executeQuery("SELECT * WHERE { ?s ?p ?o }");
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_07() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.executeUpdate("DELETE WHERE { ?s ?p ?o }");
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_08() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.executeUpdate("DELETE WHERE { ?s ?p ?o }", 0);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_09() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.executeUpdate("DELETE WHERE { ?s ?p ?o }", new int[0]);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_10() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.executeUpdate("DELETE WHERE { ?s ?p ?o }", new String[0]);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute things on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_execute_11() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.close();

        try {
            stmt.executeBatch();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to access results on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_results_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        stmt.close();

        try {
            stmt.getResultSet();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to access results on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_results_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        stmt.close();

        try {
            stmt.getMoreResults();
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to access results on closed statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_closed_results_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        stmt.close();

        try {
            stmt.getMoreResults(Statement.CLOSE_ALL_RESULTS);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.execute("SELECT * WHERE {");
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.execute("SELECT * WHERE {", 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.execute("SELECT * WHERE {", new int[0]);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.execute("SELECT * WHERE {", new String[0]);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.executeQuery("SELECT * WHERE {");
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.executeUpdate("SELECT * WHERE {");
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_07() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.executeUpdate("SELECT * WHERE {", 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_08() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.executeUpdate("SELECT * WHERE {", new int[0]);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute invalid SPARQL
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statement_bad_execute_09() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.executeUpdate("SELECT * WHERE {", new String[0]);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute updates on read-only connections
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statememt_readonly_execute_update_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        conn.setReadOnly(true);

        try {
            stmt.executeUpdate("DELETE WHERE { ?s ?p ?o }");
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute updates on read-only connections
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statememt_readonly_execute_update_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        conn.setReadOnly(true);

        try {
            stmt.executeUpdate("DELETE WHERE { ?s ?p ?o }", 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute updates on read-only connections
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statememt_readonly_execute_update_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        conn.setReadOnly(true);

        try {
            stmt.executeUpdate("DELETE WHERE { ?s ?p ?o }", new int[0]);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for trying to execute updates on read-only connections
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void statememt_readonly_execute_update_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();
        conn.setReadOnly(true);

        try {
            stmt.executeUpdate("DELETE WHERE { ?s ?p ?o }", new String[0]);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests manipulating some settings on a statement
     * 
     * @throws SQLException
     */
    @Test
    public void statement_settings_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertEquals(0, stmt.getMaxRows());
        stmt.setMaxRows(10);
        Assert.assertEquals(10, stmt.getMaxRows());
        stmt.setMaxRows(-1);
        Assert.assertEquals(0, stmt.getMaxRows());

        stmt.close();
        conn.close();
    }

    /**
     * Tests manipulating some settings on a statement
     * 
     * @throws SQLException
     */
    @Test
    public void statement_settings_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertEquals(0, stmt.getMaxFieldSize());

        // Max field size is ignored
        stmt.setMaxFieldSize(100);
        Assert.assertEquals(0, stmt.getMaxFieldSize());

        stmt.close();
        conn.close();
    }

    /**
     * Tests manipulating some settings on a statement
     * 
     * @throws SQLException
     */
    @Test
    public void statement_settings_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertEquals(0, stmt.getFetchSize());
        stmt.setFetchSize(10);
        Assert.assertEquals(10, stmt.getFetchSize());

        stmt.close();
        conn.close();
    }

    /**
     * Tests manipulating some settings on a statement
     * 
     * @throws SQLException
     */
    @Test
    public void statement_settings_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertTrue(stmt.isPoolable());

        // Poolable setting is ignored
        stmt.setPoolable(false);
        Assert.assertTrue(stmt.isPoolable());

        stmt.close();
        conn.close();
    }

    /**
     * Tests manipulating some settings on a statement
     * 
     * @throws SQLException
     */
    @Test
    public void statement_settings_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertEquals(0, stmt.getQueryTimeout());
        stmt.setQueryTimeout(10);
        Assert.assertEquals(10, stmt.getQueryTimeout());
        stmt.setQueryTimeout(-1);
        Assert.assertEquals(0, stmt.getQueryTimeout());

        stmt.close();
        conn.close();
    }

    /**
     * Tests manipulating some settings on a statement
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void statement_settings_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertEquals(ResultSet.FETCH_FORWARD, stmt.getFetchDirection());
        stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        Assert.assertEquals(ResultSet.FETCH_FORWARD, stmt.getFetchDirection());
        try {
            stmt.setFetchDirection(ResultSet.FETCH_REVERSE);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests manipulating some settings on a statement
     * 
     * @throws SQLException
     */
    @Test
    public void statement_settings_07() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        stmt.setEscapeProcessing(true);

        stmt.close();
        conn.close();
    }

    /**
     * Tests manipulating some settings on a statement
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void statement_settings_08() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        try {
            stmt.setCursorName("test");
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests getting metadata from the statement
     * 
     * @throws SQLException
     */
    @Test
    public void statement_metadata_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement();

        Assert.assertEquals(ResultSet.TYPE_FORWARD_ONLY, stmt.getResultSetType());
        Assert.assertEquals(conn.getHoldability(), stmt.getResultSetHoldability());
        Assert.assertEquals(ResultSet.CONCUR_READ_ONLY, stmt.getResultSetConcurrency());
    }

    /**
     * Tests getting metadata from the statement
     * 
     * @throws SQLException
     */
    @Test
    public void statement_metadata_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
                ResultSet.HOLD_CURSORS_OVER_COMMIT);

        Assert.assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, stmt.getResultSetType());
        Assert.assertEquals(ResultSet.HOLD_CURSORS_OVER_COMMIT, stmt.getResultSetHoldability());
        Assert.assertEquals(ResultSet.CONCUR_READ_ONLY, stmt.getResultSetConcurrency());
    }

    /**
     * Tests around prepared statement execution
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_execution_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }");

        Assert.assertTrue(stmt.execute());
        stmt.close();
        conn.close();
    }

    /**
     * Tests around prepared statement execution
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_execution_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }");

        Assert.assertNotNull(stmt.executeQuery());
        stmt.close();
        conn.close();
    }

    /**
     * Tests around prepared statement execution
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_execution_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("DELETE WHERE { ?s ?p ?o }");

        Assert.assertEquals(0, stmt.executeUpdate());
        stmt.close();
        conn.close();
    }

    /**
     * Tests around prepared statement metadata
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_metadata_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ?s ?p ?o }");

        Assert.assertNull(stmt.getMetaData());
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setArray(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setAsciiStream(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setAsciiStream(1, null, 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setAsciiStream(1, null, 0l);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setBinaryStream(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setBinaryStream(1, null, 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_07() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setBinaryStream(1, null, 0l);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_08() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setBlob(1, (Blob) null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_09() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setBlob(1, (InputStream) null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_10() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setBlob(1, (InputStream) null, 0l);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_11() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setBytes(1, new byte[0]);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_12() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setCharacterStream(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_13() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setCharacterStream(1, null, 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_14() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setCharacterStream(1, null, 0l);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_15() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setClob(1, (Clob) null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_16() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setClob(1, (Reader) null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_17() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setClob(1, (Reader) null, 0l);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_18() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setDate(1, null, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_19() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setNCharacterStream(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_20() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setNCharacterStream(1, null, 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_21() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setNCharacterStream(1, null, 0l);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_22() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setNClob(1, (NClob) null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_23() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setNClob(1, (Reader) null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_24() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setNClob(1, (Reader) null, 0l);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_25() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setNull(1, 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_26() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setNull(1, 0, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_27() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // 4 argument form is unsupported, others are supported
            stmt.setObject(1, null, 0, 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_28() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setRef(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_29() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setRowId(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_30() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setSQLXML(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_31() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setTime(1, null, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_32() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setTimestamp(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_33() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setTimestamp(1, null, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests for unsupported setters on prepared statements
     * 
     * @throws SQLException
     */
    @SuppressWarnings("deprecation")
    @Test(expected = SQLFeatureNotSupportedException.class)
    public void prepared_statement_unsupported_setters_34() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            stmt.setUnicodeStream(1, null, 0);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Should error since parameters use a one based index
            stmt.setBoolean(0, true);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Should error since parameters use a one based index
            stmt.setBoolean(2, true);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // No RDF equivalent of the given SQL Type
            stmt.setObject(1, null, Types.BLOB);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // No RDF equivalent of the given SQL Type
            stmt.setObject(1, new Object(), Types.BLOB);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // No RDF equivalent for unknown SQL Type
            stmt.setObject(1, new Object(), Integer.MAX_VALUE);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.BIGINT);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_07() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.BOOLEAN);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_08() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.DATE);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_09() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.DECIMAL);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_10() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.DOUBLE);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_11() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.FLOAT);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_12() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.INTEGER);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_13() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.SMALLINT);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_14() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.TINYINT);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests error cases for setters on prepared statements
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_bad_setters_15() throws SQLException {
        JenaConnection conn = this.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * WHERE { ? ?p ?o }");

        try {
            // Invalid cast
            stmt.setObject(1, new Object(), Types.TIME);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_01() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, "value");
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("\"value\""));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_02() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_03() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123l);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_04() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (byte) 123);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDbyte.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_05() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, BigDecimal.valueOf(1234, 1));
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_06() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (short) 123);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_07() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 12.3f);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("12.3"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDfloat.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_08() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 12.3d);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("12.3"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_09() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, true);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("true"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_10() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, new Date(0));
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDdate.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_11() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, new Time(0));
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDtime.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     * @throws MalformedURLException
     */
    @Test
    public void prepared_statement_setters_12() throws SQLException, MalformedURLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, new URL("http://example.org"));
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<http://example.org>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    @Test
    public void prepared_statement_setters_13() throws SQLException, MalformedURLException, URISyntaxException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, new URI("http://example.org"));
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<http://example.org>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_14() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, IRIFactory.iriImplementation().create("http://example.org"));
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<http://example.org>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_15() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, IRIFactory.iriImplementation().create("http://example.org"), Types.NVARCHAR);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("\"http://example.org\""));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_16() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, IRIFactory.iriImplementation().create("http://example.org"), Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<http://example.org>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_17() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 12.3f, Types.DOUBLE);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("12.3"));
        Assert.assertFalse(pss.toString().contains(XSDDatatype.XSDfloat.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_18() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 12.3d, Types.DOUBLE);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("12.3"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_19() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123l, Types.BIGINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_20() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123, Types.BIGINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_21() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (short) 123, Types.BIGINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_22() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (byte) 123, Types.BIGINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_23() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, Quad.defaultGraphIRI);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<urn:x-arq:DefaultGraph>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_24() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, ModelFactory.createDefaultModel().createResource("urn:x-arq:DefaultGraph"));
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<urn:x-arq:DefaultGraph>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_25() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Calendar c = Calendar.getInstance();
        stmt.setObject(1, c);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(c.get(Calendar.YEAR))));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_setters_26() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        // Setting random types (e.g. this test class) is not supported
        try {
            stmt.setObject(1, this);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void prepared_statement_setters_27() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        try {
            // Setting null is illegal
            stmt.setObject(1, null);
        } finally {
            stmt.close();
            conn.close();
        }
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_28() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setBigDecimal(1, BigDecimal.valueOf(1234, 1));
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_29() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setBoolean(1, true);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("true"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_30() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setByte(1, (byte) 123);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDbyte.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_31() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setFloat(1, 12.3f);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("12.3"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDfloat.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_32() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setDouble(1, 12.3d);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("12.3"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_33() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setInt(1, 123);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_34() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setLong(1, 123l);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_35() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Date dt = new Date(Calendar.getInstance().getTimeInMillis());
        stmt.setDate(1, dt);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(Calendar.getInstance().get(Calendar.YEAR))));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_36() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactoryExtra.intToNode(123l), Types.BIGINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_37() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, "123", Types.BIGINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_38() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, true, Types.BOOLEAN);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("true"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_39() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactory.createLiteral("true", XSDDatatype.XSDboolean), Types.BOOLEAN);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("true"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_40() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, "true", Types.BOOLEAN);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("true"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_41() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Calendar c = Calendar.getInstance();
        stmt.setObject(1, c, Types.DATE);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(c.get(Calendar.YEAR))));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_42() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Calendar c = Calendar.getInstance();
        stmt.setObject(1, NodeFactoryExtra.dateTimeToNode(c), Types.DATE);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(c.get(Calendar.YEAR))));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_43() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        @SuppressWarnings("deprecation")
        Time t = new Time(0, 0, 0);
        stmt.setObject(1, t, Types.DATE);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("00:00:00"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_44() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Date dt = new Date(Calendar.getInstance().getTimeInMillis());
        stmt.setObject(1, dt, Types.DATE);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(Calendar.getInstance().get(Calendar.YEAR))));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_45() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, BigDecimal.valueOf(1234, 1), Types.DECIMAL);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_46() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactory.createLiteral(BigDecimal.valueOf(1234, 1).toPlainString(), XSDDatatype.XSDdecimal),
                Types.DECIMAL);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_47() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactoryExtra.doubleToNode(123.4d), Types.DOUBLE);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_48() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123.4f, Types.FLOAT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDfloat.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_49() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactoryExtra.floatToNode(123.4f), Types.FLOAT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDfloat.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_50() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123, Types.INTEGER);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_51() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (short) 123, Types.INTEGER);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_52() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (byte) 123, Types.INTEGER);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_53() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactoryExtra.intToNode(123), Types.INTEGER);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_54() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (short) 123, Types.SMALLINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDshort.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_55() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (byte) 123, Types.SMALLINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDshort.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_56() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactory.createLiteral("123", XSDDatatype.XSDshort), Types.SMALLINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDshort.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_57() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Calendar c = Calendar.getInstance();
        stmt.setObject(1, c, Types.TIME);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(c.get(Calendar.HOUR_OF_DAY))));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_58() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Calendar c = Calendar.getInstance();
        stmt.setObject(1, NodeFactoryExtra.dateTimeToNode(c), Types.TIME);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(c.get(Calendar.HOUR_OF_DAY))));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_59() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        @SuppressWarnings("deprecation")
        Time t = new Time(0, 0, 0);
        stmt.setObject(1, t, Types.TIME);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("00:00:00"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_60() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Date dt = new Date(Calendar.getInstance().getTimeInMillis());
        stmt.setObject(1, dt, Types.TIME);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_61() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (byte) 123, Types.TINYINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDbyte.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_62() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactory.createLiteral("123", XSDDatatype.XSDbyte), Types.TINYINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDbyte.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_63() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, "123", Types.SMALLINT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDshort.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_64() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setShort(1, (short) 123);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDshort.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_65() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        @SuppressWarnings("deprecation")
        Time t = new Time(0, 0, 0);
        stmt.setTime(1, t);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("00:00:00"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_66() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, NodeFactory.createURI("http://example"), Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<http://example>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_67() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, ModelFactory.createDefaultModel().createResource("http://example"), Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<http://example>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_68() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, "test", Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("test"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_69() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, BigDecimal.valueOf(1234, 1), Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_70() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123l, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_71() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_72() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123.4d, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_73() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, 123.4f, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123.4"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDfloat.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_74() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (short) 123, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDshort.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_75() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, (byte) 123, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("123"));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDbyte.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_76() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, true, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("true"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     * @throws MalformedURLException
     */
    @Test
    public void prepared_statement_setters_77() throws SQLException, MalformedURLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, new URL("http://example"), Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<http://example>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    @Test
    public void prepared_statement_setters_78() throws SQLException, MalformedURLException, URISyntaxException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, new URI("http://example"), Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains("<http://example>"));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     * @throws MalformedURLException
     */
    @Test
    public void prepared_statement_setters_79() throws SQLException, MalformedURLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Calendar c = Calendar.getInstance();
        stmt.setObject(1, c, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(c.get(Calendar.YEAR))));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDdateTime.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     * @throws MalformedURLException
     */
    @Test
    public void prepared_statement_setters_80() throws SQLException, MalformedURLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Calendar c = Calendar.getInstance();
        Date dt = new Date(c.getTimeInMillis());
        stmt.setObject(1, dt, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(c.get(Calendar.YEAR))));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDdateTime.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     * @throws MalformedURLException
     */
    @Test
    public void prepared_statement_setters_81() throws SQLException, MalformedURLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        Calendar c = Calendar.getInstance();
        Time t = new Time(c.getTimeInMillis());
        stmt.setObject(1, t, Types.JAVA_OBJECT);
        ParameterizedSparqlString pss = stmt.getParameterizedString();
        Assert.assertTrue(pss.toString().contains(Integer.toString(c.get(Calendar.HOUR_OF_DAY))));
        Assert.assertTrue(pss.toString().contains(XSDDatatype.XSDtime.getURI()));

        stmt.close();
        conn.close();
    }

    /**
     * Tests that the various set methods of {@link JenaPreparedStatement}
     * function correctly
     * 
     * @throws SQLException
     */
    @Test
    public void prepared_statement_setters_82() throws SQLException {
        JenaConnection conn = this.getConnection();
        JenaPreparedStatement stmt = (JenaPreparedStatement) conn.prepareStatement("SELECT * WHERE { ?s ?p ? }");

        stmt.setObject(1, new Object(), Types.JAVA_OBJECT);

        stmt.close();
        conn.close();
    }
}
