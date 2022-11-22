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

package org.apache.jena.rdfpatch.system;

import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.sparql.core.DatasetGraph;

// UNFINISHED

// ?? Prefixes.
public class DatasetGraphRealChanges extends AbstractDatasetGraphAddDelete {
    /** With checking, the {@link RDFChanges} becomes reversible */
    protected final boolean checking = true ;

    public DatasetGraphRealChanges(DatasetGraph dsg) {
        super(dsg) ;
    }

    @Override
    protected void actionAdd(Node g, Node s, Node p, Node o) {
        if ( checking && get().contains(g, s, p, o) )
            return;
        get().add(g, s, p, o);
    }

    @Override
    protected void actionDelete(Node g, Node s, Node p, Node o) {
        if ( checking && ! get().contains(g, s, p, o) )
            return;
        get().delete(g, s, p, o);
    }
}
