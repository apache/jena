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

package org.apache.jena.sparql.resultset;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.sse.Item;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.sse.builders.BuilderRowSet;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestResultSetFormat1
{
    static { JenaSystem.init(); }

    // Test with ResultSets.
    // This does all the RowSet testing because ResultSets are now just wrappers on RowSets.

    // A result set of no variables and no rows.
    static String $rs0 = "(resultset ())";

    // A result set of no variables and one row (e.g SELECT * {})
    static String $rs1 = "(resultset () (row))" ;

    static String $rs2 = """
        (resultset (?a ?b ?c)
          (row (?a 1) (?b 2)       )
          (row (?a 1) (?b 4) (?c 3))
        )
        """;

    static String $rs3 = """
        (resultset (?a ?b ?c)
          (row (?a 1) (?b 4) (?c 3))
          (row (?a 1) (?b 2)       )
        )
        """;

    static String $rs4 = """
        (resultset (?a ?b ?c)
          (row (?a 1)        (?c 4))
          (row (?a 1) (?b 2) (?c 3))
        )
        """;

    static String $rs5 = """
    	(resultset (?a ?b)
    	  (row (?a 1)       )
    	  (row        (?b 2))
    	)
    	""";

    static String $rs6 = """
    	(resultset (?x)
    	"""
    	+"  (row (?x <" + RDF.type.toString() + ">))\n"
    	+"  (row (?x <" + RDFS.label.toString() + ">))\n"
        +"    (row (?x <" + XSD.integer.toString() + ">))\n"
        +"  (row (?x <" + OWL.sameAs.toString() + ">))\n"
    	+"""
    	  (row )
    	)
    	""";

    static String $rs7 = "(resultset (?x) (row))";

    static String $rs8 = """
    	(resultset (?x)
    	  (row (?x \"has \\t tab character\"))
    	)
    	""";

    static String $rs9 = """
    	(resultset (?x)
    	  (row (?x _:bnode))
    	)
    	""";

    static String $rs10 = """
    	(resultset (?x)
    	  (row (?x \"Includes a raw	tab character\"))
    	)
    	""";

    static String $rs11 = """
    	(resultset (?x)
    	  (row (?x \"Includes \\n new line\"))
    	)
    	""";


    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of(Arguments.of($rs0), Arguments.of($rs1), Arguments.of($rs2), Arguments.of($rs3), Arguments.of($rs4),
                                    Arguments.of($rs5), Arguments.of($rs6), Arguments.of($rs7), Arguments.of($rs8), Arguments.of($rs9),
                                    Arguments.of($rs10), Arguments.of($rs11));
        return x.stream();
    }

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { {$rs0}, {$rs1}, {$rs2}, {$rs3}, {$rs4}, {$rs5}, {$rs6}, {$rs7}, {$rs8}, {$rs9}, {$rs10}, {$rs11} } );
    }

    @Parameter(0)
    String $rs;

    static ResultSet make(String...strings) {
        if ( strings.length == 0 )
            throw new IllegalArgumentException();

        String x = StrUtils.strjoinNL(strings);
        Item item = SSE.parse(x);
        return ResultSetFactory.makeRewindable(BuilderRowSet.build(item));
    }

    @Test
    public void resultset_01() {
        ResultSet rs = make($rs);
        ResultSetFormatter.asText(rs);
    }

    @Test
    public void resultset_02() {
        ResultSet rs = make($rs);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsXML(out, rs);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ResultSet rs2 = ResultSetFactory.fromXML(in);
        checkIsomorphic(rs, rs2);
    }

    @Test
    public void resultset_03() {
        ResultSet rs = make($rs);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(out, rs);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ResultSet rs2 = ResultSetFactory.fromJSON(in);
        checkIsomorphic(rs, rs2);
    }

    @Test
    public void resultset_04() {
        ResultSet rs = make($rs);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsTSV(out, rs);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ResultSet rs2 = ResultSetMgr.read(in, ResultSetLang.RS_TSV);
        checkIsomorphic(rs, rs2);
    }

    @Test
    public void resultset_05() {
        ResultSet rs = make($rs);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsCSV(out, rs);
    }

    private static void checkIsomorphic(ResultSet x, ResultSet y) {
        ResultSetRewindable rs1 = x.rewindable();
        ResultSetRewindable rs2 = y.rewindable();
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

}
