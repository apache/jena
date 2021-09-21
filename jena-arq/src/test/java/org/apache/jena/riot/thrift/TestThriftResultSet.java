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

package org.apache.jena.riot.thrift;

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.InputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFactory ;
import org.apache.jena.query.ResultSetRewindable ;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.builders.BuilderResultSet ;
import org.junit.Test ;

public class TestThriftResultSet {
    // Only datatypes that transmitted perfectly. 
    static ResultSetRewindable rs0 = make
        ("(resultset (?x ?y)"
         , "   (row (?x _:a) (?y 3))"
         , "   (row (?x 1) (?y 'a'))"
         , "   (row (?y 'y'))"
         , "   (row (?x _:a))"
         , "   (row)"
         , "   (row (?x 2) (?y 10))"
         , ")"
         ) ;
    
    static ResultSetRewindable rs1 = make
        ("(resultset (?x ?y)"
         , "   (row (?x 1) (?y 3))"
         , "   (row (?x 1) (?y 'a'))"
         , ")"
         ) ;
    static ResultSetRewindable rs2 = make
        ("(resultset (?x ?y)"
         , "   (row (?x 1) (?y 'a'))"
         , "   (row (?x 1) (?y 3))"
         , ")"
         ) ;

    @Test public void resultSet_01() { test(rs0) ; }
    
    @Test public void resultSet_02() { 
        ResultSetRewindable r1 = test(rs1) ;
        // not reordered
        r1.reset();
        rs2.reset() ;
        assertFalse(ResultSetCompare.equalsByTermAndOrder(r1, rs2)) ;
        rs2.reset() ;
    }
    
    @Test public void resultSet_03() { 
        ResultSetRewindable r2 = test(rs2) ;
        // not reordered
        r2.reset();
        rs1.reset() ;
        assertFalse(ResultSetCompare.equalsByTermAndOrder(r2, rs1)) ;
        rs1.reset() ;
    }

    private static ResultSetRewindable test(ResultSetRewindable resultSet) {
        resultSet.reset(); 
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ThriftRDF.writeResultSet(out, resultSet, true) ; 
        resultSet.reset();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        ResultSet rs$ = ThriftRDF.readResultSet(in) ;
        ResultSetRewindable resultSet2 = ResultSetFactory.makeRewindable(rs$) ;
        // Includes bnode labels. 
        ResultSetCompare.equalsExact(resultSet, resultSet2) ;
        resultSet.reset(); 
        resultSet2.reset();
        return resultSet2 ;
    }

    private static ResultSetRewindable make(String ... strings) {
        String s = StrUtils.strjoinNL(strings) ; 
        Item item = SSE.parse(s) ;
        ResultSetRewindable rs = ResultSetFactory.makeRewindable(BuilderResultSet.build(item)) ;
        return rs ;
    }
    
    private static final String DIR = TS_RDFThrift.TestingDir ;
    
    @Test public void resultSet_10() {
        try (InputStream in = IO.openFile(DIR+"/results-1.srj")) {
            ResultSet rs = ResultSetFactory.fromJSON(in) ;
            test(ResultSetFactory.copyResults(rs)) ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }
}

