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

package org.apache.jena.riot.out;

import java.io.OutputStream ;
import java.io.Writer ;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.json.io.JSWriter ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.Prologue ;
import org.apache.jena.riot.system.SyntaxLabels ;

import com.hp.hpl.jena.graph.Node;

public class SinkEntityOutput implements Sink<Pair<Node, Map<Node, Set<Node>>>> {

    private Prologue prologue = null ;
    private NodeToLabel labelPolicy = null ;
    private JSWriter out ;
	
    public SinkEntityOutput(OutputStream outs)
    {
        this(outs, null, SyntaxLabels.createNodeToLabel()) ;
    }
    
    public SinkEntityOutput(OutputStream outs, Prologue prologue, NodeToLabel labels)
    {
    	init(new JSWriter(outs), prologue, labels) ;
    }

    public SinkEntityOutput(Writer outs)
    {
        this(outs, null, SyntaxLabels.createNodeToLabel()) ;
    }
    
    public SinkEntityOutput(Writer outs, Prologue prologue, NodeToLabel labels)
    {
    	init(new JSWriter(new IndentedWriterEx(outs)), prologue, labels) ;
    }
    
    private void init (JSWriter out, Prologue prologue, NodeToLabel labels) 
    {
    	this.out = out ;
    	setPrologue(prologue) ;
    	setLabelPolicy(labels) ;
    	out.startOutput() ;
    	out.startObject() ;
    }

    public void setPrologue(Prologue prologue)
    {
    	this.prologue = prologue ;
    }

    public void setLabelPolicy(NodeToLabel labels)
    {
    	this.labelPolicy = labels ;
    }

    @Override
	public void send(Pair<Node, Map<Node, Set<Node>>> item) {
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
			        String dt = o.getLiteralDatatypeURI() ;
			        String lang = o.getLiteralLanguage() ;
			        String lex = o.getLiteralLexicalForm() ;
					out.pair("type", "literal") ;
					out.pair("value", lex) ;
			        if ( dt != null ) 
			        	out.pair("datatype", dt) ;
			        if ( ( lang != null ) && ( lang != "" ) ) 
			        	out.pair("lang", lang) ;
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

    @Override
	public void flush() {
		out.finishObject() ;
		out.finishOutput();
	}
	
    @Override
	public void close() {
		flush() ;
	}

    private class IndentedWriterEx extends IndentedWriter {
		public IndentedWriterEx(Writer writer) {
			super(writer);
		}
    }

}
