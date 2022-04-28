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
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFFormatVariant;
import org.apache.jena.riot.WriterDatasetRIOT;
import org.apache.jena.riot.system.JenaTitanium;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

public class JsonLD11Writer implements WriterDatasetRIOT {

    // It does not matter what the non-null value of PRETTY_PRINTING is (it could be "false"!).
    // It is the keys presence that matters.
    private static Map<String, ?> configIndented = Map.of(JsonGenerator.PRETTY_PRINTING, true);
    private static Map<String, ?> configFlat = Map.of();

    private final RDFFormat format;

    public JsonLD11Writer(RDFFormat format) {
        this.format = format;
    }

    private Map<String, ?> config(boolean indented) {
        return indented ? configIndented : configFlat;
    }

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
        return format.getLang();
    }

    private void write$(OutputStream output, Writer writer, DatasetGraph dsg) {
        try {
            // Context, including prefixes.
            RdfDataset ds = JenaTitanium.convert(dsg);
            Document doc = RdfDocument.of(ds);
            RDFFormatVariant variant = format.getVariant();
            // We do not write top-level arrays.
            JsonStructure writeThis;
            boolean indented = true;

            // Choose algorithms
            if ( Lib.equals(variant, RDFFormat.PRETTY) ) {
                writeThis = writePretty(doc, dsg);
            } else if ( variant == null || Lib.equals(variant, RDFFormat.PLAIN) ) {
                writeThis = writePlain(doc, dsg);
            } else if ( Lib.equals(variant, RDFFormat.FLAT) ) {
                writeThis = writePlain(doc, dsg);
                indented = false;
            } else {
                writeThis = writePretty(doc, dsg);
            }

            if ( writeThis == null ) {
                FmtLog.error(this.getClass(), "Nothing to write");
                return;
            }

            if ( ! ( writeThis instanceof JsonObject ) ) {
                FmtLog.warn(this.getClass(), "Output is not a JSON object (%s)", writeThis.getClass().getSimpleName());
            }

            JsonWriter jsonWriter = startWrite(output, writer, indented);
            jsonWriter.write(writeThis);
            finishWrite(output, writer, indented);
        } catch (Throwable ex) {
            throw new JenaException("Exception while writing JSON-LD 1.1", ex);
        }
    }

    // Plain - no @context.
    private JsonStructure writePlain(Document doc, DatasetGraph dsg) throws JsonLdError {
        JsonArray array = JsonLd.fromRdf(doc).get();
        JsonObject writeRdf = Json.createObjectBuilder()
                .add(Keywords.GRAPH, array)
                .build();
        return writeRdf;
    }

    private JsonStructure writePretty(Document doc, DatasetGraph dsg) throws JsonLdError {
        JsonLdOptions options = new JsonLdOptions();
        // Native types.
        // This looses information -- xsd:int becomes xsd:integer, xsd:double becomes xsd:decimal
        //   options.setUseNativeTypes(true);
        JsonArray array = FromRdfProcessor.fromRdf(doc, options);

        // Build context
        JsonObjectBuilder cxt = Json.createObjectBuilder();
        // Do not add @version. JSON-LD 1.0 processors would reject any input even if it is OK for JSON-LD 1.0.
        //cxt.add(Keywords.VERSION, "1.1");
        dsg.prefixes().forEach((k, v) -> {
            if ( ! k.isEmpty() )
                cxt.add(k, v);
        });
        String vocab = dsg.prefixes().get("");
        if ( vocab != null )
            cxt.add(Keywords.VOCAB, vocab);

        JsonObject context = cxt.build();

        // Object to write.
        JsonObject writeRdf = Json.createObjectBuilder()
                .add(Keywords.CONTEXT, context)
                .add(Keywords.GRAPH, array)
                .build();
        Document contextDoc = JsonDocument.of(context);

        // Compaction.
        CompactionApi api = JsonLd.compact(JsonDocument.of(writeRdf), contextDoc);
        api.rdfStar();
//        // Non-absolute URIs.
//        if ( dsg.prefixes().containsPrefix("") )
//            api.base(dsg.prefixes().get(""));
        // JSON Object to output - JSON array
        JsonStructure x = api.get();
        return x;
    }

    private JsonWriter startWrite(OutputStream output, Writer writer, boolean indented) {
        Map<String,?> config = config(indented);
        JsonWriterFactory factory = Json.createWriterFactory(config);
        return (output != null ) ? factory.createWriter(output) : factory.createWriter(writer);
    }

    /*
     * if (pretty) {
        Map<String, Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory jwf = Json.createWriterFactory(config);
        StringWriter sw = new StringWriter();
        try (JsonWriter jsonWriter = jwf.createWriter(sw)) {
            jsonWriter.writeObject(jsonObject);
        }
        // return "Content-Type: application/json", not "text/plain"
        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
        return Response.ok(sw.toString(), mediaType).build();
    } else {
        return Response.ok(jsonObject).build();
    }
     */

    private void finishWrite(OutputStream output, Writer writer, boolean indented) throws IOException {
        boolean outputNL = indented;
        if ( output != null ) {
            if (outputNL) output.write('\n');
            output.flush();
        } else {
            if (outputNL) writer.write("\n");
            writer.flush();
        }
    }
}
