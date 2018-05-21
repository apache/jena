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

package arq.examples.riot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import com.github.jsonldjava.core.JsonLdOptions;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.JsonLDWriteContext;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WriterDatasetRIOT;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/** Example writing as JSON-LD */
public class ExJsonLD
{
    public static void main(String[] args) {
        (new ExJsonLD()).doIt();
    }

    void doIt() {
        doSimpleStuff();
        moreControl();
    }

    /**
     * Simple stuff.
     * 
     * output using defaults, 
     * in "expanded"", compacted" or "flattened" format
     * (framed is more difficult, not handled here)
     */
    void doSimpleStuff() {
        Model m = aSimpleModel();

        // to get a default output: just do like for any other lang
        System.out.println("--- DEFAULT ---");
        m.write(System.out, "JSON-LD");

        // same thing, using the more modern RDFDataMgr, and specifying the RDFFormat
        System.out.println("\n--- DEFAULT ---");
        RDFDataMgr.write(System.out, m, RDFFormat.JSONLD);

        // output can be pretty (with line breaks), or not
        System.out.println("\n--- DEFAULT, PRETTY (same as above, BTW) ---");
        RDFDataMgr.write(System.out, m, RDFFormat.JSONLD_PRETTY);

        System.out.println("\n--- DEFAULT, FLAT ---");
        RDFDataMgr.write(System.out, m, RDFFormat.JSONLD_FLAT);

        // all these default outputs use the JsonLD "Compact" format
        // (note the "@context" node in the output)

        // if prefixes are defined in the model,
        // they are used in computing the @context,
        // and corresponding values are displayed as prefix:localname
        // (note something nice wrt prefixes in jsonld: look in the output at the value of "seeAlso")
        m.setNsPrefix("ex", "http://www.ex.com/");
        m.setNsPrefix("sh", "http://schema.org/");
        System.out.println("\n--- DEFAULT, model including prefix mappings ---");
        RDFDataMgr.write(System.out, m, RDFFormat.JSONLD_PRETTY);

        // Besides "Compact", JSON-LD defines the following kinds of outputs: expanded, flattened, and framed
        // For each of them, there is a dedicated RDFFormat -- actually, 2 of them (one pretty, one flat)
        // As previously seen, RDFFormat.JSONLD is just an alias of RDFFormat.JSONLD_COMPACT_PRETYY
        // Let's try the other ones:

        // Expand is the fastest one
        // no @context in it
        System.out.println("\n--- EXPAND ---");
        RDFDataMgr.write(System.out, m, RDFFormat.JSONLD_EXPAND_PRETTY);

        // flatten has an @context node
        System.out.println("\n--- FLATTEN ---");
        RDFDataMgr.write(System.out, m, RDFFormat.JSONLD_FLATTEN_PRETTY);

        // framed requires some more parameters to run, we'll get back to it later
    }


    /**
     * To get more control about the output,
     * we have to use a mechanism provided by jena to pass information
     * to the writing process
     * 
     * This requires a few lines of code, see {@link #write(DatasetGraph, RDFFormat, Context)}
     * 
     * Here we use this write method to see what can be customized.
     */
    public void moreControl() {
        Model m = aSimpleModel();
        m.setNsPrefix("ex", "http://www.ex.com/");
        m.setNsPrefix("sh", "http://schema.org/");

        // the write method takes a DatasetGraph as input to represent the data that we want to output
        // Let's create one from our model:
        DatasetGraph g = DatasetFactory.wrap(m).asDatasetGraph();

        // and let's use the write method to output the data in json-ld compact format,
        // passing a null Context for the moment
        // (remember, "Context" here is not to be confused with "@context" in JSON-LD,
        // see {@link #write(DatasetGraph, RDFFormat, Context)})
        System.out.println("\n--- COMPACT with a null Context: same result as default ---");
        write(g, RDFFormat.JSONLD_COMPACT_PRETTY, null);

        // A Context is just a way to pass implementation-specific parameters as named values
        // to a given general interface (the WriterDatasetRIOT, in this case).
        // In order to make it easier to define the named values relevant here,
        // there is a subclass of Context that defines setters for the values:
        // the JsonLDWriteContext class

        // so, the way to proceed is:
        // JsonLDWriteContext ctx = new JsonLDWriteContext();       
        // ctx.setSomething(...)
        // write(g, RDFFormat.JSONLD_COMPACT_PRETTY, ctx);

        // let's see now what can be customized with the JsonLDWriteContext object

        controllingAtContext();
        settingAtContextToURI();
        frame();
        controllingJsonLDApiOptions();
    }


    /**
     * Shows how to customize the "@context" in "compacted" and "flattened" format.
     * 
     * To set it to the URI of a vocab, {@link #settingAtContextToURI()}
     */
    void controllingAtContext() {
        Model m = aSimpleModel();
        m.setNsPrefix("ex", "http://www.ex.com/");
        m.setNsPrefix("sh", "http://schema.org/");
        DatasetGraph g = DatasetFactory.wrap(m).asDatasetGraph();
        JsonLDWriteContext ctx = new JsonLDWriteContext();

        // When no value for the "@context" is provided, 
        // Jena computes one from the defined prefixes, and from the RDF content.
        // This default is probably good enough in most of the cases, 
        // but you may want to customize it. 
        // Or, if it is always the same one, you may consider that computing it again and again
        // (each time that you output data), is a waste of time.
        // (the computing of the "@context" implies to loop through all the triples).
        // You may therefore want to compute it once for all, and to pass it to the output process

        // To pass a given "@context" to the writing process,
        // you pass the corresponding value as a JSON string
        // using the setJsonLDContext(String) method.
        // (Alternatively, you can directly pass the object expected by the JSON-LD API)

        // For instance, we can pass a simple context 
        // that uses jsonld "@vocab" keyord to set the "default vocabulary"
        // to schema.org.
        String atContextAsJson = "{\"@vocab\":\"http://schema.org/\"}";
        ctx.setJsonLDContext(atContextAsJson);
        System.out.println("\n--- COMPACT using a Context that defines @vocab ---");
        write(g, RDFFormat.JSONLD_COMPACT_PRETTY, ctx);
    }

    /**
     * Shows how to set "@context" to a URI.
     */
    void settingAtContextToURI() {
        // One thing you'll probably want to do is to set the "@context" to the URL of a file
        // containing the actual JSON-LD context.

        // Let's take one Model that only uses schema.org terms,
        // and let's try to set the "@Context" to the URL of schema.org 
        // "@context" : "http://schema.org/"

        Model m = aModelThatOnlyUsesSchemaDotOrg();
        DatasetGraph g = DatasetFactory.wrap(m).asDatasetGraph();
        JsonLDWriteContext ctx = new JsonLDWriteContext();

        // The following should work, but unfortunately it doesn't (with JSONLD-java 0.8.3):
        ctx.setJsonLDContext("\"http://schema.org/\"");
        System.out.println("\n--- Setting the context to a URI, WRONG WAY: it's slow, and the output is not JSON-LD. Sorry about that. ---");
        write(g, RDFFormat.JSONLD_COMPACT_PRETTY, ctx);

        // But don't worry (be happy): 
        // - there is a solution
        // - and what we tried is not what we would want to do, anyway.
        
        // The output process needs to have the content of the "@context" at hand
        // in order to compute the output. So, if passing the URL of the vocab,
        // the output process must download the vocab before anything.
        // (that's why the previous attempt was slow)
        // -> that would not be an very efficient way to output your data.
        // -> it doesn't work, (with JSONLD-java 0.8.3), but no regret.

        // To achieve the expected result,
        // you have to do 2 things:

        // 1)
        // you have to pass the dereferenced content of http://schema.org/
        // - or the relevant subset of it (as we only use very few terms).
        // Here it is:
        String atContextAsJson = "{\"name\":{\"@id\":\"http://schema.org/name\"},\"Person\": {\"@id\": \"http://schema.org/Person\"}}";
        ctx.setJsonLDContext(atContextAsJson);
        // Alternatively, we could just pass "null":
        // ctx.setJsonLDContext(null);
        // and let jena compute the context (as the model only uses schema.org vocab)

        // 2)
        // and then you pass the schema.org url using:
        ctx.setJsonLDContextSubstitution("\"http://schema.org/\"");

        // To summarize:
        // - ctx.setJsonLDContext allows to define the @context used to compute the output
        // - ctx.setJsonLDContextSubstitution allows to change the value of the "@context" in the output
        System.out.println("\n--- COMPACT with @context replaced by schema.org URI ---");
        write(g, RDFFormat.JSONLD_COMPACT_PRETTY, ctx);

        // Final note: BEWARE when replacing the context:
        // if you let some things undefined, the output will be json, not jsonld

    }

    /**
     * Shows how to apply a frame to the output RDF data
     */
    void frame() {
        // a "frame" is a specific graph layout that is applied to output data
        // It can be used to filter the output data.
        // In this example, we show how to output only the resources of a givn rdf:type
        
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource person = m.createResource(ns + "Person");
        Resource s = m.createResource();
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, m.createProperty(ns + "url"), "http://www.janedoe.com");
        m.add(s, m.createProperty(ns + "jobTitle"), "Professor");
        m.add(s, RDF.type, person);
        s = m.createResource();
        m.add(s, m.createProperty(ns + "name"), "Gado Salamatou");
        m.add(s, m.createProperty(ns + "url"), "http://www.salamatou.com");
        m.add(s, RDF.type, person);
        s = m.createResource();
        m.add(s, m.createProperty(ns + "name"), "Not a person");
        m.add(s, RDF.type, m.createResource(ns + "Event"));

        DatasetGraph g = DatasetFactory.wrap(m).asDatasetGraph();
        JsonLDWriteContext ctx = new JsonLDWriteContext();

        // only output the persons using a frame
        
        String frame = "{\"@type\" : \"http://schema.org/Person\"}";
        ctx.setFrame(frame);
        System.out.println("\n--- Using frame to select resources to be output: only output persons ---");
        write(g, RDFFormat.JSONLD_FRAME_PRETTY, ctx);
    }

    /**
     *  the JSON-LD java API (that jena uses for JSON-LD I/O) defines a set of options
     *  that can be customized
     */
    void controllingJsonLDApiOptions() {
        Model m = aSimpleModel();
        m.setNsPrefix("ex", "http://www.ex.com/");
        m.setNsPrefix("sh", "http://schema.org/");
        DatasetGraph g = DatasetFactory.wrap(m).asDatasetGraph();
        JsonLDWriteContext ctx = new JsonLDWriteContext();
        JsonLdOptions opts = new JsonLdOptions();
        ctx.setOptions(opts);
        opts.setCompactArrays(false); // default is true
        System.out.println("\n--- COMPACT with CompactArrays false: there is an @graph node");
        write(g, RDFFormat.JSONLD_COMPACT_PRETTY, ctx);
    }

    /**
     * Write RDF data as JSON-LD.
     * 
     * To get more control about the output,
     * we have to use a mechanism provided by jena to pass information
     * to the writing process (cf. org.apache.jena.riot.WriterDatasetRIOT and the "Context" mechanism).
     * For that, we have to create a WriterDatasetRIOT (a one-time use object)
     * and we pass a "Context" object (not to be confused with the "@context" in JSON-LD) as argument to its write method
     * 
     * @param out
     * @param f RDFFormat of the output, eg. RDFFormat.JSONLD_COMPACT_PRETTY
     * @param g the data that we want to output as JSON-LD
     * @param ctx the object that allows to control the writing process (a set of parameters)
     */
    void write(OutputStream out, DatasetGraph g, RDFFormat f, Context ctx) {
        // create a WriterDatasetRIOT with the RDFFormat
        WriterDatasetRIOT w = RDFDataMgr.createDatasetWriter(f) ;
        PrefixMap pm = RiotLib.prefixMap(g);
        String base = null;
        w.write(out, g, pm, base, ctx) ;     
    }

    /** Write RDF data to the console */
    private void write(DatasetGraph g, RDFFormat f, Context ctx) {
        write(System.out, g, f, ctx);
    }

    // following 2 methods: if you want to test
    // that everything is OK in a roundtrip: model -> jsonld -> model
    // something like:
    // String jsonld = write2String(g, RDFFormat.JSONLD_COMPACT_PRETTY, ctx);
    // Model m2 = parse(jsonld);
    // System.out.println("ISO : " + m.isIsomorphicWith(m2));

    /** Write RDF data into a String */
    private String write2String(DatasetGraph g, RDFFormat f, Context ctx) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            write(out, g, f, ctx);
            out.flush();
            return out.toString("UTF-8");
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    /** Parse a jsonld string into a Model */
    private Model parse(String jsonld) {
        Model m = ModelFactory.createDefaultModel();
        StringReader reader = new StringReader(jsonld);
        m.read(reader, null, "JSON-LD");
        return m;
    }

    private Model aSimpleModel() {
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource person = m.createResource(ns + "Person");
        Resource s;
        s = m.createResource("http://www.ex.com/janedoe");
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, RDF.type, person);
        m.add(s, RDFS.seeAlso, m.createResource("http://www.ex.com/janedoe/moreinfo"));
        //        m.add(s, m.createProperty(ns + "url"), "http://www.janedoe.com");
        //        m.add(s, m.createProperty(ns + "jobTitle"), "Professor");
        //        s = m.createResource();
        //        m.add(s, m.createProperty(ns + "name"), "Salamatou Gado");
        //        m.add(s, m.createProperty(ns + "url"), "http://www.salamatou.com");
        return m;
    }

    private Model aModelThatOnlyUsesSchemaDotOrg() {
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource person = m.createResource(ns + "Person");
        Resource s;
        s = m.createResource("http://www.ex.com/janedoe");
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, RDF.type, person);
        return m;
    }
}

