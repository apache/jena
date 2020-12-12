/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.rewriters;

import java.util.Map;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.* ;

/**
 * A rewriter that implements NodeValueVisitor
 *
 */
class NodeValueRewriter extends AbstractRewriter<NodeValue> implements
		NodeValueVisitor {

	/**
	 * Constructor.  
	 * @param values The values to replace.
	 */
	public NodeValueRewriter(Map<Var, Node> values) {
		super(values);
	}

	@Override
	public void visit(NodeValueBoolean nv) {
		push(new NodeValueBoolean(nv.getBoolean(), changeNode(nv.asNode())));
	}

	@Override
	public void visit(NodeValueDecimal nv) {
		push(new NodeValueDecimal(nv.getDecimal(), changeNode(nv.asNode())));
	}

	@Override
	public void visit(NodeValueDouble nv) {
		push(new NodeValueDouble(nv.getDouble(), changeNode(nv.asNode())));
	}

	@Override
	public void visit(NodeValueFloat nv) {
		push(new NodeValueFloat(nv.getFloat(), changeNode(nv.asNode())));
	}

	@Override
	public void visit(NodeValueInteger nv) {
		push(new NodeValueInteger(nv.getInteger(), changeNode(nv.asNode())));
	}

	@Override
	public void visit(NodeValueNode nv) {
		push(new NodeValueNode(changeNode(nv.asNode())));
	}

	@Override
	public void visit(NodeValueString nv) {
		push(new NodeValueString(nv.getString(), changeNode(nv.asNode())));
	}

    @Override
    public void visit(NodeValueSortKey nv) {
    	push(new NodeValueSortKey(nv.getString(), nv.getCollation(), changeNode(nv.asNode())));
    }

	@Override
	public void visit(NodeValueDateTime nv) {
		push(new NodeValueDateTime(nv.getDateTime().toXMLFormat(),
				changeNode(nv.asNode())));
	}

	@Override
	public void visit(NodeValueDuration nodeValueDuration) {
		push(new NodeValueDuration(nodeValueDuration.getDuration(),
				changeNode(nodeValueDuration.asNode())));
	}

	@Override
	public void visit(NodeValueLang nv) {
		
		push( new NodeValueLang( changeNode(nv.asNode() )));
	}
}
