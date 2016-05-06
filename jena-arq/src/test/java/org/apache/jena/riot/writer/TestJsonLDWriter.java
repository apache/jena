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

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.JsonLDWriter;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.jsonldjava.utils.JsonUtils;

public class TestJsonLDWriter extends BaseTest {

/**
 * Checks that JSON-LD RDFFormats supposed to be pretty are pretty
 * and those supposed to be flat are flat
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
 * Checks that JSON-LD RDFFormats that are supposed to return a "&context"
 * actually do so.
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
		if (f.getVariant().toString().indexOf("frame") > -1) continue;
		String s = toString(m, f, null);
		Model m2 = parse(s);
		assertTrue(m2.isIsomorphicWith(m));		
	}
}

//@Test public final void supportEmptyNs() {
//	Model m = ModelFactory.createDefaultModel();
//	String ns = "http://www.a.com/foo/";
//	Resource s = m.createResource(ns + "s");
//	Property p = m.createProperty(ns + "p");
//	Resource o = m.createResource(ns + "o");
//	m.add(s,p,o);
//	m.add(m.createResource(ns + "s2"),p,m.createResource(ns + "o2"));
//	m.setNsPrefix("ns", ns);
//	Model m2 = parse(toJsonLDString(m));
//	assertTrue(m2.isIsomorphicWith(m));
//	
//	// RDFDataMgr.write(DevNull.out, m, RDFFormat.JSONLD) ;
//	RDFDataMgr.write(System.out, m2, RDFFormat.TURTLE);
//	RDFDataMgr.write(System.out, m, RDFFormat.JSONLD);
//}

/** verify that one may pass a context, and that it is actually used in the output */
@Test public void testSettingContext() {
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
	m.removeNsPrefix("ex");	// RDFDataMgr.write(System.out, m, RDFFormat.JSONLD) ;
	String s2 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
	// model wo prefix -> no more prefix string in result:
	assertTrue(s2.indexOf(prefixStringInResult) < 0);

	// the model wo prefix, outputed as jsonld using a context that defines the prefix	
	Context jenaCtx = new Context();
	jenaCtx.set(JsonLDWriter.JSONLD_CONTEXT, ctx);
	String s3 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, jenaCtx);
	
	assertTrue(s3.length() == s1.length());
	assertTrue(s3.indexOf(prefixStringInResult) > 0);
	Model m3 = parse(s3);
	assertTrue(m3.isIsomorphicWith(m));
	assertTrue(m3.isIsomorphicWith(m1));

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
	assertTrue(s1.indexOf(prefixStringInResult) > 0);
	Model m1 = parse(s1);
	
	// must we pass the json object associated to "@context",
	// or its parent node (that is, with the "@context") ?
	// Actually, we can do both (JSONLD-java's code ensure it)
	// We check it for here
	
	// this is json object associated to "@context" in s1
	// it includes the "ex" prefix
	
	String js = "{\"p\":{\"@id\":\"http://www.semanlink.net/test/p\",\"@type\":\"@id\"},\"ex\":\"http://www.semanlink.net/test/\"}";

	// remove the prefix from m
	m.removeNsPrefix("ex");	// RDFDataMgr.write(System.out, m, RDFFormat.JSONLD) ;
	String s2 = toString(m, RDFFormat.JSONLD_COMPACT_PRETTY, null);
	// model wo prefix -> no more prefix string in result:
	assertTrue(s2.indexOf(prefixStringInResult) < 0);

	// the model wo prefix, outputed as jsonld using a context that defines the prefix	
	Context jenaCtx = new Context();
	jenaCtx.set(JsonLDWriter.JSONLD_CONTEXT_AS_JSON_STRING, js);
	String s3 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);
	
	assertTrue(s3.length() == s1.length());
	assertTrue(s3.indexOf(prefixStringInResult) > 0);
	Model m3 = parse(s3);
	assertTrue(m3.isIsomorphicWith(m));
	assertTrue(m3.isIsomorphicWith(m1));
	
	// same thing, but passing also the "@context"
	js = "{\"@context\":" + js + "}";
	jenaCtx.set(JsonLDWriter.JSONLD_CONTEXT_AS_JSON_STRING, js);
	String s4 = toString(m, RDFFormat.JSONLD_COMPACT_FLAT, jenaCtx);
	
	assertTrue(s4.length() == s1.length());
	assertTrue(s4.indexOf(prefixStringInResult) > 0);
	Model m4 = parse(s4);
	assertTrue(m4.isIsomorphicWith(m));
	assertTrue(m4.isIsomorphicWith(m1));
}

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
	
	// without passing a context
	// String jsonld = toString(m, RDFFormat.JSONLD, null);
	// System.out.println(jsonld);
	
	// we can pass a uri in the context.
	Context jenaContext = new Context();
	try {
		jenaContext.set(JsonLDWriter.JSONLD_CONTEXT, "http://schema.org/");
		String jsonld = toString(m, RDFFormat.JSONLD, jenaContext);
	} catch (Throwable e) {
		Logger.getLogger(getClass()).info("Sorry to get this exception",e);
	}

	// It seems to work, except we get
	/*
java.lang.NoSuchMethodError: org.apache.http.impl.client.cache.CacheConfig.custom()Lorg/apache/http/impl/client/cache/CacheConfig$Builder;
	at com.github.jsonldjava.utils.JsonUtils.createDefaultHttpClient(JsonUtils.java:333)
	at com.github.jsonldjava.utils.JsonUtils.getDefaultHttpClient(JsonUtils.java:323)
	at com.github.jsonldjava.core.DocumentLoader.getHttpClient(DocumentLoader.java:84)
	at com.github.jsonldjava.core.DocumentLoader.fromURL(DocumentLoader.java:59)
	at com.github.jsonldjava.core.DocumentLoader.loadDocument(DocumentLoader.java:29)
	at com.github.jsonldjava.core.Context.parse(Context.java:169)
	at com.github.jsonldjava.core.Context.parse(Context.java:252)
	at com.github.jsonldjava.core.JsonLdProcessor.compact(JsonLdProcessor.java:57)
	at org.apache.jena.riot.out.JsonLDWriter.serialize(JsonLDWriter.java:179)
	at org.apache.jena.riot.out.JsonLDWriter.write(JsonLDWriter.java:85)
	at org.apache.jena.riot.out.JsonLDWriter.write(JsonLDWriter.java:126)
	at org.apache.jena.riot.system.RiotLib$WriterAdapter.write(RiotLib.java:376)
	at org.apache.jena.riot.RDFDataMgr.write$(RDFDataMgr.java:1235)
	at org.apache.jena.riot.RDFDataMgr.write(RDFDataMgr.java:1026)
	at org.apache.jena.riot.RDFDataMgr.write(RDFDataMgr.java:956)
	at org.apache.jena.riot.out.TestJsonLD.toString(TestJsonLD.java:232)
	at org.apache.jena.riot.out.TestJsonLD.testContextByUri(TestJsonLD.java:186)
	*/
	// 
	// httpclient-cache 4.2.6 from jena
	// httpclient.version 4.5.1 from jsonld-java
	// No chance to see this solved, as jena parent's pom says:
	/*
      <dependency>
        <groupId>com.github.jsonld-java</groupId>
        <artifactId>jsonld-java</artifactId>
        <version>${ver.jsonldjava}</version>
        <exclusions>
          <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
          </exclusion>
          <!-- Exclude so we use our choice of versions -->
          <exclusion>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient-cache</artifactId>
          </exclusion>
          <exclusion>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
          </exclusion>
	*/
	
	// But anyway, that's not what we want to do:
	// there's no point in passing the uri of a context to have it dereferenced by jsonld-java
	// (this is for a situation where one would want to parse a jsonld file containing a context defined by a uri)
	// What we want is to pass a context to jsonld-java (in order for json-ld java to produce the correct jsonls output
	// and then we want to replace the @context in the output by @context="our uri"
	
	// How would we do that?
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
	
	// only ouput the persons using a frame
	
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

//
// some utilities
//

private String toString(Model m, RDFFormat f, Context jenaContext) {
	try {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFDataMgr.write(out, m, f, jenaContext) ;
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
