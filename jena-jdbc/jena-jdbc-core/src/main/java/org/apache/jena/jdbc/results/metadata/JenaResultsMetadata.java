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

package org.apache.jena.jdbc.results.metadata;

import java.sql.SQLException;

import org.apache.jena.jdbc.results.JenaResultSet;
import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;

/**
 * Abstract implementation of result set metadata for Jena JDBC result sets,
 * specially for {@link JenaResultSet} implementations.
 * <p>
 * This differs from the base {@link AbstractResultsMetadata} in that it
 * provides a strongly typed method for accessing the {@link JenaResultSet}
 * which may be useful for some advanced metadata implementations.
 * </p>
 * 
 */
public abstract class JenaResultsMetadata extends AbstractResultsMetadata {

    private JenaResultSet jenaResults;

    /**
     * Creates new result set metadata
     * 
     * @param results
     *            Result Set
     * @param columns
     *            Column Information
     * @throws SQLException
     */
    public JenaResultsMetadata(JenaResultSet results, ColumnInfo[] columns) throws SQLException {
        super(results, columns);
        this.jenaResults = results;
    }

    /**
     * Gets the associated Jena Result Set
     * @return Jena Result Set
     */
    protected final JenaResultSet getJenaResultSet() {
        return this.jenaResults;
    }

}
