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

package org.apache.jena.memvalue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * Value related "sameness" for the java-GraphMemValue
 * in support of the storage in ArrayBunch and HashedTruipleBunch.
 */
final class ByValue {
    /**
     * Test two triples, for same-by-value.
     * <p>
     * URIs and blank nodes compare by same term.
     * <p>
     * Two literals are be "same by value" if they represent the same value
     * as mapped by the Jena Model API. This is not (quite) the same as XSD.
     * <p>
     * Triple terms equate by recursive applications.
     * <p>
     * The triples should be concrete (Node.ANY is not a wildcard).
     */

    /*package*/ static boolean sameByValue(Triple t1, Triple t2) {
        return sameByValue(t1.getSubject(),   t2.getSubject()) &&
               sameByValue(t1.getPredicate(), t2.getPredicate()) &&
               sameByValue(t1.getObject(),    t2.getObject());
    }

    private static boolean sameByValue(Node n1, Node n2) {
        if ( n1.isTripleTerm() ) {
            if ( n2.isTripleTerm() ) {
                // Apply same value recursively.
                Triple n1Triple = n1.getTriple();
                Triple n2Triple = n2.getTriple();
                return sameByValue(n1Triple,  n2Triple);
            }
        }
        return n1.sameValueAs(n2);
    }
}
