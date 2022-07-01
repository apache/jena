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

import java.util.Set;

import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.rdf.model.EmptyListException;
import org.apache.jena.rdf.model.InvalidListException;
import org.apache.jena.rdf.model.ListIndexException;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;

public interface SecuredRDFList extends RDFList, SecuredResource {

    /**
     * Apply fn to all elements of the graph for which the user has the permissions.
     *
     * @param perms The permissions the user must have on the items in the list.
     * @param fn    The function to apply.
     *
     * @sec.graph Read
     * @sec.triple Read and constraints
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    public void apply(final Set<Action> perms, final ApplyFn fn)
            throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * Retrieve an iterator on the list for which the user has the specified
     * permissions.
     *
     * @param constraints the permissions required for the user to access the node.
     * @return an iterator on the items.
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then an empty iterator is
     *            returned.
     * @sec.triple Read for triple containing value to be included in the result.
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    public ExtendedIterator<RDFNode> iterator(final Set<Action> constraints)
            throws ReadDeniedException, AuthenticationRequiredException;

    /**
     * Execute a reduct function across the list.
     * 
     * @param requiredActions the permission set required to execute the reduce.
     * @param fn              the reduction function.
     * @param initial         the initial accumulation value.
     * @return the accumulator after execution.
     * @sec.graph Read - Only readable triples will be passed to the function. if
     *            {@link SecurityEvaluator#isHardReadError()} is true and the user
     *            does not have read access then no items will be passed to the
     *            function.
     * @sec.triple Read for triple containing value to be included in the result.
     * @sec.triple Read for triple containing value.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    public Object reduce(final Set<Action> requiredActions, final ReduceFn fn, final Object initial)
            throws EmptyListException, ListIndexException, InvalidListException, ReadDeniedException,
            AuthenticationRequiredException;

}
