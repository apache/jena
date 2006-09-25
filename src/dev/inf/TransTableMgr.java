/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;

import java.sql.SQLException;

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
import com.hp.hpl.jena.sdb.util.StrUtils;

public class TransTableMgr
{
    TransGraph<Integer> transEngine = new TransGraph<Integer>() ;
    private TransTable transTable ;
    private Store inputStore ;
    
    public TransTableMgr(Store store, TransTable transTable)
    { 
        this.transTable = transTable ;
        this.inputStore = store ;
    }
    
    public void buildLinks(boolean reflexive)
    {
        findPairs() ;
        transEngine.expand() ;
        if ( reflexive )
            transEngine.expandReflexive() ;
    }
    
    // Can write to a different store
    public void write(Store outputStore)
    {
        writePairsTable(outputStore, transTable, transEngine) ;
    }

    public void write()
    {
        writePairsTable(inputStore, transTable, transEngine) ;
    }

    private void findPairs()
    {
        int idSubClassOf = -1 ;
        
        try {
            String q = 
                StrUtils.strjoinNL(
                        //String.format("PREFIX rdfs: <%s>", RDFS.getURI()) ,
                        String.format("SELECT * { ?c1 <%s> ?c2}", transTable.getProperty().getURI())
                        );
            Query query = QueryFactory.create(q) ;
            QueryExecution qExec = QueryExecutionFactory.create(query, new DatasetStore(inputStore)) ;
            
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
                    int idSubj = node2id(inputStore.getConnection(), s) ;
                    int idObj  = node2id(inputStore.getConnection(), o) ;
                    transEngine.add(idSubj, idObj) ;

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
    }

    
    static private void writePairsTable(final Store outputStore, final TransTable transTable, TransGraph<Integer> transEngine)
    {
        try {
            if ( SQLUtils.hasTable(outputStore.getConnection().getSqlConnection(), transTable.getTableName()) )
                sql(outputStore, String.format("DROP TABLE %s ;\n", transTable.getTableName())) ;
            else
                System.out.printf("-- Table not present\n" ) ;
            sql(outputStore, 
                String.format
                ( "CREATE TABLE %s (%s integer not null, %s integer not null)",
                  transTable.getTableName(), transTable.getColLeft(), transTable.getColRight())) ;
            
            TransGraph.LinkApply<Integer> action = 
                new TransGraph.LinkApply<Integer>()
                {
                    public void apply(Integer i, Integer j)
                    {
                        try {
                            sql(outputStore,
                                String.format
                                ( "INSERT INTO %s VALUES(%d, %d) ;\n",transTable.getTableName(), i, j)) ;
                        } catch (SQLException ex) { throw new SDBExceptionSQL(ex) ; }
                    }
                } ;
            transEngine.linkApply(action) ;
        } catch (SQLException ex) { throw new SDBExceptionSQL(ex) ; }
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