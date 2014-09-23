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

package org.apache.jena.riot.writer;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.StreamOps ;
import org.apache.jena.riot.system.StreamRDF ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** TriG writer that writes quads one per line
 *  in TriG form with prefixes and short form literals (e.g. integers) 
 */

public class TriGWriterFlat extends TriGWriterBase
{
    @Override
    protected void output(IndentedWriter iOut, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI) {
        StreamRDF dest = new WriterStreamRDFFlat(iOut) ;
        dest.start() ;
        dest.base(baseURI) ;
        StreamOps.sendDatasetToStream(datasetGraph, dest, prefixMap) ;
        dest.finish() ;
    }
}
