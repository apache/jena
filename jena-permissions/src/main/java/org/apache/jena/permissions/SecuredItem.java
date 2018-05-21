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

import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AuthenticationRequiredException;

/**
 * The secured item interface is mixed into instances of secured objects by the
 * proxy. It provides the security context for the security checks as well as
 * several useful shorthand methods for common checks.
 */
public interface SecuredItem {

	/**
	 * Utilities for SecuredItem implementations.
	 */
	public static class Util {
		/**
		 * Secured items are equivalent if their security evaluators and
		 * modelIRIs are equal.
		 * 
		 * @param si1
		 *            A secured item to check
		 * @param si2
		 *            A second secured item to check
		 * @return true if si1 is equivalent to si2.
		 */
		public static boolean isEquivalent(final SecuredItem si1,
				final SecuredItem si2) {
			return si1.getSecurityEvaluator()
					.equals(si2.getSecurityEvaluator())
					&& si1.getModelIRI().equals(si2.getModelIRI());
		}

		public static String modelPermissionMsg(final Node modelURI) {
			return String.format("Model permissions violation: %s", modelURI);
		}

		public static String triplePermissionMsg(final Node modelURI) {
			return String.format("Triple permissions violation: %s", modelURI);
		}
	}

	/**
	 * @return true if the securedModel allows items to be created.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canCreate() throws AuthenticationRequiredException;

	/**
	 * Return true if the triple can be created. If any s,p or o is SecNode.ANY
	 * then this method must return false if there are any restrictions where
	 * the remaining nodes and held constant and the ANY node is allowed to
	 * vary.
	 * 
	 * See canRead(Triple t)
	 * 
	 * @param t
	 *            The triple to check
	 * @return true if the triple can be created.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canCreate(Triple t) throws AuthenticationRequiredException;

	/**
	 * Return true if the fronted triple can be created.
	 * 
	 * See canRead(Triple t)
	 * 
	 * @param t
	 *            The fronted triple to check
	 * @return true if the triple can be created.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canCreate(FrontsTriple t)
			throws AuthenticationRequiredException;

	/**
	 * @return true if the securedModel allows items to be deleted.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canDelete() throws AuthenticationRequiredException;

	/**
	 * Return true if the triple can be deleted. If any s,p or o is SecNode.ANY
	 * then this method must return false if there are any restrictions where
	 * the remaining nodes and held constant and the ANY node is allowed to
	 * vary.
	 * 
	 * See canRead(Triple t)
	 * 
	 * @param t
	 *            The triple to check
	 * @return true if the triple can be deleted.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canDelete(Triple t) throws AuthenticationRequiredException;

	/**
	 * Return true if the fronted triple can be deleted.
	 * 
	 * See canRead(Triple t)
	 * 
	 * @param t
	 *            The fronted triple to check
	 * @return true if the triple can be deleted.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canDelete(FrontsTriple t)
			throws AuthenticationRequiredException;

	/**
	 * @return true if the securedModel allows items to be read.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canRead() throws AuthenticationRequiredException;

	/**
	 * Return true if the triple can be read. If any s,p or o is SecNode.ANY
	 * then this method must return false if there are any restrictions where
	 * the remaining nodes and held constant and the ANY node is allowed to
	 * vary.
	 * 
	 * (S, P, O) check if S,P,O can be read. (S, P, ANY) check if there are any
	 * S,P,x restrictions. (S, ANY, P) check if there are any S,x,P
	 * restrictions. (ANY, ANY, ANY) check if there are any restrictions on
	 * reading.
	 * 
	 * @param t
	 *            The triple to check
	 * @return true if the triple can be read.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canRead(Triple t) throws AuthenticationRequiredException;

	/**
	 * Return true if the fronted triple can be read.
	 * 
	 * @param t
	 *            The frontedtriple to check
	 * @return true if the triple can be read.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canRead(FrontsTriple t)
			throws AuthenticationRequiredException;

	/**
	 * @return true if the securedModel allows items to be updated.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canUpdate() throws AuthenticationRequiredException;

	/**
	 * Return true if the triple can be updated. If any s,p or o is SecNode.ANY
	 * then this method must return false if there are any restrictions where
	 * the remaining nodes and held constant and the ANY node is allowed to
	 * vary.
	 * 
	 * See canRead(Triple t)
	 * 
	 * @param from
	 *            The triple that will be changed
	 * @param to
	 *            The resulting triple.
	 * @return true if the from triple can be updated as the to triple.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canUpdate(Triple from, Triple to)
			throws AuthenticationRequiredException;

	/**
	 * Return true if the fronted triple can be updated.
	 * 
	 * 
	 * See canUpdate(Triple from, Triple to)
	 * 
	 * @param from
	 *            The fronted triple that will be changed
	 * @param to
	 *            The resulting fronted triple.
	 * @return true if the from triple can be updated as the to triple.
	 * @throws AuthenticationRequiredException
	 *             if user is not authenticated and is required to be.
	 */
	public boolean canUpdate(FrontsTriple from, FrontsTriple to)
			throws AuthenticationRequiredException;

	@Override
	public boolean equals(Object o);

	/**
	 * @return the base item that is being secured.
	 */
	public Object getBaseItem();

	/**
	 * @return The IRI of the securedModel that the item belongs to.
	 */
	public String getModelIRI();

	/**
	 * @return The node representation of the securedModel IRI.
	 */
	public Node getModelNode();

	/**
	 * The SecurityEvaluator implementation that is being used to determine
	 * access.
	 * 
	 * @return The SecurityEvaluator implementation.
	 */
	public SecurityEvaluator getSecurityEvaluator();

	/**
	 * Return true if this secured item is equivalent to another secured item.
	 * Generally implemented by calling SecuredItem.Util.isEquivalent
	 * 
	 * @param securedItem
	 *            the other secured item.
	 * @return True if they are equivalent, false otherwise.
	 */
	public boolean isEquivalent(SecuredItem securedItem);

}
