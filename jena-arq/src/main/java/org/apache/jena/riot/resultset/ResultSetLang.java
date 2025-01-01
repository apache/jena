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

package org.apache.jena.riot.resultset;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.rowset.RowSetReaderRegistry;
import org.apache.jena.riot.rowset.RowSetWriterRegistry;

/** {@link Lang} related to SPARQL result sets. */
public class ResultSetLang {

    private ResultSetLang() {}

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

    /** System initialization function */
    public static void init() {
        if ( initialized )
            return;
        initialized = true;
        registerResultSetLang(RS_XML);
        registerResultSetLang(RS_JSON);
        registerResultSetLang(RS_CSV);
        registerResultSetLang(RS_TSV);
        registerResultSetLang(RS_Thrift);
        registerResultSetLang(RS_Protobuf);
        // Not output-only text.
        registerResultSetLang(RS_None);

        RowSetReaderRegistry.init();
        RowSetWriterRegistry.init();

        ResultSetReaderRegistry.init();
        ResultSetWriterRegistry.init();
        registered = Set.copyOf(registered);
    }

    /**
     * Is a lang a registered {@link ResultSetLang}?
     * @param lang
     */
    public static boolean isRegistered(Lang lang) {
        return registered.contains(lang);
    }

    private static Set<Lang> registered = new HashSet<>();
    private static void registerResultSetLang(Lang rsLang)  {
        registered.add(rsLang);
        RDFLanguages.register(rsLang);
    }
}
