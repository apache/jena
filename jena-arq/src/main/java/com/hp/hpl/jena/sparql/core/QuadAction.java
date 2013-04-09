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

package com.hp.hpl.jena.sparql.core;

/** A {@code QuadAction} is record of a type of change to a {@code DatasetGraph}.
 * <p>
 * {@code DatasetGraph} are sets of quads.
 * An {@code add} only affects the state of the {@code DatasetGraph}
 * if a quad of the same value was not present, 
 * and a {@code delete} only affects the state of the {@code DatasetGraph}
 * if a quad was present.
 * <p>
 * A {@code QuadAction} can be an {@code ADD} or {@code DELETE}, indicating a change
 * to the {@code DatasetGraph} actually occured (this assumes checking is done - 
 * {@linkplain DatasetChanges} generators may not check - see implementation for details).
 * Otherwise a {@code NO_ADD}, {@code NO_DELETE} {@code QuadAction} is used.
 */ 

public enum QuadAction {
    ADD("A"), DELETE("D"), NO_ADD("#A"), NO_DELETE("#D") ;
    public final String label ;
    QuadAction(String label) { this.label = label ; }

}
