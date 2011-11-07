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

package com.hp.hpl.jena.db.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.vocabulary.RDF;


import java.util.*;
import junit.framework.*;

/**
    Abstract tests for graph query, parameterised on getGraph().
 	@author kers
*/
public abstract class AbstractTestQuery1 extends GraphTestBase
    {
    public AbstractTestQuery1(String name)
        { super(name); }
    
    public static TestSuite suite()
        { return new TestSuite( AbstractTestQuery1.class ); }

    public abstract Graph getGraph();
	public abstract Graph getGraph( ReificationStyle style );

    
    // in the stmt strings below, L indicates a long (object or predicate)
    // L, if specifed, MUST precede the URI. for the object, a prefix of
    // U indicates a reference (URI). otherwise, the object is considered
    // a literal string. if the string begins with R, it is treated as
    // a reified statement with 4 tokens, where the first is the URI that
    // reifies the subsequent statement.
    
    // the database has the following pattern. 
    
    protected String[] statementList = {
    	
    	// metadata about properties
    	
    	"Pname	Pcard	O1",
    	"Psex	Pcard	O1",
    	"Pdept	Pcardmax	O5",
    	"Pmgr	Pcardmax	O1",
    	"Ptitle	Pcardmax	O5",
    	
		"Pname	Prange	Oliteral",
		"Psex	Prange	Oliteral",
		"Pdept	Prange	Oliteral",
		"Pmgr	Prange	Oresource",
		"Ptitle	Prange	Oliteral",

		"S1		Pname	Omm",
		"S1		Psex	Omale",
		"S1		Pdept	Ojena",
		"S1		Pdept	Oapp",
		"S1		Pmgr	US4",
		"S1		Ptitle	Oboss",
		
		"S2		Pname	Obb",
		"S2		Psex	Omale",
		"S2		Pdept	Ogenesis",
		"S2		Pdept	Oapp",
		"S2		Pmgr	US4",
		"S2		Ptitle	Oboss",

		"S3		Pname	Ojb",
		"S3		Psex	Ofemale",
		"S3		Pdept	Oapp",
		"S3		Pmgr	US1",
		"S3		Ptitle	Oboss",
		
		"S4		Pname	Odick",
		"S4		Psex	Omale",
		"S4		Pmgr	Oshane",
		"S4		Ptitle	Obigboss",
   	
		"S5		Pname	Okers",
		"S5		Psex	Omale",
		"S5		Pdept	Ojena",
		"S5		Pdept	Ordql",
		"S5		Pmgr	US1",
		"S5		Ptitle	Ogrunt",
			
		"S6		Pname	Ojjc",
		"S6		Psex	Omale",
		"S6		Pdept	Ojena",
		"S6		Pdept	Ordf",
		"S6		Pdept	Oowl",
		"S6		Pmgr	US1",
		"S6		Ptitle	Ogrunt",
		
		"S7		Pname	Oder",
		"S7		Psex	Omale",
		"S7		Pdept	Ojena",
		"S7		Pdept	Ordf",
		"S7		Pdept	Oowl",
		"S7		Pmgr	US1",
		"S7		Ptitle	Ogrunt",
		"S7		Ptitle	Oboss",
		
		"S8		Pname	Obmk",
		"S8		Psex	Omale",
		"S8		Pdept	Ojena",
		"S8		Pdept	Ordf",
		"S8		Pmgr	US1",
		"S8		Ptitle	Ogrunt",
		"S8		Ptitle	Oboss",
		
		"S9		Pname	Oas",
		"S9		Psex	Omale",
		"S9		Pdept	Ojena",
		"S9		Pdept	Ordf",
		"S9		Pdept	Ordql",
		"S9		Pmgr	US1",
		"S9		Ptitle	Ogrunt",
		
		"S10		Pname	Oian",
		"S10		Psex	Omale",
		"S10		Pdept	Ojena",
		"S10		Pdept	Oowl",
		"S10		Pmgr	US1",
		"S10		Ptitle	Ogrunt",

		"S11		Pname	Osteveb",
		"S11		Psex	Omale",
		"S11		Pdept	Oapp",
		"S11		Pmgr	US3",
		"S11		Ptitle	Ogrunt",

		"S12		Pname	Ostevec",
		"S12		Psex	Omale",
		"S12		Pdept	Oapp",
		"S12		Pmgr	US3",
		"S12		Ptitle	Ogrunt",

		"S13		Pname	Ocs",
		"S13		Psex	Omale",
		"S13		Pdept	Ojena",
		"S13		Pdept	Ogenesis",
		"S13		Pmgr	US2",
		"S13		Ptitle	Ogrunt",
		
		"S14		Pname	Ohk",
		"S14		Psex	Ofemale",
		"S14		Pdept	Oapp",
		"S14		Pdept	Ogenesis",
		"S14		Pmgr	US2",
		"S14		Ptitle	Ogrunt",

		"S15		Pname	Oks",
		"S15		Psex	Omale",
		"S15		Pdept	Oapp",
		"S15		Pdept	Ogenesis",
		"S15		Pmgr	US2",

		"S16		Pname	Owkw",
		"S16		Psex	Omale",
		"S16		Pdept	Ojena",
		"S16		Pdept	Oapp",
		"S16		Pmgr	US2",
		"S16		Ptitle	Ogrunt",
		
		// reify the first few sets of statements
		
		"N1		S1		Pname	Omm",
		"N2		S1		Psex	Omale",
		"N3		S1		Pdept	Ojena",
		"N4		S1		Pdept	Oapp",
		"N5		S1		Pmgr	US4",
		"N6		S1		Ptitle	Oboss",
		
		"N7		S2		Pname	Obb",
		"N8		S2		Psex	Omale",
		"N9		S2		Pdept	Ogenesis",
		"N10	S2		Pdept	Oapp",
		"N11	S2		Pmgr	US4",
		"N12	S2		Ptitle	Oboss",

		"N13	S3		Pname	Ojb",
		"N14	S3		Psex	Ofemale",
		"N15	S3		Pdept	Oapp",
		"N16	S3		Pmgr	US1",
		"N17	S3		Ptitle	Oboss",
		
		"N18	S4		Pname	Odick",
		"N19	S4		Psex	Omale",
		"N20	S4		Pmgr	Oshane",
		"N21	S4		Ptitle	Obigboss",
   	
		"N22	S5		Pname	Okers",
		"N23	S5		Psex	Omale",
		"N24	S5		Pdept	Ojena",
		"N25	S5		Pdept	Ordql",
		"N26	S5		Pmgr	US1",
		"N27	S5		Ptitle	Ogrunt",

    };
    
    @Override
    public void setUp() throws Exception
        {        
        super.setUp();
		}
		
    protected Graph standard()
        { return fetchGraph( ReificationStyle.Standard ); }
        
     protected Graph convenient()
        { return fetchGraph( ReificationStyle.Convenient ); }
        
    protected Graph fetchGraph( ReificationStyle style )
        { Graph s = getGraph( style );
        loadGraph( s );
        return s; }
 
	@Override
    protected void tearDown() throws Exception {
        super.tearDown();
		}

    /**
        The lots-of-dots prefix to use to make things long
    */
	protected final String longPrefix = makeLongPrefix();
	
    /**
     	Answer a string of 256 dots.
    */
    private String makeLongPrefix()
        { StringBuffer sb = new StringBuffer( 256 );
        for (int i = 0; i < 256; i += 1) sb.append( '.' );
        return sb.toString(); }
	
	protected Node makeResource ( String u ) 
        { return Node.createURI( expandLong( u ) ); }
    
	protected Node makeObject ( String u ) {
		boolean isRef = u.charAt(0) == 'U';
        return 
            isRef ? makeResource( u.substring(1) )
            : Node.createLiteral( LiteralLabelFactory.create( expandLong( u ) ) );	
	}

    protected String expandLong( String s )
        { return s.charAt(0) == 'L' ? longPrefix + s.substring(1) : s; }
    
	static int stmtCnt = 0;
	
	protected void loadGraph ( Graph g ) {
        Reifier r = g.getReifier();
		for (int i = 0; i < statementList.length; i++) {
			StringTokenizer st = new StringTokenizer( statementList[i] );
			String k = st.nextToken();
			if ( k.charAt(0) == 'N' )
                r.reifyAs( makeResource( k ), nextTriple( st.nextToken(), st ) );
			else
                g.add( nextTriple( k, st ) );
			stmtCnt++;
		}
	}
    
    protected Triple nextTriple( String k, StringTokenizer st )
        { Node s = makeResource( k );
        Node p = makeResource( st.nextToken() );
        Node o = makeObject( st.nextToken() );
        return Triple.create( s, p, o ); }

    final Node V1 = node( "?v1" );
    final Node V2 = node( "?v2" );
    final Node V3 = node( "?v3" );
    final Node V4 = node( "?v4" );
    final Node V5 = node( "?v5" );

    final Node Ptitle = makeResource("Ptitle");
    final Node Psex = makeResource("Psex");
    final Node Pname = makeResource("Pname");
    final Node Pmgr = makeResource("Pmgr");
    final Node Pcard = makeResource("Pcard");
    final Node Pcardmax = makeResource("Pcardmax");
    final Node Prange = makeResource("Prange");
    final Node Pdept = makeResource("Pdept");
    final Node S1 = makeResource("S1");
    
        // object constants
    final Node Ogrunt = makeObject("Ogrunt");
    final Node Ofemale = makeObject("Ofemale");
    final Node Omale = makeObject("Omale");
    final Node Obigboss = makeObject("Obigboss");
    final Node Oboss = makeObject("Oboss");
    final Node Oshane = makeObject("Oshane");
    final Node Oliteral = makeObject("Oliteral");
    final Node Oresource = makeObject("Oresource");
    final Node Oapp = makeObject("Oapp");
    final Node Ogenesis = makeObject("Ogenesis");

    final Node O1 = makeObject("O1");
             
    public void test0()
        {
        Query query = new Query();
        query.addMatch( V1, Ptitle, Ogrunt );
        query.addMatch( V1, Psex, Ofemale );
        query.addMatch( V1, Pname, V3 );        
        checkCount( 1, standard(), query, new Node[] {V1,V3} );
        }
        
    /**    
        Q1: get names of managers of female grunts;  this has a joining variable.
    */
    public void test1()
        {
        Query query = new Query();
        query.addMatch( V1, Ptitle, Ogrunt );
        query.addMatch( V1, Psex, Ofemale );
        query.addMatch( V1, Pmgr, V2 );
        query.addMatch( V2, Pname, V3 );    
        checkCount( 1, standard(), query, new Node[] {V1,V3} );
        }
        
    /**
        Q2: get names of female grunts with female managers
    */
    public void test2()
        {
        Query query = new Query();
        query.addMatch( V1, Ptitle, Ogrunt );
        query.addMatch( V1, Psex, Ofemale );
        query.addMatch( V1, Pmgr, V2 );
        query.addMatch( V2, Psex, Ofemale );
        query.addMatch( V1, Pname, V3 );     
        checkCount( 0, standard(), query, new Node[] {V1,V3} ); 
        }
        
    /**
        Q3.0: get all properties of the bigboss
    */
    public void test3a()
        {
        Query query = new Query();
        query.addMatch( V1, Ptitle, Obigboss );
        query.addMatch( V1, Pmgr, Oshane );
        query.addMatch( V1, V2, V3 );
        checkCount( 4, standard(), query, new Node[] {V1,V2,V3} );
        }
        
    /**
        Q3: get all properties of female grunts with male managers
        this has a predicate variable. for standard reification, it
        requires a multi-stage query. for convenient, minimal, it can
        be done as a single stage (since reification is not queried). 
    */
    public void test3b()
        {
        Query query = new Query();
        query.addMatch( V1, Ptitle, Ogrunt );
        query.addMatch( V1, Psex, Ofemale );
        query.addMatch( V1, Pmgr, V2 );
        query.addMatch( V2, Psex, Omale );
        query.addMatch( V1, V3, V4 );   
        checkCount( 6, standard(), query, new Node[] {V1,V3,V4} );
        }   
         
    /**
        Q4: get all single-valued, required, literal properties of the bigboss
        similar to Q3 in terms of stages.
    */
    public void test4()
        {
        Query query = new Query();
        query.addMatch( V1, Ptitle, Obigboss );
        query.addMatch( V1, Pmgr, Oshane );
        query.addMatch( V2, Pcard, O1 );
        query.addMatch( V2, Prange, Oliteral );
        query.addMatch( V1, V2, V3 );
        checkCount( 2, standard(), query, new Node[] {V2,V3} );
        }
        
    /**
        Q5: list the name and gender of martin's boss, where the pmgr property
        is determined by a query).
        similar to Q3 in terms of stages.     
    */
    public void test5()
        {
        Query query = new Query();
        query.addMatch( V1, Pcardmax, O1 );        // get the mgr property
        query.addMatch( V1, Prange, Oresource );
        query.addMatch( S1, V1, V2 );         // get mm's mgr
        query.addMatch( V2, Pname, V3 );
        query.addMatch( V2, Psex, V4 );     
        checkCount( 1, standard(), query, new Node[] {V2,V3,V4} );      
        }
        
    /**
        Q6: list the reified subjects, predicates and objects.
        should return nothing for minimal, convenient reification.
    */
    public void test6()
        {
        Query query = new Query();
        query.addMatch( V1, RDF.Nodes.subject, V2 ); 
        query.addMatch( V1, RDF.Nodes.predicate, V3 );
        query.addMatch( V1, RDF.Nodes.object, V4 );
    /* */
        checkCount( 27, standard(), query, new Node[] {V2,V3,V4} );
        checkCount( 0, convenient(), query, new Node[] {V2,V3,V4} );
        }
        
    /**
        Q7: list the reified predicates about the bigboss.
        should return nothing for minimal, convenient reification.
    */
    public void test7()
        {
        Query query = new Query();
        query.addMatch( V1, RDF.Nodes.subject, V2 ); 
        query.addMatch( V1, RDF.Nodes.predicate, Ptitle );
        query.addMatch( V1, RDF.Nodes.object, Obigboss );
        query.addMatch( V3, RDF.Nodes.subject, V2 ); 
        query.addMatch( V3, RDF.Nodes.predicate, V4 ); 
    /* */
        checkCount( 4, standard(), query, new Node[] {V2,V3} );
        }
        
    /**
        Q8: list the reification quads for the bigboss.
        should return nothing for minimal, convenient reification.   
    */
    public void test8()
        {
        Query query = new Query();
        query.addMatch( V1, RDF.Nodes.subject, V2 ); 
        query.addMatch( V1, RDF.Nodes.predicate, Ptitle );
        query.addMatch( V1, RDF.Nodes.object, Obigboss );
        query.addMatch( V3, RDF.Nodes.subject, V2 ); 
        query.addMatch( V3, V4, V5 ); // V4 and V5 serve duty as ANY
        checkCount( 16, standard(), query, new Node[] {V3} );
        }
        
    /**
        Check that the number of results obtained from the query over the graph is
        that expected.
        
     	@param expected the number of results expected from the query
     	@param g the graph to run the query over
     	@param q the query to apply to the graph
     	@param results the results-variable array
     */
    private void checkCount( int expected, Graph g, Query q, Node [] results ) 
       {
       BindingQueryPlan plan = g.queryHandler().prepareBindings( q, results );
       ExtendedIterator<Domain> it = plan.executeBindings();
       assertEquals( "number of reified statements", expected, queryResultCount( it ) ); 
       it.close();
	   }
    
    /**
        Answer the number of elements in the iterator; each such element should be
        a List (and we make sure size() works on it, don't know why).
        
     	@param it the iterator to run down
     	@return the number of elements in that iterator
     */
    protected static int queryResultCount( ExtendedIterator<Domain> it ) {
        int n = 0;
        while (it.hasNext()) {
            n++;
            it.next().size();  // hedgehog asks, do we need to check this works?
        }
        return n;
    }

    }
