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

package org.apache.jena.sparql.pfunction;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.query.QueryBuildException ;
import org.apache.jena.sparql.ARQInternalErrorException ;

/** PropertyFunction factory that instantiates a class each time. */ 

class PropertyFunctionFactoryAuto implements PropertyFunctionFactory
{
    private final Class<? > extClass;

    PropertyFunctionFactoryAuto(Class<? > xClass) {
        extClass = xClass;

        if ( !PropertyFunction.class.isAssignableFrom(xClass) )
            throw new ARQInternalErrorException("No PropertyFunction interface for " + Lib.classShortName(xClass));
    }

    @Override
    public PropertyFunction create(String uri) {
        try {
            return (PropertyFunction)extClass.getConstructor().newInstance();
        }
        catch (Exception e) {
            throw new QueryBuildException("Can't instantiate PropertyFunction for " + uri, e);
        }
    }

    public Class<? > getAutoClass() {
        return extClass;
    }
}
