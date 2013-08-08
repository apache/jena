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

package org.apache.jena.jdbc.remote;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.jena.atlas.web.auth.ApacheModAuthFormLogin;
import org.apache.jena.atlas.web.auth.FormLogin;
import org.apache.jena.atlas.web.auth.FormsAuthenticator;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.PreemptiveBasicAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.JenaDriver;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.remote.connections.RemoteEndpointConnection;

import com.hp.hpl.jena.query.ARQ;

/**
 * <p>
 * A Jena JDBC driver which creates connections to remote endpoints
 * </p>
 * <h3>
 * Connection URL</h3>
 * <p>
 * This driver expects a URL of the following form:
 * </p>
 * 
 * <pre>
 * jdbc:jena:remote:query=http://example.org/query&update=http://example.org/update
 * </pre>
 * <p>
 * The {@code query} parameter is used to refer to a SPARQL query endpoint to
 * use, the {@code update} parameter is used to refer to a SPARQL update
 * endpoint to use. At least one of these parameters must be present in order to
 * make a connection, you may omit the former to get a write only connection or
 * the latter to get a read only connection.
 * </p>
 * <p>
 * Note that since the {@code &} character is used as a separator for connection
 * URL parameters if you need your endpoint URLs to include these you should set
 * the relevant parameters directly on the {@link Properties} object you pass to
 * the {@link #connect(String, Properties)} method instead.
 * </p>
 * <h3>Other Supported Parameters</h3>
 * <p>
 * This driver supports a variety of properties that can be used to configure
 * aspects of its behavior, firstly there are a set of properties used to
 * specify the dataset for queries and updates:
 * </p>
 * <ul>
 * <li>{@code default-graph-uri} - Sets a default graph for queries, may be
 * specified multiple times to specify multiple graphs to form the default graph
 * </li>
 * <li>{@code named-graph-uri} - Sets a named graph for queries, may be
 * specified multiple times to specify multiple named graphs for the dataset</li>
 * <li>{@code using-graph-uri} - Sets a default graph for updates, may be
 * specified multiple times to specify multiple graphs to form the default graph
 * for updates</li>
 * <li>{@code using-named-graph-uri} - Sets a named graph for updates, may be
 * specified multiple times to specify multiple named graphs for the dataset</li>
 * <li>{@code select-results-type} - Sets the format to request {@code SELECT}
 * results in from the remote endpoint</li>
 * <li>{@code model-results-type} - Sets the format to request {@code CONSTRUCT}
 * and {@code DESCRIBE} results in from the remote endpoint</li>
 * </ul>
 * <h3>Authentication Parameters</h3>
 * <p>
 * The driver also supports the standard JDBC {@code user} and {@code password}
 * parameters which are used to set user credentials for authenticating to the
 * remote HTTP server. This uses the ARQ HTTP authenticator API behind the
 * scenes so the following parameters may also be used to configure desired
 * behavior:
 * </p>
 * <ul>
 * <li>{@code preemptive-auth} - Sets a boolean indicating whether preemptive
 * basic authentication should be enabled, disabled by default.</li>
 * <li>{@code form-url} - Sets a URL to use for form based login.</li>
 * <li>{@code form-user-field} - Sets the name of the user name field used for
 * form based login. If omitted but {@code form-url} is used then the default
 * value {@code httpd_username} is used i.e. we assume you are communicating
 * with a Apache mod_auth_form protected site that uses the default form
 * configuration.</li>
 * <li>{@code form-password-field} - Sets the name of the password field used
 * for form based login. If omitted but {@code form-url} is used then the
 * default value {@code httpd_password} is used i.e. we assume you are
 * communicating with a Apache mod_auth_form protected site that uses the
 * default form configuration.</li>
 * </ul>
 * <p>
 * Alternatively you may use the {@code authenticator} parameter to set a
 * specific authenticator implementation to use, must be passed an instance of
 * {@link HttpAuthenticator} so can only be passed via the {@link Properties}
 * object and not via the connection URL. If this parameter is used then all
 * other authentication parameters are ignored.
 * </p>
 */
public class RemoteEndpointDriver extends JenaDriver {
    /**
     * Constant for the remote driver prefix, this is appended to the base
     * {@link JenaDriver#DRIVER_PREFIX} to form the URL prefix for JDBC
     * Connection URLs for this driver
     */
    public static final String REMOTE_DRIVER_PREFIX = "remote:";

    /**
     * Constant for the connection URL parameter that sets the remote SPARQL
     * query endpoint to use
     */
    public static final String PARAM_QUERY_ENDPOINT = "query";

    /**
     * Constant for the connection URL parameter that sets the remote SPARQL
     * update endpoint to use
     */
    public static final String PARAM_UPDATE_ENDPOINT = "update";

    /**
     * Constant for the connection URL parameter that sets a default graph URI
     * for SPARQL queries, may be specified multiple times to use specify
     * multiple default graphs to use
     */
    public static final String PARAM_DEFAULT_GRAPH_URI = "default-graph-uri";

    /**
     * Constant for the connection URL parameter that sets a named graph URI for
     * SPARQL queries, may be specified multiple times to use specify multiple
     * named graphs to use
     */
    public static final String PARAM_NAMED_GRAPH_URI = "named-graph-uri";

    /**
     * Constant for the connection URL parameter that sets a default graph URI
     * for SPARQL updates, may be specified multiple times to use specify
     * multiple default graphs to use
     */
    public static final String PARAM_USING_GRAPH_URI = "using-graph-uri";

    /**
     * Constant for the connection URL parameter that sets a named graph URI for
     * SPARQL updates, may be specified multiple times to use specify multiple
     * named graphs to use
     */
    public static final String PARAM_USING_NAMED_GRAPH_URI = "using-named-graph-uri";

    /**
     * Constant for the connection URL parameter that sets the results type to
     * request for {@code SELECT} queries against the remote endpoint
     */
    public static final String PARAM_SELECT_RESULTS_TYPE = "select-results-type";

    /**
     * Constant for the connection URL parameter that sets the results type to
     * request for {@code CONSTRUCT} and {@code DESCRIBE} queries against the
     * remote endpoint
     */
    public static final String PARAM_MODEL_RESULTS_TYPE = "model-results-type";

    /**
     * Constant for the connection URL parameter that sets the URL to use for
     * form based login.
     */
    public static final String PARAM_FORMS_LOGIN_URL = "form-url";

    /**
     * Constant for the connection URL parameter that sets the user name field
     * to use for form based logins
     */
    public static final String PARAM_FORMS_LOGIN_USER_FIELD = "form-user-field";

    /**
     * Constant for the connection URL parameter that sets the password field to
     * use for form based logins
     */
    public static final String PARAM_FORMS_LOGIN_PASSWORD_FIELD = "form-password-field";

    /**
     * Constant for the connection URL parameter that sets that preemptive
     * authentication should be enabled.
     */
    public static final String PARAM_PREEMPTIVE_AUTH = "preemptive-auth";

    /**
     * Constant for the parameter used to specify an authenticator used.
     * <p>
     * It is <strong>important</strong> to be aware that you must pass in an
     * actual instance of a {@link HttpAuthenticator} for this parameter so you
     * cannot use directly in the Connection URL and must pass in via the
     * {@link Properties} object.
     * </p>
     */
    public static final String PARAM_AUTHENTICATOR = "authenticator";

    /**
     * Static initializer block which ensures the driver gets registered
     */
    static {
        try {
            ARQ.init();
            register();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register Jena Remote Endpoint JDBC Driver", e);
        }
    }

    /**
     * Registers the driver with the JDBC {@link DriverManager}
     * 
     * @throws SQLException
     *             Thrown if the driver cannot be registered
     */
    public static synchronized void register() throws SQLException {
        DriverManager.registerDriver(new RemoteEndpointDriver());
    }

    /**
     * Creates a new driver
     */
    public RemoteEndpointDriver() {
        super(0, 1, REMOTE_DRIVER_PREFIX);
    }

    /**
     * Extension point for derived drivers which allows them to provide
     * different version information and driver prefix
     * 
     * @param majorVersion
     *            Major version
     * @param minorVersion
     *            Minor version
     * @param driverPrefix
     *            Driver Prefix
     */
    protected RemoteEndpointDriver(int majorVersion, int minorVersion, String driverPrefix) {
        super(majorVersion, minorVersion, driverPrefix);
    }

    @Override
    protected JenaConnection connect(Properties props, int compatibilityLevel) throws SQLException {
        String queryEndpoint = props.getProperty(PARAM_QUERY_ENDPOINT);
        String updateEndpoint = props.getProperty(PARAM_UPDATE_ENDPOINT);

        // Validate at least one endpoint present
        if (queryEndpoint == null && updateEndpoint == null)
            throw new SQLException("At least one of the " + PARAM_QUERY_ENDPOINT + " or " + PARAM_UPDATE_ENDPOINT
                    + " connection parameters must be specified to make a remote connection");

        // Gather dataset related parameters
        List<String> defaultGraphs = this.getValues(props, PARAM_DEFAULT_GRAPH_URI);
        List<String> namedGraphs = this.getValues(props, PARAM_NAMED_GRAPH_URI);
        List<String> usingGraphs = this.getValues(props, PARAM_USING_GRAPH_URI);
        List<String> usingNamedGraphs = this.getValues(props, PARAM_USING_NAMED_GRAPH_URI);

        // Authentication settings
        HttpAuthenticator authenticator = this.configureAuthenticator(queryEndpoint, updateEndpoint, props);

        // Result Types
        String selectResultsType = props.getProperty(PARAM_SELECT_RESULTS_TYPE, null);
        String modelResultsType = props.getProperty(PARAM_MODEL_RESULTS_TYPE, null);

        // Create connection
        return openConnection(queryEndpoint, updateEndpoint, defaultGraphs, namedGraphs, usingGraphs, usingNamedGraphs,
                authenticator, JenaConnection.DEFAULT_HOLDABILITY, compatibilityLevel, selectResultsType, modelResultsType);
    }

    protected HttpAuthenticator configureAuthenticator(String queryEndpoint, String updateEndpoint, Properties props)
            throws SQLException {
        // Is there a specific authenticator to use?
        Object authObj = props.get(PARAM_AUTHENTICATOR);
        if (authObj != null) {
            if (authObj instanceof HttpAuthenticator) {
                return (HttpAuthenticator) authObj;
            } else {
                throw new SQLException(
                        "The "
                                + PARAM_AUTHENTICATOR
                                + " parameter is specified but the value is not an object implementing the required HttpAuthenticator interface");
            }
        }

        // Otherwise get credentials to use
        String user = props.getProperty(PARAM_USERNAME, null);
        if (user != null && user.trim().length() == 0)
            user = null;
        String password = props.getProperty(PARAM_PASSWORD, null);
        if (password != null && password.trim().length() == 0)
            password = null;

        // If no credentials then we won't configure anything
        if (user == null || password == null)
            return null;

        // Are we using HTTP or form based login?
        String loginURL = props.getProperty(PARAM_FORMS_LOGIN_URL);
        if (loginURL != null) {
            // Determine login fields
            String userField = props.getProperty(PARAM_FORMS_LOGIN_USER_FIELD, ApacheModAuthFormLogin.USER_FIELD);
            String pwdField = props.getProperty(PARAM_FORMS_LOGIN_PASSWORD_FIELD, ApacheModAuthFormLogin.PASSWORD_FIELD);

            // Create logins
            Map<URI, FormLogin> logins = new HashMap<URI, FormLogin>();
            String baseUri = this.getCommonBase(queryEndpoint, updateEndpoint);
            if (baseUri != null) {
                // One/both endpoints are specified and they have a common
                // Base URI so we'll create a single login
                try {
                    logins.put(new URI(baseUri), new FormLogin(loginURL, userField, pwdField, user, password.toCharArray()));
                } catch (URISyntaxException e) {
                    throw new SQLException("Unable to configure form based login due to invalid Base URI", e);
                }
            } else {
                // Only one endpoint is specified or they did not share a common
                // base
                if (queryEndpoint != null) {
                    // Add a query endpoint specific login
                    try {
                        logins.put(new URI(queryEndpoint),
                                new FormLogin(loginURL, userField, pwdField, user, password.toCharArray()));
                    } catch (URISyntaxException e) {
                        throw new SQLException("Unable to configure form based login due to invalid Query Endpoint URI", e);
                    }
                }
                if (updateEndpoint != null) {
                    // Add an update endpoint specific login
                    try {
                        logins.put(new URI(updateEndpoint),
                                new FormLogin(loginURL, userField, pwdField, user, password.toCharArray()));
                    } catch (URISyntaxException e) {
                        throw new SQLException("Unable to configure form based login due to invalid Update Endpoint URI", e);
                    }
                }
            }
            return new FormsAuthenticator(logins);
        } else {
            // Do we want preemptive authentication?
            if (this.isTrue(props, PARAM_PREEMPTIVE_AUTH)) {
                return new PreemptiveBasicAuthenticator(new SimpleAuthenticator(user, password.toCharArray()));
            } else {
                // Use simple authenticator
                return new SimpleAuthenticator(user, password.toCharArray());
            }
        }
    }

    /**
     * Determines the common base of the two URIs if there is one. The common
     * base will have irrelevant components (fragment and query string) stripped
     * off of it.
     * <p>
     * If one URI is null and the other is non-null the non-null one is
     * returned.
     * </p>
     * 
     * @param x
     *            URI
     * @param y
     *            URI
     * @return Common base if it exists, null otherwise.
     */
    protected String getCommonBase(String x, String y) {
        if (x == null) {
            if (y == null) {
                return null;
            } else {
                return stripIrrelevantComponents(y);
            }
        } else if (y == null) {
            return stripIrrelevantComponents(x);
        } else if (x.equals(y)) {
            return stripIrrelevantComponents(x);
        } else {
            // Is one the base of the other?
            if (x.length() < y.length() && y.startsWith(x)) {
                return stripIrrelevantComponents(x);
            } else if (y.length() < x.length() && x.startsWith(y)) {
                return stripIrrelevantComponents(y);
            }

            // Otherwise we should strip last URI component off one/both URIs
            // and recurse
            if (x.length() < y.length()) {
                // y is longer so strip last component
                y = this.stripLastComponent(y);
            } else if (x.length() > y.length()) {
                // x is longer so strip last component
                x = this.stripLastComponent(x);
            } else {
                // Equal length so strip last component from both
                x = this.stripLastComponent(x);
                y = this.stripLastComponent(y);
            }

            // Be careful that if either returned null at this point bail out
            // Must do this before recursing as otherwise the recursive call
            // will see one input as null and treat as if the non-null is the
            // common base which is in fact incorrect in this case
            if (x == null || y == null) return null;

            return this.getCommonBase(x, y);
        }
    }

    /**
     * Strips the last component of the given URI if possible
     * 
     * @param input
     *            URI
     * @return Reduced URI or null if no further reduction is possible
     */
    private String stripLastComponent(String input) {
        try {
            URI uri = new URI(input);
            if (uri.getFragment() != null) {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(),
                        null).toString();
            } else if (uri.getQuery() != null) {
                return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null)
                        .toString();
            } else if (uri.getPath() != null) {
                // Try and strip off last segment of the path
                String currPath = uri.getPath();
                if (currPath.endsWith("/")) {
                    currPath = currPath.substring(0, currPath.length() - 1);
                    if (currPath.length() == 0)
                        currPath = null;
                    return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), currPath, null, null)
                            .toString();
                } else if (currPath.contains("/")) {
                    currPath = currPath.substring(0, currPath.lastIndexOf('/') + 1);
                    if (currPath.length() == 0)
                        currPath = null;
                    return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), currPath, null, null)
                            .toString();
                } else {
                    // If path is non-null it must always contain a /
                    // otherwise it would be an invalid path
                    // In this case there are no further components to strip
                    return null;
                }
            } else {
                // No further components to strip
                return null;
            }
        } catch (URISyntaxException e) {
            // Error stripping component
            return null;
        }
    }

    /**
     * Get the URI with irrelevant components (Fragment and Querystring)
     * stripped off
     * 
     * @param input
     *            URI
     * @return URI with irrelevant components stripped off or null if stripping
     *         is impossible
     */
    private String stripIrrelevantComponents(String input) {
        try {
            URI orig = new URI(input);
            return new URI(orig.getScheme(), orig.getUserInfo(), orig.getHost(), orig.getPort(), orig.getPath(), null, null)
                    .toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Opens the actual connection
     * <p>
     * This extension point allows derived drivers to return an extended version
     * of a {@link RemoteEndpointConnection} if they wish or merely to leverage
     * the method to provide a connection after replacing the
     * {@link #connect(Properties, int)} method with their own implementation
     * that uses different connection parameters
     * </p>
     * 
     * @param queryEndpoint
     *            SPARQL Query Endpoint
     * @param updateEndpoint
     *            SPARQL Update Endpoint
     * @param defaultGraphs
     *            Default Graphs for queries
     * @param namedGraphs
     *            Named Graphs for queries
     * @param usingGraphs
     *            Default Graphs for updates
     * @param usingNamedGraphs
     *            Named Graphs for updates
     * @param authenticator
     *            HTTP Authenticator
     * @param holdability
     *            Result Set holdability
     * @param compatibilityLevel
     *            JDBC compatibility level, see {@link JdbcCompatibility}
     * @param selectResultsType
     *            Results Type for {@code SELECT} results
     * @param modelResultsType
     *            Results Type for {@code CONSTRUCT} and {@code DESCRIBE}
     *            results
     * @return Remote endpoint connection
     * @throws SQLException
     */
    protected RemoteEndpointConnection openConnection(String queryEndpoint, String updateEndpoint, List<String> defaultGraphs,
            List<String> namedGraphs, List<String> usingGraphs, List<String> usingNamedGraphs, HttpAuthenticator authenticator,
            int holdability, int compatibilityLevel, String selectResultsType, String modelResultsType) throws SQLException {
        return new RemoteEndpointConnection(queryEndpoint, updateEndpoint, defaultGraphs, namedGraphs, usingGraphs,
                usingNamedGraphs, authenticator, holdability, compatibilityLevel, selectResultsType, modelResultsType);
    }

    @Override
    protected boolean allowsMultipleValues(String key) {
        if (PARAM_DEFAULT_GRAPH_URI.equals(key) || PARAM_NAMED_GRAPH_URI.equals(key) || PARAM_USING_GRAPH_URI.equals(key)
                || PARAM_USING_NAMED_GRAPH_URI.equals(key)) {
            return true;
        } else {
            return super.allowsMultipleValues(key);
        }
    }

    @Override
    protected DriverPropertyInfo[] getPropertyInfo(Properties connProps, List<DriverPropertyInfo> baseDriverProps) {
        DriverPropertyInfo[] driverProps = new DriverPropertyInfo[10 + baseDriverProps.size()];
        this.copyBaseProperties(driverProps, baseDriverProps, 10);

        // Query Endpoint parameter
        driverProps[0] = new DriverPropertyInfo(PARAM_QUERY_ENDPOINT, connProps.getProperty(PARAM_QUERY_ENDPOINT));
        driverProps[0].required = !connProps.containsKey(PARAM_UPDATE_ENDPOINT);
        driverProps[0].description = "Sets the SPARQL Query endpoint to use for query operations, if this is specified and "
                + PARAM_UPDATE_ENDPOINT + " is not then a read-only connection will be created";

        // Update Endpoint parameter
        driverProps[1] = new DriverPropertyInfo(PARAM_UPDATE_ENDPOINT, connProps.getProperty(PARAM_UPDATE_ENDPOINT));
        driverProps[1].required = !connProps.containsKey(PARAM_UPDATE_ENDPOINT);
        driverProps[1].description = "Sets the SPARQL Update endpoint to use for update operations, if this is specified and "
                + PARAM_QUERY_ENDPOINT + " is not then a write-only connection will be created";

        // Default Graph parameter
        driverProps[2] = new DriverPropertyInfo(PARAM_DEFAULT_GRAPH_URI, null);
        driverProps[2].required = false;
        driverProps[2].description = "Sets the URI for a default graph for queries, may be specified multiple times to specify multiple graphs which should form the default graph";

        // Named Graph parameter
        driverProps[3] = new DriverPropertyInfo(PARAM_NAMED_GRAPH_URI, null);
        driverProps[3].required = false;
        driverProps[3].description = "Sets the URI for a named graph for queries, may be specified multiple times to specify multiple named graphs which should be accessible";

        // Using Graph parameter
        driverProps[4] = new DriverPropertyInfo(PARAM_USING_GRAPH_URI, null);
        driverProps[4].required = false;
        driverProps[4].description = "Sets the URI for a default graph for updates, may be specified multiple times to specify multiple graphs which should form the default graph";

        // Using Named Graph parameter
        driverProps[5] = new DriverPropertyInfo(PARAM_USING_NAMED_GRAPH_URI, null);
        driverProps[5].required = false;
        driverProps[5].description = "Sets the URI for a named graph for updates, may be specified multiple times to specify multiple named graph which should be accessible";

        // Results Types
        driverProps[6] = new DriverPropertyInfo(PARAM_SELECT_RESULTS_TYPE, connProps.getProperty(PARAM_SELECT_RESULTS_TYPE));
        driverProps[6].required = false;
        driverProps[6].description = "Sets the results type for SELECT queries that will be requested from the remote endpoint";
        driverProps[7] = new DriverPropertyInfo(PARAM_MODEL_RESULTS_TYPE, connProps.getProperty(PARAM_MODEL_RESULTS_TYPE));
        driverProps[7].required = false;
        driverProps[7].description = "Sets the results type for CONSTRUCT and DESCRIBE queries that will be requested from the remote endpoint";

        // User Name parameter
        driverProps[8] = new DriverPropertyInfo(PARAM_USERNAME, connProps.getProperty(PARAM_USERNAME));

        // Password parameter
        driverProps[9] = new DriverPropertyInfo(PARAM_PASSWORD, connProps.getProperty(PARAM_PASSWORD));

        return driverProps;
    }
}
