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

package com.hp.hpl.jena.reasoner;

import java.io.PrintWriter;

/**
 * Derivation records are used to determine how an inferred triple
 * was derived from a set of source triples and a reasoner. SubClasses
 * provide more specific information.
 * 
 * <p>A future option might be to generate an RDF description of
 * the derivation trace. </p>
 */
public interface Derivation {

    /**
     * Return a short-form description of this derivation.
     */
    @Override
    public String toString();
    
    /**
     * Print a deep traceback of this derivation back to axioms and 
     * source assertions.
     * @param out the stream to print the trace out to
     * @param bindings set to true to print intermediate variable bindings for
     * each stage in the derivation
     */
    public void printTrace(PrintWriter out, boolean bindings);
}
