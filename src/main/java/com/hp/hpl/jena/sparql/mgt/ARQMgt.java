/**
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

import java.lang.management.ManagementFactory ;
import java.util.HashMap ;
import java.util.Map ;

import javax.management.InstanceAlreadyExistsException ;
import javax.management.InstanceNotFoundException ;
import javax.management.MBeanRegistrationException ;
import javax.management.MBeanServer ;
import javax.management.MalformedObjectNameException ;
import javax.management.NotCompliantMBeanException ;
import javax.management.ObjectName ;

import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.engine.QueryEngineBase ;

public class ARQMgt
{
    // In some environments, JMX does not exist.
    static private Logger log = LoggerFactory.getLogger(ARQMgt.class) ;
    private static boolean initialized = false ;
    private static boolean noJMX = false ;
    private static Map<ObjectName, Object> mgtObjects = new HashMap<ObjectName, Object>() ;
    private static MBeanServer mbs = null ;  
    
    public static synchronized void init()
    {
        if ( initialized )
            return ;
        initialized = true ;

        try {

            mbs = ManagementFactory.getPlatformMBeanServer();

            String NS = ARQ.PATH ;

            ContextMBean cxtBean = new ContextMBean(ARQ.getContext()) ;
            QueryEngineInfo qeInfo = QueryEngineBase.queryEngineInfo ;

            // Done in ARQ initialization -- register(NS+".system:type=SystemInfo", ARQ.systemInfo) ;
            register(NS+".system:type=Context", cxtBean) ;
            register(NS+".system:type=Engine", qeInfo) ;

        } catch (Throwable ex) {
            Log.warn(ARQMgt.class, "Failed to initialize JMX", ex) ;
            noJMX = true ;
            mbs = null ;
        }
    }
    
    public static void register(String name, Object bean)
    {
        init() ;
        
        if ( noJMX )
            return ;
        
        ObjectName objName = null ;
        try
        { objName = new ObjectName(name) ; }
        catch (MalformedObjectNameException ex)
        {  throw new ARQException("Failed to create name '"+name+"': "+ex.getMessage(), ex) ; }
        
        try {
            // Unregister to avoid classloader problems.
            // A previous load of this class will have registered something
            // with the object name. Remove it - copes with reloading.
            // (Does not cope with multiple loads running in parallel.)
            if ( mbs.isRegistered(objName) )
            {
                try { mbs.unregisterMBean(objName); }
                catch (InstanceNotFoundException ex) {}
            }
            log.debug("Register MBean: "+objName) ;
            mbs.registerMBean(bean, objName);
            // remember ...
            mgtObjects.put(objName, bean) ;

        } catch (NotCompliantMBeanException ex)
        {
            log.warn("Failed to register '"+objName.getCanonicalName()+"': "+ex.getMessage()) ;
            throw new ARQException("Failed to register '"+objName.getCanonicalName()+"': "+ex.getMessage(), ex) ;
        } catch (InstanceAlreadyExistsException ex)
        {
            log.warn("Failed to register '"+objName.getCanonicalName()+"': "+ex.getMessage()) ;
            throw new ARQException("Failed to register '"+objName.getCanonicalName()+"': "+ex.getMessage(), ex) ;
        } catch (MBeanRegistrationException ex)
        {
            log.warn("Failed to register '"+objName.getCanonicalName()+"': "+ex.getMessage()) ;
            throw new ARQException("Failed to register '"+objName.getCanonicalName()+"': "+ex.getMessage(), ex) ;
        }
    }
}
