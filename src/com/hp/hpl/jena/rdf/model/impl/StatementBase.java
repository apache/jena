/*
	 (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
	 [See end of file]
	 $Id: StatementBase.java,v 1.5 2005-02-21 12:14:54 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;

/**
 	Abstract base class for StaementImpl - pulls up the stuff that doesn't depend
 	on how statements are represented (as S/P/O or as Triples).
 	
 	@author hedgehog
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
		{
		return model;
		}

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
		return replace(new LiteralImpl(Node.createLiteral(s, lang, wellFormed),
				model));
		}

	/**
	 * "replace" the Object of this statement with the literal string value _s_.
	 * NOTE: this is a convenience function to eliminate the use of a deprecated
	 * constructor; when data-types are put properly into Jena, it will likely
	 * disappear.
	 */
	protected StatementImpl stringReplace( String s )
		{
		return stringReplace( s, "", false );
		}

	public Statement changeObject( boolean o )
		{
		return stringReplace( String.valueOf( o ) );
		}

	public Statement changeObject( long o )
		{
		return stringReplace( String.valueOf( o ) );
		}

	public Statement changeObject( char o )
		{
		return stringReplace( String.valueOf( o ) );
		}

	public Statement changeObject( float o )
		{
		return stringReplace( String.valueOf( o ) );
		}

	public Statement changeObject( double o )
		{
		return stringReplace( String.valueOf( o ) );
		}

	public Statement changeObject( String o )
		{
		return stringReplace( String.valueOf( o ) );
		}

	public Statement changeObject( String o, boolean wellFormed )
		{
		return stringReplace( String.valueOf( o ), "", wellFormed );
		}

	public Statement changeObject( String o, String l )
		{
		return stringReplace( String.valueOf( o ), l, false );
		}

	public Statement changeObject( String o, String l, boolean wellFormed )
		{
		return stringReplace( String.valueOf( o ), l, wellFormed );
		}

	public Statement changeObject( RDFNode o )
		{
		return replace( o );
		}

	public Statement changeObject( Object o )
		{
		return o instanceof RDFNode 
			? replace( (RDFNode) o ) 
		    : stringReplace( o.toString() );
		}

	public boolean getBoolean()
		{
		return getLiteral().getBoolean();
		}

	public byte getByte()
		{
		return getLiteral().getByte();
		}

	public short getShort()
		{
		return getLiteral().getShort();
		}

	public int getInt()
		{
		return getLiteral().getInt();
		}

	public long getLong()
		{
		return getLiteral().getLong();
		}

	public char getChar()
		{
		return getLiteral().getChar();
		}

	public float getFloat()
		{
		return getLiteral().getFloat();
		}

	public double getDouble()
		{
		return getLiteral().getDouble();
		}

	public String getString()
		{
		return getLiteral().getLexicalForm();
		}

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
		{
		return getLiteral().getLanguage();
		}

	public boolean getWellFormed()
		{
		return getLiteral().getWellFormed();
		}

	/**
	 	Answer a string describing this Statement in a vagely pretty way, with the 
	 	representations of the subject, predicate, and object in that order.
	*/
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
		{
		return object.asNode().toString( null, true );
		}

	}

/*
	 (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP All rights
	 reserved. Redistribution and use in source and binary forms, with or without
	 modification, are permitted provided that the following conditions are met:
	 1. Redistributions of source code must retain the above copyright notice,
	 this list of conditions and the following disclaimer. 2. Redistributions in
	 binary form must reproduce the above copyright notice, this list of
	 conditions and the following disclaimer in the documentation and/or other
	 materials provided with the distribution. 3. The name of the author may not
	 be used to endorse or promote products derived from this software without
	 specific prior written permission.
	  
	 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
	 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
	 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
	 EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
	 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
	 OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
	 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
	 OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
	 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
