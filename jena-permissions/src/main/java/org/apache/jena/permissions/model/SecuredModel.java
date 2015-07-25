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

import java.io.InputStream ;
import java.io.OutputStream ;
import java.io.Reader ;
import java.io.Writer ;
import java.util.Calendar ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.permissions.graph.SecuredGraph;
import org.apache.jena.permissions.graph.SecuredPrefixMapping;
import org.apache.jena.permissions.model.impl.SecuredNodeIterator;
import org.apache.jena.permissions.model.impl.SecuredRSIterator;
import org.apache.jena.permissions.model.impl.SecuredResIterator;
import org.apache.jena.permissions.model.impl.SecuredStatementIterator;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PropertyNotFoundException ;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;

/**
 * The interface for secured Model instances.
 * 
 * Use the SecuredModel.Factory to create instances
 */
public interface SecuredModel extends Model, SecuredPrefixMapping
{

	@Override
	public SecuredModel abort();

	/**
	 * @sec.graph Update
	 * @sec.triple Create for each statement as a triple.
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final List<Statement> statements )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create for each statement in the securedModel as a triple.
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final Model m ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p, final RDFNode o )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p, final String o )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String o, final boolean wellFormed )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,literal(lex,datatype))
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String lex, final RDFDatatype datatype )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,literal(o,l,false))
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String o, final String l ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create the statement as a triple
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final Statement s ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create all the statements as triples.
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final Statement[] statements )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create all the statements as triples.
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel add( final StmtIterator iter )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final boolean o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final char o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final double o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final float o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final int o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final Literal o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final long o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	@Deprecated
	public SecuredModel addLiteral( final Resource s, final Property p,
			final Object o ) throws AddDeniedException, UpdateDeniedException; 

	@Override
	public SecuredRDFNode asRDFNode( final Node n );

	@Override
	/**
	 * @sec.graph Read if t does exist
	 * @sec.graph Update it t does not exist
	 * @sec.triple Read if t does exist
	 * @sec.triple Create if t does exist
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 * @throws ReadException
	 */
	public SecuredStatement asStatement( final Triple t )
			throws UpdateDeniedException, AddDeniedException, ReadDeniedException;

	@Override
	public SecuredModel begin();

	@Override
	public SecuredModel commit();

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, SecNode.ANY )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final Resource s, final Property p )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, o )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final Resource s, final Property p, final RDFNode o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, o )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final Resource s, final Property p, final String o )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o,l,null) )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final Resource s, final Property p,
			final String o, final String l ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read s as a triple with null replaced by SecNode.ANY
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean contains( final Statement s ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read every statement in securedModel.
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsAll( final Model model )	throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read every statement
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsAll( final StmtIterator iter )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read any statement in securedModel to be included in check, if
	 *            no
	 *            statement in securedModel can be read will return false;
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsAny( final Model model )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read any statement in iter to be included in check, if no
	 *            statement in iter can be read will return false;
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsAny( final StmtIterator iter )
			throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final boolean o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final char o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final double o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final float o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final int o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final long o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, typedLiteral(o) )
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final Object o ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, resource) where SecTriple(s,p,resource) is in the
	 *            securedModel.
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean containsResource( final RDFNode r )
			throws ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.ANY, RDF.type, Rdf.Alt)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt createAlt() throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( uri, RDF.type, Rdf.Alt)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredAlt createAlt( final String uri )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.ANY, RDF.type, Rdf.Bag)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredBag createBag() throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( uri, RDF.type, Rdf.Bag)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredBag createBag( final String uri )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredRDFList createList() throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( RDF.nil, SecNode.IGNORE, SecNode.IGNORE)
	 * @sec.triple Create for each member SecTriple(SecNode.ANY,
	 *            RDF.first.asNode(),
	 *            member.asNode())
	 * @sec.triple Create SecTriple(SecNode.ANY, RDF.rest.asNode(), SecNode.ANY)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredRDFList createList( final Iterator<? extends RDFNode> members )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( RDF.nil, SecNode.IGNORE, SecNode.IGNORE)
	 * @sec.triple Create for each member SecTriple(SecNode.ANY,
	 *            RDF.first.asNode(),
	 *            member.asNode())
	 * @sec.triple Create SecTriple(SecNode.ANY, RDF.rest.asNode(), SecNode.ANY)
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredRDFList createList( final RDFNode[] members )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final boolean o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final char o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final double o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final float o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final int o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final long o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final Object o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Property createProperty( final String uri )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public Property createProperty( final String nameSpace,
			final String localName ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Read s as a triple
	 * @sec.triple create SecTriple( SecNode.Future, RDF.subject, t.getSubject()
	 *            )
	 * @sec.triple create SecTriple( SecNode.Future, RDF.subject,
	 *            t.getPredicate() )
	 * @sec.triple create SecTriple( SecNode.Future, RDF.subject, t.getObject() )
	 * @throws UpdateDeniedException
	 * @throws ReadDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public ReifiedStatement createReifiedStatement( final Statement s )
			throws AddDeniedException, UpdateDeniedException, ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Read s as a triple
	 * @sec.triple create SecTriple( uri, RDF.subject, t.getSubject() )
	 * @sec.triple create SecTriple( uri, RDF.subject, t.getPredicate() )
	 * @sec.triple create SecTriple( uri, RDF.subject, t.getObject() )
	 * @throws UpdateDeniedException
	 * @throws ReadDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public ReifiedStatement createReifiedStatement( final String uri,
			final Statement s ) throws AddDeniedException, UpdateDeniedException, ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Read s as a triple
	 * @sec.triple Create SecTriple( SecNode.FUTURE, SecNode.IGNORE,
	 *            SecNode.IGNORE )
	 * @throws UpdateDeniedException
	 * @throws ReadDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredResource createResource() throws AddDeniedException, UpdateDeniedException, ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Read s as a triple
	 * @sec.triple Create SecTriple( Anonymous(id), SecNode.IGNORE,
	 *            SecNode.IGNORE )
	 * @throws UpdateDeniedException
	 * @throws ReadDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredResource createResource( final AnonId id )
			throws AddDeniedException, UpdateDeniedException, ReadDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.type, type )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredResource createResource( final Resource type )
			throws AddDeniedException, UpdateDeniedException;

	@Override
	@Deprecated
	public SecuredResource createResource( final ResourceF f );

	@Override
	public SecuredResource createResource( final String uri );

	/**
	 * @sec.graph Update if uri exists
	 * @sec.graph Create if uri does not exist
	 * @sec.triple Read if SecTriple( uri, RDF.type, type ) exists
	 * @sec.triple Create if SecTriple( uri, RDF.type, type ) does not exist
	 * @throws UpdateDeniedException
	 * @throws ReadDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredResource createResource( final String uri, final Resource type )
			throws AddDeniedException, UpdateDeniedException, ReadDeniedException;

	@Override
	@Deprecated
	public SecuredResource createResource( final String uri, final ResourceF f );

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.type, RDF.Alt )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredSeq createSeq() throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( uri, RDF.type, RDF.Alt )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredSeq createSeq( final String uri )
			throws AddDeniedException, UpdateDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final RDFNode o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, o )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o ) throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, o )
	 * @throws AddDeniedException
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final boolean wellFormed )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, literal(o,l,false ))
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final String l )
			throws AddDeniedException, UpdateDeniedException; 

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, literal(o,l,wellFormed )
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final String l,
			final boolean wellFormed ) throws AddDeniedException, UpdateDeniedException; 

	@Override
	public SecuredLiteral createTypedLiteral( final boolean v );

	@Override
	public Literal createTypedLiteral( final Calendar d );

	@Override
	public SecuredLiteral createTypedLiteral( final char v );

	@Override
	public SecuredLiteral createTypedLiteral( final double v );

	@Override
	public SecuredLiteral createTypedLiteral( final float v );

	@Override
	public SecuredLiteral createTypedLiteral( final int v );

	@Override
	public SecuredLiteral createTypedLiteral( final long v );

	@Override
	public SecuredLiteral createTypedLiteral( final Object value );

	@Override
	public SecuredLiteral createTypedLiteral( final Object value,
			final RDFDatatype dtype );

	@Override
	public SecuredLiteral createTypedLiteral( final Object value,
			final String typeURI );

	@Override
	public SecuredLiteral createTypedLiteral( final String v );

	@Override
	public SecuredLiteral createTypedLiteral( final String lex,
			final RDFDatatype dtype );

	@Override
	public SecuredLiteral createTypedLiteral( final String lex,
			final String typeURI );

	/**
	 * @sec.graph Read
	 * @sec.triple Read for every triple contributed to the difference.
	 * @throws ReadDeniedException
	 */
	@Override
	public Model difference( final Model model ) throws ReadDeniedException;

	/**
	 * @sec.graph Read if read lock is requested
	 * @sec.graph Update if write lock is requested
	 * @throws ReadDeniedException
	 * @throws UpdateDeniedException
	 */
	@Override
	public void enterCriticalSection( final boolean readLockRequested ) throws ReadDeniedException, UpdateDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String expandPrefix( final String prefixed ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( resource, RDF.type, RDF.alt )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredAlt getAlt( final Resource r ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( uri, RDF.type, RDF.alt )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredAlt getAlt( final String uri ) throws ReadDeniedException;

	/**
	 * @sec.graph Read if statement exists
	 * @sec.graph Update if statement does not exist
	 * @sec.triple Read s as a triple
	 * @sec.triple Read SecTriple( result, RDF.subject, s.getSubject() ) if
	 *            reification existed
	 * @sec.triple Read SecTriple( result, RDF.predicate, s.getPredicate() ) if
	 *            reification existed
	 * @sec.triple Read SecTriple( result, RDF.object, s.getObject() ) if
	 *            reification existed
	 * @sec.triple Create SecTriple( result, RDF.subject, s.getSubject() ) if
	 *            reification did not exist.
	 * @sec.triple Create SecTriple( result, RDF.redicate, s.getPredicate() ) if
	 *            reification did not exist
	 * @sec.triple Create SecTriple( result, RDF.object, s.getObject() ) if
	 *            reification did not exist
	 * @throws ReadDeniedException
	 * @throws UpdateDeniedException
	 * @throws AddDeniedException
	 */
	@Override
	public SecuredResource getAnyReifiedStatement( final Statement s )
			throws AddDeniedException, ReadDeniedException, UpdateDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( resource, RDF.type, RDF.Bag )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredBag getBag( final Resource r ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( uri, RDF.type, RDF.Bag )
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredBag getBag( final String uri ) throws ReadDeniedException;

	@Override
	public SecuredGraph getGraph();

	/**
	 * @sec.graph Read
	 * @sec.triple Read on the returned statement.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatement getProperty( final Resource s, final Property p ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public Property getProperty( final String uri ) throws ReadDeniedException;

	/**
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public Property getProperty( final String nameSpace, final String localName ) throws ReadDeniedException;

	/**
	 * @sec.graph Read if the node exists
	 * @sec.graph Update if the node does not exist
	 * @throws ReadDeniedException
	 */
	@Override
	public RDFNode getRDFNode( final Node n ) throws ReadDeniedException;

	/**
	 * .
	 * If the PropertyNotFoundException was thrown by the enclosed securedModel
	 * and the
	 * user can not read SecTriple(s, p, SecNode.ANY) AccessDeniedExcepiton is
	 * thrown,
	 * otherwise the PropertyNotFoundException will be thrown.
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on the returned statement
	 * @sec.triple Read on SecTriple(s, p, SecNode.ANY) if
	 *            PropertyNotFoundException
	 *            was thrown
	 * @throws ReadDeniedException
	 * @throws PropertyNotFoundException
	 */
	@Override
	public SecuredStatement getRequiredProperty( final Resource s,
			final Property p ) throws PropertyNotFoundException, ReadDeniedException;

	@Override
	public SecuredResource getResource( final String uri );

	@Override
	@Deprecated
	public SecuredResource getResource( final String uri, final ResourceF f );

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on SecTriple(resource, RDF.type, RDF.Seq)
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredSeq getSeq( final Resource r ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on SecTriple(uri, RDF.type, RDF.Seq)
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredSeq getSeq( final String uri ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples contributed to the new securedModel.
	 * @throws ReadDeniedException
	 */
	@Override
	public Model intersection( final Model model ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean isEmpty() throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read all compared triples. Triples that can not be read will
	 *            not be compared.
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean isIsomorphicWith( final Model g ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on s as triple
	 * @sec.triple Read on at least one set reified statements.
	 * @throws ReadDeniedException
	 */
	@Override
	public boolean isReified( final Statement s ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate,
			final boolean object ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws ReadDeniedException
	 */

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final char object ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws ReadDeniedException
	 */

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate,
			final double object ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final float object ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws ReadDeniedException
	 */

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final long object ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public NsIterator listNameSpaces() throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each RDFNode returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredNodeIterator<RDFNode> listObjects() throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each RDFNode returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredNodeIterator<RDFNode> listObjectsOfProperty( final Property p ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each RDFNode returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredNodeIterator<RDFNode> listObjectsOfProperty( final Resource s,
			final Property p ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each Reified statement returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredRSIterator listReifiedStatements() throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each Reified statement returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredRSIterator listReifiedStatements( final Statement st ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final boolean o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final char o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final double o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final float o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final long o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final Object o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final RDFNode o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements() throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements( final Resource s,
			final Property p, final RDFNode o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements( final Resource subject,
			final Property predicate, final String object )  throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements( final Resource subject,
			final Property predicate, final String object, final String lang ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements( final Selector s ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listSubjects() throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final RDFNode o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final String o ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final String o, final String l ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredPrefixMapping lock() throws ReadDeniedException;

	@Override
	public SecuredModel notifyEvent( final Object e );

	/**
	 * 
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String qnameFor( final String uri ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredModel query( final Selector s ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredModel read( final InputStream in, final String base ) throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredModel read( final InputStream in, final String base,
			final String lang ) throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredModel read( final Reader reader, final String base ) throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredModel read( final Reader reader, final String base,
			final String lang ) throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredModel read( final String url ) throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredModel read( final String url, final String lang ) throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredModel read( final String url, final String base,
			final String lang ) throws UpdateDeniedException;

	/**
	 * 
	 * Listener will be filtered to only report events that the user can see.
	 * 
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredModel register( final ModelChangedListener listener ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in statments.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredModel remove( final List<Statement> statements )
			throws DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in baseModel.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredModel remove( final Model m ) throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on SecTriple( s, p, o )
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredModel remove( final Resource s, final Property p, final RDFNode o ) throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on statment.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredModel remove( final Statement s )
			throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in statments.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredModel remove( final Statement[] statements )
			throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in iter.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredModel remove( final StmtIterator iter )
			throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in the securedModel
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredModel removeAll() throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement identified by SecTriple( s,p,o)
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public SecuredModel removeAll( final Resource s, final Property p,
			final RDFNode r ) throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every reification statement for each statement in
	 *            statments.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public void removeAllReifications( final Statement s )
			throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public SecuredPrefixMapping removeNsPrefix( final String prefix )
			throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every reification statement fore each statement in
	 *            rs.
	 * @throws UpdateDeniedException
	 * @throws DeleteDeniedException
	 */
	@Override
	public void removeReification( final ReifiedStatement rs )
			throws  DeleteDeniedException, UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public String setReaderClassName( final String lang, final String className ) throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws UpdateDeniedException
	 */
	@Override
	public String setWriterClassName( final String lang, final String className ) throws UpdateDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public String shortForm( final String uri ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws ReadDeniedException
	 */
	@Override
	public long size() throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements contributed to the union.
	 * @throws ReadDeniedException
	 */
	@Override
	public Model union( final Model model ) throws ReadDeniedException;

	@Override
	public SecuredModel unregister( final ModelChangedListener listener );

	@Override
	public SecuredResource wrapAsResource( final Node n );

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredModel write( final OutputStream out )
			throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredModel write( final OutputStream out, final String lang )
			throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredModel write( final OutputStream out, final String lang,
			final String base ) throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredModel write( final Writer writer )
			throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredModel write( final Writer writer, final String lang )
			throws ReadDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws ReadDeniedException
	 */
	@Override
	public SecuredModel write( final Writer writer, final String lang,
			final String base ) throws ReadDeniedException;

}
