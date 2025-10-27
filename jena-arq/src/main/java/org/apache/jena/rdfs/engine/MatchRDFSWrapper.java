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

package org.apache.jena.rdfs.engine;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.rdfs.setup.ConfigRDFS;

/** MatchRDFS implementation as a wrapper over another Match source. */
public class MatchRDFSWrapper<X, T>
    extends MatchRDFS<X, T>
{
    protected Match<X, T> source;

    public MatchRDFSWrapper(ConfigRDFS<X> setup, Match<X, T> source) {
        super(setup, source.getMapper());
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public Stream<T> sourceFind(X s, X p, X o) {
        return source.match(s,p,o);
    }

    @Override
    protected boolean sourceContains(X s, X p, X o) {
        return source.contains(s, p, o);
    }
}
