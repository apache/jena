/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;



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
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.sdb.util.StrUtils;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ClassTable
{
    static class ClassPair extends Pair<Integer, Integer>
    {

        public ClassPair(Integer a, Integer b)
        {
            super(a, b) ;
        }

        @Override
        public int hashCode()
        {
            return car().hashCode() | cdr().hashCode() ; 
        }

        @Override
        public boolean equals(Object other)
        {
            if( ! ( other instanceof ClassPair ) ) return false ;
            ClassPair p2 = (ClassPair)other ;
            return  car().equals(p2.car()) && cdr().equals(p2.cdr()) ;
        }
    }
        
    
    public static void main(String[]argv)
    {
        final String classTable = "Classes";
        SDBConnection.logSQLExceptions = true ;
        //SDBConnection.logSQLStatements = true ;
        
        // All wrong!  Not a map, but a set of pairs, pair= = value=.
        Set<ClassPair> classes = findSubClasses() ;
        System.out.printf("START\n") ;
        print(classes) ;
        
        for ( ;; )
        {
            System.out.printf("--------------\n") ;
            Set<ClassPair> moreClasses = new HashSet<ClassPair>() ;
            for ( ClassPair p : classes )
            {
                int c1 = p.car() ;
                Set<Integer>x = findByLeft(classes, p.cdr()) ;
                for ( int c2 : x )
                {
                    add(moreClasses, c1, c2) ;
                }
                
                
            }
            System.out.printf("Adding:\n") ;
            print(moreClasses) ;
            
            int size = classes.size() ;
            classes.addAll(moreClasses) ;
            if ( size == classes.size() )
            {
                System.out.printf("No change\n" ) ;
                break ;
            }
            System.out.printf("Sizes %d ==> %d\n" , size, classes.size()) ; 
        } 
        System.out.printf("--------------\n") ;
        System.out.printf("Exit:\n") ;
        print(classes) ;
        
        // Now put self in
        
        Set<ClassPair> moreClasses = new HashSet<ClassPair>() ;
        for ( ClassPair p : classes )
        {
            int c1 = p.car() ;
            add(moreClasses, c1, c1) ;
            int c2 = p.cdr() ;
            add(moreClasses, c2, c2) ;
        }
        classes.addAll(moreClasses) ;
        System.out.printf("==========================\n") ;
        print(classes) ;
        System.out.printf("==========================\n") ;
        System.out.printf("DROP TABLE %s ;\n", classTable) ;
        System.out.printf("CREATE TABLE %s (\n", classTable) ;
        System.out.printf("                 subClass integer not null,\n") ;
        System.out.printf("                 superClass integer not null\n") ;
        System.out.printf("                ) ;\n", classTable) ;
        for ( ClassPair p : classes )
        {
            System.out.printf("INSERT INTO %s VALUES(%d, %d) ;\n", classTable, p.car(), p.cdr()) ;
        }
        
    }

    static Set<Integer> findByLeft(Set<ClassPair>classes, int c)
    {
        Set<Integer> x = new HashSet<Integer>() ;
        for ( ClassPair p : classes )
        {
            if ( p.car() == c )
                x.add(p.cdr()) ;
        }
        return x ;
    }
    
    static void print(Set<ClassPair> classes)
    {
        for ( ClassPair p : classes )
            System.out.printf("%d ==> %d\n", p.car(), p.cdr()) ;
    }

    
    
    static void add(Set<ClassPair> classes, int c1, int c2)
    {
        classes.add(new ClassPair(c1, c2)) ;
    }
    
    static Set<ClassPair> findSubClasses()
    {
        Set<ClassPair> classes = new HashSet<ClassPair>() ;
        
        int idSubClassOf = -1 ;
        
        try {
            StoreDesc storeDesc = StoreDesc.read("sdb.ttl") ;
            Store store = StoreFactory.create(storeDesc) ;
            String q = 
                StrUtils.strjoinNL(
                        String.format("PREFIX rdfs: <%s>", RDFS.getURI()) ,
                        "SELECT * { ?c1 rdfs:subClassOf ?c2}"
                        );
            Query query = QueryFactory.create(q) ;
            QueryExecution qExec = QueryExecutionFactory.create(query, new DatasetStore(store)) ;
            
            try {
                ResultSet rs1 = qExec.execSelect() ;
                ResultSetRewindable rs = ResultSetFactory.makeRewindable(rs1) ;
                ResultSetFormatter.out(rs) ;
                rs.reset() ;
                
                while(rs.hasNext())
                {
                    QuerySolution qs = rs.nextSolution() ;
                    Node s = qs.get("c1").asNode() ;
                    Node o = qs.get("c2").asNode() ;
                    int idSubj = lookup(store.getConnection(), s) ;
                    int idObj = lookup(store.getConnection(), o) ;
                    System.out.printf("s[%d]%-20s  rdfs:subClassOf  o[%d]%s\n",
                                      idSubj, FmtUtils.stringForNode(s),
                                      idObj, FmtUtils.stringForNode(o)) ;
                    add(classes, idSubj, idObj) ;
                }
            } finally { qExec.close() ; }
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
        }
        return classes ;
    }
    
    static Node build(SDBConnection sdb, int id) throws Exception
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

    static int lookup(SDBConnection sdb, Node n) throws Exception
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