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

package org.apache.jena.dboe.storage.prefixes;

import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.AbstractTestPrefixMappingView;

/** AbstractTestPrefixMappingView for named graph. */
public class TestPrefixMappingOverDatasetPrefixes2 extends AbstractTestPrefixMappingView
{
    static Node gn = NodeFactory.createURI("http://test/graphName");
    StoragePrefixes dsgprefixes;

    @Override
    protected PrefixMapping create() {
        dsgprefixes = PrefixesDboeFactory.newDatasetPrefixesMem();
        return view();
    }

    @Override
    protected PrefixMapping view() {
        StoragePrefixMap view = StoragePrefixesView.viewGraph(dsgprefixes, gn);
        PrefixMap pmap = PrefixesDboeFactory.newPrefixMap(view);
        return Prefixes.adapt(pmap);
    }
}

