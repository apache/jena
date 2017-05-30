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

package org.apache.jena.query.spatial;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;

import org.locationtech.spatial4j.shape.Shape;

public class SpatialIndexContext {

	private final EntityDefinition defn;
	private final SpatialIndex indexer;
	private final Map<String, Set<SpatialPredicatePairValue>> spatialPredicatePairValues;

	public SpatialIndexContext(SpatialIndex indexer) {
		super();
		this.defn = indexer.getDocDef();
		this.indexer = indexer;
		this.spatialPredicatePairValues = new HashMap<>();
	}

	@SuppressWarnings("deprecation")
    public void index(Node g, Node s, Node p, Node o) {

		if (!o.isLiteral()) {
			return;
		}

		String x = SpatialQueryFuncs.subjectToString(s) ;
		Log.info(getClass(), "Subject: "+x) ;

		if (defn.isSpatialPredicate(p) && SpatialValueUtil.isDecimal(o)) {

			boolean isLat = defn.isLatitudePredicate(p);

			SpatialPredicatePair pair = defn.getSpatialPredicatePair(p);
			Set<SpatialPredicatePairValue> pairValues = spatialPredicatePairValues
					.get(x);
			if (pairValues == null) {
				pairValues = new HashSet<>();
				spatialPredicatePairValues.put(x, pairValues);
			}

			Iterator<SpatialPredicatePairValue> it = pairValues.iterator();
			SpatialPredicatePairValue toRemove = null;

			while (it.hasNext()) {
				SpatialPredicatePairValue pairValue = it.next();
				if (pairValue.getPair().equals(pair)) {
					Double theOtherValue = pairValue.getTheOtherValue(p);
					if (theOtherValue != null) {
						if (isLat) {
							indexer.add(x, SpatialQuery.ctx.makePoint(
									theOtherValue, 
									Double.parseDouble(o.getLiteralLexicalForm())));
						} else {
							indexer.add(x, SpatialQuery.ctx.makePoint(Double.parseDouble(o.getLiteralLexicalForm()),
									theOtherValue));
						}
						toRemove = pairValue;
					}
					break;
				}
			}
			if (toRemove != null) {
				pairValues.remove(toRemove);
				return;
			}

			SpatialPredicatePairValue toAdd = new SpatialPredicatePairValue(
					pair);
			toAdd.setValue(p, Double.parseDouble(o.getLiteralLexicalForm()));
			pairValues.add(toAdd);

		} else if (defn.isWKTPredicate(p) && SpatialValueUtil.isWKTLiteral(o.getLiteral())) {
            Shape shape = SpatialQuery.ctx.readShape(o.getLiteralLexicalForm());
			indexer.add(x, shape);
		}
	}
}
