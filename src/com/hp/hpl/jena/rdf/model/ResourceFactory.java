/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ResourceFactory.java,v 1.2 2003-04-03 12:22:35 jeremy_carroll Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;

/** A Factory class for creating resources.
 * 
 * <p> This class creates resources and properties and things of that ilk.</p>
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
     * @param localname localname of the property
     * @return a new property
     */
    public static Property createProperty(String namespace, String localName) {
        return instance.createProperty(namespace, localName);
    }

    /** create a new statement.
     * 
     * <p>Uses the current factory object to create a new statement.</p>
     * 
     * @param uri of the URI of the resource
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

        public Resource createResource() {
            return new ResourceImpl();
        }

        public Resource createResource(String uriref) {
            return new ResourceImpl(uriref);
        }

        public Property createProperty(String uriref) {
            return new PropertyImpl(uriref);
        }

        public Property createProperty(String namespace, String localName) {
            return new PropertyImpl(namespace, localName);
        }

        public Statement createStatement(
            Resource subject,
            Property predicate,
            RDFNode object) {
            return new StatementImpl(subject, predicate, object);
        }

    }
}

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/