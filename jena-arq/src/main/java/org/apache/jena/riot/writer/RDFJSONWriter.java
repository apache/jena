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

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.json.io.JSWriter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.NodeUtils ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * RDF-JSON.
 * <p>
 * This is not JSON-LD.
 *
 * @see <a href="http://www.w3.org/TR/rdf-json/">http://www.w3.org/TR/rdf-json/</a>
 */
public class RDFJSONWriter extends WriterGraphRIOTBase
{
    public RDFJSONWriter() {}

	public static void output(OutputStream out, Graph graph) {
		output(new JSWriter(out), graph ) ;
	}

	public static void output(Writer out, Graph graph) {
		output(new JSWriter(new IndentedWriterEx(out)), graph ) ;
	}

	@Override
    public Lang getLang()
    {
        return Lang.RDFJSON ;
    }

    @Override
    public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        output(out, graph) ;
    }

    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context)
    {
        output(out, graph) ;
    }

    private static void output(JSWriter out, Graph graph) {
	    out.startOutput() ;
	    out.startObject();

		ExtendedIterator<Node> subjects = GraphUtil.listSubjects(graph, Node.ANY, Node.ANY) ;
		try {
			Map<Node, Set<Node>> predicates = new HashMap<>() ;
			while ( subjects.hasNext() ) {
				Node subject = subjects.next() ;
				ExtendedIterator<Triple> triples = graph.find(subject, Node.ANY, Node.ANY) ;
				try {
					while ( triples.hasNext() ) {
						Triple triple = triples.next() ;
						Node p = triple.getPredicate() ;
						if ( predicates.containsKey(p) ) {
							predicates.get(p).add(triple.getObject()) ;
						} else {
							Set<Node> objects = new HashSet<>() ;
							objects.add(triple.getObject()) ;
							predicates.put(p, objects) ;
						}
					}
				} finally {
					if ( triples != null ) triples.close() ;
				}
				send(out, new Pair<>(subject, predicates)) ;
				predicates.clear() ;
			}
		} finally {
			if ( subjects != null ) subjects.close() ;
			out.finishObject();
			out.finishOutput() ;
		}
	}

    private static void send(JSWriter out, Pair<Node, Map<Node, Set<Node>>> item) {
        Node s = item.getLeft() ;
        if ( s.isBlank() ) {
            out.key("_:" + s.getBlankNodeLabel()) ;
        } else if ( s.isURI() ) {
            out.key(s.getURI()) ;
        } else {
            throw new RiotException ("Only URIs or blank nodes are legal subjects.") ;
        }

        out.startObject() ;
        // out.pair(key, value) ;
        Map<Node, Set<Node>> predicates = item.getRight() ;
        for (Node p : predicates.keySet() ) {
            out.key(p.getURI()) ;
            out.startArray() ;
            Set<Node> objects = predicates.get(p) ;
            int i = 0;
            for ( Node o : objects ) {
                out.startObject() ;
                if ( o.isBlank() ) {
                    out.pair("type", "bnode") ;
                    out.pair("value", "_:" + o.getBlankNodeLabel()) ;
                } else if ( o.isURI() ) {
                    out.pair("type", "uri") ;
                    out.pair("value", o.getURI()) ;
                } else if ( o.isLiteral() ) {
                    String lex = o.getLiteralLexicalForm() ;
                    out.pair("type", "literal") ;
                    out.pair("value", lex) ;

                    if ( NodeUtils.isSimpleString(o) ) {
                        // No-op.
                    } else if ( NodeUtils.isLangString(o) ) {
                        String lang = o.getLiteralLanguage() ;
                        out.pair("lang", lang) ;
                    } else {
                        // Datatype, nothing special.
                        String dt = o.getLiteralDatatypeURI() ;
                        out.pair("datatype", dt) ;
                    }
                }
                out.finishObject() ;
                if (i < objects.size() - 1)
                {
                    out.arraySep();
                }
                i++;
            }
            out.finishArray() ;
        }
        out.finishObject() ;
    }

    private static class IndentedWriterEx extends IndentedWriter {
        public IndentedWriterEx(Writer writer) {
            super(writer);
        }
    }
}
