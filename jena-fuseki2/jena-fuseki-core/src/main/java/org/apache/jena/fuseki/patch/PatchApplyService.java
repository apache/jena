/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.fuseki.patch;

import static java.lang.String.format;
import static org.apache.jena.fuseki.servlets.ActionExecLib.incCounter;

import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.server.CounterName;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.rdfpatch.PatchException;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.changes.*;
import org.apache.jena.rdfpatch.text.RDFPatchReaderText;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.web.HttpSC;

/** A Fuseki service to receive and apply a patch. */
public class PatchApplyService extends ActionREST {
    static CounterName counterPatches     = CounterName.register("RDFpatch-apply", "rdf-patch.apply.requests");
    static CounterName counterPatchesGood = CounterName.register("RDFpatch-apply", "rdf-patch.apply.good");
    static CounterName counterPatchesBad  = CounterName.register("RDFpatch-apply", "rdf-patch.apply.bad");

    // It's an ActionREST because it accepts POST/PATCH with a content body.
    private ContentType ctPatchText   = WebContent.ctPatch;
    private ContentType ctPatchBinary = WebContent.ctPatchThrift;

    public PatchApplyService() {
        // Counters: the standard ActionREST counters per operation are enough.
    }

    @Override
    public void validate(HttpAction action) {
        String method = action.getRequest().getMethod();
        switch(method) {
            case HttpNames.METHOD_POST:
            case HttpNames.METHOD_PATCH:
                break;
            default:
                ServletOps.errorMethodNotAllowed(method+" : Patch must use POST or PATCH");
        }
        String ctStr = action.getRequest().getContentType();
        // Must be UTF-8 or unset. But this is wrong so often.
        // It is less trouble to just force UTF-8.
        String charset = action.getRequest().getCharacterEncoding();
        if ( charset != null && ! WebContent.charsetUTF8.equalsIgnoreCase(charset) )
            ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Charset must be omitted or UTF-8, not "+charset);

        if ( WebContent.contentTypeHTMLForm.equals(ctStr) ) {
            // Both "curl --data" and "wget --post-file"
            // without also using "--header" will set the Content-type to "application/x-www-form-urlencoded".
            // Treat this as "unset".
            ctStr = null;
        }

        // If no header Content-type - assume patch-text.
        ContentType contentType = ( ctStr != null ) ? ContentType.create(ctStr) : ctPatchText;

        if ( ! ctPatchText.equals(contentType) && ! ctPatchBinary.equals(contentType) )
            ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Allowed Content-types are "+ctPatchText+" or "+ctPatchBinary+", not "+ctStr);
        if ( ctPatchBinary.equals(contentType) )
            ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, ctPatchBinary.getContentTypeStr()+" not supported yet");
    }

    protected void operation(HttpAction action) {
        incCounter(action.getEndpoint(), counterPatches);
        try {
            operation$(action);
            incCounter(action.getEndpoint(), counterPatchesGood) ;
        } catch ( ActionErrorException ex ) {
            incCounter(action.getEndpoint(), counterPatchesBad) ;
            throw ex ;
        }
    }

    private void operation$(HttpAction action) {
        action.beginWrite();
        try {
            applyRDFPatch(action, WithPatchTxn.EXTERNAL_TXN);
            action.commit();
        }
        catch (PatchTxnAbortException ex) {
            // This is not an error in the service.
            // The patch said "abort", and transactions are being managed by this code,
            // so the patch processor throws TransactionAbortException if an abort is encountered.
            // where one patch is one transaction.
            action.abort();
            action.log.info(format("[%d] RDF Patch: abort in patch", action.id));
        }
        catch (Exception ex) {
            action.abort();
            throw ex;
        } finally { action.end(); }
        ServletOps.success(action);
    }

    private enum WithPatchTxn { PATCH_TXN, EXTERNAL_TXN }

    /** Apply a patch action.
     * <p>
     * {@code withPatchTxn} controls whether to respect the TX-TC in the patch itself,
     * or whether to execute ignoring them, allowing the caller to manage the transaction,
     * such as put one transaction around the whole patch.
     * Abort is always
     *
     * @param action
     * @param withPatchTxn Whether to use transaction markers in the patch or assume call is managing transactions.
     */
    private void applyRDFPatch(HttpAction action, WithPatchTxn withPatchTxn) {
        try {
            String ct = action.getRequest().getContentType();
            // If triples or quads, maybe POST.

            InputStream input = action.getRequest().getInputStream();
            DatasetGraph dsg = action.getDataset();

            RDFPatchReaderText pr = new RDFPatchReaderText(input);
            RDFChanges changes = new RDFChangesApply(dsg);
            // External transaction. Suppress patch recorded TX and TC.
            if ( withPatchTxn == WithPatchTxn.EXTERNAL_TXN )
                changes = new RDFChangesExternalTxn(changes);

            RDFChangesCounter counter = new RDFChangesCounter();
            RDFChanges dest = RDFChangesN.multi(changes, counter);
            pr.apply(dest);

            PatchSummary summary = counter.summary();
            if ( summary.countAddPrefix > 0 || summary.countDeletePrefix > 0 ) {
                action.log.info(format("[%d] RDF Patch: A=%d, D=%d, PA=%d, PD=%d", action.id,
                                       summary.countAddData, summary.countDeleteData,
                                       summary.countAddPrefix, summary.countDeletePrefix));
            } else {
                action.log.info(format("[%d] RDF Patch: A=%d, D=%d", action.id,
                                       summary.countAddData, summary.countDeleteData));
            }
            ServletOps.success(action);
        }
        catch (PatchTxnAbortException ex) {
            // Let this propagate to the caller. TA encountered.
            throw ex;
        }
        catch (PatchException ex) { throw ex; }
        catch (RiotException ex) {
            ServletOps.errorBadRequest("RDF Patch parse error: "+ex.getMessage());
        }
        catch (IOException ex) {
            ServletOps.errorBadRequest("IOException: "+ex.getMessage());
        }
    }

    // ---- POST or PATCH or OPTIONS

    @Override
    protected void doPost(HttpAction action) {
        operation(action);
    }

    @Override
    protected void doPatch(HttpAction action) {
        operation(action);
    }

    @Override
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.getResponse().setHeader(HttpNames.hAllow, "OPTIONS,POST,PATCH");
        action.getResponse().setHeader(HttpNames.hContentLength, "0");
    }

    @Override
    protected void doHead(HttpAction action)   { ServletOps.errorMethodNotAllowed("HEAD"); }

    @Override
    protected void doPut(HttpAction action)    { ServletOps.errorMethodNotAllowed("PUT"); }

    @Override
    protected void doDelete(HttpAction action) { ServletOps.errorMethodNotAllowed("DELETE"); }

    @Override
    protected void doGet(HttpAction action)    { ServletOps.errorMethodNotAllowed("GET"); }
}
