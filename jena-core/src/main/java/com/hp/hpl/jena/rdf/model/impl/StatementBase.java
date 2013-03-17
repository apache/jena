/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;

/**
 	Abstract base class for StaementImpl - pulls up the stuff that doesn't depend
 	on how statements are represented (as S/P/O or as Triples).
*/
public abstract class StatementBase
	{
	protected final ModelCom model;

	protected StatementBase( ModelCom model )
		{
		if (model == null) throw new JenaException( "Statement models must no be null" );
		this.model = model;
		}

	public Model getModel()
		{ return model; }

	/**
	 * replace this StaementImpl [ie the one of which this is the base] with (S,
	 * P, n). Answer the new StaementImpl. Abstract here to allow methods to be
	 * pulled up.
	 */
	protected abstract StatementImpl replace( RDFNode n );

	/**
	 * Answer the object of this statement as a Literal, or throw a
	 * LiteralRequiredException.
	 */
	public abstract Literal getLiteral();
	
	public abstract Resource getResource();
	
	public abstract Resource getSubject();
	
	public abstract Property getPredicate();
	
	public abstract RDFNode getObject();

	protected StatementImpl stringReplace(String s, String lang,
			boolean wellFormed)
		{
		return replace(new LiteralImpl(NodeFactory.createLiteral(s, lang, wellFormed),
				model));
		}

	/**
	 * "replace" the Object of this statement with the literal string value _s_.
	 * NOTE: this is a convenience function to eliminate the use of a deprecated
	 * constructor; when data-types are put properly into Jena, it will likely
	 * disappear.
	 */
	protected StatementImpl stringReplace( String s )
		{ return stringReplace( s, "", false ); }

	public Statement changeLiteralObject( boolean o )
		{ return changeObject( model.createTypedLiteral( o ) ); }
	
    public Statement changeLiteralObject( long o )
        { return changeObject( model.createTypedLiteral( o ) ); }

	public Statement changeLiteralObject( char o )
		{ return changeObject( model.createTypedLiteral( o ) ); }

    public Statement changeLiteralObject( double o )
        { return changeObject( model.createTypedLiteral( o ) ); }
    
	public Statement changeLiteralObject( float o )
		{ return changeObject( model.createTypedLiteral( o ) ); }
	
    public Statement changeLiteralObject( int o )
        { return changeObject( model.createTypedLiteral( o ) ); }

	public Statement changeObject( String o )
		{ return stringReplace( String.valueOf( o ) ); }

	public Statement changeObject( String o, boolean wellFormed )
		{ return stringReplace( String.valueOf( o ), "", wellFormed ); }

	public Statement changeObject( String o, String l )
		{ return stringReplace( String.valueOf( o ), l, false ); }

	public Statement changeObject( String o, String l, boolean wellFormed )
		{ return stringReplace( String.valueOf( o ), l, wellFormed ); }

	public Statement changeObject( RDFNode o )
		{ return replace( o ); }

	public boolean getBoolean()
		{ return getLiteral().getBoolean(); }

	public byte getByte()
		{ return getLiteral().getByte(); }

	public short getShort()
		{ return getLiteral().getShort(); }

	public int getInt()
		{ return getLiteral().getInt(); }

	public long getLong()
		{ return getLiteral().getLong(); }

	public char getChar()
		{ return getLiteral().getChar(); }

	public float getFloat()
		{ return getLiteral().getFloat(); }

	public double getDouble()
		{ return getLiteral().getDouble(); }

	public String getString()
		{ return getLiteral().getLexicalForm(); }

	/**
	 * utility: check that node is a Resource, throw otherwise
	 */
	protected Resource mustBeResource(RDFNode n)
		{
		if (n instanceof Resource)
			return (Resource) n;
		else
			throw new ResourceRequiredException(n);
		}

	public String getLanguage()
		{ return getLiteral().getLanguage(); }

	public boolean getWellFormed()
		{ return hasWellFormedXML(); }
    
    public boolean hasWellFormedXML()
        { return getLiteral().isWellFormedXML(); }

	/**
	 	Answer a string describing this Statement in a vagely pretty way, with the 
	 	representations of the subject, predicate, and object in that order.
	*/
	@Override
    public String toString()
		{
		return
		    "[" 
		    + getSubject().toString()
		    + ", " + getPredicate().toString() 
		    + ", " + objectString( getObject() )
		    + "]";
		}

	/**
	 	Answer a string describing <code>object</code>, quoting it if it is a literal.
	*/
	protected String objectString( RDFNode object )
		{ return object.asNode().toString( null, true ); }

	}
