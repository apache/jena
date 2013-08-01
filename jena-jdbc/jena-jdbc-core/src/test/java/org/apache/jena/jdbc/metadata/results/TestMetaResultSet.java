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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;

import org.apache.jena.jdbc.metadata.results.MetaResultSet;
import org.apache.jena.jdbc.results.metadata.columns.BooleanColumn;
import org.apache.jena.jdbc.results.metadata.columns.ByteColumn;
import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;
import org.apache.jena.jdbc.results.metadata.columns.DateColumn;
import org.apache.jena.jdbc.results.metadata.columns.DecimalColumn;
import org.apache.jena.jdbc.results.metadata.columns.DoubleColumn;
import org.apache.jena.jdbc.results.metadata.columns.FloatColumn;
import org.apache.jena.jdbc.results.metadata.columns.IntegerColumn;
import org.apache.jena.jdbc.results.metadata.columns.LongIntegerColumn;
import org.apache.jena.jdbc.results.metadata.columns.ShortIntegerColumn;
import org.apache.jena.jdbc.results.metadata.columns.StringColumn;
import org.apache.jena.jdbc.results.metadata.columns.TimeColumn;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link MetaResultSet}
 * 
 */
public class TestMetaResultSet {

    /**
     * Check empty meta result set
     * 
     * @throws SQLException
     */
    @Test
    public void empty_meta_result_set_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[0]);

        // Check results metadata
        ResultSetMetaData metadata = results.getMetaData();
        Assert.assertEquals(0, metadata.getColumnCount());

        // Check results
        Assert.assertTrue(results.isBeforeFirst());
        Assert.assertFalse(results.next());
        Assert.assertTrue(results.isAfterLast());

        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Check empty meta result set with a single column
     * 
     * @throws SQLException
     */
    @Test
    public void empty_meta_result_set_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new StringColumn("Test", ResultSetMetaData.columnNullable) });

        // Check results metadata
        ResultSetMetaData metadata = results.getMetaData();
        Assert.assertEquals(1, metadata.getColumnCount());

        // Check results
        Assert.assertTrue(results.isBeforeFirst());
        Assert.assertFalse(results.next());
        Assert.assertTrue(results.isAfterLast());

        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Trying to create a meta result set with bad data is an error
     * 
     * @throws SQLException
     */
    @Test(expected = SQLException.class)
    public void meta_result_set_bad_01() throws SQLException {
        // Every row must have the correct number of columns
        new MetaResultSet(new ColumnInfo[] { new StringColumn("Test", ResultSetMetaData.columnNullable) }, new Object[][] { {} });
    }

    private MetaResultSet createMetaResultSet(int rows) throws SQLException {
        ColumnInfo[] columns = new ColumnInfo[] { new IntegerColumn("Test", ResultSetMetaData.columnNoNulls, true) };
        Object[][] rowData = new Object[rows][1];
        for (int i = 0; i < rowData.length; i++) {
            rowData[i][0] = (i + 1);
        }
        return new MetaResultSet(columns, rowData);
    }

    /**
     * Test movement within meta results
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_movement_01() throws SQLException {
        MetaResultSet results = createMetaResultSet(0);
        Assert.assertTrue(results.isBeforeFirst());
        Assert.assertFalse(results.next());
        Assert.assertTrue(results.isAfterLast());

        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test movement within meta results
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_movement_02() throws SQLException {
        MetaResultSet results = createMetaResultSet(1);
        Assert.assertTrue(results.isBeforeFirst());

        // Move forwards
        Assert.assertTrue(results.next());
        Assert.assertTrue(results.isFirst());
        Assert.assertFalse(results.isBeforeFirst());
        Assert.assertTrue(results.isLast());
        Assert.assertFalse(results.isAfterLast());

        // Move to end
        Assert.assertFalse(results.next());
        Assert.assertTrue(results.isAfterLast());

        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test movement within meta results
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_movement_03() throws SQLException {
        MetaResultSet results = createMetaResultSet(1);
        Assert.assertTrue(results.isBeforeFirst());

        // Move forwards
        Assert.assertTrue(results.next());
        Assert.assertTrue(results.isFirst());
        Assert.assertFalse(results.isBeforeFirst());
        Assert.assertTrue(results.isLast());
        Assert.assertFalse(results.isAfterLast());

        // Move to end
        Assert.assertFalse(results.next());
        Assert.assertTrue(results.isAfterLast());
        
        // Move backwards
        Assert.assertTrue(results.previous());
        Assert.assertTrue(results.isFirst());
        Assert.assertFalse(results.isBeforeFirst());
        Assert.assertTrue(results.isLast());
        Assert.assertFalse(results.isAfterLast());
        
        results.close();
        Assert.assertTrue(results.isClosed());
    }
    
    /**
     * Test movement within meta results
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_movement_04() throws SQLException {
        MetaResultSet results = createMetaResultSet(1);
        Assert.assertTrue(results.isBeforeFirst());

        // Move to absolute row
        Assert.assertTrue(results.absolute(1));
        Assert.assertTrue(results.isFirst());
        Assert.assertFalse(results.isBeforeFirst());
        Assert.assertTrue(results.isLast());
        Assert.assertFalse(results.isAfterLast());

        // Move to end
        Assert.assertFalse(results.next());
        Assert.assertTrue(results.isAfterLast());

        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_string_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(
                new ColumnInfo[] { new StringColumn("Test", ResultSetMetaData.columnNullable) }, new Object[][] { { "value" } });

        Assert.assertTrue(results.next());
        String value = results.getString(1);
        Assert.assertEquals("value", value);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_string_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(
                new ColumnInfo[] { new StringColumn("Test", ResultSetMetaData.columnNullable) }, new Object[][] { { null } });

        Assert.assertTrue(results.next());
        String value = results.getString(1);
        Assert.assertEquals(null, value);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_boolean_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(
                new ColumnInfo[] { new BooleanColumn("Test", ResultSetMetaData.columnNullable) }, new Object[][] { { true } });

        Assert.assertTrue(results.next());
        Assert.assertTrue(results.getBoolean(1));
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_boolean_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(
                new ColumnInfo[] { new BooleanColumn("Test", ResultSetMetaData.columnNullable) }, new Object[][] { { null } });

        Assert.assertTrue(results.next());
        Assert.assertFalse(results.getBoolean(1));
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_byte_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new ByteColumn("Test", ResultSetMetaData.columnNullable,
                true) }, new Object[][] { { (byte) 0x10 } });

        Assert.assertTrue(results.next());
        byte value = results.getByte(1);
        Assert.assertEquals(0x10, value);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_byte_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new ByteColumn("Test", ResultSetMetaData.columnNullable,
                true) }, new Object[][] { { null } });

        Assert.assertTrue(results.next());
        byte value = results.getByte(1);
        Assert.assertEquals(0x0, value);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @SuppressWarnings("deprecation")
    @Test
    public void meta_result_set_date_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new DateColumn("Test", ResultSetMetaData.columnNullable) },
                new Object[][] { { new Date(2013, 4, 24) } });

        Assert.assertTrue(results.next());
        Date value = results.getDate(1);
        Assert.assertEquals(new Date(2013, 4, 24), value);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_date_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new DateColumn("Test", ResultSetMetaData.columnNullable) },
                new Object[][] { { null } });

        Assert.assertTrue(results.next());
        Date value = results.getDate(1);
        Assert.assertEquals(null, value);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_time_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new TimeColumn("Test", ResultSetMetaData.columnNullable) },
                new Object[][] { { new Time(0) } });

        Assert.assertTrue(results.next());
        Time value = results.getTime(1);
        Assert.assertEquals(new Time(0), value);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_time_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new TimeColumn("Test", ResultSetMetaData.columnNullable) },
                new Object[][] { { null } });

        Assert.assertTrue(results.next());
        Time value = results.getTime(1);
        Assert.assertEquals(null, value);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_decimal_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(
                new ColumnInfo[] { new DecimalColumn("Test", ResultSetMetaData.columnNullable) },
                new Object[][] { { new BigDecimal("123.4") } });

        Assert.assertTrue(results.next());
        BigDecimal value = results.getBigDecimal(1);
        Assert.assertEquals(new BigDecimal("123.4"), value);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_decimal_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(
                new ColumnInfo[] { new DecimalColumn("Test", ResultSetMetaData.columnNullable) }, new Object[][] { { null } });

        Assert.assertTrue(results.next());
        BigDecimal value = results.getBigDecimal(1);
        Assert.assertEquals(null, value);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_double_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(
                new ColumnInfo[] { new DoubleColumn("Test", ResultSetMetaData.columnNullable) }, new Object[][] { { 0.123d } });

        Assert.assertTrue(results.next());
        double value = results.getDouble(1);
        Assert.assertEquals(0.123d, value, 0d);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_double_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new ByteColumn("Test", ResultSetMetaData.columnNullable,
                true) }, new Object[][] { { null } });

        Assert.assertTrue(results.next());
        double value = results.getDouble(1);
        Assert.assertEquals(0d, value, 0d);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_float_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new FloatColumn("Test", ResultSetMetaData.columnNullable) },
                new Object[][] { { 0.123f } });

        Assert.assertTrue(results.next());
        float value = results.getFloat(1);
        Assert.assertEquals(0.123f, value, 0f);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_float_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new FloatColumn("Test", ResultSetMetaData.columnNullable) },
                new Object[][] { { null } });

        Assert.assertTrue(results.next());
        float value = results.getFloat(1);
        Assert.assertEquals(0f, value, 0f);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_integer_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new IntegerColumn("Test", ResultSetMetaData.columnNullable,
                true) }, new Object[][] { { 1234 } });

        Assert.assertTrue(results.next());
        int value = results.getInt(1);
        Assert.assertEquals(1234, value);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_integer_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new IntegerColumn("Test", ResultSetMetaData.columnNullable,
                true) }, new Object[][] { { null } });

        Assert.assertTrue(results.next());
        int value = results.getInt(1);
        Assert.assertEquals(0, value);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_long_integer_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new LongIntegerColumn("Test",
                ResultSetMetaData.columnNullable, true) }, new Object[][] { { 1234l } });

        Assert.assertTrue(results.next());
        long value = results.getLong(1);
        Assert.assertEquals(1234, value);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_long_integer_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new LongIntegerColumn("Test",
                ResultSetMetaData.columnNullable, true) }, new Object[][] { { null } });

        Assert.assertTrue(results.next());
        long value = results.getLong(1);
        Assert.assertEquals(0, value);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_short_integer_01() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new ShortIntegerColumn("Test",
                ResultSetMetaData.columnNullable, true) }, new Object[][] { { (short) 1234 } });

        Assert.assertTrue(results.next());
        short value = results.getShort(1);
        Assert.assertEquals((short) 1234, value);
        Assert.assertFalse(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }

    /**
     * Test retrieving meta column values
     * 
     * @throws SQLException
     */
    @Test
    public void meta_result_set_short_integer_02() throws SQLException {
        MetaResultSet results = new MetaResultSet(new ColumnInfo[] { new ShortIntegerColumn("Test",
                ResultSetMetaData.columnNullable, true) }, new Object[][] { { null } });

        Assert.assertTrue(results.next());
        short value = results.getShort(1);
        Assert.assertEquals((short) 0, value);
        Assert.assertTrue(results.wasNull());

        Assert.assertFalse(results.next());
        results.close();
        Assert.assertTrue(results.isClosed());
    }
}
