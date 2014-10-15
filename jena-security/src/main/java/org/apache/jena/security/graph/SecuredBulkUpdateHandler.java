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
package org.apache.jena.security.graph;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.impl.SecuredItem;

/**
 * The interface for secured BulkUpdateHanlder instances.
 * 
 * Use the SecuredBulkUpdateHandler.Factory to create instances
 */
public interface SecuredBulkUpdateHandler extends BulkUpdateHandler,
		SecuredItem
{

	/**
	 * @sec.graph Update
	 * @sec.triple Create
	 * @throws AccessDeniedException
	 */
	@Override
	public void add( final Graph g ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public void add( final Graph g, final boolean withReifications )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public void add( final Iterator<Triple> it ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public void add( final List<Triple> triples ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public void add( final Triple[] triples ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	public void delete( final Graph g ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public void delete( final Graph g, final boolean withReifications )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public void delete( final Iterator<Triple> it )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public void delete( final List<Triple> triples )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public void delete( final Triple[] triples ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	public void remove( final Node s, final Node p, final Node o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete
	 * @throws AccessDeniedException
	 */
	@Override
	public void removeAll() throws AccessDeniedException;

}
