/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SeqImpl.java,v 1.11 2003-07-21 11:10:01 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;

import com.hp.hpl.jena.enhanced.*;

/** An implementation of Seq
 *
 * @author  bwm
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.11 $' Date='$Date: 2003-07-21 11:10:01 $' 
 */
public class SeqImpl extends ContainerImpl implements Seq {

    final static public Implementation factory = new Implementation() {
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new SeqImpl(n,eg);
        }
    };
    
    static NodeIteratorFactory iteratorFactory;
    
    static {
        iteratorFactory = new SeqNodeIteratorFactoryImpl();
    }

    /** Creates new SeqMem */
    public SeqImpl(Model model)  {
        super(model);
    }
    
    public SeqImpl(String uri, Model model)  {
        super(uri, model);
    }
    
    public SeqImpl(Resource r, Model m)  {
        super(r, m);
    }

    public SeqImpl(Node n, EnhGraph g) {
        super(n,g);
    }
    
    public Resource getResource(int index)  {
        return getRequiredProperty(RDF.li(index)).getResource();
    }
    
    public Literal getLiteral(int index)  {
        return getRequiredProperty(RDF.li(index)).getLiteral();
    }
    
    public RDFNode getObject(int index)  {
        return getRequiredProperty(RDF.li(index)).getObject();
    }
    
    public boolean getBoolean(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getBoolean();
    }
    
    public byte getByte(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getByte();
    }
    
    public short getShort(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getShort();
    }
    
    public int getInt(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getInt();
    }
    
    public long getLong(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getLong();
    }
    
    public char getChar(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getChar();
    }
    
    public float getFloat(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getFloat();
    }
    
    public double getDouble(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getDouble();
    }
    
    public String getString(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getString();
    }
    
    public String getLanguage(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getLanguage();
    }
    
    public Object getObject(int index, ObjectF f)  {
        return getRequiredProperty(RDF.li(index)).getObject(f);
    }
    
    public Resource getResource(int index, ResourceF f) {
        return getRequiredProperty(RDF.li(index)).getResource(f);
    }
    
    public Bag getBag(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getBag();
    }
    
    public Alt getAlt(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getAlt();
    }
    
    public Seq getSeq(int index)  {
        checkIndex(index);
        return getRequiredProperty(RDF.li(index)).getSeq();
    }

    public Seq set(int index, RDFNode o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    public Seq set(int index, boolean o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    public Seq set(int index, long o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    public Seq set(int index, float o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    public Seq set(int index, double o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    public Seq set(int index, char o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    public Seq set(int index, String o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    public Seq set(int index, String o, String l)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o, l);
        return this;
    }
    
    public Seq set(int index, Object o)  {
        checkIndex(index);
        getRequiredProperty(RDF.li(index)).changeObject(o);
        return this;
    }
    
    public Seq add(int index, boolean o)  {
        return add( index, String.valueOf( o ) );
    }
    
    public Seq add(int index, long o)  {
        return add( index, String.valueOf( o ) );
    }
    
    public Seq add(int index, char o)  {
        return add( index, String.valueOf( o ) );
    }
    
    public Seq add(int index, float o)  {
        return add( index, String.valueOf( o ) );
    }
    
    public Seq add(int index, double o)  {
        return add( index, String.valueOf( o ) );
    }
    
    public Seq add(int index, Object o)  {
        return add( index, String.valueOf( o ) );
    }
    
    public Seq add(int index, String o)  {
        return add( index, o, "" );
    }
    
    public Seq add( int index, String o, String l )  {
        return add( index, literal( o, l ) );
    }
    
    public Seq add(int index, RDFNode o)  {
        int size = size();
        checkIndex(index, size+1);
        shiftUp(index, size);
        addProperty(RDF.li(index), o);
        return this;
    }   
        
     public NodeIterator iterator()  {
        return ((ModelI)getModel()).listContainerMembers(this, iteratorFactory);
    }
    
    public Container remove(Statement s) {
        getModel().remove(s);
        shiftDown(s.getPredicate().getOrdinal()+1, size()+1);
        return this;
    } 
    
    public Seq remove(int index)  {
        getRequiredProperty(RDF.li(index)).remove();
        shiftDown(index+1, size()+1);
        return this;
    }
    
    public Container remove(int index, RDFNode o)  {
        return remove(getModel().createStatement(this, RDF.li(index), o).remove());
    }
    
    public int indexOf(RDFNode o)  {
        return ((ModelI)getModel()).containerIndexOf(this, o);
    }    
    
    public int indexOf(boolean o)  {
        return indexOf( String.valueOf( o ) );
    }
    
    public int indexOf(long o)  {
        return indexOf( String.valueOf( o ) );
    }
    
    public int indexOf(char o)  {
        return indexOf( String.valueOf( o ) );
    }
    
    public int indexOf(float o)  {
        return indexOf( String.valueOf( o ) );
    }
    
    public int indexOf(double o)  {
        return indexOf( String.valueOf( o ) );
    }

    public int indexOf(Object o)  {
        return indexOf( String.valueOf( o ) );
    }
        
    public int indexOf(String o)  {
        return indexOf( o, "" );
    }
    
    public int indexOf(String o, String l)  {
        return indexOf( literal( o, l ) );
    }
        
    private Literal literal( String s, String lang )
        { return new LiteralImpl( Node.createLiteral( s, lang, false ), (Model) getModel() ); }
        
    protected void shiftUp(int start, int finish)  {
        Statement stmt = null;
        for (int i = finish; i >= start; i--) {
            stmt = getRequiredProperty(RDF.li(i));
            getModel().remove(stmt);
            addProperty(RDF.li(i+1), stmt.getObject());
        }
    }   
    protected void shiftDown(int start, int finish)  {
        Statement stmt = null;
        for (int i=start; i<=finish; i++) {
            stmt = getRequiredProperty(RDF.li(i));
            addProperty(RDF.li(i-1), stmt.getObject());
            stmt.remove();
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
 *  (c) Copyright Hewlett-Packard Company 2000-2003
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
