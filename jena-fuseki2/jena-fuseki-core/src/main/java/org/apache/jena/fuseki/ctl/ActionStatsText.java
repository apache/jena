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

package org.apache.jena.fuseki.ctl;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;

// Unused - left in case it should be resurrected.
public class ActionStatsText extends ActionCtl
{
    public ActionStatsText() { super(); }

    @Override
    public void validate(HttpAction action) {
        switch(action.getMethod() ) {
            case HttpNames.METHOD_GET:
            case HttpNames.METHOD_POST:
                return;
            default:
                ServletOps.errorMethodNotAllowed(action.getMethod());
        }
    }

    @Override
    public void execute(HttpAction action) {
        try {
            statsTxt(action.getResponse(), action.getDataAccessPointRegistry());
        }
        catch (IOException ex) {
            IO.exception(ex);
        }
    }

    // Text output
    private void statsTxt(HttpServletResponse resp, DataAccessPointRegistry registry) throws IOException {
        ServletOps.setNoCache(resp);
        resp.setContentType(WebContent.contentTypeTextPlain);
        resp.setCharacterEncoding(WebContent.charsetUTF8);

        ServletOutputStream out = resp.getOutputStream();
        Iterator<String> iter = registry.keys().iterator();
        while (iter.hasNext()) {
            String ds = iter.next();
            DataAccessPoint desc = registry.get(ds);
            statsTxt(out, desc);
            if ( iter.hasNext() )
                out.println();
        }
        out.flush();
    }

    private void statsTxt(ServletOutputStream out, DataAccessPoint desc) throws IOException {
        DataService dSrv = desc.getDataService();
        out.println("Dataset: " + desc.getName());
        out.println("    Requests      = " + dSrv.getCounters().value(CounterName.Requests));
        out.println("    Good          = " + dSrv.getCounters().value(CounterName.RequestsGood));
        out.println("    Bad           = " + dSrv.getCounters().value(CounterName.RequestsBad));

        if ( dSrv.getOperations().contains( Operation.Query) ) {
            out.println("  SPARQL Query:");
            out.println("    Request       = " + counter(dSrv, Operation.Query, CounterName.Requests));
            out.println("    Good          = " + counter(dSrv, Operation.Query, CounterName.RequestsGood));
            out.println("    Bad requests  = " + counter(dSrv, Operation.Query, CounterName.RequestsBad));
            //out.println("    Timeouts      = " + counter(dSrv, Operation.Query, CounterName.QueryTimeouts));
            //out.println("    Bad exec      = " + counter(dSrv, Operation.Query, CounterName.QueryExecErrors));
            //out.println("    IO Errors     = " + counter(dSrv, Operation.Query, CounterName.QueryIOErrors));
        }

        if ( dSrv.getOperations().contains( Operation.Update) ) {
            out.println("  SPARQL Update:");
            out.println("    Request       = " + counter(dSrv, Operation.Update, CounterName.Requests));
            out.println("    Good          = " + counter(dSrv, Operation.Update, CounterName.RequestsGood));
            out.println("    Bad requests  = " + counter(dSrv, Operation.Update, CounterName.RequestsBad));
            //out.println("    Bad exec      = " + counter(dSrv, Operation.Update, CounterName.UpdateExecErrors));
        }

        if ( dSrv.getOperations().contains( Operation.GSP_R) || dSrv.getOperations().contains( Operation.GSP_RW) ) {
            out.println("  SPARQL Graph Store Protocol:");
            if ( dSrv.getOperations().contains( Operation.GSP_R) ) {
                out.println("    GETs          = "
                            + counter(dSrv, Operation.GSP_R, CounterName.Requests)
                            +" (good=" + counter(dSrv, Operation.GSP_R, CounterName.RequestsGood)
                            + "/bad=" + counter(dSrv, Operation.GSP_R, CounterName.RequestsBad)+")"
                            );
            }
            if ( dSrv.getOperations().contains( Operation.GSP_RW) ) {
                out.println("    Writes        = "
                        + counter(dSrv, Operation.GSP_RW, CounterName.Requests)
                        +" (good=" + counter(dSrv, Operation.GSP_RW, CounterName.RequestsGood)
                        + "/bad=" + counter(dSrv, Operation.GSP_RW, CounterName.RequestsBad)+")"
                        );
            }
        }

//            if ( dSrv.getOperations().contains( Operation.GSP_RW) ) {
//                out.println("  SPARQL Graph Store Protocol:");
//                out.println("    GETs          = " + gspValue(dSrv, CounterName.HTTPget) + " (good=" + gspValue(dSrv, CounterName.HTTPgetGood)
//                            + "/bad=" + gspValue(dSrv, CounterName.HTTPgetBad) + ")");
//                out.println("    PUTs          = " + gspValue(dSrv, CounterName.HTTPput) + " (good=" + gspValue(dSrv, CounterName.HTTPputGood)
//                            + "/bad=" + gspValue(dSrv, CounterName.HTTPputBad) + ")");
//                out.println("    POSTs         = " + gspValue(dSrv, CounterName.HTTPpost) + " (good=" + gspValue(dSrv, CounterName.HTTPpostGood)
//                            + "/bad=" + gspValue(dSrv, CounterName.HTTPpostBad) + ")");
//                out.println("    PATCHs        = " + gspValue(dSrv, CounterName.HTTPpatch) + " (good=" + gspValue(dSrv, CounterName.HTTPpatchGood)
//                            + "/bad=" + gspValue(dSrv, CounterName.HTTPpatchBad) + ")");
//                out.println("    DELETEs       = " + gspValue(dSrv, CounterName.HTTPdelete) + " (good=" + gspValue(dSrv, CounterName.HTTPdeleteGood)
//                            + "/bad=" + gspValue(dSrv, CounterName.HTTPdeleteBad) + ")");
//                out.println("    HEADs         = " + gspValue(dSrv, CounterName.HTTPhead) + " (good=" + gspValue(dSrv, CounterName.HTTPheadGood)
//                            + "/bad=" + gspValue(dSrv, CounterName.HTTPheadBad) + ")");
//            } else if ( dSrv.getOperations().contains( Operation.GSP_R) ) {
//                out.println("  SPARQL Graph Store Protocol:");
//                out.println("    GETs          = " + gspValue(dSrv, CounterName.HTTPget) + " (good=" + gspValue(dSrv, CounterName.HTTPgetGood)
//                            + "/bad=" + gspValue(dSrv, CounterName.HTTPgetBad) + ")");
//                out.println("    HEADs         = " + gspValue(dSrv, CounterName.HTTPhead) + " (good=" + gspValue(dSrv, CounterName.HTTPheadGood)
//                            + "/bad=" + gspValue(dSrv, CounterName.HTTPheadBad) + ")");
//            }
    }

    private long counter(DataService dSrv, Operation operation, CounterName cName) {
        long x = 0;
        for ( Endpoint ep : dSrv.getEndpoints(operation) ) {
            System.out.println(cName.toString());
            System.out.println(ep.getCounters());
            x += ep.getCounters().value(cName);
        }
        return x;
    }
}


