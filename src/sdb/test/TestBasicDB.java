/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestBasicDB extends TestDB
{
    private String tempTableName ;
    
    public TestBasicDB()
    {
        super(Env.test_jdbc, false) ;
    }
    
    @BeforeClass
    static public void set()
    {
        if ( Env.test_jdbc == null || Env.test_params == null )
            System.err.println("**** Setup not complete ****") ;
    }
    
    @Before
    public void before()
    {
        execNoFail("DROP TABLE %s", tempTableName) ;
    }
    
    @Test
    public void emptyStringText()
    {
        String tempTableName =  Env.test_params.get(ParamsVocab.TempTableName) ;
        String colName = Env.test_params.get(ParamsVocab.VarcharCol) ;
        String colType = Env.test_params.get(ParamsVocab.VarcharType) ;
        perform(tempTableName, colName, colType) ;
    }
    
    private void perform(String tempTableName, String colName, String colType)
    {
        try {
            exec("CREATE TABLE %s (%s %s)",  tempTableName, colName, colType) ;
            
            String $str = sqlFormat("INSERT INTO %s values (?)", tempTableName) ;
            if ( verbose )
                System.out.println($str) ;
            
            PreparedStatement ps = jdbc.prepareStatement($str) ;
            ps.setString(1, "") ;
            ps.execute() ;
            ps.close() ;
            ResultSet rs = execQuery("SELECT %s FROM %s ", colName, tempTableName ) ;
            rs.next() ;
            // Null on empty strings (Oracle)
            String s = rs.getString(1) ;
            boolean wasNull = rs.wasNull() ;
            assertEquals(false, wasNull) ;
            
            //if ( s == null ) s = "" ;
            rs.close() ;
            assertEquals("", s) ;
        } catch (SQLException ex)
        { fail("SQLException: "+ex.getMessage()) ; }
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