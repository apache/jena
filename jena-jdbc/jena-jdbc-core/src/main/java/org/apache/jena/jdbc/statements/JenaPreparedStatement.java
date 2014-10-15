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

package org.apache.jena.jdbc.statements;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.jena.iri.IRI;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.statements.metadata.JenaParameterMetadata;
import org.apache.jena.jdbc.utils.JdbcNodeUtils;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;

/**
 * Abstract Jena JDBC implementation of a prepared statement
 * 
 */
public abstract class JenaPreparedStatement extends JenaStatement implements PreparedStatement {

    private ParameterizedSparqlString sparqlStr = new ParameterizedSparqlString();
    private ParameterMetaData paramMetadata;

    /**
     * Creates a new prepared statement
     * 
     * @param sparql
     * @param connection
     *            Connection
     * @param type
     *            Result Set type for result sets produced by this statement
     * @param fetchDir
     *            Fetch Direction
     * @param fetchSize
     *            Fetch Size
     * @param holdability
     *            Result Set holdability
     * @param autoCommit
     *            Auto-commit
     * @param transactionLevel
     *            Transaction Isolation level
     * @throws SQLException
     *             Thrown if there is a problem preparing the statement
     */
    public JenaPreparedStatement(String sparql, JenaConnection connection, int type, int fetchDir, int fetchSize,
            int holdability, boolean autoCommit, int transactionLevel) throws SQLException {
        super(connection, type, fetchDir, fetchSize, holdability, autoCommit, transactionLevel);

        this.sparqlStr.setCommandText(sparql);
        this.paramMetadata = new JenaParameterMetadata(this.sparqlStr);
    }

    @Override
    public void addBatch() throws SQLException {
        this.addBatch(this.sparqlStr.toString());
    }

    @Override
    public void clearParameters() throws SQLException {
        this.sparqlStr.clearParams();
    }

    @Override
    public boolean execute() throws SQLException {
        return this.execute(this.sparqlStr.toString());
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return this.executeQuery(this.sparqlStr.toString());
    }

    @Override
    public int executeUpdate() throws SQLException {
        return this.executeUpdate(this.sparqlStr.toString());
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        // Return null because we don't know in advance the column types
        return null;
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return this.paramMetadata;
    }

    protected final void setParameter(int parameterIndex, Node n) throws SQLException {
        // Remember that JDBC used a 1 based index
        if (parameterIndex < 1 || parameterIndex > this.paramMetadata.getParameterCount())
            throw new SQLException("Parameter Index is out of bounds");

        this.sparqlStr.setParam(parameterIndex - 1, n);
    }

    @Override
    public void setArray(int parameterIndex, Array value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream value, int arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream value, long arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactory.createLiteral(value.toPlainString(), XSDDatatype.XSDdecimal));
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream value, int arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream value, long arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBlob(int parameterIndex, Blob value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBlob(int parameterIndex, InputStream value, long arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setBoolean(int parameterIndex, boolean value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactory.createLiteral(Boolean.toString(value), XSDDatatype.XSDboolean));
    }

    @Override
    public void setByte(int parameterIndex, byte value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactory.createLiteral(Byte.toString(value), XSDDatatype.XSDbyte));
    }

    @Override
    public void setBytes(int parameterIndex, byte[] value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader value, int arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader value, long arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setClob(int parameterIndex, Clob value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setClob(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setClob(int parameterIndex, Reader value, long arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setDate(int parameterIndex, Date value) throws SQLException {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(value.getTime());
        this.setParameter(parameterIndex, NodeFactoryExtra.dateTimeToNode(c));
    }

    @Override
    public void setDate(int parameterIndex, Date value, Calendar arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setDouble(int parameterIndex, double value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactoryExtra.doubleToNode(value));
    }

    @Override
    public void setFloat(int parameterIndex, float value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactoryExtra.floatToNode(value));
    }

    @Override
    public void setInt(int parameterIndex, int value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactoryExtra.intToNode(value));
    }

    @Override
    public void setLong(int parameterIndex, long value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactoryExtra.intToNode(value));
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNClob(int parameterIndex, Reader value, long arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactory.createLiteral(value));
    }

    @Override
    public void setNull(int parameterIndex, int value) throws SQLException {
        throw new SQLFeatureNotSupportedException("Parameters for SPARQL statements are not nullable");
    }

    @Override
    public void setNull(int parameterIndex, int value, String arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException("Parameters for SPARQL statements are not nullable");
    }

    @Override
    public void setObject(int parameterIndex, Object value) throws SQLException {
        if (value == null) throw new SQLException("Setting a null value is not permitted");
        
        if (value instanceof Node) {
            this.setParameter(parameterIndex, (Node) value);
        } else if (value instanceof RDFNode) {
            this.setParameter(parameterIndex, ((RDFNode) value).asNode());
        } else if (value instanceof String) {
            this.setParameter(parameterIndex, NodeFactory.createLiteral((String) value));
        } else if (value instanceof Boolean) {
            this.setParameter(parameterIndex,
                    NodeFactory.createLiteral(Boolean.toString((Boolean) value), XSDDatatype.XSDboolean));
        } else if (value instanceof Long) {
            this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((Long) value));
        } else if (value instanceof Integer) {
            this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((Integer) value));
        } else if (value instanceof Short) {
            this.setParameter(parameterIndex, NodeFactory.createLiteral(Short.toString((Short) value), XSDDatatype.XSDshort));
        } else if (value instanceof Byte) {
            this.setParameter(parameterIndex, NodeFactory.createLiteral(Byte.toString((Byte) value), XSDDatatype.XSDbyte));
        } else if (value instanceof BigDecimal) {
            this.setParameter(parameterIndex,
                    NodeFactory.createLiteral(((BigDecimal) value).toPlainString(), XSDDatatype.XSDdecimal));
        } else if (value instanceof Float) {
            this.setParameter(parameterIndex, NodeFactoryExtra.floatToNode((Float) value));
        } else if (value instanceof Double) {
            this.setParameter(parameterIndex, NodeFactoryExtra.doubleToNode((Double) value));
        } else if (value instanceof Date) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(((Date) value).getTime());
            this.setParameter(parameterIndex, NodeFactoryExtra.dateTimeToNode(c));
        } else if (value instanceof Time) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(((Time) value).getTime());
            this.setParameter(parameterIndex, NodeFactoryExtra.timeToNode(c));
        } else if (value instanceof Calendar) {
            this.setParameter(parameterIndex, NodeFactoryExtra.dateTimeToNode((Calendar) value));
        } else if (value instanceof URL) {
            this.setParameter(parameterIndex, NodeFactory.createURI(value.toString()));
        } else if (value instanceof URI) {
            this.setParameter(parameterIndex, NodeFactory.createURI(value.toString()));
        } else if (value instanceof IRI) {
            this.setParameter(parameterIndex, NodeFactory.createURI(value.toString()));
        } else {
            throw new SQLException(
                    "setObject() received a value that could not be converted to a RDF node for use in a SPARQL query");
        }
    }

    @Override
    public void setObject(int parameterIndex, Object value, int targetSqlType) throws SQLException {
        if (value == null) throw new SQLException("Setting a null value is not permitted");
        
        try {
            switch (targetSqlType) {
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
                throw new SQLException("The provided SQL Target Type cannot be translated into an appropriate RDF term type");
            case Types.BIGINT:
                if (value instanceof Long) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((Long) value));
                } else if (value instanceof Integer) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((long)(Integer)value));
                } else if (value instanceof Short) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((long)(Short)value));
                } else if (value instanceof Byte) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((long)(Byte)value));
                } else if (value instanceof Node) {
                    long l = JdbcNodeUtils.toLong((Node) value);
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode(l));
                } else if (value instanceof String) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode(Long.parseLong((String)value)));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired target type");
                }
                break;
            case Types.BOOLEAN:
                if (value instanceof Boolean) {
                    this.setParameter(parameterIndex,
                            NodeFactory.createLiteral(Boolean.toString((Boolean) value), XSDDatatype.XSDboolean));
                } else if (value instanceof Node) {
                    boolean b = JdbcNodeUtils.toBoolean((Node) value);
                    this.setParameter(parameterIndex,
                            NodeFactory.createLiteral(Boolean.toString(b), XSDDatatype.XSDboolean));
                } else if (value instanceof String) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Boolean.toString(Boolean.parseBoolean((String)value)), XSDDatatype.XSDboolean));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired target type");
                }
                break;
            case Types.DATE:
                if (value instanceof Date) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(((Date) value).getTime());
                    this.setParameter(parameterIndex, NodeFactoryExtra.dateTimeToNode(c));
                } else if (value instanceof Node) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(JdbcNodeUtils.toDate((Node)value).getTime());
                    this.setParameter(parameterIndex, NodeFactoryExtra.dateTimeToNode(c));
                } else if (value instanceof Time) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(((Time) value).getTime());
                    this.setParameter(parameterIndex, NodeFactoryExtra.timeToNode(c));
                } else if (value instanceof Calendar) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.dateTimeToNode((Calendar) value));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired target type");
                }
                break;
            case Types.DECIMAL:
                if (value instanceof BigDecimal) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(((BigDecimal)value).toPlainString(), XSDDatatype.XSDdecimal));
                } else if (value instanceof Node) {
                    BigDecimal d = JdbcNodeUtils.toDecimal((Node)value);
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(d.toPlainString(), XSDDatatype.XSDdecimal));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired target type");
                }
                break;
            case Types.DOUBLE:
                if (value instanceof Double) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.doubleToNode((Double)value));
                } else if (value instanceof Float) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.doubleToNode((double)(Float)value));
                } else if (value instanceof Node) {
                    Double d = JdbcNodeUtils.toDouble((Node)value);
                    this.setParameter(parameterIndex, NodeFactoryExtra.doubleToNode(d));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired target type");
                }
                break;
            case Types.FLOAT:
                if (value instanceof Float) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.floatToNode((Float)value));
                } else if (value instanceof Node) {
                    Float f = JdbcNodeUtils.toFloat((Node)value);
                    this.setParameter(parameterIndex, NodeFactoryExtra.floatToNode(f));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired target type");
                }
                break;
            case Types.INTEGER:
                if (value instanceof Integer) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((Integer)value));
                } else if (value instanceof Short) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((int)(Short)value));
                } else if (value instanceof Byte) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((int)(Byte)value));
                } else if (value instanceof Node) {
                    Integer i = JdbcNodeUtils.toInt((Node)value);
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode(i));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired target type");
                }
                break;
            case Types.JAVA_OBJECT:
                if (value instanceof Node) {
                    this.setParameter(parameterIndex, (Node) value);
                } else if (value instanceof RDFNode) {
                    this.setParameter(parameterIndex, ((RDFNode) value).asNode());
                } else if (value instanceof String) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral((String) value));
                } else if (value instanceof URL) {
                    this.setParameter(parameterIndex, NodeFactory.createURI(((URL) value).toString()));
                } else if (value instanceof URI) {
                    this.setParameter(parameterIndex, NodeFactory.createURI(((URI)value).toString()));
                } else if (value instanceof IRI) {
                    this.setParameter(parameterIndex, NodeFactory.createURI(((IRI)value).toString()));
                } else if (value instanceof BigDecimal) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(((BigDecimal)value).toPlainString(), XSDDatatype.XSDdecimal));
                } else if (value instanceof Boolean) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Boolean.toString(((Boolean)value)), XSDDatatype.XSDboolean));
                } else if (value instanceof Byte) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Byte.toString((Byte)value), XSDDatatype.XSDbyte));
                } else if (value instanceof Double) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.doubleToNode((Double)value));
                } else if (value instanceof Float) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.floatToNode((Float)value));
                } else if (value instanceof Short) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Short.toString((Short)value), XSDDatatype.XSDshort));
                } else if (value instanceof Integer) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((Integer)value));
                } else if (value instanceof Long) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.intToNode((Long)value));
                } else if (value instanceof Date) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(((Date)value).getTime());
                    this.setParameter(parameterIndex, NodeFactoryExtra.dateTimeToNode(c));
                } else if (value instanceof Time) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(((Time)value).getTime());
                    this.setParameter(parameterIndex, NodeFactoryExtra.timeToNode(c));
                } else if (value instanceof Calendar) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.dateTimeToNode((Calendar)value));
                } else { 
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(value.toString()));
                }
                break;
            case Types.CHAR:
            case Types.NVARCHAR:
            case Types.VARCHAR:
                this.setParameter(parameterIndex, NodeFactory.createLiteral(value.toString()));
                break;
            case Types.SMALLINT:
                if (value instanceof Short) {
                    Short s = (Short)value;
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Short.toString(s), XSDDatatype.XSDshort));
                } else if (value instanceof Byte) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Short.toString((short)(Byte)value), XSDDatatype.XSDshort));
                } else if (value instanceof Node) {
                    Short s = JdbcNodeUtils.toShort((Node)value);
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Short.toString(s), XSDDatatype.XSDshort));
                } else if (value instanceof String) {
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Short.toString(Short.parseShort((String)value)), XSDDatatype.XSDshort));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired type");
                }
                break;
            case Types.TIME:
                if (value instanceof Time) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(((Time)value).getTime());
                    this.setParameter(parameterIndex, NodeFactoryExtra.timeToNode(c));
                } else if (value instanceof Node) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(JdbcNodeUtils.toDate((Node)value).getTime());
                    this.setParameter(parameterIndex, NodeFactoryExtra.timeToNode(c));
                } else if (value instanceof Date) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(((Date)value).getTime());
                    this.setParameter(parameterIndex, NodeFactoryExtra.timeToNode(c));
                } else if (value instanceof Calendar) {
                    this.setParameter(parameterIndex, NodeFactoryExtra.timeToNode((Calendar)value));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired type");
                }
                break;
            case Types.TINYINT:
                if (value instanceof Byte) {
                    Byte b = (Byte)value;
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Byte.toString(b), XSDDatatype.XSDbyte));
                } else if (value instanceof Node) {
                    Byte b = JdbcNodeUtils.toByte((Node)value);
                    this.setParameter(parameterIndex, NodeFactory.createLiteral(Byte.toString(b), XSDDatatype.XSDbyte));
                } else {
                    throw new SQLException("The given value is not marshallable to the desired type");
                }
                break;
            default:
                throw new SQLException("Cannot translate an unknown SQL Target Type into an appropriate RDF term type");
            }
        } catch (SQLException e) {
            // Throw as-is
            throw e;
        } catch (Throwable e) {
            // Wrap as SQL Exception
            throw new SQLException("Unexpected error trying to marshal a value to the desired target type", e);
        }
    }

    @Override
    public void setObject(int parameterIndex, Object value, int arg2, int arg3) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setRef(int parameterIndex, Ref value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setRowId(int parameterIndex, RowId value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setShort(int parameterIndex, short value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactory.createLiteral(Short.toString(value), XSDDatatype.XSDshort));
    }

    @Override
    public void setString(int parameterIndex, String value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactory.createLiteral(value));
    }

    @Override
    public void setTime(int parameterIndex, Time value) throws SQLException {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(value.getTime());
        this.setParameter(parameterIndex, NodeFactoryExtra.timeToNode(c));
    }

    @Override
    public void setTime(int parameterIndex, Time value, Calendar arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp value) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp value, Calendar arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setURL(int parameterIndex, URL value) throws SQLException {
        this.setParameter(parameterIndex, NodeFactory.createURI(value.toString()));
    }

    @Deprecated
    @Override
    public void setUnicodeStream(int parameterIndex, InputStream value, int arg2) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * Gets a copy of the underlying {@link ParameterizedSparqlString} used to
     * implement this prepared statement
     * 
     * @return Copy of the underlying string
     */
    public ParameterizedSparqlString getParameterizedString() {
        return this.sparqlStr.copy();
    }
}
