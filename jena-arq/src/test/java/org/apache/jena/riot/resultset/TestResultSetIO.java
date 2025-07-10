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

package org.apache.jena.riot.resultset;

import static org.apache.jena.riot.resultset.ResultSetLang.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.sparql.resultset.ResultsCompare;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.sse.builders.BuilderRowSet;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestResultSetIO {

    private static Stream<Arguments> provideArgs() {
        Lang[] langs = { RS_XML
                       , RS_JSON
                       , RS_CSV
                       , RS_TSV
                       , RS_Thrift
                       , RS_Protobuf
        };

        List<Arguments> x = new ArrayList<>();
        for ( Lang lang : langs ) {
            x.add(Arguments.of(lang));
        }
        return x.stream();
    }

    static String rsStr = StrUtils.strjoinNL
        ("(resultset (?x ?y)"
        ,"   (row (?x _:b0) (?y _:b1))"
        ,"   (row (?x _:b2) (?y _:b3))"
        ,"   (row (?x _:b1) (?y _:b0))"
        ,"   (row (?x 1)           )"
        ,"   (row           (?y 2) )"
        ,"   (row )"
        ,"   (row (?x 'abc'@en--ltr))"
        ,"   (row (?x 'abc'@en))"
        ,"   (row (?x 'abc'))"
        ,")"
        );

    static ResultSetRewindable test_rs = ResultSetFactory.makeRewindable(BuilderRowSet.build(SSE.parse(rsStr)));

    @BeforeEach public void beforeTest() { test_rs.reset(); }

    @Parameter private Lang lang;

    @Test public void test_resultset_01() {
        // write(data)-read-compare
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        ResultSetMgr.write(out1, test_rs, lang);
        test_rs.reset();

        ByteArrayInputStream in = new ByteArrayInputStream(out1.toByteArray());

        ResultSet rs = ResultSetMgr.read(in, lang);
        ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs);
        if ( ! lang.equals(RS_CSV) ) {
            // CSV is not faithful
            assertTrue(ResultsCompare.equalsByTerm(test_rs, rsw));
        }

        rsw.reset();
        test_rs.reset();

        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        // Round trip the output from above - write(rsw)-read-compare
        ResultSetMgr.write(out2, rsw, lang);
        rsw.reset();
        in = new ByteArrayInputStream(out2.toByteArray());
        ResultSet rs2 = ResultSetMgr.read(in, lang);
        assertTrue(ResultsCompare.equalsByTerm(rsw, rs2));
    }
}

