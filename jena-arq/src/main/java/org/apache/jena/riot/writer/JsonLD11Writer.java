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

package org.apache.jena.riot.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.api.CompactionApi;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.document.RdfDocument;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.processor.FromRdfProcessor;
import com.apicatalog.rdf.RdfDataset;

import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WriterDatasetRIOT;
import org.apache.jena.riot.system.JenaTitanium;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

public class JsonLD11Writer implements WriterDatasetRIOT {

    // It does not matter what the non-null value of PRETTY_PRINTING is (it could be "false"!).
    // It is the keys presence that matters.
    private static Map<String, ?> configPretty = Map.of(JsonGenerator.PRETTY_PRINTING, true);
    private static Map<String, ?> configFlat = Map.of();

    private final RDFFormat format;

    public JsonLD11Writer(RDFFormat format) {
        this.format = format;
    }

    private Map<String, ?> config() { return format==RDFFormat.JSONLD11_PLAIN ? configPretty : configFlat; }

    @Override
    public void write(OutputStream outputStream, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context) {
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(datasetGraph);
        write$(outputStream, null, datasetGraph);
    }

    @Override
    public void write(Writer out, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context) {
        Objects.requireNonNull(out);
        Objects.requireNonNull(datasetGraph);
        write$(null, out, datasetGraph);
    }

    @Override
    public Lang getLang() {
        return Lang.JSONLD;
    }

    private void write$(OutputStream output, Writer writer, DatasetGraph dsg) {
        boolean applyPretty = true;
        try {
            // Context, including prefixes.
            RdfDataset ds = JenaTitanium.convert(dsg);
            Document doc = RdfDocument.of(ds);

            JsonStructure writeThis = applyPretty ? writePretty(doc, dsg) : writePlain(doc);

            JsonWriter jsonWriter = startWrite(output, writer);
            jsonWriter.write(writeThis);
            finishWrite(output, writer);

        } catch (Throwable e) {
            e.printStackTrace();
            return ;
        }
    }

    private JsonStructure writePlain(Document doc) throws JsonLdError {
        return JsonLd.fromRdf(doc).get();
    }

    private JsonStructure writePretty(Document doc, DatasetGraph dsg) throws JsonLdError {
        JsonLdOptions options = new JsonLdOptions();

        // Native types.
        options.setUseNativeTypes(true);
        JsonArray array = FromRdfProcessor.fromRdf(doc, options);

        // Build context
        JsonObjectBuilder cxt = Json.createObjectBuilder().add(Keywords.VERSION, "1.1");
        dsg.prefixes().forEach((k, v) -> {
            if ( ! k.isEmpty() )
                cxt.add(k, v);
        });
        JsonObject context = cxt.build();

        // Object to write.
        JsonObject writeRdf = Json.createObjectBuilder()
                .add(Keywords.CONTEXT, context)
                .add(Keywords.GRAPH, array)
                .build();
        Document contextDoc = JsonDocument.of(context);

        // Setup compaction.
        CompactionApi api =JsonLd.compact(JsonDocument.of(writeRdf), contextDoc);
        api.rdfStar();
//                // Non-absolute URIs.
//                if ( dsg.prefixes().containsPrefix("") )
//                    api.base(dsg.prefixes().get(""));
        // Object to output.
        return api.get();
    }

    private JsonWriter startWrite(OutputStream output, Writer writer) {
        Map<String,?> config = config();
        JsonWriterFactory factory = Json.createWriterFactory(config);
        return (output != null ) ? factory.createWriter(output) : factory.createWriter(writer);
    }

    private void finishWrite(OutputStream output, Writer writer) throws IOException {
        boolean outputNL = (format != RDFFormat.JSONLD11_FLAT) ;
        if ( output != null ) {
            if (outputNL) output.write('\n');
            output.flush();
        } else {
            if (outputNL) writer.write("\n");
            writer.flush();
        }
    }
}
