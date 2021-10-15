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

package org.apache.jena.system.progress;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;

/** Send ticks to a {@link ProgressMonitor} as triples and quads
 * are sent along the {@link StreamRDF}.
 */

public class ProgressStreamRDF extends StreamRDFWrapper {

    private final ProgressMonitor monitor;

    public ProgressStreamRDF(StreamRDF other, ProgressMonitor monitor) {
        super(other);
        this.monitor = monitor;
    }

    @Override
    public void triple(Triple triple) {
        super.triple(triple);
        monitor.tick();
    }

    @Override
    public void quad(Quad quad) {
        super.quad(quad);
        monitor.tick();
    }
}
