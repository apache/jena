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
package org.apache.jena.arq.querybuilder.updatebuilder;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * An interface that defines a holder of quads.
 *
 */
public interface QuadHolder {

	/**
	 * Get an extended iterator over the quads this holder holds.
	 * @return the extended iterator.
	 */
	public ExtendedIterator<Quad> getQuads();
	
	/**
	 * Apply values to the variables in the quads held by this holder.
	 * May return this holder or a new holder instance.
	 * @param values the values to set.
	 * @return a QuadHolder in which the variables have been replaced.
	 */
	public QuadHolder setValues(Map<Var, Node> values);
}
