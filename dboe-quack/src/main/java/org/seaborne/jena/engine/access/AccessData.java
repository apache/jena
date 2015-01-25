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

package org.seaborne.jena.engine.access;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Low access to data where the pattern constrains the results 
 * by constants (fixed terms) or wildcard.  The wildcard may be {@code null} or
 * some token for "any" depending on the generic type 
 * (e.g. {@link Node#ANY}, {@link NodeId#NodeIdAny}).
 * The resulting iterator of tuples may not be the same width
 * as the pattern (i.e. see the implementation for details).
 */
public interface AccessData<X> {
    /** Access by constant/wildcard  pattern to get tuples of data */ 
    public Iterator<Tuple<X>> accessTuples(Tuple<X> pattern) ;
}
