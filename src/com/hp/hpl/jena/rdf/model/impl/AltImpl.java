/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2003
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
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.9 $' Date='$Date: 2003-06-17 12:25:05 $'
 */
public class AltImpl extends ContainerImpl implements Alt {
    
    final static public Implementation factory = new Implementation() {
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new AltImpl(n,eg);
        }
    };
    
    /** Creates new AltMem */
    public AltImpl(Model model)  
        { super( model ); }
    
    public AltImpl(String uri, Model model)  {
        super( uri, model );
    }
    
    public AltImpl(Resource r, Model m)  {
        super( r, m );
    }
    
    public AltImpl(Node n, EnhGraph g) {
        super( n, g );
    }
    
    /** get the default statement, explode if there isn't one */
    
    private Statement needDefaultStatement() 
    	{
        Statement stmt = getDefaultStatement();
        if (stmt == null) throw new JenaAltHasNoDefaultException( this );
        return stmt;
    	}
    	
    public RDFNode getDefault()  {
    	return needDefaultStatement().getObject();
    }
    
    public Resource getDefaultResource()  {
    	return needDefaultStatement().getResource();
    }
    
    public Literal getDefaultLiteral()  {
    	return needDefaultStatement().getLiteral();
    }
    
    public boolean getDefaultBoolean()  {
    	return needDefaultStatement().getBoolean();
    }
    
    public byte getDefaultByte()  {
    	return needDefaultStatement().getByte();
    }
    
    public short getDefaultShort()  {
    	return needDefaultStatement().getShort();
    }
    
    public int getDefaultInt()  {
    	return needDefaultStatement().getInt();
    }
    
    public long getDefaultLong()  {
    	return needDefaultStatement().getLong();
    }
    
    public char getDefaultChar()  {
		return needDefaultStatement().getChar();
    }
    
    public float getDefaultFloat()  {
    	return needDefaultStatement().getFloat();
    }
    
    public double getDefaultDouble()  {
    	return needDefaultStatement().getDouble();
    }
    
    public String getDefaultString()  {
    	return needDefaultStatement().getString();
    }
    
    public String getDefaultLanguage()  {
    	return needDefaultStatement().getLanguage();
    }
    
    public Resource getDefaultResource(ResourceF f)  {
    	return needDefaultStatement().getResource();
    }
    
    public Object getDefaultObject(ObjectF f)    {
     	return needDefaultStatement().getObject( f );
    }
    
    public Alt getDefaultAlt()  {
    	return needDefaultStatement().getAlt();
    }
    
    public Bag getDefaultBag()  {
    	return needDefaultStatement().getBag();
    }
    
    public Seq getDefaultSeq()  {
    	return needDefaultStatement().getSeq();
    }
    
    public Alt setDefault(RDFNode o)  {
        Statement stmt = getDefaultStatement();
        if (stmt != null) getModel().remove( stmt );
        getModel().add( this, RDF.li(1), o );
        return this;
    }    
    
    public Alt setDefault(boolean o)  {
        return setDefault( String.valueOf( o ) );
    }
    
    public Alt setDefault(long o)  {
        return setDefault( String.valueOf( o ) );
    }    
    
    public Alt setDefault(char o)  {
        return setDefault( String.valueOf( o ) );
    }        
    
    public Alt setDefault(float o)  {
        return setDefault( String.valueOf( o ) );
    } 
    
    public Alt setDefault(double o)  {
        return setDefault( String.valueOf( o ) );
    }    
    
    public Alt setDefault(Object o)  {
        return setDefault( String.valueOf( o ) );
    }
   
    public Alt setDefault(String o)  {
        return setDefault( o, "" );
    }    
    
    public Alt setDefault(String o, String l)  {
        return setDefault( new LiteralImpl( Node.createLiteral( o,l, false ), (Model) getModel()) );
    }      
        
    protected Statement getDefaultStatement()  {
        StmtIterator iter = getModel().listStatements( this, RDF.li(1), (RDFNode) null );
        if (!iter.hasNext()) {
            return null;
        }
        return iter.nextStatement();
    }
}