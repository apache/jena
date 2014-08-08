/**
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

package org.apache.jena.riot.resultset;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.Lang ;
import org.junit.Before ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetRewindable ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderResultSet ;

@RunWith(Parameterized.class)
public class TestResultSetIO {
    @Parameters(name = "{index}: {0}") public static Collection<Object[]> data()
    { 
        Lang[] langs = { ResultSetLang.SPARQLResultSetXML
                       , ResultSetLang.SPARQLResultSetJSON
                       , ResultSetLang.SPARQLResultSetCSV
                       , ResultSetLang.SPARQLResultSetTSV
        } ;
        
        List<Object[]> x = new ArrayList<>() ;
        for ( Lang lang : langs ) {
            x.add(new Object[]{ "test:"+lang.getName(), lang } ) ;
        }
        return x ;                                
    }
    
    static String rsStr = StrUtils.strjoinNL
        ("(resultset (?x ?y)"
        ,"   (row (?x _:b0) (?y _:b1))"
        ,"   (row (?x _:b2) (?y _:b3))"
        ,"   (row (?x _:b1) (?y _:b0))"
        ,"   (row (?x 1) )"
        ,"   (row (?y 2) )"
        ,"   (row )"
        ,")"
        ) ;
    
    static ResultSetRewindable test_rs = ResultSetFactory.makeRewindable(BuilderResultSet.build(SSE.parse(rsStr))) ;

    private final Lang lang ;
    @Before public void beforetest() { test_rs.reset() ; }
    
    public TestResultSetIO(String name, Lang lang) {
        this.lang = lang ;
    }
    
    @Test public void test_resultset_01() {
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ResultSetMgr.write(out, test_rs, lang) ;
        test_rs.reset(); 
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        ResultSet rs = ResultSetMgr.read(in, lang) ;
        ResultSetCompare.equalsByTerm(test_rs, rs) ;
        
        out.reset();  
        ResultSetMgr.write(out, rs, lang) ;
        test_rs.reset(); 
        in = new ByteArrayInputStream(out.toByteArray()) ;
        ResultSet rs2 = ResultSetMgr.read(in, lang) ;
        ResultSetCompare.equalsByTerm(test_rs, rs2) ;
    }
}

