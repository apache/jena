/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SeqImpl.java,v 1.1 2009-06-29 08:55:32 castagna Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;

import com.hp.hpl.jena.enhanced.*;

/** An implementation of Seq
 *
 * @author  bwm
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2009-06-29 08:55:32 $' 
*/

public class SeqImpl extends ContainerImpl implements Seq {

    @SuppressWarnings("hiding")
    final static public Implementation factory = new Implementation() {
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        @Override
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new SeqImpl(n,eg);
        }
    };
    
    static NodeIteratorFactory seqIteratorFactory = new SeqNodeIteratorFactoryImpl();

    /** Creates new SeqMem */
    public SeqImpl( ModelCom model )  {
        super( model );
    }
    
    public SeqImpl( String uri, ModelCom model )  {
        super( uri, model );
    }
    
    public SeqImpl( Resource r, ModelCom m )  {
        super( r, m );
    }

    public SeqImpl(Node n, EnhGraph g) {
        super(n,g);
    }
    
    @Override
    public Resource getResource(int index)  {
        return getRequiredProperty(RDF.li(index)).getResource();
    }
    
    @Override
    public Literal getLiteral(int index)  {
        return getRequiredProperty(RDF.li(index)).getLiteral();
    }
    
    @Override
    public RDFNode getObject(int index)  {
        return getRequiredProperty(RDF.li(index)).getObject();
    }
    
    @Override
    public boolean getBoolean(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getBoolean();
    }
    
    @Override
    public byte getByte(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getByte();
    }
    
    @Override
    public short getShort(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getShort();
    }
    
    @Override
    public int getInt(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getInt();
    }
    
    @Override
    public long getLong(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getLong();
    }
    
    @Override
    public char getChar(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getChar();
    }
    
    @Override
    public float getFloat(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getFloat();
    }
    
    @Override
    public double getDouble(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getDouble();
    }
    
    @Override
    public String getString(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getString();
    }
    
    @Override
    public String getLanguage(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getLanguage();
    }
    
    @Override
    @Deprecated public Resource getResource(int index, ResourceF f) {
        return getRequiredProperty(RDF.li(index)).getResource(f);
    }
    
    @Override
    public Bag getBag(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getBag();
    }
    
    @Override
    public Alt getAlt(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getAlt();
    }
    
    @Override
    public Seq getSeq(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getSeq();
    }

    @Override
    public Seq set(int index, RDFNode o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    @Override
    public Seq set(int index, boolean o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeLiteralObject(o);
        return this;
    }
    
    @Override
    public Seq set(int index, long o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeLiteralObject(o);
        return this;
    }
    
    @Override
    public Seq set(int index, float o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeLiteralObject(o);
        return this;
    }
    
    @Override
    public Seq set(int index, double o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeLiteralObject(o);
        return this;
    }
    
    @Override
    public Seq set(int index, char o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeLiteralObject(o);
        return this;
    }
    
    @Override
    public Seq set(int index, String o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    @Override
    public Seq set(int index, String o, String l)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o, l);
        return this;
    }
    
    @Override
    public Seq set(int index, Object o)  {
        checkIndex(index);
        Statement s = getRequiredProperty(RDF.li(index)) ;
        Model m = s.getModel() ;
        Statement s2 = m.createLiteralStatement(s.getSubject(), s.getPredicate(), o) ; 
        s.getModel().remove(s) ;
        s.getModel().add(s2) ;
        //getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    @Override
    public Seq add(int index, boolean o)  {
        return add( index, String.valueOf( o ) );
    }
    
    @Override
    public Seq add(int index, long o)  {
        return add( index, String.valueOf( o ) );
    }
    
    @Override
    public Seq add(int index, char o)  {
        return add( index, String.valueOf( o ) );
    }
    
    @Override
    public Seq add(int index, float o)  {
        return add( index, String.valueOf( o ) );
    }
    
    @Override
    public Seq add(int index, double o)  {
        return add( index, String.valueOf( o ) );
    }
    
    @Override
    public Seq add(int index, Object o)  {
        return add( index, String.valueOf( o ) );
    }
    
    @Override
    public Seq add(int index, String o)  {
        return add( index, o, "" );
    }
    
    @Override
    public Seq add( int index, String o, String l )  {
        return add( index, literal( o, l ) );
    }
    
    @Override
    public Seq add(int index, RDFNode o)  {
        int size = size();
        checkIndex(index, size+1);
        shiftUp(index, size);
        addProperty(RDF.li(index), o);
        return this;
    }   
        
     @Override
    public NodeIterator iterator()  
        { return listContainerMembers( seqIteratorFactory ); }
    
    @Override
    public Container remove(Statement s) {
        // System.err.println( "]] SeqImpl.remove " + s );
        getModel().remove(s);
        // System.err.println( "]] SeqImpl.remove - about to shift down " + (s.getPredicate().getOrdinal()+1) + " to " + (size()+1) );
        shiftDown(s.getPredicate().getOrdinal()+1, size()+1);
        // System.err.println( "]] SeqImpl.remov completed" );
        return this;
    } 
    
    @Override
    public Seq remove(int index)  {
        getRequiredProperty(RDF.li(index)).remove();
        shiftDown(index+1, size()+1);
        return this;
    }
    
    @Override
    public Container remove(int index, RDFNode o)  {
        // System.err.println( "]] SeqImpl::remove( " + index + ", " + o + ")" );
        return remove(getModel().createStatement(this, RDF.li(index), o).remove());
    }
    
    @Override
    public int indexOf( RDFNode o )  {
        return containerIndexOf( o );
    }    
    
    @Override
    public int indexOf(boolean o)  {
        return indexOf( String.valueOf( o ) );
    }
    
    @Override
    public int indexOf(long o)  {
        return indexOf( String.valueOf( o ) );
    }
    
    @Override
    public int indexOf(char o)  {
        return indexOf( String.valueOf( o ) );
    }
    
    @Override
    public int indexOf(float o)  {
        return indexOf( String.valueOf( o ) );
    }
    
    @Override
    public int indexOf(double o)  {
        return indexOf( String.valueOf( o ) );
    }

    @Override
    public int indexOf(Object o)  {
        return indexOf( String.valueOf( o ) );
    }
        
    @Override
    public int indexOf(String o)  {
        return indexOf( o, "" );
    }
    
    @Override
    public int indexOf(String o, String l)  {
        return indexOf( literal( o, l ) );
    }
        
    private Literal literal( String s, String lang )
        { return new LiteralImpl( Node.createLiteral( s, lang, false ), getModelCom() ); }
        
    protected void shiftUp(int start, int finish)  {
        Statement stmt = null;
        for (int i = finish; i >= start; i--) {
            stmt = getRequiredProperty(RDF.li(i));
            getModel().remove(stmt);
            addProperty(RDF.li(i+1), stmt.getObject());
        }
    }   
    protected void shiftDown(int start, int finish)  {
        for (int i=start; i<=finish; i++) {
            Statement stmt = getRequiredProperty( RDF.li(i) );
            // System.err.println( "]]* remove " + stmt );
            stmt.remove();
            // System.err.println( "]]* addProperty( " + RDF.li(i-1) + " " + stmt.getObject() );
            addProperty(RDF.li(i-1), stmt.getObject());
        }
    }
    
    protected void checkIndex(int index)  {
        checkIndex( index, size() );
    } 
    
    protected void checkIndex(int index, int max)  {
        if (! (1 <= index && index <= max)) {
            throw new SeqIndexBoundsException( max, index );
        }
    }
}
/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 *
 * SeqImpl.java
 *
 * Created on 08 August 2000, 17:10
 */
