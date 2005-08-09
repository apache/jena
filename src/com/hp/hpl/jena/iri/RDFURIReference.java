/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri;

import java.net.*;
import java.util.*;

public interface RDFURIReference extends IRIConformanceLevels {

    public boolean isAbsolute();
    public boolean isOpaque();
    public boolean isRelative();
    public boolean isRDFURIReference();
    public boolean isIRI();
    public boolean isJavaNetURI();
    public boolean isURIinASCII();
    boolean isVeryBad();
    boolean isXSanyURI();
    boolean hasException(int conformance);
    Iterator exceptions(int conformance);
    boolean hasException();
    Iterator exceptions();
    

    

    public RDFURIReference resolve(RDFURIReference rel);
    public RDFURIReference resolve(String uri);
    

    public URL toURL() throws MalformedURLException;

    // not well specified really ...
//    public RDFURIReference relativize(RDFURIReference x);


    public String toString();


    public String toASCIIString();
    
   

    static final public int SAMEDOCUMENT = 1;
    static final public int NETWORK = 2;
    static final public int ABSOLUTE = 4;
    static final public int RELATIVE = 8;
    static final public int PARENT = 16;
    static final public int GRANDPARENT = 32;
    
    public String relativize(String abs, int flags);
    public RDFURIReference relativize(RDFURIReference abs, int flags);
    public String getUserinfo();
    public int getPort();
    public String getPath();
    public String getQuery();
    public String getFragment();
    public String getHost();
    public String getScheme();

}

/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 *
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
 */

