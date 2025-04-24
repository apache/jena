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

import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;

/**
 * A wrapper around another {@link StreamRDF}
 *
 */
public class StreamRDFWrapper implements StreamRDF
{
    protected final StreamRDF other ;
    public final StreamRDF get() { return other; }

    public StreamRDFWrapper(StreamRDF other) { this.other = other ; }

    @Override
    public void start()
    { other.start() ; }

    @Override
    public void triple(Triple triple)
    { other.triple(triple) ; }

    @Override
    public void quad(Quad quad)
    { other.quad(quad) ; }

    @Override
    public void base(String base)
    { other.base(base) ; }

    @Override
    public void prefix(String prefix, String iri)
    { other.prefix(prefix, iri) ; }

    @Override
    public void version(String versionString)
    { other.version(versionString); }

    @Override
    public void finish()
    { other.finish() ; }
}
