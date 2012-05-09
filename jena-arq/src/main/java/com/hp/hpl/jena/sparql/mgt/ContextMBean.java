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
    
    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        return getAsString(attribute) ;
    }

    @Override
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

    @Override
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

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException,
                                                                                ReflectionException
    {
        return null ;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
                                                 MBeanException, ReflectionException
    {
        context.set(Symbol.create(attribute.getName()), attribute.getValue()) ;
    }

    @Override
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
