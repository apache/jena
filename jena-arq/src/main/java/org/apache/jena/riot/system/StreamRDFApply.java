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

import java.util.function.Consumer;

import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;

/**
 * Apply a function to every triple and quad.
 */
public class StreamRDFApply extends StreamRDFReject {

    private final Consumer<Triple> tripleAction;
    private final Consumer<Quad> quadAction;

    public StreamRDFApply(Consumer<Triple> tripleAction, Consumer<Quad> quadAction) {
        this.tripleAction = tripleAction == null ? x->{} : tripleAction;
        this.quadAction = quadAction == null ? x->{} : quadAction;
    }

    @Override
    public void triple(Triple triple) {
        tripleAction.accept(triple);
    }

    @Override
    public void quad(Quad quad) {
        quadAction.accept(quad);
    }
}
