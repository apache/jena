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

import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;

/**
 * A result set implementation specifically for representing JDBC metadata
 * 
 */
public class MetaResultSet implements ResultSet {

    private int currRow = -1;
    private boolean wasNull = false, closed = false;
    private ColumnInfo[] columns;
    private Object[][] rows;
    private ResultSetMetaData metadata;

    /**
     * Creates new empty metadata
     * 
     * @param columns
     *            Columns
     * @throws SQLException
     */
    public MetaResultSet(ColumnInfo[] columns) throws SQLException {
        this(columns, new Object[0][0]);
    }

    /**
     * Creates new metadata
     * 
     * @param columns
     *            Columns
     * @param rows
     *            Rows
     * @throws SQLException
     */
    public MetaResultSet(ColumnInfo[] columns, Object[][] rows) throws SQLException {
        if (columns == null)
            throw new SQLException("Column information cannot be null");
        if (rows == null)
            throw new SQLException("Row data cannot be null");
        this.columns = columns;
        this.rows = rows;
        this.metadata = new MetaResultSetMetadata(this, this.columns);
        
        // Validate row widths if any rows
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].length != this.columns.length) throw new SQLException("Row " + (i+1) + " does not have the expected number of columns");
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set is closed");
        } else if (row == 0) {
            // Nothing to do
            return true;
        } else if (row == 1) {
            return this.first();
        } else if (row == -1) {
            return this.last();
        } else if (row > 0) {
            if (row >= this.rows.length) {
                // If target position is after last row move to after last row
                this.currRow = this.rows.length;
                return false;
            } else {
                // Otherwise move to specific row
                this.currRow = row;
                return true;
            }
        } else {
            // Calculate the expected position which can be done by adding the
            // negative absolute row to the total rows thus giving the actual
            // row number we should move to
            int pos = rows.length + row;
            if (pos < 1) {
                // If resulting position is less than one move to before first
                // row
                this.currRow = -1;
                return false;
            } else {
                // Otherwise move to specific row
                this.currRow = pos;
                return true;
            }
        }
    }

    @Override
    public void afterLast() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set is closed");
        } else {
            this.currRow = this.rows.length;
        }
    }

    @Override
    public void beforeFirst() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set is closed");
        } else {
            this.currRow = -1;
        }
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void clearWarnings() throws SQLException {
        // No-op
    }

    @Override
    public void close() throws SQLException {
        this.closed = true;
    }

    @Override
    public void deleteRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result set is closed");
        for (int i = 0; i < this.columns.length; i++) {
            if (this.columns[i].getLabel().equals(columnLabel)) {
                // Remember that JDBC uses a 1 based index
                return i + 1;
            }
        }
        throw new SQLException("The given column does not exist in this result set");
    }

    @Override
    public boolean first() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set is closed");
        } else {
            this.currRow = 0;
            return true;
        }
    }

    private Object getValue(int columnIndex, int expectedType, Class<?> targetType, Object nullValue) throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result set is closed");
        if (this.currRow < 0 || this.currRow >= this.rows.length)
            throw new SQLException("Not currently at a row");
        if (columnIndex >= 1 && columnIndex <= this.columns.length) {
            // Remember that JDBC uses a 1 based index
            ColumnInfo info = this.columns[columnIndex - 1];

            // Determine whether the column has a null value
            Object obj = this.rows[this.currRow][columnIndex - 1];
            this.wasNull = (obj == null);
            if (this.wasNull)
                return nullValue;

            if (info.getType() == expectedType) {
                // If the column is typed appropriately try and marshal
                // appropriately
                if (targetType.isAssignableFrom(obj.getClass())) {
                    try {
                        Object temp = targetType.cast(obj);
                        return temp;
                    } catch (ClassCastException e) {
                        throw new SQLException("Value for this column (Row " + (currRow+1) + " Column " + columnIndex + ") is not valid for the columns declared type", e);
                    }
                } else {
                    throw new SQLException("Value for this column (Row " + (currRow+1) + " Column " + columnIndex + ") is not valid for the columns declared type");
                }
            } else {
                throw new SQLException("Given column (Row " + (currRow+1) + " Column " + columnIndex + ") does not contain appropriately typed values.  Column type declared as " + info.getType() + " but expected type for this lookup is " + expectedType);
            }
        } else {
            throw new SQLException("Column index is out of bounds");
        }
    }

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

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return (BigDecimal) this.getValue(columnIndex, Types.DECIMAL, BigDecimal.class, null);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnLabel));
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Deprecated
    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
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
    public boolean getBoolean(int columnIndex) throws SQLException {
        return (Boolean) this.getValue(columnIndex, Types.BOOLEAN, Boolean.class, false);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return this.getBoolean(this.findColumn(columnLabel));
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return (Byte) this.getValue(columnIndex, Types.TINYINT, Byte.class, (byte)0x0);
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return this.getByte(this.findColumn(columnLabel));
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
    public int getConcurrency() throws SQLException {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public String getCursorName() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return (Date) this.getValue(columnIndex, Types.DATE, Date.class, null);
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return this.getDate(this.findColumn(columnLabel));
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return (Double) this.getValue(columnIndex, Types.DOUBLE, Double.class, 0d);
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return this.getDouble(this.findColumn(columnLabel));
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public int getFetchSize() throws SQLException {
        // Not supported
        return 0;
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return (Float) this.getValue(columnIndex, Types.FLOAT, Float.class, 0f);
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return this.getFloat(this.findColumn(columnLabel));
    }

    @Override
    public int getHoldability() throws SQLException {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return (Integer) this.getValue(columnIndex, Types.INTEGER, Integer.class, 0);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return this.getInt(this.findColumn(columnLabel));
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return (Long) this.getValue(columnIndex, Types.BIGINT, Long.class, 0l);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return this.getLong(this.findColumn(columnLabel));
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this.metadata;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
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
    public String getNString(int columnIndex) throws SQLException {
        return (String) this.getValue(columnIndex, Types.NVARCHAR, String.class, null);
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return this.getNString(this.findColumn(columnLabel));
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return this.getObject(this.findColumn(columnLabel));
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException();
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
    public int getRow() throws SQLException {
        // Remember JDBC used a 1 based index
        if (this.currRow >= 0 && this.currRow < this.rows.length) {
            return this.currRow + 1;
        } else {
            return 0;
        }
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
    public short getShort(int columnIndex) throws SQLException {
        return (Short) this.getValue(columnIndex, Types.SMALLINT, Short.class, (short) 0);
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return this.getShort(this.findColumn(columnLabel));
    }

    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return (String) this.getValue(columnIndex, Types.NVARCHAR, String.class, null);
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return this.getString(this.findColumn(columnLabel));
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return (Time) this.getValue(columnIndex, Types.TIME, Time.class, null);
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return this.getTime(this.findColumn(columnLabel));
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return (Timestamp) this.getValue(columnIndex, Types.TIMESTAMP, Timestamp.class, null);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return this.getTimestamp(this.findColumn(columnLabel));
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getType() throws SQLException {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
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

	// Java 6/7 compatibility
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
	
	public <T> T getObject(String columnLabel,  Class<T> type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertRow() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAfterLast() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result set is closed");
        return this.currRow == this.rows.length;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result set is closed");
        return this.currRow == -1;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.closed;
    }

    @Override
    public boolean isFirst() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result set is closed");
        return this.currRow == 0;
    }

    @Override
    public boolean isLast() throws SQLException {
        if (this.isClosed())
            throw new SQLException("Result set is closed");
        return this.currRow == this.rows.length - 1;
    }

    @Override
    public boolean last() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set is closed");
        } else {
            this.currRow = this.rows.length - 1;
            return true;
        }
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public boolean next() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set is closed");
        } else if (this.currRow == this.rows.length) {
            return false;
        } else {
            this.currRow++;
            return (this.currRow < this.rows.length);
        }
    }

    @Override
    public boolean previous() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set is closed");
        } else if (this.currRow == -1) {
            return false;
        } else {
            this.currRow--;
            return (this.currRow > -1);
        }
    }

    @Override
    public void refreshRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Cannot move to a row after the result set is closed");
        } else if (rows == 0) {
            // Nothing to do
            return true;
        } else if (rows > 0) {
            if (this.currRow + rows >= this.rows.length) {
                // If target position is after last row move to after last row
                this.currRow = this.rows.length;
                return false;
            } else {
                // Otherwise move appropriately
                this.currRow += rows;
                return true;
            }
        } else {
            // Calculate the expected position which can be done by adding the
            // negative absolute row to the total rows thus giving the actual
            // row number we should move to
            int pos = this.currRow + rows;
            if (pos < 1) {
                // If resulting position is less than one move to before first
                // row
                this.currRow = -1;
                return false;
            } else {
                // Otherwise move to specific row
                this.currRow = pos;
                return true;
            }
        }
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        // Results are read-only
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        // Results are read-only
        return false;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        // Results are read-only
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        // Not supported
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        // Not supported
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateRow() throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException("Jena JDBC metadata is read-only");
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.wasNull;
    }
}
