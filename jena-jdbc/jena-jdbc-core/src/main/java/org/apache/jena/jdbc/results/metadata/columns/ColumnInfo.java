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

package org.apache.jena.jdbc.results.metadata.columns;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.hp.hpl.jena.graph.Node;

/**
 * Abstract class for representing column information
 * 
 */
public abstract class ColumnInfo {

    private String label, className = Object.class.getCanonicalName(), typeName = Node.class.getCanonicalName();
    private int displaySize = Integer.MAX_VALUE, type = Types.JAVA_OBJECT, precision = 0, scale = 0,
            nullable = ResultSetMetaData.columnNoNulls;
    private boolean signed = false;

    /**
     * Creates new column information
     * 
     * @param label
     *            Column Label
     * @param type
     *            JDBC Type
     * @throws SQLException
     */
    public ColumnInfo(String label, int type) throws SQLException {
        if (label == null)
            throw new SQLException("Column label cannot be null");
        this.label = label;
        this.type = type;
    }

    /**
     * Gets the column label
     * 
     * @return Label
     */
    public final String getLabel() {
        return this.label;
    }

    /**
     * Gets the class name for the column
     * 
     * @return Class name
     */
    public final String getClassName() {
        return this.className;
    }

    /**
     * Sets the class name for the column
     * 
     * @param className
     *            Class name
     */
    protected void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets the display size for the column
     * 
     * @return Display size
     */
    public final int getDisplaySize() {
        return this.displaySize;
    }

    /**
     * Sets the display size for the column
     * 
     * @param size
     *            Display size
     */
    protected final void setDisplaySize(int size) {
        this.displaySize = size;
    }

    /**
     * Gets the JDBC type for the column
     * 
     * @return JDBC type, a value from the constants in {@link Types}
     */
    public final int getType() {
        return this.type;
    }

    /**
     * Gets the underlying database type name
     * 
     * @return Type name
     */
    public final String getTypeName() {
        return this.typeName;
    }

    /**
     * Sets the underlying database type name
     * 
     * @param typeName
     *            Type name
     */
    protected final void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Gets the precision
     * 
     * @return Precision
     */
    public final int getPrecision() {
        return this.precision;
    }

    /**
     * Sets the precision
     * 
     * @param precision
     *            Precision
     */
    protected final void setPrecision(int precision) {
        this.precision = precision;
    }

    /**
     * Gets the scale
     * 
     * @return Scale
     */
    public final int getScale() {
        return this.scale;
    }

    /**
     * Sets the scale
     * 
     * @param scale
     *            Scale
     */
    protected final void setScale(int scale) {
        this.scale = scale;
    }

    /**
     * Gets whether the column is nullable
     * 
     * @return Nullability of the column
     */
    public final int getNullability() {
        return this.nullable;
    }

    /**
     * Sets the nullability of the column
     * 
     * @param nullable
     *            Nullability
     */
    protected final void setNullable(int nullable) {
        this.nullable = nullable;
    }

    /**
     * Gets whether the column is case sensitive
     * 
     * @return True if case sensitive, false otherwise
     */
    public boolean isCaseSensitive() {
        // Most types in RDF/SPARQL are subject to case sensitivity especially
        // when talking strict RDF equality semantics
        return true;
    }

    /**
     * Gets whether the column represents a currency type
     * 
     * @return True if a currency type, false otherwise
     */
    public boolean isCurrency() {
        // No specific currency type in RDF/SPARQL
        return false;
    }

    /**
     * Gets whether the column is an auto-increment type
     * 
     * @return True if auto-increment, false otherwise
     */
    public boolean isAutoIncrement() {
        // SPARQL engines don't have a notion of auto-increment
        return false;
    }

    /**
     * Gets whether the column is writable
     * 
     * @return True if writable, false otherwise
     */
    public boolean isWritable() {
        // All Jena JDBC results are read-only currently
        return false;
    }

    /**
     * Gets whether the column is read-only
     * 
     * @return True if read-only, false otherwise
     */
    public boolean isReadOnly() {
        // All Jena JDBC results are read-only currently
        return true;
    }

    /**
     * Gets whether the column is searchable
     * 
     * @return True if searchable, false otherwise
     */
    public boolean isSearchable() {
        // Assume all columns are searchable since the entire RDF dataset is
        // searchable
        return true;
    }

    /**
     * Gets whether the column is signed
     * 
     * @return True if signed, false otherwise
     */
    public final boolean isSigned() {
        return this.signed;
    }

    /**
     * Sets whether the column is signed
     * 
     * @param signed
     */
    protected final void setSigned(boolean signed) {
        this.signed = signed;
    }
}
