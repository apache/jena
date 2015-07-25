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
package org.apache.jena.permissions.graph;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphStatisticsHandler ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.shared.AddDeniedException ;
import org.apache.jena.shared.DeleteDeniedException ;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * The interface for secured Graph instances.
 * 
 * Use the SecuredGraph.Factory to create instances
 */
public interface SecuredGraph extends Graph, SecuredItem
{

	/**
	 * @sec.graph Update
	 * @sec.triple Create
	 * @throws AddDeniedException
	 */
	@Override
	public void add( final Triple t ) throws AddDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final Node s, final Node p, final Node o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final Triple t ) throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws DeleteDeniedException
	 */
	@Override
	public void delete( final Triple t ) throws DeleteDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean dependsOn( final Graph other ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read, otherwise filtered from iterator.
	 * @throws ReadDeniedException
	 */
	@Override
	public ExtendedIterator<Triple> find( final Node s, final Node p,
			final Node o ) throws ReadDeniedException;

    /**
	 * @sec.graph Read
	 * @sec.triple Read, otherwise filtered from iterator.
	 * @throws ReadDeniedException
	 */
	@Override
	public ExtendedIterator<Triple> find( final Triple triple )
			throws ReadDeniedException;

	@Override
	public SecuredCapabilities getCapabilities();

	@Override
	public SecuredGraphEventManager getEventManager();

	/**
	 * Return the name of the graph.
	 * @return The name of the graph as a node.
	 */
	@Override
    public Node getModelNode();

	@Override
	public SecuredPrefixMapping getPrefixMapping();

	@Override
    public SecurityEvaluator getSecurityEvaluator();

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public GraphStatisticsHandler getStatisticsHandler()
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean isEmpty() throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean isIsomorphicWith( final Graph g )
			throws ReadDeniedException;


	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public int size() throws ReadDeniedException;
	
	/**
	 * @sec.graph Update
	 * @sec.triple Delete for every triple
	 * @throws DeleteDeniedException
	 */
	@Override
	public void clear() throws DeleteDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete (s, p, o )
	 * @throws DeleteDeniedException
	 */
	@Override
	public void remove( Node s, Node p, Node o ) throws DeleteDeniedException;

}
