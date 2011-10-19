/*
 *  (c) Copyright 2000, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 * AltImpl.java
 *
 * Created on 08 August 2000, 16:39
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/** An implementation of Alt.
 *
 * @author  bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1 $' Date='$Date: 2009-06-29 08:55:32 $'
 */
public class AltImpl extends ContainerImpl implements Alt {
    
    @SuppressWarnings("hiding")
    final static public Implementation factory = new Implementation() {
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        @Override
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new AltImpl(n,eg);
        }
    };
    
    /** Creates new AltMem */
    public AltImpl( ModelCom model )  
        { super( model ); }
    
    public AltImpl( String uri, ModelCom model )  {
        super( uri, model );
    }
    
    public AltImpl( Resource r, ModelCom m )  {
        super( r, m );
    }
    
    public AltImpl(Node n, EnhGraph g) {
        super( n, g );
    }
    
    /** get the default statement, explode if there isn't one */
    
    private Statement needDefaultStatement() 
    	{
        Statement stmt = getDefaultStatement();
        if (stmt == null) throw new AltHasNoDefaultException( this );
        return stmt;
    	}
    	
    @Override
    public RDFNode getDefault()  {
    	return needDefaultStatement().getObject();
    }
    
    @Override
    public Resource getDefaultResource()  {
    	return needDefaultStatement().getResource();
    }
    
    @Override
    public Literal getDefaultLiteral()  {
    	return needDefaultStatement().getLiteral();
    }
    
    @Override
    public boolean getDefaultBoolean()  {
    	return needDefaultStatement().getBoolean();
    }
    
    @Override
    public byte getDefaultByte()  {
    	return needDefaultStatement().getByte();
    }
    
    @Override
    public short getDefaultShort()  {
    	return needDefaultStatement().getShort();
    }
    
    @Override
    public int getDefaultInt()  {
    	return needDefaultStatement().getInt();
    }
    
    @Override
    public long getDefaultLong()  {
    	return needDefaultStatement().getLong();
    }
    
    @Override
    public char getDefaultChar()  {
		return needDefaultStatement().getChar();
    }
    
    @Override
    public float getDefaultFloat()  {
    	return needDefaultStatement().getFloat();
    }
    
    @Override
    public double getDefaultDouble()  {
    	return needDefaultStatement().getDouble();
    }
    
    @Override
    public String getDefaultString()  {
    	return needDefaultStatement().getString();
    }
    
    @Override
    public String getDefaultLanguage()  {
    	return needDefaultStatement().getLanguage();
    }
    
    @Override
    @Deprecated public Resource getDefaultResource(ResourceF f)  {
    	return needDefaultStatement().getResource();
    }
    
    @Override
    public Alt getDefaultAlt()  {
    	return needDefaultStatement().getAlt();
    }
    
    @Override
    public Bag getDefaultBag()  {
    	return needDefaultStatement().getBag();
    }
    
    @Override
    public Seq getDefaultSeq()  {
    	return needDefaultStatement().getSeq();
    }
    
    @Override
    public Alt setDefault(RDFNode o)  {
        Statement stmt = getDefaultStatement();
        if (stmt != null) getModel().remove( stmt );
        getModel().add( this, RDF.li(1), o );
        return this;
    }    
    
    @Override
    public Alt setDefault(boolean o)  {
        return setDefault( String.valueOf( o ) );
    }
    
    @Override
    public Alt setDefault(long o)  {
        return setDefault( String.valueOf( o ) );
    }    
    
    @Override
    public Alt setDefault(char o)  {
        return setDefault( String.valueOf( o ) );
    }        
    
    @Override
    public Alt setDefault(float o)  {
        return setDefault( String.valueOf( o ) );
    } 
    
    @Override
    public Alt setDefault(double o)  {
        return setDefault( String.valueOf( o ) );
    }    
    
    @Override
    public Alt setDefault(Object o)  {
        return setDefault( String.valueOf( o ) );
    }
   
    @Override
    public Alt setDefault(String o)  {
        return setDefault( o, "" );
    }    
    
    @Override
    public Alt setDefault(String o, String l)  {
        return setDefault( new LiteralImpl( Node.createLiteral( o,l, false ), getModelCom()) );
    }      
        
    protected Statement getDefaultStatement()  
        {
        StmtIterator iter = getModel().listStatements( this, RDF.li(1), (RDFNode) null );
        try { return iter.hasNext() ? iter.nextStatement() : null; }
        finally { iter.close(); }
        }

}