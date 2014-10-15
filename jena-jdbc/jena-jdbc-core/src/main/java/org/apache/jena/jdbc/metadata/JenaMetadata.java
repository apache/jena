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

package org.apache.jena.jdbc.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.metadata.results.MetaResultSet;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Database metadata for Jena JDBC connections
 */
public abstract class JenaMetadata implements DatabaseMetaData {

    /**
     * Constant for the term used for catalogues
     */
    public static final String CATALOG_TERM = "RDF Store";

    /**
     * Constant for the term used for schemas
     */
    public static final String SCHEMA_TERM = "Dataset";

    /**
     * Constant for the default catalog which is the only catalog we report as
     * existing by default
     */
    public static final String DEFAULT_CATALOG = "RDF";

    /**
     * Constant for the default schema which is the only schema we report as
     * existing by default
     */
    public static final String DEFAULT_SCHEMA = "Dataset";

    protected static final int NO_LIMIT = 0;

    protected static final int UNKNOWN_LIMIT = 0;

    /**
     * Constants for SPARQL Keywords
     */
    protected static final String[] SPARQL_KEYWORDS = new String[] { "BASE", "PREFIX", "SELECT", "DISTINCT", "REDUCED", "AS",
            "CONSTRUCT", "DESCRIBE", "ASK", "FROM", "NAMED", "WHERE", "GROUP", "BY", "HAVING", "ORDER", "ASC", "DESC", "LIMIT",
            "OFFSET", "VALUES", "LOAD", "SILENT", "INTO", "GRAPH", "CLEAR", "DROP", "CREATE", "ADD", "MOVE", "COPY",
            "INSERT DATA", "DELETE DATA", "DELETE WHERE", "WITH", "INSERT", "USING", "DEFAULT", "ALL", "OPTIONAL", "SERVICE",
            "BIND", "UNION", "UNDEF", "MINUS", "EXISTS", "NOT EXISTS", "FILTER", "a", "IN", "NOT IN", "STR", "LANG",
            "LANGMATCHES", "DATATYPE", "BOUND", "IRI", "URI", "BNODE", "RAND", "ABS", "CEIL", "FLOOR", "ROUND", "CONCAT",
            "STRLEN", "UCASE", "LCASE", "ENCODE_FOR_URI", "CONTAINS", "STRSTARTS", "STRENDS", "STRBEFORE", "STRAFTER", "YEAR",
            "MONTH", "DAY", "HOURS", "MINUTES", "SECONDS", "TIMEZONE", "TZ", "NOW", "UUID", "STRUUID", "MD5", "SHA1", "SHA256",
            "SHA384", "SHA512", "COALESCE", "IF", "STRLANG", "STRDT", "SAMETERM", "ISIRI", "ISURI", "ISBLANK", "REGEX", "SUBSTR",
            "REPLACE", "COUNT", "SUM", "MIN", "MAX", "AVG", "SAMPLE", "GROUP_CONCAT", "SEPARATOR", "true", "false" };

    /**
     * Constants for SPARQL numeric functions
     */
    protected static final String[] SPARQL_NUMERIC_FUNCTIONS = new String[] { "ABS", "CEIL", "FLOOR", "RAND", "ROUND" };

    /**
     * Constants for SPARQL string functions
     */
    protected static final String[] SPARQL_STR_FUNCTIONS = new String[] { "STR", "LANG", "LANGMATCHES", "CONCAT", "STRLEN",
            "UCASE", "LCASE", "ENCODE_FOR_URI", "CONTAINS", "STRSTARTS", "STRENDS", "STRBEFORE", "STRAFTER", "REGEX", "SUBSTR",
            "REPLACE" };

    protected static final String[] SPARQL_DATETIME_FUNCTIONS = new String[] { "YEAR", "MONTH", "DAY", "HOURS", "MINUTES",
            "SECONDS", "TIMEZONE", "TZ", "NOW" };

    private JenaConnection connection;

    /**
     * Creates new connection metadata
     * 
     * @param connection
     *            Connection
     * @throws SQLException
     */
    public JenaMetadata(JenaConnection connection) throws SQLException {
        if (connection == null)
            throw new SQLException("Connection cannot be null");
        this.connection = connection;
    }

    /**
     * Gets the associated Jena connection instance
     * 
     * @return Jena connection
     */
    public JenaConnection getJenaConnection() {
        return this.connection;
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
    public boolean allProceduresAreCallable() throws SQLException {
        // Callable procedures not supported in SPARQL
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        // There is a single table in RDF (the quads table) and it is selectable
        return true;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        // Auto-commit failure does not close all result sets
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        // SPARQL Update causes a commit by default for non-transactional
        // connections
        return true;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        // SPARQL Update is not ignored for non-transactional connections
        return false;
    }

    @Override
    public boolean deletesAreDetected(int arg0) throws SQLException {
        // Since modification of result sets is not supported we can report
        // true for the ability to detect row deletes
        return true;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        // There is no max row size in RDF/SPARQL
        return true;
    }

    @Override
    public ResultSet getAttributes(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        return new MetaResultSet(MetadataSchema.getAttributeColumns());
    }

    @Override
    public ResultSet getBestRowIdentifier(String arg0, String arg1, String arg2, int arg3, boolean arg4) throws SQLException {
        return new MetaResultSet(MetadataSchema.getBestRowIdentifierColumns());
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        // Use an empty string to indicate not applicable
        return "";
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return CATALOG_TERM;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        return new MetaResultSet(MetadataSchema.getCatalogsColumns(), new Object[][] { { DEFAULT_CATALOG } });
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return new MetaResultSet(MetadataSchema.getClientInfoPropertyColumns());
    }

    @Override
    public ResultSet getColumnPrivileges(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        return new MetaResultSet(MetadataSchema.getColumnPrivilegeColumns());
    }

    @Override
    public ResultSet getColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        return new MetaResultSet(MetadataSchema.getColumnColumns());
    }

    @Override
    public final Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    public ResultSet getCrossReference(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5)
            throws SQLException {
        return new MetaResultSet(MetadataSchema.getCrossReferenceColumns());
    }

    @Override
    public abstract int getDatabaseMajorVersion() throws SQLException;

    @Override
    public abstract int getDatabaseMinorVersion() throws SQLException;

    @Override
    public abstract String getDatabaseProductName() throws SQLException;

    @Override
    public abstract String getDatabaseProductVersion() throws SQLException;

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public abstract int getDriverMajorVersion();

    @Override
    public abstract int getDriverMinorVersion();

    @Override
    public abstract String getDriverName() throws SQLException;

    @Override
    public abstract String getDriverVersion() throws SQLException;

    @Override
    public ResultSet getExportedKeys(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getExportedKeyColumns());
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        // Since SPARQL doesn't really have a notion of identifiers like SQL
        // does we return that there are no extra name characters
        return "";
    }

    @Override
    public ResultSet getFunctionColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        return new MetaResultSet(MetadataSchema.getFunctionColumnColumns());
    }

    @Override
    public ResultSet getFunctions(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getFunctionColumns());
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        // Not supported in SPARQL so return space per the JDBC javadoc
        return " ";
    }

    @Override
    public ResultSet getImportedKeys(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getImportedKeyColumns());
    }

    @Override
    public ResultSet getIndexInfo(String arg0, String arg1, String arg2, boolean arg3, boolean arg4) throws SQLException {
        return new MetaResultSet(MetadataSchema.getIndexInfoColumns());
    }

    @Override
    public final int getJDBCMajorVersion() throws SQLException {
        return 4;
    }

    @Override
    public final int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        // No limit on RDF term sizes
        return NO_LIMIT;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        // No limit on catalog name lengths because we don't
        // really support catalogs
        return NO_LIMIT;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        // No limit on RDF term sizes
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        // No limit on column name lengths
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        // SPARQL allows arbitrarily many columns in a GROUP BY
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        // RDF stores typically index on up to 4 columns since that is all we
        // have
        return 4;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        // SPARQL allows arbitrarily many columns in ORDER BY
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        // SPARQL allows arbitrarily many columns in SELECT clause
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        // RDF stores have up to 4 columns
        return 4;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        // Max connections will typically be unlimited
        return NO_LIMIT;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        // Named cursors aren't supported so there is no limit
        return UNKNOWN_LIMIT;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        // RDF stores typically have no limit on index size, they are as big as
        // they need to be
        return NO_LIMIT;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        // Procedures aren't supported so unknown
        return UNKNOWN_LIMIT;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        // No limit on triple size
        return NO_LIMIT;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        // We don't really support schemas so there is no limit
        return NO_LIMIT;
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        // SPARQL Queries/Updates may be arbitrarily large
        return NO_LIMIT;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        // We don't impose any limit on this
        return NO_LIMIT;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        // We don't support tables so there is no limit
        return NO_LIMIT;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        // No limit
        return NO_LIMIT;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        // Authentication is an implementation specific detail so unknown
        return UNKNOWN_LIMIT;
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return StrUtils.strjoin(",", SPARQL_NUMERIC_FUNCTIONS);
    }

    @Override
    public ResultSet getPrimaryKeys(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getPrimaryKeyColumns());
    }

    @Override
    public ResultSet getProcedureColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        return new MetaResultSet(MetadataSchema.getProcedureColumnColumns());
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        // Not supported
        return null;
    }

    @Override
    public ResultSet getProcedures(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getProcedureColumns());
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return JenaConnection.DEFAULT_HOLDABILITY;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        // Not supported
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        // TODO Use http://developer.mimer.com/validator/sql-reserved-words.tml
        // as a reference to remove those that also count as SQL Keywords
        return StrUtils.strjoin(",", SPARQL_KEYWORDS);
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return sqlStateXOpen;
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return SCHEMA_TERM;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return new MetaResultSet(MetadataSchema.getSchemaColumns(), new Object[][] { { DEFAULT_SCHEMA, DEFAULT_CATALOG } });
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        if (DEFAULT_CATALOG.equals(catalog)) {
            if (schemaPattern == null || DEFAULT_SCHEMA.equals(schemaPattern)) {
                return this.getSchemas();
            } else {
                return new MetaResultSet(MetadataSchema.getSchemaColumns());
            }
        } else {
            return new MetaResultSet(MetadataSchema.getSchemaColumns());
        }
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        // Does not apply to SPARQL
        return "";
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return StrUtils.strjoin(",", SPARQL_STR_FUNCTIONS);
    }

    @Override
    public ResultSet getSuperTables(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getSuperTableColumns());
    }

    @Override
    public ResultSet getSuperTypes(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getSuperTypeColumns());
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        // No system functions supported
        return "";
    }

    @Override
    public ResultSet getTablePrivileges(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getTablePrivilegeColumns());
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        return new MetaResultSet(MetadataSchema.getTableTypeColumns());
    }

    @Override
    public ResultSet getTables(String arg0, String arg1, String arg2, String[] arg3) throws SQLException {
        return new MetaResultSet(MetadataSchema.getTableColumns());
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return StrUtils.strjoin(",", SPARQL_DATETIME_FUNCTIONS);
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        // TYPE_NAME String => Type name
        // DATA_TYPE int => SQL data type from java.sql.Types
        // PRECISION int => maximum precision
        // LITERAL_PREFIX String => prefix used to quote a literal (may be null)
        // LITERAL_SUFFIX String => suffix used to quote a literal (may be null)
        // CREATE_PARAMS String => parameters used in creating the type (may be
        // null)
        // NULLABLE short => can you use NULL for this type.
        // typeNoNulls - does not allow NULL values
        // typeNullable - allows NULL values
        // typeNullableUnknown - nullability unknown
        // CASE_SENSITIVE boolean=> is it case sensitive.
        // SEARCHABLE short => can you use "WHERE" based on this type:
        // typePredNone - No support
        // typePredChar - Only supported with WHERE .. LIKE
        // typePredBasic - Supported except for WHERE .. LIKE
        // typeSearchable - Supported for all WHERE ..
        // UNSIGNED_ATTRIBUTE boolean => is it unsigned.
        // FIXED_PREC_SCALE boolean => can it be a money value.
        // AUTO_INCREMENT boolean => can it be used for an auto-increment value.
        // LOCAL_TYPE_NAME String => localized version of type name (may be
        // null)
        // MINIMUM_SCALE short => minimum scale supported
        // MAXIMUM_SCALE short => maximum scale supported
        // SQL_DATA_TYPE int => unused
        // SQL_DATETIME_SUB int => unused
        // NUM_PREC_RADIX int => usually 2 or 10

        // Report types we can marshal appropriately
        return new MetaResultSet(MetadataSchema.getTypeInfoColumns(), new Object[][] {
                { XSD.xboolean.toString(), Types.BOOLEAN, 0, null, null, null, (short) typeNullable, false,
                        (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0, 0, 0 },
                { XSD.xbyte.toString(), Types.TINYINT, Byte.toString(Byte.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0,
                        0, 0 },
                { XSD.date.toString(), Types.DATE, 0, "\"", "\"", null, (short) typeNullable, false, (short) typeSearchable,
                        false, false, false, null, (short) 0, (short) 0, 0, 0, 0 },
                { XSD.dateTime.toString(), Types.DATE, 0, "\"", "\"", null, (short) typeNullable, false, (short) typeSearchable,
                        false, false, false, null, (short) 0, (short) 0, 0, 0, 0 },
                { XSD.decimal.toString(), Types.DECIMAL, 16, null, null, null, (short) typeNullable, false,
                        (short) typeSearchable, false, false, false, null, (short) 0, (short) 16, 0, 0, 10 },
                { XSD.xdouble.toString(), Types.DOUBLE, 16, null, null, null, (short) typeNullable, false,
                        (short) typeSearchable, false, false, false, null, (short) 0, (short) 16, 0, 0, 10 },
                { XSD.xfloat.toString(), Types.FLOAT, 15, "\"", "\"", null, (short) typeNullable, false, (short) typeSearchable,
                        false, false, false, null, (short) 0, (short) 7, 0, 0, 10 },
                { XSD.xshort.toString(), Types.INTEGER, Integer.toString(Integer.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.integer.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), null, null, null,
                        (short) typeNullable, false, (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.xlong.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.xint.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.negativeInteger.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.nonNegativeInteger.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, true, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.nonPositiveInteger.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.positiveInteger.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, true, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.unsignedByte.toString(), Types.TINYINT, Byte.toString(Byte.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, true, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.unsignedInt.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, true, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.unsignedLong.toString(), Types.BIGINT, Long.toString(Long.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, true, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.unsignedShort.toString(), Types.INTEGER, Integer.toString(Integer.MAX_VALUE).length(), "\"", "\"", null,
                        (short) typeNullable, false, (short) typeSearchable, true, false, false, null, (short) 0, (short) 0, 0,
                        0, 10 },
                { XSD.xstring.toString(), Types.NVARCHAR, 0, "\"", "\"", null, (short) typeNullable, true,
                        (short) typeSearchable, false, false, false, null, (short) 0, (short) 0, 0, 0, 0 },
                { XSD.time.toString(), Types.TIME, 0, "\"", "\"", null, (short) typeNullable, false, (short) typeSearchable,
                        false, false, false, null, (short) 0, (short) 0, 0, 0, 0 }, });
    }

    @Override
    public ResultSet getUDTs(String arg0, String arg1, String arg2, int[] arg3) throws SQLException {
        return new MetaResultSet(MetadataSchema.getUdtColumns());
    }

    @Override
    public abstract String getURL() throws SQLException;

    @Override
    public String getUserName() throws SQLException {
        // No authentication used by default
        return null;
    }

    @Override
    public ResultSet getVersionColumns(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getVersionColumns());
    }

    @Override
    public boolean insertsAreDetected(int arg0) throws SQLException {
        // We can't detect inserts that happen while streaming results
        return false;
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        // We don't really support catalogs so we'll say yes
        return true;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.connection.isReadOnly();
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        // SPARQL doesn't support the LOB types so return false
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        // Concatenating nulls (i.e. unbound/type error) in SPARQL results
        // leads to nulls
        return true;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        // SPARQL sort order puts nulls (i.e. unbound) first
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        // SPARQL sort order puts nulls (i.e. unbound) first
        return true;
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        // SPARQL sort order puts nulls (i.e. unbound) first
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        // SPARQL sort order puts nulls (i.e. unbound) first
        return true;
    }

    @Override
    public boolean othersDeletesAreVisible(int arg0) throws SQLException {
        // Since results are streamed it may be possible to see deletes from
        // others depending on the underlying implementation
        return true;
    }

    @Override
    public boolean othersInsertsAreVisible(int arg0) throws SQLException {
        // Since results are streamed it may be possible to see inserts from
        // others depending on the underlying implementation
        return true;
    }

    @Override
    public boolean othersUpdatesAreVisible(int arg0) throws SQLException {
        // Since results are streamed it may be possible to see updates from
        // others depending on the underlying implementation
        return true;
    }

    @Override
    public boolean ownDeletesAreVisible(int arg0) throws SQLException {
        // Since results are streamed it may be possible to see deletes from
        // ourselves depending on the underlying implementation
        return true;
    }

    @Override
    public boolean ownInsertsAreVisible(int arg0) throws SQLException {
        // Since results are streamed it may be possible to see inserts from
        // ourselves depending on the underlying implementation
        return true;
    }

    @Override
    public boolean ownUpdatesAreVisible(int arg0) throws SQLException {
        // Since results are streamed it may be possible to see deletes from
        // others depending on the underlying implementation
        return true;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        // Schema alteration is not supported
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        // Schema alteration is not supported
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        // Batch updates are implemented
        return true;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        // We don't really support catalogs so using them in SPARQL Update is
        // not permitted
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        // Custom indexes are not supported
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        // SPARQL has no privilege definition statements
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        // SPARQL has no procedure calls
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        // SPARQL has no table definition statements
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        // SPARQL requires aliasing for computed columns
        return true;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        // JDBC convert is not supported
        return false;
    }

    @Override
    public boolean supportsConvert(int arg0, int arg1) throws SQLException {
        // JDBC convert is not supported
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        // SPARQL supports sub-queries
        return true;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        // SPARQL update may be used within a transaction
        return true;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        // Transactions may consist only of SPARQL updates
        return true;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        // We don't support tables as such so no
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        // SPARQL allows expressions in ORDER BY
        return true;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        // SPARQL supports all sorts of joins
        return true;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        // SPARQL has no notion of auto-generated keys (you can argue that
        // UUID() counts) but we certainly can't return them
        return false;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        // SPARQL supports GROUP BY
        return true;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        // You can GROUP BY a column that you don't select in SPARQL
        return true;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        // You can GROUP BY a column that you don't select in SPARQL
        return true;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        // Integrity Enhancement SQL is not supported
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        // No LIKE in SPARQL
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        // SPARQL supports all kinds of joins
        return true;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        // We have no direct equivalent to SQL identifiers
        return false;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        // We have no direct equivalent to SQL identifiers
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        // We support multiple open results
        return true;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        // We don't support multiple result sets from a single execute() call,
        // we do support this from executeBatch()
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        // In principle yes this is possible though exact behaviour may vary by
        // underlying implementation
        return true;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        // We don't support callable statements
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        // All columns in a RDF store are non-nullable
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        // Cursors may be closed depending on the type of commit
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        // Cursors may be closed depending on the type of commit
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        // Statements remain open across commits
        return true;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        // Statements remain open across rollbacks
        return true;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        // SPARQL allows ORDER BY on a column that you don't SELECT
        return true;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        // SPARQL supports all kinds of joins
        return true;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        // We don't support deleting from result set
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        // We don't support updating from result set
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
        // We only support read-only result sets
        if (concurrency != ResultSet.CONCUR_READ_ONLY)
            return false;
        return supportsResultSetType(type);
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        // Both kinds of holdability are supported
        return true;
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException {
        // FORWARD_ONLY and SCROLL_INSENSITIVE are supported
        switch (type) {
        case ResultSet.TYPE_FORWARD_ONLY:
        case ResultSet.TYPE_SCROLL_INSENSITIVE:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        // No notion of savepoints
        return false;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        // We don't really support schemas
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        // RDF stores don't allow custom indices
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        // SPARQL has no privilege definition statements
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        // SPARQL has no procedure calls
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        // We don't really support schemas
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        // No SPARQL equivalent
        return false;
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        // We don't do pooling
        return false;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        // Stored procedures are not supported in SPARQL
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        // Stored procedures are not supported in SPARQL
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        // Can't use subqueries in this way in SPARQL
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        // SPARQL does allow sub-queries in EXISTS though strictly speaking our
        // EXISTS has no relation to the SQL equivalent
        return true;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        // Can't use subqueries in this way in SPARQL
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        // I have no idea what this mean so assume we can't use sub-queries this
        // way in SPARQL
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        // We don't really support tables
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int arg0) throws SQLException {
        // Currently only None or Serializable is supported
        switch (arg0) {
        case Connection.TRANSACTION_NONE:
        case Connection.TRANSACTION_SERIALIZABLE:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        // Currently transactions are not supported
        return false;
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        // SPARQL supports UNION
        return true;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        // No SPARQL equivalent of UNION ALL
        return false;
    }

    @Override
    public boolean updatesAreDetected(int arg0) throws SQLException {
        // Updates are never detectable
        return false;
    }

    @Override
    public abstract boolean usesLocalFilePerTable() throws SQLException;

    @Override
    public abstract boolean usesLocalFiles() throws SQLException;

    @SuppressWarnings("javadoc")
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        return new MetaResultSet(MetadataSchema.getPsuedoColumnColumns());
    }

    // Java 6/7 compatibility
    @SuppressWarnings("javadoc")
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        // We don't support returning keys
        return false;
    }
}
