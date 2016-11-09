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

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

public class PrefixMapBase implements PrefixMapI {

    static final IRIFactory        factory = IRIFactory.iriImplementation();
    private final PrefixMapStorage prefixes;

    PrefixMapBase(PrefixMapStorage storage) {
        this.prefixes = storage;
    }

    // remove IRI handling at this level
    @Override
    public Map<String, IRI> getMapping() {
        return getMappingCopy();
    }

    // remove IRI handling at this level
    @Override
    public Map<String, IRI> getMappingCopy() {
        return prefixes.stream().collect(Collectors.toMap((e) -> e.getPrefix(), (e) -> factory.create(e.getUri())));
    }

    @Override
    public Map<String, String> getMappingCopyStr() {
        return prefixes.stream().collect(Collectors.toMap((e) -> e.getPrefix(), (e) -> e.getUri()));
    }

    @Override
    public PrefixMapStorage getPrefixMapStorage() {
        return prefixes;
    }

    @Override
    public void add(String prefix, String iriString) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        prefixes.put(prefix, iriString);
    }

    @Override
    public void add(String prefix, IRI iri) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        prefixes.put(prefix, iri.toString());
    }

    @Override
    public void putAll(PrefixMapI pmap) {
        Map<String, IRI> map = pmap.getMapping();
        for ( Entry<String, IRI> e : map.entrySet() )
            add(e.getKey(), e.getValue());
    }

    @Override
    public void delete(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        prefixes.remove(prefix);
    }

    @Override
    public Stream<PrefixEntry> stream() {
        return prefixes.stream();
    }

    @Override
    public void clear() {
        prefixes.clear();
    }

    @Override
    public boolean isEmpty() {
        return prefixes.isEmpty();
    }

    @Override
    public int size() {
        return prefixes.size();
    }

    @Override
    public String get(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        return prefixes.get(prefix);
    }

    @Override
    public boolean containPrefix(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        return prefixes.containsPrefix(prefix);
    }

    @Override
    public String abbreviate(String uriStr) {
        for ( PrefixEntry e : prefixes ) {
            String prefix = e.getPrefix();
            String prefixUri = e.getUri();
            if ( uriStr.startsWith(prefixUri) ) {
                String ln = uriStr.substring(prefixUri.length());
                if ( strSafeFor(ln, '/') && strSafeFor(ln, '#') && strSafeFor(ln, ':') )
                    return prefix + ":" + ln;
            }
        }
        return null;
    }

    private static boolean strSafeFor(String str, char ch) {
        return str.indexOf(ch) == -1;
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        for ( PrefixEntry e : prefixes ) {
            String uriForPrefix = e.getUri();
            if ( uriStr.startsWith(uriForPrefix) )
                return Pair.create(e.getPrefix(), uriStr.substring(uriForPrefix.length()));
        }
        return null;
    }

    @Override
    public String expand(String prefixedName) {
        int i = prefixedName.indexOf(':');
        if ( i < 0 )
            return null;
        return expand(prefixedName.substring(0, i), prefixedName.substring(i + 1));
    }

    @Override
    public String expand(String prefix, String localName) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        String x = prefixes.get(prefix);
        if ( x == null )
            return null;
        return x + localName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        boolean first = true;

        for ( PrefixEntry e : prefixes ) {
            if ( first )
                first = false;
            else
                sb.append(" ,");
            sb.append(e.getPrefix());
            sb.append(":=");
            sb.append(e.getUri());
        }
        sb.append(" }");
        return sb.toString();
    }
}
