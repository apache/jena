/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestStringBase extends TestDB 
{
    private Params params ;
    private String tempTableName ;
    private String testLabel ;
    private String baseString ; 
    

    //static Charset csUTF8 = Charset.forName("UTF-8") ;


    public TestStringBase(String testLabel, String baseString, 
                    Connection jdbc, Params params, boolean verbose)
    {
        super(jdbc, verbose) ;
        this.params = params ;
        tempTableName = params.get(ParamsVocab.TempTableName) ;
        this.testLabel = testLabel ;
        this.baseString = baseString ;
    }

    @Before
    public void before()
    {
        execNoFail("DROP TABLE %s", tempTableName) ;
    }
    
    @After
    public void after()
    { }
    
    // --------
    @Test
    public void text() throws Exception
    { runTextTest(testLabel+"/Text", baseString, params.get(ParamsVocab.VarcharCol), params.get(ParamsVocab.VarcharType)) ; }

    @Test
    public void binary() throws Exception
    { runBytesTest(testLabel+"/Binary", baseString, params.get(ParamsVocab.BinaryCol), params.get(ParamsVocab.BinaryType)) ; }

    private void runTextTest(String label, String testString, String colName, String colType) throws Exception
    {
        //testString = ":"+testString+":" ;
        if ( testString == null )
            fail(label+": Test broken - null input") ; 
        exec("CREATE TABLE %s (%s %s)",  tempTableName, colName, colType) ;

        String $str = sqlFormat("INSERT INTO %s values (?)", tempTableName) ;
        if ( verbose )
            System.out.println($str) ;

        PreparedStatement ps = jdbc.prepareStatement($str) ;
        ps.setString(1, testString) ;
        try { ps.execute() ; }  // Idiom for debugging - can breakpoint the throw
        catch (SQLException ex)
        { throw ex ; } 
        ps.close() ;

        ResultSet rs = execQuery("SELECT %s FROM %s ", colName, tempTableName ) ;
        rs.next() ;
        // In Oracle an empty string is a NULL.  This is not ANSI compliant.
        
        String s = rs.getString(1) ;
        if ( s == null )
            s = "" ;
        byte[] b = rs.getBytes(1) ;
        
        
//        boolean wasNull = rs.wasNull() ;
//        if ( testString != null && wasNull )
//            fail(testLabel+" : Got null back") ;

        rs.close() ;
        
        if ( ! testString.equals(s) )   // Debug point
        {
            for ( int i = 0 ; i < s.length() ; i++ )
            {
                System.out.printf("%x:%x ", (int)testString.charAt(i), (int)s.charAt(i)) ;
            }
            System.out.println() ;
            String $ = s ;              // Pointless
        }
                   
        assertEquals(testLabel+" : "+label, testString, s) ;
        //System.out.println("Passed: "+label) ;
    }

    private void runBytesTest(String label, String testString, String colName, String colType) throws Exception
    {
        //testString = ":"+testString+":" ;
        if ( testString == null )
            fail(label+": Test broken - null input") ; 

        exec("CREATE TABLE %s (%s %s)",  tempTableName, colName, colType) ;

        String $str = sqlFormat("INSERT INTO %s values (?)", tempTableName) ;
        if ( verbose )
            System.out.println($str) ;

        PreparedStatement ps = jdbc.prepareStatement($str) ;
        ps.setBytes(1, stringToBytes(testString)) ;
        try { ps.execute() ; }  // Idiom for debugging
        catch (SQLException ex)
        { throw ex ; } 
        ps.close() ;

        ResultSet rs = execQuery("SELECT %s FROM %s ", colName, tempTableName ) ;
        rs.next() ;
        byte[]b = rs.getBytes(1) ;
        
//        boolean wasNull = rs.wasNull() ;
//        if ( testString != null && wasNull )
//            fail(testLabel+": got an SQL null back") ;

            // In Oracle, an empty binary is a NULL.  This is not ANSI compliant.
        String s = "" ;
        if ( b != null )
            s = bytesToString(b) ;
        rs.close() ;
        if ( ! testString.equals(s) )   // Debug point
        {
            String $ = s ;              // Pointless
        }
        assertEquals(testLabel+" : "+label, testString, s) ;
        //System.out.println("Passed: "+label) ;
    }
    
     // String(byte[], Charset) and .getBytes(Charset) are Java6-isms.
    
    String bytesToString(byte[] b)
    {
        if ( b == null )
            fail(testLabel+": bytesToString(null)") ;
        
        try { return new String(b, "UTF-8") ; }
        catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException("No UTF-8 - should not happen") ;
        }
    }
    byte[] stringToBytes(String s)
    {
        if ( s == null )
            fail(testLabel+": stringToByte(null)") ;
        try { return s.getBytes("UTF-8") ; } 
        catch (UnsupportedEncodingException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException("No UTF-8 - should not happen") ;
        }
        
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