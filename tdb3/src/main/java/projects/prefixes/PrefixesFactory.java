/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package projects.prefixes;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Pair ;

import org.apache.jena.graph.Node ;
import org.apache.jena.shared.PrefixMapping ;

/** Factory */
// Just "Prefixes"?
// Mixes storage and API level.
public class PrefixesFactory
{
    public static PrefixMapI createMem() { return newPrefixMap(newPrefixMapStorageMem() ) ; }
    
    public static DatasetPrefixesStorage2 newDatasetPrefixesMem()
    { return new DatasetPrefixesMem() ; }
    
    public static PrefixMapping newPrefixMappingOverPrefixMapI(PrefixMapI pmap)
    { return new PrefixMappingOverPrefixMapI(pmap) ; }
    
    public static PrefixMapStorage newPrefixMapStorageMem()
    { return new PrefixMapStorageMem() ; }
    
    public static PrefixMapI newPrefixMap(PrefixMapStorage storage)
    { return new PrefixMapBase(storage) ; }
    
    public static PrefixMapI empty() { return emptyPrefixMap ; }
    
    static DatasetPrefixesStorage2 emptyDatasetPrefixes = new EmptyDatasetPrefixes() ;
    static PrefixMapI emptyPrefixMap = newPrefixMap(PrefixMapStorageView.viewDefaultGraph(emptyDatasetPrefixes)) ;
    
    private static class EmptyDatasetPrefixes implements DatasetPrefixesStorage2
    {
        @Override
        public String get(Node graphNode, String prefix)
        { return null ; }

        @Override
        public Iterator<PrefixEntry> get(Node graphNode)
        { return Iter.nullIterator() ; }

        @Override
        public Iterator<Node> listGraphNodes()
        { return Iter.nullIterator() ; }

        @Override
        public void add(Node graphNode, String prefix, String iriStr)
        { new UnsupportedOperationException() ; }

        @Override
        public void delete(Node graphNode, String prefix)
        { new UnsupportedOperationException() ; }

        @Override
        public void deleteAll(Node graphNode)
        { new UnsupportedOperationException() ; }

        @Override
        public String abbreviate(Node graphNode, String iriStr) {
            return null ;
        }

        @Override
        public Pair<String, String> abbrev(Node graphNode, String uriStr) {
            return null ;
        }

        @Override
        public String expand(Node graphNode, String prefixedName) {
            return null ;
        }

        @Override
        public String expand(Node graphNode, String prefix, String localName) {
            return null ;
        }

        @Override
        public boolean isEmpty() {
            return true ;
        }

        @Override
        public int size() {
            return 0 ;
        }
    }
//    
//    static PrefixMapI emptyPrefixMapEmpty = new PrefixMapI() {
//
//        @Override
//        public Map<String, IRI> getMapping()
//        {
//            Map<String, IRI> x = DS.map() ;
//            return Collections.unmodifiableMap(x) ;
//        }
//
//        @Override
//        public Map<String, IRI> getMappingCopy()
//        {
//            return getMapping() ;
//        }
//
//        @Override
//        public Map<String, String> getMappingCopyStr()
//        {
//            Map<String, String> x = DS.map() ;
//            return Collections.unmodifiableMap(x) ;
//        }
//
//        @Override
//        public PrefixMapStorage getPrefixMapStorage()
//        {
//            return new PrefixMapStorage() {
//                @Override
//                public boolean containsKey(String key)
//                {
//                    return false ;
//                }
//                
//                @Override
//                public void sync()
//                {}
//                
//                @Override
//                public void remove(String prefix)
//                {}
//                
//                @Override
//                public void put(String prefix, String uriStr)
//                {}
//                
//                @Override
//                public Iterator<String> keys()
//                {
//                    return null ;
//                }
//                
//                @Override
//                public Iterator<Pair<String, String>> iterator()
//                {
//                    return null ;
//                }
//                
//                @Override
//                public boolean isEmpty()
//                {
//                    return false ;
//                }
//                
//                @Override
//                public String get(String prefix)
//                {
//                    return null ;
//                }
//                
//                @Override
//                public void close()
//                {}
//                
//                @Override
//                public void clear()
//                {}
//            } ;
//        }
//
//        @Override
//        public void add(String prefix, String iriString)
//        { throw new UnsupportedOperationException() ; }
//
//        @Override
//        public void add(String prefix, IRI iri)
//        { throw new UnsupportedOperationException() ; }
//
//        @Override
//        public void putAll(PrefixMapI pmap)
//        { throw new UnsupportedOperationException() ; }
//
//        @Override
//        public void delete(String prefix)
//        {}
//
//        @Override
//        public String get(String prefix)
//        {
//            return null ;
//        }
//
//        @Override
//        public boolean contains(String prefix)
//        {
//            return false ;
//        }
//
//        @Override
//        public String abbreviate(String uriStr)
//        {
//            return null ;
//        }
//
//        @Override
//        public Pair<String, String> abbrev(String uriStr)
//        {
//            return null ;
//        }
//
//        @Override
//        public String expand(String prefixedName)
//        {
//            return null ;
//        }
//
//        @Override
//        public String expand(String prefix, String localName)
//        {
//            return null ;
//        }
//    } ;
}

