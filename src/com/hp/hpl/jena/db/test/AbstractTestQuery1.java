/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: AbstractTestQuery1.java,v 1.1 2003-08-19 02:33:00 wkw Exp $
*/

package com.hp.hpl.jena.db.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.graph.query.*;
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

    public abstract Graph getGraph();
	public abstract Graph getGraph(Reifier.Style style);

    
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
    
    public static TestSuite suite()
        { return new TestSuite( AbstractTestQuery1.class ); }
                
    public void setUp()
        {
		}
		
	protected void tearDown() throws java.lang.Exception {
		}


	protected String longpfx = null;
	
	protected String longPrefix() {
		if ( longpfx == null ) {
			String s = ".";
			for(int i=0;i<255;i++) s += ".";
			longpfx = s;
		}
		return longpfx;
	}
	
	protected Node makeResource ( String u ) {
		boolean isLong = u.charAt(1) == 'L';
		if ( isLong ) {
			u = longPrefix() + u.substring(1);
		}
		return Node.createURI(u);		
	}
	
	protected Node makeObject ( String u ) {
		boolean isRef = u.charAt(0) == 'U';
		if ( isRef ) return makeResource(u.substring(1));
		boolean isLong = u.charAt(1) == 'L';
		if ( isLong ) {
			u = longPrefix() + u.substring(1);
		}
		return Node.createLiteral(new LiteralLabel(u));		
	}

	static int stmtCnt = 0;
	
	protected void loadGraph ( Graph g ) {
		Node s,p,o; String k; Node n = null;
		Triple t;
		boolean isReified;
		
		for(int i=0; i<statementList.length; i++) {
			StringTokenizer st = new StringTokenizer(statementList[i]);
			k = st.nextToken();
			if ( k.charAt(0) == 'N' ) {
				isReified = true;
				n = makeResource(k);
				k = st.nextToken();
			} else
				isReified = false;
			s = makeResource(k);
			p = makeResource(st.nextToken());
			o = makeObject(st.nextToken());
			t = new Triple(s,p,o);
			if ( isReified )
				g.getReifier().reifyAs(n,t);
			else
				g.add(t);
			stmtCnt++;
		}
	}
	
    protected int queryResultCount ( ExtendedIterator it ) {
    	int n = 0;
    	List r;
    	while ( it.hasNext() ) {
    		n++;
    		r = (List) it.next();
    		int i = r.size();
    	}
	    return n;
    }

     
    public void testBinding1( )
        {
        Graph g = getGraph(Reifier.Standard);
        Graph gc = getGraph (Reifier.Convenient);
        loadGraph(g); loadGraph(gc);
        
        Query q;
        Node V1 = node( "?v1" ), V2 = node( "?v2" ), V3 = node( "?v3" );
        Node V4 = node( "?v4" ), V5 = node( "?v5" );
		BindingQueryPlan qp;
		ExtendedIterator it;

        // property constants
        Node Ptitle = makeResource("Ptitle");
        Node Psex = makeResource("Psex");
        Node Pname = makeResource("Pname");
		Node Pmgr = makeResource("Pmgr");
		Node Pcard = makeResource("Pcard");
		Node Pcardmax = makeResource("Pcardmax");
		Node Prange = makeResource("Prange");
		Node Pdept = makeResource("Pdept");
		Node S1 = makeResource("S1");
	
		// object constants
		Node Ogrunt = makeObject("Ogrunt");
		Node Ofemale = makeObject("Ofemale");
		Node Omale = makeObject("Omale");
		Node Obigboss = makeObject("Obigboss");
		Node Oboss = makeObject("Oboss");
		Node Oshane = makeObject("Oshane");
		Node Oliteral = makeObject("Oliteral");
		Node Oresource = makeObject("Oresource");
		Node Oapp = makeObject("Oapp");
		Node Ogenesis = makeObject("Ogenesis");

		Node O1 = makeObject("O1");

		// Q0: how many female grunts?
		// a simple select
		q = new Query();
		q.addMatch( V1, Ptitle, Ogrunt );
		q.addMatch( V1, Psex, Ofemale );
		q.addMatch( V1, Pname, V3 );	
	
        qp = g.queryHandler().prepareBindings( q, new Node[] {V1,V3} );
        it = qp.executeBindings();
        assertEquals( "female grunts", 1, queryResultCount(it) ); it.close();
        
        // Q1: get names of managers of female grunts
        // this has a joining variable
		q = new Query();
		q.addMatch( V1, Ptitle, Ogrunt );
		q.addMatch( V1, Psex, Ofemale );
		q.addMatch( V1, Pmgr, V2 );
		q.addMatch( V2, Pname, V3 );	
	
		qp = g.queryHandler().prepareBindings( q, new Node[] {V1,V3} );
		it = qp.executeBindings();
		assertEquals( "mgrs of female grunts", 1, queryResultCount(it) ); it.close();

		// Q2: get names of female grunts with female managers
		q = new Query();
		q.addMatch( V1, Ptitle, Ogrunt );
		q.addMatch( V1, Psex, Ofemale );
		q.addMatch( V1, Pmgr, V2 );
		q.addMatch( V2, Psex, Ofemale );
		q.addMatch( V1, Pname, V3 );	
	
		qp = g.queryHandler().prepareBindings( q, new Node[] {V1,V3} );
		it = qp.executeBindings();
		assertEquals( "female grunts with female mgrs", 0, queryResultCount(it) ); it.close();
		
		// Q3.0: get all properties of the bigboss
		q = new Query();
		q.addMatch( V1, Ptitle, Obigboss );
		q.addMatch( V1, Pmgr, Oshane );
		q.addMatch( V1, V2, V3 );
	
		qp = g.queryHandler().prepareBindings( q, new Node[] {V1,V2,V3} );
		it = qp.executeBindings();
		assertEquals( "all properties of the bigboss", 4, queryResultCount(it) ); it.close();


		// Q3: get all properties of female grunts with male managers
		// this has a predicate variable. for standard reification, it
		// requires a multi-stage query. for convenient, minimal, it can
		// be done as a single stage (since reification is not queried).
		q = new Query();
		q.addMatch( V1, Ptitle, Ogrunt );
		q.addMatch( V1, Psex, Ofemale );
		q.addMatch( V1, Pmgr, V2 );
		q.addMatch( V2, Psex, Omale );
		q.addMatch( V1, V3, V4 );	
	
		qp = g.queryHandler().prepareBindings( q, new Node[] {V1,V3,V4} );
		it = qp.executeBindings();
		assertEquals( "all properties of female grunts with male mgrs", 6, queryResultCount(it) ); it.close();
       
		// Q4: get all single-valued, required, literal properties of the bigboss
		// similar to Q3 in terms of stages.
		q = new Query();
		q.addMatch( V1, Ptitle, Obigboss );
		q.addMatch( V1, Pmgr, Oshane );
		q.addMatch( V2, Pcard, O1 );
		q.addMatch( V2, Prange, Oliteral );
		q.addMatch( V1, V2, V3 );
	
		qp = g.queryHandler().prepareBindings( q, new Node[] {V2,V3} );
		it = qp.executeBindings();
		assertEquals( "single-valued, literal properties of the bigboss", 2, queryResultCount(it) ); it.close();


		// Q5: list the name and gender of martin's boss, where the pmgr property
		// is determined by a query).
		// similar to Q3 in terms of stages.
		q = new Query();
		q.addMatch( V1, Pcardmax, O1 );        // get the mgr property
		q.addMatch( V1, Prange, Oresource );
		q.addMatch( S1, V1, V2 );         // get mm's mgr
		q.addMatch( V2, Pname, V3 );
		q.addMatch( V2, Psex, V4 );		
	
		qp = g.queryHandler().prepareBindings( q, new Node[] {V2,V3,V4} );
		it = qp.executeBindings();
		assertEquals( "name, gender of referenced people", 1, queryResultCount(it) ); it.close();

		// Q6: list the reified subjects, predicates and objects.
		// should return nothing for minimal, convenient reification.
		q = new Query();
		q.addMatch( V1, RDF.Nodes.subject, V2 ); 
		q.addMatch( V1, RDF.Nodes.predicate, V3 );
		q.addMatch( V1, RDF.Nodes.object, V4 );
	
		qp = g.queryHandler().prepareBindings( q, new Node[] {V2,V3,V4} );
		it = qp.executeBindings();
		assertEquals( "number of reified statements", 27, queryResultCount(it) ); it.close();

		qp = gc.queryHandler().prepareBindings( q, new Node[] {V2,V3,V4} );
		it = qp.executeBindings();
		assertEquals( "number of reified statements", 0, queryResultCount(it) ); it.close();

		
		// Q7: list the reified predicates about the bigboss.
		// should return nothing for minimal, convenient reification.
		q = new Query();
		q.addMatch( V1, RDF.Nodes.subject, V2 ); 
		q.addMatch( V1, RDF.Nodes.predicate, Ptitle );
		q.addMatch( V1, RDF.Nodes.object, Obigboss );
		q.addMatch( V3, RDF.Nodes.subject, V2 ); 
		q.addMatch( V3, RDF.Nodes.predicate, V4 ); 

		qp = g.queryHandler().prepareBindings( q, new Node[] {V2,V3} );
		it = qp.executeBindings();
		assertEquals( "number of reified statements", 4, queryResultCount(it) ); it.close();
		
		// Q8: list the reification quads for the bigboss.
		// should return nothing for minimal, convenient reification.
		q = new Query();
		q.addMatch( V1, RDF.Nodes.subject, V2 ); 
		q.addMatch( V1, RDF.Nodes.predicate, Ptitle );
		q.addMatch( V1, RDF.Nodes.object, Obigboss );
		q.addMatch( V3, RDF.Nodes.subject, V2 ); 
		q.addMatch( V3, V4, V5 ); // haven't figured out how to specify any so use dummy variables

		qp = g.queryHandler().prepareBindings( q, new Node[] {V3} );
		it = qp.executeBindings();
		assertEquals( "number of reified statements", 16, queryResultCount(it) ); it.close();



        }

    }


/*
    (c) Copyright Hewlett-Packard Company 2003
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