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
package org.apache.jena.security.model;

import com.hp.hpl.jena.rdf.model.EmptyListException;
import com.hp.hpl.jena.rdf.model.InvalidListException;
import com.hp.hpl.jena.rdf.model.ListIndexException;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.security.AccessDeniedException;
import org.apache.jena.security.SecurityEvaluator.Action;

public interface SecuredRDFList extends RDFList, SecuredResource
{

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple(SecNode.FUTURE, listFirst(), value)
	 * @sec.triple Create SecTriple(SecNode.FUTURE, listFirst(), listNil())
	 * @throws AccessDeniedException
	 */
	@Override
	public void add( final RDFNode value ) throws AccessDeniedException;

	/**
	 * Resulting list will contain the readable nodes from this list
	 * concatenated with nodes
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
	 */
	@Override
	public RDFList append( final Iterator<? extends RDFNode> nodes )
			throws AccessDeniedException;

	/**
	 * Resulting list will contain the readable nodes from this list
	 * concatenated
	 * with the list argument
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
	 */
	@Override
	public RDFList append( final RDFList list ) throws AccessDeniedException;

	/**
	 * Uses the security settings for the application of the function calls.
	 * Thus if the function reads data the Read must be allowed, etc.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read (to be included in the calculation)
	 * @sec.triple other permissions required by the function.
	 * @throws AccessDeniedException
	 *             graph Read or other permissions are not met
	 */
	@Override
	public void apply( final ApplyFn fn ) throws AccessDeniedException;

	/**
	 * This method is intended to provide the capabilities to apply functions
	 * that
	 * need to do more than read the graph.
	 * 
	 * If the user does not have constraints access to the item in the list the
	 * item
	 * is not included in the function.
	 * 
	 * @param constraints
	 *            The permissions the user must have on the items in the list.
	 * @param fn
	 *            The function to apply.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read and constraints
	 * @throws AccessDeniedException
	 */
	public void apply( Set<Action> constraints, final ApplyFn fn )
			throws AccessDeniedException;

	/**
	 * @sec.triple Read for triples containing the returned RDFNodes.
	 * @return List<SecuredRDFNode>
	 */
	@Override
	public List<RDFNode> asJavaList();

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, node ) for each
	 *            node in
	 *            nodes.
	 * @throws AccessDeniedException
	 */
	@Override
	public void concatenate( final Iterator<? extends RDFNode> nodes )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, node ) for each
	 *            node in
	 *            list.
	 * @throws AccessDeniedException
	 */
	@Override
	public void concatenate( final RDFList list ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredRDFList cons( final RDFNode value )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final RDFNode value ) throws AccessDeniedException;

	/**
	 * Creates a copy of this list comprising the readable elements of this
	 * list.
	 * @sec.graph Read to read the items to copy
	 * @sec.triple Read on each triple to be read.
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
	 */
	@Override
	public SecuredRDFList copy();

	/**
	 * Answer the node that is the i'th element of the list, assuming that the
	 * head is item zero. If the list is too
	 * short to have an i'th element, throws a ListIndexException.
	 * 
	 * List may be shortened by security constraints.
	 * 
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public SecuredRDFNode get( final int i ) throws AccessDeniedException,
			EmptyListException, ListIndexException, InvalidListException;

	/**
	 * The value that is at the head of the list.
	 * 
	 * head may be shifted by security constraints.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 */
	@Override
	public RDFNode getHead() throws AccessDeniedException, EmptyListException;

	/**
	 * The value that is at the tail of the list.
	 * 
	 * tail may be shifted by security constraints.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public SecuredRDFList getTail() throws AccessDeniedException,
			EmptyListException, ListIndexException, InvalidListException;

	/**
	 * @sec.graph Read
	 */
	@Override
	public String getValidityErrorMessage() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public int indexOf( final RDFNode value ) throws AccessDeniedException,
			EmptyListException, ListIndexException, InvalidListException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public int indexOf( final RDFNode value, final int start )
			throws AccessDeniedException, EmptyListException,
			ListIndexException, InvalidListException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean isEmpty() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean isValid() throws AccessDeniedException, EmptyListException,
			ListIndexException, InvalidListException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value to be included in the result.
	 * @throws AccessDeniedException
	 */
	@Override
	public ExtendedIterator<RDFNode> iterator() throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read + requiredPerms for triple containing value to be
	 *            included in the result.
	 * @throws AccessDeniedException
	 */
	public ExtendedIterator<RDFNode> iterator( Set<Action> requiredPerms )
			throws AccessDeniedException, EmptyListException,
			ListIndexException, InvalidListException;

	/**
	 * Only readable triples will be passed to the function. If the function
	 * does
	 * any action other than read those permissions must also be granted.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public Object reduce( final ReduceFn fn, final Object initial )
			throws AccessDeniedException, EmptyListException,
			ListIndexException, InvalidListException;

	/**
	 * Only readable triples will be passed to the function. In addition,
	 * only triples that pass the requiredActions tests will be passed to the
	 * function.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @param requiredActions
	 *            The set of permission (in addition to Read) that the user must
	 *            have
	 * @param fn
	 *            The reduction function
	 * @param initial
	 *            The initial state for the ruduce value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	public Object reduce( Set<Action> requiredActions, final ReduceFn fn,
			final Object initial ) throws AccessDeniedException,
			EmptyListException, ListIndexException, InvalidListException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete for triple containing value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public RDFList remove( final RDFNode val ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete for all triples.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	@Deprecated
	public void removeAll() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete for the head triple.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public RDFList removeHead() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete for triple containing value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public void removeList() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update for triplie i, and value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public SecuredRDFNode replace( final int i, final RDFNode value )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triples included in the comparison.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public boolean sameListAs( final RDFList list )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create for triple containing value.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public SecuredRDFNode setHead( final RDFNode value )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public void setStrict( final boolean strict ) throws AccessDeniedException;

	/**
	 * Size may be modified by security constraionts.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triples counted in the result.
	 * @throws AccessDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 */
	@Override
	public int size() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create for triple containing value.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredRDFList with( final RDFNode value )
			throws AccessDeniedException;

}
