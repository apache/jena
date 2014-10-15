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

import java.math.BigDecimal ;
import java.sql.* ;
import java.util.Calendar ;

import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo ;
import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

/**
 * Tests for the various helper methods of {@link JdbcCompatibility}
 */
public class TestCompatibility {

    /**
     * Tests constants are not normalized to different values
     */
    @Test
    public void test_level_normalization_01() {
        Assert.assertEquals(JdbcCompatibility.LOW, JdbcCompatibility.normalizeLevel(JdbcCompatibility.LOW));
        Assert.assertEquals(JdbcCompatibility.MEDIUM, JdbcCompatibility.normalizeLevel(JdbcCompatibility.MEDIUM));
        Assert.assertEquals(JdbcCompatibility.HIGH, JdbcCompatibility.normalizeLevel(JdbcCompatibility.HIGH));
        Assert.assertEquals(JdbcCompatibility.MEDIUM, JdbcCompatibility.normalizeLevel(JdbcCompatibility.DEFAULT));
    }
    
    /**
     * Test values outside range are normalized to range ends
     */
    @Test
    public void test_level_normalization_02() {
        Assert.assertEquals(JdbcCompatibility.LOW, JdbcCompatibility.normalizeLevel(Integer.MIN_VALUE));
        Assert.assertEquals(JdbcCompatibility.HIGH, JdbcCompatibility.normalizeLevel(Integer.MAX_VALUE));
    }
    
    /**
     * Test all values in acceptable range are left as is
     */
    @Test
    public void test_level_normalization_03() {
        for (int i = 1; i <= 9; i++) {
            Assert.assertEquals(i, JdbcCompatibility.normalizeLevel(i));
        }
    }

    /**
     * Test that with low compatibility columns will not be typed as strings/detected types
     */
    @Test
    public void test_level_behaviours_columns_01() {
        Assert.assertFalse(JdbcCompatibility.shouldTypeColumnsAsString(JdbcCompatibility.LOW));
        Assert.assertFalse(JdbcCompatibility.shouldDetectColumnTypes(JdbcCompatibility.LOW));
    }
    
    /**
     * Test that with medium compatibility columns will be typed as strings but not detected types
     */
    @Test
    public void test_level_behaviours_columns_02() {
        Assert.assertTrue(JdbcCompatibility.shouldTypeColumnsAsString(JdbcCompatibility.MEDIUM));
        Assert.assertFalse(JdbcCompatibility.shouldDetectColumnTypes(JdbcCompatibility.MEDIUM));
        
        // This also applies to DEFAULT level
        Assert.assertTrue(JdbcCompatibility.shouldTypeColumnsAsString(JdbcCompatibility.DEFAULT));
        Assert.assertFalse(JdbcCompatibility.shouldDetectColumnTypes(JdbcCompatibility.DEFAULT));
    }
    
    /**
     * Test that with high compatibility columns will not be typed as strings but with detected types
     */
    @Test
    public void test_level_behaviours_columns_03() {
        Assert.assertFalse(JdbcCompatibility.shouldTypeColumnsAsString(JdbcCompatibility.HIGH));
        Assert.assertTrue(JdbcCompatibility.shouldDetectColumnTypes(JdbcCompatibility.HIGH));
    }
        
    /**
     * Detects a columns types and checks basic information from the detected type, returns the detected column information so tests can make further assertions on this
     * @param var Variable Name i.e. Column Label
     * @param value Example value to detect from
     * @param allowNulls Whether the column allows nulls
     * @param jdbcType Expected detected JDBC type
     * @param className Expected detected class name
     * @return Column Information
     * @throws SQLException
     */
    private ColumnInfo testColumnTypeDetection(String var, Node value, boolean allowNulls, int jdbcType, String className) throws SQLException {
        ColumnInfo info = JdbcCompatibility.detectColumnType(var, value, allowNulls);
        Assert.assertEquals(var, info.getLabel());
        if (allowNulls) {
            Assert.assertEquals(ResultSetMetaData.columnNullable, info.getNullability());
        } else {
            Assert.assertEquals(ResultSetMetaData.columnNoNulls, info.getNullability());
        }
        Assert.assertEquals(jdbcType, info.getType());
        Assert.assertEquals(className, info.getClassName());
        Assert.assertEquals(Node.class.getCanonicalName(), info.getTypeName());
        return info;
    }
    
    /**
     * Expect xsd:integer to be typed as integers
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_integer_01() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactoryExtra.intToNode(1234), true, Types.BIGINT, Long.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertTrue(info.isSigned());
    }
    
    /**
     * Expect xsd:int to be typed as integers
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_integer_02() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("1234", XSDDatatype.XSDint), true, Types.BIGINT, Long.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertTrue(info.isSigned());
    }
    
    /**
     * Expect xsd:long to be typed as integers
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_integer_03() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("1234", XSDDatatype.XSDlong), true, Types.BIGINT, Long.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertTrue(info.isSigned());
    }
    
    /**
     * Expect xsd:unsignedInteger to be typed as integers
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_integer_04() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("1234", XSDDatatype.XSDunsignedInt), true, Types.BIGINT, Long.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertFalse(info.isSigned());
    }
    
    /**
     * Expect xsd:unsignedLong to be typed as integers
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_integer_05() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("1234", XSDDatatype.XSDunsignedLong), true, Types.BIGINT, Long.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertFalse(info.isSigned());
    }
    
    /**
     * Expect xsd:short to be typed as integers
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_integer_06() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("1234", XSDDatatype.XSDshort), true, Types.INTEGER, Integer.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertTrue(info.isSigned());
    }
    
    /**
     * Expect xsd:unsignedShort to be typed as integers
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_integer_07() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("1234", XSDDatatype.XSDunsignedShort), true, Types.INTEGER, Integer.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertFalse(info.isSigned());
    }
    
    /**
     * Expect xsd:byte to be typed as bytes
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_byte_01() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("123", XSDDatatype.XSDbyte), true, Types.TINYINT, Byte.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertTrue(info.isSigned());
    }
    
    /**
     * Expect xsd:unsignedByte to be typed as bytes
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_byte_02() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("123", XSDDatatype.XSDunsignedByte), true, Types.TINYINT, Byte.class.getCanonicalName());
        Assert.assertEquals(0, info.getScale());
        Assert.assertFalse(info.isSigned());
    }
    
    /**
     * Expect xsd:boolean to be typed as booleans
     * @throws SQLException
     */
    @Test
    public void test_column_type_detection_boolean() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createLiteral("true", XSDDatatype.XSDboolean), true, Types.BOOLEAN, Boolean.class.getCanonicalName());
    }
    
    
    /**
     * Expect xsd:decimal to be typed as decimal
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_decimal() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("123.4", XSDDatatype.XSDdecimal), true, Types.DECIMAL, BigDecimal.class.getCanonicalName());
        Assert.assertEquals(16, info.getScale());
        Assert.assertEquals(16, info.getPrecision());
        Assert.assertTrue(info.isSigned());
    }
    
    /**
     * Expect xsd:double to be typed as double
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_double() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("12.3e4", XSDDatatype.XSDdouble), true, Types.DOUBLE, Double.class.getCanonicalName());
        Assert.assertEquals(16, info.getScale());
        Assert.assertEquals(16, info.getPrecision());
        Assert.assertTrue(info.isSigned());
    }
    
    /**
     * Expect xsd:float to be typed as float
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_float() throws SQLException {
        ColumnInfo info = testColumnTypeDetection("x", NodeFactory.createLiteral("12.3e4", XSDDatatype.XSDfloat), true, Types.FLOAT, Float.class.getCanonicalName());
        Assert.assertEquals(7, info.getScale());
        Assert.assertEquals(15, info.getPrecision());
        Assert.assertTrue(info.isSigned());
    }
    
    /**
     * Tests that xsd:dateTime is typed as Date
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_datetimes_01() throws SQLException {
        Model m = ModelFactory.createDefaultModel();
        testColumnTypeDetection("x", m.createTypedLiteral(Calendar.getInstance()).asNode(), true, Types.TIMESTAMP, Timestamp.class.getCanonicalName());
    }
    
    /**
     * Tests that xsd:date is typed as Date
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_datetimes_02() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createLiteral("2013-04-08", XSDDatatype.XSDdate), true, Types.DATE, Date.class.getCanonicalName());
    }
    
    /**
     * Tests that xsd:time is typed as Time
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_datetimes_03() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createLiteral("13:15:00", XSDDatatype.XSDtime), true, Types.TIME, Time.class.getCanonicalName());
    }
    
    /**
     * Tests that xsd:time is typed as Time
     * @throws SQLException 
     */
    @Test
    public void test_column_type_detection_datetimes_04() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createLiteral("13:15:00.123", XSDDatatype.XSDtime), true, Types.TIME, Time.class.getCanonicalName());
    }
    
    /**
     * Test that simple literals are typed as strings
     * @throws SQLException
     */
    @Test
    public void test_column_type_detection_strings_01() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createLiteral("simple"), true, Types.NVARCHAR, String.class.getCanonicalName());
    }
    
    /**
     * Test that literals with languages are typed as strings
     * @throws SQLException
     */
    @Test
    public void test_column_type_detection_strings_02() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createLiteral("simple", "en", false), true, Types.NVARCHAR, String.class.getCanonicalName());
    }
    
    /**
     * Test that xsd:string literals are typed as strings
     * @throws SQLException
     */
    @Test
    public void test_column_type_detection_strings_03() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createLiteral("simple", XSDDatatype.XSDstring), true, Types.NVARCHAR, String.class.getCanonicalName());
    }
    
    /**
     * Test that custom typed literals are typed as strings
     * @throws SQLException
     */
    @Test
    public void test_column_type_detection_strings_04() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createLiteral("simple", TypeMapper.getInstance().getSafeTypeByName("http://datatypes/custom")), true, Types.NVARCHAR, String.class.getCanonicalName());
    }
    
    /**
     * Test that URI are typed as strings
     * @throws SQLException
     */
    @Test
    public void test_column_type_detection_strings_05() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createURI("http://example.org"), true, Types.NVARCHAR, String.class.getCanonicalName());
    }
    
    /**
     * Test that blank nodes are typed as strings
     * @throws SQLException
     */
    @Test
    public void test_column_type_detection_strings_06() throws SQLException {
        testColumnTypeDetection("x", NodeFactory.createAnon(), true, Types.NVARCHAR, String.class.getCanonicalName());
    }
    
    /**
     * Tests treatment of nulls in type detection
     * @throws SQLException
     */
    @Test
    public void test_column_type_detection_nulls_01() throws SQLException {
        testColumnTypeDetection("x", null, true, Types.NVARCHAR, String.class.getCanonicalName());
    }
    
    /**
     * Tests treatment of nulls in type detection
     * @throws SQLException
     */
    @Test(expected=SQLException.class)
    public void test_column_type_detection_nulls_02() throws SQLException {
        // Expect an error to be thrown if a null is provided when the column should not allow nulls
        testColumnTypeDetection("x", null, false, Types.NVARCHAR, String.class.getCanonicalName());
    }
}