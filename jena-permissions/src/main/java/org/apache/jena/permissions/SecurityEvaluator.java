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
package org.apache.jena.permissions;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AuthenticationRequiredException;

import java.util.*;

/**
 * SecurityEvaluator.
 * <p>
 * The security evaluator is the link between the graph security system and an external
 * security system. This interface specifies the methods that are required by the graph
 * security system. It is assumed that the implementation will handle tracking the current
 * user and will query some underlying data source to determine what actions the user can
 * and can not take.
 * </p>
 * <p>
 * All questions of white listing or black listing will be handled in the concrete
 * implementation.
 * </p>
 * <p>
 * Implementations of this class should probably cache any evaluate calculations as the
 * evaluate methods are called frequently. However, the underlying classes do cache
 * results within a single method check.
 * </p>
 * <p>
 * <dl>
 * <dt>Secured operations</dt>
 * <dd>The security system recognizes and secures each of the CRUD (Create, Read, Update
 * and Delete) operations as represented by the Action enumeration.</dd>
 * <dt>Levels of security</dt>
 * <dd>The security interfaces operates at two (2) levels: graph (or Model) and triple.
 * <p>
 * At the the graph level the security evaluator may restrict CRUD access to the graph or
 * model as a whole. When evaluating the restriction, if the user it not permitted to
 * perform the operation on the graph or model access is denied. If the user is permitted
 * any triple restrictions are evaluated.
 * </p>
 * <p>
 * At the triple level the security evaluator may restrict CRUD access to specific
 * triples. In order to skip potentially expensive triple security checks the system will
 * generally ask if the user is permitted the CRUD action on any triple. This is
 * represented by the SecTriple <code>(ANY, ANY, ANY)</code>.
 * </p>
 * <ul>
 * <li>If the system does not support triple level security the system should always
 * return <code>true</code>.</li> If the system does support triple level security and is
 * unable to verify that the user can execute the CRUD action against any arbitrary triple
 * the system should return <code>false</code>.</li>
 * <li>See <code>Node.ANY</code>, <code>SecurityEvaluator.FUTURE</code>, and
 * <code>SecurityEvaluator.VARIABLE</code> for discussion of specifics of their respective
 * usages.</li>
 * </ul>
 * </dd>
 * </dl>
 */
public interface SecurityEvaluator {
	/**
	 * Identifies a sepcific CRUD actions.
	 */
	static enum Action {
		/**
		 * Allow creation of the object in question.
		 */
		Create,
		/**
		 * Allow the user to read the object in question.
		 */
		Read,
		/**
		 * Allow the user to update the object in question
		 */
		Update,
		/**
		 * Allow the user to delete the object in question.
		 */
		Delete

	}

	/**
	 * A collection of utility functions for the SecurityEvaluator
	 * implementations.
	 */
	public static class Util {
		/**
		 * Return an array of actions as a set.
		 * <p>
		 * The order of the collection is preserved
		 * </p>
		 * 
		 * @param actions
		 *            The actions.
		 * @return The set of actions.
		 */
		public static Set<Action> asSet(final Action[] actions) {
			return Util.asSet(Arrays.asList(actions));
		}

		/**
		 * Return a collection of actions as a set.
		 * <p>
		 * The order of the collection is preserved
		 * </p>
		 * 
		 * @param actions
		 *            The collection of actions.
		 * @return The set of actions.
		 */
		public static Set<Action> asSet(final Collection<Action> actions) {
			if (actions instanceof Set) {
				return (Set<Action>) actions;
			} else {
				return new LinkedHashSet<>(actions);
			}
		}
	}

	/**
	 * Indicates a variable in the triple.
	 * <p>
	 * </p>
	 * This differs from <code>ANY</code> in that the system is asking if there
	 * are any prohibitions not if the user may perform. Thus queries with the
	 * VARIABLE type node should return <code>true</code> where <code>ANY</code>
	 * returns <code>false</code>. In general this type is used in the query to
	 * determine if triple level filtering of results must be performed.
	 * <p>
	 * </p>
	 * <p>
	 * <dl>
	 * <dt><code>(VARIABLE, X, Y )</code></dt>
	 * <dd>
	 * Asks if there are any prohibitions against the user seeing all subjects
	 * that have property X and object Y.</dd>
	 * <dt>
	 * <code>(X, VARIABLE, Y )</code></dt>
	 * <dd>
	 * Asks if there are any prohibitions against the user seeing all predicates
	 * that have subject X and object Y.</dd>
	 * <dt>
	 * <code>(X, Y, VARIABLE)</code></dt>
	 * <dd>
	 * Asks if there are any prohibitions against the user seeing all objects
	 * that have subject X and predicate Y.</dd>
	 * </dl>
	 * The <code>VARIABLE</code> may occur multiple times and may occur with the
	 * <code>ANY</code> node.
	 * </p>
	 * 
	 */
	public static final Node VARIABLE = NodeFactory
			.createBlankNode("urn:jena-permissions:VARIABLE");

	/**
	 * This is a blank (anonymous) node that will be created in the future.
	 * <p>
	 * FUTURE is used to check that a blank node may be created in as specific
	 * position in a triple.
	 * </p>
	 * <p>
	 * <dl>
	 * <dt><code>(FUTURE, X, Y )</code></dt>
	 * <dd>
	 * Asks if there the user may create a blank node that has property X and
	 * object Y.</dd>
	 * <dt>
	 * <code>(X, Y, FUTURE)</code></dt>
	 * <dd>
	 * Asks if there the user may create a blank node that has subject X and
	 * property Y.</dd>
	 * </dl>
	 * The <code>FUTURE</code> may occur multiple times and may occur with the
	 * <code>ANY</code> node.
	 * </p>
	 */
	public static final Node FUTURE = NodeFactory
			.createBlankNode("urn:jena-permissions:FUTURE");

	/**
	 * Determine if the action is allowed on the graph.
	 *
	 * @param principal
	 *            The principal that is attempting the action.
	 *
	 * @param action
	 *            The action to perform
	 * @param graphIRI
	 *            The IRI of the graph to check
	 * @return true if the action is allowed, false otherwise.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean evaluate(Object principal, Action action, Node graphIRI)
			throws AuthenticationRequiredException;

	/**
	 * Determine if the action is allowed on the triple within the graph.
	 * <p>
	 * The evaluation should be performed in the following order:
	 * <ol>
	 * <li>
	 * If the triple contains a <code>VARIABLE</code> then this method must
	 * return <code>true</code> if there are any restrictions where the
	 * remaining nodes are either constants or <code>ANY</code> nodes. This will
	 * force the system to use subsequent checks to verify access by
	 * substituting the value of the <code>VARIABLE</code>s.
	 * <em>If the system can not quickly verify the solution
	 * it is always acceptable to return <code>true</code>.</em>
	 * <li>
	 * Except as specified in the above check, if the triple contains an
	 * <code>ANY</code> then this method must return <code>false</code> if there
	 * are any restrictions where the remaining nodes are held constant and the
	 * ANY node is allowed to vary. This checks is used to avoid subsequent
	 * explicit triple checks.
	 * <em>If the system can not quickly verify the solution it is always
	 * acceptable to return <code>false</code>.</em></li>
	 * <li>All other triples are explicit triples and the system must determine
	 * if the user is permitted to perform the action on the triple. If the
	 * triple contains a <code>FUTURE</code> node that node should be considered
	 * as an anonymous or blank node that is not yet created. It should only be
	 * used with <code>Create</code> actions and is asking if the user may
	 * create a blank node in that position in the triple.</li>
	 * </ol>
	 * </p>
	 *
	 * @param principal
	 *            The principal that is attempting the action.
	 *
	 * @param action
	 *            The action to perform
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon. May be
	 *            <code>ANY</code>.
	 * @param triple
	 *            The triple to check
	 * @return true if the action is allowed, false otherwise.
	 * @throws IllegalArgumentException
	 *             if any argument is null.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean evaluate(Object principal, Action action, Node graphIRI,
			Triple triple) throws AuthenticationRequiredException;

	/**
	 * Determine if all actions are allowed on the graph.
	 *
	 * @param principal
	 *            The principal that is attempting the action.
	 *
	 * @param actions
	 *            The set of actions to perform
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon. May be
	 *            <code>ANY</code>.
	 * @return true if all the actions are allowed, false otherwise.
	 * @throws IllegalArgumentException
	 *             if any argument is null.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public default boolean evaluate(Object principal, Set<Action> actions, Node graphIRI)
			throws AuthenticationRequiredException {
		return actions.stream().allMatch(action -> evaluate(principal, action, graphIRI));
	}

	/**
	 * Determine if all the actions are allowed on the triple within the graph.
	 * <p>
	 * See evaluate( Action, Node, Triple ) for discussion of evaluation
	 * strategy.
	 * </p>
	 * 
	 * @param actions
	 *            The actions to perform.
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon. May be
	 *            <code>ANY</code>.
	 * @param triple
	 *            The triple to check
	 * @return true if all the actions are allowed, false otherwise.
	 * @throws IllegalArgumentException
	 *             if any argument is null.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public default boolean evaluate(Object principal, Set<Action> actions,
			Node graphIRI, Triple triple)
			throws AuthenticationRequiredException {
		return actions.stream().allMatch(action -> evaluate(principal, action, graphIRI));
	}

	/**
	 * Determine if any of the actions are allowed on the graph.
	 *
	 * @param principal
	 *            The principal that is attempting the action.
	 *
	 * @param actions
	 *            The actions to perform
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon. May be
	 *            <code>ANY</code>.
	 * @return true true if any the actions are allowed, false otherwise.
	 * @throws IllegalArgumentException
	 *             if any argument is null.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public default boolean evaluateAny(Object principal, Set<Action> actions,
			Node graphIRI) throws AuthenticationRequiredException {
		return actions.stream().anyMatch(action -> evaluate(principal, action, graphIRI));
	}

	/**
	 * Determine if any of the actions are allowed on the triple within the
	 * graph.
	 * <p>
	 * See evaluate( Action, Node, Triple ) for discussion of evaluation
	 * strategy.
	 * </p>
	 *
	 * @param principal
	 *            The principal that is attempting the action.
	 *
	 * @param actions
	 *            The actions to check.
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon. May be
	 *            <code>ANY</code>.
	 * @param triple
	 *            The triple to check
	 * @return true if any the actions are allowed, false otherwise.
	 * @throws IllegalArgumentException
	 *             if any argument is null.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public default boolean evaluateAny(Object principal, Set<Action> actions,
			Node graphIRI, Triple triple)
			throws AuthenticationRequiredException {
		return actions.stream().anyMatch(action -> evaluate(principal, action, graphIRI, triple));
	}

	/**
	 * Determine if the user is allowed to update the "from" triple to the "to"
	 * triple.
	 * <p>
	 * Update is a special case since it modifies one triple to be another. So
	 * the user must have permissions to change the "from" triple into the "to"
	 * triple.
	 *
	 * @param principal
	 *            The principal that is attempting the action.
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon. May be
	 *            <code>ANY</code>.
	 * @param from
	 *            The triple to be changed
	 * @param to
	 *            The value to change it to.
	 * @return true if the user may make the change, false otherwise.
	 * @throws IllegalArgumentException
	 *             if any argument is null.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public default boolean evaluateUpdate(Object principal, Node graphIRI, Triple from,
			Triple to) throws AuthenticationRequiredException {
		return evaluate(principal, Action.Delete, graphIRI, from) && evaluate(principal, Action.Create, graphIRI, to);
	}

	/**
	 * returns the current principal or null if there is no current principal.
	 *
	 * All security evaluation methods use this method to determine who the call
	 * is being executed as. This allows subsystems (like the listener system)
	 * to capture the current user and evaluate later calls in terms of that
	 * user.
	 *
	 * @return The current principal
	 */
	public Object getPrincipal();

	/**
	 * Returns true if the principal is recognized as an authenticated principal
	 * by the underlying authentication mechanism.
	 * 
	 * This is to handle the case where an authentication mechanism returns a
	 * non-null object to indicate a non-authenticated principal. (e.g. Shiro).
	 * 
	 * The principal is guaranteed to have been the return value from an earlier
	 * getPrincipal() call.
	 * 
	 * @param principal
	 *            The principal to check.
	 * @return true if authenticated, false if not.
	 */
	public boolean isPrincipalAuthenticated(Object principal);
}
