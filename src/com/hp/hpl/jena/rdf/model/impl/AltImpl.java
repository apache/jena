/*
 *  (c) Copyright Hewlett-Packard Company 2000 
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
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.2 $' Date='$Date: 2003-02-01 14:35:31 $'
 */
public class AltImpl extends ContainerImpl implements Alt {
    

    
    static private Type[] myTypes = new Type[]{
        Resource.type,
        Alt.type
    };
    final static public Implementation factory = new Implementation() {
        public Type[] implementedTypes() {
            return myTypes;
        }
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new AltImpl(n,eg);
        }
    };
    
    private AltImpl( Resource r )
        { super( r );
         setTypes(myTypes);
        }
        
    /** Creates new AltMem */
    public AltImpl(Model model) throws RDFException {
        super(model);
        setTypes(myTypes);
    }
    
    public AltImpl(String uri, Model model) throws RDFException {
        super(uri, model);
        setTypes(myTypes);
    }
    
    public AltImpl(Resource r, Model m) throws RDFException {
        super(r, m);
        setTypes(myTypes);
    }
    
    public AltImpl(Node n, EnhGraph g) {
        super(n,g);
        setTypes(myTypes);
    }
    
    /** get the default statement, explode if there isn't one */
    
    private Statement needDefaultStatement() throws RDFException
    	{
        Statement stmt = getDefaultStatement();
        if (stmt == null) throw new RDFException( RDFException.ALTHASNODEFAULT );
        return stmt;
    	}
    	
    public RDFNode getDefault() throws RDFException {
    	return needDefaultStatement().getObject();
    }
    
    public Resource getDefaultResource() throws RDFException {
    	return needDefaultStatement().getResource();
    }
    
    public Literal getDefaultLiteral() throws RDFException {
    	return needDefaultStatement().getLiteral();
    }
    
    public boolean getDefaultBoolean() throws RDFException {
    	return needDefaultStatement().getBoolean();
    }
    
    public byte getDefaultByte() throws RDFException {
    	return needDefaultStatement().getByte();
    }
    
    public short getDefaultShort() throws RDFException {
    	return needDefaultStatement().getShort();
    }
    
    public int getDefaultInt() throws RDFException {
    	return needDefaultStatement().getInt();
    }
    
    public long getDefaultLong() throws RDFException {
    	return needDefaultStatement().getLong();
    }
    
    public char getDefaultChar() throws RDFException {
		return needDefaultStatement().getChar();
    }
    
    public float getDefaultFloat() throws RDFException {
    	return needDefaultStatement().getFloat();
    }
    
    public double getDefaultDouble() throws RDFException {
    	return needDefaultStatement().getDouble();
    }
    
    public String getDefaultString() throws RDFException {
    	return needDefaultStatement().getString();
    }
    
    public String getDefaultLanguage() throws RDFException {
    	return needDefaultStatement().getLanguage();
    }
    
    public Resource getDefaultResource(ResourceF f) throws RDFException {
    	return needDefaultStatement().getResource();
    }
    
    public Object getDefaultObject(ObjectF f)   throws RDFException {
     	return needDefaultStatement().getObject( f );
    }
    
    public Alt getDefaultAlt() throws RDFException {
    	return needDefaultStatement().getAlt();
    }
    
    public Bag getDefaultBag() throws RDFException {
    	return needDefaultStatement().getBag();
    }
    
    public Seq getDefaultSeq() throws RDFException {
    	return needDefaultStatement().getSeq();
    }
    
    public Alt setDefault(RDFNode o) throws RDFException {
        Statement stmt = getDefaultStatement();
        if (stmt != null) getModel().remove( stmt );
        getModel().add( this, RDF.li(1), o );
        return this;
    }    
    
    public Alt setDefault(boolean o) throws RDFException {
        return setDefault( String.valueOf( o ) );
    }
    
    public Alt setDefault(long o) throws RDFException {
        return setDefault( String.valueOf( o ) );
    }    
    
    public Alt setDefault(char o) throws RDFException {
        return setDefault( String.valueOf( o ) );
    }        
    
    public Alt setDefault(float o) throws RDFException {
        return setDefault( String.valueOf( o ) );
    } 
    
    public Alt setDefault(double o) throws RDFException {
        return setDefault( String.valueOf( o ) );
    }    
    
    public Alt setDefault(Object o) throws RDFException {
        return setDefault( String.valueOf( o ) );
    }
   
    public Alt setDefault(String o) throws RDFException {
        return setDefault( o, "" );
    }    
    
    public Alt setDefault(String o, String l) throws RDFException {
        return setDefault( new LiteralImpl( Node.makeLiteral( o,l, false ), (Model) getModel()) );
    }      
        
    protected Statement getDefaultStatement() throws RDFException {
        StmtIterator iter = getModel().listStatements(
                             new SelectorImpl(this, RDF.li(1), (RDFNode) null));
        if (!iter.hasNext()) {
            return null;
        }
        return iter.nextStatement();
    }
}