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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

public class TestLongText extends TestCase 
{
    private String tempTableName = null ;
    private String content ;
    private Params params ;
    private Connection jdbc ;
    
    public TestLongText(Connection jdbc, String name, String content, Params params)
    {
        super(name+"/Long text") ;
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
                    params.get(ParamsVocab.LongTextCol),
                    params.get(ParamsVocab.LongTextType));
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
        rs.close() ;
        
        assertEquals("Text different", testString, s) ;
    }

}
