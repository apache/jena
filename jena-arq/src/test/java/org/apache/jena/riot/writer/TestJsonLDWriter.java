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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
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
import org.apache.log4j.Logger;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

public class TestJsonLDWriter extends BaseTest {

    /**
     * Checks that JSON-LD RDFFormats supposed to be pretty are pretty
     * and that those supposed to be flat are flat
     */
    @Test public final void prettyIsNotFlat() {
        Model m = simpleModel();
        m.setNsPrefix("ex", "http://www.a.com/foo/");
        String s;

        // pretty is pretty

        s = toString(m, RDFFormat.JSONLD_EXPAND_PRETTY, null);
        assertTrue(s.trim().indexOf("\n") > -1);
        s = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        assertTrue(s.trim().indexOf("\n") > -1);
        s = toString(m, RDFFormat.JSONLD_FLATTEN_PRETTY, null);
        assertTrue(s.trim().indexOf("\n") > -1);

        // and flat is flat

        s = toString(m, RDFFormat.JSONLD_EXPAND_FLAT, null);
        assertTrue(s.trim().indexOf("\n") < 0);
        s = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, null);
        assertTrue(s.trim().indexOf("\n") < 0);
        s = toString(m, RDFFormat.JSONLD_FLATTEN_FLAT, null);
        assertTrue(s.trim().indexOf("\n") < 0);
        assertTrue(s.trim().indexOf("\n") < 0);
        // JSON_LD FRAME case not tested here, but in testFrames
    }

    /**
     * Checks that JSON-LD RDFFormats that are supposed to return a "@context" do so.
     */
    @Test public final void contextOrNot() {
        Model m = simpleModel();
        m.setNsPrefix("ex", "http://www.a.com/foo/");
        String s;

        // there's no "@context" in expand

        s = toString(m, RDFFormat.JSONLD_EXPAND_PRETTY, null);
        assertTrue(s.indexOf("@context") < 0);
        s = toString(m, RDFFormat.JSONLD_EXPAND_FLAT, null);
        assertTrue(s.indexOf("@context") < 0);

        // there's an "@context" in compact and flatten

        s = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        assertTrue(s.indexOf("@context") > -1);
        s = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, null);
        assertTrue(s.indexOf("@context") > -1);
        s = toString(m, RDFFormat.JSONLD_FLATTEN_PRETTY, null);
        assertTrue(s.indexOf("@context") > -1);
        s = toString(m, RDFFormat.JSONLD_FLATTEN_FLAT, null);
        assertTrue(s.indexOf("@context") > -1);
    }

    private Model simpleModel() {
        Model m = ModelFactory.createDefaultModel();
        String url = "http://www.a.com/foo/";
        Resource s = m.createResource(url + "s");
        Property p = m.createProperty(url + "p");
        Resource o = m.createResource(url + "o");
        m.add(s,p,o);
        return m;
    }

    /**
     * Write a model and parse it back: you should get the same thing
     * (except with frame)
     */
    @Test public final void roundTrip() {
        Model m = simpleModel();
        m.setNsPrefix("ex", "http://www.a.com/foo/");
        for (RDFFormat f : JSON_LD_FORMATS) {
            if (((RDFFormat.JSONLDVariant) f.getVariant()).isFrame()) continue;
            String s = toString(m, f, null);
            Model m2 = parse(s);
            assertTrue(m2.isIsomorphicWith(m));        
        }
    }

    /**
     * an output with something like:<pre>
     *  "@context" : {
     *      "" : "http://www.a.com/foo/",
     * <pre> is incorrect
     */
    @Test public final void noEmptyPrefixInContext() {
        Model m = simpleModel();
        m.setNsPrefix("", "http://www.a.com/foo/");
        String jsonld = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        assertTrue(jsonld.indexOf("\"\"") < 0);
        Model m2 = parse(jsonld);
        assertTrue(m2.isIsomorphicWith(m));
        System.out.println(jsonld);
    }

    /** verify that one may pass a context as a JSON string, and that it is actually used in the output */
    @Test public void testSettingContextAsJsonString() {
        // 1) get the context generated by default by jena
        // for a simple model with a prefix declaration
        // 2) remove prefix declaration from model,
        // output as jsonld is different
        // 3) output the model as jsonld using the context:
        // we should get the same output as in 1
        Model m = ModelFactory.createDefaultModel();
        String url = "http://www.semanlink.net/test/";
        Resource s = m.createResource(url + "s");
        Property p = m.createProperty(url + "p");
        Resource o = m.createResource(url + "o");
        m.add(s,p,o);
        m.setNsPrefix("ex", url);

        String s1 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, null);
        // there's a prefix in m, and we find it in the output
        String prefixStringInResult = "\"ex\":\"" + url + "\"";
        assertTrue(s1.indexOf(prefixStringInResult) > -1);
        Model m1 = parse(s1);

        // this is json object associated to "@context" in s1
        // it includes the "ex" prefix

        String js = "{\"p\":{\"@id\":\"http://www.semanlink.net/test/p\",\"@type\":\"@id\"},\"ex\":\"http://www.semanlink.net/test/\"}";

        // remove the prefix from m
        m.removeNsPrefix("ex");
        String s2 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        // model wo prefix -> no more prefix string in result:
        assertTrue(s2.indexOf(prefixStringInResult) < 0);

        // the model wo prefix, output as jsonld using a context that defines the prefix    
        JsonLDWriteContext jenaCtx = new JsonLDWriteContext();
        jenaCtx.setJsonLdContext(js);
        
        
        String s3 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);

        assertTrue(s3.length() == s1.length());
        assertTrue(s3.indexOf(prefixStringInResult) > 0);
        Model m3 = parse(s3);
        assertTrue(m3.isIsomorphicWith(m));
        assertTrue(m3.isIsomorphicWith(m1));

        // same thing, but passing also the "@context"
        js = "{\"@context\":" + js + "}";
        jenaCtx.setJsonLdContext(js);
        
        String s4 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);

        assertTrue(s4.length() == s1.length());
        assertTrue(s4.indexOf(prefixStringInResult) > 0);
        Model m4 = parse(s4);
        assertTrue(m4.isIsomorphicWith(m));
        assertTrue(m4.isIsomorphicWith(m1));
        
        // same thing, but passing a JsonObject //////////////////////////////////////////////////////
        JsonObject obj = new JsonObject();  
        obj.put("@id", "http://www.semanlink.net/test/p");
        obj.put("@type", "@id");
        JsonObject json = new JsonObject();
        json.put("p", obj);
        json.put("ex", "http://www.semanlink.net/test/");
        
        jenaCtx.setJsonLdContext(json.toString());
        String s5 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);

        assertTrue(s5.length() == s1.length());
        assertTrue(s5.indexOf(prefixStringInResult) > 0);
        Model m5 = parse(s5);
        assertTrue(m5.isIsomorphicWith(m));
        assertTrue(m5.isIsomorphicWith(m1));
      
    }

    
// KO
    
    
    /**
     * Checks that one can pass a context defined by its URI
     */
    @Test public final void testContextByUri() {
        Model m = ModelFactory.createDefaultModel();
        String ns = "http://schema.org/";
        Resource s = m.createResource();
        m.add(s, m.createProperty(ns + "name"), "Jane Doe");
        m.add(s, m.createProperty(ns + "url"), "http://www.janedoe.com");
        m.add(s, RDF.type, "Person");
        
        // we can pass an uri in the context, as a quoted string (it is a JSON string)
        Context jenaContext = new Context();
        try {
            jenaContext.set(JsonLDWriter.JSONLD_CONTEXT, "{\"@context\" : \"http://schema.org/\"}");
            String jsonld = toString(m, RDFFormat.JSONLD, jenaContext);
            System.out.println("----------");
            System.out.println(jsonld);
            // check it parses ok
            Model m2 = parse(jsonld);
            
            System.out.println("----------");
            m2.write(System.out, "TURTLE");
            assertTrue(m2.isIsomorphicWith(m));
            
        } catch (Throwable e) {
            // maybe test run in a setting wo external connectivity - not a real problem
            String mess = e.getMessage();
            if ((mess != null) && (mess.indexOf("loading remote context failed") > -1)) {
                Logger.getLogger(getClass()).info(mess);
                e.printStackTrace();
            } else {
                throw e;
            }
        }

        // But anyway, that's not what we want to do:
        // there's no point in passing the uri of a context to have it dereferenced by jsonld-java
        // (this is for a situation where one would want to parse a jsonld file containing a context defined by a uri)
        // What we want is to pass a context to jsonld-java (in order for json-ld java to produce the correct jsonld output)
        // and then we want to replace the @context in the output by "@context":"ourUri"

        // How would we do that? see testSubstitutingContext()
    }

    /**
     * This generates a warning, but one may pass a context directly as the object
     * used by the JSON-LD java API.
     */
    @Test public void testSettingContextAsObjectExpectedByJsonldAPI() {
        // 1) get the context generated by default by jena
        // for a simple model with a prefix declaration
        // 2) remove prefix declaration from model,
        // output as jsonld is different
        // 3) output the model as jsonld using the context:
        // we should get the same output as in 1
        Model m = ModelFactory.createDefaultModel();
        String url = "http://www.semanlink.net/test/";
        Resource s = m.createResource(url + "s");
        Property p = m.createProperty(url + "p");
        Resource o = m.createResource(url + "o");
        m.add(s,p,o);
        m.setNsPrefix("ex", url);

        String s1 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        // there's a prefix in m, and we find it in the output
        String prefixStringInResult = "\"ex\" : \"" + url + "\"";
        assertTrue(s1.indexOf(prefixStringInResult) > 0);
        Model m1 = parse(s1);

        // the context used in this case, created automatically by jena as none is set
        // it includes one prefix
        Object ctx = JsonLDWriter.createJsonldContext(m.getGraph());

        // remove the prefix from m
        m.removeNsPrefix("ex");
        String s2 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        // model wo prefix -> no more prefix string in result:
        assertTrue(s2.indexOf(prefixStringInResult) < 0);

        // the model wo prefix, output as jsonld using a context that defines the prefix
        Context jenaCtx = new Context();
        jenaCtx.set(JsonLDWriter.JSONLD_CONTEXT, ctx);
        String s3 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, jenaCtx);

        assertTrue(s3.length() == s1.length());
        assertTrue(s3.indexOf(prefixStringInResult) > 0);
        Model m3 = parse(s3);
        assertTrue(m3.isIsomorphicWith(m));
        assertTrue(m3.isIsomorphicWith(m1));

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
        jenaCtx.setJsonLdContextSubstitution((new JsonString(ns)).toString());
        String jsonld;
        jsonld = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);
        String c = "\"@context\":\"http://schema.org/\"";
        assertTrue(jsonld.indexOf(c) > -1);

         // change @context to a given ctx

        String ctx = "{\"jobTitle\":{\"@id\":\"http://ex.com/jobTitle\"},\"url\":{\"@id\":\"http://ex.com/url\"},\"name\":{\"@id\":\"http://ex.com/name\"}}";
        jenaCtx.setJsonLdContextSubstitution(ctx);
        jsonld = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);
        assertTrue(jsonld.indexOf("http://ex.com/name") > -1);
    }
    
    @Test public void readingContextDefinedByUri() {
        String jsonld = "{\"@id\":\"_:b0\",\"@type\":\"http://schema.org/Person\",\"jobTitle\":\"Professor\",\"name\":\"Jane Doe\",\"url\":\"http://www.janedoe.com\",\"@context\":\"http://schema.org/\"}";
        Model m = parse(jsonld);
        
        System.out.println("-- TURTLE -------------");
        m.write(System.out, "TURTLE");
        
        jsonld = toString(m, RDFFormat.JSONLD_EXPAND_PRETTY, null);
        System.out.println("-- JSONLD_EXPAND_PRETTY -------------");
        System.out.println(jsonld);
        
        jsonld = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
        System.out.println("-- JSONLD_COMPACT_PRETTY -------------");
        System.out.println(jsonld);
        
        JsonLDWriteContext jenaCtx = new JsonLDWriteContext();
        Map mapCtx = new HashMap();
        mapCtx.put("@context", "http://schema.org/");
        jenaCtx.set(JsonLDWriter.JSONLD_CONTEXT,mapCtx);
        jsonld = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, jenaCtx);
        System.out.println("-- JSONLD_COMPACT_PRETTY WITH URI BASED CONTEXT (?) -------------");
        System.out.println(jsonld);
        
    }
 
    @Test public void foo1() throws JsonParseException, IOException, JsonLdError {
        // String jsonld = "{\"@id\":\"_:b0\",\"@type\":\"http://schema.org/Person\",\"jobTitle\":\"Professor\",\"name\":\"Jane Doe\",\"url\":\"http://www.janedoe.com\",\"@context\":\"http://schema.org/\"}";
        String jsonld = "{\"@id\":\"_:b0\",\"jobTitle\":\"Professor\",\"name\":\"Jane Doe\",\"@context\":{\"jobTitle\":{\"@id\":\"http://schema.org/jobTitle\"},\"name\":{\"@id\":\"http://schema.org/name\"}}}";
        Model m = parse(jsonld);
        m.write(System.out, "TURTLE");
        
    }
    
    private String simpleContextJsonld() {
        return "{\"@id\":\"_:b0\",\"jobTitle\":\"Professor\",\"name\":\"Jane Doe\",\"@context\":{\"jobTitle\":{\"@id\":\"http://schema.org/jobTitle\"},\"name\":{\"@id\":\"http://schema.org/name\"}}}";
    }
    
    @Test public void test1() throws JsonParseException, IOException, JsonLdError {
        String jsonld = simpleContextJsonld();
        
        Object jsonObject = JsonUtils.fromString(jsonld);
        
        JsonLdOptions options = new JsonLdOptions();

        Object expand = JsonLdProcessor.expand(jsonObject, options);
        //Print out the result (or don't, it's your call!)
        
        System.out.println("-- expand: ");
        System.out.println(JsonUtils.toPrettyString(expand));
    }

    
    @Test public void foo() throws JsonParseException, IOException, JsonLdError {
        // String jsonld = "{\"@id\":\"_:b0\",\"@type\":\"http://schema.org/Person\",\"jobTitle\":\"Professor\",\"name\":\"Jane Doe\",\"url\":\"http://www.janedoe.com\",\"@context\":\"http://schema.org/\"}";
        String jsonld = "{\"@id\":\"_:b0\",\"jobTitle\":\"Professor\",\"name\":\"Jane Doe\",\"@context\":{\"jobTitle\":{\"@id\":\"http://schema.org/jobTitle\"},\"name\":{\"@id\":\"http://schema.org/name\"}}}";
        System.out.println(jsonld);
        
        Map jsonObject = (Map) JsonUtils.fromString(jsonld);
        Map context = new HashMap();
        // context.put("@context", "http://schema.org/");
//        context.put("@context", jsonObject.get("@context"));
        JsonLdOptions options = new JsonLdOptions();
        // options.setExpandContext(context);
     // Customise options...
     // Call whichever JSONLD function you want! (e.g. compact)
Object expand = JsonLdProcessor.expand(jsonObject, options);
System.out.println(JsonUtils.toPrettyString(expand));
     Object compact = JsonLdProcessor.compact(jsonObject, context, options);
     // Print out the result (or don't, it's your call!)
     System.out.println(JsonUtils.toPrettyString(compact));
     
     
//         Model m = ModelFactory.createDefaultModel();
//         String ns = "http://schema.org/";
//         // Resource person = m.createResource(ns + "Person");
//         Resource s = m.createResource();
//         m.add(s, m.createProperty(ns + "name"), "Jane Doe");
//         m.add(s, m.createProperty(ns + "jobTitle"), "Professor");
//         // m.add(s, RDF.type, person);
//         s = m.createResource();
//         // m.setNsPrefix("sh", ns);
//         jsonld = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, null);
//         System.out.println(jsonld);
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
        jenaCtx.set(JsonLDWriter.JSONLD_FRAME, JsonUtils.fromString(frame.toString()));
        String jsonld = toString(m, RDFFormat.JSONLD_FRAME_PRETTY, jenaCtx);

        Model m2 = parse(jsonld);
        // 2 subjects with a type in m2
        assertTrue(m2.listStatements((Resource) null, RDF.type, (RDFNode) null).toList().size() == 2);
        // 2 persons in m2
        assertTrue(m2.listStatements((Resource) null, RDF.type, person).toList().size() == 2);
        // something we hadn't tested in prettyIsNotFlat
        assertTrue(jsonld.trim().indexOf("\n") > -1);

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
        assertTrue(jsonld.trim().indexOf("\n") < 0);
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
        assertTrue(jsonld.indexOf(vv) > -1);
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
        assertTrue(jsonld.indexOf("\"name\" : \"") > -1);
        assertTrue(jsonld.indexOf("/name\" : \"") > -1);
 
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
        assertTrue(jsonld.indexOf("\"name\" : \"") > -1);
        assertTrue((jsonld.indexOf("\"ns1:name\" : \"") > -1) || (jsonld.indexOf("\"ns2:name\" : \"") > -1));
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
        assertTrue(jsonld.indexOf("@graph") < 0);
        // compactArrays is true -> string, not an array for props with one value
        assertTrue(jsonld.indexOf("\"jobTitle\" : \"Professor\"") > -1);
        
        // now output using a value for JsonLdOptions in Context that sets compactArrays to false
        
        JsonLDWriteContext jenaCtx = new JsonLDWriteContext();
        
        JsonLdOptions opts = new JsonLdOptions(null);
        opts.setCompactArrays(false);       
 
        jenaCtx.setOptions(opts);

        jsonld = toString(m, RDFFormat.JSONLD, jenaCtx);

        // compactArrays is false -> a "@graph" node
        assertTrue(jsonld.indexOf("@graph") > -1);
        // compactArrays is false -> an array for all props, even when there's only one value
        assertTrue(jsonld.indexOf("\"jobTitle\" : [ \"Professor\" ]") > -1);
    }
    
    //
    // some utilities
    //

    private String toString(Model m, RDFFormat f, Context jenaContext) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            WriterDatasetRIOT w = RDFDataMgr.createDatasetWriter(f) ;
            DatasetGraph g = DatasetFactory.create(m).asDatasetGraph();
            PrefixMap pm = RiotLib.prefixMap(g);
            String base = null;
            w.write(out, g, pm, base, jenaContext) ;

            out.flush();
            String x = out.toString("UTF-8");
            out.close();
            return x;
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
