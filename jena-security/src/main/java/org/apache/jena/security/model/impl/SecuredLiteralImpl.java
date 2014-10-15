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
package org.apache.jena.security.model.impl;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.ResourceRequiredException;

import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemInvoker;
import org.apache.jena.security.model.SecuredLiteral;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.model.SecuredResource;

/**
 * Implementation of SecuredLiteral to be used by a SecuredItemInvoker proxy.
 */
public class SecuredLiteralImpl extends SecuredRDFNodeImpl implements
		SecuredLiteral
{
	/**
	 * Get an instance of SecuredLiteral
	 * 
	 * @param securedModel
	 *            the item providing the security context.
	 * @param literal
	 *            the literal to secure
	 * @return SecuredLiteral
	 */
	public static SecuredLiteral getInstance( final SecuredModel securedModel,
			final Literal literal )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (literal == null)
		{
			throw new IllegalArgumentException("literal may not be null");
		}

		// check that literal has a securedModel.
		Literal goodLiteral = literal;
		if (goodLiteral.getModel() == null)
		{
			goodLiteral = securedModel.createTypedLiteral(
					literal.getLexicalForm(), literal.getDatatype());
		}

		final ItemHolder<Literal, SecuredLiteral> holder = new ItemHolder<Literal, SecuredLiteral>(
				goodLiteral);
		final SecuredLiteralImpl checker = new SecuredLiteralImpl(securedModel,
				holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (goodLiteral instanceof SecuredLiteral)
		{
			if (checker.isEquivalent((SecuredLiteral) goodLiteral))
			{
				return (SecuredLiteral) goodLiteral;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(literal.getClass(),
				checker));
	}

	// the item holder that contains this SecuredLiteral
	private final ItemHolder<? extends Literal, ? extends SecuredLiteral> holder;

	/**
	 * Constructor
	 * 
	 * @param securityEvaluator
	 *            The security evaluator to use.
	 * @param graphIRI
	 *            the graph IRI to validate against.
	 * @param holder
	 *            The item holder that will contain this SecuredLiteral.
	 */

	private SecuredLiteralImpl( final SecuredModel securedModel,
			final ItemHolder<? extends Literal, ? extends SecuredLiteral> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
	}

	@Override
	public SecuredLiteral asLiteral()
	{
		return holder.getSecuredItem();
	}

	@Override
	public SecuredResource asResource()
	{
		if (canRead())
		{
			throw new ResourceRequiredException(asNode());
		}
		else
		{
			throw new ResourceRequiredException(
					Node.createLiteral("Can not read"));
		}
	}

	@Override
	public boolean getBoolean()
	{
		checkRead();
		return holder.getBaseItem().getBoolean();
	}

	@Override
	public byte getByte()
	{
		checkRead();
		return holder.getBaseItem().getByte();
	}

	@Override
	public char getChar()
	{
		checkRead();
		return holder.getBaseItem().getChar();
	}

	/**
	 * Return the datatype of the literal. This will be null in the
	 * case of plain literals.
	 */
	@Override
	public RDFDatatype getDatatype()
	{
		checkRead();
		return holder.getBaseItem().getDatatype();
	}

	/**
	 * Return the uri of the datatype of the literal. This will be null in the
	 * case of plain literals.
	 */
	@Override
	public String getDatatypeURI()
	{
		checkRead();
		return holder.getBaseItem().getDatatypeURI();
	}

	@Override
	public double getDouble()
	{
		checkRead();
		return holder.getBaseItem().getDouble();
	}

	@Override
	public float getFloat()
	{
		checkRead();
		return holder.getBaseItem().getFloat();
	}

	@Override
	public int getInt()
	{
		checkRead();
		return holder.getBaseItem().getInt();
	}

	@Override
	public String getLanguage()
	{
		checkRead();
		return holder.getBaseItem().getLanguage();
	}

	/**
	 * Return the lexical form of the literal.
	 */
	@Override
	public String getLexicalForm()
	{
		checkRead();
		return holder.getBaseItem().getLexicalForm();
	}

	@Override
	public long getLong()
	{
		checkRead();
		return holder.getBaseItem().getLong();
	}

	@Override
	public short getShort()
	{
		checkRead();
		return holder.getBaseItem().getShort();
	}

	@Override
	public String getString()
	{
		checkRead();
		return holder.getBaseItem().getString();
	}

	/**
	 * Return the value of the literal. In the case of plain literals
	 * this will return the literal string. In the case of typed literals
	 * it will return a java object representing the value. In the case
	 * of typed literals representing a java primitive then the appropriate
	 * java wrapper class (Integer etc) will be returned.
	 */
	@Override
	public Object getValue()
	{
		checkRead();
		return holder.getBaseItem().getValue();
	}

	@Override
	public Literal inModel( final Model m )
	{
		checkRead();
		return m.createTypedLiteral(holder.getBaseItem().getLexicalForm(),
				holder.getBaseItem().getDatatype());
	}

	@Override
	public boolean isWellFormedXML()
	{
		checkRead();
		return holder.getBaseItem().isWellFormedXML();
	}

	/**
	 * Test that two literals are semantically equivalent.
	 * In some cases this may be the sames as equals, in others
	 * equals is stricter. For example, two xsd:int literals with
	 * the same value but different language tag are semantically
	 * equivalent but distinguished by the java equality function
	 * in order to support round tripping.
	 */
	@Override
	public boolean sameValueAs( final Literal other )
	{
		checkRead();
		return holder.getBaseItem().sameValueAs(other);
	}

	@Override
	public Object visitWith( final RDFVisitor rv )
	{
		return rv.visitLiteral(this);
	}
}
