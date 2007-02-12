/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.syntax;

//import org.apache.commons.logging.*;
import java.util.* ;

import com.hp.hpl.jena.query.core.LabelMap;
import com.hp.hpl.jena.query.expr.Expr;

/** ElementFunction - an extension point for new elements.
 * 
 * @author Andy Seaborne
 * @version $Id: ElementExtension.java,v 1.11 2007/01/02 11:20:30 andy_seaborne Exp $
 */

public class ElementExtension extends Element
{
    private String uriStr ;
    private List args ;
    
    
    public ElementExtension(String uri, List args, Element element)
    {
        this.uriStr = uri ;
        this.args = args ;
    }
    
    public String getURI() { return uriStr ; }
    public List getArgs()  { return args ; }
    /** Get the ith argument (counting 1) */ 
    public Expr getArg(int i)
    { 
        if ( i < 1 || i > args.size() )
            return null ;
        return (Expr)args.get(i-1) ;
    }
    public int numArgs()  { return args.size() ; }
    
    //@Override
    public int hashCode() { return uriStr.hashCode() ^ args.hashCode() ; }

    //@Override
    public boolean equalTo(Element el2, LabelMap labelMap)
    {
        if ( el2 == null ) return false ;

        if ( ! ( el2 instanceof ElementExtension ) ) 
            return false ;
        ElementExtension ext2 = (ElementExtension)el2 ;
        if ( this.getURI() != ext2.getURI() )
            return false ;
        if ( ! this.getArgs().equals(ext2.getArgs()) )
            return false ;
        return true ;
    }
    
    public void visit(ElementVisitor v) { v.visit(this) ; }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
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