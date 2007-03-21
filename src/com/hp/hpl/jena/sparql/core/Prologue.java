/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.util.*;

public class Prologue implements Printable
{
    private static Log log = LogFactory.getLog(Prologue.class) ;

    // BASE URI
    protected boolean seenBaseURI = false ;
    protected String baseURI = null ;

    // Prefixes
    protected PrefixMapping prefixMap = new PrefixMappingImpl() ;
    
    public Prologue() {}
    
    public Prologue(PrefixMapping pmap) { this.prefixMap = pmap ; }
    
    public Prologue(PrefixMapping pmap, String base) { this.prefixMap = pmap ; setBaseURI(base) ; }
    
    /**
     * @return True if the query has an explicitly set base URI. 
     */
    public boolean explicitlySetBaseURI() { return seenBaseURI ; }

    /**
     * @return Returns the baseURI.
     */
    public String getBaseURI()
    {
        if ( baseURI == null )
            initParserBaseURI() ;
        return baseURI;
    }
    /**
     * @param baseURI The baseURI to set.
     */
    public void setBaseURI(String baseURI)
    {
        this.baseURI = baseURI;
        this.seenBaseURI = true ;
    }
    
    public void initParserBaseURI() { initParserBaseURI(null) ; }
    public void initParserBaseURI(String base)
    {
        if ( baseURI != null )
            return ;
        
        baseURI = RelURI.chooseBaseURI(base) ;
    }
    
    // ---- Query prefixes
    
    /** Set a prefix for this query */
    public void setPrefix(String prefix, String expansion)
    {
        try {
            prefixMap.setNsPrefix(prefix, expansion) ;
        } catch (PrefixMapping.IllegalPrefixException ex)
        {
            log.warn("Illegal prefix mapping(ignored): "+prefix+"=>"+expansion) ;
        }
    }   

    /** Return the prefix map from the parsed query */ 
    public PrefixMapping getPrefixMapping() { return prefixMap ; }
    /** Set the mapping */
    public void setPrefixMapping(PrefixMapping pmap ) { prefixMap = pmap ; }

    /** Lookup a prefix for this query, including the default prefixes */
    public String getPrefix(String prefix)
    {
        return prefixMap.getNsPrefixURI(prefix) ;
    }
    
    /**
     * @deprecated use expandPrefixedName instead
     * @param qname
     * @return String Expanded prefixed name
     */
    
    public String expandQName(String qname)
    { return expandPrefixedName(qname) ; }
    
    
    /** Expand prefixed name 
     * 
     * @param qname  The prefixed name to be expanded
     * @return URI, or null if not expanded.
     */

    public String expandPrefixedName(String qname)
    {
        String s = prefixMap.expandPrefix(qname) ;
        if ( s.equals(qname) )
            return null ;
        return s ;
    }
    
    
    /** Use the prefix map to turn a URI into a qname, or return the original URI */
    
    public String shortForm(String uri)
    {
        return prefixMap.shortForm(uri) ;
    }

    public String toString()
    { return PrintUtils.toString(this) ; }
    
    public String toString(PrefixMapping pmap)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        IndentedWriter out = buff.getIndentedWriter() ;
        this.output(out) ;
        return buff.toString() ;
    }

    public void output(IndentedWriter out)
    {
        printBase(out) ;
        printPrefixes(out) ;
    }
    
    private void printBase(IndentedWriter out)
    {
        if ( getBaseURI() != null && explicitlySetBaseURI() )
        {
            out.print("BASE    ") ;
            out.print("<"+getBaseURI()+">") ;
            out.newline() ;
        }
    }
    
    public void printPrefixes(IndentedWriter out)
    {
        Map pmap = null ;
        
        if ( getPrefixMapping() instanceof PrefixMapping2 )
        {
            PrefixMapping2 pm2 = (PrefixMapping2)getPrefixMapping() ;
            pmap = pm2.getNsPrefixMap(false) ;
        }
        else
            pmap = getPrefixMapping().getNsPrefixMap() ;
        
        if ( pmap.size() > 0 )
        {
            //boolean first = true ;
            for ( Iterator iter = pmap.keySet().iterator() ; iter.hasNext() ; )
            {
                String k = (String)iter.next() ;
                String v = (String)pmap.get(k) ;
                out.print("PREFIX  ") ;
                out.print(k) ;
                out.print(':') ;
                out.print(' ', -k.length()) ;
                // Include at least one space 
                out.print(" <"+v+">") ;
                out.newline() ;
            }
        }
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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