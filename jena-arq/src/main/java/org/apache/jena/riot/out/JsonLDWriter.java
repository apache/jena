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

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.util.* ;
import java.util.Map.Entry ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.iterator.Action ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.writer.WriterDatasetRIOTBase ;

import com.fasterxml.jackson.core.JsonGenerationException ;
import com.fasterxml.jackson.databind.JsonMappingException ;
import com.github.jsonldjava.core.JsonLdError ;
import com.github.jsonldjava.core.JsonLdOptions ;
import com.github.jsonldjava.core.JsonLdProcessor ;
import com.github.jsonldjava.utils.JsonUtils ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class JsonLDWriter extends WriterDatasetRIOTBase
{
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
        serialize(out, dataset, prefixMap, baseURI) ;
    }

    private boolean isPretty() {
        return RDFFormat.PRETTY.equals(format.getVariant()) ;
    }

    @Override
    public void write(OutputStream out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) {
        Writer w = new OutputStreamWriter(out, Chars.charsetUTF8) ;
        write(w, dataset, prefixMap, baseURI, context) ;
        IO.flush(w) ;
    }

    private void serialize(Writer writer, DatasetGraph dataset, PrefixMap prefixMap, String baseURI) {
        final Map<String, Object> ctx = new LinkedHashMap<>() ;
        addProperties(ctx, dataset.getDefaultGraph()) ;
        addPrefixes(ctx, prefixMap) ;

        try {
            JsonLdOptions opts = new JsonLdOptions(baseURI);
            opts.useNamespaces = true ;
            //opts.setUseRdfType(true);
            opts.setUseNativeTypes(true);
            opts.setCompactArrays(true);
            Object obj = JsonLdProcessor.fromRDF(dataset, opts, new JenaRDF2JSONLD()) ;
            
            Map<String, Object> localCtx = new HashMap<>() ;
            localCtx.put("@context", ctx) ;

            // Unclear as to the way to set better printing.
            obj = JsonLdProcessor.compact(obj, localCtx, opts) ;

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

    private static void addPrefixes(Map<String, Object> ctx, PrefixMap prefixMap) {
        Map<String, IRI> pmap = prefixMap.getMapping() ;
        for ( Entry<String, IRI> e : pmap.entrySet() ) {
            String key = e.getKey() ;
            IRI iri = e.getValue() ;
            ctx.put(e.getKey(), e.getValue().toString()) ;
        }
    }

    private static void addProperties(final Map<String, Object> ctx, Graph graph) {
        // Add some properties directly so it becomes "localname": ....
        final Set<String> dups = new HashSet<>() ;
        Action<Triple> x = new Action<Triple>() {
            @Override
            public void apply(Triple item) {
                Node p = item.getPredicate() ;
                Node o = item.getObject() ;
                if ( p.equals(RDF.type.asNode()) )
                    return ;
                String x = p.getLocalName() ;
                if ( dups.contains(x) )
                    return ;

                if ( ctx.containsKey(x) ) {
                    // Check different URI
                    // pmap2.remove(x) ;
                    // dups.add(x) ;
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