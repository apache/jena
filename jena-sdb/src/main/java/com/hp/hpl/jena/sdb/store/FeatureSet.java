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

package com.hp.hpl.jena.sdb.store;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.atlas.io.PrintableBase ;
import org.apache.jena.atlas.iterator.Iter ;

/** A set of features (order retained */

public class FeatureSet extends PrintableBase implements Printable, Iterable<Feature>
{
    List <Feature> features = new ArrayList<Feature>() ;
    
    public FeatureSet() {}
    public void addFeature(Feature feature)
    { 
        if ( features.contains(feature) )
            return ;
        features.add(feature) ;
    }
    
    public boolean hasFeature(Feature feature) { return features.contains(feature) ; } 

    public Feature getFeature(String name)
    { 
        for ( Feature f : features )
            if ( f.getName().equals(name) ) 
                return f ;
        return null ;
    }

    public boolean hasFeature(String name) { return getFeature(name) != null ; }
    
    public List <Feature> getFeatures() { return features ; }

    @Override
    public Iterator<Feature> iterator()
    { return features.iterator() ; }

    @Override
    public void output(IndentedWriter out)
    {
        out.print(Iter.asString(features)) ;
    }
}
