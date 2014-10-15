/*
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

package dbtest;


/** Deafult settings for various databases */

public class Setup
{
    public static void setParams(Params params, String dbtype)
    {
        params.putDft( ParamsVocab.User,             "USER") ;
        params.putDft( ParamsVocab.Password,         "PASSWORD") ;
        params.putDft( ParamsVocab.TempTableName,    "JenaJdbcTempTable") ;
        params.putDft( ParamsVocab.TestLongBinary,   "false") ;
        params.putDft( ParamsVocab.TestLongText,     "false") ;
        
        params.putDft( ParamsVocab.VarcharCol,       "colVarchar") ;
        params.putDft( ParamsVocab.LongTextCol,      "colLongText") ;
        params.putDft( ParamsVocab.LongBinaryCol,        "colLongBinary") ;

        dbtype = dbtype.toLowerCase() ;

        if ( dbtype.equalsIgnoreCase("HSQLDB") ||
             dbtype.equalsIgnoreCase("HSQL") )
        {
            setup_HSQL(params) ;
            return ;
        }

        if ( dbtype.equalsIgnoreCase("MySQL") )
        {        
            setup_MySQL(params) ;
            return ;
        }

        if ( dbtype.equalsIgnoreCase("PostgreSQL") ||
             dbtype.equalsIgnoreCase("Postgres") ||
             dbtype.equalsIgnoreCase("PSQL") )
        {
            setup_PSQL(params) ;
            return ; 
        }

        if ( dbtype.equalsIgnoreCase("MSSQLServer") ||
             dbtype.equalsIgnoreCase("SQLServer") ||
             dbtype.equalsIgnoreCase("MSSQL") )
        {
            setup_MSSQL(params) ;
            return ;
        }

        if ( dbtype.equalsIgnoreCase("Oracle") )
        {
            setup_Oracle(params) ;
            return ;
        }

        if ( dbtype.equalsIgnoreCase("Derby") )
        {
            setup_Derby(params) ;
            return ;
        }

        if ( dbtype.equalsIgnoreCase("SAP") )
        {
            setup_SAP(params) ;
            return ;
        }

        System.err.println("Unknown DB type: "+dbtype) ;
        System.exit(2) ;

        dbtype = dbtype.toLowerCase() ;
    }

    private static void setup_HSQL(Params params)
    {
        params.putDft( ParamsVocab.Driver,           "org.hsqldb.jdbcDriver") ;
        params.put( ParamsVocab.TestLongBinary,      "true") ;
        params.put( ParamsVocab.TestLongText,        "true") ;
        
        params.putDft( ParamsVocab.VarcharType,      "VARCHAR(200)") ;
        params.putDft( ParamsVocab.LongBinaryType,   "VARBINARY") ;
        params.putDft( ParamsVocab.LongTextType,     "LONGVARCHAR") ;

        params.put( ParamsVocab.User,                "sa") ;
        params.put( ParamsVocab.Password,            "") ;

        params.putDft( ParamsVocab.JDBC,             "jdbc:hsqldb:mem:TEST") ;
        return ;
    }

    private static void setup_MySQL(Params params)
    {
        // Jena uses long binary
        params.putDft( ParamsVocab.Driver,          "com.mysql.jdbc.Driver") ;
        params.put( ParamsVocab.TestLongBinary,     "true") ;
        params.put( ParamsVocab.TestLongText,       "true") ;
        
        params.putDft( ParamsVocab.VarcharType,     "VARCHAR(200)") ;
        params.putDft( ParamsVocab.LongBinaryType,  "MEDIUMBLOB") ;
        params.putDft( ParamsVocab.LongTextType,    "MEDIUMTEXT") ;
        return ;
    }

    private static void setup_PSQL(Params params)
    {
        // Jena uses long text
        params.putDft( ParamsVocab.Driver,           "org.postgresql.Driver") ;
        params.put( ParamsVocab.TestLongBinary,      "true") ;
        params.put( ParamsVocab.TestLongText,        "true") ;

        params.putDft( ParamsVocab.VarcharType,      "VARCHAR(200)") ;
        params.putDft( ParamsVocab.LongBinaryType,   "BYTEA") ;
        params.putDft( ParamsVocab.LongTextType,     "TEXT") ;
        return ;
    }

    private static void setup_Derby(Params params)
    {
        params.putDft( ParamsVocab.Driver,          "org.apache.derby.jdbc.EmbeddedDriver") ;
        params.put( ParamsVocab.TestLongBinary,     "false") ;
        params.put( ParamsVocab.TestLongText,       "true") ;

        params.putDft( ParamsVocab.VarcharType,     "VARCHAR(200)") ;
//      params.putDft( ParamsVocab.LongBinaryType,    "LONGVARBINARY") ;?????
        params.putDft( ParamsVocab.LongTextType,    "VARCHAR(32672)") ;
        
        //params.putDft( ParamsVocab.Driver, "org.apache.derby.jdbc.ClientDriver") ;
        return ;
    }

    private static void setup_MSSQL(Params params)
    {
        // Jena used long text
        params.putDft( ParamsVocab.Driver,          "com.microsoft.sqlserver.jdbc.SQLServerDriver") ;
        params.put( ParamsVocab.TestLongBinary,     "true") ;
        params.put( ParamsVocab.TestLongText,       "true") ;
        
        params.putDft( ParamsVocab.VarcharType,     "NVARCHAR(200)") ;
        params.putDft( ParamsVocab.LongBinaryType,  "VARBINARY(max)") ;
        params.putDft( ParamsVocab.LongTextType,    "NTEXT") ;
        
        params.putDft( ParamsVocab.VarcharCol,       "colVarchar") ;
        return ;
    }

    private static void setup_Oracle(Params params)
    {
        // Jena used long binary
        params.putDft( ParamsVocab.Driver,           "oracle.jdbc.driver.OracleDriver") ;
        params.put( ParamsVocab.TestLongBinary,      "true") ;
        params.put( ParamsVocab.TestLongText,        "true") ;
        
        params.putDft( ParamsVocab.VarcharType,      "VARCHAR(200)") ; // NVARCHAR2 would be better.
        params.putDft( ParamsVocab.LongBinaryType,   "BLOB") ;
        params.putDft( ParamsVocab.LongTextType,     "CLOB") ;
//        params.putDft( ParamsVocab.LongBinaryType,   "LONG VARBINARY") ;
//        params.putDft( ParamsVocab.LongTextType,     "LONG VARCHAR") ;
        return ;
    }

    private static void setup_SAP(Params params)
    {
        // Jena uses long text
        params.putDft( ParamsVocab.Driver,           "com.sap.db.jdbc.Driver") ;
        params.put( ParamsVocab.TestLongBinary,      "true") ;
        params.put( ParamsVocab.TestLongText,        "true") ;

        params.putDft( ParamsVocab.VarcharType,      "NVARCHAR(200)") ;
        params.putDft( ParamsVocab.LongBinaryType,   "VARBINARY(5000)") ;
        params.putDft( ParamsVocab.LongTextType,     "NVARCHAR(5000)") ;
        return ;
    }
}
