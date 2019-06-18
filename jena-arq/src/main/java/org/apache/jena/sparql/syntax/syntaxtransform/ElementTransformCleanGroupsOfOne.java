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

package org.apache.jena.sparql.syntax.syntaxtransform;

import java.util.List ;

import org.apache.jena.sparql.syntax.* ;

/** 
 * Clean a SPARQL and ARQ syntax. This applies after using OpAsQuery.
 * <ul>
 * <li>Unwrap groups of one where they do not matter.
 * <li>Merge adjacent ElementPathBlock</li>
 * </ul>
 * Groups of one do matter for <code>OPTIONAL { { ?s ?p ?o FILTER(?foo) } }</code>.
 */
public class ElementTransformCleanGroupsOfOne extends ElementTransformCopyBase {
    // Improvements: scan group elements to work out for non-reduceable adjacents.
    // These ones may clash with an adjeact one in the group above.
    // ElementTransformCleanGroupsOfOne -> ElementTransformCleanGroups
    
    public ElementTransformCleanGroupsOfOne() {}
    
    @Override
    public Element transform(ElementGroup eltGroup, List<Element> elts) {
        if ( elts.size() != 1 )
            return super.transform(eltGroup, elts) ;
        Element elt = elts.get(0) ;
        if ( ( elt instanceof ElementTriplesBlock ) ||
             ( elt instanceof ElementPathBlock ) ||
             ( elt instanceof ElementFilter ) )
            return super.transform(eltGroup, elts) ;    // No transformation.
        return elt ;
    }

    // The ElementGroup transformation can be too strong.
    // Ensure unions have ElementGroup (for compatibility with parsing;
    // it is safe to have non-ElementGroup in a ElementUnion but it is
    // a different query syntax tree).
    @Override
    public Element transform(ElementUnion eltUnion, List<Element> elts) {
        ElementUnion el2 = new ElementUnion() ;
        for ( int i = 0 ; i < elts.size() ; i++ ) {
            Element el = elts.get(i) ;
            if ( ! ( el instanceof ElementGroup ) ) {
                ElementGroup elg = new ElementGroup() ;
                elg.addElement(el);
                el = elg ;
            }
            el2.addElement(el); 
        }
        return el2 ;
    }
    
    // Special case: If Optional, and the original had a {{}} protected filter, keep {{}}
    // transform/ElementGroup has already run so undo if necessary.
    @Override
    public Element transform(ElementOptional eltOptional, Element transformedElt) {
        // RHS of optional is always an ElementGroup in a normal syntax tree.
        if ( ! ( transformedElt instanceof ElementGroup ) ) {
            // DRY
            ElementGroup protectedElt = new ElementGroup() ;
            protectedElt.addElement(transformedElt);
            transformedElt = protectedElt ;
        }
        
        // Step 1 : does the original eltOptional has a {{}} RHS? 
        Element x = eltOptional.getOptionalElement() ;
        
        if ( ! ( x instanceof ElementGroup ) )
            // No. But it is not possible in written query syntax to have a nongroup as the RHS. 
            return super.transform(eltOptional, transformedElt) ;
        // So far - {}-RHS.
        ElementGroup eGroup = (ElementGroup)x ;
        
        // Is it {{}}?
        //ElementGroup inner = getGroupInGroup(x) ;
        if ( eGroup.size() != 1 )
            return super.transform(eltOptional, transformedElt) ;
        Element inner = eGroup.get(0) ;
        if ( ! ( inner instanceof ElementGroup ) )
            return super.transform(eltOptional, transformedElt) ;
        // Yes - {{}}
        ElementGroup innerGroup = (ElementGroup)inner ;
        // Unbundle multiple levels.
        innerGroup = unwrap(innerGroup) ;
        boolean mustProtect = containsFilter(innerGroup) ;
        
        if ( mustProtect ) {
            // No need to check for {{}} in elt1 as the transform(ElementGroup) will have processed it.
            ElementGroup protectedElt = new ElementGroup() ;
            protectedElt.addElement(transformedElt);
            return new ElementOptional(protectedElt) ;
        } 
        // No need to protect - process as usual.
        return super.transform(eltOptional, transformedElt) ;
    }
    
    private boolean containsFilter(ElementGroup eltGroup) {
        return eltGroup.getElements().stream().anyMatch(el2 ->( el2 instanceof ElementFilter ) ) ;
    }
    
    // Removed layers of groups of one.  Return inner most group.
    private ElementGroup unwrap(ElementGroup eltGroup) {
        if ( eltGroup.size() != 1 )
            return eltGroup ;
        Element el = eltGroup.get(0) ;
        if ( ! ( el instanceof ElementGroup ) )
            return eltGroup ;
        ElementGroup eltGroup2 = (ElementGroup)el ;
        return unwrap(eltGroup2) ; 
    }

}

