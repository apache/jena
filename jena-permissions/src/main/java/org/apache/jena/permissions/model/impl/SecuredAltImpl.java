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
package org.apache.jena.permissions.model.impl;

import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.SecuredAlt;
import org.apache.jena.permissions.model.SecuredBag;
import org.apache.jena.permissions.model.SecuredLiteral;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.model.SecuredRDFNode;
import org.apache.jena.permissions.model.SecuredResource;
import org.apache.jena.permissions.model.SecuredSeq;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Implementation of SecuredAlt to be used by a SecuredItemInvoker proxy.
 */
public class SecuredAltImpl extends SecuredContainerImpl implements SecuredAlt {
	/**
	 * Get an instance of SecuredAlt.
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param alt
	 *            The Alt to be secured.
	 * @return The secured Alt instance.
	 */
	public static SecuredAlt getInstance(final SecuredModel securedModel,
			final Alt alt) {
		if (securedModel == null) {
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (alt == null) {
			throw new IllegalArgumentException("Alt may not be null");
		}
		final ItemHolder<Alt, SecuredAlt> holder = new ItemHolder<>(
				alt);
		final SecuredAltImpl checker = new SecuredAltImpl(securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (alt instanceof SecuredAlt) {
			if (checker.isEquivalent((SecuredAlt) alt)) {
				return (SecuredAlt) alt;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(alt.getClass(),
				checker));
	}

	// The item holder holding this SecuredAlt
	private final ItemHolder<? extends Alt, ? extends SecuredAlt> holder;

	/**
	 * Constructor.
	 * 
	 * @param securedModel
	 *            the securedModel to use.
	 * @param holder
	 *            The item holder that will hold this SecuredAlt.
	 */
	protected SecuredAltImpl(final SecuredModel securedModel,
			final ItemHolder<? extends Alt, ? extends SecuredAlt> holder) {
		super(securedModel, holder);
		this.holder = holder;
	}

	@Override
	public SecuredRDFNode getDefault() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return SecuredRDFNodeImpl.getInstance(getModel(), getDefaultStatement()
				.getObject());
	}

	@Override
	public SecuredAlt getDefaultAlt() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return SecuredAltImpl.getInstance(getModel(), getDefaultStatement()
				.getAlt());
	}

	@Override
	public SecuredBag getDefaultBag() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return SecuredBagImpl.getInstance(getModel(), getDefaultStatement()
				.getBag());
	}

	@Override
	public boolean getDefaultBoolean() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getBoolean();
	}

	@Override
	public byte getDefaultByte() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getByte();
	}

	@Override
	public char getDefaultChar() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getChar();
	}

	@Override
	public double getDefaultDouble() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getDouble();
	}

	@Override
	public float getDefaultFloat() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getFloat();
	}

	@Override
	public int getDefaultInt() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getInt();
	}

	@Override
	public String getDefaultLanguage() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getLanguage();
	}

	@Override
	public SecuredLiteral getDefaultLiteral() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return SecuredLiteralImpl.getInstance(getModel(), getDefaultStatement()
				.getLiteral());
	}

	@Override
	public long getDefaultLong() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getLong();
	}

	@Override
	public SecuredResource getDefaultResource() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return SecuredResourceImpl.getInstance(getModel(),
				getDefaultStatement().getResource());
	}

	@Override
	public SecuredSeq getDefaultSeq() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return SecuredSeqImpl.getInstance(getModel(), getDefaultStatement()
				.getSeq());
	}

	@Override
	public short getDefaultShort() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getShort();

	}

	private Statement getDefaultStatement() throws ReadDeniedException,
			AuthenticationRequiredException {
		checkRead();
		final ExtendedIterator<Statement> iter = getStatementIterator(Action.Read);
		try {
			if (iter.hasNext()) {
				return iter.next();
			}
			throw new AltHasNoDefaultException(this);
		} finally {
			iter.close();
		}
	}

	@Override
	public String getDefaultString() throws ReadDeniedException,
			AuthenticationRequiredException {
		// getDefaultStatement() calls checkRead
		return getDefaultStatement().getString();

	}

	@Override
	public SecuredAlt setDefault(final boolean o) throws UpdateDeniedException,
			AuthenticationRequiredException {
		return setDefault(asObject(o));
	}

	@Override
	public SecuredAlt setDefault(final char o) throws UpdateDeniedException,
			AuthenticationRequiredException {
		return setDefault(asObject(o));
	}

	@Override
	public SecuredAlt setDefault(final double o) throws UpdateDeniedException,
			AuthenticationRequiredException {
		return setDefault(asObject(o));
	}

	@Override
	public SecuredAlt setDefault(final float o) throws UpdateDeniedException,
			AuthenticationRequiredException {
		return setDefault(asObject(o));
	}

	@Override
	public SecuredAlt setDefault(final long o) throws UpdateDeniedException,
			AuthenticationRequiredException {
		return setDefault(asObject(o));
	}

	@Override
	public SecuredAlt setDefault(final Object o) throws UpdateDeniedException,
			AuthenticationRequiredException {
		return setDefault(asObject(o));
	}

	@Override
	public SecuredAlt setDefault(final RDFNode o) throws UpdateDeniedException,
			AuthenticationRequiredException {
		checkUpdate();
		final ExtendedIterator<Statement> iter = getStatementIterator(Action.Read);
		try {
			if (iter.hasNext()) {
				final Statement stmt = iter.next();
				final Triple t = stmt.asTriple();
				final Triple t2 = new Triple(t.getSubject(), t.getPredicate(),
						o.asNode());
				checkUpdate(t, t2);
				stmt.changeObject(o);
				return holder.getSecuredItem();
			} else {
				add(o);
				return holder.getSecuredItem();
			}
		} finally {
			iter.close();
		}

	}

	@Override
	public SecuredAlt setDefault(final String o) throws UpdateDeniedException,
			AuthenticationRequiredException {
		return setDefault(asLiteral(o, ""));
	}

	@Override
	public SecuredAlt setDefault(final String o, final String l)
			throws UpdateDeniedException, AuthenticationRequiredException {
		return setDefault(asLiteral(o, l));
	}
}
