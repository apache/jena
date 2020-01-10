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

import org.apache.jena.jdbc.connections.JenaConnection;
import org.apache.jena.jdbc.metadata.results.MetaResultSet;
import org.apache.jena.vocabulary.XSD ;

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
    public boolean isWrapperFor(Class<?> arg0) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean allProceduresAreCallable() {
        // Callable procedures not supported in SPARQL
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() {
        // There is a single table in RDF (the quads table) and it is selectable
        return true;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() {
        // Auto-commit failure does not close all result sets
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() {
        // SPARQL Update causes a commit by default for non-transactional
        // connections
        return true;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() {
        // SPARQL Update is not ignored for non-transactional connections
        return false;
    }

    @Override
    public boolean deletesAreDetected(int arg0) {
        // Since modification of result sets is not supported we can report
        // true for the ability to detect row deletes
        return true;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
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
    public String getCatalogSeparator() {
        // Use an empty string to indicate not applicable
        return "";
    }

    @Override
    public String getCatalogTerm() {
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
    public final Connection getConnection() {
        return this.connection;
    }

    @Override
    public ResultSet getCrossReference(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5)
            throws SQLException {
        return new MetaResultSet(MetadataSchema.getCrossReferenceColumns());
    }

    @Override
    public abstract int getDatabaseMajorVersion();

    @Override
    public abstract int getDatabaseMinorVersion();

    @Override
    public abstract String getDatabaseProductName();

    @Override
    public abstract String getDatabaseProductVersion();

    @Override
    public int getDefaultTransactionIsolation() {
        return Connection.TRANSACTION_NONE;
    }

    @Override
    public abstract int getDriverMajorVersion();

    @Override
    public abstract int getDriverMinorVersion();

    @Override
    public abstract String getDriverName();

    @Override
    public abstract String getDriverVersion();

    @Override
    public ResultSet getExportedKeys(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getExportedKeyColumns());
    }

    @Override
    public String getExtraNameCharacters() {
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
    public String getIdentifierQuoteString() {
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
    public final int getJDBCMajorVersion() {
        return 4;
    }

    @Override
    public final int getJDBCMinorVersion() {
        return 0;
    }

    @Override
    public int getMaxBinaryLiteralLength() {
        // No limit on RDF term sizes
        return NO_LIMIT;
    }

    @Override
    public int getMaxCatalogNameLength() {
        // No limit on catalog name lengths because we don't
        // really support catalogs
        return NO_LIMIT;
    }

    @Override
    public int getMaxCharLiteralLength() {
        // No limit on RDF term sizes
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnNameLength() {
        // No limit on column name lengths
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnsInGroupBy() {
        // SPARQL allows arbitrarily many columns in a GROUP BY
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnsInIndex() {
        // RDF stores typically index on up to 4 columns since that is all we
        // have
        return 4;
    }

    @Override
    public int getMaxColumnsInOrderBy() {
        // SPARQL allows arbitrarily many columns in ORDER BY
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnsInSelect() {
        // SPARQL allows arbitrarily many columns in SELECT clause
        return NO_LIMIT;
    }

    @Override
    public int getMaxColumnsInTable() {
        // RDF stores have up to 4 columns
        return 4;
    }

    @Override
    public int getMaxConnections() {
        // Max connections will typically be unlimited
        return NO_LIMIT;
    }

    @Override
    public int getMaxCursorNameLength() {
        // Named cursors aren't supported so there is no limit
        return UNKNOWN_LIMIT;
    }

    @Override
    public int getMaxIndexLength() {
        // RDF stores typically have no limit on index size, they are as big as
        // they need to be
        return NO_LIMIT;
    }

    @Override
    public int getMaxProcedureNameLength() {
        // Procedures aren't supported so unknown
        return UNKNOWN_LIMIT;
    }

    @Override
    public int getMaxRowSize() {
        // No limit on triple size
        return NO_LIMIT;
    }

    @Override
    public int getMaxSchemaNameLength() {
        // We don't really support schemas so there is no limit
        return NO_LIMIT;
    }

    @Override
    public int getMaxStatementLength() {
        // SPARQL Queries/Updates may be arbitrarily large
        return NO_LIMIT;
    }

    @Override
    public int getMaxStatements() {
        // We don't impose any limit on this
        return NO_LIMIT;
    }

    @Override
    public int getMaxTableNameLength() {
        // We don't support tables so there is no limit
        return NO_LIMIT;
    }

    @Override
    public int getMaxTablesInSelect() {
        // No limit
        return NO_LIMIT;
    }

    @Override
    public int getMaxUserNameLength() {
        // Authentication is an implementation specific detail so unknown
        return UNKNOWN_LIMIT;
    }

    @Override
    public String getNumericFunctions() {
        return String.join(",", SPARQL_NUMERIC_FUNCTIONS);
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
    public String getProcedureTerm() {
        // Not supported
        return null;
    }

    @Override
    public ResultSet getProcedures(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getProcedureColumns());
    }

    @Override
    public int getResultSetHoldability() {
        return JenaConnection.DEFAULT_HOLDABILITY;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() {
        // Not supported
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }

    @Override
    public String getSQLKeywords() {
        // TODO Use http://developer.mimer.com/validator/sql-reserved-words.tml
        // as a reference to remove those that also count as SQL Keywords
        return String.join(",", SPARQL_KEYWORDS);
    }

    @Override
    public int getSQLStateType() {
        return sqlStateXOpen;
    }

    @Override
    public String getSchemaTerm() {
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
    public String getSearchStringEscape() {
        // Does not apply to SPARQL
        return "";
    }

    @Override
    public String getStringFunctions() {
        return String.join(",", SPARQL_STR_FUNCTIONS);
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
    public String getSystemFunctions() {
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
    public String getTimeDateFunctions() {
        return String.join(",", SPARQL_DATETIME_FUNCTIONS);
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
    public abstract String getURL();

    @Override
    public String getUserName() {
        // No authentication used by default
        return null;
    }

    @Override
    public ResultSet getVersionColumns(String arg0, String arg1, String arg2) throws SQLException {
        return new MetaResultSet(MetadataSchema.getVersionColumns());
    }

    @Override
    public boolean insertsAreDetected(int arg0) {
        // We can't detect inserts that happen while streaming results
        return false;
    }

    @Override
    public boolean isCatalogAtStart() {
        // We don't really support catalogs so we'll say yes
        return true;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.connection.isReadOnly();
    }

    @Override
    public boolean locatorsUpdateCopy() {
        // SPARQL doesn't support the LOB types so return false
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() {
        // Concatenating nulls (i.e. unbound/type error) in SPARQL results
        // leads to nulls
        return true;
    }

    @Override
    public boolean nullsAreSortedAtEnd() {
        // SPARQL sort order puts nulls (i.e. unbound) first
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() {
        // SPARQL sort order puts nulls (i.e. unbound) first
        return true;
    }

    @Override
    public boolean nullsAreSortedHigh() {
        // SPARQL sort order puts nulls (i.e. unbound) first
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() {
        // SPARQL sort order puts nulls (i.e. unbound) first
        return true;
    }

    @Override
    public boolean othersDeletesAreVisible(int arg0) {
        // Since results are streamed it may be possible to see deletes from
        // others depending on the underlying implementation
        return true;
    }

    @Override
    public boolean othersInsertsAreVisible(int arg0) {
        // Since results are streamed it may be possible to see inserts from
        // others depending on the underlying implementation
        return true;
    }

    @Override
    public boolean othersUpdatesAreVisible(int arg0) {
        // Since results are streamed it may be possible to see updates from
        // others depending on the underlying implementation
        return true;
    }

    @Override
    public boolean ownDeletesAreVisible(int arg0) {
        // Since results are streamed it may be possible to see deletes from
        // ourselves depending on the underlying implementation
        return true;
    }

    @Override
    public boolean ownInsertsAreVisible(int arg0) {
        // Since results are streamed it may be possible to see inserts from
        // ourselves depending on the underlying implementation
        return true;
    }

    @Override
    public boolean ownUpdatesAreVisible(int arg0) {
        // Since results are streamed it may be possible to see deletes from
        // others depending on the underlying implementation
        return true;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() {
        // We don't support identifiers in the way that JDBC means so we say
        // false
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() {
        // Schema alteration is not supported
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() {
        // Schema alteration is not supported
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() {
        // Batch updates are implemented
        return true;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() {
        // We don't really support catalogs so using them in SPARQL Update is
        // not permitted
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() {
        // Custom indexes are not supported
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() {
        // SPARQL has no privilege definition statements
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() {
        // SPARQL has no procedure calls
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        // SPARQL has no table definition statements
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() {
        // SPARQL requires aliasing for computed columns
        return true;
    }

    @Override
    public boolean supportsConvert() {
        // JDBC convert is not supported
        return false;
    }

    @Override
    public boolean supportsConvert(int arg0, int arg1) {
        // JDBC convert is not supported
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() {
        // SPARQL supports sub-queries
        return true;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        // SPARQL update may be used within a transaction
        return true;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() {
        // Transactions may consist only of SPARQL updates
        return true;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() {
        // We don't support tables as such so no
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() {
        // SPARQL allows expressions in ORDER BY
        return true;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() {
        // SPARQL supports all sorts of joins
        return true;
    }

    @Override
    public boolean supportsGetGeneratedKeys() {
        // SPARQL has no notion of auto-generated keys (you can argue that
        // UUID() counts) but we certainly can't return them
        return false;
    }

    @Override
    public boolean supportsGroupBy() {
        // SPARQL supports GROUP BY
        return true;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() {
        // You can GROUP BY a column that you don't select in SPARQL
        return true;
    }

    @Override
    public boolean supportsGroupByUnrelated() {
        // You can GROUP BY a column that you don't select in SPARQL
        return true;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() {
        // Integrity Enhancement SQL is not supported
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() {
        // No LIKE in SPARQL
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() {
        // SPARQL supports all kinds of joins
        return true;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() {
        // We don't support SQL
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() {
        // We have no direct equivalent to SQL identifiers
        return false;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() {
        // We have no direct equivalent to SQL identifiers
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() {
        // We support multiple open results
        return true;
    }

    @Override
    public boolean supportsMultipleResultSets() {
        // We don't support multiple result sets from a single execute() call,
        // we do support this from executeBatch()
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() {
        // In principle yes this is possible though exact behaviour may vary by
        // underlying implementation
        return true;
    }

    @Override
    public boolean supportsNamedParameters() {
        // We don't support callable statements
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() {
        // All columns in a RDF store are non-nullable
        return true;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        // Cursors may be closed depending on the type of commit
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        // Cursors may be closed depending on the type of commit
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        // Statements remain open across commits
        return true;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        // Statements remain open across rollbacks
        return true;
    }

    @Override
    public boolean supportsOrderByUnrelated() {
        // SPARQL allows ORDER BY on a column that you don't SELECT
        return true;
    }

    @Override
    public boolean supportsOuterJoins() {
        // SPARQL supports all kinds of joins
        return true;
    }

    @Override
    public boolean supportsPositionedDelete() {
        // We don't support deleting from result set
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() {
        // We don't support updating from result set
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) {
        // We only support read-only result sets
        if (concurrency != ResultSet.CONCUR_READ_ONLY)
            return false;
        return supportsResultSetType(type);
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) {
        // Both kinds of holdability are supported
        return true;
    }

    @Override
    public boolean supportsResultSetType(int type) {
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
    public boolean supportsSavepoints() {
        // No notion of savepoints
        return false;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() {
        // We don't really support schemas
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() {
        // RDF stores don't allow custom indices
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() {
        // SPARQL has no privilege definition statements
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() {
        // SPARQL has no procedure calls
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() {
        // We don't really support schemas
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() {
        // No SPARQL equivalent
        return false;
    }

    @Override
    public boolean supportsStatementPooling() {
        // We don't do pooling
        return false;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() {
        // Stored procedures are not supported in SPARQL
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() {
        // Stored procedures are not supported in SPARQL
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() {
        // Can't use subqueries in this way in SPARQL
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() {
        // SPARQL does allow sub-queries in EXISTS though strictly speaking our
        // EXISTS has no relation to the SQL equivalent
        return true;
    }

    @Override
    public boolean supportsSubqueriesInIns() {
        // Can't use subqueries in this way in SPARQL
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() {
        // I have no idea what this mean so assume we can't use sub-queries this
        // way in SPARQL
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() {
        // We don't really support tables
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int arg0) {
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
    public boolean supportsTransactions() {
        // Currently transactions are not supported
        return false;
    }

    @Override
    public boolean supportsUnion() {
        // SPARQL supports UNION
        return true;
    }

    @Override
    public boolean supportsUnionAll() {
        // No SPARQL equivalent of UNION ALL
        return false;
    }

    @Override
    public boolean updatesAreDetected(int arg0) {
        // Updates are never detectable
        return false;
    }

    @Override
    public abstract boolean usesLocalFilePerTable();

    @Override
    public abstract boolean usesLocalFiles();

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
            throws SQLException {
        return new MetaResultSet(MetadataSchema.getPsuedoColumnColumns());
    }

    @Override
    public boolean generatedKeyAlwaysReturned() {
        // We don't support returning keys
        return false;
    }
}
