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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFactory ;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.rw.RowSetReaderJSONStreaming;
import org.apache.jena.riot.rowset.rw.RowSetReaderTSV;
import org.apache.jena.riot.rowset.rw.rs_json.Severity;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;
import org.junit.Assert;
import org.junit.Test;

public class TestResultSetFormat2 {
    @Test
    public void resultset_tsv_01() {
        // Empty Header Row (no variables), no rows.
        parseTSV("\n");
    }

    @Test
    public void resultset_tsv_02() {
        // No vars, one row.
        String x = "\n\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_03() {
        // One var, one row empty (unbound)
        String x = "?x\n\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_04() {
        // One var, no rows.
        String x = "?x\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_05() {
        // One var, one rows.
        String x = "?x\n'a'\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_06() {
        // Two vars, one row empty other than the tab separator which is
        // required
        // when two or more variables are present
        String x = "?x\t?y\n\t\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_07() {
        // Three vars, one row of no values
        String x = "?x\t?y\t?z\n\t\t";
        parseTSV(x);
    }

    // various values

    @Test
    public void resultset_tsv_08() {
        String x = "?x\n<http://example/foo>\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_09() {
        String x = "?x\n_:abc\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_11() {
        String x = "?x\n123\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_12() {
        // We allow leading white space.
        String x = "?x\n  123\n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_13() {
        // We allow trailing white space.
        String x = "?x\n123   \n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_14() {
        // We allow trailing white space.
        String x = "?x\n<http://example/>    \n";
        parseTSV(x);
    }

    @Test
    public void resultset_tsv_boolean_01() {
        // true is valid
        String x = "?_askResult\ntrue";
        parseTSVAsBoolean(x, true);
    }

    @Test
    public void resultset_tsv_boolean_02() {
        // true is valid regardless of case
        String x = "?_askResult\nTRUE";
        parseTSVAsBoolean(x, true);
    }

    @Test
    public void resultset_tsv_boolean_03() {
        // true is valid regardless of case
        String x = "?_askResult\ntRuE";
        parseTSVAsBoolean(x, true);
    }

    @Test
    public void resultset_tsv_boolean_04() {
        // yes is valid
        String x = "?_askResult\nyes";
        parseTSVAsBoolean(x, true);
    }

    @Test
    public void resultset_tsv_boolean_05() {
        // yes is valid regardless of case
        String x = "?_askResult\nYES";
        parseTSVAsBoolean(x, true);
    }

    @Test
    public void resultset_tsv_boolean_06() {
        // yes is valid regardless of case
        String x = "?_askResult\nyEs";
        parseTSVAsBoolean(x, true);
    }

    @Test
    public void resultset_tsv_boolean_07() {
        // false is valid
        String x = "?_askResult\nfalse";
        parseTSVAsBoolean(x, false);
    }

    @Test
    public void resultset_tsv_boolean_08() {
        // false is valid regardless of case
        String x = "?_askResult\nFALSE";
        parseTSVAsBoolean(x, false);
    }

    @Test
    public void resultset_tsv_boolean_09() {
        // false is valid regardless of case
        String x = "?_askResult\nfAlSe";
        parseTSVAsBoolean(x, false);
    }

    @Test
    public void resultset_tsv_boolean_10() {
        // no is valid
        String x = "?_askResult\nno";
        parseTSVAsBoolean(x, false);
    }

    @Test
    public void resultset_tsv_boolean_11() {
        // no is valid regardless of case
        String x = "?_askResult\nNO";
        parseTSVAsBoolean(x, false);
    }

    @Test
    public void resultset_tsv_boolean_12() {
        // no is valid regardless of case
        String x = "?_askResult\nnO";
        parseTSVAsBoolean(x, false);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_bad_tsv_01() {
        // Two vars, row of 3 values.
        String x = "?x\t?y\n'a'\t'b'\t'c'";
        parseTSV(x);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_bad_tsv_02() {
        // Two vars, row of 1 value only.
        String x = "?x\t?y\n'a'";
        parseTSV(x);
    }

    @Test(expected = ARQException.class)
    public void resultset_bad_tsv_03() {
        // No input
        parseTSV("");
    }

    @Test(expected = ResultSetException.class)
    public void resultset_bad_tsv_04() {
        // Two vars but a completely empty row (should contain a tab)
        String x = "?x\t?y\n\n";
        parseTSV(x);
    }

    // various values - broken

    @Test(expected = ResultSetException.class)
    public void resultset_bad_tsv_05() {
        String x = "?x\n<http://example/";
        parseTSV(x);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_bad_tsv_06() {
        String x = "?x\n<http://example/ white space >";
        parseTSV(x);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_bad_tsv_07() {
        String x = "?x\n<<<<http://example/>>>>";
        parseTSV(x);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_bad_tsv_08() {
        String x = "?x\n_:abc def";
        parseTSV(x);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_bad_tsv_09() {
        String x = "x\n<http://example.com>";
        parseTSV(x);
    }

    @Test(expected = ARQException.class)
    public void resultset_bad_tsv_boolean_01() {
        // Not in allowed set of true yes false no
        String x = "?_askResults\nblah";
        parseTSVAsBoolean(x, false);
    }

    @Test(expected = ARQException.class)
    public void resultset_bad_tsv_boolean_02() {
        // Missing header
        String x = "true";
        parseTSVAsBoolean(x, false);
    }

    @Test(expected = ARQException.class)
    public void resultset_bad_tsv_boolean_03() {
        // Missing boolean
        String x = "?_askResult\n";
        parseTSVAsBoolean(x, false);
    }

    @Test(expected = ARQException.class)
    public void resultset_bad_tsv_boolean_04() {
        // A normal result set header
        String x = "?x\n";
        parseTSVAsBoolean(x, false);
    }

    @Test(expected = ARQException.class)
    public void resultset_bad_tsv_boolean_05() {
        // A normal result set header
        String x = "?x\t?y\n";
        parseTSVAsBoolean(x, false);
    }

    @Test
    public void resultset_csv_01() {
        // Normal header
        String x = "x,y\n";
        parseCSV(x);
    }

    @Test
    public void resultset_csv_02() {
        // Header with variable names using CSV field encoding i.e. surrounded by quotes
        String x = "\"x\",\"y\"\n";
        parseCSV(x);
    }

    @Test
    public void resultset_json_01() {
        //@formatter:off
        String input = StrUtils.strjoinNL("{\"head\":{\"vars\":[\"s\"]},",
                                          "     \"results\": {",
                                          "      \"bindings\":[",
                                          "       {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}}",
                                          "      ]",
                                          "     },",
                                          "     \"warnings\": [\"parser warning: Variable o was bound but is unused in the query on line 1\",",
                                          "     \"parser warning: Variable p was bound but is unused in the query on line 1\"]",
                                          "    })",
                                          "    }");
        //@formatter:on
        parseJSON(input);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_json_02() {
        //@formatter:off
        String input = StrUtils.strjoinNL("{\"head\":{\"vars\":[\"s\"]},",
                                          "     \"results\": {",
                                          "      \"bindings\":[",
                                          "       {\"s\":{\"type\":\"uri\"}}",
                                          "      ]",
                                          "     },",
                                          "     \"warnings\": [\"parser warning: Variable o was bound but is unused in the query on line 1\",",
                                          "     \"parser warning: Variable p was bound but is unused in the query on line 1\"]",
                                          "    })",
                                          "    }");
        //@formatter:on

        // No value for URI is illegal
        parseJSON(input);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_json_03() {
        String input = "{\"head\":{\"vars\":[\"s\"]}}";

        // Missing results is illegal
        parseJSON(input);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_json_04() {
        String input = "{\"results\":{}}";

        // Missing head is illegal
        parseJSON(input);
    }

    @Test
    public void resultset_json_05_head_after_json() {
        //@formatter:off
        String input = StrUtils.strjoinNL("{",
                                          "  \"results\": {",
                                          "    \"bindings\":[",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}}",
                                          "    ]",
                                          "   },",
                                          "  \"head\":{\"vars\":[\"s\"]}",
                                          "}");
        //@formatter:on
        parseJSON(input);
    }

    @Test
    public void resultset_json_06_repeated_head() {
        //@formatter:off
        String input = StrUtils.strjoinNL("{",
                                          "  \"results\": {",
                                          "    \"bindings\":[",
                                          "      {\"c\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Foobar\"}},",
                                          "      {\"c\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}}",
                                          "    ]",
                                          "   },",
                                          "  \"head\":{\"vars\":[\"a\"]},",
                                          "  \"head\":{\"vars\":[\"b\"]},",
                                          "  \"head\":{\"vars\":[\"c\"]}",
                                          "}");
        //@formatter:on
        ResultSet rs = resultSetFromJSON(input, null);
        Assert.assertEquals(Arrays.asList("c"), rs.getResultVars());
    }

    public static ResultSet resultset_json_07_data(Context cxt) {
        //@formatter:off
        String input = StrUtils.strjoinNL("{",
                                          "  \"results\": {",
                                          "    \"bindings\":[",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}},",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}}",
                                          "    ]",
                                          "   },",
                                          "  \"head\":{\"vars\":[\"a\"]},",
                                          "  \"head\":{\"vars\":[\"b\"]},",
                                          "  \"results\": {",
                                          "    \"bindings\":[",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}},",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}},",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}}",
                                          "    ]",
                                          "   }",
                                          "}");
        //@formatter:on
        return resultSetFromJSON(input, cxt);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_json_07a_repeated_results() {
        ResultSet rs = resultset_json_07_data(null);

        // Fail upon trying to access the 'head' which encounters the repeated
        // 'results' key.
        // The following line is there for documentation/robustness.
        // Actually the involved ResultSet.adapt() already invokes
        // RowSet.getResultVars() eagerly
        rs.getResultVars();
    }

    @Test
    public void resultset_json_07b_repeated_results() {
        Context cxt = new Context();
        cxt.set(RowSetReaderJSONStreaming.rsJsonSeverityInvalidatedResults, Severity.IGNORE);
        ResultSet rs = resultset_json_07_data(cxt);
        // In contrast to 07a retrieving the header should work
        Assert.assertEquals(Arrays.asList("b"), rs.getResultVars());
        // Ignoring the error should give 5 bindings and not log a warning during testing
        Assert.assertEquals(5, ResultSetFormatter.consume(rs));
    }

    public static ResultSet resultset_json_08_data(Context cxt) {
        //@formatter:off
        String input = StrUtils.strjoinNL("{",
                                          "  \"head\":{\"vars\":[\"a\"]},",
                                          "  \"head\":{\"vars\":[\"b\"]},",
                                          "  \"results\": {",
                                          "    \"bindings\":[",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}},",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}},",
                                          "      {\"s\":{\"type\":\"uri\",\"value\":\"http://rdf.myexperiment.org/ontologies/snarm/Policy\"}}",
                                          "    ]",
                                          "   },",
                                          "  \"head\":{\"vars\":[\"c\"]}",
                                          "}");
        //@formatter:on
        return resultSetFromJSON(input, cxt);
    }

    @Test(expected = ResultSetException.class)
    public void resultset_json_08a_repeated_results() {
        ResultSet rs = resultset_json_08_data(null);
        // Scanning for the first head should work
        Assert.assertEquals(Arrays.asList("b"), rs.getResultVars());
        // Expected to fail on last 'head' because it invalidates the seen head
        ResultSetFormatter.consume(rs);
    }

    @Test
    public void resultset_json_08b_repeated_results() {
        Context cxt = new Context();
        cxt.set(RowSetReaderJSONStreaming.rsJsonSeverityInvalidatedHead, Severity.IGNORE);
        ResultSet rs = resultset_json_08_data(cxt);
        Assert.assertEquals(Arrays.asList("b"), rs.getResultVars());
        // Ignoring the error should give 3 bindings and not log a warning during testing
        Assert.assertEquals(3, ResultSetFormatter.consume(rs));
    }

    private void parseTSV(String x) {
        byte[] b = StrUtils.asUTF8bytes(x);
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        ResultSet rs2 = ResultSetMgr.read(in, ResultSetLang.RS_TSV);

        while (rs2.hasNext()) {
            rs2.nextBinding();
        }
    }

    private void parseCSV(String x) {
        byte[] b = StrUtils.asUTF8bytes(x);
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        ResultSet rs2 = ResultSetMgr.read(in, ResultSetLang.RS_CSV);
        while (rs2.hasNext()) {
            rs2.nextBinding();
        }
    }

    private void parseJSON(String input) {
        byte[] b = StrUtils.asUTF8bytes(input);
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        ResultSet rs = ResultSetFactory.fromJSON(in);
        while (rs.hasNext()) {
            rs.nextBinding();
        }
    }

    private static ResultSet resultSetFromJSON(String input, Context cxt) {
        byte[] b = StrUtils.asUTF8bytes(input);
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        RowSet rowSet = RowSetReaderJSONStreaming.factory.create(ResultSetLang.RS_JSON).read(in, cxt);
        ResultSet rs = ResultSet.adapt(rowSet);
        return rs;
    }

    private void parseTSVAsBoolean(String x, boolean expected) {
        byte[] b = StrUtils.asUTF8bytes(x);
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        boolean actual = RowSetReaderTSV.booleanFromTSV(in);
        assertEquals(expected, actual);
    }
}
