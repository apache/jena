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
        
        try (ResultSet rs = execQuery("SELECT %s FROM %s ", colName, tempTableName )) {
            rs.next() ;
            // In Oracle an empty string is a NULL.  This is not ANSI compliant.

            String s = rs.getString(1) ;
            if ( s == null )
                s = "" ;
            byte[] b = rs.getBytes(1) ;

            //        boolean wasNull = rs.wasNull() ;
            //        if ( testString != null && wasNull )
            //            fail(testLabel+" : Got null back") ;
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

        try ( ResultSet rs = execQuery("SELECT %s FROM %s ", colName, tempTableName ) ) {
            rs.next() ;
            byte[]b = rs.getBytes(1) ;

            //        boolean wasNull = rs.wasNull() ;
            //        if ( testString != null && wasNull )
            //            fail(testLabel+": got an SQL null back") ;

            // In Oracle, an empty binary is a NULL.  This is not ANSI compliant.
            String s = "" ;
            if ( b != null )
                s = bytesToString(b) ;
            if ( ! testString.equals(s) )   // Debug point
            {
                String $ = s ;              // Pointless
            }
            assertEquals(testLabel+" : "+label, testString, s) ;
            //System.out.println("Passed: "+label) ;
    }
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
