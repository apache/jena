/**
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

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** The interface for the output of RDF, such as the RIOT parsers.
 *  The parser event model is that items are emitted for signficant events.
 *  The events are start/finish, emitting triples/quads/tuples as necessary, prefixes and base directives.
 *  Tuples are generalized triples or quads.  A triple language will call triple(),
 *  quad language quad() in preference.    
 */
public interface StreamRDF
{
    /** Start parsing */
    public void start() ;
    
    /** Triple emitted */
    public void triple(Triple triple) ;

    /** Quad emitted */
    public void quad(Quad quad) ;

//    /** Generalized emitted */
//    public void tuple(Tuple<Node> tuple) ;
//
    /** base declaration seen */
    public void base(String base) ;

    /** prefix declaration seen */
    public void prefix(String prefix, String iri) ;

    /** Finish parsing */
    public void finish() ;

}
