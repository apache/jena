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

package org.apache.jena.riot.lang;

import org.apache.jena.graph.Graph ;
import org.apache.jena.riot.ErrorHandlerTestLib.ErrorHandlerEx ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.graph.GraphFactory ;

/** Helper code for RIOT language parsing tests. */
class ParserTestBaseLib {

    /** Parse for a language - convert errors.wranigns to ErrorHandlerEx */
    static Graph parseGraph(Lang lang, String ...strings) {
        Graph graph = GraphFactory.createDefaultGraph() ;
        StreamRDF dest = StreamRDFLib.graph(graph) ;
        parse(lang, dest, strings) ;
        return graph ;
    }

    /** Parse for a language - convert errors and warning to ErrorHandlerEx */
    static DatasetGraph parseDataset(Lang lang, String ...strings) {
        DatasetGraph dsg = DatasetGraphFactory.create() ;
        StreamRDF dest = StreamRDFLib.dataset(dsg) ;
        parse(lang, dest, strings);
        return dsg ;
    }

    /** Parse strings to destination (checking on, URI resolution off) - convert errors and warning to ErrorHandlerEx */
    static void parse(Lang lang, StreamRDF dest, String... strings) {
        String string = String.join("\n", strings) ;
        String baseIRI = "http://base/" ;
        RDFParser.create()
            .fromString(string)
            .base(baseIRI)
            .errorHandler(new ErrorHandlerEx())
            .lang(lang)
            .parse(dest);
    }

    /** Parse for a language - convert errors.wranigns to ErrorHandlerEx */
    static long parseCount(Lang lang, String ...strings) {
        StreamRDFCounting dest = StreamRDFLib.count() ;
        parse(lang, dest, strings) ;
        return dest.count() ;
    }
}

