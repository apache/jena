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
package org.apache.jena.jdbc.preprocessing;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Interface for command pre-processors
 * <p>
 * Pre-processors provide an extension point within Jena JDBC which allows for
 * arbitrary manipulation of the incoming command text and the parsed SPARQL
 * queries and updates to be carried out. The intention of this is to provide a
 * mechanism by which users can modify the behavior of the module to deal with
 * any peculiarities in behavior that particular JDBC based tools may exhibit
 * when attempting to use them with Jena JDBC drivers.
 * </p>
 * 
 */
public interface CommandPreProcessor {
    
    /**
     * Initializes the pre-processor
     * <p>
     * Called when the pre-processor is first created, properties object provides access to all connection configuration parameters except password
     * </p>
     * @param props Connection properties
     * @throws SQLException Thrown if there is a problem initializing the pre-processor
     */
    public void initialize(Properties props) throws SQLException;
    
    /**
     * Pre-process incoming command text
     * <p>
     * This is invoked before Jena JDBC has attempted to determine whether the
     * text is a query/update. This allows an application to do textual clean
     * up/alteration of the incoming command if it so desires.
     * </p>
     * 
     * @param text
     *            Command Text
     * @return Command Text which may have been altered
     * @throws SQLException
     *             Thrown if pre-processing encounters an issue
     */
    public String preProcessCommandText(String text) throws SQLException;

    /**
     * Pre-process a query
     * <p>
     * This is invoked during query execution prior to Jena JDBC making any of
     * its own manipulations on the query e.g. using
     * {@link Statement#setMaxRows(int)} to add a {@code LIMIT} clause.
     * </p>
     * 
     * @param q
     *            Query
     * @return Query which may have been altered
     * @throws SQLException
     *             Thrown if pre-processing encounters an issue
     */
    public Query preProcessQuery(Query q) throws SQLException;

    /**
     * Pre-process an update
     * <p>
     * This is invoked during update execution prior to Jena JDBC making any of
     * its own manipulations on the update.
     * </p>
     * 
     * @param u
     *            Update
     * @return Update which may have been altered
     * @throws SQLException
     *             Thrown if pre-processing encounters an issue
     */
    public UpdateRequest preProcessUpdate(UpdateRequest u) throws SQLException;
}
