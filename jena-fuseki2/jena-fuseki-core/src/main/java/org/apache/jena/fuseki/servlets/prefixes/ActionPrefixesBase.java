/*
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

package org.apache.jena.fuseki.servlets.prefixes;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.servlets.ActionREST;
import org.apache.jena.fuseki.servlets.BaseActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;

import java.util.Iterator;

/**
 * Base of {@link ActionREST} that provides
 * to an {@link HttpAction}.
 */
public abstract class ActionPrefixesBase extends BaseActionREST {

    /** Helper function that returns a {@link PrefixesAccess} for the dataset of this {@link HttpAction}. */
    protected static PrefixesAccess prefixesFromAction(HttpAction action) {
        DatasetGraph dsg = action.getDataset();
        PrefixesMap pMap = new PrefixesMap(dsg.prefixes(), dsg);
        return pMap;
    }

    /** Return an {@link PrefixesAccess} for the dataset of this {@link HttpAction}. */
    protected abstract PrefixesAccess prefixes(HttpAction action);

    @Override
    public final void validate(HttpAction action) {
        // General validation
        Iterator<String> paramNames = action.getRequestParameterNames().asIterator();
        while(paramNames.hasNext()) {
            String check = paramNames.next();
            if ( ! PrefixUtils.isPrefixesParam(check) ) {
                FmtLog.warn(action.log, "[%d] Parameter not recognized: %s", action.id, check);
                ServletOps.errorBadRequest("Parameter not recognized: "+check);
                return;
            }
        }

        String prefix = action.getRequestParameter(PrefixUtils.PREFIX);
        String uri = action.getRequestParameter(PrefixUtils.URI);

        if ( prefix != null ) {
            if ( prefix.isEmpty() ) {
                FmtLog.warn(action.log, "[%d] Empty prefix - not supported", action.id);
                ServletOps.errorBadRequest("Bad prefix parameter value");
                return;
            }
            if ( ! PrefixUtils.prefixIsValid(prefix) ) {
                FmtLog.warn(action.log, "[%d] Bad value for prefix: '%s'", action.id, prefix);
                ServletOps.errorBadRequest("Bad prefix parameter value");
                return;
            }
        }

        if ( uri != null && ! PrefixUtils.uriIsValid(uri) ) {
            FmtLog.warn(action.log, "[%d] Bad value for uri: '%s'", action.id, uri);
            ServletOps.errorBadRequest("Bad uri parameter value");
            return;
        }

        // Per HTTP operation validation.
        switch ( action.getRequestMethod() ) {
            case HttpNames.METHOD_GET -> validatePrefixesGET(action);
            case HttpNames.METHOD_POST -> validatePrefixesPOST(action);
            case HttpNames.METHOD_DELETE -> validatePrefixesDELETE(action);
        }
    }

    // Default actions
    protected void validatePrefixesGET(HttpAction action) {
        ServletOps.errorMethodNotAllowed(action.getRequestMethod());
    }

    protected void validatePrefixesPOST(HttpAction action)  {
        ServletOps.errorMethodNotAllowed(action.getRequestMethod());
    }

    protected void validatePrefixesDELETE(HttpAction action)  {
        ServletOps.errorMethodNotAllowed(action.getRequestMethod());
    }
}
