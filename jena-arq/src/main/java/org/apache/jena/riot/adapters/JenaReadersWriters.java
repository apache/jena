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

package org.apache.jena.riot.adapters;

public class JenaReadersWriters
{

    // Yukky hack to integrate into current jena-core where the structure of model.read assumes
    // the language is determined before the reading process starts.
    // Temporary - eventually, replace all model.read calls with the correct call to RIOT
    // and then the common RDFReaderRIOT can be used.
    public static class RDFReaderRIOT_RDFXML extends RDFReaderRIOT   { public RDFReaderRIOT_RDFXML() { super("RDF/XML") ; } }
    public static class RDFReaderRIOT_TTL extends RDFReaderRIOT      { public RDFReaderRIOT_TTL() { super("TTL") ; } }
    public static class RDFReaderRIOT_NT extends RDFReaderRIOT       { public RDFReaderRIOT_NT() { super("N-TRIPLE") ; } }
    public static class RDFReaderRIOT_RDFJSON extends RDFReaderRIOT  { public RDFReaderRIOT_RDFJSON() { super("RDF/JSON") ; } }

}

