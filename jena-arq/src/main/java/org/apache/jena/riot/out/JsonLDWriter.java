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

package org.apache.jena.riot.out ;

import static org.apache.jena.rdf.model.impl.Util.isLangString;
import static org.apache.jena.rdf.model.impl.Util.isSimpleString;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.writer.WriterDatasetRIOTBase ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.vocabulary.RDF ;

import com.fasterxml.jackson.core.JsonGenerationException ;
import com.fasterxml.jackson.databind.JsonMappingException ;
import com.github.jsonldjava.core.JsonLdError ;
import com.github.jsonldjava.core.JsonLdOptions ;
import com.github.jsonldjava.core.JsonLdProcessor ;
import com.github.jsonldjava.utils.JsonUtils ;

/**
 * Writer that prints out JSON-LD.
 * 
 * By default, the output is "compact" (in JSON-LD terminology).
 * One can choose another form using one of the dedicated RDFFormats (JSONLD_EXPAND_PRETTY, etc.).
 * For formats using a context ("@context" node), (compact and expand), this automatically generates a default one.
 * One can pass a jsonld context using the (jena) context mechanism, defining a (jena) Context
 * (sorry for this clash of contexts), (cf. 4th argument in
 * {@link org.apache.jena.riot.RDFDataMgr#write(OutputStream out, Model model, RDFFormat serialization, Context ctx)})
 * with:
 * <pre>
 * Context jenaContext = new Context()
 * jenaCtx.set(JsonLDWriter.JSONLD_CONTEXT, jsonldCtx);
 * </pre>
 * where jsonldCtx is the object expected by the JSONLD-java API.
 * 
 * One can also pass a frame with the {@link #JSONLD_FRAME}, or define the options expected
 * by JSONLD-java using {@link #JSONLD_OPTIONS} 
 * 
 * 
 */
public class JsonLDWriter extends WriterDatasetRIOTBase
{
		/** value: the context expected by JsonLdProcessor.compact anf flatten */
		public static final Symbol JSONLD_CONTEXT = Symbol.create("JSONLD_CONTEXT");
		/** value: the frame object expected by JsonLdProcessor.frame */
		public static final Symbol JSONLD_FRAME = Symbol.create("JSONLD_FRAME");
		/** value: the option object expected by JsonLdProcessor (instance of JsonLdOptions) */
		public static final Symbol JSONLD_OPTIONS = Symbol.create("JSONLD_OPTIONS");
		private static enum JSONLD_FORMAT {
			COMPACT,
			FLATTEN,
			EXPAND,
			FRAME
		}
		
    private final RDFFormat format ;

    public JsonLDWriter(RDFFormat syntaxForm) {
        format = syntaxForm ;
    }

    @Override
    public Lang getLang() {
        return format.getLang() ;
    }

    @Override
    public void write(Writer out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) {
        serialize(out, dataset, prefixMap, baseURI, context) ;
    }

    @Override
    public void write(OutputStream out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) {
        Writer w = new OutputStreamWriter(out, Chars.charsetUTF8) ;
        write(w, dataset, prefixMap, baseURI, context) ;
        IO.flush(w) ;
    }

    private JSONLD_FORMAT getOutputFormat() {
	  		if ((RDFFormat.JSONLD_COMPACT_PRETTY.equals(format)) || (RDFFormat.JSONLD_COMPACT_FLAT.equals(format))) return JSONLD_FORMAT.COMPACT;
	  		if ((RDFFormat.JSONLD_EXPAND_PRETTY.equals(format)) || (RDFFormat.JSONLD_EXPAND_FLAT.equals(format))) return JSONLD_FORMAT.EXPAND;
	  		if ((RDFFormat.JSONLD_FLATTEN_PRETTY.equals(format)) || (RDFFormat.JSONLD_FLATTEN_FLAT.equals(format))) return JSONLD_FORMAT.FLATTEN;
	  		if ((RDFFormat.JSONLD_FRAME_PRETTY.equals(format)) || (RDFFormat.JSONLD_FRAME_FLAT.equals(format))) return JSONLD_FORMAT.FRAME;
	  		throw new RuntimeException("Unexpected output format");
    }
    
    private boolean isPretty() {
    		return (((RDFFormat.JSONLD_COMPACT_PRETTY.equals(format))
    				|| (RDFFormat.JSONLD_FLATTEN_PRETTY.equals(format))
    				|| (RDFFormat.JSONLD_EXPAND_PRETTY.equals(format)))
    				|| (RDFFormat.JSONLD_FRAME_PRETTY.equals(format))) ;
    }
    
    private JsonLdOptions getJsonLdOptions(String baseURI, Context jenaContext) {
	  		JsonLdOptions opts = null;
	  		if (jenaContext != null) {
	  			opts = (JsonLdOptions) jenaContext.get(JSONLD_OPTIONS);
	  		}
	  		if (opts == null) {
	        opts = new JsonLdOptions(baseURI);
	        opts.useNamespaces = true ;
	        //opts.setUseRdfType(true);
	        opts.setUseNativeTypes(true);
	        opts.setCompactArrays(true);	  			
	  		} 
	  		return opts;
    }

    private void serialize(Writer writer, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context jenaContext) {
        try {
        		JsonLdOptions opts = getJsonLdOptions(baseURI, jenaContext) ;
        		
            Object obj = JsonLdProcessor.fromRDF(dataset, opts, new JenaRDF2JSONLD()) ;
            
            JSONLD_FORMAT outputForm = getOutputFormat() ;
      	    if (outputForm == JSONLD_FORMAT.EXPAND) {
      	    	// nothing more to do
      	    
      	    } else if (outputForm == JSONLD_FORMAT.FRAME) {
      	    	Object frame = null;
      	    	if (jenaContext != null) 
      	    		frame = jenaContext.get(JSONLD_FRAME);
      	    	
      	    	if (frame == null) {
      	    		throw new IllegalArgumentException("No frame object found in context");
      	    	}
      	    	obj = JsonLdProcessor.frame(obj, frame, opts);

      	    } else { // we need a (jsonld) context
      	  		Object ctx = null;
      	  		boolean isCtxDefined = false; // to allow jenaContext to set ctx to null. Useful?

      	  		if (jenaContext != null) {
      	  			if (jenaContext.isDefined(JSONLD_CONTEXT)) {
      	  				isCtxDefined = true;
      	  				ctx = jenaContext.get(JSONLD_CONTEXT);
      	  			}
      	  		}

      	  		if (!isCtxDefined) {
      	  			// if no ctx passed via jenaContext, create one in order to have localnames as keys for properties
      	  			ctx = createJsonldContext(dataset.getDefaultGraph(), prefixMap) ;
      	  			
                // I don't think this should be done: the JsonLdProcessor begins
                // by looking whether the argument passed is a map with key "@context" and takes corresponding value
                // Better not to do this: we create a map for nothing, and worse,
      	  			// if the context object has been created by a user and passed through the (jena) context
                // in case he got the same idea, we would end up with 2 levels of maps an it would work
//                Map<String, Object> localCtx = new HashMap<>() ;
//                localCtx.put("@context", ctx) ;
//              	obj = JsonLdProcessor.compact(obj, localCtx, opts) ;
      	  		}
      	
      	  		if (outputForm == JSONLD_FORMAT.COMPACT) {
      	      	obj = JsonLdProcessor.compact(obj, ctx, opts);
      	      	
      	      } else if (outputForm == JSONLD_FORMAT.FLATTEN) {
      	      	obj = JsonLdProcessor.flatten(obj, ctx, opts);
      	      	
      	      } else {
      	      	throw new IllegalArgumentException("Unexpected output form " + outputForm);
      	      }
      	    }

            if ( isPretty() )
                JsonUtils.writePrettyPrint(writer, obj) ;
            else
                JsonUtils.write(writer, obj) ;
            writer.write("\n") ;
        }
        catch (JsonLdError | JsonMappingException | JsonGenerationException e) {
            throw new RiotException(e) ;
        }
        catch (IOException e) {
            IO.exception(e) ;
        }
    }

    //
    // creating a (jsonld) context
    //
    
  	// useful to help people wanting to create their own context?
  	public static Object createJsonldContext(Graph g) {
  		return createJsonldContext(g, PrefixMapFactory.create(g.getPrefixMapping()));
  	}

  	private static Object createJsonldContext(Graph g, PrefixMap prefixMap) {
  		final Map<String, Object> ctx = new LinkedHashMap<>() ;
  		addProperties(ctx, g) ;
  		addPrefixes(ctx, prefixMap) ;	
  		return ctx ;
  	}

    private static void addPrefixes(Map<String, Object> ctx, PrefixMap prefixMap) {
        Map<String, IRI> pmap = prefixMap.getMapping() ;
        for ( Entry<String, IRI> e : pmap.entrySet() ) {
            String key = e.getKey() ;
            if ( key.isEmpty() ) {
                // Prefix "" is not allowed in JSON-LD
            		// we could replace "" with "@vocab"
              	// key = "@vocab" ;
            		continue;
            }
            ctx.put(key, e.getValue().toString()) ;
        }
    }

    private static void addProperties(final Map<String, Object> ctx, Graph graph) {
        // Add some properties directly so it becomes "localname": ....
        Consumer<Triple> x = new Consumer<Triple>() {
            @Override
            public void accept(Triple item) {
                Node p = item.getPredicate() ;
                Node o = item.getObject() ;
                if ( p.equals(RDF.type.asNode()) )
                    return ;
                String x = p.getLocalName() ;

                if ( ctx.containsKey(x) ) {
                } else if ( o.isBlank() || o.isURI() ) {
                    // add property as a property (the object is an IRI)
                    Map<String, Object> x2 = new LinkedHashMap<>() ;
                    x2.put("@id", p.getURI()) ;
                    x2.put("@type", "@id") ;
                    ctx.put(x, x2) ;
                } else if ( o.isLiteral() ) {
                    String literalDatatypeURI = o.getLiteralDatatypeURI() ;
                    if ( literalDatatypeURI != null ) {
                        // add property as a typed attribute (the object is a
                        // typed literal)
                        Map<String, Object> x2 = new LinkedHashMap<>() ;
                        x2.put("@id", p.getURI()) ;
                        if (! isLangString(o) && ! isSimpleString(o) ) 
                            // RDF 1.1 : Skip if rdf:langString or xsd:string.
                            x2.put("@type", literalDatatypeURI) ; 
                        ctx.put(x, x2) ;
                    } else {
                        // add property as an untyped attribute (the object is
                        // an untyped literal)
                        ctx.put(x, p.getURI()) ;
                    }
                }
            }
        } ;

        Iter.iter(graph.find(null, null, null)).apply(x) ;
    }
}