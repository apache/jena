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

package org.apache.jena.rdfpatch;

import org.apache.jena.graph.Node;

public interface RDFPatch {
    // Long name - not preferred.
    static final String PREVIOUS        = "previous";

    public PatchHeader header();

    public default Node getHeader(String field) {
        return header().get(field) ;
    }

    public default Node getId() {
        return header().get(RDFPatchConst.ID);
    }

    public default Node getPrevious() {
        Node n = header().get(RDFPatchConst.PREV);
        if ( n == null )
            n = header().get(PREVIOUS);
        return n;
    }

    /** Act on the patch by sending it to a changes processor. */
    public void apply(RDFChanges changes);

    public boolean repeatable();
}
