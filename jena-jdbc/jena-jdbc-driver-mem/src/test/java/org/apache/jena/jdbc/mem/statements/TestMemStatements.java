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

package org.apache.jena.jdbc.mem.statements;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.mem.connections.MemConnection;
import org.apache.jena.jdbc.statements.AbstractJenaStatementTests;

import com.hp.hpl.jena.query.DatasetFactory;

/**
 * Tests for statements produced by {@link MemConnection} instances
 *
 */
public class TestMemStatements extends AbstractJenaStatementTests {

    @Override
    protected JenaConnection getConnection() throws SQLException {
        return new MemConnection(DatasetFactory.createMem(), JenaConnection.DEFAULT_HOLDABILITY, false, Connection.TRANSACTION_NONE, JdbcCompatibility.DEFAULT);
    }

}
