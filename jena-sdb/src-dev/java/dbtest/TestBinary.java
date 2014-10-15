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
