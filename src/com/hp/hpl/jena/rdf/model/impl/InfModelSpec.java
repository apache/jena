/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: InfModelSpec.java,v 1.12 2004-08-31 09:49:50 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import junit.framework.Assert;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.WrappedReasonerFactory;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;

/**
    A ModelSpec for InfModels. The description of an InfModel is the description of a 
    ModelMaker [for the base graph] plus the JMS.reasonsWith property to give the
    Resource who's URI identifies the reasoner to use [as per the ReasonerRegistry].
    
 	@author kers
*/
public class InfModelSpec extends ModelSpecImpl
    {
    /**
        The Resource who's URI identifies the reasoner to use.
    */
    protected Resource reasonerResource;
    protected Resource reasonerRoot;
    protected ReasonerFactory factory;
    
    /**
        Initialise an InfModelSpec using the ModelMaker specification and the value of
        the JMS.reasoner property.
    */
    public InfModelSpec( Resource root, Model description )
        {
        super( root, description );
        Statement st = description.getRequiredProperty( null, JMS.reasoner );
        Statement xx = description.listStatements( root, JMS.reasonsWith, (RDFNode) null ).nextStatement();
        reasonerRoot = st.getSubject();
        Resource yy = xx.getResource();
        Assert.assertEquals( reasonerRoot, yy );
        reasonerResource = st.getResource();
        factory = getReasonerFactory( st.getSubject(), description );
        }   

    /**
        Answer an InfModel that does the reasoning as defined by the reasoner URI over
        a new graph that is created by the ModelMaker.
        
        @return a new InfModel reasoning over a new base graph.
    */
    public Model createModel()
        { return createModel( maker.getGraphMaker().createGraph() ); }
        
    /**
        Answer a new InfModel based over the graph named in the underlying ModelMaker. 
     	@see com.hp.hpl.jena.rdf.model.ModelSpec#createModelOver(java.lang.String)
     */
    public Model createModelOver( String name )
        { return createModel( maker.getGraphMaker().createGraph( name ) ); }
        
    /**
        Answer an InfModel based on the given Graph which does reasoning as required
        by this Specs reasonerResource.
        
     	@param base the base graph that the inference is done over
     	@return an inference model that does this spec's reasoning over the base
     */
    protected Model createModel( Graph base )
        {
        Reasoner reasoner = factory.create( null ); 
        return new InfModelImpl( reasoner.bind( base ) );     
        }
    
    /**
        Answer the maker property needed by descriptions.
        @return JMS.maker
    */
    public Property getMakerProperty()
        { return JMS.maker; }
    
    /**
        Add this ModelMaker and Reasoner description to the supplied model under the
        given name, and answer the descrption model.
        
        @param desc the model to augment with this description
        @param self the resource to use as our name
        @return desc, for cascading
    */
    public Model addDescription( Model desc, Resource self )
        {
        super.addDescription( desc, self );
        Resource r = reasonerRoot; // desc.createResource();
        desc.add( self, JMS.reasonsWith, r );
        desc.add( r, JMS.reasoner, reasonerResource );
        new ModelExtract( notJMS ) .extractInto( desc, r, description );
        return desc;    
        }
    
    private final static TripleBoundary notJMSTriple = new TripleBoundary()
        { public boolean stopAt( Triple t ) { return !t.getPredicate().getNameSpace().equals( JMS.baseURI ); }};
    
    private static final StatementBoundary notJMS = new StatementBoundary()
        {
        public boolean stopAt( Statement s ) { return notJMSTriple.stopAt( s.asTriple() ); }
        public TripleBoundary asTripleBoundary( Model ignored ) { return notJMSTriple; }
        };

    /**
         Answer a ReasonerFactory described by the properties of the resource
         <code>R</code> in the model <code>rs</code>. Will throw 
         NoReasonerSuppliedException if no jms:reasoner is supplied, or
         NoSuchReasonerException if the reasoner value isn't known to
         ReasonerRegistry. If any <code>ruleSetURL</code>s are supplied, the
         reasoner factory must be a RuleReasonerFactory, and is wrapped so that
         the supplied rules are specific to this Factory.
    */
    public static ReasonerFactory getReasonerFactory( Resource R, Model rs )
        {
        StmtIterator r = rs.listStatements( R, JMS.reasoner, (RDFNode) null );
        if (r.hasNext() == false) throw new NoReasonerSuppliedException();
        Resource rr = r.nextStatement().getResource();
        String rrs = rr.getURI();
        ReasonerFactory rf = ReasonerRegistry.theRegistry().getFactory( rrs );
        if (rf == null) throw new NoSuchReasonerException( rrs );
        return loadFactory( rf, rs, R );
        }

    /**
        If there are no jms:ruleSet or jms:ruleSetURL properties of <code>R</code>, answer the
        supplied factory <code>rf</code>. Otherwise, <code>rf</code> must be a RuleReasonerFactory,
        and it is wrapped up in a WrappedRuleReasonerFactory which is loaded with all the specified
        rules.
    */
    private static ReasonerFactory loadFactory( ReasonerFactory rf, Model rs, Resource R )
        {
        WrappedReasonerFactory f = new WrappedReasonerFactory( rf );
    	loadSchemas( rs, R, f );
        loadRulesets( rs, R, f );
        return f;
        }

    /**
        load the factory <code>f</code> with all the rulesets given by the jms:ruleSet and 
        jms:ruleSetURL properties of <code>R</code> in the model <code>rs</code>.
	*/
	private static void loadRulesets( Model rs, Resource R, WrappedReasonerFactory f )
		{
		StmtIterator rulesets = rs.listStatements( R, JMS.ruleSetURL, (RDFNode) null );
		StmtIterator others = rs.listStatements( R, JMS.ruleSet, (RDFNode) null );
		while (rulesets.hasNext()) load( f, rulesets.nextStatement().getResource() );
		while (others.hasNext()) loadNamedRulesets( f, others.nextStatement().getResource() );
		}

	/**
	 	load the factory <code>f</code> with the schemas given by the jms:schemaURL
	 	properties of <code>R</code> in <code>rs</code>.
	*/
	private static void loadSchemas( Model rs, Resource R, WrappedReasonerFactory f )
		{
		StmtIterator schemas = rs.listStatements( R, JMS.schemaURL, (RDFNode) null );
		while (schemas.hasNext())
			{
			Statement s = schemas.nextStatement();
			Resource sc = s.getResource();
			f.bindSchema( FileManager.get().loadModel( sc.getURI() ).getGraph() );
			}
		}

	/**
        load into the factory <code>f</code> any rules described by the jms:hasRule and
        jms:ruleSetURL properties of <code>ruleSet</code>.
    */
    protected static void loadNamedRulesets( RuleReasonerFactory f, Resource ruleSet )
        {
        loadRulesetStrings( f, ruleSet );
        loadRulesetURLs( f, ruleSet );
        }
    
    /**
    	load into the factory <code>f</code> all the rules which are at the URLs
    	specified by the resources-values of the jms:ruleSetURL properties
    	of <code>ruleSet</code>.
    */
    protected static void loadRulesetURLs( RuleReasonerFactory f, Resource ruleSet )
    	{
    	StmtIterator it = ruleSet.listProperties( JMS.ruleSetURL );
    	while (it.hasNext()) load( f, it.nextStatement().getResource() );
    	}

    /**
     	load into the factory <code>f</code> the rules given by the literal strings
     	which are the jms:hasRule properties of <code>ruleSet</code>.
    */
    protected static void loadRulesetStrings( RuleReasonerFactory f, Resource ruleSet )
    	{
    	StmtIterator it = ruleSet.listProperties( JMS.hasRule );
    	while (it.hasNext())
    	    f.addRules( Rule.parseRules( it.nextStatement().getString() ) );
    	}
    
    /**
     load into the factory <code>f</code> the rules at the URL which is the
        URI of the resource <code>u</code>.
    */
    protected static void load( RuleReasonerFactory rf, Resource u )
        { rf.addRules( Rule.rulesFromURL( u.getURI() ) ); }
    }


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/