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
 * PropertyImpl.java
 *
 * Created on 03 August 2000, 13:47
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/** An implementation of Property.
 *
 * @author  bwm
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.2 $' Date='$Date: 2003-02-19 10:53:39 $'
 */

// next free error code = 2

public class PropertyImpl extends ResourceImpl implements Property {

    final static public Implementation factory = new Implementation() {
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new PropertyImpl(n,eg);
        }
    };
    
    protected int    ordinal   = 0;

    private PropertyImpl( Resource r )
        { this( r.getURI(), r.getModel() ); }
        
    /** Creates new PropertyImpl */
    public PropertyImpl(String uri) throws RDFException {
        super( uri );
        checkLocalName();
        checkOrdinal();
    }
    
    private void checkLocalName()
        {
        String localName = getLocalName();
        if (localName == null || localName.equals( "" )) 
            throw new RDFException( RDFException.INVALIDPROPERTYURI );
        }

    public PropertyImpl(String nameSpace, String localName)
      throws RDFException {
        super(nameSpace, localName);
        checkLocalName();
        checkOrdinal();
    }

    public PropertyImpl(String uri, Model m) throws RDFException {
        super(uri, m);
        checkOrdinal();
    }

    public PropertyImpl(String nameSpace, String localName, Model m)
      throws RDFException {
        super(nameSpace, localName, m);
        checkOrdinal();
    }
    
    public PropertyImpl(Node n, EnhGraph m)
      throws RDFException {
        super(n, m);
        checkOrdinal();
    }

    public PropertyImpl(String nameSpace,
                        String localName,
                        int ordinal,
                        Model m) throws RDFException {
        super(nameSpace, localName, m);
        checkLocalName();
        this.ordinal = ordinal;
    }

    public boolean isProperty() {
    	return true;
    }
    
    public int getOrdinal() {
        return ordinal;
    }

    protected void checkOrdinal()
    {
        char c;
        String nameSpace = getNameSpace();
        String localName = getLocalName();
        // check for an rdf:_xxx property
        if (localName.length() > 0) 
            {
            if (localName.charAt(0) == '_' && nameSpace.equals(RDF.getURI())
                && nameSpace.equals(RDF.getURI())
                && localName.length() > 1
                ) 
                {
                for (int i=1; i<localName.length(); i++) {
                    c = localName.charAt(i);
                    if (c < '0'  || c > '9') return;
                }
                try {
                  ordinal = Integer.parseInt(localName.substring(1));
                } catch (NumberFormatException e) {
                    ErrorHelper.logInternalError(this.getClass().getName(), 1);
                }
            }
        }
    }

}
