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
package org.apache.jena.security.utils;

import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.Set;

import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.impl.SecuredItem;
import org.apache.jena.security.impl.SecuredItemImpl;

public class RDFListSecFilter<T extends RDFList> extends Filter<T>
{
	private final SecuredItem securedItem;
	private final Set<Action> perms;
	private final Object principal;

	public RDFListSecFilter( final SecuredItem securedItem, final Action perm )
	{
		this(securedItem, SecurityEvaluator.Util.asSet(new Action[] { perm }));
	}

	public RDFListSecFilter( final SecuredItem securedItem,
			final Set<Action> perms )
	{
		this.securedItem = securedItem;
		this.perms = perms;
		this.principal = securedItem.getSecurityEvaluator().getPrincipal();
	}

	@Override
	public boolean accept( final RDFList o )
	{
		final Statement s = o.getRequiredProperty(RDF.first);
		return securedItem.getSecurityEvaluator().evaluate(principal, perms,
				securedItem.getModelNode(),
				SecuredItemImpl.convert(s.asTriple()));
	}
}
