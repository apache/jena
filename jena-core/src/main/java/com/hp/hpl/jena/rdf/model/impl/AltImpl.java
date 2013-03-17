/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/** An implementation of Alt.
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
        return setDefault( new LiteralImpl( NodeFactory.createLiteral( o,l, false ), getModelCom()) );
    }      
        
    protected Statement getDefaultStatement()  
        {
        StmtIterator iter = getModel().listStatements( this, RDF.li(1), (RDFNode) null );
        try { return iter.hasNext() ? iter.nextStatement() : null; }
        finally { iter.close(); }
        }

}
