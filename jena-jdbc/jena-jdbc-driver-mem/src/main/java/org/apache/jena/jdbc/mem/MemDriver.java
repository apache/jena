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

package org.apache.jena.jdbc.mem;

import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.jena.jdbc.JenaDriver;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.mem.connections.MemConnection;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

/**
 * <p>
 * A Jena JDBC driver which creates connections to in-memory datasets
 * </p>
 * <h3>
 * Connection URL</h3>
 * <p>
 * This driver expects a URL of the following form:
 * </p>
 * 
 * <pre>
 * jdbc:jena:mem:dataset=file.nq
 * </pre>
 * <p>
 * The {@code dataset} parameter is used to refer to a file containing the
 * dataset you wish to load. Note that if you are creating the connection in
 * code you may alternatively opt to provide a {@link Dataset} instance directly
 * as a property named {@code dataset} to the
 * {@link #connect(String, Properties)} method instead.
 * </p>
 * <p>
 * If you simply want to start with an empty dataset you may instead set the
 * {@code empty} parameter to be true e.g.
 * </p>
 * 
 * <pre>
 * jdbc:jena:mem:empty=true
 * </pre>
 */
public class MemDriver extends JenaDriver {

    /**
     * Constant for the memory driver prefix, this is appended to the base
     * {@link JenaDriver#DRIVER_PREFIX} to form the URL prefix for JDBC
     * Connection URLs for this driver
     */
    public static final String MEM_DRIVER_PREFIX = "mem:";

    /**
     * Constant for the connection URL parameter used to specify a dataset
     * file/instance to use
     */
    public static final String PARAM_DATASET = "dataset";

    /**
     * Constant for the connection URL parameter used to specify that an empty
     * dataset should be used. If {@link #PARAM_DATASET} is present then that
     * parameter has precedence.
     */
    public static final String PARAM_EMPTY = "empty";

    /**
     * Static initializer block which ensures the driver gets registered
     */
    static {
        try {
            ARQ.init();
            register();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register Jena In-Memory JDBC Driver", e);
        }
    }

    /**
     * Registers the driver with the JDBC {@link DriverManager}
     * 
     * @throws SQLException
     *             Thrown if the driver cannot be registered
     */
    public static synchronized void register() throws SQLException {
        DriverManager.registerDriver(new MemDriver());
    }

    /**
     * Creates a new driver
     */
    public MemDriver() {
        super(0, 1, MEM_DRIVER_PREFIX);
    }

    @Override
    protected JenaConnection connect(Properties props, int compatibilityLevel) throws SQLException {
        Object dsObj = props.get(PARAM_DATASET);
        String empty = props.getProperty(PARAM_EMPTY);
        if (dsObj == null && empty == null)
            throw new SQLException("Neither one of the " + PARAM_DATASET + " or " + PARAM_EMPTY
                    + " connection parameters is present in the JDBC Connection URL or the provided Properties object");

        if (dsObj != null) {
            if (dsObj instanceof Dataset) {
                // Dataset provided directly
                return new MemConnection((Dataset) dsObj, JenaConnection.DEFAULT_HOLDABILITY, JenaConnection.DEFAULT_AUTO_COMMIT,
                        JenaConnection.DEFAULT_ISOLATION_LEVEL, compatibilityLevel);
            } else {
                // Load dataset from a file
                try {
                    Dataset ds = DatasetFactory.createMem();
                    RDFDataMgr.read(ds, dsObj.toString());
                    return new MemConnection(ds, JenaConnection.DEFAULT_HOLDABILITY, JenaConnection.DEFAULT_AUTO_COMMIT,
                            JenaConnection.DEFAULT_ISOLATION_LEVEL, compatibilityLevel);
                } catch (Exception e) {
                    throw new SQLException("Error occurred while reading from the specified RDF dataset file - "
                            + dsObj.toString(), e);
                }
            }
        } else if (this.isTrue(props, PARAM_EMPTY)) {
            // Use an empty dataset
            return new MemConnection(DatasetFactory.createMem(), JenaConnection.DEFAULT_HOLDABILITY,
                    JenaConnection.DEFAULT_AUTO_COMMIT, JenaConnection.DEFAULT_ISOLATION_LEVEL, compatibilityLevel);
        } else {
            throw new SQLException(
                    "Insufficient parameters to create a Jena JDBC in-memory connection, please supply a Dataset file/instance via the "
                            + PARAM_DATASET + " parameter or supply " + PARAM_EMPTY + "=true to connect to a new empty dataset");
        }
    }

    @Override
    protected DriverPropertyInfo[] getPropertyInfo(Properties connProps, List<DriverPropertyInfo> baseDriverProps) {
        DriverPropertyInfo[] driverProps;
        if (connProps.containsKey(PARAM_DATASET) || !this.isTrue(connProps, PARAM_EMPTY)) {
            driverProps = new DriverPropertyInfo[1 + baseDriverProps.size()];

            // Dataset parameter
            driverProps[0] = new DriverPropertyInfo(PARAM_DATASET, connProps.getProperty(PARAM_DATASET));
            driverProps[0].required = true;
            driverProps[0].description = "Sets a path to a file that should be read in to form an in-memory dataset";

            this.copyBaseProperties(driverProps, baseDriverProps, 1);
        } else if (connProps.containsKey(PARAM_EMPTY)) {
            driverProps = new DriverPropertyInfo[1 + baseDriverProps.size()];

            // Empty parameter
            driverProps[0] = new DriverPropertyInfo(PARAM_EMPTY, connProps.getProperty(PARAM_EMPTY));
            driverProps[0].required = true;
            driverProps[0].choices = new String[] { "true", "false" };
            driverProps[0].description = "Sets that the driver will use an empty in-memory dataset as the initial dataset, when set to true the "
                    + PARAM_DATASET + " parameter is not required";

            this.copyBaseProperties(driverProps, baseDriverProps, 1);
        } else {
            driverProps = new DriverPropertyInfo[2 + baseDriverProps.size()];

            // Dataset parameter
            driverProps[0] = new DriverPropertyInfo(PARAM_DATASET, connProps.getProperty(PARAM_DATASET));
            driverProps[0].required = true;
            driverProps[0].description = "Sets a path to a file that should be read in to form an in-memory dataset";

            // Empty parameter
            driverProps[1] = new DriverPropertyInfo(PARAM_EMPTY, connProps.getProperty(PARAM_EMPTY));
            driverProps[1].required = false;
            driverProps[1].choices = new String[] { "true", "false" };
            driverProps[1].description = "Sets that the driver will use an empty in-memory dataset as the initial dataset, when set to true the "
                    + PARAM_DATASET + " parameter is not required";

            copyBaseProperties(driverProps, baseDriverProps, 2);
        }
        return driverProps;
    }

}
