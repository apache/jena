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

package org.apache.jena.rdflink;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.http.HttpLib;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/** package-wide utilities etc */
class LibRDFLink {
    private static String dftName =  "default" ;

    /*package*/ static boolean isDefault(Node name) {
        return name == null || Quad.isDefaultGraph(name);
    }

    private static String queryStringForGraph(String ch, Node graphName) {
        return
            ch +
                (LibRDFLink.isDefault(graphName)
                ? "default"
                : "graph="+encode(graphName));
    }

    private static String encode(Node node) {
        return HttpLib.urlEncodeQueryString(node.getURI());
    }

    /*package*/ static String urlForGraph(String graphStoreProtocolService, Node graphName) {
        // If query string
        String ch = "?";
        if ( graphStoreProtocolService.contains("?") )
            // Already has a query string, append with "&"
            ch = "&";
        return graphStoreProtocolService + queryStringForGraph(ch, graphName);
    }

    /*package*/ static Model graph2model(Graph graph) {
        return ModelFactory.createModelForGraph(graph);
    }

    /*package*/ static Graph model2graph(Model model) {
        return model.getGraph();
    }

    /*package*/ static Dataset asDataset(DatasetGraph dsg) {
        return DatasetFactory.wrap(dsg);
    }

    /*package*/ static DatasetGraph asDatasetGraph(Dataset dataset) {
        return dataset.asDatasetGraph();
    }

    /*package*/ static Node name(String graphName) {
        if ( graphName == null || graphName.equals("default") )
            return Quad.defaultGraphIRI;
        return NodeFactory.createURI(graphName);
    }

    /*package*/ static String formServiceURL(String destination, String srvEndpoint) {
        if ( srvEndpoint == null )
            return null;
        if ( srvEndpoint == RDFLinkHTTPBuilder.SameAsDestination )
            return destination;
        if ( destination == null )
            return srvEndpoint;

        // If the srvEndpoint looks like an absolute URL, use as given.
        if ( srvEndpoint.startsWith("http:/") || srvEndpoint.startsWith("https:/") )
            return srvEndpoint;
        String queryString = null;
        String dest = destination;
        if ( destination.contains("?") ) {
            // query string : remove and append later.
            int i = destination.indexOf('?');
            queryString = destination.substring(i);
            dest = destination.substring(0, i);
        }
        if ( dest.endsWith("/") )
            dest = dest.substring(0, dest.length()-1);
        dest = dest+"/"+srvEndpoint;
        if ( queryString != null )
            dest = dest+queryString;
        return dest;
    }
}
