/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;



import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import arq.cmd.CmdUtils;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.resultset.ResultSetRewindable;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.data.CustomizeType;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.layout2.LoaderOneTriple;
import com.hp.hpl.jena.sdb.layout2.QueryCompiler2;
import com.hp.hpl.jena.sdb.layout2.ValueType;
import com.hp.hpl.jena.sdb.sql.RS;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.*;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.sdb.util.PrintSDB;
import com.hp.hpl.jena.sdb.util.StrUtils;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ClassTable
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }
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

    static class SubClasses extends HashSet<ClassPair> {} 

    static final String classTable = SubClassTable.tableSubClass ;

    public static void main(String[]argv)
    {
        StoreDesc storeDesc = StoreDesc.read("sdb.ttl") ;
        Store store = StoreFactory.create(storeDesc) ;
        store.getConnection().setLogSQLExceptions(true) ;
        try {
            formatAndLoadStore(store) ;
            buildSubClassTable(store) ;
            dumpClassTable(store) ;
            play(store) ;
        } catch (Exception ex) 
        { ex.printStackTrace(System.err) ; }
        finally { store.close() ; }
    }

    
    
    private static void play(Store store)
    {
        System.out.println("\nEXPERIMENT\n") ;
        QueryCompiler qc = new QueryCompiler2(new BlockCompilerSubClass(), null, null) ;

        Store store2 = new StoreBase(store.getConnection(),
                                     store.getPlanTranslator(),
                                     store.getLoader(),
                                     store.getTableFormatter(),
                                     qc,
                                     store.getSQLGenerator() ,
                                     new CustomizeType()) ;
        play2(store2) ;
    }
        
    private static void play2(Store store2)
    {
        SDB.init() ;    // Check called - this hsoudl not be needed.
        String q = "SELECT * { ?x a ?t}" ;

        Query query = QueryFactory.create(q) ;
        query.serialize(System.out) ;
        QueryExecution qExec = null ; 
        
        //qExec = QueryExecutionFactory.create(query, new DatasetStore(store2)) ;
        qExec = new QueryEngineSDB(store2, query) ;
        
        divider() ;
        PrintSDB.printBlocks(store2, query, null) ;
        divider() ;
        
        store2.getConnection().setLogSQLQueries(true) ;
        
        try {
            ResultSet rs1 = qExec.execSelect() ;
            ResultSetRewindable rs = ResultSetFactory.makeRewindable(rs1) ;
            ResultSetFormatter.out(rs) ;
            rs.reset() ;
        } finally { qExec.close(); }
        store2.getConnection().setLogSQLQueries(false) ;
    }



    private static void divider()
    {
        System.out.println("-------------------------------------------------") ;
    }

    private static void dumpClassTable(Store store) throws SQLException
    {
        String s = "SELECT * FROM "+classTable ;
        java.sql.ResultSet rs = store.getConnection().execQuery(s) ;
        RS.printResultSet(rs) ;
        RS.close(rs) ;
    }

    private static void buildSubClassTable(Store store) throws SQLException
    {
        
        //SDBConnection.logSQLStatements = true ;

        // ---- Find declared subclass assertions 
        SubClasses classes = findSubClasses(store) ;

        // --- debug
        System.out.printf("START\n") ;
        print(classes) ;

        // --- In-memory expansion of subclass relationships. 
        expand(classes) ;

        // --- debug
        divider() ;
        System.out.printf("Exit:\n") ;
        print(classes) ;

        // ---- Put in X subClassOf X 
        expandSelf(classes) ;

        // --- debug
        System.out.printf("==========================\n") ;
        print(classes) ;

        // Generate SQL
        System.out.printf("==========================\n") ;

        if ( SQLUtils.hasTable(store.getConnection().getSqlConnection(), classTable) )
            sql(store, String.format("DROP TABLE %s ;\n", classTable)) ;
        else
            System.out.printf("-- Table not present\n" ) ;
        sql(store, 
            String.format(
                          "CREATE TABLE %s (%s integer not null, %s integer not null)",
                          classTable, SubClassTable.colSubClass, SubClassTable.colSuperClass)) ;

        for ( ClassPair p : classes )
            sql(store,
                String.format(
                              "INSERT INTO %s VALUES(%d, %d) ;\n", classTable, p.car(), p.cdr())) ;

    }
    private static void formatAndLoadStore(Store store)
    {
        store.getTableFormatter().format() ;
        Model model = SDBFactory.connectModel(store) ;
        model.read("file:D.ttl", null, "N3") ;
        
        String q = "SELECT * { ?ss ?pp ?oo}" ;

        Query query = QueryFactory.create(q) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, new DatasetStore(store)) ;
        try {
            ResultSetFormatter.out(qExec.execSelect()) ;
        } finally { qExec.close() ; }
        
    }

    static void sql(Store store, String sql) throws SQLException
    {
        if ( true )
            store.getConnection().execUpdate(sql) ;
        else
            System.out.println(sql) ;
    }
    
    static SubClasses findSubClasses(Store store)
    {
        SubClasses classes = new SubClasses() ;
        
        int idSubClassOf = -1 ;
        
        try {
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
                    int idSubj = node2id(store.getConnection(), s) ;
                    int idObj = node2id(store.getConnection(), o) ;
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

    static void expand(SubClasses classes)
    {
        for ( ;; )
        {
            divider() ;
            SubClasses moreClasses = new SubClasses() ;
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
    }
    
    
    static void expandSelf(SubClasses classes)
    {
        // X rdfs:subClassOf X
        SubClasses moreClasses = new SubClasses() ;
        for ( ClassPair p : classes )
        {
            int c1 = p.car() ;
            add(moreClasses, c1, c1) ;
            int c2 = p.cdr() ;
            add(moreClasses, c2, c2) ;
        }
        classes.addAll(moreClasses) ;
    }


    static Set<Integer> findByLeft(SubClasses classes, int c)
    {
        Set<Integer> x = new HashSet<Integer>() ;
        for ( ClassPair p : classes )
        {
            if ( p.car() == c )
                x.add(p.cdr()) ;
        }
        return x ;
    }
    
    static void print(SubClasses classes)
    {
        for ( ClassPair p : classes )
            System.out.printf("%d ==> %d\n", p.car(), p.cdr()) ;
    }
    
    static void add(SubClasses classes, int c1, int c2)
    {
        classes.add(new ClassPair(c1, c2)) ;
    }
    
    static Node id2node(SDBConnection sdb, int id) throws Exception
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

    static int node2id(SDBConnection sdb, Node n) throws Exception
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