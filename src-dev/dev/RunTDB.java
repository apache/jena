/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.io.StringReader ;
import java.io.StringWriter ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import perf.Performance ;
import atlas.io.PeekReader ;
import atlas.iterator.Filter ;
import atlas.lib.Sink ;
import atlas.lib.SinkCounting ;
import atlas.lib.SinkPrint ;
import atlas.lib.Tuple ;
import atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.riot.JenaReaderTurtle2 ;
import com.hp.hpl.jena.riot.lang.LangRIOT ;
import com.hp.hpl.jena.riot.lang.LangTurtle ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerText ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class RunTDB
{
    static { Log.setLog4j() ; }
    static String divider = "----------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }

    
    
    
    public static void main(String[] args) throws IOException
    {
        streamInference() ; System.exit(0) ;
        
        TDB.init();
        Model m = ModelFactory.createDefaultModel();
        m.createResource("http://example.org/#-1", RDFS.Resource);
        
        {
            StringWriter w = new StringWriter() ;
            m.write(w, "TURTLE");
            String s = w.toString() ;
            StringReader r = new StringReader(s) ;
            m.read(r, null, "TURTLE");
        }
        
//        m.write(new FileWriter("test.ttl"), "TURTLE");
//        m.read(new FileReader("test.ttl"), null, "TURTLE");
        {
            StringWriter w = new StringWriter() ;
            m.setNsPrefix("eg", "http://example.org/#");
            m.write(w, "TURTLE");
            String s = w.toString() ;

            System.out.println(s) ;

            StringReader r = new StringReader(s) ;
            m.read(r, null, "TURTLE");
            
            m.write(System.out, "TURTLE");
        }
        System.exit(0) ;
        
        
        if ( args.length == 0 )
            args = new String[]{"/home/afs/Datasets/BSBM/bsbm-250k.nt.gz"} ;
        
        //Performance.tokenizer(args[0]) ; System.exit(0) ;
        Performance.ntriples(args[0]) ; System.exit(0) ;
        
        tdb.turtle.main("D.ttl") ; System.exit(0) ;

        DevCmds.tdbquery("--tdb=tdb.ttl", "--set=tdb:logExec=info", "SELECT * {GRAPH ?g { ?s ?p ?o}}") ;
        System.exit(0) ;
    }

    static void tupleFilter()
    {
        Dataset ds = TDBFactory.createDataset("DB") ;
        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;

        final NodeTable nodeTable = dsg.getQuadTable().getNodeTupleTable().getNodeTable() ;
        final NodeId target = nodeTable.getNodeIdForNode(Node.createURI("http://example/graph2")) ;

        System.out.println("Filter: "+target) ;
        
        Filter<Tuple<NodeId>> filter = new Filter<Tuple<NodeId>>() {
            
            public boolean accept(Tuple<NodeId> item)
            {
                
                // Reverse the lookup as a demo
                Node n = nodeTable.getNodeForNodeId(target) ;
                
                //System.err.println(item) ;
                if ( item.size() == 4 && item.get(0).equals(target) )
                {
                    System.out.println("Reject: "+item) ;
                    return false ;
                }
                System.out.println("Accept: "+item) ;
                return true ;
            } } ;
            
            
        TDB.getContext().set(SystemTDB.symTupleFilter, filter) ;

        String qs = "SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } }}" ;
        //String qs = "SELECT * { GRAPH ?g { ?s ?p ?o } }" ;
        
        DevCmds.tdbquery("--tdb=tdb.ttl", qs) ;
    }
    
    public static void turtle2() throws IOException
    {
        // Also tdb.turtle.
        //        TDB.init();
        //        RDFWriter w = new JenaWriterNTriples2() ;
        //        Model model = FileManager.get().loadModel("D.ttl") ;
        //        w.write(model, System.out, null) ;
        //        System.exit(0) ;

        InputStream input = new FileInputStream("D.ttl") ;
        JenaReaderTurtle2.parse(input) ;
        System.out.println("END") ;
        System.exit(0) ;
    }
    
    static final class InferenceExpander implements Sink<Triple>
    {
        // Assumes rdf:type is not a superproperty. 
        
        // Expanded hierarchy:
        // If C < C1 < C2 then C2 is in the list for C 
        final private Map<Node, List<Node>> transClasses ;
        final private Map<Node, List<Node>> transProperties;
        final private Sink<Triple> output ;
        final private Map<Node, List<Node>> domainList ;
        final private Map<Node, List<Node>> rangeList ;  
        
        static final Node rdfType = RDF.type.asNode() ;
        
        public InferenceExpander(Sink<Triple> output,
                                 Map<Node, List<Node>> transClasses,
                                 Map<Node, List<Node>> transProperties,
                                 Map<Node, List<Node>> domainList,
                                 Map<Node, List<Node>> rangeList)
        {
            this.output = output ;
            this.transClasses = transClasses ;
            this.transProperties = transProperties ;
            this.domainList = domainList ;
            this.rangeList = rangeList ;
            // Class trigger.
            
        }
        
        public void send(Triple triple)
        {
            System.out.println();
            output.send(triple) ;
            Node s = triple.getSubject() ;
            Node p = triple.getPredicate() ;
            Node o = triple.getObject() ;

            subClass(s,p,o) ;
            subProperty(s,p,o) ;

            domain(s,p,o) ;
            
            // Beware of literal subjects.
            range(s,p,o) ;
        }

        /*
        [rdfs2:  (?p rdfs:domain ?c) -> [(?x rdf:type ?c) <- (?x ?p ?y)] ] 
         [rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ] 
        */
        
        final private void domain(Node s, Node p, Node o)
        {
            List<Node> x = domainList.get(p) ;
            if ( x != null )
            {
                for ( Node c : x )
                {
                    output.send(new Triple(s,rdfType,c)) ;
                    subClass(s, rdfType, c) ;
                }
            }
        }

        final private void range(Node s, Node p, Node o)
        {
            // Range
            List<Node> x = rangeList.get(p) ;
            if ( x != null )
            {
                for ( Node c : x )
                {
                    output.send(new Triple(o,rdfType,c)) ;
                    subClass(o, rdfType, c) ;
                }
            }
        }

        final private void subClass(Node s, Node p, Node o)
        {
            if ( p.equals(rdfType) )
            {
                List<Node> x = transClasses.get(o) ;
                if ( x != null )
                    for ( Node c : x )
                        output.send(new Triple(s,p,c)) ;
            }
        }
        
        private void subProperty(Node s, Node p, Node o)
        {
            List<Node> x = transProperties.get(p) ;
            if ( x != null )
            {
                for ( Node p2 : x )
                    output.send(new Triple(s,p2,o)) ;
            }
        }
        
        public void flush()
        { output.flush(); }

        public void close()
        { output.close(); }
        
    }
    
    public static void streamInference()
    {
        Map<Node, List<Node>> transClasses = new HashMap<Node, List<Node>>() ;
        
        Node c = NodeFactory.parseNode("<http://example/ns#C>") ;
        Node c1 = NodeFactory.parseNode("<http://example/ns#C_1>") ;
        Node c2 = NodeFactory.parseNode("<http://example/ns#C_2>") ;
        Node c3 = NodeFactory.parseNode("<http://example/ns#C_3>") ;
        Node c4 = NodeFactory.parseNode("<http://example/ns#C_4>") ;
        
        transClasses.put(c, new ArrayList<Node>()) ;
        transClasses.get(c).add(c1) ;
        transClasses.get(c).add(c2) ;
        transClasses.put(c1, new ArrayList<Node>()) ;
        transClasses.get(c1).add(c2) ;
        
        Map<Node, List<Node>> transProperties = new HashMap<Node, List<Node>>() ;
        Node p = NodeFactory.parseNode("<http://example/ns#P>") ;
        Node p1 = NodeFactory.parseNode("<http://example/ns#P_1>") ;
        Node p2 = NodeFactory.parseNode("<http://example/ns#P_2>") ;

        transProperties.put(p, new ArrayList<Node>()) ;
        transProperties.get(p).add(p1) ;
        transProperties.get(p).add(p2) ;
        transProperties.put(p1, new ArrayList<Node>()) ;
        transProperties.get(p1).add(p2) ;

        Map<Node, List<Node>> domainList = new HashMap<Node, List<Node>>() ;
        Node pD = NodeFactory.parseNode("<http://example/ns#D>") ;
        domainList.put(pD, new ArrayList<Node>()) ;
        domainList.get(pD).add(c3) ;
        
        Map<Node, List<Node>> rangeList = new HashMap<Node, List<Node>>() ;
        Node pR = NodeFactory.parseNode("<http://example/ns#R>") ;
        rangeList.put(pR, new ArrayList<Node>()) ;
        rangeList.get(pR).add(c4) ;
        //Now C1 recurse
        
        SinkCounting<Triple> outputSink = new SinkCounting<Triple>(new SinkPrint<Triple>()) ;
        SinkCounting<Triple> inputSink = new SinkCounting<Triple>(new InferenceExpander(outputSink,
                                                                                   transClasses,
                                                                                   transProperties,
                                                                                   domainList,
                                                                                   rangeList
                                                                                   )) ;
        Tokenizer tokenizer = new TokenizerText(PeekReader.open("D.ttl")) ;
        LangRIOT parser = new LangTurtle("http://base/", tokenizer, inputSink) ;
        parser.parse() ;
        inputSink.flush() ;
        
        System.out.println() ;
        System.out.printf("Input  =  %d\n", inputSink.getCount()) ;
        System.out.printf("Total  =  %d\n", outputSink.getCount()) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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