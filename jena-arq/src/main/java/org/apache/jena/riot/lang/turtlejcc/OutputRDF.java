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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.riot.lang.turtlejcc;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/** The StreamRDF interface but extracted so it can be used in jena-core. */

public interface OutputRDF {
    /** Start processing */
    public void start() ;

    /** Triple emitted */
    public void triple(Triple triple) ;

    /** Quad emitted */
    public void quad(Quad quad) ;

    /** base declaration seen */
    public void base(String base) ;

    /** prefix declaration seen */
    public void prefix(String prefix, String iri) ;

    /** version declaration seen */
    public void version(String version) ;

    /** Finish processing */
    public void finish() ;
}
