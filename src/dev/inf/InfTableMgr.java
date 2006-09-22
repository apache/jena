/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.resultset.ResultSetRewindable;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sdb.layout2.LoaderOneTriple;
import com.hp.hpl.jena.sdb.layout2.ValueType;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.sdb.util.StrUtils;

public class InfTableMgr
{
    private Pairs pairs ;
    private Node relation ;
    private String pairsTable ;
    private String colLeft ;
    private String colRight ;
    
    public InfTableMgr(String pairsTable, String colLeft, String colRight, Node relation)
    { 
        this.pairsTable = pairsTable ;
        this.relation = relation ; 
        this.colLeft = colLeft ;
        this.colRight = colRight ;
    }
    
    public void buildPairs(Store store)
    {
        pairs = findPairs(store, relation) ;
        expand(pairs) ;
        expandSelf(pairs) ;
    }
    
    public void writePairs(Store store)
    {
        try 
        { 
            writePairsTable(store, pairsTable, colLeft, colRight, pairs) ;
        } 
        catch (SQLException ex) { throw new SDBExceptionSQL(ex) ; }
    }

    // -------- Internal
    static class IntPair extends Pair<Integer, Integer>
    {
        public IntPair(Integer a, Integer b)
        {
            super(a, b) ;
        }
        // HashCode from parent is OK
        @Override
        public boolean equals(Object other)
        {
            if( ! ( other instanceof IntPair ) ) return false ;
            return super.equals(other) ;
        }
    }

    static class Pairs extends HashSet<IntPair> {} 
    // -------- Internal

    private static Pairs findPairs(Store store, Node relation)
    {
        Pairs pairs = new Pairs() ;
        
        int idSubClassOf = -1 ;
        
        try {
            String q = 
                StrUtils.strjoinNL(
                        //String.format("PREFIX rdfs: <%s>", RDFS.getURI()) ,
                        String.format("SELECT * { ?c1 <%s> ?c2}", relation.getURI())
                        );
            Query query = QueryFactory.create(q) ;
            QueryExecution qExec = QueryExecutionFactory.create(query, new DatasetStore(store)) ;
            
            try {
                ResultSet rs = qExec.execSelect() ;
                if ( false )
                {
                    ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(rs1) ;
                    ResultSetFormatter.out(rs1) ;
                    rs1.reset() ;
                    rs = rs1 ;
                }
                
                while(rs.hasNext())
                {
                    QuerySolution qs = rs.nextSolution() ;
                    Node s = qs.get("c1").asNode() ;
                    Node o = qs.get("c2").asNode() ;
                    int idSubj = node2id(store.getConnection(), s) ;
                    int idObj  = node2id(store.getConnection(), o) ;
                    pairs.add(new IntPair(idSubj, idObj)) ;

                    if ( false )
                        System.out.printf("s[%d]%-20s  rdfs:subClassOf  o[%d]%s\n",
                                          idSubj, FmtUtils.stringForNode(s),
                                          idObj, FmtUtils.stringForNode(o)) ;
                }
            } finally { qExec.close() ; }
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
        }
        return pairs ;
    }

    
    private static void expand(Pairs pairs)
    {
        // Crude.
        for ( ;; )
        {
            Pairs morePairs = new Pairs() ;
            for ( IntPair p : pairs )
            {
                int c1 = p.car() ;
                Set<Integer>x = findByLeft(pairs, p.cdr()) ;
                for ( int c2 : x )
                    morePairs.add(new IntPair(c1, c2)) ;
            }
            
            int size = pairs.size() ;
            pairs.addAll(morePairs) ;
            if ( size == pairs.size() )
                break ;
        } 
    }
    
    
    private static void expandSelf(Pairs pairs)
    {
        // Reflexive.
        // X rdfs:subClassOf X
        // X rdfs:subPropertyOf X 
        Pairs morePairs = new Pairs() ;
        for ( IntPair p : pairs )
        {
            int c1 = p.car() ;
            morePairs.add(new IntPair(c1, c1)) ;
            int c2 = p.cdr() ;
            morePairs.add(new IntPair(c2, c2)) ;
        }
        pairs.addAll(morePairs) ;
    }

    private static Set<Integer> findByLeft(Pairs classes, int c)
    {
        Set<Integer> x = new HashSet<Integer>() ;
        for ( IntPair p : classes )
        {
            if ( p.car() == c )
                x.add(p.cdr()) ;
        }
        return x ;
    }

    static void writePairsTable(Store store, String tableName, String colLeft, String colRight, Pairs pairs) throws SQLException
    {
        if ( SQLUtils.hasTable(store.getConnection().getSqlConnection(), tableName) )
            sql(store, String.format("DROP TABLE %s ;\n", tableName)) ;
        else
            System.out.printf("-- Table not present\n" ) ;
        sql(store, 
            String.format(
                          "CREATE TABLE %s (%s integer not null, %s integer not null)",
                          tableName, colLeft, colRight)) ;

        for ( IntPair p : pairs )
            sql(store,
                String.format(
                              "INSERT INTO %s VALUES(%d, %d) ;\n", tableName, p.car(), p.cdr())) ;

    }


    
    private static void sql(Store store, String sql) throws SQLException
    {
        if ( true )
            store.getConnection().execUpdate(sql) ;
        else
            System.out.println(sql) ;
    }
    
    private static Node id2node(SDBConnection sdb, int id) throws Exception
    {
        String q = String.format("SELECT * FROM Nodes WHERE id = %d", id) ;
        java.sql.ResultSet rs = sdb.execQuery(q) ;
        while(rs.next())
        {
            String lex = rs.getString("lex") ;
            int type = rs.getInt("type") ;
            String datatype =  rs.getString("datatype") ;
            String lang =  rs.getString("lang") ;
            ValueType vType = ValueType.lookup(type) ;
            Node x = makeNode(lex, datatype, lang, vType) ;
            return x ;
        }
        System.err.println("Not found : "+id) ;
        return null ;
    }

    private static int node2id(SDBConnection sdb, Node n) throws Exception
    {
        return LoaderOneTriple.getIndex(sdb, n) ;
    }
    
    private static Node makeNode(String lex, String datatype, String lang, ValueType vType)
    {
        switch (vType)
        {
            case BNODE:
                return Node.createAnon(new AnonId(lex)) ;
            case URI:
                return Node.createURI(lex) ;
            case STRING:
                return Node.createLiteral(lex, lang, false) ;
            case XSDSTRING:
                return Node.createLiteral(lex, null, XSDDatatype.XSDstring) ;
            case INTEGER:
                return Node.createLiteral(lex, null, XSDDatatype.XSDinteger) ;
            case DOUBLE:
                return Node.createLiteral(lex, null, XSDDatatype.XSDdouble) ;
            case DATETIME:       
                return Node.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
            case OTHER:
                RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(datatype);
                return Node.createLiteral(lex, null, dt) ;
            default:
                System.err.println("Unrecognized: ("+lex+", "+lang+", "+vType+")") ;
            return Node.createLiteral("UNRECOGNIZED") ; 
        }
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */