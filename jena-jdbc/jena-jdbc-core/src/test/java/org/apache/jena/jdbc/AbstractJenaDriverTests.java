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

package org.apache.jena.jdbc;

import java.io.File ;
import java.io.FileWriter ;
import java.io.IOException ;
import java.sql.Connection ;
import java.sql.SQLException ;
import java.util.Iterator ;
import java.util.Properties ;

import org.apache.jena.jdbc.connections.JenaConnection ;
import org.apache.jena.jdbc.postprocessing.ResultsEcho ;
import org.apache.jena.jdbc.postprocessing.ResultsPostProcessor ;
import org.apache.jena.jdbc.preprocessing.CommandPreProcessor ;
import org.apache.jena.jdbc.preprocessing.Echo ;
import org.junit.Assert ;
import org.junit.Assume ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;

/**
 * Abstract tests for {@link JenaDriver} implementations
 * 
 */
public abstract class AbstractJenaDriverTests {

    static {
        ARQ.init();
    }

    /**
     * Method which derived classes must implement to return an instance of
     * their driver implementation.
     */
    protected abstract JenaDriver getDriver() throws SQLException;

    /**
     * Method which derived classes must implement to provide a connection URL
     * for an empty database.
     * <p>
     * {@code null} may be returned if this is not supported by the driver
     * implementation. If this is the case then relevant tests will be skipped.
     * </p>
     * 
     * @return Connection URL or null
     * @throws SQLException 
     */
    protected abstract String getConnectionUrl() throws SQLException;

    /**
     * Method which derives classes must implement to provide a connection URL
     * which is known to result in a creation error in the form of a
     * {@link SQLException} when the
     * {@link JenaDriver#connect(String, java.util.Properties)} is called with
     * it.
     * <p>
     * {@code null} may be returned if there are no invalid connection URLs for
     * the driver (this is considered highly unlikely)
     * </p>
     * 
     * @return Bad Connection URL or null
     */
    protected abstract String getBadConnectionUrl();

    /**
     * Test that an implementation will accept its own URLs
     * 
     * @throws SQLException
     */
    @Test
    public void driver_accepts_01() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        JenaDriver driver = this.getDriver();

        Assert.assertTrue(driver.acceptsURL(url));
    }

    /**
     * Tests that an implementation will not accept an arbitrary URL
     * 
     * @throws SQLException
     */
    @Test
    public void driver_accepts_02() throws SQLException {
        String url = "jdbc:unknown:http://example.org";
        JenaDriver driver = this.getDriver();

        Assert.assertFalse(driver.acceptsURL(url));
    }

    /**
     * Tests using a driver to create a connection with its own URLs
     * 
     * @throws SQLException
     */
    @Test
    public void driver_connect_01() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        JenaDriver driver = this.getDriver();

        Connection conn = driver.connect(url, null);
        Assert.assertFalse(conn.isClosed());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard JDBC compatibility parameter
     * 
     * @throws SQLException
     */
    @Test
    public void driver_connect_02() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        url = url + "&" + JenaDriver.PARAM_JDBC_COMPATIBILITY + "=" + JdbcCompatibility.LOW;
        JenaDriver driver = this.getDriver();

        JenaConnection conn = (JenaConnection) driver.connect(url, null);
        Assert.assertFalse(conn.isClosed());
        Assert.assertEquals(JdbcCompatibility.LOW, conn.getJdbcCompatibilityLevel());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard JDBC compatibility parameter
     * 
     * @throws SQLException
     */
    @Test
    public void driver_connect_03() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        url = url + "&" + JenaDriver.PARAM_JDBC_COMPATIBILITY + "=" + Integer.MIN_VALUE;
        JenaDriver driver = this.getDriver();

        JenaConnection conn = (JenaConnection) driver.connect(url, null);
        Assert.assertFalse(conn.isClosed());
        Assert.assertEquals(JdbcCompatibility.LOW, conn.getJdbcCompatibilityLevel());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard JDBC compatibility parameter
     * 
     * @throws SQLException
     */
    @Test
    public void driver_connect_04() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        url = url + "&" + JenaDriver.PARAM_JDBC_COMPATIBILITY + "=" + JdbcCompatibility.HIGH;
        JenaDriver driver = this.getDriver();

        JenaConnection conn = (JenaConnection) driver.connect(url, null);
        Assert.assertFalse(conn.isClosed());
        Assert.assertEquals(JdbcCompatibility.HIGH, conn.getJdbcCompatibilityLevel());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard JDBC compatibility parameter
     * 
     * @throws SQLException
     */
    @Test
    public void driver_connect_05() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        url = url + "&" + JenaDriver.PARAM_JDBC_COMPATIBILITY + "=" + Integer.MAX_VALUE;
        JenaDriver driver = this.getDriver();

        JenaConnection conn = (JenaConnection) driver.connect(url, null);
        Assert.assertFalse(conn.isClosed());
        Assert.assertEquals(JdbcCompatibility.HIGH, conn.getJdbcCompatibilityLevel());
        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard pre-processor parameter
     * 
     * @throws SQLException
     */
    @Test
    public void driver_connect_06() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName();
        JenaDriver driver = this.getDriver();

        JenaConnection conn = (JenaConnection) driver.connect(url, null);
        Assert.assertFalse(conn.isClosed());
        Iterator<CommandPreProcessor> iter = conn.getPreProcessors();
        Assert.assertTrue(iter.hasNext());
        iter.next();
        Assert.assertFalse(iter.hasNext());

        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard pre-processor parameter
     * 
     * @throws SQLException
     */
    @Test
    public void driver_connect_07() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        // We can register it twice if we want
        url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName();
        url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName();
        JenaDriver driver = this.getDriver();

        JenaConnection conn = (JenaConnection) driver.connect(url, null);
        Assert.assertFalse(conn.isClosed());
        Iterator<CommandPreProcessor> iter = conn.getPreProcessors();
        Assert.assertTrue(iter.hasNext());
        iter.next();
        Assert.assertTrue(iter.hasNext());
        iter.next();
        Assert.assertFalse(iter.hasNext());

        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard pre-processor and post-processor parameter
     * 
     * @throws SQLException
     */
    @Test
    public void driver_connect_08() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName();
        url = url + "&" + JenaDriver.PARAM_POST_PROCESSOR + "=" + ResultsEcho.class.getCanonicalName();
        JenaDriver driver = this.getDriver();

        JenaConnection conn = (JenaConnection) driver.connect(url, null);
        Assert.assertFalse(conn.isClosed());
        Iterator<CommandPreProcessor> iter = conn.getPreProcessors();
        Assert.assertTrue(iter.hasNext());
        Assert.assertTrue(iter.next() instanceof Echo);
        Assert.assertFalse(iter.hasNext());

        Iterator<ResultsPostProcessor> iter2 = conn.getPostProcessors();
        Assert.assertTrue(iter2.hasNext());
        Assert.assertTrue(iter2.next() instanceof ResultsEcho);
        Assert.assertFalse(iter2.hasNext());

        conn.close();
        Assert.assertTrue(conn.isClosed());
    }

    /**
     * Tests the precedence rules for connection URL parameters
     * 
     * @throws SQLException
     */
    @Test
    public void driver_config_precedence_01() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName() + "&test=url";
        JenaDriver driver = this.getDriver();

        Properties ps = new Properties();
        ps.put("test", "props");

        JenaConnection conn = (JenaConnection) driver.connect(url, ps);
        Iterator<CommandPreProcessor> preProcessors = conn.getPreProcessors();
        Assert.assertTrue(preProcessors.hasNext());
        Echo echo = (Echo) preProcessors.next();

        Properties actual = echo.getProperties();
        Assert.assertEquals("props", actual.getProperty("test"));

        conn.close();
    }

    /**
     * Tests the precedence rules for connection URL parameters
     * 
     * @throws SQLException
     */
    @Test
    public void driver_config_precedence_02() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName() + "&test=url";
        JenaDriver driver = this.getDriver();

        Properties ps = new Properties();

        JenaConnection conn = (JenaConnection) driver.connect(url, ps);
        Iterator<CommandPreProcessor> preProcessors = conn.getPreProcessors();
        Assert.assertTrue(preProcessors.hasNext());
        Echo echo = (Echo) preProcessors.next();

        Properties actual = echo.getProperties();
        Assert.assertEquals("url", actual.getProperty("test"));

        conn.close();
    }

    /**
     * Tests the precedence rules for connection URL parameters
     * 
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void driver_config_precedence_03() throws SQLException, IOException {
        File f = null;
        try {
            f = File.createTempFile("config", ".properties");
            FileWriter writer = new FileWriter(f);
            writer.write("test=external");
            writer.close();

            String url = this.getConnectionUrl();
            Assume.assumeNotNull(url);
            Assume.assumeFalse(url.contains(JenaDriver.PARAM_CONFIG + "="));
            url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName() + "&"
                    + JenaDriver.PARAM_CONFIG + "=" + f.getAbsolutePath();
            JenaDriver driver = this.getDriver();

            Properties ps = new Properties();

            JenaConnection conn = (JenaConnection) driver.connect(url, ps);
            Iterator<CommandPreProcessor> preProcessors = conn.getPreProcessors();
            Assert.assertTrue(preProcessors.hasNext());
            Echo echo = (Echo) preProcessors.next();

            Properties actual = echo.getProperties();
            Assert.assertEquals("external", actual.getProperty("test"));

            conn.close();
        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * Tests the precedence rules for connection URL parameters
     * 
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void driver_config_precedence_04() throws SQLException, IOException {
        File f = null;
        try {
            f = File.createTempFile("config", ".properties");
            FileWriter writer = new FileWriter(f);
            writer.write("test=external");
            writer.close();

            String url = this.getConnectionUrl();
            Assume.assumeNotNull(url);
            Assume.assumeFalse(url.contains(JenaDriver.PARAM_CONFIG + "="));
            url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName() + "&test=url&"
                    + JenaDriver.PARAM_CONFIG + "=" + f.getAbsolutePath();
            JenaDriver driver = this.getDriver();

            Properties ps = new Properties();

            JenaConnection conn = (JenaConnection) driver.connect(url, ps);
            Iterator<CommandPreProcessor> preProcessors = conn.getPreProcessors();
            Assert.assertTrue(preProcessors.hasNext());
            Echo echo = (Echo) preProcessors.next();

            Properties actual = echo.getProperties();
            Assert.assertEquals("url", actual.getProperty("test"));

            conn.close();
        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * Tests the precedence rules for connection URL parameters
     * 
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void driver_config_precedence_05() throws SQLException, IOException {
        File f = null;
        try {
            f = File.createTempFile("config", ".properties");
            FileWriter writer = new FileWriter(f);
            writer.write("test=external");
            writer.close();

            String url = this.getConnectionUrl();
            Assume.assumeNotNull(url);
            Assume.assumeFalse(url.contains(JenaDriver.PARAM_CONFIG + "="));
            url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName() + "&test=url&"
                    + JenaDriver.PARAM_CONFIG + "=" + f.getAbsolutePath();
            JenaDriver driver = this.getDriver();

            Properties ps = new Properties();
            ps.put("test", "props");

            JenaConnection conn = (JenaConnection) driver.connect(url, ps);
            Iterator<CommandPreProcessor> preProcessors = conn.getPreProcessors();
            Assert.assertTrue(preProcessors.hasNext());
            Echo echo = (Echo) preProcessors.next();

            Properties actual = echo.getProperties();
            Assert.assertEquals("props", actual.getProperty("test"));

            conn.close();
        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * Tests the precedence rules for connection URL parameters
     * 
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void driver_config_precedence_06() throws SQLException, IOException {
        File f1 = null, f2 = null;
        try {
            f1 = File.createTempFile("config", ".properties");
            FileWriter writer = new FileWriter(f1);
            writer.write("test=external-url");
            writer.close();
            f2 = File.createTempFile("config", ".properties");
            writer = new FileWriter(f2);
            writer.write("test=external-props");
            writer.close();

            String url = this.getConnectionUrl();
            Assume.assumeNotNull(url);
            Assume.assumeFalse(url.contains(JenaDriver.PARAM_CONFIG + "="));
            url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Echo.class.getCanonicalName() + "&"
                    + JenaDriver.PARAM_CONFIG + "=" + f1.getAbsolutePath();
            JenaDriver driver = this.getDriver();

            Properties ps = new Properties();
            ps.put(JenaDriver.PARAM_CONFIG, f2.getAbsolutePath());

            JenaConnection conn = (JenaConnection) driver.connect(url, ps);
            Iterator<CommandPreProcessor> preProcessors = conn.getPreProcessors();
            Assert.assertTrue(preProcessors.hasNext());
            Echo echo = (Echo) preProcessors.next();

            Properties actual = echo.getProperties();
            Assert.assertEquals("external-props", actual.getProperty("test"));

            conn.close();
        } finally {
            if (f1 != null && f1.exists()) {
                f1.delete();
            }
            if (f2 != null && f2.exists()) {
                f2.delete();
            }
        }
    }

    /**
     * Tests that multiple values for properties are supported by external
     * config when comma separated lists are used
     * 
     * @throws SQLException
     * @throws IOException
     */
    @Test
    public void driver_external_config_01() throws SQLException, IOException {
        File f = null;
        try {
            f = File.createTempFile("config", ".properties");
            FileWriter writer = new FileWriter(f);
            writer.write("pre-processor=" + Echo.class.getCanonicalName() + "," + Echo.class.getCanonicalName());
            writer.close();

            String url = this.getConnectionUrl();
            Assume.assumeNotNull(url);
            Assume.assumeFalse(url.contains(JenaDriver.PARAM_CONFIG + "="));
            url = url + "&" + JenaDriver.PARAM_CONFIG + "=" + f.getAbsolutePath();
            JenaDriver driver = this.getDriver();

            JenaConnection conn = (JenaConnection) driver.connect(url, null);
            Iterator<CommandPreProcessor> preProcessors = conn.getPreProcessors();
            Assert.assertTrue(preProcessors.hasNext());
            preProcessors.next();
            Assert.assertTrue(preProcessors.hasNext());
            preProcessors.next();
            Assert.assertFalse(preProcessors.hasNext());

            conn.close();
        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * Tests using a driver to create a connection with its own URLs which are
     * known to be bad
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void driver_connect_bad_01() throws SQLException {
        String url = this.getBadConnectionUrl();
        Assume.assumeNotNull(url);
        JenaDriver driver = this.getDriver();

        driver.connect(url, null);
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard pre-processor parameter but setting the pre-processor to a value
     * that creates an error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void driver_connect_bad_02() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        // Try to use a class that doesn't exist
        url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=NoSuchClass";

        JenaDriver driver = this.getDriver();
        driver.connect(url, null);
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard pre-processor parameter but setting the pre-processor to a value
     * that creates an error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void driver_connect_bad_03() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        // Try to use a class that exists but isn't a CommandPreProcessor
        url = url + "&" + JenaDriver.PARAM_PRE_PROCESSOR + "=" + Node.class.getCanonicalName();

        JenaDriver driver = this.getDriver();
        driver.connect(url, null);
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard post-processor parameter but setting the post-processor to a
     * value that creates an error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void driver_connect_bad_04() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        // Try to use a class that doesn't exist
        url = url + "&" + JenaDriver.PARAM_POST_PROCESSOR + "=NoSuchClass";

        JenaDriver driver = this.getDriver();
        driver.connect(url, null);
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard post-processor parameter but setting the post-processor to a
     * value that creates an error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void driver_connect_bad_05() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        // Try to use a class that exists but isn't a ResultsPostProcessor
        url = url + "&" + JenaDriver.PARAM_POST_PROCESSOR + "=" + Node.class.getCanonicalName();

        JenaDriver driver = this.getDriver();
        driver.connect(url, null);
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard logging parameter but setting it to a value that creates an
     * error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void driver_connect_bad_06() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        // Try to use a non-existent file/class path resource
        url = url + "&" + JenaDriver.PARAM_LOGGING + "=/nosuch.properties";

        JenaDriver driver = this.getDriver();
        driver.connect(url, null);
    }

    /**
     * Tests using a driver to create a connection with its own URLs plus the
     * standard config parameter but setting it to a value that creates an error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void driver_connect_bad_07() throws SQLException {
        String url = this.getConnectionUrl();
        Assume.assumeNotNull(url);
        // Try to use a non-existent file/class path resource
        url = url + "&" + JenaDriver.PARAM_CONFIG + "=/nosuch.properties";

        JenaDriver driver = this.getDriver();
        driver.connect(url, null);
    }
}
