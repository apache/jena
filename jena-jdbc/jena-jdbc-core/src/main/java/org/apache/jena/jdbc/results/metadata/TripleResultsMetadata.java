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
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Iterator;

import org.apache.jena.atlas.iterator.PeekIterator;
import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.results.JenaResultSet;
import org.apache.jena.jdbc.results.TripleIteratorResults;
import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;
import org.apache.jena.jdbc.results.metadata.columns.SparqlColumnInfo;
import org.apache.jena.jdbc.results.metadata.columns.StringColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * Result set metadata for {@link TripleIteratorResults} instances
 * 
 */
public class TripleResultsMetadata extends JenaResultsMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(TripleResultsMetadata.class);

    /**
     * Constant for the default subject column label
     */
    public static final String COLUMN_LABEL_SUBJECT = "Subject";
    /**
     * Constant for the default predicate column label
     */
    public static final String COLUMN_LABEL_PREDICATE = "Predicate";
    /**
     * Constant for the default object column label
     */
    public static final String COLUMN_LABEL_OBJECT = "Object";
    /**
     * Constant for the subject column index (assuming no columns are omitted)
     */
    public static final int COLUMN_INDEX_SUBJECT = 1;
    /**
     * Constant for the predicate column index (assuming no columns are omitted)
     */
    public static final int COLUMN_INDEX_PREDICATE = 2;
    /**
     * Constant for the object column index (assuming no columns are omitted)
     */
    public static final int COLUMN_INDEX_OBJECT = 3;
    /**
     * Constant for the number of columns in triple results
     */
    public static final int NUM_COLUMNS = 3;

    private PeekIterator<Triple> ts;
    private String subjColumn, predColumn, objColumn;

    /**
     * Gets the columns for CONSTRUCT/DESCRIBE results
     * 
     * @param results
     *            Results
     * @param ts
     *            Underlying triples
     * 
     * @return Column Information
     * @throws SQLException
     */
    private static ColumnInfo[] makeColumns(JenaResultSet results, PeekIterator<Triple> ts) throws SQLException {
        return makeColumns(results, ts, COLUMN_LABEL_SUBJECT, COLUMN_LABEL_PREDICATE, COLUMN_LABEL_OBJECT);
    }

    /**
     * Gets the columns for CONSTRUCT/DESCRIBE results
     * 
     * @param results
     *            Results
     * @param ts
     *            Underlying triples
     * @param subjLabel
     *            Label for subject column, use {@code null} to omit the subject
     *            column
     * @param predLabel
     *            Label for predicate column, use {@code null} to omit the
     *            predicate column
     * @param objLabel
     *            Label for object column, use {@code null} to omit the object
     *            column
     * 
     * @return Column Information
     * @throws SQLException
     */
    private static ColumnInfo[] makeColumns(JenaResultSet results, PeekIterator<Triple> ts, String subjLabel, String predLabel,
            String objLabel) throws SQLException {
        int numColumns = 0;
        if (subjLabel != null)
            numColumns++;
        if (predLabel != null)
            numColumns++;
        if (objLabel != null)
            numColumns++;
        ColumnInfo[] columns = new ColumnInfo[numColumns];

        // Figure out column names
        String[] names = new String[numColumns];
        names[0] = subjLabel != null ? subjLabel : (predLabel != null ? predLabel : objLabel);
        if (numColumns > 1) {
            names[1] = subjLabel != null && predLabel != null ? predLabel : objLabel;
        }
        if (numColumns == 3) {
            names[2] = objLabel;
        }

        int level = JdbcCompatibility.normalizeLevel(results.getJdbcCompatibilityLevel());
        boolean columnsAsStrings = JdbcCompatibility.shouldTypeColumnsAsString(level);
        boolean columnsDetected = JdbcCompatibility.shouldDetectColumnTypes(level);

        Triple t = null;
        Node[] values = new Node[numColumns];
        if (columnsDetected) {
            if (ts.hasNext()) {
                // Need to peek the first Triple and grab appropriate nodes
                t = ts.peek();
                if (numColumns == NUM_COLUMNS) {
                    values[0] = t.getSubject();
                    values[1] = t.getPredicate();
                    values[2] = t.getObject();
                } else {
                    values[0] = subjLabel != null ? t.getSubject() : (predLabel != null ? t.getPredicate() : t.getObject());
                    if (numColumns > 1) {
                        values[1] = subjLabel != null && predLabel != null ? t.getPredicate() : t.getObject();
                    }
                }
            } else {
                // If we were supposed to detect columns but there is no data
                // available then we will just fallback to typing everything as
                // strings
                columnsAsStrings = true;
                columnsDetected = false;
            }
        }

        for (int i = 0; i < columns.length; i++) {
            if (!columnsAsStrings && !columnsDetected) {
                // Low compatibility, report columns as being typed as
                // JAVA_OBJECT with ARQ Node as the column class
                columns[i] = new SparqlColumnInfo(names[i], Types.JAVA_OBJECT, columnNoNulls);
                LOGGER.info("Low JDBC compatibility, column " + names[i] + " is being typed as Node");
            } else if (columnsAsStrings) {
                // Medium compatibility, report columns as being typed as
                // NVARChar with String as the column class
                columns[i] = new StringColumn(names[i], columnNoNulls);
                LOGGER.info("Medium JDBC compatibility, column " + names[i] + " is being typed as String");
            } else if (columnsDetected) {
                // High compatibility, detect columns types based on first row
                // of results
                columns[i] = JdbcCompatibility.detectColumnType(names[i], values[i], false);
                LOGGER.info("High compatibility, column " + names[i] + " was detected as being of type "
                        + columns[i].getClassName());
            } else {
                throw new SQLFeatureNotSupportedException("Unknown JDBC compatibility level was set");
            }
        }

        return columns;
    }

    /**
     * Creates new results metadata for triple (CONSTRUCT/DESCRIBE) results
     * 
     * @param results
     *            Result Set
     * @param ts
     *            Triple iterator
     * @throws SQLException
     *             Thrown if the metadata cannot be created
     */
    public TripleResultsMetadata(JenaResultSet results, PeekIterator<Triple> ts) throws SQLException {
        super(results, makeColumns(results, ts));
        this.subjColumn = COLUMN_LABEL_SUBJECT;
        this.predColumn = COLUMN_LABEL_PREDICATE;
        this.objColumn = COLUMN_LABEL_OBJECT;
        this.ts = ts;
    }

    /**
     * Creates new results metadata for triple (CONSTRUCT/DESCRIBE) results
     * 
     * @param results
     *            Result Set
     * @param ts
     *            Triple iterator
     * @throws SQLException
     *             Thrown if the metadata cannot be created
     */
    public TripleResultsMetadata(JenaResultSet results, Iterator<Triple> ts) throws SQLException {
        this(results, PeekIterator.create(ts));
    }

    /**
     * Creates new results metadata for triple (CONSTRUCT/DESCRIBE) results
     * 
     * @param results
     *            Result Set
     * @param ts
     *            Triple iterator
     * @param subjLabel
     *            Label for subject column, use {@code null} to omit the subject
     *            column
     * @param predLabel
     *            Label for predicate column, use {@code null} to omit the
     *            predicate column
     * @param objLabel
     *            Label for object column, use {@code null} to omit the object
     *            column
     * @throws SQLException
     *             Thrown if the metadata cannot be created
     */
    public TripleResultsMetadata(JenaResultSet results, PeekIterator<Triple> ts, String subjLabel, String predLabel,
            String objLabel) throws SQLException {
        super(results, makeColumns(results, ts, subjLabel, predLabel, objLabel));
        this.subjColumn = subjLabel;
        this.predColumn = predLabel;
        this.objColumn = objLabel;
        this.ts = ts;
    }

    /**
     * Creates new results metadata for triple (CONSTRUCT/DESCRIBE) results
     * 
     * @param results
     *            Result Set
     * @param ts
     *            Triple iterator
     * @param subjLabel
     *            Label for subject column, use {@code null} to omit the subject
     *            column
     * @param predLabel
     *            Label for predicate column, use {@code null} to omit the
     *            predicate column
     * @param objLabel
     *            Label for object column, use {@code null} to omit the object
     *            column
     * @throws SQLException
     *             Thrown if the metadata cannot be created
     */
    public TripleResultsMetadata(JenaResultSet results, Iterator<Triple> ts, String subjLabel, String predLabel, String objLabel)
            throws SQLException {
        this(results, PeekIterator.create(ts), subjLabel, predLabel, objLabel);
    }

    /**
     * Creates new results metadata for triple (CONSTRUCT/DESCRIBE) results
     * 
     * @param metadata
     *            Metadata
     * @param subjLabel
     *            Label for subject column, use {@code null} to omit the subject
     *            column
     * @param predLabel
     *            Label for predicate column, use {@code null} to omit the
     *            predicate column
     * @param objLabel
     *            Label for object column, use {@code null} to omit the object
     *            column
     * @throws SQLException
     *             Thrown if the metadata cannot be created
     */
    public TripleResultsMetadata(TripleResultsMetadata metadata, String subjLabel, String predLabel, String objLabel)
            throws SQLException {
        this(metadata.getJenaResultSet(), metadata.ts, subjLabel, predLabel, objLabel);
    }

    /**
     * Creates new results metadata for triple (CONSTRUCT/DESCRIBE) results
     * 
     * @param metadata
     *            Metadata
     * @param columns
     *            Column Information
     * @throws SQLException
     *             Thrown if the metadata cannot be created
     */
    public TripleResultsMetadata(TripleResultsMetadata metadata, ColumnInfo[] columns) throws SQLException {
        super(metadata.getJenaResultSet(), columns);
        if (metadata.getColumnCount() != columns.length)
            throw new SQLException(
                    "Must provide same number of columns as in original metadata, to filter down included columns use the TripleResultsMetadata(TripleResultsMetadata, String, String, String) constructor first");
        this.subjColumn = metadata.getSubjectColumnLabel();
        this.predColumn = metadata.getPredicateColumnLabel();
        this.objColumn = metadata.getObjectColumnLabel();
    }

    /**
     * Gets the subject column label
     * 
     * @return Column label or {@code null} if the column is omitted
     */
    public String getSubjectColumnLabel() {
        return this.subjColumn;
    }

    /**
     * Gets the predicate column label
     * 
     * @return Column label or {@code null} if the column is omitted
     */
    public String getPredicateColumnLabel() {
        return this.predColumn;
    }

    /**
     * Gets the object column label
     * 
     * @return Column label or {@code null} if the column is omitted
     */
    public String getObjectColumnLabel() {
        return this.objColumn;
    }
}
