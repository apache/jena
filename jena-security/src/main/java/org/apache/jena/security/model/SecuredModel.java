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
package org.apache.jena.security.model;

import java.io.InputStream ;
import java.io.OutputStream ;
import java.io.Reader ;
import java.io.Writer ;
import java.util.Calendar ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.security.AccessDeniedException ;
import org.apache.jena.security.graph.SecuredGraph ;
import org.apache.jena.security.graph.SecuredPrefixMapping ;
import org.apache.jena.security.model.impl.SecuredNodeIterator ;
import org.apache.jena.security.model.impl.SecuredRSIterator ;
import org.apache.jena.security.model.impl.SecuredResIterator ;
import org.apache.jena.security.model.impl.SecuredStatementIterator ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.PropertyNotFoundException ;

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
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final List<Statement> statements )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create for each statement in the securedModel as a triple.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final Model m ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p, final RDFNode o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p, final String o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String o, final boolean wellFormed )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,literal(lex,datatype))
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String lex, final RDFDatatype datatype )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create the triple SecTriple(s,p,literal(o,l,false))
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final Resource s, final Property p,
			final String o, final String l ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create the statement as a triple
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final Statement s ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create all the statements as triples.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final Statement[] statements )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create all the statements as triples.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel add( final StmtIterator iter )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final boolean o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final char o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final double o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final float o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final int o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final Literal o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel addLiteral( final Resource s, final Property p,
			final long o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create triple(s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	@Deprecated
	public SecuredModel addLiteral( final Resource s, final Property p,
			final Object o ) throws AccessDeniedException;

	@Override
	public SecuredRDFNode asRDFNode( final Node n );

	@Override
	/**
	 * @sec.graph Read if t does exist
	 * @sec.graph Update it t does not exist
	 * @sec.triple Read if t does exist
	 * @sec.triple Create if t does exist
	 * @throws AccessDeniedException
	 */
	public SecuredStatement asStatement( final Triple t )
			throws AccessDeniedException;

	@Override
	public SecuredModel begin();

	@Override
	public SecuredModel commit();

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, SecNode.ANY )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final Resource s, final Property p )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final Resource s, final Property p, final RDFNode o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final Resource s, final Property p, final String o )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o,l,null) )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final Resource s, final Property p,
			final String o, final String l ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read s as a triple with null replaced by SecNode.ANY
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean contains( final Statement s ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read every statement in securedModel.
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsAll( final Model model )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read every statement
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsAll( final StmtIterator iter )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read any statement in securedModel to be included in check, if
	 *            no
	 *            statement in securedModel can be read will return false;
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsAny( final Model model )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read any statement in iter to be included in check, if no
	 *            statement in iter can be read will return false;
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsAny( final StmtIterator iter )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final boolean o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final char o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final double o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final float o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final int o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, literal(o) )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final long o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, typedLiteral(o) )
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsLiteral( final Resource s, final Property p,
			final Object o ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( s, p, resource) where SecTriple(s,p,resource) is in the
	 *            securedModel.
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean containsResource( final RDFNode r )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.ANY, RDF.type, Rdf.Alt)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt createAlt() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( uri, RDF.type, Rdf.Alt)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt createAlt( final String uri )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.ANY, RDF.type, Rdf.Bag)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredBag createBag() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( uri, RDF.type, Rdf.Bag)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredBag createBag( final String uri )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredRDFList createList() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( RDF.nil, SecNode.IGNORE, SecNode.IGNORE)
	 * @sec.triple Create for each member SecTriple(SecNode.ANY,
	 *            RDF.first.asNode(),
	 *            member.asNode())
	 * @sec.triple Create SecTriple(SecNode.ANY, RDF.rest.asNode(), SecNode.ANY)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredRDFList createList( final Iterator<? extends RDFNode> members )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( RDF.nil, SecNode.IGNORE, SecNode.IGNORE)
	 * @sec.triple Create for each member SecTriple(SecNode.ANY,
	 *            RDF.first.asNode(),
	 *            member.asNode())
	 * @sec.triple Create SecTriple(SecNode.ANY, RDF.rest.asNode(), SecNode.ANY)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredRDFList createList( final RDFNode[] members )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final boolean o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final char o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final double o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final float o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final int o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final long o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createLiteralStatement( final Resource s,
			final Property p, final Object o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Property createProperty( final String uri )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s,p,o )
	 * @throws AccessDeniedException
	 */
	@Override
	public Property createProperty( final String nameSpace,
			final String localName ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Read s as a triple
	 * @sec.triple create SecTriple( SecNode.Future, RDF.subject, t.getSubject()
	 *            )
	 * @sec.triple create SecTriple( SecNode.Future, RDF.subject,
	 *            t.getPredicate() )
	 * @sec.triple create SecTriple( SecNode.Future, RDF.subject, t.getObject() )
	 * @throws AccessDeniedException
	 */
	@Override
	public ReifiedStatement createReifiedStatement( final Statement s )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Read s as a triple
	 * @sec.triple create SecTriple( uri, RDF.subject, t.getSubject() )
	 * @sec.triple create SecTriple( uri, RDF.subject, t.getPredicate() )
	 * @sec.triple create SecTriple( uri, RDF.subject, t.getObject() )
	 * @throws AccessDeniedException
	 */
	@Override
	public ReifiedStatement createReifiedStatement( final String uri,
			final Statement s ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Read s as a triple
	 * @sec.triple create SecTriple( SecNode.FUTURE, SecNode.IGNORE,
	 *            SecNode.IGNORE )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource createResource() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Read s as a triple
	 * @sec.triple create SecTriple( Anonymous(id), SecNode.IGNORE,
	 *            SecNode.IGNORE )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource createResource( final AnonId id )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.type, type )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource createResource( final Resource type )
			throws AccessDeniedException;

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
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource createResource( final String uri, final Resource type )
			throws AccessDeniedException;

	@Override
	@Deprecated
	public SecuredResource createResource( final String uri, final ResourceF f );

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.type, RDF.Alt )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredSeq createSeq() throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( uri, RDF.type, RDF.Alt )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredSeq createSeq( final String uri )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final RDFNode o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o ) throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final boolean wellFormed )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, literal(o,l,false ))
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final String l )
			throws AccessDeniedException;

	/**
	 * @sec.graph Update
	 * @sec.triple Create SecTriple( s, p, literal(o,l,wellFormed )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement createStatement( final Resource s,
			final Property p, final String o, final String l,
			final boolean wellFormed ) throws AccessDeniedException;

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
	 * @throws AccessDeniedException
	 */
	@Override
	public Model difference( final Model model ) throws AccessDeniedException;

	/**
	 * @sec.graph Read if read lock is requested
	 * @sec.graph Update if write lock is requested
	 * @throws AccessDeniedException
	 */
	@Override
	public void enterCriticalSection( final boolean readLockRequested )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String expandPrefix( final String prefixed )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( resource, RDF.type, RDF.alt )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt getAlt( final Resource r ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( uri, RDF.type, RDF.alt )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredAlt getAlt( final String uri ) throws AccessDeniedException;

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
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResource getAnyReifiedStatement( final Statement s )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( resource, RDF.type, RDF.Bag )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredBag getBag( final Resource r ) throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @sec.triple Read SecTriple( uri, RDF.type, RDF.Bag )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredBag getBag( final String uri ) throws AccessDeniedException;

	@Override
	public SecuredGraph getGraph();

	/**
	 * @sec.graph Read
	 * @sec.triple Read on the returned statement.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatement getProperty( final Resource s, final Property p )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public Property getProperty( final String uri )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public Property getProperty( final String nameSpace, final String localName )
			throws AccessDeniedException;

	/**
	 * @sec.graph Read if the node exists
	 * @sec.graph Update if the node does not exist
	 * @throws AccessDeniedException
	 */
	@Override
	public RDFNode getRDFNode( final Node n ) throws AccessDeniedException;

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
	 * @throws AccessDeniedException
	 * @throws PropertyNotFoundException
	 */
	@Override
	public SecuredStatement getRequiredProperty( final Resource s,
			final Property p ) throws PropertyNotFoundException,
			AccessDeniedException;

	@Override
	public SecuredResource getResource( final String uri );

	@Override
	@Deprecated
	public SecuredResource getResource( final String uri, final ResourceF f );

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on SecTriple(resource, RDF.type, RDF.Seq)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredSeq getSeq( final Resource r ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on SecTriple(uri, RDF.type, RDF.Seq)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredSeq getSeq( final String uri ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples contributed to the new securedModel.
	 * @throws AccessDeniedException
	 */
	@Override
	public Model intersection( final Model model ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean isEmpty() throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read all compared triples. Triples that can not be read will
	 *            not be compared.
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean isIsomorphicWith( final Model g )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on s as triple
	 * @sec.triple Read on at least one set reified statements.
	 * @throws AccessDeniedException
	 */
	@Override
	public boolean isReified( final Statement s ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate,
			final boolean object ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws AccessDeniedException
	 */

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final char object )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws AccessDeniedException
	 */

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate,
			final double object ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws AccessDeniedException
	 */

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final float object )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned.
	 * @throws AccessDeniedException
	 */

	@Override
	public SecuredStatementIterator listLiteralStatements(
			final Resource subject, final Property predicate, final long object )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public NsIterator listNameSpaces() throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each RDFNode returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredNodeIterator<RDFNode> listObjects() throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each RDFNode returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredNodeIterator<RDFNode> listObjectsOfProperty( final Property p )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each RDFNode returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredNodeIterator<RDFNode> listObjectsOfProperty( final Resource s,
			final Property p ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each Reified statement returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredRSIterator listReifiedStatements()
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on each Reified statement returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredRSIterator listReifiedStatements( final Statement st )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws AccessDeniedException
	 */

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final boolean o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws AccessDeniedException
	 */

	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final char o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final double o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final float o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final long o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned;
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final Object o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listResourcesWithProperty( final Property p,
			final RDFNode o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements()
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements( final Resource s,
			final Property p, final RDFNode o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements( final Resource subject,
			final Property predicate, final String object )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements( final Resource subject,
			final Property predicate, final String object, final String lang )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all triples returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredStatementIterator listStatements( final Selector s )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listSubjects() throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final RDFNode o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final String o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read at least one SecTriple( resource, p, o ) for each
	 *            resource
	 *            returned
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredResIterator listSubjectsWithProperty( final Property p,
			final String o, final String l ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredPrefixMapping lock() throws AccessDeniedException;

	@Override
	public SecuredModel notifyEvent( final Object e );

	/**
	 * 
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String qnameFor( final String uri ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel query( final Selector s ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel read( final InputStream in, final String base )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel read( final InputStream in, final String base,
			final String lang ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel read( final Reader reader, final String base )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel read( final Reader reader, final String base,
			final String lang ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel read( final String url ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel read( final String url, final String lang )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel read( final String url, final String base,
			final String lang ) throws AccessDeniedException;

	/**
	 * 
	 * Listener will be filtered to only report events that the user can see.
	 * 
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel register( final ModelChangedListener listener )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in statments.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel remove( final List<Statement> statements )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in baseModel.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel remove( final Model m ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on SecTriple( s, p, o )
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel remove( final Resource s, final Property p, final RDFNode o ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on statment.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel remove( final Statement s )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in statments.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel remove( final Statement[] statements )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in iter.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel remove( final StmtIterator iter )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement in the securedModel
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel removeAll() throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every statement identified by SecTriple( s,p,o)
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel removeAll( final Resource s, final Property p,
			final RDFNode r ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every reification statement for each statement in
	 *            statments.
	 * @throws AccessDeniedException
	 */
	@Override
	public void removeAllReifications( final Statement s )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredPrefixMapping removeNsPrefix( final String prefix )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @sec.triple Delete on every reification statement fore each statement in
	 *            rs.
	 * @throws AccessDeniedException
	 */
	@Override
	public void removeReification( final ReifiedStatement rs )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public String setReaderClassName( final String lang, final String className )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Update
	 * @throws AccessDeniedException
	 */
	@Override
	public String setWriterClassName( final String lang, final String className )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public String shortForm( final String uri ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @throws AccessDeniedException
	 */
	@Override
	public long size() throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements contributed to the union.
	 * @throws AccessDeniedException
	 */
	@Override
	public Model union( final Model model ) throws AccessDeniedException;

	@Override
	public SecuredModel unregister( final ModelChangedListener listener );

	@Override
	public SecuredResource wrapAsResource( final Node n );

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel write( final OutputStream out )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel write( final OutputStream out, final String lang )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel write( final OutputStream out, final String lang,
			final String base ) throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel write( final Writer writer )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel write( final Writer writer, final String lang )
			throws AccessDeniedException;

	/**
	 * 
	 * @sec.graph Read
	 * @sec.triple Read on all statements that are written.
	 * @throws AccessDeniedException
	 */
	@Override
	public SecuredModel write( final Writer writer, final String lang,
			final String base ) throws AccessDeniedException;

}
