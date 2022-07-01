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

import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;

/** {@link StreamRDF} that throws an exception on each operation except {@code start()} and {@code finish()}.
 *  @see StreamRDFBase
 */
public class StreamRDFReject implements StreamRDF
{
    @Override
    public void start() {}

    @Override
    public void finish() {}

    @Override
    public void triple(Triple triple)
    { throw new UnsupportedOperationException("StreamRDF.triple"); }

    @Override
    public void quad(Quad quad)
    { throw new UnsupportedOperationException("StreamRDF.quad"); }

    @Override
    public void base(String base)
    { throw new UnsupportedOperationException("StreamRDF.base"); }

    @Override
    public void prefix(String prefix, String iri)
    { throw new UnsupportedOperationException("StreamRDF.prefix"); }
}
