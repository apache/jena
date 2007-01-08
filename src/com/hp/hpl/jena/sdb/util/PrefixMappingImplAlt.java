/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PrefixMappingImplAlt.java,v 1.2 2006/04/29 17:06:29 andy_seaborne Exp $
*/

package com.hp.hpl.jena.sdb.util;

import java.util.Iterator;
import java.util.Map;

import org.apache.xerces.util.XMLChar;

import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.CollectionFactory;

/**
 * Implemention of PrefixMapping that separates out storage of the (string,string)
 * mapping from the interface and application-level rules about prefixes.  
 *   
 * Based largely on PrefixMappingImpl by kers.
 * @author Andy Seaborne
 * @version $Id: PrefixMappingImplAlt.java,v 1.2 2006/04/29 17:06:29 andy_seaborne Exp $
 */
public abstract class PrefixMappingImplAlt implements PrefixMapping
{
    static class PrefixMappingEntry extends Pair<String, String>
    {
        public PrefixMappingEntry(String a, String b)
        {
            super(a, b) ;
        }
        
        public String getPrefix() { return car() ; }
        public String getURI()    { return cdr() ; }
    }
    
    // ----------------------------
    // The contract with the storage implementation.
    // Note: there can be more then one prefix for a URI,
    // but only one URI for a prefix. 
    // Arguments are not null.
    
    // Remove all prefixes.  
    abstract public void clear() ;

    // prefix => URI
    abstract public String getByPrefix( String prefix ) ;

    // URI => prefix (any choice if there is more that one)
    abstract public String getByURI( String uri ) ;
    
    // Add a prefix, replacing any old use of prefix. 
    abstract public void set( String prefix, String uri ) ;
    
    // remove prefix 
    abstract public void removeByPrefix(String prefix) ;
    
    // remove all prefixes leading to this URI  
    abstract public void removeByURI(String uri) ;
    
    // Get a short form.  If multiple choices, choose one.  
    abstract public PrefixMappingEntry findPrefixFor(String uri) ;
    
    // Get all entries.  
    abstract public Iterator<PrefixMappingEntry> getEntries() ;
    // ----------------------------
    
    protected boolean locked = false ;
   

    public PrefixMapping setNsPrefix( String prefix, String uri ) 
    {
        checkNotNull(prefix) ;
        checkNotNull(uri) ;
        checkUnlocked();
        checkLegal( prefix );
        if (!prefix.equals( "" ))
            checkProper( uri );
        set( prefix, uri );
        return this;
    }

    
    public PrefixMapping removeNsPrefix(String prefix)
    {
        checkNotNull(prefix) ;
        checkUnlocked() ;
        removeByPrefix(prefix) ;
        return this ;
    }
    
        
    private void checkProper( String uri )
    {
        checkNotNull(uri) ;
//        if (!isNiceURI( uri ))
//            throw new NamespaceEndsWithNameCharException( uri );
    }
        
    public static boolean isNiceURI( String uri )
    {
        checkNotNull(uri) ;
        if (uri.equals( "" ))
            return false;
        char last = uri.charAt( uri.length() - 1 );
        return Util.notNameChar( last ); 
    }
        
    
    public PrefixMapping setNsPrefixes( PrefixMapping other )
    {
        checkNotNull(other) ;
        // Can do a little better if other is a same as us.
        return setNsPrefixes( other.getNsPrefixMap() ) ;
    }

    public PrefixMapping withDefaultMappings( PrefixMapping other )
    {
        checkNotNull(other) ;
        // Can do a little better if other is a same as us.
        checkUnlocked();
        Iterator it = other.getNsPrefixMap().entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry e = (Map.Entry) it.next();
            String prefix = (String) e.getKey(), uri = (String) e.getValue();
            if (getNsPrefixURI( prefix ) == null && getNsURIPrefix( uri ) == null)
                setNsPrefix( prefix, uri );
        }
        return this;
    }


    public PrefixMapping setNsPrefixes( Map other )
    {
        checkNotNull(other) ;
        checkUnlocked();
        Iterator it = other.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry e = (Map.Entry) it.next();
            setNsPrefix( (String) e.getKey(), (String) e.getValue() );
        }
        return this;
    }


    /** Checks that a prefix is "legal" - it must be a valid XML NCName. */
    private void checkLegal( String prefix )
    {
        if (prefix == null ) 
            throw new PrefixMapping.IllegalPrefixException("Null prefix");
        if ( prefix.length() > 0 && !XMLChar.isValidNCName( prefix ))
            throw new PrefixMapping.IllegalPrefixException( prefix ); 
    }
        
    public String getNsPrefixURI( String prefix ) 
    { 
        checkNotNull(prefix) ;
        return getByPrefix( prefix );
    }
        
    @SuppressWarnings("unchecked")
    public Map getNsPrefixMap()
    { 
        Map m = CollectionFactory.createHashedMap() ;
        for ( Iterator<PrefixMappingEntry> iter = getEntries() ; iter.hasNext() ; )
        {
            PrefixMappingEntry e = iter.next() ;
            m.put(e.getPrefix(), e.getURI()) ;
        }
        return m ;
    }
        
    public String getNsURIPrefix(String uri)
    {
        checkNotNull(uri) ;
        return getByURI(uri) ;
    }
        
    public String expandPrefix( String prefixed )
    {
        checkNotNull(prefixed) ;
        int colon = prefixed.indexOf( ':' );
        if (colon < 0) 
            return prefixed;
        else
        {
            String uri = getByPrefix( prefixed.substring( 0, colon ) );
            return uri == null ? prefixed : uri + prefixed.substring( colon + 1 );
        } 
    }


    /** Answer a readable (we hope) representation of this prefix mapping. */
    @Override
    public String toString()
    {
        StringBuilder buff = new StringBuilder() ;
        for ( Iterator<PrefixMappingEntry> iter = getEntries() ; iter.hasNext() ; )
        {
            PrefixMappingEntry e = iter.next() ;
            buff.append(String.format("%s -> %s\n",e.getPrefix(), e.getURI())) ;
        }
        return buff.toString() ;
    }

        
    public String qnameFor( String uri )
    {
        checkNotNull(uri) ;
        int split = Util.splitNamespace( uri );
        String ns = uri.substring( 0, split ), local = uri.substring( split );
        if (local.equals( "" )) return null;
        String prefix = (String) getByURI( ns );
        return prefix == null ? null : prefix + ":" + local;
    }
    
    /**
        Obsolete - use shortForm.
     	@see com.hp.hpl.jena.shared.PrefixMapping#usePrefix(java.lang.String)
     */
    public String usePrefix( String uri )
        { return shortForm( uri ); }
    
    /**
        Compress the URI using the prefix mapping. This version of the code looks
        through all the maplets and checks each candidate prefix URI for being a
        leading substring of the argument URI. There's probably a much more
        efficient algorithm available, preprocessing the prefix strings into some
        kind of search table, but for the moment we don't need it.
    */
    public String shortForm( String uri )
    {
        checkNotNull(uri) ;
        PrefixMappingEntry e = findPrefixFor( uri );
        return e == null ? uri : e.car() + ":" + uri.substring( ((String) e.cdr()).length() );
    }
    
    public boolean samePrefixMappingAs( PrefixMapping other )
    {
        // TODO Improve the dispatch to storage if compatible 
        return equalsByMap( other ) ;
//        return other instanceof PrefixMappingImpl 
//        ? equals( (PrefixMappingImpl) other )
//        : equalsByMap( other )
//        ;
    }

//    protected boolean equals( PrefixMappingImpl other )
//    { return other.sameAs( this ); }
//
//    protected boolean sameAs( PrefixMappingImpl other )
//    { return prefixToURI.equals( other.prefixToURI ); }
//
    protected final boolean equalsByMap( PrefixMapping other )
    { 
        if ( this == other ) return true ;
        checkNotNull(other) ;
        return getNsPrefixMap().equals( other.getNsPrefixMap() );
    }


    public PrefixMapping lock()    { locked = true ; return this ; }

    public PrefixMapping unlock()  { locked = false ; return this ; }
    
    protected void checkUnlocked()
    {   
        if (locked)
            throw new JenaLockedException( this );
    }
    
    private static void checkNotNull(Object x)
    { 
        if ( x == null )
            throw new NullPointerException() ;
    }
    
}


/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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