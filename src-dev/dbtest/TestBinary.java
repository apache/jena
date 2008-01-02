/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dbtest;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

public class TestBinary extends TestCase 
{
    private String tempTableName = null ;
    private String content ;
    private Params params ;
    private Connection jdbc ;
    
    public TestBinary(Connection jdbc, String name, String content, Params params)
    {
        super(name+"/Binary") ;
        this.content = content ;
        this.params = params ;
        tempTableName = params.get(ParamsVocab.TempTableName) ;
        this.jdbc = jdbc ;
    }
    
    @Override
    public void setUp()
    {
        DB.execNoFail(jdbc, "DROP TABLE "+tempTableName) ;
    }
    
    @Override
    public void runTest() throws Exception
    {
        runBytesTest(content, 
                     params.get(ParamsVocab.LongBinaryCol),
                     params.get(ParamsVocab.LongBinaryType));
    }
    
    private void runBytesTest(String testString, String colName, String colType) throws Exception
    {
        String tempTableName = params.get(ParamsVocab.TempTableName) ;
        //testString = ":"+testString+":" ;
        if ( testString == null )
            fail("Test broken - null input") ; 

        DB.exec(jdbc, "CREATE TABLE "+tempTableName+" ("+colName+" "+colType+")") ;

        String $str = "INSERT INTO "+tempTableName+" values (?)" ;
        PreparedStatement ps = jdbc.prepareStatement($str) ;
        ps.setBytes(1, stringToBytes(testString)) ;
        try { ps.execute() ; }  // Idiom for debugging
        catch (SQLException ex)
        { throw ex ; } 
        ps.close() ;

        ResultSet rs = DB.execQuery(jdbc, "SELECT "+colName+" FROM "+tempTableName) ;
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
        assertEquals("Binary different", testString, s) ;
        //System.out.println("Passed: "+label) ;
    }

    String bytesToString(byte[] b)
    {
        if ( b == null )
            fail("bytesToString(null)") ;
        
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
            fail("stringToByte(null)") ;
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