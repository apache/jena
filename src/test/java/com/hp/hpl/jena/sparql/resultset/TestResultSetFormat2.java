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
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

public class TestResultSetFormat2
{
    @Test (expected=QueryException.class) 
    public void resultset_10()
    {
        // This is illegal
        // Two vars, row of 3 values.
        String x = "?x\t?y\n'a'\t'b'\t'c'" ;
        byte[] b = StrUtils.asUTF8bytes(x) ;
        ByteArrayInputStream in = new ByteArrayInputStream(b) ;
        ResultSet rs2 = ResultSetFactory.fromTSV(in) ;
        
        while (rs2.hasNext())
        {
        	Binding binding = rs2.nextBinding();
        	System.out.println(binding);
        }
    }

    @Test (expected=QueryException.class) 
    public void resultset_11()
    {
        // This is illegal
        // Two vars, row of 1 value only.
        String x = "?x\t?y\n'a'" ;
        byte[] b = StrUtils.asUTF8bytes(x) ;
        ByteArrayInputStream in = new ByteArrayInputStream(b) ;
        ResultSet rs2 = ResultSetFactory.fromTSV(in) ;
        
        while (rs2.hasNext())
        {
        	Binding binding = rs2.nextBinding();
        	System.out.println(binding);
        }
    }    
    
}
