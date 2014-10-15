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

package org.apache.jena.jdbc.tdb;

import java.io.File;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.jena.jdbc.JenaDriver;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.tdb.connections.TDBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * <p>
 * A Jena JDBC driver which creates connections to TDB datasets
 * </p>
 * <h3>
 * Connection URL</h3>
 * <p>
 * This driver expects a URL of the following form:
 * </p>
 * 
 * <pre>
 * jdbc:jena:tdb:location=/path/to/dataset;must-exist=false
 * </pre>
 * <p>
 * The {@code location} parameter is used to refer to a folder containing the
 * TDB dataset you wish to load. The {@code must-exist} parameter indicates
 * whether the TDB dataset must already exist, if false then the driver will
 * create a new empty TDB dataset in that location if a TDB dataset does not
 * already exist.
 * </p>
 * <p>
 * Connections to TDB always support transactions and operate in auto-commit
 * mode by default.
 * </p>
 */
public class TDBDriver extends JenaDriver {
    private static final Logger LOGGER = LoggerFactory.getLogger(TDBDriver.class);

    /**
     * Constant for the TDB driver prefix, this is appended to the base
     * {@link JenaDriver#DRIVER_PREFIX} to form the URL prefix for JDBC
     * Connection URLs for this driver
     */
    public static final String TDB_DRIVER_PREFIX = "tdb:";

    /**
     * Constant for driver parameter that sets the location of the TDB dataset
     */
    public static final String PARAM_LOCATION = "location";

    /**
     * Constant for special value which may be used as the value of the
     * {@code location} parameter ({@link #PARAM_LOCATION}) to indicate that a
     * pure in-memory dataset is desired, this should not be used for anything
     * other than trivial testing
     */
    public static final String LOCATION_MEM = "memory";

    /**
     * Constant for driver parameter that sets whether the TDB dataset must
     * already exist
     */
    public static final String PARAM_MUST_EXIST = "must-exist";

    /**
     * Static initializer block which ensures the driver gets registered
     */
    static {
        try {
            ARQ.init();
            TDB.init();
            register();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register Jena TDB JDBC Driver", e);
        }
    }

    /**
     * Registers the driver with the JDBC {@link DriverManager}
     * 
     * @throws SQLException
     *             Thrown if the driver cannot be registered
     */
    public static synchronized void register() throws SQLException {
        DriverManager.registerDriver(new TDBDriver());
    }

    /**
     * Creates a new TDB driver
     */
    public TDBDriver() {
        super(0, 1, TDB_DRIVER_PREFIX);
    }

    @Override
    protected JenaConnection connect(Properties props, int compatibilityLevel) throws SQLException {
        String location = props.getProperty(PARAM_LOCATION);
        if (location == null)
            throw new SQLException("Required connection parameter " + PARAM_LOCATION
                    + " is not present in the connection URL or the provided Properties object");

        // Determine location
        boolean useMem = this.isSetToValue(props, PARAM_LOCATION, LOCATION_MEM);
        File loc = new File(location);
        if (useMem) {
            LOGGER.warn("TDB Driver connection string specifies use of a pure in-memory dataset, this is not recommended for anything other than basic testing");
        } else {
            if (!loc.isAbsolute()) {
                LOGGER.warn("TDB Driver connection string specifies location " + loc.getAbsolutePath()
                        + ", if this was not the expected location consider using an absolute instead of a relative path");
            } else {
                LOGGER.info("TDB Driver connection string specifies location " + loc.getAbsolutePath());
            }
        }

        // Validate location if required
        if (this.isTrue(props, PARAM_MUST_EXIST) && !useMem) {
            if (!loc.exists()) {
                throw new SQLException("TDB Driver connection string specifies location " + loc.getAbsolutePath()
                        + " which does not exist, correct the " + PARAM_LOCATION + " parameter or set the " + PARAM_MUST_EXIST
                        + " parameter to false");
            } else if (!loc.isDirectory()) {
                throw new SQLException("TDB Driver connection string specifies location " + loc.getAbsolutePath()
                        + " which is not a directory, correct the " + PARAM_LOCATION + " parameter or set the "
                        + PARAM_MUST_EXIST + " parameter to false");
            }
        }

        // Open the TDB dataset
        try {
            Dataset tdb = useMem ? TDBFactory.createDataset() : TDBFactory.createDataset(location);

            // Return a new connection for the TDB dataset
            return new TDBConnection(tdb, ResultSet.HOLD_CURSORS_OVER_COMMIT, true, compatibilityLevel);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Unexpected error establishing TDB driver connection, see inner exception for details", e);
        }
    }

    @Override
    protected DriverPropertyInfo[] getPropertyInfo(Properties connProps, List<DriverPropertyInfo> baseDriverProps) {
        DriverPropertyInfo[] driverProps = new DriverPropertyInfo[2 + baseDriverProps.size()];
        this.copyBaseProperties(driverProps, baseDriverProps, 2);

        // Location parameter
        driverProps[0] = new DriverPropertyInfo(PARAM_LOCATION, connProps.getProperty(PARAM_LOCATION));
        driverProps[0].required = true;
        driverProps[0].description = "Sets the location of a TDB dataset, should be a file system path.  The value "
                + LOCATION_MEM + " may be used for a non-persistent in-memory dataset but this should only be used for testing";

        // Must Exist parameter
        driverProps[1] = new DriverPropertyInfo(PARAM_MUST_EXIST, connProps.getProperty(PARAM_MUST_EXIST));
        driverProps[1].required = false;
        driverProps[1].choices = new String[] { "true", "false" };
        driverProps[1].description = "If set to true requests that the driver check whether the " + PARAM_LOCATION
                + " parameter refers to an existing location before establishing a connection";

        return driverProps;
    }

}
