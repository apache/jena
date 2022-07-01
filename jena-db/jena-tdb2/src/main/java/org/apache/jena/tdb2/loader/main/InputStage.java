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

package org.apache.jena.tdb2.loader.main;

/**
 * The first phase, parsing to at least one index each of triples and quads
 * can be done in several ways.
 * <ul>
 * <li> {@code MULTI} - one thread parsing (caller), one for nodetable/tuples, and one for each primary index
 * <li> {@code PARSE_NODE} - one thread parsing (caller) and the nodetable/tuples, and one for each primary index
 * <li> {@code PARSE_NODE_INDEX} - use the caller thread for all operations
 * </ul>
 * {@code MULTI} is fastest when hardware allows.
 * <br/>
 * When data is triples or quads, not a mixture, {@code PARSE_NODE} uses two threads.
 * <br/>
 * {@code PARSE_NODE_INDEX} uses only the caller thread for all steps.
 */
enum InputStage {
    MULTI ,
    PARSE_NODE ,
    PARSE_NODE_INDEX
}
