/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.nio.charset.Charset;
import java.sql.*;
import java.util.List;

import com.hp.hpl.jena.query.util.Utils;

import sdb.cmd.CmdArgsDB;

/** Run some DB tests to check setup */ 

public class DBTest extends CmdArgsDB 
{
    
    public static void main(String [] argv)
    {
        new DBTest(argv).mainAndExit() ;
    }
    
    String filename = null ;

    public DBTest(String[] args)
    {
        super(args);
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary()  { return getCommandName()+" <SPEC> "; }
    
    @Override
    protected void processModulesAndArgs()
    {
    }
    
    static final String tableName    = "FOO" ;
    
    static final String blobType     = "VARBINARY" ;
    static final String typeVarchar  = "VARCHAR(200)" ;
    
    static final String colBinary    = "colBinary" ;
    static final String colVarchar   = "colVarchar" ;
    
    static final String baseString   = "abcéíﬂabcαβγz" ; 
    static Charset csUTF8 = null ;
    static {
        csUTF8 = Charset.forName("UTF-8") ;
    }
    
    @Override
    protected void execCmd(List<String> args)
    {
        Connection jdbc = getModStore().getConnection().getSqlConnection() ;
        testColBinary("Binary",  jdbc, blobType, colBinary) ;
        testColText("Varchar", jdbc, typeVarchar, colVarchar) ;
        //try { jdbc.close(); } catch (SQLException ex) {}
    }
    
    private void testColBinary(String label, Connection jdbc, String colType, String colName)
    {
        try {
            execNoFail(jdbc, "DROP TABLE %s", tableName) ;
            exec(jdbc, "CREATE TABLE %s (%s %s)",  tableName, colName, colType) ;

            String testString = baseString ;

            String $str = sqlFormat("INSERT INTO %s values (?)", tableName) ;
            if ( verbose )
                System.out.println($str) ;
            
            PreparedStatement ps = jdbc.prepareStatement($str) ;
            ps.setBytes(1, baseString.getBytes(csUTF8)) ;
            ps.execute() ;
            ps.close() ;

            ResultSet rs = execQuery(jdbc, "SELECT %s FROM %s ", colName, tableName ) ;
            rs.next() ;
            byte[] b = rs.getBytes(1) ;
            String s = new String(b, csUTF8) ;
            if ( ! testString.equals(s) )
                System.err.printf("Failed '%s' test\n", label) ;
            else
                System.err.printf("Passed '%s' test\n", label) ;
            rs.close() ;
            execNoFail(jdbc, "DROP TABLE %s", tableName) ;
        } catch (SQLException ex)
        {
            ex.printStackTrace(System.err) ;
        }
    }

    private void testColText(String label, Connection jdbc, String colType, String colName)
    {
        try {
            execNoFail(jdbc, "DROP TABLE %s", tableName) ;
            exec(jdbc, "CREATE TABLE %s (%s %s)",  tableName, colName, colType) ;

            String testString = baseString ;

            String $str = sqlFormat("INSERT INTO %s values (?)", tableName) ;
            if ( verbose )
                System.out.println($str) ;
            
            PreparedStatement ps = jdbc.prepareStatement($str) ;
            ps.setString(1, testString) ;
            ps.execute() ;
            ps.close() ;

            ResultSet rs = execQuery(jdbc, "SELECT %s FROM %s ", colName, tableName ) ;
            rs.next() ;
            String s = rs.getString(1) ;
            
            if ( ! testString.equals(s) )
                System.err.printf("Failed '%s' test\n", label) ;
            else
                System.err.printf("Passed '%s' test\n", label) ;
            rs.close() ;
            execNoFail(jdbc, "DROP TABLE %s", tableName) ;
        } catch (SQLException ex)
        {
            ex.printStackTrace(System.err) ;
        }
    }

    private String sqlFormat(String sql, Object... args)
    {
        return String.format(sql, args) ;
    }
    
    private void execNoFail(Connection jdbc, String sql, Object... args)
    {
        try { exec(jdbc, sql, args) ;
        } catch (SQLException ex) {}
    }

    private void exec(Connection jdbc, String sql, Object... args) throws SQLException
    {
        sql = sqlFormat(sql, args) ;
        Statement stmt = null ;
        try {
            stmt = jdbc.createStatement() ;
            if ( verbose )
                System.out.println(sql) ;
            stmt.execute(sql) ;
        } finally {
            if ( stmt != null ) stmt.close() ;
        }
    }
    
    private ResultSet execQuery(Connection jdbc, String sql, Object... args) throws SQLException
    {
        sql = sqlFormat(sql, args) ;
        if ( verbose )
            System.out.println(sql) ;
        Statement stmt = jdbc.createStatement() ;
        return stmt.executeQuery(sql) ;
    }
    

}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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