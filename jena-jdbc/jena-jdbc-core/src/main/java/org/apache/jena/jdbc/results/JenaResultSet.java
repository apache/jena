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

package org.apache.jena.jdbc.results;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Map;

import org.apache.jena.jdbc.JdbcCompatibility;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.statements.JenaStatement;
import org.apache.jena.jdbc.utils.JdbcNodeUtils;

import com.hp.hpl.jena.graph.Node;

/**
 * Abstract implementation of a JDBC Result Set which makes all update methods
 * throw {@link SQLFeatureNotSupportedException}
 * 
 */
public abstract class JenaResultSet implements ResultSet {

    private static final int DEFAULT_HOLDABILITY = ResultSet.CLOSE_CURSORS_AT_COMMIT;

    private SQLWarning warnings;
    private JenaStatement statement;
    private boolean wasNull = false;
    private int holdability = DEFAULT_HOLDABILITY;
    private int compatibilityLevel = JdbcCompatibility.DEFAULT;

    /**
     * Creates a new result set
     * 
     * @param statement
     *            Statement that originated the result set
     * @throws SQLException
     *             Thrown if the arguments are invalid
     */
    public JenaResultSet(JenaStatement statement) throws SQLException {
        if (statement == null)
            throw new SQLException("Statement for a Result Set cannot be null");
        this.statement = statement;
        this.compatibilityLevel = JdbcCompatibility
                .normalizeLevel(this.statement.getJdbcCompatibilityLevel());
    }

    /**
     * Gets the {@link JenaStatement} associated with the result set
     * 
     * @return Jena Statement
     */
    public JenaStatement getJenaStatement() {
        return this.statement;
    }

    /**
     * Gets the JDBC compatibility level to use for the result set, this will
     * reflect the compatibility level at the time the result set was created
     * not necessarily the current compatibility level of the backing
     * {@link JenaConnection}
     * 
     * @return JDBC compatibility level, see {@link JdbcCompatibility}
     */
    public int getJdbcCompatibilityLevel() {
        return this.compatibilityLevel;
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
    public void cancelRowUpdates() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public final void clearWarnings() throws SQLException {
        this.warnings = null;
    }

    @Override
    public void deleteRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public final int getHoldability() throws SQLException {
        return this.holdability;
    }

    @Override
    public final int getConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public abstract ResultSetMetaData getMetaData() throws SQLException;

    // Get Methods for things we do support

    /**
     * Helper method which retrieves the Node for the given index of the current
     * row
     * 
     * @param columnIndex
     *            Column Index
     * @return Node if there is a value, null if no value for the column
     * @throws SQLException
     *             Should be thrown if there is no current row, the column index
     *             is invalid or the result set is closed
     */
    protected Node getNode(int columnIndex) throws SQLException {
        return this.getNode(this.findColumnLabel(columnIndex));
    }

    /**
     * Helper method which derived classes must implement to map a column index
     * to a column label
     * 
     * @param columnIndex
     *            Column Index
     * @return Column Label
     * @throws SQLException
     *             Should be thrown if the column index is invalid
     */
    protected abstract String findColumnLabel(int columnIndex) throws SQLException;

    /**
     * Helper method which derived classes must implement to retrieve the Node
     * for the given column of the current row
     * 
     * @param columnLabel
     *            Column Label
     * @return Node if there is a value, null if no value for the column
     * @throws SQLException
     *             Should be thrown if there is no current row, the column label
     *             is invalid or the result set is closed
     */
    protected abstract Node getNode(String columnLabel) throws SQLException;

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return this.getBigDecimal(this.findColumnLabel(columnIndex));
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return null;
        } else {
            // Try to marshal into a decimal
            this.setNull(false);
            return JdbcNodeUtils.toDecimal(n);
        }
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return this.getBoolean(this.findColumnLabel(columnIndex));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return false;
        } else {
            // Try to marshal into a boolean
            this.setNull(false);
            return JdbcNodeUtils.toBoolean(n);
        }
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return this.getByte(this.findColumnLabel(columnIndex));
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return 0;
        } else {
            // Try to marshal into a byte
            this.setNull(false);
            return JdbcNodeUtils.toByte(n);
        }
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return this.getDate(this.findColumnLabel(columnIndex));
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return null;
        } else {
            // Try to marshal into a date
            this.setNull(false);
            return JdbcNodeUtils.toDate(n);
        }
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return this.getDouble(this.findColumnLabel(columnIndex));
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return 0;
        } else {
            // Try to marshal into a date
            this.setNull(false);
            return JdbcNodeUtils.toDouble(n);
        }
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return this.getFloat(this.findColumnLabel(columnIndex));
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return 0;
        } else {
            // Try to marshal into a date
            this.setNull(false);
            return JdbcNodeUtils.toFloat(n);
        }
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return this.getInt(this.findColumnLabel(columnIndex));
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return 0;
        } else {
            // Try to marshal into an integer
            this.setNull(false);
            return JdbcNodeUtils.toInt(n);
        }
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return this.getLong(this.findColumnLabel(columnIndex));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return 0;
        } else {
            // Try to marshal into an integer
            this.setNull(false);
            return JdbcNodeUtils.toLong(n);
        }
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return this.getNString(this.findColumnLabel(columnIndex));
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return null;
        } else {
            this.setNull(false);
            return JdbcNodeUtils.toString(n);
        }
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return this.getObject(this.findColumnLabel(columnIndex));
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return null;
        } else {
            // Need to marshal to an appropriate type based on declared JDBC
            // type of the column in order to comply with the JDBC semantics of
            // the getObject() method
            int jdbcType = this.getMetaData().getColumnType(this.findColumn(columnLabel));
            this.setNull(false);
            
            switch (jdbcType) {
            case Types.ARRAY:
            case Types.BINARY:
            case Types.BIT:
            case Types.BLOB:
            case Types.CLOB:
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.LONGNVARCHAR:
            case Types.LONGVARBINARY:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NCLOB:
            case Types.NULL:
            case Types.NUMERIC:
            case Types.OTHER:
            case Types.REAL:
            case Types.REF:
            case Types.ROWID:
            case Types.SQLXML:
            case Types.STRUCT:
            case Types.VARBINARY:
                throw new SQLException("Unable to marhsal a RDF Node to the declared column type " + jdbcType);
            case Types.BOOLEAN:
                return JdbcNodeUtils.toBoolean(n);
            case Types.BIGINT:
                return JdbcNodeUtils.toLong(n);
            case Types.DATE:
                return JdbcNodeUtils.toDate(n);
            case Types.DECIMAL:
                return JdbcNodeUtils.toDecimal(n);
            case Types.DOUBLE:
                return JdbcNodeUtils.toDouble(n);
            case Types.FLOAT:
                return JdbcNodeUtils.toFloat(n);
            case Types.INTEGER:
                return JdbcNodeUtils.toInt(n);
            case Types.JAVA_OBJECT:
                return n;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
                return JdbcNodeUtils.toString(n);
            case Types.SMALLINT:
                return JdbcNodeUtils.toShort(n);
            case Types.TIME:
                return JdbcNodeUtils.toTime(n);
            case Types.TIMESTAMP:
                return JdbcNodeUtils.toTimestamp(n);
            case Types.TINYINT:
                return JdbcNodeUtils.toByte(n);
            default:
                throw new SQLException("Unable to marshal a RDF Node to the declared unknown column type " + jdbcType);
            }
        }
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return this.getShort(this.findColumnLabel(columnIndex));
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return 0;
        } else {
            // Try to marshal into an integer
            this.setNull(false);
            return JdbcNodeUtils.toShort(n);
        }
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return this.getString(this.findColumnLabel(columnIndex));
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return null;
        } else {
            this.setNull(false);
            return JdbcNodeUtils.toString(n);
        }
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return this.getTime(this.findColumnLabel(columnIndex));
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return null;
        } else {
            // Try to marshal into a time
            this.setNull(false);
            return JdbcNodeUtils.toTime(n);
        }
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return this.getTimestamp(this.findColumnLabel(columnIndex));
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return null;
        } else {
            // Try to marshal into a timestamp
            this.setNull(false);
            return JdbcNodeUtils.toTimestamp(n);
        }
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return this.getURL(this.findColumnLabel(columnIndex));
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        Node n = this.getNode(columnLabel);
        if (n == null) {
            this.setNull(true);
            return null;
        } else {
            this.setNull(false);
            return JdbcNodeUtils.toURL(n);
        }
    }

    // Get Methods for things we don't support

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getBigDecimal() is supported");
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getBigDecimal() is supported");
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public String getCursorName() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getDate() is supported");
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getDate() is supported");
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getObject() is supported");
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getObject() is supported");
    }
    
    @SuppressWarnings("javadoc")
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getObject() is supported");
    }
    
    @SuppressWarnings("javadoc")
    public <T> T getObject(String columnLabel,  Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getObject() is supported");
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return this.statement;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getTime() is supported");
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getTime() is supported");
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getTimestamp() is supported");
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException("Only the single argument form of getTimestamp() is supported");
    }

    @Deprecated
    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Deprecated
    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
	
    @Override
    public final SQLWarning getWarnings() throws SQLException {
        return this.warnings;
    }

    /**
     * Helper method that derived classes may use to set warnings
     * 
     * @param warning
     *            Warning
     */
    protected void setWarning(SQLWarning warning) {
        if (this.warnings == null) {
            this.warnings = warning;
        } else {
            // Chain with existing warnings
            warning.setNextWarning(this.warnings);
            this.warnings = warning;
        }
    }

    /**
     * Helper method that derived classes may use to set warnings
     * 
     * @param warning
     *            Warning
     */
    protected void setWarning(String warning) {
        this.setWarning(new SQLWarning(warning));
    }

    /**
     * Helper method that derived classes may use to set warnings
     * 
     * @param warning
     *            Warning
     * @param cause
     *            Cause
     */
    protected void setWarning(String warning, Throwable cause) {
        this.setWarning(new SQLWarning(warning, cause));
    }

    @Override
    public void insertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public boolean previous() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are forward-only");
    }

    @Override
    public void refreshRow() throws SQLException {
        // No-op
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC Result Sets are read-only");
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.wasNull;
    }

    /**
     * Helper method for setting the wasNull() status of the last column read
     * 
     * @param wasNull
     *            Whether the last column was null
     */
    protected void setNull(boolean wasNull) {
        this.wasNull = wasNull;
    }

}
