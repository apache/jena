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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.utils.JsonUtils;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.JsonLDWriteContext;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestJsonLDWriter {

    /**
     * Checks that JSON-LD RDFFormats supposed to be pretty are pretty
     * and that those supposed to be flat are flat
     */
    @Test public final void prettyIsNotFlat() {
        String ns = "http://www.a.com/foo/";
        Model m = simpleModel(ns);
        m.setNsPrefix("ex", ns);
        String s;

        // pretty is pretty

        s = toString(m, RDFFormat.JSONLD_EXPAND_PRETTY, null);
        assertTrue(s.trim().contains("\n"));
        s = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        assertTrue(s.trim().contains("\n"));
        s = toString(m, RDFFormat.JSONLD_FLATTEN_PRETTY, null);
        assertTrue(s.trim().contains("\n"));

        // and flat is flat

        s = toString(m, RDFFormat.JSONLD_EXPAND_FLAT, null);
        assertFalse(s.trim().contains("\n"));
        s = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, null);
        assertFalse(s.trim().contains("\n"));
        s = toString(m, RDFFormat.JSONLD_FLATTEN_FLAT, null);
        assertFalse(s.trim().contains("\n"));
        assertFalse(s.trim().contains("\n"));
        // JSON_LD FRAME case not tested here, but in testFrames
    }

    /**
     * Checks that JSON-LD RDFFormats that are supposed to return a "@context" do so.
     */
    @Test public final void contextOrNot() {
        String ns = "http://www.a.com/foo/";
        Model m = simpleModel(ns);
        m.setNsPrefix("ex", ns);
        String s;

        // there's no "@context" in expand

        s = toString(m, RDFFormat.JSONLD_EXPAND_PRETTY, null);
        assertFalse(s.contains("@context"));
        s = toString(m, RDFFormat.JSONLD_EXPAND_FLAT, null);
        assertFalse(s.contains("@context"));

        // there's an "@context" in compact and flatten

        s = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        assertTrue(s.contains("@context"));
        s = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, null);
        assertTrue(s.contains("@context"));
        s = toString(m, RDFFormat.JSONLD_FLATTEN_PRETTY, null);
        assertTrue(s.contains("@context"));
        s = toString(m, RDFFormat.JSONLD_FLATTEN_FLAT, null);
        assertTrue(s.contains("@context"));
    }

    private Model simpleModel(String ns) {
        Model m = ModelFactory.createDefaultModel();
        Resource s = m.createResource(ns + "s");
        Property p = m.createProperty(ns + "p");
        Resource o = m.createResource(ns + "o");
        m.add(s,p,o);
        return m;
    }

    /**
     * Write a model and parse it back: you should get the same thing
     * (except with frame)
     */
    @Test public final void roundTrip() {
        String ns = "http://www.a.com/foo/";
        Model m = simpleModel(ns);
        m.setNsPrefix("ex", ns);
        for (RDFFormat f : JSON_LD_FORMATS) {
            if (((RDFFormat.JSONLDVariant) f.getVariant()).isFrame()) continue;
            String s = toString(m, f, null);
            Model m2 = parse(s);
            assertTrue(m2.isIsomorphicWith(m));
        }
    }

    /**
     * Test that we do not use an "" prefix in @Context.
     */
    @Test public final void noEmptyPrefixInContext() {
        String ns = "http://www.a.com/foo/";
        Model m = simpleModel(ns);
        m.setNsPrefix("", ns);
        String jsonld = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        assertFalse(jsonld.contains("\"\""));
        Model m2 = parse(jsonld);
        assertTrue(m2.isIsomorphicWith(m));
    }

    /** verify that one may pass a context as a JSON string, and that it is actually used in the output */
    @Test public void testSettingContextAsJsonString() {
        // 1) get the context generated by default by jena
        // for a simple model with a prefix declaration
        // 2) remove prefix declaration from model,
        // output as jsonld is different
        // 3) output the model as jsonld using the context:
        // we should get the same output as in 1
        String ns = "http://www.a.com/foo/";
        Model m = simpleModel(ns);
        m.setNsPrefix("ex", ns);

        String s1 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, null);
        // there's a prefix in m, and we find it in the output
        String prefixStringInResult = "\"ex\":\"" + ns + "\"";
        assertTrue(s1.contains(prefixStringInResult));
        Model m1 = parse(s1);

        // this is the json object associated to "@context" in s1
        // it includes the "ex" prefix

        // String js = "{\"p\":{\"@id\":\"http://www.a.com/foo/p\",\"@type\":\"@id\"},\"ex\":\"http://www.a.com/foo/\"}";
        // constructing the js string ny hand:
        JsonObject obj = new JsonObject();
        obj.put("@id", ns + "p");
        obj.put("@type", "@id");
        JsonObject json = new JsonObject();
        json.put("p", obj);
        json.put("ex", ns);
        String js = json.toString();

        // remove the prefix from m
        m.removeNsPrefix("ex");
        String s2 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        // model wo prefix -> no more prefix string in result:
        assertFalse(s2.contains(prefixStringInResult));

        // the model wo prefix, output as jsonld using a context that defines the prefix
        JsonLDWriteContext jenaCtx = new JsonLDWriteContext();
        jenaCtx.setJsonLDContext(js);

        String s3 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);

        assertTrue(s3.length() == s1.length());
        assertTrue(s3.contains(prefixStringInResult));
        Model m3 = parse(s3);
        assertTrue(m3.isIsomorphicWith(m));
        assertTrue(m3.isIsomorphicWith(m1));

        // to be noted: things also work if passing the "@context"
        js = "{\"@context\":" + js + "}";
        jenaCtx.setJsonLDContext(js);

        String s4 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);

        assertTrue(s4.length() == s1.length());
        assertTrue(s4.contains(prefixStringInResult));
        Model m4 = parse(s4);
        assertTrue(m4.isIsomorphicWith(m));
        assertTrue(m4.isIsomorphicWith(m1));
    }

    /**
     * one may pass the object expected by the JSON-LD java AP as context
     * (otherwise, same thing as testSettingContextAsJsonString)
     */
    @Test public void testSettingContextAsObjectExpectedByJsonldAPI() {
        String ns = "http://www.a.com/foo/";
        Model m = simpleModel(ns);
        m.setNsPrefix("ex", ns);

        String s1 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, null);
        // there's a prefix in m, and we find it in the output
        String prefixStringInResult = "\"ex\":\"" + ns + "\"";
        assertTrue(s1.contains(prefixStringInResult));
        Model m1 = parse(s1);

        // the context used in this case, as it would automatically be created as none is set
        // it includes one prefix
        Object ctx = JsonLDWriter.createJsonldContext(m.getGraph());

        // remove the prefix from m
        m.removeNsPrefix("ex");
        String s2 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        // model wo prefix -> no more prefix string in result:
        assertFalse(s2.contains(prefixStringInResult));

        // the model wo prefix, output as jsonld using a context that defines the prefix
        Context jenaCtx = new Context();
        jenaCtx.set(JsonLDWriter.JSONLD_CONTEXT, ctx);
        String s3 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);

        assertTrue(s3.length() == s1.length());
        assertTrue(s3.contains(prefixStringInResult));
        Model m3 = parse(s3);
        assertTrue(m3.isIsomorphicWith(m));
        assertTrue(m3.isIsomorphicWith(m1));
    }

    /**
     * Checks that one can pass a context defined by its URI
     *
     */
    @Test
    public final void testContextByUri() {
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource s = m.createResource();
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, m.createProperty(ns + "url"), "http://www.janedoe.com");
        m.add(s, RDF.type, "Person");

        // we can pass an uri in the context, as a quoted string (it is a JSON string)
        JsonLDWriteContext jenaContext = new JsonLDWriteContext();
        jenaContext.setJsonLDContext("{\"@context\" : \"http://schema.org/\"}");
        String jsonld = toString(m, RDFFormat.JSONLD, jenaContext);
        // check it parses ok
        Model m2 = parse(jsonld);
        assertTrue(m2.isIsomorphicWith(m));
    }



    /**
     * Test using a context to compute the output, and replacing the @context with a given value
     */
    @Test public void testSubstitutingContext() {
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource person = m.createResource(ns + "Person");
        Resource s = m.createResource();
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, m.createProperty(ns + "url"), "http://www.janedoe.com");
        m.add(s, m.createProperty(ns + "jobTitle"), "Professor");
        m.add(s, RDF.type, person);

        // change @context to a URI

        JsonLDWriteContext jenaCtx = new JsonLDWriteContext();
        jenaCtx.setJsonLDContextSubstitution((new JsonString(ns)).toString());
        String jsonld;
        jsonld = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);
        String c = "\"@context\":\"http://schema.org/\"";
        assertTrue(jsonld.contains(c));

        // change @context to a given ctx

        String ctx = "{\"jobTitle\":{\"@id\":\"http://ex.com/jobTitle\"},\"url\":{\"@id\":\"http://ex.com/url\"},\"name\":{\"@id\":\"http://ex.com/name\"}}";
        jenaCtx.setJsonLDContextSubstitution(ctx);
        jsonld = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);
        assertTrue(jsonld.contains("http://ex.com/name"));
    }

    /**
     * Checking frames
     */
    @Test public final void testFrames() throws JsonParseException, IOException {
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

        Context jenaCtx = new Context();
        JsonObject frame = new JsonObject();

        // only output the persons using a frame

        frame.put("@type", ns +"Person");
        jenaCtx.set(JsonLDWriter.JSONLD_FRAME, frame.toString());
        String jsonld = toString(m, RDFFormat.JSONLD_FRAME_PRETTY, jenaCtx);

        Model m2 = parse(jsonld);
        // 2 subjects with a type in m2
        assertTrue(m2.listStatements((Resource) null, RDF.type, (RDFNode) null).toList().size() == 2);
        // 2 persons in m2
        assertTrue(m2.listStatements((Resource) null, RDF.type, person).toList().size() == 2);
        // something we hadn't tested in prettyIsNotFlat
        assertTrue(jsonld.trim().contains("\n"));

        // only output the subjects which have a jobTitle

        frame = new JsonObject();
        frame.put("http://schema.org/jobTitle", new JsonObject());
        jenaCtx.set(JsonLDWriter.JSONLD_FRAME, JsonUtils.fromString(frame.toString()));
        jsonld = toString(m, RDFFormat.JSONLD_FRAME_FLAT, jenaCtx);
        m2 = parse(jsonld);
        // 1 subject with a type in m2
        assertTrue(m2.listStatements((Resource) null, RDF.type, (RDFNode) null).toList().size() == 1);
        // 1 subject with a jobTitle in m2
        assertTrue(m2.listStatements((Resource) null, m.createProperty(ns + "jobTitle"), (RDFNode) null).toList().size() == 1);
        // something we hadn't tested in prettyIsNotFlat
        assertFalse(jsonld.trim().contains("\n"));
    }

    /**
     * There was a problem with props taking a string as value.
     * cf https://mail-archives.apache.org/mod_mbox/jena-users/201604.mbox/%3c218AC4A3-030B-4248-A7DA-2B2597328242@gmail.com%3e
     */
    @Test public final void testStringPropsInContext() {
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://www.a.com/foo/";
        Resource s = m.createResource(ns + "s");
        m.add(s,m.createProperty(ns + "plangstring"),"a langstring","fr");
        m.add(s, m.createProperty(ns + "pint"), m.createTypedLiteral(42));
        m.add(s, m.createProperty(ns + "pfloat"), m.createTypedLiteral((float) 1789.14));
        m.add(s, m.createProperty(ns + "pstring"), m.createTypedLiteral("a TypedLiteral atring"));

        String jsonld = toString(m, RDFFormat.JSONLD_FLAT, null);

        // without following line in JsonLDWriter, the test fails
        // if (! isLangString(o) && ! isSimpleString(o) )
        String vv = "\"plangstring\":{\"@language\":\"fr\",\"@value\":\"a langstring\"}";
        assertTrue(jsonld.contains(vv));
    }

    /**
     * Check there are no problems when 2 properties have the same localname
     */
    @Test public final void clashOfPropLocalnames() {
        Model m = ModelFactory.createDefaultModel();
        Resource s = m.createResource();
        String ns1 = "http://schema.org/";
        String ns2 = "http://ex.com/";

        m.add(s, m.createProperty(ns1 + "name"), "schema.org name");
        m.add(s, m.createProperty(ns2 + "name"), "ex.com name");

        String jsonld = toString(m, RDFFormat.JSONLD, null);

        // in one case, we have "name" : "xxx", and the other "http://.../name" : "yyy"
        assertTrue(jsonld.contains("\"name\" : \""));
        assertTrue(jsonld.contains("/name\" : \""));

        m.setNsPrefix("ns1", ns1);
        m.setNsPrefix("ns2", "http://ex.com/");
        jsonld = toString(m, RDFFormat.JSONLD, null);
        // we get either:
        /*
      "name" : "ex.com name",
      "ns1:name" : "schema.org name",
         */
        // or
        /*
      "name" : "schema.org name",
      "ns2:name" : "ex.com name",
         */
        assertTrue(jsonld.contains("\"name\" : \""));
        assertTrue((jsonld.contains("\"ns1:name\" : \"")) || (jsonld.contains("\"ns2:name\" : \"")));
    }

    /** Test passing a JsonLdOptions through Context */
    @Test public final void jsonldOptions() {
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource s = m.createResource();
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, m.createProperty(ns + "url"), "http://www.janedoe.com");
        m.add(s, m.createProperty(ns + "jobTitle"), "Professor");

        // our default uses true for compactArrays

        String jsonld = toString(m, RDFFormat.JSONLD, null);

        // compactArrays is true -> no "@graph"
        assertFalse(jsonld.contains("@graph"));
        // compactArrays is true -> string, not an array for props with one value
        assertTrue(jsonld.contains("\"jobTitle\" : \"Professor\""));

        // now output using a value for JsonLdOptions in Context that sets compactArrays to false

        JsonLDWriteContext jenaCtx = new JsonLDWriteContext();

        JsonLdOptions opts = new JsonLdOptions(null);
        opts.setCompactArrays(false);

        jenaCtx.setOptions(opts);

        jsonld = toString(m, RDFFormat.JSONLD, jenaCtx);

        // compactArrays is false -> a "@graph" node
        assertTrue(jsonld.contains("@graph"));
        // compactArrays is false -> an array for all props, even when there's only one value
        assertTrue(jsonld.contains("\"jobTitle\" : [ \"Professor\" ]"));
    }

    //
    // @vocab
    //

    // checks we get @vocab when using an "" ns prefix
    @Test public final void atVocab() throws JsonParseException, JsonLdError, IOException {
        // "Jane knows John" Model
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource person = m.createResource(ns + "Person");
        Resource s = m.createResource("http://doe.com/jane");
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, RDF.type, person);
        Resource s2 = m.createResource("http://doe.com/joe");
        m.add(s2, m.createProperty(ns + "name"), "John Doe");
        m.add(s2, RDF.type, person);
        m.add(s, m.createProperty(ns + "knows"), s2);

        m.setNsPrefix("", ns);
        String jsonld = toString(m, RDFFormat.JSONLD, null);
        assertTrue(jsonld.contains("@vocab"));
    }

    /**
     * setting @vocab and replacing @context
     * not really a test, sample code for JENA-1292 */
    @SuppressWarnings("unchecked")
    @Test public final void atVocabJENA1292() throws JsonParseException, JsonLdError, IOException {
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource person = m.createResource(ns + "Person");
        Resource s = m.createResource();
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, m.createProperty(ns + "url"), "http://www.janedoe.com");
        m.add(s, m.createProperty(ns + "jobTitle"), "Professor");
        m.add(s, FOAF.nick, "jd");
        m.add(s, RDF.type, person);

        m.setNsPrefix("", ns);

        DatasetGraph g = DatasetFactory.wrap(m).asDatasetGraph();
        PrefixMap pm = g.prefixes();
        String base = null;
        Context jenaContext = null;

        // the JSON-LD API object. It's a map
        Map<String, Object> map = (Map<String, Object>) JsonLDWriter.toJsonLDJavaAPI((RDFFormat.JSONLDVariant)RDFFormat.JSONLD.getVariant()
                , g, pm, base, jenaContext);

        // get the @context:
        Map<String, Object> ctx = (Map<String, Object>) map.get("@context");

        // remove from ctx declaration of props in ns
        List<String> remove = new ArrayList<>();
        for (Entry<String, Object> e : ctx.entrySet()) {
            // is it the declaration of a prop in ns?
            Object o = e.getValue();
            if (o instanceof Map) {
                o = ((Map<String, Object>) o).get("@id");
            }
            if ((o != null) && (o instanceof String)) {
                if (((String) o).equals(ns + e.getKey())) {
                    remove.add(e.getKey());
                }
            }
        }
        for (String key : remove) {
            ctx.remove(key);
        }

        // add to ctx the "@vocab" key
        ctx.put("@vocab", "http://schema.org/");

        // JsonUtils.writePrettyPrint(new PrintWriter(System.out), map) ;
    }


    //
    // some utilities
    //

    private String toString(Model m, RDFFormat f, Context jenaContext) {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            RDFWriter.source(m).format(f).context(jenaContext).output(out);
            out.flush();
            return out.toString("UTF-8");
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private Model parse(String jsonld) {
        Model m = ModelFactory.createDefaultModel();
        StringReader reader = new StringReader(jsonld);
        m.read(reader, null, "JSON-LD");
        return m;
    }

    private static RDFFormat[] JSON_LD_FORMATS = {
            RDFFormat.JSONLD_COMPACT_PRETTY,
            RDFFormat.JSONLD_FLATTEN_PRETTY,
            RDFFormat.JSONLD_EXPAND_PRETTY,
            RDFFormat.JSONLD_FRAME_PRETTY,
            RDFFormat.JSONLD_COMPACT_FLAT,
            RDFFormat.JSONLD_FLATTEN_FLAT,
            RDFFormat.JSONLD_EXPAND_FLAT,
            RDFFormat.JSONLD_FRAME_FLAT,
    };
}
