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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation;

/**
 * A Transformer that applies a reordering to all BGPs and Quad Patterns present in the algebra
 * <p>
 * This transformer may be slightly naive in that it only leverages the 
 * {@link ReorderTransformation#reorder(BasicPattern)} method and does not use 
 * the {@link ReorderTransformation#reorderIndexes(BasicPattern)} method at all
 * so may not achieve the best reordering
 * </p>
 */
public class TransformReorder extends TransformCopy {
	
	private ReorderTransformation reorder;
	
	/**
	 * Creates a Transformer that uses the fixed reordering provided by {@link ReorderLib#fixed()}
	 */
	public TransformReorder() {
		this(ReorderLib.fixed());
	}
	
	/**
	 * Creates a Transformer that uses the given reordering
	 */
	public TransformReorder(ReorderTransformation reorder) {
		if (reorder == null) throw new IllegalArgumentException("reorder cannot be null");
		this.reorder = reorder;
	}

	/**
	 * Transforms BGPs with the reordering
	 */
	@Override
	public Op transform(OpBGP opBGP) {
		BasicPattern pattern = opBGP.getPattern();
		if ( pattern.size() < 2 ) 
		    return opBGP ; 
		BasicPattern pattern2 = reorder.reorder(pattern);
		return new OpBGP(pattern2);
	}

	/**
	 * Transforms Quad Patterns with the reordering
	 */
	@Override
	public Op transform(OpQuadPattern opQuadPattern) {
		BasicPattern pattern = opQuadPattern.getBasicPattern();
        if ( pattern.size() < 2 ) 
            return opQuadPattern ; 
		BasicPattern pattern2 = reorder.reorder(pattern);
		return new OpQuadPattern(opQuadPattern.getGraphNode(), pattern2);
	}

}

