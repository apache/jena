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

import static java.sql.ResultSetMetaData.*;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.jena.jdbc.results.metadata.columns.BooleanColumn;
import org.apache.jena.jdbc.results.metadata.columns.ColumnInfo;
import org.apache.jena.jdbc.results.metadata.columns.IntegerColumn;
import org.apache.jena.jdbc.results.metadata.columns.ShortIntegerColumn;
import org.apache.jena.jdbc.results.metadata.columns.StringColumn;

/**
 * Helper class containing constants pertaining to the columns returned by
 * various methods of a {@link DatabaseMetaData} implementation
 * 
 */
public class MetadataSchema {

    /**
     * Private constructor prevents instantiation
     */
    private MetadataSchema() {
    }

    private static boolean init = false;

    private static ColumnInfo[] ATTRIBUTE_COLUMNS, BEST_ROW_IDENTIFIER_COLUMNS, CATALOG_COLUMNS, CLIENT_INFO_PROPERTY_COLUMNS,
            COLUMN_COLUMNS, COLUMN_PRIVILEGE_COLUMNS, CROSS_REFERENCE_COLUMNS, EXPORTED_KEY_COLUMNS, FUNCTION_COLUMN_COLUMNS,
            FUNCTION_COLUMNS, IMPORTED_KEY_COLUMNS, INDEX_INFO_COLUMNS, PRIMARY_KEY_COLUMNS, PROCEDURE_COLUMN_COLUMNS,
            PROCEDURE_COLUMNS, PSUEDO_COLUMN_COLUMNS, SCHEMA_COLUMNS, SUPER_TABLE_COLUMNS, SUPER_TYPE_COLUMNS,
            TABLE_PRIVILEGE_COLUMNS, TABLE_TYPE_COLUMNS, TABLE_COLUMNS, TYPE_INFO_COLUMNS, UDT_COLUMNS, VERSION_COLUMNS;

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getAttributes(String, String, String, String)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getAttributeColumns() {
        return ATTRIBUTE_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getBestRowIdentifier(String, String, String, int, boolean)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getBestRowIdentifierColumns() {
        return BEST_ROW_IDENTIFIER_COLUMNS;
    }

    /**
     * Gets the columns for the {@link DatabaseMetaData#getCatalogs()} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getCatalogsColumns() {
        return CATALOG_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getClientInfoProperties()} method
     * 
     * @return Column information
     * 
     */
    public static ColumnInfo[] getClientInfoPropertyColumns() {
        return CLIENT_INFO_PROPERTY_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getColumns(String, String, String, String)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getColumnColumns() {
        return COLUMN_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getColumnPrivileges(String, String, String, String)}
     * method
     * 
     * @return Column Information
     */
    public static ColumnInfo[] getColumnPrivilegeColumns() {
        return COLUMN_PRIVILEGE_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getCrossReference(String, String, String, String, String, String)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getCrossReferenceColumns() {
        return CROSS_REFERENCE_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getExportedKeys(String, String, String)} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getExportedKeyColumns() {
        return EXPORTED_KEY_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getFunctionColumns(String, String, String, String)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getFunctionColumnColumns() {
        return FUNCTION_COLUMN_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getFunctions(String, String, String)} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getFunctionColumns() {
        return FUNCTION_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getImportedKeys(String, String, String)} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getImportedKeyColumns() {
        return IMPORTED_KEY_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getIndexInfo(String, String, String, boolean, boolean)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getIndexInfoColumns() {
        return INDEX_INFO_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getPrimaryKeys(String, String, String)} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getPrimaryKeyColumns() {
        return PRIMARY_KEY_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getProcedureColumns(String, String, String, String)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getProcedureColumnColumns() {
        return PROCEDURE_COLUMN_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getProcedures(String, String, String)} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getProcedureColumns() {
        return PROCEDURE_COLUMNS;
    }

    /**
     * Gets the columns for
     * {@link JenaMetadata#getPseudoColumns(String, String, String, String)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getPsuedoColumnColumns() {
        return PSUEDO_COLUMN_COLUMNS;
    }

    /**
     * Gets the columns for the {@link DatabaseMetaData#getSchemas()} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getSchemaColumns() {
        return SCHEMA_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getSuperTables(String, String, String)} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getSuperTableColumns() {
        return SUPER_TABLE_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getSuperTypes(String, String, String)} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getSuperTypeColumns() {
        return SUPER_TYPE_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getTablePrivileges(String, String, String)}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getTablePrivilegeColumns() {
        return TABLE_PRIVILEGE_COLUMNS;
    }

    /**
     * Gets the columns for the {@link DatabaseMetaData#getTableTypes()} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getTableTypeColumns() {
        return TABLE_TYPE_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getTables(String, String, String, String[])}
     * method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getTableColumns() {
        return TABLE_COLUMNS;
    }

    /**
     * Gets the columns for the {@link DatabaseMetaData#getTypeInfo()} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getTypeInfoColumns() {
        return TYPE_INFO_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getUDTs(String, String, String, int[])} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getUdtColumns() {
        return UDT_COLUMNS;
    }

    /**
     * Gets the columns for the
     * {@link DatabaseMetaData#getVersionColumns(String, String, String)} method
     * 
     * @return Column information
     */
    public static ColumnInfo[] getVersionColumns() {
        return VERSION_COLUMNS;
    }

    /**
     * Static initializer, calls private synchronized static initializer to
     * avoid multi-threaded race conditions
     */
    static {
        init();
    }

    /**
     * Synchronized static initializer is called by the class static
     * initializer, this is done to avoid multi-threaded race conditions and
     * ensure once only initialization
     */
    private static synchronized void init() {
        if (init)
            return;
        try {
            // Define all the columns we are going to use since some of these
            // appear in multiple schema
            ColumnInfo empty = new StringColumn("", columnNullable);
            ColumnInfo typeCat = new StringColumn("TYPE_CATA", columnNullable);
            ColumnInfo typeSchema = new StringColumn("TYPE_SCHEM", columnNullable);
            ColumnInfo typeName = new StringColumn("TYPE_NAME", columnNoNulls);
            ColumnInfo attrName = new StringColumn("ATTR_NAME", columnNoNulls);
            ColumnInfo dataType = new IntegerColumn("DATA_TYPE", columnNoNulls, true);
            ColumnInfo attrTypeName = new StringColumn("ATTR_TYPE_NAME", columnNoNulls);
            ColumnInfo attrSize = new IntegerColumn("ATTR_SIZE", columnNoNulls, true);
            ColumnInfo decimalDigits = new IntegerColumn("DECIMAL_DIGITS", columnNoNulls, true);
            ColumnInfo numPrecRadix = new IntegerColumn("NUM_PREC_RADIX", columnNoNulls, true);
            ColumnInfo nullable = new IntegerColumn("NULLABLE", columnNoNulls, true);
            ColumnInfo shortNullable = new ShortIntegerColumn("NULLABLE", columnNoNulls, true);
            ColumnInfo remarks = new StringColumn("REMARKS", columnNullable);
            ColumnInfo attrDef = new StringColumn("ATTR_DEF", columnNullable);
            ColumnInfo sqlDataType = new IntegerColumn("SQL_DATA_TYPE", columnNoNulls, true);
            ColumnInfo sqlDateTimeSub = new IntegerColumn("SQL_DATETIME_SUB", columnNoNulls, true);
            ColumnInfo charOctetLength = new IntegerColumn("CHAR_OCTET_LENGTH", columnNoNulls, true);
            ColumnInfo ordinalPosition = new IntegerColumn("ORDINAL_POSITION", columnNoNulls, true);
            ColumnInfo isNullable = new StringColumn("IS_NULLABLE", columnNoNulls);
            ColumnInfo scope = new ShortIntegerColumn("SCOPE", columnNoNulls, true);
            ColumnInfo scopeCatalog = new StringColumn("SCOPE_CATALOG", columnNullable);
            ColumnInfo scopeSchema = new StringColumn("SCOPE_SCHEMA", columnNullable);
            ColumnInfo scopeTable = new StringColumn("SCOPE_TABLE", columnNullable);
            ColumnInfo sourceDataType = new ShortIntegerColumn("SOURCE_DATA_TYPE", columnNullable, true);
            ColumnInfo columnName = new StringColumn("COLUMN_NAME", columnNoNulls);
            ColumnInfo columnSize = new IntegerColumn("COLUMN_SIZE", columnNoNulls, true);
            ColumnInfo columnDef = new StringColumn("COLUMN_DEF", columnNullable);
            ColumnInfo bufferLength = new IntegerColumn("BUFFER_LENGTH", columnNoNulls, true);
            ColumnInfo psuedoColumn = new ShortIntegerColumn("PSUEDO_COLUMN", columnNoNulls, true);
            ColumnInfo tableCat = new StringColumn("TABLE_CAT", columnNullable);
            ColumnInfo tableCatalog = new StringColumn("TABLE_CATALOG", columnNullable);
            ColumnInfo tableSchema = new StringColumn("TABLE_SCHEM", columnNullable);
            ColumnInfo tableName = new StringColumn("TABLE_NAME", columnNoNulls);
            ColumnInfo name = new StringColumn("NAME", columnNoNulls);
            ColumnInfo maxLen = new IntegerColumn("MAX_LEN", columnNoNulls, true);
            ColumnInfo defaultValue = new StringColumn("DEFAULT_VALUE", columnNullable);
            ColumnInfo description = new StringColumn("DESCRIPTION", columnNullable);
            ColumnInfo isAutoIncrement = new StringColumn("IS_AUTOINCREMENT", columnNoNulls);
            ColumnInfo className = new StringColumn("CLASS_NAME", columnNoNulls);
            ColumnInfo baseType = new ShortIntegerColumn("BASE_TYPE", columnNullable, true);
            ColumnInfo grantor = new StringColumn("GRANTOR", columnNullable);
            ColumnInfo grantee = new StringColumn("GRANTEE", columnNullable);
            ColumnInfo privilege = new StringColumn("PRIVILEGE", columnNoNulls);
            ColumnInfo isGrantable = new StringColumn("IS_GRANTABLE", columnNoNulls);
            ColumnInfo pkTableCat = new StringColumn("PKTABLE_CAT", columnNullable);
            ColumnInfo pkTableSchema = new StringColumn("PKTABLE_SCHEM", columnNullable);
            ColumnInfo pkTableName = new StringColumn("PKTABLE_NAME", columnNoNulls);
            ColumnInfo pkColumnName = new StringColumn("PKCOLUMN_NAME", columnNoNulls);
            ColumnInfo fkTableCat = new StringColumn("FKTABLE_CAT", columnNullable);
            ColumnInfo fkTableSchema = new StringColumn("FKTABLE_SCHEM", columnNullable);
            ColumnInfo fkTableName = new StringColumn("FKTABLE_NAME", columnNoNulls);
            ColumnInfo fkColumnName = new StringColumn("FKCOLUMN_NAME", columnNoNulls);
            ColumnInfo keySeq = new ShortIntegerColumn("KEY_SEQ", columnNoNulls, true);
            ColumnInfo updateRule = new ShortIntegerColumn("UPDATE_RULE", columnNoNulls, true);
            ColumnInfo deleteRule = new ShortIntegerColumn("DELETE_RULE", columnNoNulls, true);
            ColumnInfo fkName = new StringColumn("FK_NAME", columnNullable);
            ColumnInfo pkName = new StringColumn("PK_NAME", columnNullable);
            ColumnInfo deferrability = new ShortIntegerColumn("DEFERRABILITY", columnNoNulls, true);
            ColumnInfo functionCat = new StringColumn("FUNCTION_CAT", columnNullable);
            ColumnInfo functionSchema = new StringColumn("FUNCTION_SCHEM", columnNullable);
            ColumnInfo functionName = new StringColumn("FUNCTION_NAME", columnNoNulls);
            ColumnInfo columnType = new ShortIntegerColumn("COLUMN_TYPE", columnNoNulls, true);
            ColumnInfo precision = new IntegerColumn("PRECISION", columnNoNulls, true);
            ColumnInfo length = new IntegerColumn("LENGTH", columnNoNulls, true);
            ColumnInfo scale = new ShortIntegerColumn("SCALE", columnNoNulls, true);
            ColumnInfo radix = new ShortIntegerColumn("RADIX", columnNoNulls, true);
            ColumnInfo specificName = new StringColumn("SPECIFIC_NAME", columnNoNulls);
            ColumnInfo functionType = new ShortIntegerColumn("FUNCTION_TYPE", columnNoNulls, true);
            ColumnInfo nonUnique = new BooleanColumn("NON_UNIQUE", columnNoNulls);
            ColumnInfo indexQualifier = new StringColumn("INDEX_QUALIFIER", columnNullable);
            ColumnInfo indexName = new StringColumn("INDEX_NAME", columnNullable);
            ColumnInfo type = new ShortIntegerColumn("TYPE", columnNoNulls, true);
            ColumnInfo ascOrDesc = new StringColumn("ASC_OR_DESC", columnNullable);
            ColumnInfo cardinality = new IntegerColumn("CARDINALITY", columnNoNulls, true);
            ColumnInfo pages = new IntegerColumn("PAGES", columnNoNulls, true);
            ColumnInfo filterCondition = new StringColumn("FILTER_CONDITION", columnNullable);
            ColumnInfo procedureCat = new StringColumn("PROCEDURE_CAT", columnNullable);
            ColumnInfo procedureSchema = new StringColumn("PROCEDURE_SCHEM", columnNullable);
            ColumnInfo procedureName = new StringColumn("PROCEDURE_NAME", columnNoNulls);
            ColumnInfo procedureType = new ShortIntegerColumn("PROCEDURE_TYPE", columnNoNulls, true);
            ColumnInfo superTableName = new StringColumn("SUPERTABLE_NAME", columnNoNulls);
            ColumnInfo superTypeCat = new StringColumn("SUPERTYPE_CAT", columnNullable);
            ColumnInfo superTypeSchema = new StringColumn("SUPERTYPE_SCHEM", columnNullable);
            ColumnInfo superTypeName = new StringColumn("SUPERTYPE_NAME", columnNoNulls);
            ColumnInfo litPrefix = new StringColumn("LITERAL_PREFIX", columnNullable);
            ColumnInfo litSuffix = new StringColumn("LITERAL_SUFFIX", columnNullable);
            ColumnInfo createParams = new StringColumn("CREATE_PARAMS", columnNullable);
            ColumnInfo caseSensitive = new BooleanColumn("CASE_SENSITIVE", columnNoNulls);
            ColumnInfo searchable = new ShortIntegerColumn("SEARCHABLE", columnNoNulls, true);
            ColumnInfo unsignedAttr = new BooleanColumn("UNSIGNED_ATTRIBUTE", columnNoNulls);
            ColumnInfo fixedPrecScale = new BooleanColumn("FIXED_PREC_SCALE", columnNoNulls);
            ColumnInfo autoIncrement = new BooleanColumn("AUTO_INCREMENT", columnNoNulls);
            ColumnInfo localTypeName = new StringColumn("LOCAL_TYPE_NAME", columnNullable);
            ColumnInfo minScale = new ShortIntegerColumn("MINIMUM_SCALE", columnNoNulls, true);
            ColumnInfo maxScale = new ShortIntegerColumn("MAXIMUM_SCALE", columnNullable, true);
            ColumnInfo tableType = new StringColumn("TABLE_TYPE", columnNoNulls);
            ColumnInfo selfRefColName = new StringColumn("SELF_REFERENCING_COL_NAME", columnNullable);
            ColumnInfo refGeneration = new StringColumn("REF_GENERATION", columnNullable);
            ColumnInfo columnUsage = new StringColumn("COLUMN_USAGE", columnNoNulls);

            ATTRIBUTE_COLUMNS = new ColumnInfo[] {
                    // TYPE_CAT String => type catalog (may be null)
                    typeCat,
                    // TYPE_SCHEM String => type schema (may be null)
                    typeSchema,
                    // TYPE_NAME String => type name
                    typeName,
                    // ATTR_NAME String => attribute name
                    attrName,
                    // DATA_TYPE int => attribute type SQL type from
                    // java.sql.Types
                    dataType,
                    // ATTR_TYPE_NAME String => Data source dependent type name.
                    // For a UDT, the type name is fully qualified. For a REF,
                    // the type name is fully qualified and represents the
                    // target type of the reference type.
                    attrTypeName,
                    // ATTR_SIZE int => column size. For char or date types this
                    // is the maximum number of characters; for numeric or
                    // decimal types this is precision.
                    attrSize,
                    // DECIMAL_DIGITS int => the number of fractional digits.
                    // Null is returned for data types where DECIMAL_DIGITS is
                    // not applicable.
                    decimalDigits,
                    // NUM_PREC_RADIX int => Radix (typically either 10 or 2)
                    numPrecRadix,
                    // NULLABLE int => whether NULL is allowed
                    // attributeNoNulls - might not allow NULL values
                    // attributeNullable - definitely allows NULL values
                    // attributeNullableUnknown - nullability unknown
                    nullable,
                    // REMARKS String => comment describing column (may be null)
                    remarks,
                    // ATTR_DEF String => default value (may be null)
                    attrDef,
                    // SQL_DATA_TYPE int => unused
                    sqlDataType,
                    // SQL_DATETIME_SUB int => unused
                    sqlDateTimeSub,
                    // CHAR_OCTET_LENGTH int => for char types the maximum
                    // number of bytes in the column
                    charOctetLength,
                    // ORDINAL_POSITION int => index of the attribute in the UDT
                    // (starting at 1)
                    ordinalPosition,
                    // IS_NULLABLE String => ISO rules are used to determine the
                    // nullability for a attribute.
                    // YES --- if the attribute can include NULLs
                    // NO --- if the attribute cannot include NULLs
                    // empty string --- if the nullability for the attribute is
                    // unknown
                    isNullable,
                    // SCOPE_CATALOG String => catalog of table that is the
                    // scope of a
                    // reference attribute (null if DATA_TYPE isn't REF)
                    scopeCatalog,
                    // SCOPE_SCHEMA String => schema of table that is the scope
                    // of a reference attribute (null if DATA_TYPE isn't REF)
                    scopeSchema,
                    // SCOPE_TABLE String => table name that is the scope of a
                    // referenceattribute (null if the DATA_TYPE isn't REF)
                    scopeTable,
                    // SOURCE_DATA_TYPE short => source type of a distinct type
                    // or user-generated Ref type,SQL type from java.sql.Types
                    // (null if DATA_TYPE isn't DISTINCT or user-generated REF)
                    sourceDataType };

            BEST_ROW_IDENTIFIER_COLUMNS = new ColumnInfo[] {
                    // SCOPE short => actual scope of result
                    // bestRowTemporary - very temporary, while using row
                    // bestRowTransaction - valid for remainder of current
                    // transaction
                    // bestRowSession - valid for remainder of current session
                    scope,
                    // COLUMN_NAME String => column name
                    columnName,
                    // DATA_TYPE int => SQL data type from java.sql.Types
                    dataType,
                    // TYPE_NAME String => Data source dependent type name, for
                    // a UDT the type name is fully qualified
                    typeName,
                    // COLUMN_SIZE int => precision
                    columnSize,
                    // BUFFER_LENGTH int => not used
                    bufferLength,
                    // DECIMAL_DIGITS short => scale - Null is returned for data
                    // types where DECIMAL_DIGITS is not applicable.
                    decimalDigits,
                    // PSEUDO_COLUMN short => is this a pseudo column like an
                    // Oracle ROWID
                    // bestRowUnknown - may or may not be pseudo column
                    // bestRowNotPseudo - is NOT a pseudo column
                    // bestRowPseudo - is a pseudo column
                    psuedoColumn };

            CATALOG_COLUMNS = new ColumnInfo[] {
            // TABLE_CAT String => catalog name
            tableCat };

            CLIENT_INFO_PROPERTY_COLUMNS = new ColumnInfo[] {
                    // NAME String=> The name of the client info property
                    name,
                    // MAX_LEN int=> The maximum length of the value for the
                    // property
                    maxLen,
                    // DEFAULT_VALUE String=> The default value of the property
                    defaultValue,
                    // DESCRIPTION String=> A description of the property. This
                    // will typically contain information as to where this
                    // property is stored in the database.
                    description };

            COLUMN_COLUMNS = new ColumnInfo[] {
                    // TABLE_CAT String => table catalog (may be null)
                    tableCat,
                    // TABLE_SCHEM String => table schema (may be null)
                    tableSchema,
                    // TABLE_NAME String => table name
                    tableName,
                    // COLUMN_NAME String => column name
                    columnName,
                    // DATA_TYPE int => SQL type from java.sql.Types
                    dataType,
                    // TYPE_NAME String => Data source dependent type name, for
                    // a UDT
                    // the type name is fully qualified
                    typeName,
                    // COLUMN_SIZE int => column size.
                    columnSize,
                    // BUFFER_LENGTH is not used.
                    bufferLength,
                    // DECIMAL_DIGITS int => the number of fractional digits.
                    // Null is
                    // returned for data types where DECIMAL_DIGITS is not
                    // applicable.
                    decimalDigits,
                    // NUM_PREC_RADIX int => Radix (typically either 10 or 2)
                    numPrecRadix,
                    // NULLABLE int => is NULL allowed.
                    // columnNoNulls - might not allow NULL values
                    // columnNullable - definitely allows NULL values
                    // columnNullableUnknown - nullability unknown
                    nullable,
                    // REMARKS String => comment describing column (may be
                    // null),
                    remarks,
                    // COLUMN_DEF String => default value for the column, which
                    // should
                    // be interpreted as a string when the value is enclosed in
                    // single
                    // quotes (may be null)
                    columnDef,
                    // SQL_DATA_TYPE int => unused
                    sqlDataType,
                    // SQL_DATETIME_SUB int => unused
                    sqlDateTimeSub,
                    // CHAR_OCTET_LENGTH int => for char types the maximum
                    // number of
                    // bytes in the column
                    charOctetLength,
                    // ORDINAL_POSITION int => index of column in table
                    // (starting at 1)
                    ordinalPosition,
                    // IS_NULLABLE String => ISO rules are used to determine the
                    // nullability for a column.
                    // YES --- if the parameter can include NULLs
                    // NO --- if the parameter cannot include NULLs
                    // empty string --- if the nullability for the parameter is
                    // unknown
                    isNullable,
                    // SCOPE_CATLOG String => catalog of table that is the scope
                    // of a
                    // reference attribute (null if DATA_TYPE isn't REF)
                    scopeCatalog,
                    // SCOPE_SCHEMA String => schema of table that is the scope
                    // of a
                    // reference attribute (null if the DATA_TYPE isn't REF)
                    scopeSchema,
                    // SCOPE_TABLE String => table name that this the scope of a
                    // reference attribure (null if the DATA_TYPE isn't REF)
                    scopeTable,
                    // SOURCE_DATA_TYPE short => source type of a distinct type
                    // or
                    // user-generated Ref type, SQL type from java.sql.Types
                    // (null if
                    // DATA_TYPE isn't DISTINCT or user-generated REF)
                    sourceDataType,
                    // IS_AUTOINCREMENT String => Indicates whether this column
                    // is auto
                    // incremented
                    // YES --- if the column is auto incremented
                    // NO --- if the column is not auto incremented
                    // empty string --- if it cannot be determined whether the
                    // column is
                    // auto incremented parameter is unknown
                    isAutoIncrement };

            COLUMN_PRIVILEGE_COLUMNS = new ColumnInfo[] {
                    // TABLE_CAT String => table catalog (may be null)
                    tableCat,
                    // TABLE_SCHEM String => table schema (may be null)
                    tableSchema,
                    // TABLE_NAME String => table name
                    tableName,
                    // COLUMN_NAME String => column name
                    columnName,
                    // GRANTOR String => grantor of access (may be null)
                    grantor,
                    // GRANTEE String => grantee of access
                    grantee,
                    // PRIVILEGE String => name of access (SELECT, INSERT,
                    // UPDATE, REFRENCES, ...)
                    privilege,
                    // IS_GRANTABLE String => "YES" if grantee is permitted to
                    // grant to others; "NO" if not; null if unknown
                    isGrantable };

            CROSS_REFERENCE_COLUMNS = new ColumnInfo[] {
                    // PKTABLE_CAT String => parent key table catalog (may be
                    // null)
                    pkTableCat,
                    // PKTABLE_SCHEM String => parent key table schema (may be
                    // null)
                    pkTableSchema,
                    // PKTABLE_NAME String => parent key table name
                    pkTableName,
                    // PKCOLUMN_NAME String => parent key column name
                    pkColumnName,
                    // FKTABLE_CAT String => foreign key table catalog (may be
                    // null)
                    // being exported (may be null)
                    fkTableCat,
                    // FKTABLE_SCHEM String => foreign key table schema (may be
                    // null) being exported (may be null)
                    fkTableSchema,
                    // FKTABLE_NAME String => foreign key table name being
                    // exported
                    fkTableName,
                    // FKCOLUMN_NAME String => foreign key column name being
                    // exported
                    fkColumnName,
                    // KEY_SEQ short => sequence number within foreign key( a
                    // value of 1 represents the first column of the foreign
                    // key, a value of 2 would represent the second column
                    // within the foreign key).
                    keySeq,
                    // UPDATE_RULE short => What happens to foreign key when
                    // parent key is updated:
                    // importedNoAction - do not allow update of parent key if
                    // it has been imported
                    // importedKeyCascade - change imported key to agree with
                    // parent key update
                    // importedKeySetNull - change imported key to NULL if its
                    // parent key has been updated
                    // importedKeySetDefault - change imported key to default
                    // values if its parent key has been updated
                    // importedKeyRestrict - same as importedKeyNoAction (for
                    // ODBC 2.x compatibility)
                    updateRule,
                    // DELETE_RULE short => What happens to the foreign key when
                    // parent key is deleted.
                    // importedKeyNoAction - do not allow delete of parent key
                    // if it has been imported
                    // importedKeyCascade - delete rows that import a deleted
                    // key
                    // importedKeySetNull - change imported key to NULL if its
                    // primary key has been deleted
                    // importedKeyRestrict - same as importedKeyNoAction (for
                    // ODBC 2.x compatibility)
                    // importedKeySetDefault - change imported key to default if
                    // its parent key has been deleted
                    deleteRule,
                    // FK_NAME String => foreign key name (may be null)
                    fkName,
                    // PK_NAME String => parent key name (may be null)
                    pkName,
                    // DEFERRABILITY short => can the evaluation of foreign key
                    // constraints be deferred until commit
                    // importedKeyInitiallyDeferred - see SQL92 for definition
                    // importedKeyInitiallyImmediate - see SQL92 for definition
                    // importedKeyNotDeferrable - see SQL92 for definition
                    deferrability };

            EXPORTED_KEY_COLUMNS = new ColumnInfo[] {
                    // PKTABLE_CAT String => primary key table catalog (may be
                    // null)
                    pkTableCat,
                    // PKTABLE_SCHEM String => primary key table schema (may be
                    // null)
                    pkTableSchema,
                    // PKTABLE_NAME String => primary key table name
                    pkTableName,
                    // PKCOLUMN_NAME String => primary key column name
                    pkColumnName,
                    // FKTABLE_CAT String => foreign key table catalog (may be
                    // null) being exported (may be null)
                    fkTableCat,
                    // FKTABLE_SCHEM String => foreign key table schema (may be
                    // null) being exported (may be null)
                    fkTableSchema,
                    // FKTABLE_NAME String => foreign key table name being
                    // exported
                    fkTableName,
                    // FKCOLUMN_NAME String => foreign key column name being
                    // exported
                    fkColumnName,
                    // KEY_SEQ short => sequence number within foreign key( a
                    // value of 1 represents the first column of the foreign
                    // key, a value of 2 would represent the second column
                    // within the foreign key).
                    keySeq,
                    // UPDATE_RULE short => What happens to foreign key when
                    // primary is updated:
                    // importedNoAction - do not allow update of primary key if
                    // it has been imported
                    // importedKeyCascade - change imported key to agree with
                    // primary key update
                    // importedKeySetNull - change imported key to NULL if its
                    // primary key has been updated
                    // importedKeySetDefault - change imported key to default
                    // values if its primary key has been updated
                    // importedKeyRestrict - same as importedKeyNoAction (for
                    // ODBC 2.x compatibility)
                    updateRule,
                    // DELETE_RULE short => What happens to the foreign key when
                    // primary is deleted.
                    // importedKeyNoAction - do not allow delete of primary key
                    // if it has been imported
                    // importedKeyCascade - delete rows that import a deleted
                    // key
                    // importedKeySetNull - change imported key to NULL if its
                    // primary key has been deleted
                    // importedKeyRestrict - same as importedKeyNoAction (for
                    // ODBC 2.x compatibility)
                    // importedKeySetDefault - change imported key to default if
                    // its primary key has been deleted
                    deleteRule,
                    // FK_NAME String => foreign key name (may be null)
                    fkName,
                    // PK_NAME String => primary key name (may be null)
                    pkName,
                    // DEFERRABILITY short => can the evaluation of foreign key
                    // constraints be deferred until commit
                    // importedKeyInitiallyDeferred - see SQL92 for definition
                    // importedKeyInitiallyImmediate - see SQL92 for definition
                    // importedKeyNotDeferrable - see SQL92 for definition
                    deferrability };

            FUNCTION_COLUMN_COLUMNS = new ColumnInfo[] {
                    // FUNCTION_CAT String => function catalog (may be null)
                    functionCat,
                    // FUNCTION_SCHEM String => function schema (may be null)
                    functionSchema,
                    // FUNCTION_NAME String => function name. This is the name
                    // used to invoke the function
                    functionName,
                    // COLUMN_NAME String => column/parameter name
                    columnName,
                    // COLUMN_TYPE Short => kind of column/parameter:
                    // functionColumnUnknown - nobody knows
                    // functionColumnIn - IN parameter
                    // functionColumnInOut - INOUT parameter
                    // functionColumnOut - OUT parameter
                    // functionColumnReturn - function return value
                    // functionColumnResult - Indicates that the parameter or
                    // column is a column in the ResultSet
                    columnType,
                    // DATA_TYPE int => SQL type from java.sql.Types
                    dataType,
                    // TYPE_NAME String => SQL type name, for a UDT type the
                    // type name is fully qualified
                    typeName,
                    // PRECISION int => precision
                    precision,
                    // LENGTH int => length in bytes of data
                    length,
                    // SCALE short => scale - null is returned for data types
                    // where SCALE is not applicable.
                    scale,
                    // RADIX short => radix
                    radix,
                    // NULLABLE short => can it contain NULL.
                    // functionNoNulls - does not allow NULL values
                    // functionNullable - allows NULL values
                    // functionNullableUnknown - nullability unknown
                    shortNullable,
                    // REMARKS String => comment describing column/parameter
                    remarks,
                    // CHAR_OCTET_LENGTH int => the maximum length of binary and
                    // character based parameters or columns. For any other
                    // datatype the returned value is a NULL
                    charOctetLength,
                    // ORDINAL_POSITION int => the ordinal position, starting
                    // from 1, for the input and output parameters. A value of 0
                    // is returned if this row describes the function's return
                    // value. For result set columns, it is the ordinal position
                    // of the column in the result set starting from 1.
                    ordinalPosition,
                    // IS_NULLABLE String => ISO rules are used to determine the
                    // nullability for a parameter or column.
                    // YES --- if the parameter or column can include NULLs
                    // NO --- if the parameter or column cannot include NULLs
                    // empty string --- if the nullability for the parameter or
                    // column is unknown
                    isNullable,
                    // SPECIFIC_NAME String => the name which uniquely
                    // identifies this function within its schema. This is a
                    // user specified, or DBMS generated, name that may be
                    // different then the FUNCTION_NAME for example with
                    // overload functions
                    specificName };

            FUNCTION_COLUMNS = new ColumnInfo[] {
                    // FUNCTION_CAT String => function catalog (may be null)
                    functionCat,
                    // FUNCTION_SCHEM String => function schema (may be null)
                    functionSchema,
                    // FUNCTION_NAME String => function name. This is the name
                    // used to invoke the function
                    functionName,
                    // REMARKS String => explanatory comment on the function
                    remarks,
                    // FUNCTION_TYPE short => kind of function:
                    // functionResultUnknown - Cannot determine if a return
                    // value or table will be returned
                    // functionNoTable- Does not return a table
                    // functionReturnsTable - Returns a table
                    functionType,
                    // SPECIFIC_NAME String => the name which uniquely
                    // identifies this function within its schema. This is a
                    // user specified, or DBMS generated, name that may be
                    // different then the FUNCTION_NAME for example with
                    // overload functions
                    specificName };

            IMPORTED_KEY_COLUMNS = new ColumnInfo[] {
                    // PKTABLE_CAT String => primary key table catalog being
                    // imported (may be null)
                    pkTableCat,
                    // PKTABLE_SCHEM String => primary key table schema being
                    // imported (may be null)
                    pkTableSchema,
                    // PKTABLE_NAME String => primary key table name being
                    // imported
                    pkTableName,
                    // PKCOLUMN_NAME String => primary key column name being
                    // imported
                    pkColumnName,
                    // FKTABLE_CAT String => foreign key table catalog (may be
                    // null)
                    fkTableCat,
                    // FKTABLE_SCHEM String => foreign key table schema (may be
                    // null)
                    fkTableSchema,
                    // FKTABLE_NAME String => foreign key table name
                    fkTableName,
                    // FKCOLUMN_NAME String => foreign key column name
                    fkColumnName,
                    // KEY_SEQ short => sequence number within a foreign key( a
                    // value of 1 represents the first column of the foreign
                    // key, a value of 2 would represent the second column
                    // within the foreign key).
                    keySeq,
                    // UPDATE_RULE short => What happens to a foreign key when
                    // the primary key is updated:
                    // importedNoAction - do not allow update of primary key if
                    // it has been imported
                    // importedKeyCascade - change imported key to agree with
                    // primary key update
                    // importedKeySetNull - change imported key to NULL if its
                    // primary key has been updated
                    // importedKeySetDefault - change imported key to default
                    // values if its primary key has been updated
                    // importedKeyRestrict - same as importedKeyNoAction (for
                    // ODBC 2.x compatibility)
                    updateRule,
                    // DELETE_RULE short => What happens to the foreign key when
                    // primary is deleted.
                    // importedKeyNoAction - do not allow delete of primary key
                    // if it has been imported
                    // importedKeyCascade - delete rows that import a deleted
                    // key
                    // importedKeySetNull - change imported key to NULL if its
                    // primary key has been deleted
                    // importedKeyRestrict - same as importedKeyNoAction (for
                    // ODBC 2.x compatibility)
                    // importedKeySetDefault - change imported key to default if
                    // its primary key has been deleted
                    deleteRule,
                    // FK_NAME String => foreign key name (may be null)
                    fkName,
                    // PK_NAME String => primary key name (may be null)
                    pkName,
                    // DEFERRABILITY short => can the evaluation of foreign key
                    // constraints be deferred until commit
                    // importedKeyInitiallyDeferred - see SQL92 for definition
                    // importedKeyInitiallyImmediate - see SQL92 for definition
                    // importedKeyNotDeferrable - see SQL92 for definition
                    deferrability };

            INDEX_INFO_COLUMNS = new ColumnInfo[] {
                    // TABLE_CAT String => table catalog (may be null)
                    tableCat,
                    // TABLE_SCHEM String => table schema (may be null)
                    tableSchema,
                    // TABLE_NAME String => table name
                    tableName,
                    // NON_UNIQUE boolean => Can index values be non-unique.
                    // false when TYPE is tableIndexStatistic
                    nonUnique,
                    // INDEX_QUALIFIER String => index catalog (may be null);
                    // null when TYPE is tableIndexStatistic
                    indexQualifier,
                    // INDEX_NAME String => index name; null when TYPE is
                    // tableIndexStatistic
                    indexName,
                    // TYPE short => index type:
                    // tableIndexStatistic - this identifies table statistics
                    // that are returned in conjuction with a table's index
                    // descriptions
                    // tableIndexClustered - this is a clustered index
                    // tableIndexHashed - this is a hashed index
                    // tableIndexOther - this is some other style of index
                    type,
                    // ORDINAL_POSITION short => column sequence number within
                    // index; zero when TYPE is tableIndexStatistic
                    ordinalPosition,
                    // COLUMN_NAME String => column name; null when TYPE is
                    // tableIndexStatistic
                    columnName,
                    // ASC_OR_DESC String => column sort sequence, "A" =>
                    // ascending, "D" => descending, may be null if sort
                    // sequence is not supported; null when TYPE is
                    // tableIndexStatistic
                    ascOrDesc,
                    // CARDINALITY int => When TYPE is tableIndexStatistic, then
                    // this is the number of rows in the table; otherwise, it is
                    // the number of unique values in the index.
                    cardinality,
                    // PAGES int => When TYPE is tableIndexStatisic then this is
                    // the number of pages used for the table, otherwise it is
                    // the number of pages used for the current index.
                    pages,
                    // FILTER_CONDITION String => Filter condition, if any. (may
                    // be null)
                    filterCondition };

            PRIMARY_KEY_COLUMNS = new ColumnInfo[] {
                    // TABLE_CAT String => table catalog (may be null)
                    tableCat,
                    // TABLE_SCHEM String => table schema (may be null)
                    tableSchema,
                    // TABLE_NAME String => table name
                    tableName,
                    // COLUMN_NAME String => column name
                    columnName,
                    // KEY_SEQ short => sequence number within primary key( a
                    // value of 1 represents the first column of the primary
                    // key, a value of 2 would represent the second column
                    // within the primary key).
                    keySeq,
                    // PK_NAME String => primary key name (may be null)
                    pkName };

            PROCEDURE_COLUMN_COLUMNS = new ColumnInfo[] {
                    // PROCEDURE_CAT String => procedure catalog (may be null)
                    procedureCat,
                    // PROCEDURE_SCHEM String => procedure schema (may be null)
                    procedureSchema,
                    // PROCEDURE_NAME String => procedure name
                    procedureName,
                    // COLUMN_NAME String => column/parameter name
                    columnName,
                    // COLUMN_TYPE Short => kind of column/parameter:
                    // procedureColumnUnknown - nobody knows
                    // procedureColumnIn - IN parameter
                    // procedureColumnInOut - INOUT parameter
                    // procedureColumnOut - OUT parameter
                    // procedureColumnReturn - procedure return value
                    // procedureColumnResult - result column in ResultSet
                    columnType,
                    // DATA_TYPE int => SQL type from java.sql.Types
                    dataType,
                    // TYPE_NAME String => SQL type name, for a UDT type the
                    // type name is fully qualified
                    typeName,
                    // PRECISION int => precision
                    precision,
                    // LENGTH int => length in bytes of data
                    length,
                    // SCALE short => scale - null is returned for data types
                    // where SCALE is not applicable.
                    scale,
                    // RADIX short => radix
                    radix,
                    // NULLABLE short => can it contain NULL.
                    // procedureNoNulls - does not allow NULL values
                    // procedureNullable - allows NULL values
                    // procedureNullableUnknown - nullability unknown
                    shortNullable,
                    // REMARKS String => comment describing parameter/column
                    remarks,
                    // COLUMN_DEF String => default value for the column, which
                    // should be interpreted as a string when the value is
                    // enclosed in single quotes (may be null)
                    // The string NULL (not enclosed in quotes) - if NULL was
                    // specified as the default value
                    // TRUNCATE (not enclosed in quotes) - if the specified
                    // default value cannot be represented without truncation
                    // NULL - if a default value was not specified
                    columnDef,
                    // SQL_DATA_TYPE int => reserved for future use
                    sqlDataType,
                    // SQL_DATETIME_SUB int => reserved for future use
                    sqlDateTimeSub,
                    // CHAR_OCTET_LENGTH int => the maximum length of binary and
                    // character based columns. For any other datatype the
                    // returned value is a NULL
                    charOctetLength,
                    // ORDINAL_POSITION int => the ordinal position, starting
                    // from 1, for the input and output parameters for a
                    // procedure. A value of 0 is returned if this row describes
                    // the procedure's return value. For result set columns, it
                    // is the ordinal position of the column in the result set
                    // starting from 1. If there are multiple result sets, the
                    // column ordinal positions are implementation defined.
                    ordinalPosition,
                    // IS_NULLABLE String => ISO rules are used to determine the
                    // nullability for a column.
                    // YES --- if the parameter can include NULLs
                    // NO --- if the parameter cannot include NULLs
                    // empty string --- if the nullability for the parameter is
                    // unknown
                    isNullable,
                    // SPECIFIC_NAME String => the name which uniquely
                    // identifies this procedure within its schema.
                    specificName };

            PROCEDURE_COLUMNS = new ColumnInfo[] {
                    // PROCEDURE_CAT String => procedure catalog (may be null)
                    procedureCat,
                    // PROCEDURE_SCHEM String => procedure schema (may be null)
                    procedureSchema,
                    // PROCEDURE_NAME String => procedure name
                    procedureName,
                    // reserved for future use
                    empty,
                    // reserved for future use
                    empty,
                    // reserved for future use
                    empty,
                    // REMARKS String => explanatory comment on the procedure
                    remarks,
                    // PROCEDURE_TYPE short => kind of procedure:
                    // procedureResultUnknown - Cannot determine if a return
                    // value will be returned
                    // procedureNoResult - Does not return a return value
                    // procedureReturnsResult - Returns a return value
                    procedureType,
                    // SPECIFIC_NAME String => The name which uniquely
                    // identifies this procedure within its schema.
                    specificName };

            PSUEDO_COLUMN_COLUMNS = new ColumnInfo[] {
                    // TABLE_CAT String => table catalog (may be null)
                    tableCat,
                    // TABLE_SCHEM String => table schema (may be null)
                    tableSchema,
                    // TABLE_NAME String => table name
                    tableName,
                    // COLUMN_NAME String => column name
                    columnName,
                    // DATA_TYPE int => SQL type from java.sql.Types
                    dataType,
                    // COLUMN_SIZE int => column size.
                    columnSize,
                    // DECIMAL_DIGITS int => the number of fractional digits.
                    // Null is returned for data types where DECIMAL_DIGITS is
                    // not applicable.
                    decimalDigits,
                    // NUM_PREC_RADIX int => Radix (typically either 10 or 2)
                    numPrecRadix,
                    // COLUMN_USAGE String => The allowed usage for the column.
                    // The value returned will correspond to the enum name
                    // returned by PseudoColumnUsage.name()
                    columnUsage,
                    // REMARKS String => comment describing column (may be null)
                    remarks,
                    // CHAR_OCTET_LENGTH int => for char types the maximum
                    // number of bytes in the column
                    charOctetLength,
                    // IS_NULLABLE String => ISO rules are used to determine the
                    // nullability for a column.
                    // YES --- if the column can include NULLs
                    // NO --- if the column cannot include NULLs
                    // empty string --- if the nullability for the column is
                    // unknown
                    isNullable };

            // NB - For some reason JDBC suddenly uses TABLE_CATALOG instead of
            // TABLE_CAT here?
            SCHEMA_COLUMNS = new ColumnInfo[] {
                    // TABLE_SCHEM String => schema name
                    tableSchema,
                    // TABLE_CATALOG String => catalog name (may be null)
                    tableCatalog };

            SUPER_TABLE_COLUMNS = new ColumnInfo[] {
                    // TABLE_CAT String => the type's catalog (may be null)
                    tableCat,
                    // TABLE_SCHEM String => type's schema (may be null)
                    tableSchema,
                    // TABLE_NAME String => type name
                    tableName,
                    // SUPERTABLE_NAME String => the direct super type's name
                    superTableName };

            SUPER_TYPE_COLUMNS = new ColumnInfo[] {
                    // TYPE_CAT String => the UDT's catalog (may be null)
                    typeCat,
                    // TYPE_SCHEM String => UDT's schema (may be null)
                    typeSchema,
                    // TYPE_NAME String => type name of the UDT
                    typeName,
                    // SUPERTYPE_CAT String => the direct super type's catalog
                    // (may be null)
                    superTypeCat,
                    // SUPERTYPE_SCHEM String => the direct super type's schema
                    // (may be null)
                    superTypeSchema,
                    // SUPERTYPE_NAME String => the direct super type's name
                    superTypeName };

            TABLE_PRIVILEGE_COLUMNS = new ColumnInfo[] {
                    // TABLE_CAT String => table catalog (may be null)
                    tableCat,
                    // TABLE_SCHEM String => table schema (may be null)
                    tableSchema,
                    // TABLE_NAME String => table name
                    tableName,
                    // GRANTOR String => grantor of access (may be null)
                    grantor,
                    // GRANTEE String => grantee of access
                    grantee,
                    // PRIVILEGE String => name of access (SELECT, INSERT,
                    // UPDATE, REFRENCES, ...)
                    privilege,
                    // IS_GRANTABLE String => "YES" if grantee is permitted to
                    // grant to others; "NO" if not; null if unknown
                    isGrantable };

            TABLE_TYPE_COLUMNS = new ColumnInfo[] {
            // TABLE_TYPE String => table type. Typical types are "TABLE",
            // "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY",
            // "ALIAS", "SYNONYM".
            tableType };

            TABLE_COLUMNS = new ColumnInfo[] {
                    // TABLE_CAT String => table catalog (may be null)
                    tableCat,
                    // TABLE_SCHEM String => table schema (may be null)
                    tableSchema,
                    // TABLE_NAME String => table name
                    tableName,
                    // TABLE_TYPE String => table type. Typical types are
                    // "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
                    // "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
                    tableType,
                    // REMARKS String => explanatory comment on the table
                    remarks,
                    // TYPE_CAT String => the types catalog (may be null)
                    typeCat,
                    // TYPE_SCHEM String => the types schema (may be null)
                    typeSchema,
                    // TYPE_NAME String => type name (may be null)
                    typeName,
                    // SELF_REFERENCING_COL_NAME String => name of the
                    // designated "identifier" column of a typed table (may be
                    // null)
                    selfRefColName,
                    // REF_GENERATION String => specifies how values in
                    // SELF_REFERENCING_COL_NAME are created. Values are
                    // "SYSTEM", "USER", "DERIVED". (may be null)
                    refGeneration };

            TYPE_INFO_COLUMNS = new ColumnInfo[] {
                    // TYPE_NAME String => Type name
                    typeName,
                    // DATA_TYPE int => SQL data type from java.sql.Types
                    dataType,
                    // PRECISION int => maximum precision
                    precision,
                    // LITERAL_PREFIX String => prefix used to quote a literal
                    // (may be null)
                    litPrefix,
                    // LITERAL_SUFFIX String => suffix used to quote a literal
                    // (may be null)
                    litSuffix,
                    // CREATE_PARAMS String => parameters used in creating the
                    // type (may be null)
                    createParams,
                    // NULLABLE short => can you use NULL for this type.
                    // typeNoNulls - does not allow NULL values
                    // typeNullable - allows NULL values
                    // typeNullableUnknown - nullability unknown
                    shortNullable,
                    // CASE_SENSITIVE boolean=> is it case sensitive.
                    caseSensitive,
                    // SEARCHABLE short => can you use "WHERE" based on this
                    // type:
                    // typePredNone - No support
                    // typePredChar - Only supported with WHERE .. LIKE
                    // typePredBasic - Supported except for WHERE .. LIKE
                    // typeSearchable - Supported for all WHERE ..
                    searchable,
                    // UNSIGNED_ATTRIBUTE boolean => is it unsigned.
                    unsignedAttr,
                    // FIXED_PREC_SCALE boolean => can it be a money value.
                    fixedPrecScale,
                    // AUTO_INCREMENT boolean => can it be used for an
                    // auto-increment value.
                    autoIncrement,
                    // LOCAL_TYPE_NAME String => localized version of type name
                    // (may be null)
                    localTypeName,
                    // MINIMUM_SCALE short => minimum scale supported
                    minScale,
                    // MAXIMUM_SCALE short => maximum scale supported
                    maxScale,
                    // SQL_DATA_TYPE int => unused
                    sqlDataType,
                    // SQL_DATETIME_SUB int => unused
                    sqlDateTimeSub,
                    // NUM_PREC_RADIX int => usually 2 or 10
                    numPrecRadix };

            UDT_COLUMNS = new ColumnInfo[] {
                    // TYPE_CAT String => the type's catalog (may be null)
                    typeCat,
                    // TYPE_SCHEM String => type's schema (may be null)
                    typeSchema,
                    // TYPE_NAME String => type name
                    typeName,
                    // CLASS_NAME String => Java class name
                    className,
                    // DATA_TYPE int => type value defined in java.sql.Types.
                    // One of JAVA_OBJECT, STRUCT, or DISTINCT
                    dataType,
                    // REMARKS String => explanatory comment on the type
                    remarks,
                    // BASE_TYPE short => type code of the source type of a
                    // DISTINCT type or the type that implements the
                    // user-generated reference type of the
                    // SELF_REFERENCING_COLUMN of a structured type as defined
                    // in java.sql.Types (null if DATA_TYPE is not DISTINCT or
                    // not STRUCT with REFERENCE_GENERATION = USER_DEFINED)
                    baseType };

            VERSION_COLUMNS = new ColumnInfo[] {
                    // SCOPE short => is not used
                    scope,
                    // COLUMN_NAME String => column name
                    columnName,
                    // DATA_TYPE int => SQL data type from java.sql.Types
                    dataType,
                    // TYPE_NAME String => Data source-dependent type name
                    typeName,
                    // COLUMN_SIZE int => precision
                    columnSize,
                    // BUFFER_LENGTH int => length of column value in bytes
                    bufferLength,
                    // DECIMAL_DIGITS short => scale - Null is returned for data
                    // types where DECIMAL_DIGITS is not applicable.
                    decimalDigits,
                    // PSEUDO_COLUMN short => whether this is pseudo column like
                    // an Oracle ROWID
                    // versionColumnUnknown - may or may not be pseudo column
                    // versionColumnNotPseudo - is NOT a pseudo column
                    // versionColumnPseudo - is a pseudo column
                    psuedoColumn };

        } catch (SQLException e) {
            throw new Error("Fatal error initializing JDBC metadata schema information");
        }
        init = true;
    }
}
