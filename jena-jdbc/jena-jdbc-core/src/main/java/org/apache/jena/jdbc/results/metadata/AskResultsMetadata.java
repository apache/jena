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
import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.results.AskResults;
import org.apache.jena.jdbc.results.JenaResultSet;
import org.apache.jena.jdbc.results.metadata.columns.BooleanColumn;
import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;

/**
 * Meta data for {@link AskResults}
 * <p>
 * Note that ASK results are something of a special case because they contain
 * only a single column and we know exactly what the type of the column is, with
 * other forms of results we don't have this luxury and so the
 * {@link JdbcCompatibility} levels are used to determine how we report types.
 * </p>
 * 
 */
public class AskResultsMetadata extends JenaResultsMetadata {

    /**
     * Constant for the default ASK results column label
     */
    public static final String COLUMN_LABEL_ASK = "ASK";

    /**
     * Constant for the only column index for ASK queries
     */
    public static final int COLUMN_INDEX_ASK = 1;

    private static final ColumnInfo[] getColumns() throws SQLException {
        return getColumns(COLUMN_LABEL_ASK);
    }

    private static final ColumnInfo[] getColumns(String label) throws SQLException {
        if (label == null)
            label = COLUMN_LABEL_ASK;
        return new ColumnInfo[] { new BooleanColumn(label, columnNoNulls) };
    }

    /**
     * Creates new ASK results metadata
     * 
     * @param results
     *            Results
     * @throws SQLException
     *             Thrown if the metadata cannot be created
     */
    public AskResultsMetadata(JenaResultSet results) throws SQLException {
        super(results, AskResultsMetadata.getColumns());
    }

    /**
     * Creates new ASK results metadata
     * 
     * @param metadata
     *            Metadata
     * @param label
     *            Label to give the single column
     * 
     * @throws SQLException
     *             Thrown if the metadata cannot be created
     */
    public AskResultsMetadata(AskResultsMetadata metadata, String label) throws SQLException {
        super(metadata.getJenaResultSet(), AskResultsMetadata.getColumns(label));
    }
}
