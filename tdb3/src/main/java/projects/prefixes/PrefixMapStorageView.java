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
import java.util.List ;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Quad ;

/** Implementation over {@link DatasetPrefixesStorage2}.
 */
public class PrefixMapStorageView implements PrefixMapStorage
{
    private final DatasetPrefixesStorage2 dsgPrefixes;
    private final Node graphName ;
    
    public static PrefixMapStorage viewDefaultGraph(DatasetPrefixesStorage2 dsgPrefixes)
    { return new PrefixMapStorageView(dsgPrefixes, null) ; }
    
    public static PrefixMapStorage viewGraph(DatasetPrefixesStorage2 dsgPrefixes, Node graphName) 
    { return new PrefixMapStorageView(dsgPrefixes, graphName) ; }
    
    private PrefixMapStorageView(DatasetPrefixesStorage2 dsgPrefixes, Node graphName)
    {
        this.dsgPrefixes = dsgPrefixes ;
        this.graphName = graphName ;
    }
    
    @Override
    public void put(String prefix, String uriStr)   { dsgPrefixes.add(graphName, prefix, uriStr) ; }
    
    
    @Override
    public String get(String prefix)                { return dsgPrefixes.get(graphName, prefix) ; } 
    
    @Override
    public boolean containsPrefix(String prefix)
    {
        return get(prefix) != null ;
    }
    
    @Override
    public void remove(String prefix) { dsgPrefixes.delete(graphName, prefix) ; }
    
    @Override
    public void clear() {
        List<PrefixEntry> x = Iter.toList(iterator());
        for ( PrefixEntry e : x )
            remove(e.getPrefix());
    }

    @Override
    public boolean isEmpty() {
        return !dsgPrefixes.listGraphNodes().hasNext();
    }

    @Override
    public int size() {
        return (int)Iter.count(dsgPrefixes.get(graphName));
    }

    @Override
    public Iterator<PrefixEntry> iterator() {
        return dsgPrefixes.get(graphName);
    }
    
    @Override
    public Stream<PrefixEntry> stream() {
        return Iter.asStream(dsgPrefixes.get(graphName));
    }
    
//    @Override
//    public void sync() {}
//    
//    @Override
//    public void close() {}
    
    // The default graph : preferred name is the explicitly used name.
    private static final Node dftGraph =  Quad.defaultGraphIRI ;
    // Also seen as:
    private static final Node dftGraph2 = Quad.defaultGraphNodeGenerated ;
}
