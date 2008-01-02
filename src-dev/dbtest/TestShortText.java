/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dbtest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

public class TestShortText extends TestCase 
{
    private String tempTableName = null ;
    private String content ;
    private Params params ;
    private Connection jdbc ;
    
    public TestShortText(Connection jdbc, String name, String content, Params params)
    {
        super(name+"/Text") ;
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
        runTextTest(content, 
                    params.get(ParamsVocab.VarcharCol),
                    params.get(ParamsVocab.VarcharType));
    }
    
    private void runTextTest(String testString, String colName, String colType)
    throws Exception
    {
        //testString = ":"+testString+":" ;
        if ( testString == null )
            fail("Test broken - null input") ; 
        DB.exec(jdbc, "CREATE TABLE "+tempTableName+"("+colName+" "+colType+")") ;

        String $str = "INSERT INTO "+tempTableName+" values (?)" ;
        if ( DB.verbose )
            System.out.println($str) ;

        PreparedStatement ps = jdbc.prepareStatement($str) ;
        ps.setString(1, testString) ;
        try { ps.execute() ; }  // Idiom for debugging - can breakpoint the throw
        catch (SQLException ex)
        { throw ex ; } 
        ps.close() ;

        ResultSet rs = DB.execQuery(jdbc, "SELECT "+colName+" FROM "+tempTableName ) ;
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
        
//        if ( ! testString.equals(s) )   // Debug point
//        {
//            for ( int i = 0 ; i < s.length() ; i++ )
//            {
//                System.out.printf("%x:%x ", (int)testString.charAt(i), (int)s.charAt(i)) ;
//            }
//            System.out.println() ;
//            String $ = s ;              // Pointless
//        }
                   
        assertEquals("Text different", testString, s) ;
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