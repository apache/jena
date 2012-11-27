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
import java.io.ByteArrayOutputStream ;
import java.util.Arrays ;
import java.util.Collection ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Assert;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderResultSet ;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

@RunWith(Parameterized.class)
public class TestResultSetFormat1
{
    // A result set of no variables and no rows.
    static String[] $rs0 = { "(resultset ())" } ;
    
    // A result set of no variables and one row (e.g SELECT * {})
    static String[] $rs1 = { "(resultset () (row))" } ;

    static String[] $rs2 = {
        "(resultset (?a ?b ?c)",
        "  (row (?a 1) (?b 2)       )",
        "  (row (?a 1) (?b 4) (?c 3))",
        ")"} ;

    static String[] $rs3 = {
        "(resultset (?a ?b ?c)",
        "  (row (?a 1) (?b 4) (?c 3))",
        "  (row (?a 1) (?b 2)       )",
        ")"} ;

    static String[] $rs4 = {
        "(resultset (?a ?b ?c)", 
        "  (row (?a 1)        (?c 4))",
        "  (row (?a 1) (?b 2) (?c 3))",
        ")"} ;
    
    static String[] $rs5 = {
    	"(resultset (?a ?b)",
    	" (row (?a 1)       )",
    	" (row        (?b 2))",
    	")" };
    
    static String[] $rs6 = {
    	"(resultset (?x)",
    	" (row (?x <" + RDF.type.toString() + ">))",
    	" (row (?x <" + RDFS.label.toString() + ">))",
    	" (row (?x <" + XSD.integer.toString() + ">))",
    	" (row (?x <" + OWL.sameAs.toString() + ">))",
    	" (row )",
    	")" };
    
    static String[] $rs7 = {
        "(resultset (?x) (row))" } ;
    
    static String[] $rs8 = {
    	"(resultset (?x)",
    	" (row (?x \"has \\t tab character\"))",
    	")" } ;
    
    static String[] $rs9 = {
    	"(resultset (?x)",
    	" (row (?x _:bnode))",
    	")" } ;
    
    static String[] $rs10 = {
    	"(resultset (?x)",
    	" (row (?x \"Includes a raw	tab character\"))",
    	")" } ;
    
    static String[] $rs11 = {
    	"(resultset (?x)",
    	" (row (?x \"Includes \\n new line\"))",
    	")" } ;

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][] { {$rs0}, {$rs1}, {$rs2}, {$rs3}, {$rs4}, {$rs5}, {$rs6}, {$rs7}, {$rs8}, {$rs9}, {$rs10}, {$rs11} } ) ;
    }

    private final String[] $rs ;
    
    public TestResultSetFormat1(String[] rs)
    {
        this.$rs = rs ;
    }
    
    static ResultSet make(String... strings)
    {
        if ( strings.length == 0 )
            throw new IllegalArgumentException() ;
        
        String x = StrUtils.strjoinNL(strings) ;
        Item item = SSE.parse(x) ;
        return ResultSetFactory.makeRewindable(BuilderResultSet.build(item));
    }
    
    @Test public void resultset_01()           
    {
        ResultSet rs = make($rs) ; 
        ResultSetFormatter.asText(rs) ;
    }
    
    @Test public void resultset_02()           
    {
        ResultSet rs = make($rs) ; 
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsXML(out, rs) ;
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromXML(in) ;
        areIsomorphic(rs, rs2);
    }

    @Test public void resultset_03()           
    {
        ResultSet rs = make($rs) ; 
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsJSON(out, rs) ;
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromJSON(in) ;
        areIsomorphic(rs, rs2);
    }
    
    @Test public void resultset_04()           
    {
        ResultSet rs = make($rs) ; 
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsTSV(out, rs) ;
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromTSV(in) ;
        areIsomorphic(rs, rs2);
    }

    @Test public void resultset_05()           
    {
        ResultSet rs = make($rs) ; 
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsCSV(out, rs) ;
    }
    
    private static void areIsomorphic(ResultSet x, ResultSet y)
    {
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(x) ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(y) ;
//        System.out.println(ResultSetFormatter.asText(rs1));
//        System.out.println();
//        System.out.println(ResultSetFormatter.asText(rs2));
//        rs1.reset();
//        rs2.reset();
        Assert.assertTrue(ResultSetCompare.isomorphic(rs1, rs2)) ;
    }

}
