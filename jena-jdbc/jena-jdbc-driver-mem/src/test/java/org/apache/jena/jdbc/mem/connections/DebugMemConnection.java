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

package org.apache.jena.jdbc.mem.connections;

import java.sql.SQLException;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.mem.connections.MemConnection;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

/**
 * A test only variant of {@link MemConnection} which allows the dataset to be changed on the fly
 *
 */
public class DebugMemConnection extends MemConnection {
    
    /**
     * Creates a debug dataset connection
     * @throws SQLException
     */
    public DebugMemConnection() throws SQLException {
        this(DatasetFactory.createMem());
    }

    /**
     * Creates a debug dataset connection
     * @param ds Dataset
     * @throws SQLException
     */
    public DebugMemConnection(Dataset ds) throws SQLException {
        super(ds, JenaConnection.DEFAULT_HOLDABILITY, JenaConnection.DEFAULT_AUTO_COMMIT, JenaConnection.DEFAULT_ISOLATION_LEVEL, JdbcCompatibility.DEFAULT);
    }

    /**
     * Sets the Jena dataset in use
     * @param ds Dataset
     */
    public void setJenaDataset(Dataset ds) {
        this.ds = ds;
    }
}
