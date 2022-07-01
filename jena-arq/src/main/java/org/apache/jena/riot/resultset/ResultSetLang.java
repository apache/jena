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

package org.apache.jena.riot.resultset;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.rowset.RowSetReaderRegistry;
import org.apache.jena.riot.rowset.RowSetWriterRegistry;

public class ResultSetLang {

    /** SPARQL results in XML syntax */
    public static final Lang RS_XML = LangBuilder.create("SPARQL-Results-XML", WebContent.contentTypeResultsXML)
                     .addAltNames("SRX")
                     .addFileExtensions("srx")
                     .build();

    public static final Lang RS_JSON = LangBuilder.create("SPARQL-Results-JSON", WebContent.contentTypeResultsJSON)
                     .addAltNames("SRJ")
                     .addFileExtensions("srj")
                     .build();

    public static final Lang RS_CSV = Lang.CSV;

    public static final Lang RS_TSV = Lang.TSV;

    public static final Lang RS_Thrift = LangBuilder.create("SPARQL-Results-Thrift", WebContent.contentTypeResultsThrift)
                     .addAltNames("SRT")
                     .addFileExtensions("srt")
                     .build();

    public static final Lang RS_Protobuf = LangBuilder.create("SPARQL-Results-Protobuf", WebContent.contentTypeResultsProtobuf)
                     .addAltNames("SRP")
                     .addFileExtensions("srp")
                     .build();

    public static final Lang RS_Text = LangBuilder.create("SPARQL-Results-Text", WebContent.contentTypeTextPlain)
                     .addFileExtensions("txt")
                     .build();

    public static final Lang RS_None = LangBuilder.create("SPARQL-Results-None", "application/sparql-results+none").build();

    private static boolean initialized = false;
    public static void init() {
        if ( initialized )
            return;
        initialized = true;
        RDFLanguages.register(RS_XML);
        RDFLanguages.register(RS_JSON);
        RDFLanguages.register(RS_CSV);
        RDFLanguages.register(RS_TSV);
        RDFLanguages.register(RS_Thrift);
        // Not output-only text.
        RDFLanguages.register(RS_None);

        RowSetReaderRegistry.init();
        RowSetWriterRegistry.init();

        ResultSetReaderRegistry.init();
        ResultSetWriterRegistry.init();
    }
}
