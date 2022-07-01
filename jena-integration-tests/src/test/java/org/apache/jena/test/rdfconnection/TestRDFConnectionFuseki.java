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

package org.apache.jena.test.rdfconnection;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.web.HttpSC.Code;
import org.junit.Ignore;
import org.junit.Test;

public class TestRDFConnectionFuseki extends TestRDFConnectionRemote {
    @Override
    protected RDFConnection connection() {
        return RDFConnection.connect(server.datasetURL("/ds"));
    }

    @Ignore
    @Override
    @Test(expected=QueryParseException.class)
    public void non_standard_syntax_0() {}

    @Override
    @Test(expected=QueryParseException.class)
    public void non_standard_syntax_1() {
        RDFConnection conn = RDFConnectionFuseki.service(server.datasetURL("/ds")).parseCheckSPARQL(true).build();
        try ( conn ) {
            ResultSet rs = conn.query("FOOBAR").execSelect();
        }
    }

    @Override
    @Test
    public void non_standard_syntax_2() {
        // This should result in a 400 from Fuseki - and not a parse-check before sending.
        RDFConnection conn = RDFConnectionFuseki.service(server.datasetURL("/ds")).parseCheckSPARQL(false).build();
        try ( conn ) {
            String level = LogCtl.getLevel(Fuseki.actionLog);
            try {
                LogCtl.setLevel(Fuseki.actionLog, "ERROR");
                FusekiTestLib.expectQueryFail(()->conn.query("FOOBAR").execSelect(), Code.BAD_REQUEST);
            } finally {
                LogCtl.setLevel(Fuseki.actionLog, level);
            }
        }
    }

}
