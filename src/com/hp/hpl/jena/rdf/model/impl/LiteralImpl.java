/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
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
 * LiteralImpl.java
 *
 * Created on 03 August 2000, 14:42
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.enhanced.*;

/** An implementation of Literal.
 *
 * @author  bwm and der
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.12 $' Date='$Date: 2003-07-21 10:32:46 $'
 */
public class LiteralImpl extends EnhNode implements Literal {
//    private Node node;
//    String  literal;
//    String  language = "";
//    boolean wellFormed = false;   // literal is well formed XML which
                                  // does not need escaping when wriiten
                                  // as RDF/XML
  
    final static public Implementation factory = new Implementation() {
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new LiteralImpl(n,eg);
        }
    };          
          
    public LiteralImpl(Node n,Model m) {
        super(n,(ModelCom)m );
    }
    
    public LiteralImpl( Node n, EnhGraph m ) {
        this( n, (Model)m );
    }
    
    public Object visitWith( RDFVisitor rv )
        { return rv.visitLiteral( this ); }
        
    /**
        Literals are not in any particular model, and so inModel can return this.
        @param m a model to move the literal into
        @return this
    */
    public RDFNode inModel( Model m )
        { return this; }

    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(boolean b) {this(String.valueOf(b));}
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(long l)    {this(String.valueOf(l));}
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(char c)    {this(String.valueOf(c));}
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(float f)   {this(String.valueOf(f));}
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(double d)  {this(String.valueOf(d));}
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(String s)  {this(s,"");}
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(String s, String l) {this(s,l,false);}
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(String s, boolean wellFormed) {
        this(s,"",wellFormed);
    }
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(String s, String l, boolean wellFormed) {
        this(s,l,wellFormed,null);
    }
    
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */
    public LiteralImpl(String s, String l, boolean wellFormed,Model m) {    	
        this(Node.createLiteral(s,l,wellFormed),m);
    }
    
    /**
     *@deprecated Please use the createLiteral methods on Model.
     *Model implementors should use Literal instructors which include the Model.
     */                  
    public LiteralImpl(Object o)  {this( o.toString());}
    
    
    public boolean isLiteral() {
    	return true;
    }
    
    public String toString() {
        return asNode().toString();
    }
    
    /**
     * Return the value of the literal. In the case of plain literals
     * this will return the literal string. In the case of typed literals
     * it will return a java object representing the value. In the case
     * of typed literals representing a java primitive then the appropriate
     * java wrapper class (Integer etc) will be returned.
     */
    public Object getValue() {
        return asNode().getLiteral().getValue();
    }
    
    /**
     * Return the datatype of the literal. This will be null in the
     * case of plain literals.
     */
    public RDFDatatype getDatatype() {
        return asNode().getLiteral().getDatatype();
    }
     
    /**
     * Return the uri of the datatype of the literal. This will be null in the
     * case of plain literals.
     */
    public String getDatatypeURI() {
        return asNode().getLiteral().getDatatypeURI();
    }
    
    /**
     * Return true if this is a "plain" (i.e. old style, not typed) literal.
     */
    public boolean isPlainLiteral() {
        return asNode().getLiteral().getDatatype() == null;
    }
    
    /**
     * Return the lexical form of the literal.
     */
    public String getLexicalForm() {
        return asNode().getLiteral().getLexicalForm();
    }

    public boolean getBoolean()  {
        Object value = asNode().getLiteral().getValue();
        if (isPlainLiteral()) {
            // old style plain literal - try parsing the string
            if (value.equals("true")) {
                return true;
            } else if (value.equals("false")) {
                return false;
            } else {
                throw new BadBooleanException( value.toString() );
            }
        } else {
            // typed literal
            if (value instanceof Boolean) {
                return ((Boolean)value).booleanValue();
            } else {
                throw new DatatypeFormatException(this.toString() + " is not a Boolean");
            }
        }
    }
    
    public byte getByte()  {
        if (isPlainLiteral()) {
            return Byte.parseByte(getLexicalForm());
        } else {
            return asNumber(getValue()).byteValue();
        }
    }
    
    public short getShort()  {
        if (isPlainLiteral()) {
            return Short.parseShort(getLexicalForm());
        } else {
            return asNumber(getValue()).shortValue();
        }
    }

    public int getInt()  {
        if (isPlainLiteral()) {
            return Integer.parseInt(getLexicalForm());
        } else {
            return asNumber(getValue()).intValue();
        }
    }

    public long getLong()  {
        if (isPlainLiteral()) {
            return Long.parseLong(getLexicalForm());
        } else {
            return asNumber(getValue()).longValue();
        }
    }

    public char getChar()  {
        if (isPlainLiteral()) {
            if (getString().length()==1) {
                return (getString().charAt(0));
            } else {
                throw new BadCharLiteralException( getString() );
            }
        } else {
            Object value = getValue();
            if (value instanceof Character) {
                return ((Character) value).charValue();
            } else {
                throw new DatatypeFormatException(value.toString() + " is not a Character");
            }
        }
    }
    
    public float getFloat()  {
        if (isPlainLiteral()) {
            return Float.parseFloat(getLexicalForm());
        } else {
            return asNumber(getValue()).floatValue();
        }
    }

    public double getDouble()  {
        if (isPlainLiteral()) {
            return Double.parseDouble(getLexicalForm());
        } else {
            return asNumber(getValue()).doubleValue();
        }
    }

    public String getString()  {
        return asNode().getLiteral().getLexicalForm();
    }
    
    public Object getObject(ObjectF f)  {
        if (isPlainLiteral()) {
            try {
                return f.createObject(getString());
            } catch (Exception e) {
                throw new JenaException(e);
            }
        } else {
            return getValue();
        }
    }
    
    public String getLanguage() {
        return asNode().getLiteral().language();
    }
    
    public boolean getWellFormed() {
        return asNode().getLiteral().isXML();
    } 
   
    /**
     * Test that two literals are semantically equivalent.
     * In some cases this may be the sames as equals, in others
     * equals is stricter. For example, two xsd:int literals with
     * the same value but different language tag are semantically
     * equivalent but distinguished by the java equality function
     * in order to support round tripping.
     */
    public boolean sameValueAs(Literal other) {
        return asNode().sameValueAs(other.asNode());
    }
        
     // Internal helper method to convert a value to number
    private Number asNumber(Object value) {
        if (value instanceof Number) {
            return ((Number)value);
        } else {
            throw new DatatypeFormatException(value.toString() + " is not a Number");
        }
    }
        
}