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

package com.hp.hpl.jena.rdf.model;

import java.util.Calendar;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
import com.hp.hpl.jena.rdf.model.impl.*;

/** A Factory class for creating resources.
 * 
 * <p> This class creates resources and properties and things of that ilk.
 * These resources are <i>not</i> associated with a user-modifiable
 * model: doing getModel() on them will return null.
 * </p>
 * 
 * <p> It is designed as a singleton.  There are static convenience
 * methods on this class itself, so the easy way to create resource
 * is for example to do something like:</p>
 * <pre> 
 *   <code>Resource r = ResourceFactory.createResource();</code>
 * </pre>
 * 
 * <p> If a factory object is needed, then this can be obtained using
 * the <code>getInstance</code> method on the class.  The factory
 * object used may be changed using the <code>setInstance</code>
 * method.</p> 
*/
public class ResourceFactory {
    protected static Interface instance = new Impl();

    private ResourceFactory() {
    }

    /** get the current factory object.
     * 
     *  @return the current factory object
     */
    public static Interface getInstance() {
        return instance;
    }
    /** set the current factory object.
     * 
     * @param newInstance the new factory object
     * @return the previous factory object
     */
    public static Interface setInstance(Interface newInstance) {
        Interface previousInstance = instance;
        instance = newInstance;
        return previousInstance;
    }

    /** create a new anonymous resource.
     * 
     * <p>Uses the current factory object to create a new anonymous resource.</p>
     * 
     * @return a new anonymous resource
     */
    public static Resource createResource() {
        return instance.createResource();
    }

    /** create a new resource.
     * 
     * <p>Uses the current factory object to create a new resource.</p>
     * 
     * @param uriref URIREF of the resource
     * @return a new resource
     */
    public static Resource createResource(String uriref) {
        return instance.createResource(uriref);
    }
    
    /**
    Answer a plain (untyped) literal with no language and the given content.
    @param string the string which forms the value of the literal
    @return a Literal node with that string as value
     */
    public static Literal createPlainLiteral( String string ) {
        return instance.createPlainLiteral( string );
    }
    
    /**
    Answer a plain (untyped) literal with no language and the given content.
    @param string the string which forms the value of the literal
    @param lang The language tag to be used
    @return a Literal node with that string as value
     */

    public static Literal createLangLiteral( String string , String lang ) {
        return instance.createLangLiteral( string , lang );
    }

    /**
    Answer a typed literal.
    @param string the string which forms the value of the literal
    @param dType RDFDatatype of the type literal
    @return a Literal node with that string as value
    */

    public static Literal createTypedLiteral( String string , RDFDatatype dType)
    {
        return instance.createTypedLiteral( string , dType);
    }
    
    /**
    Answer a typed literal.
    @param value a java Object, the default RDFDatatype for that object will be used
    @return a Literal node with that value
    */
    public static Literal createTypedLiteral( Object value ) {
        return instance.createTypedLiteral(value);
    }
    
    /** create a new property.
     * 
     * <p>Uses the current factory object to create a new resource.</p>
     * 
     * @param uriref URIREF of the property
     * @return a new property
     */
    public static Property createProperty(String uriref) {
        return instance.createProperty(uriref);
    }

    /** create a new property.
     * 
     * <p>Uses the current factory object to create a new property.</p>
     * 
     * @param namespace URIREF of the namespace of the property
     * @param localName localname of the property
     * @return a new property
     */
    public static Property createProperty(String namespace, String localName) {
        return instance.createProperty(namespace, localName);
    }

    /** create a new statement.
     * Uses the current factory object to create a new statement.</p>
     * 
     * @param subject the subject of the new statement
     * @param predicate the predicate of the new statement
     * @param object the objectof the new statement
     * @return a new resource
     */
    public static Statement createStatement(
        Resource subject,
        Property predicate,
        RDFNode object) {
        return instance.createStatement(subject, predicate, object);
    }

    /** the interface to resource factory objects.
     */
    public interface Interface {

        /** create a new anonymous resource.
         * 
         * @return a new anonymous resource
         */
        public Resource createResource();

        /** create a new resource.
         * 
         * @param uriref URIREF of the resource
         * @return a new resource
         */
        public Resource createResource(String uriref);
        
        /**
            Answer a plain (untyped) literal with no language and the given content.
            @param string the string which forms the value of the literal
            @return a Literal node with that string as value
        */
        public Literal createPlainLiteral( String string );

        /**
        Answer a plain (untyped) literal with no language and the given content.
        @param string the string which forms the value of the literal
        @param lang The language tag to be used
        @return a Literal node with that string as value
         */

        public Literal createLangLiteral( String string , String lang );

        /**
        Answer a typed literal.
        @param string the string which forms the value of the literal
        @param datatype RDFDatatype of the type literal
        @return a Literal node with that string as value
        */
        public Literal createTypedLiteral( String string , RDFDatatype datatype) ;


        /**
        Answer a typed literal.
        @param value a java Object, the default RDFDatatype for that object will be used
        @return a Literal node with that value
        */
        public Literal createTypedLiteral( Object value ) ;

        /** create a new property.
         * 
         * @param uriref URIREF of the property
         * @return a new property
         */
        public Property createProperty(String uriref);

        /** create a new property.
         * 
         * @param namespace uriref of the namespace
         * @param localName localname of the property
         * @return a new property
         */
        public Property createProperty(String namespace, String localName);

        /** create a new statement.
         * 
         * @param subject subject of the new statement
         * @param predicate predicate of the new statement
         * @param object object of the new statement
         * @return a new statement
         */
        public Statement createStatement(
            Resource subject,
            Property predicate,
            RDFNode object);
    }

    static class Impl implements Interface {

        Impl() {
        }

        @Override
        public Resource createResource() {
            return new ResourceImpl();
        }

        @Override
        public Resource createResource(String uriref) {
            return new ResourceImpl(uriref);
        }
        
        @Override
        public Literal createPlainLiteral( String string ) {
            return new LiteralImpl(  NodeFactory.createLiteral( string, "", false ), null );
        }

        @Override
        public Literal createLangLiteral( String string , String lang ) {
            return new LiteralImpl(  NodeFactory.createLiteral( string, lang, false ), null );
        }
        
        @Override
        public Literal createTypedLiteral( String string , RDFDatatype dType)
        {
            return new LiteralImpl(NodeFactory.createLiteral(string, "", dType), null) ;
        }

        @Override
        public Literal createTypedLiteral( Object value ) {
            LiteralLabel ll = null;
            if (value instanceof Calendar) {
                Object valuec = new XSDDateTime( (Calendar) value);
                ll = LiteralLabelFactory.create(valuec, "", XSDDatatype.XSDdateTime);
            } else {
                ll =  LiteralLabelFactory.create(value);
            }
            return new LiteralImpl(NodeFactory.createLiteral( ll ), null) ;
        }
        
        @Override
        public Property createProperty(String uriref) {
            return new PropertyImpl(uriref);
        }

        @Override
        public Property createProperty(String namespace, String localName) {
            return new PropertyImpl(namespace, localName);
        }

        @Override
        public Statement createStatement(
            Resource subject,
            Property predicate,
            RDFNode object) {
            return new StatementImpl(subject, predicate, object);
        }

    }
}
