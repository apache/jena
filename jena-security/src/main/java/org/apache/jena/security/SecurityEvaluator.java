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
package org.apache.jena.security;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * SecurityEvaluator.
 * <p>
 * The security evaluator is the link between the graph security system and an
 * external
 * security system. This interface specifies the methods that are required by
 * the graph
 * security system. It is assumed that the implementation will handle tracking
 * the current
 * user and will query some underlying data source to determine what actions the
 * user can
 * and can not take.
 * </p><p>
 * All questions of white listing or black listing will be handled in the concrete
 * implementation.
 * </p><p>
 * Implementations of this class should probably cache any evaluate calculations
 * as the evaluate methods are called frequently.  However, the underlying classes
 * do cache results within a single method check.
 * </p>
 * <p>
 * <dl>
 * <dt>Secured operations</dt>
 * <dd>The security system recognizes and secures each of the CRUD (Create, Read, Update and Delete)
 * operations as represented by the Action enumeration.</dd>
 * </dl>
 * <dl>
 * <dt>Levels of security</dt>
 * <dd>The security interfaces operates at two (2) levels: graph (or Model) and triple.
 * <p>At the the graph level the security evaluator may restrict CRUD access to the graph or model as a whole.
 * When evaluating the restriction, if the user it not permitted to perform the operation on the 
 * graph or model access is denied.  If the user is permitted any triple restrictions are evaluated.
 * </p><p>
 * At the triple level the security evaluator may restrict CRUD access to specific triples.  In order
 * to skip potentially expensive triple security checks the system will generally ask if the user is 
 * permitted the CRUD action on any triple.  This is represented by the SecTriple 
 * <code>(ANY, ANY, ANY)</code>.  
 * <ul>
 * <li>
 * If the system does not support triple level security the system should always return <code>true</code>.
 * </li>
 * If the system does support triple level security and is unable to verify that the user can execute
 * the CRUD action against any arbitrary triple the system should return <code>false</code>.
 * </li>
 * <li>See <code>SecNode.ANY</code>, <code>SecNode.FUTURE</code>, and <code>SecNode.VARIABLE</code>
 * for discussion of specifics of their respective usages.</li> 
 * </ul>
 * </p>
 * </dd>
 * </dl>
 * <dl>
 * <dt>
 * 
 * </p>
 */
public interface SecurityEvaluator
{
	/**
	 * Identifies a sepcific CRUD actions.
	 */
	static enum Action
	{
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
	 * A node in the evaluation.
	 * <p>
	 * A node with no value represents a node of that type but unknown
	 * exactitude. (e.g.
	 * <code>SecNode(URI,"")</code> is a URI but of unknown value. Useful for systems that
	 * restrict
	 * type creation.
	 * </p><p>
	 * <code>SecNode(Anonymous,"")</code> represents an anonymous node that will be created.
	 * Otherwise anonymous
	 * node values are the values within the secured graph.
	 * </p><p>
	 * An "Any" node type matches any node.
	 * </p>
	 */
	public static class SecNode implements Comparable<SecNode>
	{

		/**
		 * The types of nodes.
		 */
		public static enum Type
		{
			/**
			 * A URI type node
			 */
			URI, 
			/**
			 * A Literal node.
			 */
			Literal, 
			/**
			 * An anonymous node.  Also called a "blank" node.
			 */
			Anonymous, 
			/**
			 * Any node.
			 */
			Any
		}

		/**
		 * Matches any node in the security system.
		 * <p>
		 * Used in triple checks as follows:
		 * <dl>
		 * <dt><code>(ANY, ANY, ANY)</code>
		 * </dt><dd>Asks if the user may perform the action on any triple. 
		 * </dd>
		 * <dt><code>(X, ANY, ANY)</code>
		 * </dt><dd>Asks if the user may perform the action against
		 * any triple where X is the subject.
		 * </dd>
		 * <dt><code>(ANY, X, ANY)</code>
		 * </dt><dd>Asks if the user may perform the action against
		 * any triple where X is the predicate.
		 * </dd>
		 * <dt><code>(SecNode.ANY, SecNode.ANY, SecNode.X)</code>
		 * </dt><dd>Asks if if the user may perform the action against
		 * any triple where X is the object.
		 * </dd>
		 * </dl>
		 * The <code>ANY</code> may occur multiple times and may occur with the 
		 * <code>VARIABLE</code> and/or <code>FUTURE</code> nodes.
		 * </p>
		 */
		public static final SecNode ANY = new SecNode(Type.Any, "any");
		
		/**
		 * Indicates a variable in the triple.
		 * <p>
		 * </p>This differs from <code>ANY</code>
		 * in that the system is asking if there are any prohibitions not if the user 
		 * may perform. Thus queries with the VARIABLE type node should return <code>true</code>
		 * where <code>ANY</code> returns <code>false</code>.  In general this type is used in the 
		 * query to determine if triple level filtering of results must be performed.<p>
		 * </p><p> 
		 * <dl>
		 * <dt><code>(VARIABLE, X, Y )</code>
		 * </dt><dd>
		 * Asks if there are any prohibitions against the user seeing all subjects
		 * that have property X and object Y.
		 * </dd><dt>
		 * <code>(X, VARIABLE, Y )</code>
		 * </dt><dd>
		 * Asks if there are any prohibitions against the user seeing all predicates
		 * that have subject X and object Y.
		 * </dd><dt>
		 * <code>(X, Y, VARIABLE)</code>
		 * </dt><dd>
		 * Asks if there are any prohibitions against the user seeing all objects
		 * that have subject X and predicate Y.
		 * </dd>
		 * </dl>
		 * The <code>VARIABLE</code> may occur multiple times and may occur with the 
		 * <code>ANY</code> node.
		 * </p>
		 *  
		 */
		public static final SecNode VARIABLE = new SecNode(Type.Any, "variable");

		/**
		 * This is an anonymous node that will be created in the future.
		 * <p>
		 * FUTURE is used to check that an anonymous node may be created in
		 * as specific position in a triple.
		 * </p><p> 
		 * <dl>
		 * <dt><code>(FUTURE, X, Y )</code>
		 * </dt><dd>
		 * Asks if there the user may create an anonymous node 
		 * that has property X and object Y.
		 * </dd><dt>
		 * <code>(X, Y, FUTURE)</code>
		 * </dt><dd>
		 * Asks if there the user may create an anonymous node 
		 * that has subject X and property Y.
		 * </dd>
		 * </dl>
		 * The <code>FUTURE</code> may occur multiple times and may occur with the 
		 * <code>ANY</code> node.
		 * </p>
		 */
		public static final SecNode FUTURE = new SecNode(Type.Anonymous, "");

		private final Type type;
		private final String value;
		private Integer hashCode;

		/**
		 * Create a SecNode of the type and value.
		 * @param type The type of the node
		 * @param value The value of the node.  A null is interpreted as an empty string.
		 */
		public SecNode( final Type type, final String value )
		{
			this.type = type;
			this.value = value == null ? "" : value;
		}

		@Override
		public int compareTo( final SecNode node )
		{
			final int retval = type.compareTo(node.type);
			return retval == 0 ? value.compareTo(node.value) : retval;
		}

		@Override
		public boolean equals( final Object o )
		{
			if (o instanceof SecNode)
			{
				return this.compareTo((SecNode) o) == 0;
			}
			return false;
		}

		/**
		 * Get the type of the node.
		 * @return The type of the node.
		 */
		public Type getType()
		{
			return type;
		}

		/**
		 * Get the value of the node.
		 * @return the value of the node
		 */
		public String getValue()
		{
			return value;
		}

		@Override
		public int hashCode()
		{
			if (hashCode == null)
			{
				hashCode = new HashCodeBuilder().append(type).append(value)
						.toHashCode();
			}
			return hashCode;
		}

		@Override
		public String toString()
		{
			return String.format("[%s:%s]", getType(), getValue());
		}
	}

	/**
	 * An immutable triple of SecNodes.
	 */
	public static class SecTriple implements Comparable<SecTriple>
	{
		private final SecNode subject;
		private final SecNode predicate;
		private final SecNode object;
		private transient Integer hashCode;

		/**
		 * The triple of <code>(ANY, ANY, ANY)</code>.
		 */
		public static final SecTriple ANY = new SecTriple(SecNode.ANY,
				SecNode.ANY, SecNode.ANY);

		/**
		 * Create the sec triple
		 * @param subject The subject node.
		 * @param predicate The predicate node.
		 * @param object The object node.
		 * @throws IllegalArgumentException is any value is null.
		 */
		public SecTriple( final SecNode subject, final SecNode predicate,
				final SecNode object )
		{
			if (subject == null)
			{
				throw new IllegalArgumentException("Subject may not be null");
			}
			if (predicate == null)
			{
				throw new IllegalArgumentException("Predicate may not be null");
			}
			if (object == null)
			{
				throw new IllegalArgumentException("Object may not be null");
			}
			this.subject = subject;
			this.predicate = predicate;
			this.object = object;
		}

		@Override
		public int compareTo( final SecTriple o )
		{
			if (o == null)
			{
				return 1;
			}
			int retval = subject.compareTo(o.subject);
			if (retval == 0)
			{
				retval = predicate.compareTo(o.predicate);
			}
			return retval == 0 ? object.compareTo(o.object) : retval;
		}

		@Override
		public boolean equals( final Object o )
		{
			if (o instanceof SecTriple)
			{
				return this.compareTo((SecTriple) o) == 0;
			}
			return false;
		}

		/**
		 * @return the object node.
		 */
		public SecNode getObject()
		{
			return object;
		}

		/**
		 * @return the predicate node.
		 */
		public SecNode getPredicate()
		{
			return predicate;
		}

		/**
		 * @return the subject node.
		 */
		public SecNode getSubject()
		{
			return subject;
		}

		@Override
		public int hashCode()
		{
			if (hashCode == null)
			{
				hashCode = new HashCodeBuilder().append(object)
						.append(predicate).append(subject).toHashCode();
			}
			return hashCode;
		}

		@Override
		public String toString()
		{
			return String.format("( %s, %s, %s )", getSubject(),
					getPredicate(), getObject());
		}
	}

	/**
	 * A collection of utility functions for the SecurityEvaluator implementations.
	 */
	public static class Util
	{
		/**
		 * Return an array of actions as a set.
		 * <p>The order of the collection is preserved</p>
		 * @param actions The actions.
		 * @return The set of actions.
		 */
		public static Set<Action> asSet( final Action[] actions )
		{
			return Util.asSet(Arrays.asList(actions));
		}

		/**
		 * Return a collection of actions as a set.
		 * <p>The order of the collection is preserved</p>
		 * @param actions The collection of actions.
		 * @return The set of actions.
		 */
		public static Set<Action> asSet( final Collection<Action> actions )
		{
			if (actions instanceof Set)
			{
				return (Set<Action>) actions;
			}
			else
			{
				return new LinkedHashSet<Action>(actions);
			}
		}
	}

	/**
	 * Determine if the action is allowed on the graph.
	 * 
	 * @param action
	 *            The action to perform
	 * @param graphIRI
	 *            The IRI of the graph to check
	 * @return true if the action is allowed, false otherwise.
	 */
	public boolean evaluate( Action action, SecNode graphIRI );

	/**
	 * Determine if the action is allowed on the triple within the graph.
	 * <p>
	 * The evaluation should be performed in the following order:
	 * <ol>
	 * <li>
	 * If the triple contains a <code>VARIABLE</code> then this method must return <code>true</code> if
	 * there are any restrictions where the remaining nodes are either constants or <code>ANY</code>
	 * nodes.  This will force the system to use subsequent checks to verify access by substituting
	 * the value of the <code>VARIABLE</code>s. <em>If the system can not quickly verify the solution
	 * it is always acceptable to return <code>true</code>.</em>
	 * <li>
	 * Except as specified in the above check, if the triple contains an <code>ANY</code> then this 
	 * method must return <code>false</code> if
	 * there are any restrictions where the remaining nodes are held constant and the ANY
	 * node is allowed to vary.  This checks is used to avoid subsequent explicit triple checks.
	 * <em>If the system can not quickly verify the solution it is always
	 * acceptable to return <code>false</code>.</em>
	 * </li>
	 * <li>All other triples are explict triples and the system must determine if the user is
	 * permitted to perform the action on the triple. 
	 * If the triple contains a <code>FUTURE</code> node that node should be considered as 
	 * an anonymous or blank node that is not yet created.  It should only be used with 
	 * <code>Create</code> actions and is asking if the user may create a blank node in that
	 * position in the triple.</li>
	 * </ol>
	 * </p>
	 * @param action
	 *            The action to perform
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon.  May be <code>ANY</code>.
	 * @param triple
	 *            The triple to check
	 * @return true if the action is allowed, false otherwise.
	 * @throws IllegalArgumentException if any argument is null.
	 */
	public boolean evaluate( Action action, SecNode graphIRI, SecTriple triple );

	/**
	 * Determine if all actions are allowed on the graph.
	 * 
	 * @param actions
	 *            The set of actions to perform
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon.  May be <code>ANY</code>.
	 * @return true if all the actions are allowed, false otherwise.
	 * @throws IllegalArgumentException if any argument is null.
	 */
	public boolean evaluate( Set<Action> actions, SecNode graphIRI );

	/**
	 * Determine if all the actions are allowed on the triple within the graph.
	 * <p>
	 * See evaluate( Action, SecNode, SecTriple ) for discussion of evaluation strategy.
	 * </p>
	 * @param actions
	 *            The actions to perform.
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon.  May be <code>ANY</code>.
	 * @param triple
	 *            The triple to check
	 * @return true if all the actions are allowed, false otherwise.
	 * @throws IllegalArgumentException if any argument is null.
	 */
	public boolean evaluate( Set<Action> actions, SecNode graphIRI,
			SecTriple triple );

	/**
	 * Determine if any of the actions are allowed on the graph.
	 * 
	 * @param actions
	 *            The actions to perform
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon.  May be <code>ANY</code>.
	 * @return true true if any the actions are allowed, false otherwise.
	 * @throws IllegalArgumentException if any argument is null.
	 */
	public boolean evaluateAny( Set<Action> actions, SecNode graphIRI );

	/**
	 * Determine if any of the actions are allowed on the triple within the graph.
	 * <p>
	 * See evaluate( Action, SecNode, SecTriple ) for discussion of evaluation strategy.
	 * </p>
	 * 
	 * @param actions
	 *            The actions to check.
	 * @param graphIRI
	 *           The IRI of the graph to the action is being taken upon.  May be <code>ANY</code>.
	 * @param triple
	 *            The triple to check
	 * @return true if any the actions are allowed, false otherwise.
	 * @throws IllegalArgumentException if any argument is null.
	 */
	public boolean evaluateAny( Set<Action> actions, SecNode graphIRI,
			SecTriple triple );

	/**
	 * Determine if the user is allowed to update the "from" triple to the "to"
	 * triple.
	 * <p>
	 * Update is a special case since it modifies one triple to be another.  So the user must 
	 * have permissions to change the "from" triple into the "to" triple.
	 * 
	 * @param graphIRI
	 *            The IRI of the graph to the action is being taken upon.  May be <code>ANY</code>.
	 * @param from
	 *            The triple to be changed
	 * @param to
	 *            The value to change it to.
	 * @return true if the user may make the change, false otherwise.
	 * @throws IllegalArgumentException if any argument is null.
	 */
	public boolean evaluateUpdate( SecNode graphIRI, SecTriple from,
			SecTriple to );

	/**
	 * returns the current principal or null if there is no current principal.
	 * 
	 * All security evaluation methods use this method to determine who
	 * the call is being executed as. This allows subsystems (like the listener system) 
	 * to capture the current user
	 * and evaluate later calls in terms of that user.
	 * 
	 * @return The current principal
	 */
	public Principal getPrincipal();
}
