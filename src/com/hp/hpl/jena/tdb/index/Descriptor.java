/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index;

import static com.hp.hpl.jena.tdb.Const.SizeOfNodeId;

import java.util.HashSet;
import java.util.Set;

import lib.Tuple;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

public class Descriptor // implements Index3 // And in SPO order
{
    static interface Selector
    { 
        NodeId choose(NodeId s, NodeId p , NodeId o) ;

        // Basic selectors

        static final Selector sel_1 = new Selector() {
            @Override public NodeId choose(NodeId n1, NodeId n2, NodeId n3)
            { return n1 ; }
        } ;

        static final Selector sel_2 = new Selector() {
            @Override public NodeId choose(NodeId n1, NodeId n2, NodeId n3)
            { return n2 ; }
        } ;

        static final Selector sel_3 = new Selector() {
            @Override public NodeId choose(NodeId n1, NodeId n2, NodeId n3)
            { return n3 ; }
        } ;

        static final Selector selectors[] = new Selector[] { sel_1, sel_2, sel_3 } ;
    }

    // ---- Selectors for SPO => Index order selection.
    static final private Selector sel_s = Selector.sel_1 ;  
    static final private Selector sel_p = Selector.sel_2 ;            
    static final private Selector sel_o = Selector.sel_3 ;

    // ---- Selectors for Index order selection => SPO
    private final Selector sel_slot_1 ;  
    private final Selector sel_slot_2 ; 
    private final Selector sel_slot_3 ;  

    // ---- Selectors for putting index order into SPO
    private final Selector sel_extract_s ;  
    private final Selector sel_extract_p ; 
    private final Selector sel_extract_o ;

    private final RecordFactory recordFactory ;
    private final String label ;
    
    // Slurp in the mapper.
    //Selector selectors[] = new Selector[3] ;
    public Descriptor(String desc, RecordFactory recordFactory)
    {
        this.recordFactory = recordFactory ;
        checkDescriptor(desc) ;
        label = desc ;
        
        //Selectors : SPO to index order 
        sel_slot_1 = getSelector(desc, 0) ;
        sel_slot_2 = getSelector(desc, 1) ;
        sel_slot_3 = getSelector(desc, 2) ;
        
        //Selectors : index order to SPO 
        sel_extract_s = findSelector(desc, 'S') ;
        sel_extract_p = findSelector(desc, 'P') ;
        sel_extract_o = findSelector(desc, 'O') ;
    }
    
    private void checkDescriptor(String desc)
    {
        if ( desc.length() != 3 ) 
            throw new TDBException("Bad descriptor: "+desc) ; 
        
        Set<Character> x = new HashSet<Character>() ;
        for ( int i = 0 ; i < desc.length() ; i++ )
        {
            char ch = desc.charAt(i) ;
            x.add(ch) ;
        }
        if ( x.size() != desc.length() )
            throw new TDBException("Bad descriptor: "+desc) ; 
    }

    // Get selector for SPO
    private Selector getSelector(String desc, int i)
    {
        char ch = desc.charAt(i) ;
        ch = Character.toUpperCase(ch) ;
        return selector(ch) ;
    }

    // Get selector for index order.
    private Selector findSelector(String desc, char ch)
    {
        int idx = desc.indexOf(ch) ;
        if ( idx < 0 )
            throw new TDBException("Bad findSelector: "+desc+" : "+ch) ; 
        return Selector.selectors[idx] ;
    }
    
    private Selector selector(char ch)
    {
        switch (ch)
        {
            case 'S': return sel_s ;
            case 'P': return sel_p ;
            case 'O': return sel_o ;
            //case 'G':
        }
        throw new TDBException("Can't find a select for "+ch) ;
    }
    
    public String getLabel() { return label ; }

    @Override
    public String toString() { return "Descriptor: "+label ; }
    
    public final NodeId getSubj(NodeId x, NodeId y, NodeId z)
    { return sel_extract_s.choose(x,y,z) ; }

    public final NodeId getPred(NodeId x, NodeId y, NodeId z)
    { return sel_extract_p.choose(x,y,z) ; }

    public final NodeId getObj(NodeId x, NodeId y, NodeId z)
    { return sel_extract_o.choose(x,y,z) ; }

    public final NodeId getSlot1(NodeId x, NodeId y, NodeId z)
    { return sel_slot_1.choose(x,y,z) ; }

    public final NodeId getSlot2(NodeId x, NodeId y, NodeId z)
    { return sel_slot_2.choose(x,y,z) ; }

    public final NodeId getSlot3(NodeId x, NodeId y, NodeId z)
    { return sel_slot_3.choose(x,y,z) ; }
    
    // In SPO order
    public final Record record(Tuple<NodeId> tuple)
    { return record(tuple.get(0), tuple.get(1), tuple.get(2)) ; }
    
    public final Record record(NodeId s, NodeId p, NodeId o)
    {
        NodeId id1 = sel_slot_1.choose(s, p, o) ;
        NodeId id2 = sel_slot_2.choose(s, p, o) ;
        NodeId id3 = sel_slot_3.choose(s, p, o) ;
        return NodeLib.record(recordFactory, id1, id2, id3) ;
    }
    
    public final Tuple<NodeId> tuple(Record e)
    {
        // In index native order
        long x = NodeLib.getId(e, 0) ;
        long y = NodeLib.getId(e, SizeOfNodeId) ;
        long z = NodeLib.getId(e, 2*SizeOfNodeId) ;
        // In SPO order
        return tuple(NodeId.create(x), 
                     NodeId.create(y), 
                     NodeId.create(z)) ;
    }
    
    // To SPO order
    public final Tuple<NodeId> tuple(NodeId x, NodeId y, NodeId z)
    {
        NodeId sId = getSubj(x,y,z) ;
        NodeId pId = getPred(x,y,z) ;
        NodeId oId = getObj(x,y,z) ;
        
        return new Tuple<NodeId>(sId, pId, oId) ; 
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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