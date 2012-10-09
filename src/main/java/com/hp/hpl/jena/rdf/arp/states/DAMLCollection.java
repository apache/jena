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

package com.hp.hpl.jena.rdf.arp.states;

import com.hp.hpl.jena.rdf.arp.impl.ANode;
import com.hp.hpl.jena.rdf.arp.impl.AbsXMLContext;

/**
 * @deprecated This DAML class will be removed from future versions of Jena because it is obsolete.
 */
public class DAMLCollection extends Collection {

    public DAMLCollection(WantsObjectFrameI s, AbsXMLContext x) {
        super(s, x);
    }

    @Override
    void restTriple(ANode subj, ANode obj) {
        triple(subj,DAML_REST,obj);
    }

    @Override
    void firstTriple(ANode subj, ANode obj) {
        triple(subj,DAML_FIRST,obj);
        triple(subj,RDF_TYPE,DAML_LIST);
    }

    @Override
    ANode nil() {
        return DAML_NIL;
    }

}
