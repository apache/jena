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

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

public abstract class AbstractTestModel extends ModelTestBase
    {
    public AbstractTestModel( String name )
        { super(name); }

    public abstract Model getModel();
    
    private Model model;
    
    @Override
    public void setUp()
        { model = getModel(); }
        
    @Override
    public void tearDown()
        { model.close(); } 
       
    public void testTransactions()
        { 
        Command cmd = new Command() 
        	{ @Override
            public Object execute() { return null; } };
        if (model.supportsTransactions()) model.executeInTransaction( cmd );
        }
        
    public void testCreateResourceFromNode()
        {
        RDFNode S = model.getRDFNode( NodeCreateUtils.create( "spoo:S" ) ); 
        assertInstanceOf( Resource.class, S );
        assertEquals( "spoo:S", ((Resource) S).getURI() );
        }
        
    public void testCreateLiteralFromNode()
        {
        RDFNode S = model.getRDFNode( NodeCreateUtils.create( "42" ) ); 
        assertInstanceOf( Literal.class, S );
        assertEquals( "42", ((Literal) S).getLexicalForm() );
        }    
            
   public void testCreateBlankFromNode()
        {
        RDFNode S = model.getRDFNode( NodeCreateUtils.create( "_Blank" ) ); 
        assertInstanceOf( Resource.class, S );
        assertEquals( new AnonId( "_Blank" ), ((Resource) S).getId() );
        }
        
    public void testIsEmpty()
        {
        Statement S1 = statement( model, "model rdf:type nonEmpty" );
        Statement S2 = statement( model, "pinky rdf:type Pig" );
        assertTrue( model.isEmpty() );
        model.add( S1 );
        assertFalse( model.isEmpty() );
        model.add( S2 );
        assertFalse( model.isEmpty() );
        model.remove( S1 );
        assertFalse( model.isEmpty() );
        model.remove( S2 );
        assertTrue( model.isEmpty() );
        }
        
    public void testContainsResource()
        {
        modelAdd( model, "x R y; _a P _b" );
        assertTrue( model.containsResource( resource( model, "x" ) ) );
        assertTrue( model.containsResource( resource( model, "R" ) ) );
        assertTrue( model.containsResource( resource( model, "y" ) ) );
        assertTrue( model.containsResource( resource( model, "_a" ) ) );
        assertTrue( model.containsResource( resource( model, "P" ) ) );
        assertTrue( model.containsResource( resource( model, "_b" ) ) );
        assertFalse( model.containsResource( resource( model, "i" ) ) );
        assertFalse( model.containsResource( resource( model, "_j" ) ) );
        }
        
    /**
        Test the new version of getProperty(), which delivers null for not-found
        properties.
    */
    public void testGetProperty()
        {
        modelAdd( model, "x P a; x P b; x R c" );
        Resource x = resource( model, "x" );
        assertEquals( resource( model, "c" ), x.getProperty( property( model, "R" ) ).getObject() );
        RDFNode ob = x.getProperty( property( model, "P" ) ).getObject();
        assertTrue( ob.equals( resource( model, "a" ) ) || ob.equals( resource( model, "b" ) ) );
        assertNull( x.getProperty( property( model, "noSuchPropertyHere" ) ) );
        }
    
    public void testToStatement()
        {
        Triple t = triple( "a P b" );
        Statement s = model.asStatement( t );
        assertEquals( node( "a" ), s.getSubject().asNode() );
        assertEquals( node( "P" ), s.getPredicate().asNode() );
        assertEquals( node( "b" ), s.getObject().asNode() );
        }
    
    public void testAsRDF()
        {
        testPresentAsRDFNode( node( "a" ), Resource.class );
        testPresentAsRDFNode( node( "17" ), Literal.class );
        testPresentAsRDFNode( node( "_b" ), Resource.class );
        }

    private void testPresentAsRDFNode( Node n, Class<? extends RDFNode> nodeClass )
        {
        RDFNode r = model.asRDFNode( n );
        assertSame( n, r.asNode() );
        assertInstanceOf( nodeClass, r );
        }
        
    public void testURINodeAsResource()
        {
        Node n = node( "a" );
        Resource r = model.wrapAsResource( n );
        assertSame( n, r.asNode() );
        }
        
    public void testLiteralNodeAsResourceFails()
        {
        try 
            {
            model.wrapAsResource( node( "17" ) );
            fail( "should fail to convert literal to Resource" );
            }
        catch (UnsupportedOperationException e)
            { pass(); }
        }
    
    public void testRemoveAll()
        {
        testRemoveAll( "" );
        testRemoveAll( "a RR b" );
        testRemoveAll( "x P y; a Q b; c R 17; _d S 'e'" );
        testRemoveAll( "subject Predicate 'object'; http://nowhere/x scheme:cunning not:plan" );
        }
    
    protected void testRemoveAll( String statements )
        {
        modelAdd( model, statements );
        assertSame( model, model.removeAll() );
        assertEquals( "model should have size 0 following removeAll(): ", 0, model.size() );
        }
    
    /**
 		Test cases for RemoveSPO(); each entry is a triple (add, remove, result).
	 	<ul>
	 	<li>add - the triples to add to the graph to start with
	 	<li>remove - the pattern to use in the removal
	 	<li>result - the triples that should remain in the graph
	 	</ul>
	*/
	protected String[][] cases =
		{
	            { "x R y", "x R y", "" },
	            { "x R y; a P b", "x R y", "a P b" },
	            { "x R y; a P b", "?? R y", "a P b" },
	            { "x R y; a P b", "x R ??", "a P b" },
	            { "x R y; a P b", "x ?? y", "a P b" },      
	            { "x R y; a P b", "?? ?? ??", "" },       
	            { "x R y; a P b; c P d", "?? P ??", "x R y" },       
	            { "x R y; a P b; x S y", "x ?? ??", "a P b" },                 
		};
    
    /**
 	Test that remove(s, p, o) works, in the presence of inferencing graphs that
 	mean emptyness isn't available. This is why we go round the houses and
 	test that expected ~= initialContent + addedStuff - removed - initialContent.
 	*/
	public void testRemoveSPO()
	    {
	    ModelCom mc = (ModelCom) ModelFactory.createDefaultModel();
            for ( String[] aCase : cases )
            {
                for ( int j = 0; j < 3; j += 1 )
                {
                    Model content = getModel();
                    Model baseContent = copy( content );
                    modelAdd( content, aCase[0] );
                    Triple remove = triple( aCase[1] );
                    Node s = remove.getSubject(), p = remove.getPredicate(), o = remove.getObject();
                    Resource S = (Resource) ( s.equals( Node.ANY ) ? null : mc.getRDFNode( s ) );
                    Property P = ( ( p.equals( Node.ANY ) ? null : mc.getRDFNode( p ).as( Property.class ) ) );
                    RDFNode O = o.equals( Node.ANY ) ? null : mc.getRDFNode( o );
                    Model expected = modelWithStatements( aCase[2] );
                    content.removeAll( S, P, O );
                    Model finalContent = copy( content ).remove( baseContent );
                    assertIsoModels( aCase[1], expected, finalContent );
                }
            }
	    }
	
    public void testIsClosedDelegatedToGraph()
        {
        Model m = getModel();
        assertFalse( m.isClosed() );
        m.close();
        assertTrue( m.isClosed() );
        }
    
	protected Model copy( Model m )
	    {
	    return ModelFactory.createDefaultModel().add( m );
	    }
    }
