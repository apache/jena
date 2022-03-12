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

package org.apache.jena.riot.rowset.rw;

import java.io.InputStream;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.DataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetReader;
import org.apache.jena.riot.rowset.RowSetReaderFactory;
import org.apache.jena.riot.rowset.rw.RowSetJSONStreaming.ValidationSettings;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.Severity;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSetBuffered;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowSetReaderJSONStreaming
    implements RowSetReader
{
    private static final Logger log = LoggerFactory.getLogger(RowSetReaderJSONStreaming.class);

    /** Search for the head key eagerly on row set creation. This may immediately
     * consume (and buffer) the whole stream if the head is located at the end.
     * default: false */
    public static final Symbol rsJsonSearchHeadEagerly = SystemARQ.allocSymbol("rsJsonSearchHeadEagerly");

    /* Settings affecting the configuration of the {@link RowSetJSONStreaming} instance.
     * See {@link ValidationSettings} for default values. */

    public static final Symbol rsJsonSeverityEmptyJson = SystemARQ.allocSymbol("rsJsonSeverityEmptyJson");
    public static final Symbol rsJsonSeverityMissingHead = SystemARQ.allocSymbol("rsJsonSeverityMissingHead");
    public static final Symbol rsJsonSeverityInvalidatedResults = SystemARQ.allocSymbol("rsJsonSeverityInvalidatedResults");
    public static final Symbol rsJsonSeverityInvalidatedHead = SystemARQ.allocSymbol("rsJsonSeverityInvalidatedHead");
    public static final Symbol rsJsonSeverityMixedResults = SystemARQ.allocSymbol("rsJsonSeverityMixedResults");
    public static final Symbol rsJsonSeverityUnexpectedJsonElement = SystemARQ.allocSymbol("rsJsonSeverityUnexpectedJsonElement");

    public static final RowSetReaderFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_JSON ) )
            throw new ResultSetException("RowSet for JSON asked for a "+ lang);
        return new RowSetReaderJSONStreaming();
    };

    @Override
    public QueryExecResult readAny(InputStream in, Context context) {
        return process(in, context);
    }

    public static QueryExecResult process(InputStream in, Context context) {
        Context cxt = context == null ? ARQ.getContext() : context;

        QueryExecResult result = null;
        RowSetBuffered<RowSetJSONStreaming> rs = createRowSet(in, cxt);

        Boolean searchHeaderEagerly = cxt.get(rsJsonSearchHeadEagerly, false);
        if (Boolean.TRUE.equals(searchHeaderEagerly)) {
            // This triggers searching for the first header
            rs.getResultVars();
        }

        // If there are no bindings we check for an ask result
        if (!rs.hasNext()) {
            // Unwrapping in order to access the ask result
            RowSetJSONStreaming inner = rs.getDelegate();
            Boolean askResult = inner.getAskResult();

            if (askResult != null) {
                result = new QueryExecResult(askResult);
            }
        }

        if (result == null) {
            result = new QueryExecResult(rs);
        }

        return result;
    }

    public static RowSetBuffered<RowSetJSONStreaming> createRowSet(InputStream in, Context context) {
        Context cxt = context == null ? ARQ.getContext() : context;

        boolean inputGraphBNodeLabels = cxt.isTrue(ARQ.inputGraphBNodeLabels);
        LabelToNode labelMap = inputGraphBNodeLabels
            ? SyntaxLabels.createLabelToNodeAsGiven()
            : SyntaxLabels.createLabelToNode();

        Supplier<DataBag<Binding>> bufferFactory = () -> {
            ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(cxt);
            DataBag<Binding> r = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory());
            return r;
        };

        ValidationSettings validationSettings = configureValidationFromContext(new ValidationSettings(), cxt);

        // Log warnings but otherwise raise exceptions without logging
        return RowSetJSONStreaming.createBuffered(in, labelMap, bufferFactory, validationSettings,
                ErrorHandlerFactory.errorHandlerWarnOrExceptions(log));
    }

    /** Apply settings present in the context to a given ValidationSettings instance */
    public static ValidationSettings configureValidationFromContext(ValidationSettings settings, Context cxt) {
        Severity severity;
        if ((severity = cxt.get(rsJsonSeverityEmptyJson)) != null) {
            settings.setEmptyJsonSeverity(severity);
        }

        if ((severity = cxt.get(rsJsonSeverityMissingHead)) != null) {
            settings.setMissingHeadSeverity(severity);
        }

        if ((severity = cxt.get(rsJsonSeverityInvalidatedResults)) != null) {
            settings.setInvalidatedResultsSeverity(severity);
        }

        if ((severity = cxt.get(rsJsonSeverityInvalidatedHead)) != null) {
            settings.setInvalidatedHeadSeverity(severity);
        }

        if ((severity = cxt.get(rsJsonSeverityMixedResults)) != null) {
            settings.setMixedResultsSeverity(severity);
        }

        if ((severity = cxt.get(rsJsonSeverityUnexpectedJsonElement)) != null) {
            settings.setUnexpectedJsonElementSeverity(severity);
        }

        return settings;
    }
}
