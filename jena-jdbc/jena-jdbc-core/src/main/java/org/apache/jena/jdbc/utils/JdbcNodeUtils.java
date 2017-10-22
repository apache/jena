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

package org.apache.jena.jdbc.utils;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.apache.jena.vocabulary.XSD ;

/**
 * Class with helpful utility methods for Jena JDBC
 * 
 */
public class JdbcNodeUtils {

    private static Set<String> numericTypes = new HashSet<>();

    static {
        numericTypes.add(XSD.decimal.getURI());
        numericTypes.add(XSD.integer.getURI());
        numericTypes.add(XSD.negativeInteger.getURI());
        numericTypes.add(XSD.nonNegativeInteger.getURI());
        numericTypes.add(XSD.nonPositiveInteger.getURI());
        numericTypes.add(XSD.unsignedByte.getURI());
        numericTypes.add(XSD.unsignedInt.getURI());
        numericTypes.add(XSD.unsignedLong.getURI());
        numericTypes.add(XSD.unsignedShort.getURI());
        numericTypes.add(XSD.xbyte.getURI());
        numericTypes.add(XSD.xdouble.getURI());
        numericTypes.add(XSD.xfloat.getURI());
        numericTypes.add(XSD.xint.getURI());
        numericTypes.add(XSD.xlong.getURI());
        numericTypes.add(XSD.xshort.getURI());
    }

    /**
     * Private constructor prevents instantiation
     */
    private JdbcNodeUtils() {
    }

    /**
     * Tries to convert a node to a boolean
     * 
     * @param n
     *            Node
     * @return Boolean
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static boolean toBoolean(Node n) throws SQLException {
        try {
            if (n == null)
                return false;
            if (n.isLiteral()) {
                if (n.getLiteralDatatypeURI().equals(XSD.xboolean.getURI())) {
                    return Boolean.parseBoolean(n.getLiteralLexicalForm());
                } else if (hasNumericDatatype(n)) {
                    return parseAsInteger(n) == 0 ? false : true;
                } else {
                    throw new SQLException("Unable to marshal the given literal to a boolean");
                }
            } else {
                throw new SQLException("Unable to marshal a non-literal to a boolean");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a boolean", e);
        }
    }

    /**
     * Tries to convert a node to a byte
     * 
     * @param n
     *            Node
     * @return Byte
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static byte toByte(Node n) throws SQLException {
        try {
            if (n == null)
                return 0;
            if (n.isLiteral()) {
                return Byte.decode(n.getLiteralLexicalForm());
            } else {
                throw new SQLException("Unable to marshal a non-literal to a byte");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a byte", e);
        }
    }

    /**
     * Tries to convert a node to a short integer
     * 
     * @param n
     *            Node
     * @return Short Integer
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static short toShort(Node n) throws SQLException {
        try {
            if (n == null)
                return 0;
            if (n.isLiteral()) {
                return Short.parseShort(n.getLiteralLexicalForm());
            } else {
                throw new SQLException("Unable to marshal a non-literal to an integer");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to an integer", e);
        }
    }

    /**
     * Tries to convert a node to an integer
     * 
     * @param n
     *            Node
     * @return Integer
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static int toInt(Node n) throws SQLException {
        try {
            if (n == null)
                return 0;
            if (n.isLiteral()) {
                return NodeFactoryExtra.nodeToInt(n);
            } else {
                throw new SQLException("Unable to marshal a non-literal to an integer");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to an integer", e);
        }
    }

    /**
     * Tries to convert a node to a long integer
     * 
     * @param n
     *            Node
     * @return Long Integer
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static long toLong(Node n) throws SQLException {
        try {
            if (n == null)
                return 0;
            if (n.isLiteral()) {
                return NodeFactoryExtra.nodeToLong(n);
            } else {
                throw new SQLException("Unable to marshal a non-literal to a long integer");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a long integer", e);
        }
    }

    /**
     * Tries to convert a node to a float
     * 
     * @param n
     *            Node
     * @return Float
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static float toFloat(Node n) throws SQLException {
        try {
            if (n == null)
                return 0;
            if (n.isLiteral()) {
                return NodeFactoryExtra.nodeToFloat(n);
            } else {
                throw new SQLException("Unable to marshal a non-literal to a float");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a float", e);
        }
    }

    /**
     * Tries to convert a node to a double
     * 
     * @param n
     *            Node
     * @return Double
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static double toDouble(Node n) throws SQLException {
        try {
            if (n == null)
                return 0;
            if (n.isLiteral()) {
                return NodeFactoryExtra.nodeToDouble(n);
            } else {
                throw new SQLException("Unable to marshal a non-literal to a double");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a double", e);
        }
    }

    /**
     * Tries to convert a node to a decimal
     * 
     * @param n
     *            Node
     * @return Decimal
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static BigDecimal toDecimal(Node n) throws SQLException {
        try {
            if (n == null)
                return null;
            if (n.isLiteral()) {
                return new BigDecimal(n.getLiteralLexicalForm());
            } else {
                throw new SQLException("Unable to marshal a non-literal to a decimal");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a decimal", e);
        }
    }

    /**
     * Tries to convert a node to a date
     * 
     * @param n
     *            Node
     * @return Date
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static Date toDate(Node n) throws SQLException {
        try {
            if (n == null)
                return null;
            if (n.isLiteral()) {
                return new Date(NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar(n.getLiteralLexicalForm())
                        .toGregorianCalendar().getTimeInMillis());
            } else {
                throw new SQLException("Unable to marshal a non-literal to a date");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a date", e);
        }
    }

    /**
     * Tries to convert a node to a time
     * 
     * @param n
     *            Node
     * @return Time
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static Time toTime(Node n) throws SQLException {
        try {
            if (n == null)
                return null;
            if (n.isLiteral()) {
                return new Time(NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar(n.getLiteralLexicalForm())
                        .toGregorianCalendar().getTimeInMillis());
            } else {
                throw new SQLException("Unable to marshal a non-literal to a time");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a time", e);
        }
    }

    /**
     * Tries to convert a node to a timestamp
     * 
     * @param n
     *            Node
     * @return Timestamp
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static Timestamp toTimestamp(Node n) throws SQLException {
        try {
            if (n == null)
                return null;
            if (n.isLiteral()) {
                return new Timestamp(NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar(n.getLiteralLexicalForm())
                        .toGregorianCalendar().getTimeInMillis());
            } else {
                throw new SQLException("Unable to marshal a non-literal to a timestamp");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a timestamp", e);
        }
    }

    /**
     * Tries to convert a now to a string
     * 
     * @param n
     *            Node
     * @return String
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static String toString(Node n) throws SQLException {
        try {
            if (n == null)
                return null;
            if (n.isURI()) {
                return n.getURI();
            } else if (n.isLiteral()) {
                return n.getLiteralLexicalForm();
            } else if (n.isBlank()) {
                return n.getBlankNodeLabel();
            } else if (n.isVariable()) {
                return n.getName();
            } else {
                throw new SQLException("Unable to marshal unknown node types to a string");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a string", e);
        }
    }

    /**
     * Tries to convert a node to a URL
     * 
     * @param n
     *            Node
     * @return URL or null
     * @throws SQLException
     *             Thrown if the node cannot be converted
     */
    public static URL toURL(Node n) throws SQLException {
        try {
            if (n == null)
                return null;
            if (n.isURI()) {
                return new URL(n.getURI());
            } else {
                throw new SQLException("Unable to marshal a non-uri to a URL");
            }
        } catch (SQLException e) {
            // Throw as is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions
            throw new SQLException("Unable to marshal the value to a URL", e);
        }
    }

    private static long parseAsInteger(Node n) throws SQLException {
        if (n == null)
            throw new SQLException("Unable to marshal a null to an integer");
        if (n.isLiteral()) {
            try {
                String lex = n.getLiteralLexicalForm();
                if (lex.contains(".")) {
                    return Long.parseLong(lex.substring(0, lex.indexOf('.')));
                } else {
                    return Long.parseLong(lex);
                }
            } catch (Exception e) {
                throw new SQLException("Unable to marshal an invalid numeric representation to an integer", e);
            }
        } else {
            throw new SQLException("Unable to marshal a non-literal to an integer");
        }
    }

    /**
     * Gets whether a node has a numeric datatype
     * 
     * @param n
     *            Node
     * @return True if a numeric datatype, false otherwise
     */
    private static boolean hasNumericDatatype(Node n) {
        if (n == null)
            return false;
        if (!n.isLiteral())
            return false;
        return numericTypes.contains(n.getLiteralDatatypeURI());
    }
}
