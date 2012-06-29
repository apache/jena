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

package com.hp.hpl.jena.sparql.resultset;

import java.io.ByteArrayInputStream ;

import org.junit.Test ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

public class TestResultSetFormat2
{
    @Test
    public void resultset_tsv_01()
    {
        // Empty Header Row (no variables), no rows.
        parseTSV("\n");
    }
    
    @Test 
    public void resultset_tsv_02()
    {
        // No vars, one row.
        String x = "\n\n" ;
        parseTSV(x);
    }
    
    @Test
    public void resultset_tsv_03()
    {
        // One var, one row empty (unbound)
        String x = "?x\n\n";
        parseTSV(x);
    }

    @Test 
    public void resultset_tsv_04()
    {
        // One var, no rows.
        String x = "?x\n" ;
        parseTSV(x);
    }

    @Test 
    public void resultset_tsv_05()
    {
        // One var, one rows.
        String x = "?x\n'a'\n" ;
        parseTSV(x);
    }
    
    @Test
    public void resultset_tsv_06()
    {
    	// Two vars, one row empty other than the tab separator which is required
    	// when two or more variables are present
    	String x = "?x\t?y\n\t\n";
    	parseTSV(x);
    }
    
    @Test
    public void resultset_tsv_07()
    {
    	//Three vars, one row of no values
    	String x = "?x\t?y\t?z\n\t\t";
    }
    
    @Test (expected=QueryException.class) 
    public void resultset_bad_tsv_01()
    {
        // Two vars, row of 3 values.
        String x = "?x\t?y\n'a'\t'b'\t'c'" ;
        parseTSV(x);
    }

    @Test (expected=QueryException.class) 
    public void resultset_bad_tsv_02()
    {
        // Two vars, row of 1 value only.
        String x = "?x\t?y\n'a'" ;
        parseTSV(x);
    }

    @Test (expected=ARQException.class)
    public void resultset_bad_tsv_03()
    {
    	// No input
    	parseTSV("");
    }
    
    @Test (expected=QueryException.class)
    public void resultset_bad_tsv_04()
    {
    	//Two vars but a completely empty row (should contain a tab)
    	String x = "?x\t?y\n\n";
    	parseTSV(x);
    }
        
    public void parseTSV(String x)
    {
        byte[] b = StrUtils.asUTF8bytes(x) ;
        ByteArrayInputStream in = new ByteArrayInputStream(b) ;
        ResultSet rs2 = ResultSetFactory.fromTSV(in) ;
        
        while (rs2.hasNext())
        {
        	Binding binding = rs2.nextBinding();
        }
    }
    
}
