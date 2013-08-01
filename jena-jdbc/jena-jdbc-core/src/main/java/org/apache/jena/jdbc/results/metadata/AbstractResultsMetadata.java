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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;

/**
 * Abstract implementation of JDBC result set metadata
 * 
 */
public class AbstractResultsMetadata implements ResultSetMetaData {

    protected ResultSet results;
    protected List<ColumnInfo> columns = new ArrayList<ColumnInfo>();

    /**
     * Abstract implementation of result set metadata
     * 
     * @param results
     *            Result Set
     * @param columns
     *            Column information
     * @throws SQLException
     *             Thrown if metadata cannot be created
     */
    public AbstractResultsMetadata(ResultSet results, ColumnInfo[] columns) throws SQLException {
        if (results == null)
            throw new SQLException("Result Set cannot be null");
        this.results = results;
        for (ColumnInfo column : columns) {
            this.columns.add(column);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        if (this.results != null) {
            return this.results.getStatement().getConnection().getCatalog();
        } else {
            return "";
        }
    }

    protected final ColumnInfo getColumnInfo(int column) throws SQLException {
        // Remember JDBC columns use a 1 based index
        if (column >= 1 && column <= this.columns.size()) {
            return this.columns.get(column - 1);
        } else {
            throw new SQLException("Column Index is out of bounds");
        }
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return this.getColumnInfo(column).getClassName();
    }

    @Override
    public int getColumnCount() throws SQLException {
        return this.columns.size();
    }

    /**
     * Gets a columns display size
     * <p>
     * Since RDF imposes no maximum on the size of a term this may be
     * arbitrarily large hence {@link Integer#MAX_VALUE} is returned, users
     * should not rely on this method to give them accurate information for UI
     * usage.
     * </p>
     */
    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return this.getColumnInfo(column).getDisplaySize();
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return this.getColumnInfo(column).getLabel();
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return this.getColumnInfo(column).getLabel();
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return this.getColumnInfo(column).getType();
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return this.getColumnInfo(column).getTypeName();
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return this.getColumnInfo(column).getPrecision();
    }

    @Override
    public int getScale(int column) throws SQLException {
        return this.getColumnInfo(column).getScale();
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        // Not applicable so return empty string
        return "";
    }

    @Override
    public String getTableName(int column) throws SQLException {
        // Not applicable so return empty string
        return "";
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return this.getColumnInfo(column).isAutoIncrement();
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return this.getColumnInfo(column).isCaseSensitive();
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return this.getColumnInfo(column).isCurrency();
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return this.isWritable(column);
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return this.getColumnInfo(column).getNullability();
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return this.getColumnInfo(column).isReadOnly();
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return this.getColumnInfo(column).isSearchable();
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return this.getColumnInfo(column).isSigned();
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return this.getColumnInfo(column).isWritable();
    }

    /**
     * Gets a copy of the raw underlying column information
     * 
     * @return Column Information
     * @throws SQLException
     */
    public List<ColumnInfo> getJenaColumnInfo() throws SQLException {
        return new ArrayList<ColumnInfo>(this.columns);
    }

}