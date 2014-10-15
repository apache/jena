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

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.spatial.DatasetGraphSpatial ;
import org.apache.jena.query.spatial.SpatialIndex ;
import org.apache.jena.query.spatial.SpatialQuery ;
import org.apache.lucene.spatial.query.SpatialOperation ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterExtendByVar ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSlice ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase ;
import com.hp.hpl.jena.sparql.util.IterLib ;

public abstract class SpatialOperationPFBase extends PropertyFunctionBase {

	private static Logger log = LoggerFactory.getLogger(SpatialOperationPFBase.class);

	protected SpatialIndex server = null;
	private boolean warningIssued = false;

	public SpatialOperationPFBase() {
	}
	
	@Override
	public void build(PropFuncArg argSubject, Node predicate,
			PropFuncArg argObject, ExecutionContext execCxt) {
		super.build(argSubject, predicate, argObject, execCxt);
		DatasetGraph dsg = execCxt.getDataset();
		server = chooseTextIndex(dsg);
	}

	protected SpatialIndex chooseTextIndex(DatasetGraph dsg) {
		Object obj = dsg.getContext().get(SpatialQuery.spatialIndex);

		if (obj != null) {
			try {
				return (SpatialIndex) obj;
			} catch (ClassCastException ex) {
				Log.warn(SpatialOperationWithCircleBase.class, "Context setting '"
						+ SpatialQuery.spatialIndex + "'is not a SpatialIndex");
			}
		}

		if (dsg instanceof DatasetGraphSpatial) {
			DatasetGraphSpatial x = (DatasetGraphSpatial) dsg;
			return x.getSpatialIndex();
		}
		Log.warn(
				SpatialOperationWithCircleBase.class,
				"Failed to find the spatial index : tried context and as a spatial-enabled dataset");
		return null;
	}

	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
		if (server == null) {
			if (!warningIssued) {
				Log.warn(getClass(), "No spatial index - no spatial search performed");
				warningIssued = true;
			}
			// Not a text dataset - no-op
			return IterLib.result(binding, execCxt);
		}

		DatasetGraph dsg = execCxt.getDataset();
		
        argSubject = Substitute.substitute(argSubject, binding) ;
        argObject = Substitute.substitute(argObject, binding) ;
		
		if (!argSubject.isNode())
			throw new InternalErrorException("Subject is not a node (it was earlier!)");

		Node s = argSubject.getArg();

		if (s.isLiteral())
			// Does not match
			return IterLib.noResults(execCxt);

		SpatialMatch match = objectToStruct(argObject);

		if (match == null) {
			// can't match
			return IterLib.noResults(execCxt);
		}

		// ----

		QueryIterator qIter = (Var.isVar(s)) ? variableSubject(binding, s,
				match, execCxt) : concreteSubject(binding, s, match, execCxt);
		if (match.getLimit() >= 0)
			qIter = new QueryIterSlice(qIter, 0, match.getLimit(), execCxt);
		return qIter;
	}

	private QueryIterator variableSubject(Binding binding, Node s,
			SpatialMatch match, ExecutionContext execCxt) {

		Var v = Var.alloc(s);
		List<Node> r = query(match);
		// Make distinct. Note interaction with limit is imperfect
		r = Iter.iter(r).distinct().toList();
		QueryIterator qIter = new QueryIterExtendByVar(binding, v,
				r.iterator(), execCxt);
		return qIter;
	}

	private QueryIterator concreteSubject(Binding binding, Node s,
			SpatialMatch match, ExecutionContext execCxt) {
		if (!s.isURI()) {
			log.warn("Subject not a URI: " + s);
			return IterLib.noResults(execCxt);
		}

		List<Node> x = query(match);
		if (x == null || !x.contains(s))
			return IterLib.noResults(execCxt);
		else
			return IterLib.result(binding, execCxt);
	}

	private List<Node> query(SpatialMatch match) {

		return server.query(match.getShape(), match.getLimit(),
				match.getSpatialOperation());
	}

	/** Deconstruct the node or list object argument and make a SpatialMatch */
	protected abstract SpatialMatch objectToStruct(PropFuncArg argObject);
	
	protected abstract SpatialOperation getSpatialOperation();
}
