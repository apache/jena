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

package org.apache.jena.query.spatial.pfunction;

import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.impl.LiteralLabel ;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.query.spatial.SpatialIndexException;
import org.apache.jena.query.spatial.SpatialValueUtil;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SpatialOperationWithBoxPFBase extends SpatialOperationPFBase {
	private static Logger log = LoggerFactory.getLogger(SpatialOperationWithCircleBase.class);

	public SpatialOperationWithBoxPFBase() {
	}

	@Override
	public void build(PropFuncArg argSubject, Node predicate,
			PropFuncArg argObject, ExecutionContext execCxt) {
		super.build(argSubject, predicate, argObject, execCxt);
		
		if (!argSubject.isNode())
			throw new QueryBuildException("Subject is not a single node: "
					+ argSubject);

		if (argObject.isList()) {
			List<Node> list = argObject.getArgList();
			if (list.size() < 4)
				throw new QueryBuildException("Not enough arguments in list");

			if (list.size() > 5)
				throw new QueryBuildException("Too many arguments in list : "
						+ list);
		}
	}

	/** Deconstruct the node or list object argument and make a SpatialMatch */
	@Override
	protected SpatialMatch objectToStruct(PropFuncArg argObject) {
		
		if (argObject.isNode()) {
			log.warn("Object not a List: " + argObject);
			return null;
		}

		List<Node> list = argObject.getArgList();

		if (list.size() < 4 || list.size() > 5)
			throw new SpatialIndexException("Change in object list size");

		int idx = 0;

		Node x = list.get(idx);
		if (!x.isLiteral()) {
			log.warn("Latitude 1 is not a literal " + list);
			return null;
		}
		if (!SpatialValueUtil.isDecimal(x)) {
			log.warn("Latitude 1 is not a decimal " + list);
			return null;
		}
		Double latitude1 = Double.parseDouble(x.getLiteralLexicalForm());

		idx++;

		x = list.get(idx);
		if (!x.isLiteral()) {
			log.warn("Longitude 1 is not a literal " + list);
			return null;
		}
		if (!SpatialValueUtil.isDecimal(x)) {
			log.warn("Longitude 1 is not a decimal " + list);
			return null;
		}
		Double longtitude1 = Double.parseDouble(x.getLiteralLexicalForm());

		idx++;

		x = list.get(idx);
		if (!x.isLiteral()) {
			log.warn("Latitude 2 is not a literal " + list);
			return null;
		}
		if (!SpatialValueUtil.isDecimal(x)) {
			log.warn("Latitude 2 is not a decimal " + list);
			return null;
		}
		Double latitude2 = Double.parseDouble(x.getLiteralLexicalForm());

		idx++;

		x = list.get(idx);
		if (!x.isLiteral()) {
			log.warn("Longitude 2 is not a literal " + list);
			return null;
		}
		if (!SpatialValueUtil.isDecimal(x)) {
			log.warn("Longitude 2 is not a decimal " + list);
			return null;
		}
		Double longtitude2 = Double.parseDouble(x.getLiteralLexicalForm());

		idx++;
		int limit =-1;
		
		if (idx < list.size()) {
			x = list.get(idx);

			if (!x.isLiteral()) {
				log.warn("Limit is not a literal " + list);
				return null;
			}

			LiteralLabel lit = x.getLiteral();

			if (!XSDDatatype.XSDinteger.isValidLiteral(lit)) {
				log.warn("Limit is not an integer " + list);
				return null;
			}

			int v = NodeFactoryExtra.nodeToInt(x);
			limit = (v < 0) ? -1 : v;

			idx++;
			if (idx < list.size()) {
				log.warn("Limit is not the last parameter " + list);
				return null;
			}
		}
		
		SpatialMatch match = new SpatialMatch(latitude1, longtitude1,
				latitude2, longtitude2, limit, getSpatialOperation());

		if (log.isDebugEnabled())
			log.debug("Trying SpatialMatch: " + match.toString());
		return match;
	}


}
