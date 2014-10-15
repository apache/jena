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

package org.apache.jena.jdbc.metadata.results;

import java.sql.SQLException;
import org.apache.jena.jdbc.results.metadata.AbstractResultsMetadata;
import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;

/**
 * Abstract implementation of result set metadata for Jena JDBC metadata result sets
 * 
 */
public class MetaResultSetMetadata extends AbstractResultsMetadata {
    /**
     * Creates new result set metadata
     * 
     * @param results
     *            Result Set
     * @param columns
     *            Column Information
     * @throws SQLException
     */
    public MetaResultSetMetadata(MetaResultSet results, ColumnInfo[] columns) throws SQLException {
        super(results, columns);
    }
}
