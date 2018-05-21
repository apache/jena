/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ext.xerces.impl.dv.xs;

import org.apache.jena.ext.xerces.impl.dv.*;
import org.apache.jena.ext.xerces.xs.*;

/**
 * Base class for XSSimpleType wrapper implementations.
 * 
 * {@literal @xerces.internal}
 * 
 * @version $Id: XSSimpleTypeDelegate.java 1024038 2010-10-18 22:06:35Z sandygao $
 */
public class XSSimpleTypeDelegate
    implements XSSimpleType {

    protected final XSSimpleType type;
    
    public XSSimpleTypeDelegate(XSSimpleType type) {
        if (type == null) {
            throw new NullPointerException();
        }
        this.type = type;
    }
    
    public XSSimpleType getWrappedXSSimpleType() {
        return type;
    }

    @Override
    public XSObjectList getAnnotations() {
        return type.getAnnotations();
    }

    @Override
    public boolean getBounded() {
        return type.getBounded();
    }

    @Override
    public short getBuiltInKind() {
        return type.getBuiltInKind();
    }

    @Override
    public short getDefinedFacets() {
        return type.getDefinedFacets();
    }

    @Override
    public XSObjectList getFacets() {
        return type.getFacets();
    }

    @Override
    public XSObject getFacet(int facetType) {
        return type.getFacet(facetType);
    }

    @Override
    public boolean getFinite() {
        return type.getFinite();
    }

    @Override
    public short getFixedFacets() {
        return type.getFixedFacets();
    }

    @Override
    public XSSimpleTypeDefinition getItemType() {
        return type.getItemType();
    }

    @Override
    public StringList getLexicalEnumeration() {
        return type.getLexicalEnumeration();
    }

    @Override
    public String getLexicalFacetValue(short facetName) {
        return type.getLexicalFacetValue(facetName);
    }

    @Override
    public StringList getLexicalPattern() {
        return type.getLexicalPattern();
    }

    @Override
    public XSObjectList getMemberTypes() {
        return type.getMemberTypes();
    }

    @Override
    public XSObjectList getMultiValueFacets() {
        return type.getMultiValueFacets();
    }

    @Override
    public boolean getNumeric() {
        return type.getNumeric();
    }

    @Override
    public short getOrdered() {
        return type.getOrdered();
    }

    @Override
    public XSSimpleTypeDefinition getPrimitiveType() {
        return type.getPrimitiveType();
    }

    @Override
    public short getVariety() {
        return type.getVariety();
    }

    @Override
    public boolean isDefinedFacet(short facetName) {
        return type.isDefinedFacet(facetName);
    }

    @Override
    public boolean isFixedFacet(short facetName) {
        return type.isFixedFacet(facetName);
    }

    @Override
    public boolean derivedFrom(String namespace, String name, short derivationMethod) {
        return type.derivedFrom(namespace, name, derivationMethod);
    }

    @Override
    public boolean derivedFromType(XSTypeDefinition ancestorType, short derivationMethod) {
        return type.derivedFromType(ancestorType, derivationMethod);
    }

    @Override
    public boolean getAnonymous() {
        return type.getAnonymous();
    }

    @Override
    public XSTypeDefinition getBaseType() {
        return type.getBaseType();
    }

    @Override
    public short getFinal() {
        return type.getFinal();
    }

    @Override
    public short getTypeCategory() {
        return type.getTypeCategory();
    }

    @Override
    public boolean isFinal(short restriction) {
        return type.isFinal(restriction);
    }

    @Override
    public String getName() {
        return type.getName();
    }

    @Override
    public String getNamespace() {
        return type.getNamespace();
    }

    @Override
    public XSNamespaceItem getNamespaceItem() {
        return type.getNamespaceItem();
    }

    @Override
    public short getType() {
        return type.getType();
    }

    @Override
    public void applyFacets(XSFacets facets, short presentFacet, short fixedFacet, ValidationContext context) 
        throws InvalidDatatypeFacetException {
        type.applyFacets(facets, presentFacet, fixedFacet, context);
    }

    @Override
    public short getPrimitiveKind() {
        return type.getPrimitiveKind();
    }

    @Override
    public short getWhitespace() throws DatatypeException {
        return type.getWhitespace();
    }

    @Override
    public boolean isEqual(Object value1, Object value2) {
        return type.isEqual(value1, value2);
    }

    @Override
    public boolean isIDType() {
        return type.isIDType();
    }

    @Override
    public void validate(ValidationContext context, ValidatedInfo validatedInfo) 
        throws InvalidDatatypeValueException {
        type.validate(context, validatedInfo);
    }

    @Override
    public Object validate(String content, ValidationContext context, ValidatedInfo validatedInfo) 
        throws InvalidDatatypeValueException {
        return type.validate(content, context, validatedInfo);
    }

    @Override
    public Object validate(Object content, ValidationContext context, ValidatedInfo validatedInfo) 
        throws InvalidDatatypeValueException {
        return type.validate(content, context, validatedInfo);
    }
    
    @Override
    public String toString() {
        return type.toString();
    }
}
