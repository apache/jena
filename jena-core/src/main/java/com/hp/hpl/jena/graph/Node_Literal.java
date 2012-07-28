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

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.shared.*;

/**
    An RDF node holding a literal value. Literals may have datatypes.
*/
public class Node_Literal extends Node_Concrete
{
    /* package */ Node_Literal( Object label )
        { super( label ); }

    @Override
    public LiteralLabel getLiteral()
        { return (LiteralLabel) label; }
    
    @Override
    public final Object getLiteralValue()
        { return getLiteral().getValue(); }
    
    @Override
    public final String getLiteralLexicalForm()
        { return getLiteral().getLexicalForm(); }
    
    @Override
    public final String getLiteralLanguage()
        { return getLiteral().language(); }
    
    @Override
    public final String getLiteralDatatypeURI()
        { return getLiteral().getDatatypeURI(); }
    
    @Override
    public final RDFDatatype getLiteralDatatype()
        { return getLiteral().getDatatype(); }
    
    @Override
    public final boolean getLiteralIsXML()
        { return getLiteral().isXML(); }
    
    @Override
    public String toString( PrefixMapping pm, boolean quoting )
        { return ((LiteralLabel) label).toString( quoting ); }
        
    @Override
    public boolean isLiteral() 
        { return true; }    
        
    /**
        Literal nodes defer their indexing value to the component literal.
        @see com.hp.hpl.jena.graph.Node#getIndexingValue()
    */
    @Override
    public Object getIndexingValue()
        { return getLiteral().getIndexingValue(); }
    
    @Override
    public Object visitWith( NodeVisitor v )
        { return v.visitLiteral( this, getLiteral() ); }
        
    @Override
    public boolean equals( Object other )
        {
        if ( this == other ) return true ;
        return other instanceof Node_Literal && label.equals( ((Node_Literal) other).label );
        }
        
    /**
     * Test that two nodes are semantically equivalent.
     * In some cases this may be the sames as equals, in others
     * equals is stricter. For example, two xsd:int literals with
     * the same value but different language tag are semantically
     * equivalent but distinguished by the java equality function
     * in order to support round tripping.
     * <p>Default implementation is to use equals, subclasses should
     * override this.</p>
     */
    @Override
    public boolean sameValueAs(Object o) {
        return o instanceof Node_Literal 
              && ((LiteralLabel)label).sameValueAs( ((Node_Literal) o).getLiteral() );
    }
    
    @Override
    public boolean matches( Node x )
        { return sameValueAs( x ); }
    
}
