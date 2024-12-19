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

package org.apache.jena.fuseki.mod.ui;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Iterator;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.fuseki.ctl.ActionContainerItem;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.WebContent;

/**
 * Text output for stats, than JSON {@link ActionStats}.
 * Separate because content negotiation for known JSON may
 * be a simple "Accept: {@literal *}/*}".
 * <p>
 * <pre>
 *   /$/serviceurl -- all datatsets
 *   /$/serviceurl/name -- one dataset
 * </pre>
 *
 */
public class ActionStatsTxt extends ActionContainerItem {

    public ActionStatsTxt() { super(); }

    @Override
    public void validate(HttpAction action) {}

    // Stats as plain text.
    // Not simple conneg because JSON can be wanted but the HRRP is "Accept */*"
    // Probably replace with a "simple" view.

    // All datasets
    @Override
    protected JsonValue execGetContainer(HttpAction action) {
        execContainer(action);
        return null;
    }

    // One dataset
    @Override
    protected JsonValue execGetItem(HttpAction action) {
        execItem(action);
        return null;
    }

    // POST is GET (but know to be fresh)
    @Override
    protected JsonValue execPostContainer(HttpAction action) {
        execContainer(action);
        return null;
    }

    @Override
    protected JsonValue execPostItem(HttpAction action) {
        execItem(action);
        return null;
    }

    private void execContainer(HttpAction action) {
        if ( action.verbose )
            action.log.info(format("[%d] GET stats text", action.id));
        try {
            statsTxt(action.getResponse(), action.getDataAccessPointRegistry());
        } catch (IOException ex) {
            action.log.warn(format("[%d] GET stats text: IO error: %s", action.id, ex.getMessage()));
        }
    }

    private void execItem(HttpAction action) {
        String name = getItemDatasetName(action);
        if ( name == null )
            ServletOps.errorBadRequest("No dataset name");
        DataAccessPoint desc = action.getDataAccessPointRegistry().get(name);
        if ( desc == null )
            ServletOps.errorBadRequest("No such dataset: "+name);
        try {
            HttpServletResponse resp = action.getResponse();
            ServletOutputStream out = resp.getOutputStream();
            resp.setContentType(WebContent.contentTypeTextPlain);
            resp.setCharacterEncoding(WebContent.charsetUTF8);
            statsTxt(out, desc);
        } catch (IOException ex) {
            action.log.warn(format("[%d] GET stats text: IO error: %s", action.id, ex.getMessage()));
        }
    }

    private void statsTxt(HttpServletResponse resp, DataAccessPointRegistry registry) throws IOException {
        ServletOutputStream out = resp.getOutputStream();
        resp.setContentType(WebContent.contentTypeTextPlain);
        resp.setCharacterEncoding(WebContent.charsetUTF8);

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

        if ( desc.getDataService().hasOperation(Operation.Query)) {
            out.println("  SPARQL Query:");
            out.println("    Request       = " + counter(dSrv, Operation.Query, CounterName.Requests));
            out.println("    Good          = " + counter(dSrv, Operation.Query, CounterName.RequestsGood));
            out.println("    Bad requests  = " + counter(dSrv, Operation.Query, CounterName.RequestsBad));
            out.println("    Timeouts      = " + counter(dSrv, Operation.Query, CounterName.QueryTimeouts));
            out.println("    Bad exec      = " + counter(dSrv, Operation.Query, CounterName.QueryExecErrors));
            //out.println("    IO Errors     = " + counter(dSrv, Operation.Query, CounterName.QueryIOErrors));
        }

        if ( desc.getDataService().hasOperation(Operation.Update)) {
            out.println("  SPARQL Update:");
            out.println("    Request       = " + counter(dSrv, Operation.Update, CounterName.Requests));
            out.println("    Good          = " + counter(dSrv, Operation.Update, CounterName.RequestsGood));
            out.println("    Bad requests  = " + counter(dSrv, Operation.Update, CounterName.RequestsBad));
            out.println("    Bad exec      = " + counter(dSrv, Operation.Update, CounterName.UpdateExecErrors));
        }

        if ( desc.getDataService().hasOperation(Operation.Upload)) {
            out.println("  Upload:");
            out.println("    Requests      = " + counter(dSrv, Operation.Upload, CounterName.Requests));
            out.println("    Good          = " + counter(dSrv, Operation.Upload, CounterName.RequestsGood));
            out.println("    Bad           = " + counter(dSrv, Operation.Upload, CounterName.RequestsBad));
        }

        if ( desc.getDataService().hasOperation(Operation.GSP_R) || desc.getDataService().hasOperation(Operation.GSP_RW) ) {
            out.println("  SPARQL Graph Store Protocol:");
            out.println("    GETs          = " + gspValue(dSrv, CounterName.HTTPget) + " (good=" + gspValue(dSrv, CounterName.HTTPgetGood)
            + "/bad=" + gspValue(dSrv, CounterName.HTTPgetBad) + ")");
            if ( desc.getDataService().hasOperation(Operation.GSP_RW) ) {
                out.println("    PUTs          = " + gspValue(dSrv, CounterName.HTTPput) + " (good=" + gspValue(dSrv, CounterName.HTTPputGood)
                    + "/bad=" + gspValue(dSrv, CounterName.HTTPputBad) + ")");
                out.println("    POSTs         = " + gspValue(dSrv, CounterName.HTTPpost) + " (good=" + gspValue(dSrv, CounterName.HTTPpostGood)
                    + "/bad=" + gspValue(dSrv, CounterName.HTTPpostBad) + ")");
                out.println("    PATCHs        = " + gspValue(dSrv, CounterName.HTTPpatch) + " (good=" + gspValue(dSrv, CounterName.HTTPpatchGood)
                    + "/bad=" + gspValue(dSrv, CounterName.HTTPpatchBad) + ")");
                out.println("    DELETEs       = " + gspValue(dSrv, CounterName.HTTPdelete) + " (good=" + gspValue(dSrv, CounterName.HTTPdeleteGood)
                    + "/bad=" + gspValue(dSrv, CounterName.HTTPdeleteBad) + ")");
            }
            out.println("    HEADs         = " + gspValue(dSrv, CounterName.HTTPhead) + " (good=" + gspValue(dSrv, CounterName.HTTPheadGood)
                        + "/bad=" + gspValue(dSrv, CounterName.HTTPheadBad) + ")");
        }
    }

    private long counter(DataService dSrv, Operation operation, CounterName cName) {
        return 0;
    }

    private long gspValue(DataService dSrv, CounterName cn) {
        return  counter(dSrv, Operation.GSP_RW, cn) +
                counter(dSrv, Operation.GSP_R, cn);
    }
}


