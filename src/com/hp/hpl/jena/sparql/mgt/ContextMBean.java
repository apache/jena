/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.mgt;

import javax.management.* ;

import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;

/** MBean for a context object (which can change) */
public class ContextMBean implements DynamicMBean
{
    private final Context context ;
    
    public ContextMBean(Context context)
    { 
        this.context = context ;
    }

    private Object getAsString(String name) { return context.getAsString(Symbol.create(name)) ; }
    
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        return getAsString(attribute) ;
    }

    public AttributeList getAttributes(String[] attributes)
    {
        AttributeList x = new AttributeList() ;
        for ( String k : attributes )
        {
            Attribute a = new Attribute(k,  getAsString(k)) ; 
            x.add(a) ;
        }
        return x ;
    }

    public MBeanInfo getMBeanInfo()
    {
        /*
         * MBeanAttributeInfo(String name, String type, String description, boolean isReadable, boolean isWritable, boolean isIs) 
         */
        MBeanAttributeInfo[] attrInfo = new MBeanAttributeInfo[context.size()] ;
        int idx = 0 ;
        for ( Symbol sk : context.keys() )
        {
            // Not all are settable - only is string, boolean, integer.
            Object obj = context.get(sk) ;
            boolean settable = false ;
            
            if ( obj instanceof String ) settable = true ;
            if ( obj instanceof Boolean ) settable = true ;
            if ( obj instanceof Integer ) settable = true ;
            
            MBeanAttributeInfo attr = new MBeanAttributeInfo(sk.getSymbol(), "java.lang.String", sk.getSymbol(),
                                                             true, settable, false) ;
            attrInfo[idx++] = attr ;
        }
        
        /*
         * String className,
                 String description,
                 MBeanAttributeInfo[] attributes,
                 MBeanConstructorInfo[] constructors,
                 MBeanOperationInfo[] operations,
                 MBeanNotificationInfo[] notifications)
          throws IllegalArgumentException
         */
        MBeanInfo info = new MBeanInfo(this.getClass().getName(), "ARQ global context",
                                       attrInfo,
                                       null,        // Constructors
                                       null,        // Operations
                                       null         // Notifications
                                        ) ;
        return info ;
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
                                                                                ReflectionException
    {
        return null ;
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
                                                 MBeanException, ReflectionException
    {
        context.set(Symbol.create(attribute.getName()), attribute.getValue()) ;
    }

    public AttributeList setAttributes(AttributeList attributes)
    {
        AttributeList results = new AttributeList() ;
        for ( Object obj : attributes )
        {
            Attribute a = (Attribute)obj ;
            
            Object value = a.getValue() ;
            Object oldValue = getAsString(a.getName()) ;
            
            // Check type of old value.
            //if ( oldValue instanceof Boolean )
            
            try { setAttribute(a) ; } catch (Exception ex) {}
            results.add(new Attribute( a.getName(), getAsString(a.getName()) )) ;
        }
        
        return results ;
    }
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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