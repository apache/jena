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

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format;
import static org.apache.jena.fuseki.servlets.GraphTarget.determineTarget;

import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.web.HttpSC;

/**
 * SHACL validation service. Receives a shapes file and validates a graph named in the
 * {@code ?graph=} parameter.
 * <p>
 * {@code ?graph=} can be any graph name, or one of the words "default" or "union" (without quotes)
 * to indicate the default graph, which is also the default and the dataset union graph.
 * <p>
 * Optional parameter {@code ?target=} specifies the target node for the validation report.
 */
public class SHACL_Validation extends BaseActionREST { //ActionREST {

    public SHACL_Validation() {}

    @Override
    protected void doPost(HttpAction action) {
        // Response syntax
        MediaType mediaType = ActionLib.contentNegotation(action, DEF.rdfOffer, DEF.acceptTurtle);
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentTypeStr());
        if ( lang == null )
            lang = RDFLanguages.TTL;

        String targetNodeStr = action.getRequest().getParameter(HttpNames.paramTarget);

        action.beginRead();
        try {
            GraphTarget graphTarget = determineTarget(action.getActiveDSG(), action);
            if ( ! graphTarget.exists() )
                ServletOps.errorNotFound("No data graph: "+graphTarget.label());
            Graph data = graphTarget.graph();
            Graph shapesGraph = ActionLib.readFromRequest(action, Lang.TTL);

            Node targetNode = null;
            if ( targetNodeStr != null ) {
                String x = data.getPrefixMapping().expandPrefix(targetNodeStr);
                targetNode = NodeFactory.createURI(x);
            }

            Shapes shapes = Shapes.parse(shapesGraph);
            ValidationReport report = ( targetNode == null )
                ? ShaclValidator.get().validate(shapesGraph, data)
                : ShaclValidator.get().validate(shapesGraph, data, targetNode);

            if ( report.conforms() )
                action.log.info(format("[%d] shacl: conforms", action.id));
            else
                action.log.info(format("[%d] shacl: %d validation errors", action.id, report.getEntries().size()));
            report.getEntries().size();
            action.response.setStatus(HttpSC.OK_200);
            ActionLib.graphResponse(action, report.getGraph(), lang);
        } finally {
            action.endRead();
        }
    }
}
