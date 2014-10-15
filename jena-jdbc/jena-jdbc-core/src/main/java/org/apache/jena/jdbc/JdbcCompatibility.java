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

package org.apache.jena.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.jena.jdbc.results.metadata.columns.BooleanColumn;
import org.apache.jena.jdbc.results.metadata.columns.ByteColumn;
import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;
import org.apache.jena.jdbc.results.metadata.columns.DateColumn;
import org.apache.jena.jdbc.results.metadata.columns.DateTimeColumn;
import org.apache.jena.jdbc.results.metadata.columns.DecimalColumn;
import org.apache.jena.jdbc.results.metadata.columns.DoubleColumn;
import org.apache.jena.jdbc.results.metadata.columns.FloatColumn;
import org.apache.jena.jdbc.results.metadata.columns.LongIntegerColumn;
import org.apache.jena.jdbc.results.metadata.columns.IntegerColumn;
import org.apache.jena.jdbc.results.metadata.columns.StringColumn;
import org.apache.jena.jdbc.results.metadata.columns.TimeColumn;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * <p>
 * Class containing constants and helper methods related to JDBC compatibility
 * </p>
 * <h3>Understanding Compatibility Levels</h3>
 * <p>
 * Since JDBC is very SQL centric API by definition shoe-horning SPARQL into it
 * has some caveats and provisos, the aim of this class it to provide some level
 * of configurability of how nice we will try to play with JDBC. We provide the
 * notion of a configurable compatibility level, by definition we use the
 * {@link #MEDIUM} compatibility level, see the documentation on the constants
 * and helper methods to understand exactly what each level means.
 * </p>
 * 
 */
public class JdbcCompatibility {
    
    /**
     * Private constructor prevents instantiation
     */
    private JdbcCompatibility() {
    }

    /**
     * Constant for low JDBC compatibility level
     * <p>
     * This is the level you should use when you know you are accessing a SPARQL
     * source and are able to cope with the Jena/ARQ representation of RDF terms
     * natively.
     * </p>
     * <h3>Behavior Specifies</h3>
     * <ul>
     * <li>Column Typing - All result set columns are reported as being as typed
     * as {@link Types#JAVA_OBJECT} and Java type is the ARQ {@link Node} type.</li>
     * </ul>
     */
    public static final int LOW = 1;

    /**
     * Constant for medium JDBC compatibility level
     * <p>
     * This is the default compatibility level, we will make some effort to be
     * compatible with JDBC but these efforts will not be perfect.
     * </p>
     * <h3>Behavior Specifics</h3>
     * <ul>
     * <li>Column Typing - All result set columns are reported as being as typed
     * as {@link Types#NVARCHAR} and Java type is {@link String}.</li>
     * </ul>
     */
    public static final int MEDIUM = 5;

    /**
     * Constant for high JDBC compatibility level
     * <p>
     * This is the highest compatibility level, we will do our best to be
     * compatible with JDBC however these efforts may still not be perfect.
     * </p>
     * <h3>Behavior Specifics</h3>
     * <ul>
     * <li>Column Typing - Result set columns are typed by inspecting the first
     * row of the data so native JDBC types like {@link Types#INTEGER} and so
     * forth may be reported depending on the query.</li>
     * </ul>
     */
    public static final int HIGH = 9;

    /**
     * Constant for default JDBC compatibility which is set to {@link #MEDIUM}
     */
    public static final int DEFAULT = MEDIUM;

    /**
     * Normalizes the compatibility level given to be within the acceptable
     * range of 1-9
     * 
     * @param level
     *            Level
     * @return Normalized level
     */
    public static int normalizeLevel(int level) {
        if (level < LOW) {
            return LOW;
        } else if (level > HIGH) {
            return HIGH;
        } else {
            return level;
        }
    }

    /**
     * Returns whether a result set is expected to determine the column types
     * from the returned data
     * 
     * @param level
     *            Desired compatibility level
     * @return True if column types should be detected, false otherwise
     */
    public static boolean shouldDetectColumnTypes(int level) {
        level = normalizeLevel(level);
        return level == HIGH;
    }

    /**
     * Returns whether a result set is expected to type returned columns as
     * strings
     * 
     * @param level
     *            Desired compatibility level
     * @return True if columns should be typed as string, false otherwise
     */
    public static boolean shouldTypeColumnsAsString(int level) {
        level = normalizeLevel(level);
        return level >= MEDIUM && level < HIGH;
    }

    /**
     * Detects the column type information based on an example value
     * 
     * @param var
     *            Variable Name i.e. the column label
     * @param value
     *            Example value
     * @param allowsNulls
     *            Whether the result set we are detecting the type for allows
     *            null values in this column
     * @return Column Information
     * @throws SQLException
     *             Thrown if the column type cannot be detected, this should
     *             only occur if you state that the column does not allow nulls
     *             and then provide a null example value
     */
    public static ColumnInfo detectColumnType(String var, Node value, boolean allowsNulls) throws SQLException {
        if (allowsNulls && value == null) {
            // If we are allowing nulls and the value is null just type the
            // column as string
            return new StringColumn(var, ResultSetMetaData.columnNullable);
        } else if (!allowsNulls && value == null) {
            throw new SQLException("Unable to determine column type, column is non-nullable but example value is null");
        } else {
            // We know we have a non-null value so now we need to determine the
            // column type appropriately
            int nullable = allowsNulls ? ResultSetMetaData.columnNullable : ResultSetMetaData.columnNoNulls;
            if (value.isBlank()) {
                // Type blank nodes as strings
                return new StringColumn(var, nullable);
            } else if (value.isURI()) {
                // Type URIs as strings
                // TODO: Does JDBC have a URL type?
                return new StringColumn(var, nullable);
            } else if (value.isLiteral()) {
                // Literals will be typed based on the declared data type where
                // applicable
                String dtUri = value.getLiteralDatatypeURI();
                if (dtUri != null) {
                    // Is a typed literal
                    return selectColumnType(var, dtUri, nullable);
                } else {
                    // Untyped literals are typed as strings
                    return new StringColumn(var, nullable);
                }
            } else {
                // Anything else we treat as a string
                return new StringColumn(var, nullable);
            }
        }
    }

    /**
     * Select the column type based on the data type URI
     * 
     * @param var
     *            Variable Name i.e. the column label
     * @param dtUri
     *            Data type URI
     * @param nullable
     *            Whether the column is nullable
     * @return Column type
     * @throws SQLException
     */
    private static ColumnInfo selectColumnType(String var, String dtUri, int nullable) throws SQLException {
        if (dtUri.equals(XSD.date.toString())) {
            // Date column
            return new DateColumn(var, nullable);
        } else if (dtUri.equals(XSD.dateTime.toString())) {
            // Date time column
            return new DateTimeColumn(var, nullable);
        } else if (dtUri.equals(XSD.decimal.toString())) {
            // Decimal column
            return new DecimalColumn(var, nullable);
        } else if (dtUri.equals(XSD.duration.toString())) {
            // JDBC has no notion of durations so return as a string
            return new StringColumn(var, nullable);
        } else if (dtUri.equals(XSD.integer.toString()) || dtUri.equals(XSD.xint.toString())
                || dtUri.equals(XSD.xlong.toString())) {
            // Integer column
            return new LongIntegerColumn(var, nullable, true);
        } else if (dtUri.equals(XSD.unsignedInt.toString()) || dtUri.equals(XSD.unsignedLong.toString())) {
            // Unsigned Integer column
            return new LongIntegerColumn(var, nullable, false);
        } else if (dtUri.equals(XSD.positiveInteger.toString()) || dtUri.equals(XSD.nonNegativeInteger.toString())) {
            // Unsigned Integer column
            return new LongIntegerColumn(var, nullable, false);
        } else if (dtUri.equals(XSD.nonPositiveInteger.toString()) || dtUri.equals(XSD.negativeInteger.toString())) {
            // Signed Integer column
            return new LongIntegerColumn(var, nullable, true);
        } else if (dtUri.equals(XSD.xshort.toString())) {
            // Short Integer column
            return new IntegerColumn(var, nullable, true);
        } else if (dtUri.equals(XSD.unsignedShort.toString())) {
            // Unsigned Short Integer column
            return new IntegerColumn(var, nullable, false);
        } else if (dtUri.equals(XSD.xbyte.toString())) {
            // Signed Byte
            return new ByteColumn(var, nullable, true);
        } else if (dtUri.equals(XSD.unsignedByte.toString())) {
            // Unsigned Byte
            return new ByteColumn(var, nullable, false);
        } else if (dtUri.equals(XSD.time.toString())) {
            // Time column
            return new TimeColumn(var, nullable);
        } else if (dtUri.equals(XSD.xboolean.toString())) {
            // Boolean column
            return new BooleanColumn(var, nullable);
        } else if (dtUri.equals(XSD.xdouble.toString())) {
            // Double column
            return new DoubleColumn(var, nullable);
        } else if (dtUri.equals(XSD.xfloat.toString())) {
            // Float column
            return new FloatColumn(var, nullable);
        } else if (dtUri.equals(XSD.xstring.toString())) {
            // String column
            return new StringColumn(var, nullable);
        } else {
            // Anything else we'll treat as a String
            return new StringColumn(var, nullable);
        }
    }

    /**
     * Parses the JDBC compatibility level from the given object and normalizes
     * it if necessary. If the given object is null or does not contain a valid
     * integer value (or can be parsed as such) then the returned compatibility
     * level will be {@link #DEFAULT}
     * 
     * @param object
     *            Object to parse
     * @return Normalized JDBC compatibility level
     */
    public static int parseLevel(Object object) {
        if (object == null) {
            return DEFAULT;
        } else if (object instanceof Integer) {
            return normalizeLevel((Integer) object);
        } else {
            try {
                int level = Integer.parseInt(object.toString());
                return normalizeLevel(level);
            } catch (NumberFormatException e) {
                return DEFAULT;
            }
        }
    }
}
