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
import com.hp.hpl.jena.shared.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An implementation of Property.
 */

public class PropertyImpl extends ResourceImpl implements Property
    {

    @SuppressWarnings("hiding")
    final static public Implementation factory = new Implementation() 
        {
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
            { return n.isURI(); }

        @Override
        public EnhNode wrap( Node n, EnhGraph eg )
            { return new PropertyImpl( n, eg ); }
    };

    protected static Logger logger = LoggerFactory.getLogger( PropertyImpl.class );

    protected int ordinal = -1;

    /** Creates new PropertyImpl */
    public PropertyImpl( String uri )
        {
        super( uri );
        checkLocalName();
        checkOrdinal();
        }

    @Override public Property inModel( Model m )
        { return getModel() == m ? this : m.createProperty( getURI() ); }

    private void checkLocalName()
        {
        String localName = getLocalName();
        if (localName == null || localName.equals( "" )) 
            throw new InvalidPropertyURIException( getURI() );
        }

    public PropertyImpl( String nameSpace, String localName )
        {
        super( nameSpace, localName );
        checkLocalName();
        checkOrdinal();
        }

    public PropertyImpl( String uri, ModelCom m )
        {
        super( uri, m );
        checkOrdinal();
        }

    public PropertyImpl( String nameSpace, String localName, ModelCom m )
        {
        super( nameSpace, localName, m );
        checkOrdinal();
        }

    public PropertyImpl( Node n, EnhGraph m )
        {
        super( n, m );
        checkOrdinal();
        }

    public PropertyImpl( String nameSpace, String localName, int ordinal, ModelCom m )
        {
        super( nameSpace, localName, m );
        checkLocalName();
        this.ordinal = ordinal;
        }

    @Override
    public boolean isProperty()
        { return true; }

    @Override
    public int getOrdinal()
        {
        if (ordinal < 0) ordinal = computeOrdinal();
        return ordinal;
        }

    private int computeOrdinal()
        {
        String localName = getLocalName();
        if (getNameSpace().equals( RDF.getURI() ) && localName.matches( "_[0-9]+" )) 
            return parseInt( localName.substring( 1 ) );
        return 0;
        }

    private int parseInt( String digits )
        {
        try { return Integer.parseInt( digits );}
        catch (NumberFormatException e) { throw new JenaException( "checkOrdinal fails on " + digits, e ); }
        }

    // Remove shortly.

    protected void checkOrdinal()
        {
        // char c;
        // String nameSpace = getNameSpace();
        // String localName = getLocalName();
        // // check for an rdf:_xxx property
        // if (localName.length() > 0)
        // {
        // if (localName.charAt(0) == '_' && nameSpace.equals(RDF.getURI())
        // && nameSpace.equals(RDF.getURI())
        // && localName.length() > 1
        // )
        // {
        // for (int i=1; i<localName.length(); i++) {
        // c = localName.charAt(i);
        // if (c < '0' || c > '9') return;
        // }
        //                try {
        //                  ordinal = Integer.parseInt(localName.substring(1));
        //                } catch (NumberFormatException e) {
        //                    logger.error( "checkOrdinal fails on " + localName, e );
        //                }
        //            }
        //        }
        }

    }
