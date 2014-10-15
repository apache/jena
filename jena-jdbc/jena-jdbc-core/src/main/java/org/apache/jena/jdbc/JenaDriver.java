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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.postprocessing.ResultsPostProcessor;
import org.apache.jena.jdbc.preprocessing.CommandPreProcessor;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Abstract implementation of a Jena JDBC driver
 * </p>
 * <h2>Connection URLs</h2>
 * <p>
 * All Jena JDBC drivers are expected to have connection URLs which start with
 * the following:
 * </p>
 * 
 * <pre>
 * jdbc:jena:foo:
 * </pre>
 * <p>
 * The {@code jdbc:jena:} portion is the common prefix for all Jena JDBC
 * drivers, the {@code foo:} portion is an implementation specific prefix. Each
 * Jena JDBC driver will have a different {@code foo:} prefix chosen such that
 * it appropriately identifies at a glance the type of the underlying RDF
 * database.
 * </p>
 * <h3>Connection URL Parameters</h3>
 * <p>
 * After the prefix a Jena JDBC connection URL consists of a sequence of key
 * value pairs, the characters ampersand ({@code &}), semicolon ({@code ;}) and
 * pipe ({@code |}) are considered to be separators between pairs, the
 * separators are reserved characters and may not be used in values. The key is
 * separated from the value by a equals sign ({@code =}) though unlike the
 * separators this is not a reserved character in values.
 * </p>
 * <p>
 * There is no notion of character escaping in connection parameters so if you
 * need to use any of the reserved characters in your values then you should
 * pass these to the {@link #connect(String, Properties)} method directly in the
 * {@link Properties} object.
 * </p>
 * <h4>Common Parameters</h4>
 * <p>
 * There are some common parameter understood by all Jena JDBC drivers and which
 * apply regardless of driver implementation.
 * </p>
 * <h5>JDBC Compatibility Level</h5>
 * <p>
 * The first of these is the {@code jdbc-compatibility} parameter. To avoid
 * typos when creating URLs programmatically a constant (
 * {@link #PARAM_JDBC_COMPATIBILITY}) is provided which contains the parameter
 * name exactly as the code expects it. This parameter provides an integer value
 * in the range 1-9 which denotes how compatible the driver should attempt to
 * be, see {@link JdbcCompatibility} for discussion on the meaning of
 * compatibility levels.
 * </p>
 * <p>
 * When not set the {@link JdbcCompatibility#DEFAULT} compatibility level is
 * used, note that {@link JenaConnection} objects support changing this after
 * the connection has been established.
 * </p>
 * <h5>Pre-Processors</h5>
 * <p>
 * The second of these is the {@code pre-processor} parameter which is used to
 * specify one/more {@link CommandPreProcessor} implementations to use. The
 * parameter should be specified once for each pre-processor you wish to you and
 * you should supply a fully qualified class name to ensure the pre-processor
 * can be loaded and registered on your connections. The driver will report an
 * error if you specify a class that cannot be appropriately loaded and
 * registered.
 * </p>
 * <p>
 * Pre-processors are registered in the order that they are specified so if you
 * use multiple pre-processors and they have ordering dependencies please ensure
 * that you specify them in the desired order. Note that {@link JenaConnection}
 * objects support changing registered pre-processors after the connection has
 * been established.
 * </p>
 * <h5>Post-Processors</h5>
 * <p>
 * There is also a {@code post-processor} parameter which is used to specify
 * one/more {@link ResultsPostProcessor} implementations to use. The parameter
 * should be specified once for each post-processor you wish to use and you
 * should supply a fully qualified class name to ensure the post-processor can
 * be loaded and registered on your connections. The driver will report an error
 * is you specify a class that cannot be appropriately loaded and registered.
 * </p>
 * <p>
 * Post-processors are registered in the order that they are specified so if you
 * use multiple post-processors and they have ordering dependencies please
 * ensure that you specify them in the desired order. Note that
 * {@link JenaConnection} objects support changing registered post-processors
 * after the connection has been established.
 * </p>
 */
public abstract class JenaDriver implements Driver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenaDriver.class);

    /**
     * Constant for the primary Jena JDBC Driver prefix, implementations supply
     * an additional prefix which will form the next portion of the JDBC URL
     */
    public static final String DRIVER_PREFIX = "jdbc:jena:";

    /**
     * Constant for the connection URL parameter which sets the desired JDBC
     * compatibility level
     */
    public static final String PARAM_JDBC_COMPATIBILITY = "jdbc-compatibility";

    /**
     * Constant for the connection URL parameter used to set class names of
     * {@link CommandPreProcessor} implementations to use with returned
     * connections.
     * <p>
     * This parameter may be specified multiple times and pre-processors will be
     * registered (and thus applied) in the order specified. If any
     * pre-processors are passed as part of the {@link Properties} object rather
     * than in the connection URL then these will be registered (and thus
     * applied) prior to those specified in the connection URL.
     * </p>
     */
    public static final String PARAM_PRE_PROCESSOR = "pre-processor";

    /**
     * Constant for the connection URL parameter used to set class names of
     * {@link ResultsPostProcessor} implementations to use with returned
     * connections.
     * <p>
     * This parameter may be specified multiple times and post-processors will
     * be registered (and thus applied) in the order specified. If any
     * post-processors are passed as part of the {@link Properties} object
     * rather than in the connection URL then these will be registered (and thus
     * applied) prior to those specified in the connection URL.
     * </p>
     */
    public static final String PARAM_POST_PROCESSOR = "post-processor";

    /**
     * Constant for the standard JDBC connection URL parameter used to set user
     * name for drivers that support authentication
     */
    public static final String PARAM_USERNAME = "user";

    /**
     * Constant for the standard JDBC connection URL parameter used to set
     * password for drivers that support authentication
     */
    public static final String PARAM_PASSWORD = "password";

    /**
     * Constant for the connection URL parameter used to set the path to a log4j
     * properties file used to configure logging. When set to a file the file
     * system is searched before the class path (since on the class path you are
     * more likely to have duplicates particularly if using a standard name like
     * <strong>log4j.properties</strong>).
     * <p>
     * When not set this defaults to the special value <strong>no-auto</strong>
     * provided by the constant {@link #NO_AUTO_LOGGING_CONFIGURATION} that
     * indicates that the driver should not configure any logging. This is
     * useful if you want to have your application managed its own
     * configuration.
     * </p>
     */
    public static final String PARAM_LOGGING = "logging";

    /**
     * Constant for the special value used with the {@link #PARAM_LOGGING}
     * parameter to indicate that the user code will manage configuration of
     * logging. This is also the default value when that parameter is not set.
     */
    public static final String NO_AUTO_LOGGING_CONFIGURATION = "no-auto";

    /**
     * Constant for the connection URL parameter used to set the path to a Java
     * properties file used to provide additional connection parameters external
     * to the URL. This is useful when you want to have a single connection
     * string and pick up different configurations depending on where the driver
     * is used.
     * <p>
     * Just like the {@link #PARAM_LOGGING} parameter the file system is
     * searched before the class path. Also note that this is the first
     * parameter that is honored so settings present in the properties file may
     * be overridden by those explicitly provided in the connection URL or those
     * given in the {@link Properties} object passed to the
     * {@link Driver#connect(String, Properties)} method.
     * </p>
     * <p>
     * If you specify this parameter in both the connection URL and the
     * {@link Properties} object then the latter takes precedence and the former
     * is ignored. It is also important to note that this is not transitively
     * resolved i.e. if the referenced property file includes a {@code config}
     * parameter it does not result in a further properties file/files being
     * loaded.
     * </p>
     */
    public static final String PARAM_CONFIG = "config";

    private int majorVer, minorVer;
    private String implPrefix;

    /**
     * Creates a new driver
     * 
     * @param majorVer
     *            Major Version
     * @param minorVer
     *            Minor Version
     * @param prefix
     *            Implementation specific prefix which must end with a colon
     */
    public JenaDriver(int majorVer, int minorVer, String prefix) {
        this.majorVer = majorVer;
        this.minorVer = minorVer;

        if (prefix == null)
            throw new IllegalArgumentException("Implementation specific prefix cannot be null");
        if (!prefix.endsWith(":"))
            throw new IllegalArgumentException("Implementation specific prefix must end with a :");
        // Validate that implPrefix is a valid scheme i.e. [A-Za-z\d\-_]+
        if (!prefix.matches("[A-Za-z\\d\\-_]+:"))
            throw new IllegalArgumentException(
                    "Implementation specific prefix must conform to the regular expression [A-Za-z\\d\\-_]+:");
        this.implPrefix = prefix;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url.startsWith(DRIVER_PREFIX + this.implPrefix)) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Connection connect(String url, Properties props) throws SQLException {
        // Make sure to return null if the URL is not supported
        if (!this.acceptsURL(url))
            return null;

        // Compute the effective properties
        Properties ps = this.getEffectiveProperties(url, props);
        this.modifyProperties(ps);

        // Configure logging appropriately
        String logConfig = ps.getProperty(PARAM_LOGGING);
        if (logConfig == null || logConfig.trim().length() == 0) {
            logConfig = NO_AUTO_LOGGING_CONFIGURATION;
        }

        // Unless set to no configuration (which is the default attempt to
        // configure)
        if (!logConfig.equals(NO_AUTO_LOGGING_CONFIGURATION)) {
            // Search file system first
            File logConfigFile = new File(logConfig);
            if (logConfigFile.exists() && logConfigFile.isFile()) {
                PropertyConfigurator.configure(logConfig);
                LOGGER.info("Successfully configured logging using log file " + logConfigFile.getAbsolutePath());
            } else {
                // Otherwise try class path
                URL logURL = this.getClass().getResource(logConfig);
                if (logURL != null) {
                    PropertyConfigurator.configure(logURL);
                    LOGGER.info("Successfully configured logging using class path resource " + logConfig);
                } else {
                    throw new SQLException(
                            "Unable to locate the specified log4j configuration file on either the file system or the class path");
                }
            }
        }

        // Figure out desired JDBC compatibility level
        int compatibilityLevel = JdbcCompatibility.parseLevel(ps.get(PARAM_JDBC_COMPATIBILITY));

        // Try to create the connection
        JenaConnection conn = null;
        boolean abort = false;
        try {
            // Attempt connection
            conn = this.connect(ps, compatibilityLevel);

            // Prepare reduced properties for initializing any pre and
            // post-processors with
            Properties initProps = new Properties(ps);
            initProps.remove(PARAM_PASSWORD);

            // Attempt registration of command pre-processors
            Object ppObj = ps.get(PARAM_PRE_PROCESSOR);
            List<String> preProcessors;
            if (ppObj != null) {
                if (ppObj instanceof String) {
                    // Single instance to try and register
                    preProcessors = new ArrayList<String>();
                    preProcessors.add(ppObj.toString());
                } else if (ppObj instanceof List<?>) {
                    // Multiple instances to try and register
                    preProcessors = (List<String>) ppObj;
                } else {
                    // Parameter set to some unexpected type
                    LOGGER.error("Driver Parameter " + PARAM_PRE_PROCESSOR + " has unexpected invalid value");
                    throw new SQLException("Parameter " + PARAM_PRE_PROCESSOR + " was set to a value of unexpected type "
                            + ppObj.getClass().getCanonicalName()
                            + ", expected either a String or List<String> as the parameter value");
                }

                // Try and create each pre-processor
                for (String ppClassName : preProcessors) {
                    // Ignore null values
                    if (ppClassName == null)
                        continue;

                    try {
                        LOGGER.info("Attempting to initialize pre-processor " + ppClassName);
                        Class<?> c = Class.forName(ppClassName);
                        Object i = c.newInstance();

                        if (i instanceof CommandPreProcessor) {
                            // If it implements the right interface initialize
                            // and
                            // register it
                            CommandPreProcessor pp = (CommandPreProcessor) i;
                            pp.initialize(initProps);
                            conn.addPreProcessor(pp);

                            LOGGER.info("Initialized pre-processor " + ppClassName + " successfully");
                        } else {
                            // Otherwise throw an error
                            LOGGER.error("Invalid value for " + PARAM_PRE_PROCESSOR
                                    + " parameter, references a class that exists but does not implement the required interface");
                            throw new SQLException(
                                    "Parameter "
                                            + PARAM_PRE_PROCESSOR
                                            + " includes the value "
                                            + ppClassName
                                            + " which references a class that does not implement the expected CommandPreProcessor interface, please ensure that the class name is corect and that the class implements the required interface");
                        }
                    } catch (ClassNotFoundException e) {
                        // Unable to find the referenced class
                        LOGGER.error("Invalid value for " + PARAM_PRE_PROCESSOR
                                + " parameter, references a class that did not exist", e);
                        throw new SQLException(
                                "Parameter "
                                        + PARAM_PRE_PROCESSOR
                                        + " includes the value "
                                        + ppClassName
                                        + " which references a class that could not be found, please ensure that the class name is correct and the JAR containing this class is on your class path",
                                e);
                    } catch (InstantiationException e) {
                        // Unable to instantiate the referenced class
                        LOGGER.error("Invalid value for " + PARAM_PRE_PROCESSOR
                                + " parameter, references a class that exists but does not have an appropriate constructor", e);
                        throw new SQLException(
                                "Parameter "
                                        + PARAM_PRE_PROCESSOR
                                        + " includes the value "
                                        + ppClassName
                                        + " which references a class that could not be sucessfully instantiated, this class must have an unparameterized constructor to be usable with this parameter.  If this is not possible try calling addPreProcessor() on the returned JenaConnection instead",
                                e);
                    } catch (IllegalAccessException e) {
                        // Referenced class is not accessible
                        LOGGER.error("Invalid value for " + PARAM_PRE_PROCESSOR
                                + " parameter, references a class that exists but is inaccessible", e);
                        throw new SQLException(
                                "Parameter "
                                        + PARAM_PRE_PROCESSOR
                                        + " includes the value "
                                        + ppClassName
                                        + " which references a class that could not be sucessfully instantiated, this class must have a publicly accessible unparameterized constructor to be usable with this parameter.  If this is not possible try calling addPreProcessor() on the returned JenaConnection instead",
                                e);
                    } catch (SQLException e) {
                        // Throw as-is
                        throw e;
                    } catch (Exception e) {
                        // Unexpected error
                        LOGGER.error(
                                "Invalid value for "
                                        + PARAM_PRE_PROCESSOR
                                        + " parameter, references a class that attempting to initialize produced an unexpected exception",
                                e);
                        throw new SQLException(
                                "Parameter "
                                        + PARAM_PRE_PROCESSOR
                                        + " includes the value "
                                        + ppClassName
                                        + " which caused an unexpected exception when trying to instantiate it, see the inner exception for details",
                                e);
                    }
                }
            }

            // Attempt registration of results post-processors
            ppObj = ps.get(PARAM_POST_PROCESSOR);
            List<String> postProcessors;
            if (ppObj != null) {

                if (ppObj instanceof String) {
                    // Single instance to try and register
                    postProcessors = new ArrayList<String>();
                    postProcessors.add(ppObj.toString());
                } else if (ppObj instanceof List<?>) {
                    // Multiple instances to try and register
                    postProcessors = (List<String>) ppObj;
                } else {
                    // Parameter set to some unexpected type
                    LOGGER.error("Driver Parameter " + PARAM_POST_PROCESSOR + " has unexpected invalid value");
                    throw new SQLException("Parameter " + PARAM_POST_PROCESSOR + " was set to a value of unexpected type "
                            + ppObj.getClass().getCanonicalName()
                            + ", expected either a String or List<String> as the parameter value");
                }

                // Try and create each pre-processor
                for (String ppClassName : postProcessors) {
                    // Ignore null values
                    if (ppClassName == null)
                        continue;

                    try {
                        LOGGER.info("Attempting to initialize post-processor " + ppClassName);
                        Class<?> c = Class.forName(ppClassName);
                        Object i = c.newInstance();

                        if (i instanceof ResultsPostProcessor) {
                            // If it implements the right interface initialize
                            // and
                            // register it
                            ResultsPostProcessor pp = (ResultsPostProcessor) i;
                            pp.initialize(initProps);
                            conn.addPostProcessor(pp);

                            LOGGER.info("Initialized post-processor " + ppClassName + " successfully");
                        } else {
                            // Otherwise throw an error
                            LOGGER.error("Invalid value for " + PARAM_POST_PROCESSOR
                                    + " parameter, references a class that exists but does not implement the required interface");
                            throw new SQLException(
                                    "Parameter "
                                            + PARAM_POST_PROCESSOR
                                            + " includes the value "
                                            + ppClassName
                                            + " which references a class that does not implement the expected ResultsPostProcessor interface, please ensure that the class name is corect and that the class implements the required interface");
                        }
                    } catch (ClassNotFoundException e) {
                        // Unable to find the referenced class
                        LOGGER.error("Invalid value for " + PARAM_POST_PROCESSOR
                                + " parameter, references a class that did not exist", e);
                        throw new SQLException(
                                "Parameter "
                                        + PARAM_POST_PROCESSOR
                                        + " includes the value "
                                        + ppClassName
                                        + " which references a class that could not be found, please ensure that the class name is correct and the JAR containing this class is on your class path",
                                e);
                    } catch (InstantiationException e) {
                        // Unable to instantiate the referenced class
                        LOGGER.error("Invalid value for " + PARAM_POST_PROCESSOR
                                + " parameter, references a class that exists but does not have an appropriate constructor", e);
                        throw new SQLException(
                                "Parameter "
                                        + PARAM_POST_PROCESSOR
                                        + " includes the value "
                                        + ppClassName
                                        + " which references a class that could not be sucessfully instantiated, this class must have an unparameterized constructor to be usable with this parameter.  If this is not possible try calling addPostProcessor() on the returned JenaConnection instead",
                                e);
                    } catch (IllegalAccessException e) {
                        // Referenced class is not accessible
                        LOGGER.error("Invalid value for " + PARAM_POST_PROCESSOR
                                + " parameter, references a class that exists but is inaccessible", e);
                        throw new SQLException(
                                "Parameter "
                                        + PARAM_POST_PROCESSOR
                                        + " includes the value "
                                        + ppClassName
                                        + " which references a class that could not be sucessfully instantiated, this class must have a publicly accessible unparameterized constructor to be usable with this parameter.  If this is not possible try calling addPostProcessor() on the returned JenaConnection instead",
                                e);
                    } catch (SQLException e) {
                        // Throw as-is
                        throw e;
                    } catch (Exception e) {
                        // Unexpected error
                        LOGGER.error(
                                "Invalid value for "
                                        + PARAM_POST_PROCESSOR
                                        + " parameter, references a class that attempting to initialize produced an unexpected exception",
                                e);
                        throw new SQLException(
                                "Parameter "
                                        + PARAM_POST_PROCESSOR
                                        + " includes the value "
                                        + ppClassName
                                        + " which caused an unexpected exception when trying to instantiate it, see the inner exception for details",
                                e);
                    }
                }
            }

            // All pre and post processors successfully registered, return the
            // connection
            return conn;
        } catch (SQLException e) {
            abort = true;
            throw e;
        } catch (Exception e) {
            abort = true;
            LOGGER.error("Unexpected exception while establishing a connection", e);
            throw new SQLException("Unexpected exception while establishing a connection, see inner exception for details", e);
        } finally {
            // If something has gone badly wrong close the connection
            if (abort && conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Determines the effective properties
     * <p>
     * This method parses the connection URL for properties and then
     * appends/overwrites properties from the given {@link Properties} object as
     * appropriate
     * </p>
     * 
     * @param url
     *            Connection URL
     * @param props
     *            Properties
     * @return Effective Properties
     * @throws SQLException
     *             Thrown if the properties are invalid
     */
    @SuppressWarnings("unchecked")
    private Properties getEffectiveProperties(String url, Properties props) throws SQLException {
        // Create new empty properties
        Properties ps = new Properties();

        // Parse out the key value pairs from the connection URL
        url = url.substring(DRIVER_PREFIX.length() + this.implPrefix.length());
        String[] kvps = url.split("&|;");
        for ( String kvp : kvps )
        {
            if ( kvp.length() == 0 )
            {
                continue;
            }

            // Try to split into key and value
            String key, value;
            if ( kvp.contains( "=" ) )
            {
                String[] temp = kvp.split( "=", 2 );
                key = temp[0];
                value = temp[1];
            }
            else
            {
                key = kvp;
                value = null;
            }

            // All keys are normalized to lower case using the English Locale
            key = key.toLowerCase( Locale.ENGLISH );

            // Put into properties appropriately
            if ( !ps.containsKey( key ) )
            {
                // Doesn't yet exist, add a string/list as appropriate
                if ( this.allowsMultipleValues( key ) )
                {
                    List<String> values = new ArrayList<String>();
                    if ( value.contains( "," ) )
                    {
                        // Comma separated lists are usable for multiple value
                        // properties
                        String[] vs = value.split( "," );
                        for ( String v : vs )
                        {
                            values.add( v );
                        }
                    }
                    else
                    {
                        values.add( value );
                    }
                    ps.put( key, values );
                }
                else
                {
                    ps.put( key, value );
                }
            }
            else if ( this.allowsMultipleValues( key ) )
            {
                // If it allows multiple values append to those existing
                Object currValue = ps.get( key );
                if ( currValue instanceof List<?> )
                {
                    // Can just append to existing list
                    if ( value.contains( "," ) )
                    {
                        // Comma separated lists are usable for multiple value
                        // properties
                        String[] vs = value.split( "," );
                        for ( String v : vs )
                        {
                            ( (List<Object>) currValue ).add( v );
                        }
                    }
                    else
                    {
                        ( (List<Object>) currValue ).add( value );
                    }
                }
                else
                {
                    // Convert to list
                    List<String> values = new ArrayList<String>();
                    values.add( currValue.toString() );
                    if ( value.contains( "," ) )
                    {
                        String[] vs = value.split( "," );
                        for ( String v : vs )
                        {
                            values.add( v );
                        }
                    }
                    else
                    {
                        values.add( value );
                    }
                    ps.put( key, values );
                }
            }
            else
            {
                LOGGER.warn( "Cannot specify parameter " + key + " multiple times in the connection URL" );
                throw new SQLException( "Invalid connection URL parameter " + kvp + " encountered, the parameter " + key
                                            + " may only be specified once" );
            }
        }

        // Next up load in external properties file if relevant
        if (props != null && props.containsKey(PARAM_CONFIG)) {
            // If present in passed Properties object this takes preference
            Properties external = this.loadProperties(props.getProperty(PARAM_CONFIG));
            LOGGER.info("Merging in external properties file " + props.getProperty(PARAM_CONFIG));
            this.mergeProperties(external, ps, false);
        } else if (ps.containsKey(PARAM_CONFIG)) {
            // If present in connection URL then use this
            Properties external = this.loadProperties(ps.getProperty(PARAM_CONFIG));
            LOGGER.info("Merging in external properties file " + ps.getProperty(PARAM_CONFIG));
            this.mergeProperties(external, ps, false);
        }

        // Overwrite connection URL parameters with code provided parameters
        // if applicable
        if (props != null) {
            this.mergeProperties(props, ps, true);
        }

        return ps;
    }

    /**
     * Merges one set of properties into another
     * 
     * @param source
     *            Source Properties to be merged
     * @param target
     *            Target Properties to merge to
     * @param overwriteOrAppendIfExists
     *            Whether source properties should overwrite/append to
     *            properties already present in the target properties
     */
    @SuppressWarnings("unchecked")
    private void mergeProperties(Properties source, Properties target, boolean overwriteOrAppendIfExists) {
        // Copy across normalizing keys to lower case in the English Locale
        for (Entry<Object, Object> e : source.entrySet()) {
            String key = e.getKey().toString().toLowerCase(Locale.ENGLISH);
            Object value = e.getValue();

            if (target.containsKey(key)) {
                // Already exists in connection URL so append/overwrite if
                // appropriate
                if (overwriteOrAppendIfExists) {
                    if (this.allowsMultipleValues(key)) {
                        // Should append to existing values
                        Object currValue = target.get(key);
                        if (currValue instanceof List<?>) {
                            // Can just append to existing list
                            ((List<Object>) currValue).add(value);
                        } else {
                            // Convert to list
                            List<String> values = new ArrayList<String>();
                            values.add(currValue.toString());
                            values.add(value.toString());
                            target.put(key, values);
                        }
                    } else {
                        // Overwrite existing value
                        target.put(key, value);
                    }
                }
            } else {
                // Not in connection URL so add key value pair
                target.put(key, value);
            }
        }
    }

    private Properties loadProperties(String resource) throws SQLException {
        Properties ps = new Properties();

        // Search file system first
        File propFile = new File(resource);
        if (propFile.exists() && propFile.isFile()) {
            try {
                FileInputStream input = new FileInputStream(propFile);
                ps.load(input);
                input.close();
            } catch (FileNotFoundException e) {
                throw new SQLException("Located external properties file " + propFile.getAbsolutePath()
                        + " on file system but it was removed before it could be read", e);
            } catch (IOException e) {
                throw new SQLException("IO Error attempting to load external properties file " + propFile.getAbsolutePath());
            }
            LOGGER.info("Successfully loaded external properties file " + propFile.getAbsolutePath());
        } else {
            // Otherwise try class path
            URL propURL = this.getClass().getResource(resource);
            if (propURL != null) {
                try {
                    InputStream input = propURL.openStream();
                    ps.load(input);
                    input.close();
                } catch (IOException e) {
                    throw new SQLException("IO Error attempting to load class path properties file from resource " + resource, e);
                }
                LOGGER.info("Successfully loaded class path properties file from resource " + resource);
            } else {
                throw new SQLException(
                        "Unable to locate the specified external properties file on either the file system or the class path");
            }
        }

        // Process properties for those that support multiple values
        for (Entry<Object, Object> e : ps.entrySet()) {
            String key = e.getKey().toString().toLowerCase(Locale.ENGLISH);
            if (this.allowsMultipleValues(key)) {
                Object currValue = e.getValue();
                if (currValue instanceof String) {
                    // Does the value contain a comma separated list?
                    if (currValue.toString().contains(",")) {
                        String[] values = currValue.toString().split(",");
                        ps.put(e.getKey(), Arrays.asList(values));
                    }
                }
            }
        }

        return ps;
    }

    /**
     * Method that will be called after the driver has computed the effective
     * properties from the connection URL and the {@link Properties} object
     * provided to the {@link #connect(String, Properties)} method. Allows for
     * derived implementations to inject/modify properties before the driver
     * actually attempts to use them to create the connection.
     * 
     * @param props
     *            Properties
     */
    protected void modifyProperties(Properties props) throws SQLException {
        // Default implementation does nothing
    }

    /**
     * Gets whether a parameter is allowed to have multiple values
     * <p>
     * If you override this method in your driver implementation you should make
     * sure to include a call back to this method as otherwise you may cause
     * incorrect driver instantiation.
     * </p>
     * 
     * @param key
     *            Key
     * @return True if multiple values are allowed, false otherwise
     */
    protected boolean allowsMultipleValues(String key) {
        if (PARAM_PRE_PROCESSOR.equals(key) || PARAM_POST_PROCESSOR.equals(key)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method which derived classes must implement to create their actual
     * connections
     * <p>
     * The driver will already have parsed the connection URL to extract out any
     * connection parameters and has added them to the parameters provided in
     * the Properties object. Parameters which the implementation has indicated
     * allow multiple values will be present as {@link List} in the properties,
     * all other parameters will be either strings or the original values as
     * injected by the calling code. Properties specified in the
     * {@link Properties} object by the calling code have precedence over the
     * connection URL parameters.
     * </p>
     * 
     * @param props
     *            Properties
     * @return Connection
     * @throws SQLException
     *             Thrown if a connection cannot be created for any reason
     */
    protected abstract JenaConnection connect(Properties props, int compatabilityLevel) throws SQLException;

    @Override
    public int getMajorVersion() {
        return this.minorVer;
    }

    @Override
    public int getMinorVersion() {
        return this.majorVer;
    }

    public final DriverPropertyInfo[] getPropertyInfo(String url, Properties props) throws SQLException {
        Properties ps = this.getEffectiveProperties(url, props);

        // Create base driver properties
        List<DriverPropertyInfo> baseProps = new ArrayList<DriverPropertyInfo>();

        // JDBC compatibility level
        DriverPropertyInfo jdbcCompatLevel = new DriverPropertyInfo(PARAM_JDBC_COMPATIBILITY, ps.getProperty(
                PARAM_JDBC_COMPATIBILITY, Integer.toString(JdbcCompatibility.DEFAULT)));
        jdbcCompatLevel.description = "Configures how compatible the driver will attempt to be with JDBC, primarily affects reported column types for result sets";
        jdbcCompatLevel.required = false;
        String[] choices = new String[9];
        for (int i = 0; i < choices.length; i++) {
            choices[i] = Integer.toString(i + 1);
        }
        jdbcCompatLevel.choices = choices;
        baseProps.add(jdbcCompatLevel);

        // Pre-processors
        DriverPropertyInfo preProcessor = new DriverPropertyInfo(PARAM_PRE_PROCESSOR, StrUtils.strjoin(",",
                this.getValues(ps, PARAM_PRE_PROCESSOR)));
        preProcessor.description = "Configures pre-processors which are used to amend SPARQL text, queries or updates before these are passed to the underlying SPARQL engine, multiple fully qualified class names may be specified";
        preProcessor.required = false;
        baseProps.add(preProcessor);

        // Logging config
        DriverPropertyInfo logging = new DriverPropertyInfo(PARAM_LOGGING, ps.getProperty(PARAM_LOGGING));
        logging.description = "Sets the path to a log4j properties file for configuring logging, the file system is considered first and then the classpath.  If not set defaults to log4j.properties";
        logging.required = false;
        baseProps.add(logging);

        // Have the derived implementation create the final property information
        return this.getPropertyInfo(ps, baseProps);
    }

    /**
     * Helper method for copying the base properties into the final properties
     * 
     * @param finalProps
     *            Final Properties array
     * @param baseProps
     *            Base Properties
     * @param start
     *            Index in the final properties array at which to start copying
     */
    protected final void copyBaseProperties(DriverPropertyInfo[] finalProps, List<DriverPropertyInfo> baseProps, int start) {
        for (int i = start, j = 0; i < finalProps.length && j < baseProps.size(); i++, j++) {
            finalProps[i] = baseProps.get(j);
        }
    }

    /**
     * Gets driver property information to aid in making a connection
     * 
     * @param connProps
     *            Known connection properties
     * @param baseDriverProps
     *            Base driver properties supported by the driver
     * @return Driver property information
     */
    protected abstract DriverPropertyInfo[] getPropertyInfo(Properties connProps, List<DriverPropertyInfo> baseDriverProps);

    /**
     * Returns that a Jena JDBC driver is not JDBC compliant since strict JDBC
     * compliance requires support for SQL-92 and since we are using SPARQL we
     * don't meet that criteria
     */
    @Override
    public final boolean jdbcCompliant() {
        // This has to be false since we are not JDBC compliant in that
        // we use SPARQL in place of SQL-92
        return false;
    }

    /**
     * Helper method which attempts to return the value for a parameter that may
     * allow multiple values
     * 
     * @param props
     *            Properties
     * @param key
     *            Parameter
     * @return List of values (may be empty)
     * @throws SQLException
     *             Thrown if the parameter has a value of an incompatible type
     */
    @SuppressWarnings("unchecked")
    protected List<String> getValues(Properties props, String key) throws SQLException {
        Object obj = props.get(key);
        if (obj == null)
            return new ArrayList<String>();
        if (obj instanceof List<?>)
            return (List<String>) obj;
        if (obj instanceof String) {
            List<String> values = new ArrayList<String>();
            values.add(obj.toString());
            return values;
        } else {
            throw new SQLException("Value given for parameter " + key + " was not a string/list of strings");
        }
    }

    /**
     * Gets whether a given property is set to true
     * 
     * @param props
     *            Properties
     * @param key
     *            Key
     * @return True if the key is set to a non-null value whose lower case
     *         string equals {@code true}, otherwise false
     */
    protected boolean isTrue(Properties props, String key) {
        Object obj = props.get(key);
        if (obj == null)
            return false;
        String value = obj.toString().toLowerCase(Locale.ENGLISH).trim();
        return "true".equals(value);
    }

    /**
     * Gets the boolean value of a property
     * <p>
     * Looks for the string values {@code true} or {@code false} and returns the
     * equivalent boolean if found. If there is no value for the key or it has
     * some other value then the value of the {@code defaultValue} parameter is
     * returned.
     * </p>
     * 
     * @param props
     *            Properties
     * @param key
     *            Key
     * @param defaultValue
     *            Default value
     * @return Boolean for the value
     */
    protected boolean getBoolean(Properties props, String key, boolean defaultValue) {
        Object obj = props.get(key);
        if (obj == null)
            return defaultValue;
        String value = obj.toString().toLowerCase(Locale.ENGLISH).trim();
        if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets whether a given property is set to a specific value
     * <p>
     * Applies {@link String#trim()} and {@link String#toLowerCase(Locale)} to
     * the set value before comparing it with the expected value
     * </p>
     * 
     * @param props
     *            Properties
     * @param key
     *            Key
     * @param value
     *            Expected value
     * @return True if the key is set to a value which matches the expected
     *         value, otherwise false
     */
    protected boolean isSetToValue(Properties props, String key, String value) {
        Object obj = props.get(key);
        if (obj == null)
            return value == null;
        if (value == null)
            return false;
        String actualValue = obj.toString().trim().toLowerCase(Locale.ENGLISH);
        return value.equals(actualValue);
    }

	// Java6/7 compatibility
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException() ;
	}
}
