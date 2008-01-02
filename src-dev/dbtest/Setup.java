/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */