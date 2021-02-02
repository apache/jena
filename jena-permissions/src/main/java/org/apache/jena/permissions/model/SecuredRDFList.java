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
package org.apache.jena.permissions.model;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;

public interface SecuredRDFList extends RDFList, SecuredResource {

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple(SecNode.FUTURE, listFirst(), value)
	 * @sec.triple Create SecTriple(SecNode.FUTURE, listFirst(), listNil())
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public void add(final RDFNode value) throws UpdateDeniedException,
			AddDeniedException, AuthenticationRequiredException;

	/**
	 * Resulting list will contain the readable nodes from this list
	 * concatenated with nodes
	 * 
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public RDFList append(final Iterator<? extends RDFNode> nodes)
			throws UpdateDeniedException, AddDeniedException,
			AuthenticationRequiredException;

	/**
	 * Resulting list will contain the readable nodes from this list
	 * concatenated with the list argument
	 * 
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public RDFList append(final RDFList list) throws UpdateDeniedException,
			AddDeniedException, AuthenticationRequiredException;

	/**
	 * Uses the security settings for the application of the function calls.
	 * Thus if the function reads data the Read must be allowed, etc.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read (to be included in the calculation)
	 * @sec.triple other permissions required by the function.
	 * @throws ReadDeniedException
	 *             graph Read or other permissions are not met
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public void apply(final ApplyFn fn) throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * This method is intended to provide the capabilities to apply functions
	 * that need to do more than read the graph.
	 * 
	 * If the user does not have constraints access to the item in the list the
	 * item is not included in the function.
	 * 
	 * @param constraints
	 *            The permissions the user must have on the items in the list.
	 * @param fn
	 *            The function to apply.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read and constraints
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public void apply(Set<Action> constraints, final ApplyFn fn)
			throws ReadDeniedException, AuthenticationRequiredException;

	/**
	 * @sec.triple Read for triples containing the returned RDFNodes.
	 * @return List<SecuredRDFNode>
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public List<RDFNode> asJavaList() throws AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, node ) for each
	 *             node in nodes.
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public void concatenate(final Iterator<? extends RDFNode> nodes)
			throws UpdateDeniedException, AddDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, node ) for each
	 *             node in list.
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public void concatenate(final RDFList list) throws UpdateDeniedException,
			AddDeniedException, AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public SecuredRDFList cons(final RDFNode value)
			throws UpdateDeniedException, AddDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public boolean contains(final RDFNode value) throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * Creates a copy of this list comprising the readable elements of this
	 * list.
	 * 
	 * @sec.graph Read to read the items to copy
	 * @sec.triple Read on each triple to be read.
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
	 * @throws ReadDeniedException
	 * @throws AddDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public SecuredRDFList copy() throws ReadDeniedException,
			AddDeniedException, AuthenticationRequiredException;

	/**
	 * Answer the node that is the i'th element of the list, assuming that the
	 * head is item zero. If the list is too short to have an i'th element,
	 * throws a ListIndexException.
	 * 
	 * List may be shortened by security constraints.
	 * 
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public SecuredRDFNode get(final int i) throws ReadDeniedException,
			EmptyListException, ListIndexException, InvalidListException,
			AuthenticationRequiredException;

	/**
	 * The value that is at the head of the list.
	 * 
	 * head may be shifted by security constraints.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public RDFNode getHead() throws ReadDeniedException, EmptyListException,
			AuthenticationRequiredException;

	/**
	 * The value that is at the tail of the list.
	 * 
	 * tail may be shifted by security constraints.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public SecuredRDFList getTail() throws ReadDeniedException,
			EmptyListException, ListIndexException, InvalidListException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public String getValidityErrorMessage() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public int indexOf(final RDFNode value) throws ReadDeniedException,
			EmptyListException, ListIndexException, InvalidListException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public int indexOf(final RDFNode value, final int start)
			throws ReadDeniedException, EmptyListException, ListIndexException,
			InvalidListException, AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public boolean isEmpty() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public boolean isValid() throws ReadDeniedException, EmptyListException,
			ListIndexException, InvalidListException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value to be included in the
	 *             result.
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public ExtendedIterator<RDFNode> iterator() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read + requiredPerms for triple containing value to be
	 *             included in the result.
	 * @throws ReadDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public ExtendedIterator<RDFNode> iterator(Set<Action> requiredPerms)
			throws ReadDeniedException, EmptyListException, ListIndexException,
			InvalidListException, AuthenticationRequiredException;

	/**
	 * Only readable triples will be passed to the function. If the function
	 * does any action other than read those permissions must also be granted.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triple containing value.
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public Object reduce(final ReduceFn fn, final Object initial)
			throws ReadDeniedException, EmptyListException, ListIndexException,
			InvalidListException, AuthenticationRequiredException;

	/**
	 * Only readable triples will be passed to the function. In addition, only
	 * triples that pass the requiredActions tests will be passed to the
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
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public Object reduce(Set<Action> requiredActions, final ReduceFn fn,
			final Object initial) throws ReadDeniedException,
			EmptyListException, ListIndexException, InvalidListException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete for triple containing value.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public RDFList remove(final RDFNode val) throws UpdateDeniedException,
			DeleteDeniedException, AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete for the head triple.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public RDFList removeHead() throws UpdateDeniedException,
			DeleteDeniedException, AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Delete for triple containing value.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public void removeList() throws UpdateDeniedException,
			DeleteDeniedException, AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Update for triple i, and value.
	 * @throws UpdateDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public SecuredRDFNode replace(final int i, final RDFNode value)
			throws UpdateDeniedException, AuthenticationRequiredException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read for triples included in the comparison.
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public boolean sameListAs(final RDFList list) throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create for triple containing value.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public SecuredRDFNode setHead(final RDFNode value)
			throws UpdateDeniedException, DeleteDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public void setStrict(final boolean strict) throws UpdateDeniedException,
			AuthenticationRequiredException;

	/**
	 * Size may be modified by security constraionts.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read for triples counted in the result.
	 * @throws ReadDeniedException
	 * @throws EmptyListException
	 * @throws ListIndexException
	 * @throws InvalidListException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public int size() throws ReadDeniedException,
			AuthenticationRequiredException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create for triple containing value.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	@Override
	public SecuredRDFList with(final RDFNode value)
			throws UpdateDeniedException, DeleteDeniedException,
			AuthenticationRequiredException;

}
