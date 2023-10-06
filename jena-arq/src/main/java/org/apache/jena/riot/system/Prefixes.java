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

package org.apache.jena.riot.system;

import static java.lang.String.format;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;


/**
 * Functions related to {@link PrefixMap}.
 *
 * @see PrefixMapFactory
 */
public class Prefixes {
    // Distinguished nodes:
    // Default graph : Quad.defaultGraphNodeGenerated would have been preferred.
    // For compatibility reasons, in TDB2, this is the URI <> (empty string).

    // **** Implementations
    // ** DatasetGraphMap, DatasetGraphMapLink : adapter to the default graph
    //
    // ** DatasetGraphOne : adapt the graph
    //
    // ** TIM
    //    Prefixes are thread safe, MRSW but not transactional. (Uses PrefixMapStd).
    //
    // ** TDB1
    //    DatasetGraphTDB
    //    -> DatasetPrefixesTDB
    //    -> GraphPrefixesProjection which is a prefixMap
    //
    // ** TDB2
    //    DatasetGraphTDB
    //    -> StoragePrefixes
    //    -> StoragePrefixesView(projection) implements StoragePrefixMap
    //    -> PrefixMapOverStorage
    //
    // ** DatasetGraphNull > DatasetGraphSink/DatasetGraphZero
    //    PrefixMapNull > PrefixMapSink/PrefixMapZero

    /**
     * Special name for default graph prefixes.
     * This is different to the datasetPrefixSet.
     * Only used by (TDB1) GraphTxnTDB_Prefixes and (TDB2) GraphViewSwitchable_Prefixes
     * which have separated dataset/default graph prefixes.
     *
     * For legacy migration reasons, TDB1 uses "" and TDB2 uses
     * defaultGraphNodeGenerated for the dataset prefix set.
     * See {@code}
     * TDB1 )
     * TDB2 {@code StoragePrefixesView}
     */
    public static final String  dftGraphPrefixSet = ARQ.arqParamNS+"dftGraphPrefixSet";

    /** Name for dataset-wide prefixes. */
    public static final String  datasetPrefixSet  = "";

    // As Nodes.
    /** Name assigned to the default graph. */
    public static Node          nodeDefaultGraph  = NodeFactory.createURI(dftGraphPrefixSet);

    /** Name for dataset prefixes. */
    public static Node          nodeDataset       = NodeFactory.createURI(datasetPrefixSet);

    private static final String dftUri1           = Quad.defaultGraphIRI.getURI();
    private static final String dftUri2           = Quad.defaultGraphNodeGenerated.getURI();

    // Unused : datasets have prefix sets and all graphs for the dataset use that prefix set.
//    /** Is this a name for the default graph prefix set? */
//    public static boolean isDftGraph(String graphName) {
//        return graphName == null
//               //|| graphName.equals(dftGraphPrefixSet)
//               || graphName.equals(dftUri1) || graphName.equals(dftUri2);
//    }
//
//    /** Is this a name node for the default graph prefix set? */
//    public static boolean isDftGraph(Node graphName) {
//        if ( !graphName.isURI() )
//            return false;
//        return graphName == null
//            //|| graphName.equals(nodeDefaultGraph)
//            || Quad.isDefaultGraph(graphName);
//    }

    /**
     * Canonical prefix - remove a trailing ":". The return is not null.
     */
    public static String prefix(String prefix) {
        if ( prefix == null )
            // null is not good style but let's be robust.
            return "";
        if ( prefix.endsWith(":") )
            return prefix.substring(prefix.length() - 1);
        return prefix;
    }

    /**
     * Reverse lookup of URI to a prefix. General implementation by scanning the
     * {@link PrefixMap}. Returns a prefix if found or null. If several prefixes for
     * the same URI, returns one at random.
     */
    public static String findByURI(PrefixMap pmap, String uriStr) {
        return pmap.getMapping().entrySet().stream()
            .filter(e -> Objects.equals(uriStr, e.getValue().toString()))
            .map(Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    /**
     * Provide, via an adapter if necessary, the {@link PrefixMap} for a {@link Graph}.
     */
    public static PrefixMap adapt(Graph graph) {
        Objects.requireNonNull(graph);
        return adapt(graph.getPrefixMapping());
    }

    /**
     * Apply a wrapper to a {@link PrefixMap} to provide the
     * {@link PrefixMapping} API.
     */
    public static PrefixMapping adapt(PrefixMap prefixMap) {
        Objects.requireNonNull(prefixMap);
        if ( prefixMap instanceof PrefixMapAdapter prefixMapAdapter)
            // Already adapter from a PrefixMapping
            return prefixMapAdapter.getPrefixMapping();
        return new PrefixMappingAdapter(prefixMap);
    }

    /**
     * Apply a wrapper to a {@link PrefixMapping} to provide the
     * {@link PrefixMap} API.
     */
    public static PrefixMap adapt(PrefixMapping prefixMapping) {
        Objects.requireNonNull(prefixMapping);
        if ( prefixMapping instanceof PrefixMappingAdapter )
            // Already adapter from a PrefixMap
            return ((PrefixMappingAdapter)prefixMapping).getPrefixMap();
        return new PrefixMapAdapter(prefixMapping);
    }

    /** Calculate a printable multi-line string. */
    public static String toString(PrefixMap prefixMap) {
        if ( prefixMap.isEmpty() )
            return "{}";
        StringJoiner sj = new StringJoiner("\n", "{\n", "\n}");
        prefixMap.getMapping().forEach((p,u)->sj.add(format("  %-8s <%s>", p+":", u)));
        return sj.toString();
    }
}
