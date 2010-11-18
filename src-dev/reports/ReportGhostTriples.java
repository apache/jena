/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.BitsLong ;
import org.openjena.atlas.lib.NumberUtils ;
import org.openjena.atlas.lib.Pair ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class ReportGhostTriples
{
    public static void main(String ...argv)
    {
        main2(argv) ; System.exit(0) ;
        
        Node subject = Node.createURI("http://clips.greenpeace.ch/clip/45e03cfb-f95f-4bbb-afd0-03159bfde64f") ;
        Node predicate = Node.createURI("http://www.getunik.com/2010/02/clips#last_sending") ;
        Node object = Node.createLiteral("2010-05-07T00:03:56.79-04:00", null, XSDDatatype.XSDdateTime) ;

        // 04E07DA53803DDD6
        // 04E07DA53803DB0F
        
        NodeId calc = NodeId.inline(object) ;
        NodeId actual = NodeId.create(Long.parseLong("04E07DA53803DB0F",16)) ;
        
        disemble(calc.getId()) ;
        disemble(actual.getId()) ;
//        
//        long val = BitsLong.clear(actual.getId(), 56, 64) ;
//        System.out.printf("%016X\n", val) ;
//        String lex = DateTimeNode.unpackDateTime(val) ; 

        
        
        
        Node actualNode = NodeId.extract(actual) ;
        
        
        System.out.println("Calc:   "+calc+" >> "+object) ;
        System.out.println("Actual: "+actual+" >> "+actualNode) ;
    }
    
 // Const-ize
    static final int DATE_LEN = 22 ;    // 13 bits year, 4 bits month, 5 bits day => 22 bits
    static final int TIME_LEN = 27 ;    // 5 bits hour + 6 bits minute + 16 bits seconds (to millisecond)
    
    static final int MILLI = 0 ;
    static final int MILLI_LEN = 16 ;

    static final int MINUTES = MILLI_LEN ;
    static final int MINUTES_LEN = 6 ;

    static final int HOUR = MILLI_LEN + MINUTES_LEN ;
    static final int HOUR_LEN = 5 ;

    
    static final int DAY = TIME_LEN  ;
    static final int DAY_LEN = 5 ;

    static final int MONTH = TIME_LEN + DAY_LEN ;
    static final int MONTH_LEN = 4 ;
    
    static final int YEAR = TIME_LEN + MONTH_LEN + DAY_LEN ;
    static final int YEAR_LEN = 13 ;
    
    
    static final int TZ = TIME_LEN + DATE_LEN ;
    static final int TZ_LEN = 7 ;
    static final int TZ_Z = 0x7F ;      // Value for Z
    static final int TZ_NONE = 0x7E ;   // Value for no timezone.
    
    private static void disemble(long v)
    {
//        int years = (int)BitsLong.unpack(v, YEAR, YEAR+YEAR_LEN) ;
//        int months = (int)BitsLong.unpack(v, MONTH, MONTH+MONTH_LEN) ;
//        int days = (int)BitsLong.unpack(v, DAY, DAY+DAY_LEN) ;
//        System.out.printf("%d %d %d\n", years, months, days) ;
//        
//        // Hours: 5, mins 6, milli 16, TZ 7 => 34 bits 
//        int hours = (int)BitsLong.unpack(v, HOUR, HOUR+HOUR_LEN) ;
//        int minutes = (int)BitsLong.unpack(v, MINUTES, MINUTES+MINUTES_LEN) ; 
//        int milliSeconds = (int)BitsLong.unpack(v, MILLI, MILLI+MILLI_LEN) ;
//        
//        System.out.printf("%d %d %d\n", hours, minutes, milliSeconds) ;

        int milliSeconds = (int)BitsLong.unpack(v, MILLI, MILLI+MILLI_LEN) ;
        int sec = milliSeconds / 1000 ;
        int fractionSec = milliSeconds % 1000 ;
        
        StringBuilder sb = new StringBuilder() ;
        NumberUtils.formatInt(sb, sec, 2) ;
        if ( fractionSec != 0 )
        {
            sb.append(".") ;
            NumberUtils.formatInt(sb, fractionSec,3) ;
        }
        System.out.println("Seconds:"+sb.toString()) ;
        
//        int tz = (int)BitsLong.unpack(v, TZ, TZ+TZ_LEN);
//        System.out.printf("%d\n", tz) ;
        
//        int tzH = tz/4 ;
//        int tzM = (tz%4)*15 ;
//            StringBuilder sb = new StringBuilder() ;
//        NumberUtils.formatSignedInt(sb, tzH, 3) ; // Sign always included.
//        sb.append(':') ;
//        NumberUtils.formatInt(sb, tzM, 2) ;
//        System.out.println("TZ:"+sb.toString()) ;
    
    }
    
    public static void main2(String ...argv)
    {
        Dataset dataset =TDBFactory.createDataset("DB") ;
        Graph graph = dataset.asDatasetGraph().getDefaultGraph() ;
        
        NodeTable nt = ((DatasetGraphTDB)(dataset.asDatasetGraph())).getTripleTable().getNodeTupleTable().getNodeTable() ;
        
        if ( false )
        {
            Iterator <Pair<NodeId, Node>> iter = nt.all() ;
            if ( iter.hasNext() )
                System.out.println("YES") ;
            else
                System.out.println("NO") ;
            Iter.toList(Iter.debug(iter)) ;
        }
        
        Node subject = Node.createURI("http://clips.greenpeace.ch/clip/45e03cfb-f95f-4bbb-afd0-03159bfde64f") ;
        Node predicate = Node.createURI("http://www.getunik.com/2010/02/clips#last_sending") ;
        Node object = Node.createLiteral("2010-05-07T00:03:56.079-04:00", null, XSDDatatype.XSDdateTime) ;

        NodeId sNodeId = nt.getNodeIdForNode(subject) ;
        NodeId pNodeId = nt.getNodeIdForNode(predicate) ;
        NodeId oNodeId = nt.getNodeIdForNode(object) ;
        
        NodeId calc = NodeId.inline(object) ;
        NodeId actual = NodeId.create(Long.parseLong("04E07DA53803DB0F",16)) ;
        Node actualNode = NodeId.extract(actual) ;
        
        System.out.println("["+sNodeId+" "+pNodeId+" "+oNodeId+"]") ;
        System.out.println("["+sNodeId+" "+pNodeId+" "+calc+"]") ;
        
        System.out.println(calc+" >> "+object) ;
        System.out.println(actual+" >> "+actualNode) ;
        
        TupleIndex idx = ((DatasetGraphTDB)(dataset.asDatasetGraph())).getTripleTable().getNodeTupleTable().getTupleTable().getIndex(0) ;

        //Iterator<Tuple<NodeId>> iterIdx = idx.all() ;
        Tuple<NodeId> x = Tuple.create(sNodeId, pNodeId, null) ;
        Iterator<Tuple<NodeId>> iterIdx = idx.find(x) ;
        if ( ! iterIdx.hasNext() )
            System.out.println("--> empty") ;
        else
        {
            for ( ; iterIdx.hasNext() ; )
            {
                Tuple<NodeId> tuple = iterIdx.next() ;
                System.out.println("--> "+tuple.get(2)+" --> "+nt.getNodeForNodeId(tuple.get(2))) ;
            }
        }
        Iterator<Tuple<Node>> iter2 = ((DatasetGraphTDB)(dataset.asDatasetGraph())).getTripleTable().getNodeTupleTable().find(subject, predicate, null) ;
        if ( !iter2.hasNext() )
            System.out.println("==> empty") ;
        else
        {
            for ( ; iter2.hasNext() ; )
            {
                Tuple<Node> tuple = iter2.next() ;
                System.out.println("==> "+tuple) ;
            }
        }

//        System.out.println("Pattern:") ;
//        Iterator<Triple> iter = graph.find(Node.createURI("http://clips.greenpeace.ch/clip/45e03cfb-f95f-4bbb-afd0-03159bfde64f"), 
//                                           Node.createURI("http://www.getunik.com/2010/02/clips#last_sending"), Node.ANY) ;
//        for ( ; iter.hasNext() ; )
//        {
//            Triple t = iter.next() ;
//            System.out.println(t) ;
//        }

        System.out.println("Concrete:") ;
        {
            Iterator<Triple> iterGFind = graph.find(Node.createURI("http://clips.greenpeace.ch/clip/45e03cfb-f95f-4bbb-afd0-03159bfde64f"), 
                                                Node.createURI("http://www.getunik.com/2010/02/clips#last_sending"),
                                                object) ;

            if ( ! iterGFind.hasNext() )
                System.out.println("<nothing>") ;
            else
            {
                for ( ; iterGFind.hasNext() ; )
                {
                    Triple t = iterGFind.next() ;
                    System.out.println(t) ;
                }
            }
        }
        System.out.println("DONE") ;
//        
//        Query query = QueryFactory.create("SELECT ?x {<http://clips.greenpeace.ch/clip/45e03cfb-f95f-4bbb-afd0-03159bfde64f> <http://www.getunik.com/2010/02/clips#last_sending> '2010-05-07T00:03:56.79-04:00'^^<http://www.w3.org/2001/XMLSchema#dateTime> }") ;
//        QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
//        QueryExecUtils.executeQuery(query, qExec) ; 
        
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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