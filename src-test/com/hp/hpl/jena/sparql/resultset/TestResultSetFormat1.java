/*
 * (c) Copyright 2009 Talis Systems Ltd
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.util.Arrays ;
import java.util.Collection ;

import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderResultSet ;

@RunWith(Parameterized.class)
public class TestResultSetFormat1
{

    static String[] $rs1 = {
        "(resultset (?a ?b ?c)",
        "  (row (?a 1) (?b 2)       )",
        "  (row (?a 1) (?b 4) (?c 3))",
        ")"} ;

    static String[] $rs2 = {
        "(resultset (?a ?b ?c)",
        "  (row (?a 1) (?b 4) (?c 3))",
        "  (row (?a 1) (?b 2)       )",
        ")"} ;

    static String[] $rs3 = {
        "(resultset (?a ?b ?c)", 
        "  (row (?a 1)        (?c 4))",
        "  (row (?a 1) (?b 2) (?c 3))",
        ")"} ;

    
    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][] { {$rs1}, {$rs2}, {$rs3} } ) ;
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
        return BuilderResultSet.build(item) ;
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
    }

    @Test public void resultset_03()           
    {
        ResultSet rs = make($rs) ; 
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsJSON(out, rs) ;
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromJSON(in) ;
    }
    
    @Test public void resultset_04()           
    {
        ResultSet rs = make($rs) ; 
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsTSV(out, rs) ;
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        ResultSet rs2 = ResultSetFactory.fromTSV(in) ;
    }

    @Test public void resultset_05()           
    {
        ResultSet rs = make($rs) ; 
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        ResultSetFormatter.outputAsCSV(out, rs) ;
    }
}

/*
 * (c) Copyright 2009 Talis Systems Ltd
 * (c) Copyright 2011 Epimorphics Ltd.
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