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
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.enhanced.*;

/** An implementation of Literal.
 */
public class LiteralImpl extends EnhNode implements Literal {
  
    final static public Implementation factory = new Implementation() {
        @Override public boolean canWrap( Node n, EnhGraph eg )
            { return n.isLiteral(); }
            
        @Override public EnhNode wrap(Node n, EnhGraph eg) {
            if (!n.isLiteral()) throw new LiteralRequiredException( n );
            return new LiteralImpl(n,eg);
        }
    };          
          
    public LiteralImpl( Node n, ModelCom m) {
        super( n, m );
    }
    
    public LiteralImpl( Node n, EnhGraph m ) {
        super( n, m );
    }
    
    @Override
    public Object visitWith( RDFVisitor rv )
        { return rv.visitLiteral( this ); }
        
    /**
        Literals are not in any particular model, and so inModel can return this.
        @param m a model to move the literal into
        @return this
    */
    @Override
    public Literal inModel( Model m )
        {
        return getModel() == m 
            ? this 
            : (Literal) m.getRDFNode( asNode() )
            ;
         }
    
    @Override
    public Literal asLiteral()
        { return this; }
    
    @Override
    public Resource asResource()
        { throw new ResourceRequiredException( asNode() ); }
    
    /**
        Answer the model this literal was created in, if any, otherwise null.
    */
    @Override
    public Model getModel()
        { return (ModelCom) getGraph(); }
    
    @Override public String toString() {
        return asNode().toString( PrefixMapping.Standard, false );
    }
    
    /**
     * Return the value of the literal. In the case of plain literals
     * this will return the literal string. In the case of typed literals
     * it will return a java object representing the value. In the case
     * of typed literals representing a java primitive then the appropriate
     * java wrapper class (Integer etc) will be returned.
     */
    @Override
    public Object getValue() {
        return asNode().getLiteralValue();
    }
    
    /**
     * Return the datatype of the literal. This will be null in the
     * case of plain literals.
     */
    @Override
    public RDFDatatype getDatatype() {
        return asNode().getLiteralDatatype();
    }
     
    /**
     * Return the uri of the datatype of the literal. This will be null in the
     * case of plain literals.
     */
    @Override
    public String getDatatypeURI() {
        return asNode().getLiteralDatatypeURI();
    }
    
    /**
     * Return true if this is a "plain" (i.e. old style, not typed) literal.
     */
    public boolean isPlainLiteral() {
        return asNode().getLiteralDatatype() == null;
    }
    
    /**
     * Return the lexical form of the literal.
     */
    @Override
    public String getLexicalForm() {
        return asNode().getLiteralLexicalForm();
    }

    @Override
    public boolean getBoolean()  {
        Object value = asNode().getLiteralValue();
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
                return (Boolean) value;
            } else {
                throw new DatatypeFormatException(this.toString() + " is not a Boolean");
            }
        }
    }
    
    @Override
    public byte getByte()  {
        if (isPlainLiteral()) {
            return Byte.parseByte(getLexicalForm());
        } else {
            return byteValue( asNumber( getValue() ) );
        }
    }

    
    @Override
    public short getShort()  {
        if (isPlainLiteral()) {
            return Short.parseShort(getLexicalForm());
        } else {
            return shortValue( asNumber( getValue() ) );
        }
    }

    @Override
    public int getInt()  {
        if (isPlainLiteral()) {
            return Integer.parseInt(getLexicalForm());
        } else {
            return intValue( asNumber( getValue() ) );
        }
    }

    @Override
    public long getLong()  {
        if (isPlainLiteral()) {
            return Long.parseLong(getLexicalForm());
        } else {
            return asNumber(getValue()).longValue();
        }
    }

    @Override
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
                return (Character) value;
            } else {
                throw new DatatypeFormatException(value.toString() + " is not a Character");
            }
        }
    }
    
    @Override
    public float getFloat()  {
        if (isPlainLiteral()) {
            return Float.parseFloat(getLexicalForm());
        } else {
            return asNumber(getValue()).floatValue();
        }
    }

    @Override
    public double getDouble()  {
        if (isPlainLiteral()) {
            return Double.parseDouble(getLexicalForm());
        } else {
            return asNumber(getValue()).doubleValue();
        }
    }

    @Override
    public String getString()  {
        return asNode().getLiteralLexicalForm();
    }
    
//    @Deprecated public Object getObject(ObjectF f)  {
//        if (isPlainLiteral()) {
//            try {
//                return f.createObject(getString());
//            } catch (Exception e) {
//                throw new JenaException(e);
//            }
//        } else {
//            return getValue();
//        }
//    }
    
    @Override
    public String getLanguage() {
        return asNode().getLiteralLanguage();
    }
    
    @Override
    public boolean isWellFormedXML() {
        return asNode().getLiteralIsXML();
    } 
   
    /**
     * Test that two literals are semantically equivalent.
     * In some cases this may be the sames as equals, in others
     * equals is stricter. For example, two xsd:int literals with
     * the same value but different language tag are semantically
     * equivalent but distinguished by the java equality function
     * in order to support round tripping.
     */
    @Override
    public boolean sameValueAs(Literal other) {
        return asNode().sameValueAs(other.asNode());
    }
        
     // Internal helper method to convert a value to number
    private Number asNumber(Object value) {
        if (value instanceof Number) {
            return ((Number)value);
        } else {
            String message = "Error converting typed value to a number. \n";
            message += "Datatype is: " + getDatatypeURI();
            if ( getDatatypeURI() == null || ! getDatatypeURI().startsWith(XSDDatatype.XSD)) {
                message +=" which is not an xsd type.";
            }
            message += " \n";
            String type = 
            message += "Java representation type is " + (value == null ? "null" : value.getClass().toString());
            throw new DatatypeFormatException(message);
        }
    }    
    private byte byteValue( Number n )
        {
        return (byte) getIntegralValueInRange( Byte.MIN_VALUE, n, Byte.MAX_VALUE );
        }

    private short shortValue( Number n )
        {
        return (short) getIntegralValueInRange( Short.MIN_VALUE, n, Short.MAX_VALUE );
        }

    private int intValue( Number n )
        {        
        return (int) getIntegralValueInRange( Integer.MIN_VALUE, n, Integer.MAX_VALUE );
        }

    private long getIntegralValueInRange( long min, Number n, long max )
        {
        long result = n.longValue();
        if (min <= result && result <= max) return result;
        throw new IllegalArgumentException( "byte value required: " + result );
        }
        
}
