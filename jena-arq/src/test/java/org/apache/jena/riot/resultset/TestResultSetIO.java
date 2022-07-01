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

import static org.apache.jena.riot.resultset.ResultSetLang.* ;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFactory ;
import org.apache.jena.query.ResultSetRewindable ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.ResultSetMgr ;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.builders.BuilderRowSet;
import org.junit.Before ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)
public class TestResultSetIO {
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        Lang[] langs = { RS_XML
                       , RS_JSON
                       , RS_CSV
                       , RS_TSV
                       , RS_Thrift
                       , RS_Protobuf
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
        ,"   (row (?x 1)           )"
        ,"   (row           (?y 2) )"
        ,"   (row )"
        ,")"
        ) ;

    static ResultSetRewindable test_rs = ResultSetFactory.makeRewindable(BuilderRowSet.build(SSE.parse(rsStr))) ;

    private final Lang lang ;
    @Before public void beforetest() { test_rs.reset() ; }

    public TestResultSetIO(String name, Lang lang) {
        this.lang = lang ;
    }

    @Test public void test_resultset_01() {
        // write(data)-read-compare
        ByteArrayOutputStream out1 = new ByteArrayOutputStream() ;
        ResultSetMgr.write(out1, test_rs, lang) ;
        test_rs.reset();
        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray()) ;

        ResultSet rs = ResultSetMgr.read(in, lang) ;
        ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs) ;
        if ( ! lang.equals(RS_CSV) )
            // CSV is not faithful
            assertTrue(ResultSetCompare.equalsByTerm(test_rs, rsw)) ;

        rsw.reset();
        test_rs.reset();

        ByteArrayOutputStream out2 = new ByteArrayOutputStream() ;
        // Round trip the output from above - write(rsw)-read-compare
        ResultSetMgr.write(out2, rsw, lang) ;
        rsw.reset();
        in = new ByteArrayInputStream(out2.toByteArray()) ;
        ResultSet rs2 = ResultSetMgr.read(in, lang) ;
        assertTrue(ResultSetCompare.equalsByTerm(rsw, rs2)) ;
    }
}

