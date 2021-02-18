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
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;

/**
 * A rewriter that implements PathVisitor.
 *
 */
public class PathRewriter extends AbstractRewriter<Path> implements PathVisitor {

	/**
	 * Constructor.
	 * @param values The values to replace.
	 */
	public PathRewriter(Map<Var, Node> values) {
		super(values);
	}

	@Override
	public void visit(P_Link pathNode) {
		push(new P_Link(changeNode(pathNode.getNode())));
	}

	@Override
	public void visit(P_ReverseLink pathNode) {
		push(new P_ReverseLink(changeNode(pathNode.getNode())));
	}

	/*
	 * Reverse transformations. X !(^:uri1|...|^:urin)Y ==> ^(X
	 * !(:uri1|...|:urin) Y) Split into forward and reverse. X
	 * !(:uri1|...|:urii|^:urii+1|...|^:urim) Y ==> { X !(:uri1|...|:urii|)Y }
	 * UNION { X !(^:urii+1|...|^:urim) Y }
	 */
	@Override
	public void visit(P_NegPropSet pathNotOneOf) {
		P_NegPropSet retval = new P_NegPropSet();

		for (Path p : pathNotOneOf.getNodes()) {
			p.visit(this);
			retval.add((P_Path0) pop());
		}
		push(retval);
	}

	@Override
	public void visit(P_Inverse inversePath) {
		inversePath.getSubPath().visit(this);
		push(new P_Inverse(pop()));
	}

	@Override
	public void visit(P_Mod pathMod) {
		pathMod.getSubPath().visit(this);
		push(new P_Mod(pop(), pathMod.getMin(), pathMod.getMax()));
	}

	@Override
	public void visit(P_FixedLength pFixedLength) {
		pFixedLength.getSubPath().visit(this);
		push(new P_FixedLength(pop(), pFixedLength.getCount()));
	}

	@Override
	public void visit(P_Alt pathAlt) {
		pathAlt.getRight().visit(this);
		pathAlt.getLeft().visit(this);
		push(new P_Alt(pop(), pop()));
	}

	@Override
	public void visit(P_Seq pathSeq) {
		pathSeq.getRight().visit(this);
		pathSeq.getLeft().visit(this);
		push(new P_Seq(pop(), pop()));
	}

	@Override
	public void visit(P_Distinct pathDistinct) {
		pathDistinct.getSubPath().visit(this);
		push(new P_Distinct(pop()));
	}

	@Override
	public void visit(P_Multi pathMulti) {
		pathMulti.getSubPath().visit(this);
		push(new P_Multi(pop()));
	}

	@Override
	public void visit(P_Shortest pathShortest) {
		pathShortest.getSubPath().visit(this);
		push(new P_Shortest(pop()));
	}

	@Override
	public void visit(P_ZeroOrOne path) {
		path.getSubPath().visit(this);
		push(new P_ZeroOrOne(pop()));
	}

	@Override
	public void visit(P_ZeroOrMore1 path) {
		path.getSubPath().visit(this);
		push(new P_ZeroOrMore1(pop()));
	}

	@Override
	public void visit(P_ZeroOrMoreN path) {
		path.getSubPath().visit(this);
		push(new P_ZeroOrMoreN(pop()));
	}

	@Override
	public void visit(P_OneOrMore1 path) {
		path.getSubPath().visit(this);
		push(new P_OneOrMore1(pop()));
	}

	@Override
	public void visit(P_OneOrMoreN path) {
		path.getSubPath().visit(this);
		push(new P_OneOrMoreN(pop()));
	}
}