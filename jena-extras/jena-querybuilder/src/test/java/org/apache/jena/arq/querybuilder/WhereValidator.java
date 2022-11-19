/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder;

import java.util.List;

import org.apache.jena.sparql.syntax.*;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/**
 * Class to validate that an element exists in another element structure.
 *
 * The WhereValidator traverses the element tree that it is attempting to match.
 * Once it finds a node that matches the target element an attempt is made to
 * match all elements enclosed by the target. If a match is found the matcher
 * stops and the <code>matching</code> variable will be <code>true</code>. if a
 * match is not the matcher continues to scan for the next candidate until a
 * match is found or all candidates are exhausted.
 *
 * Usage: <pre>
 *  WhereValidator wv = new WhereValidator( target );
 *  query.getQueryPattern().visit( wv );
 *  assertTrue( wv.matching );
 *  </pre>
 */
public class WhereValidator implements ElementVisitor {

    private Element target;
    public boolean matching = false;
    private NodeIsomorphismMap nim;

    /**
     * Constructor.
     *
     * @param target The target element to locate.
     */
    public WhereValidator(Element target) {
        this.target = target;
        this.nim = new NodeIsomorphismMap();
    }

    private void checkMatching(Element el) {
        if (!matching) {
            matching = el.equalTo(target, nim);
        }
    }

    @Override
    public void visit(ElementTriplesBlock el) {
        checkMatching(el);
        return;
    }

    @Override
    public void visit(ElementPathBlock el) {
        checkMatching(el);
        return;
    }

    @Override
    public void visit(ElementFilter el) {
        checkMatching(el);
        return;
    }

    @Override
    public void visit(ElementAssign el) {
        checkMatching(el);
        return;
    }

    @Override
    public void visit(ElementBind el) {
        checkMatching(el);
        return;
    }

    @Override
    public void visit(ElementData el) {
        checkMatching(el);
        return;
    }

    private void checkList(List<Element> lst) {
        for (Element e : lst) {
            e.visit(this);
            if (matching) {
                return;
            }
        }
    }

    @Override
    public void visit(ElementUnion el) {
        checkMatching(el);
        if (!matching) {
            checkList(el.getElements());
        }
        return;
    }

    @Override
    public void visit(ElementLateral el) {
        checkMatching(el);
        if (!matching) {
            el.getLateralElement().visit(this);
        }
    }

    @Override
    public void visit(ElementOptional el) {
        checkMatching(el);
        if (!matching) {
            el.getOptionalElement().visit(this);
        }
    }

    @Override
    public void visit(ElementGroup el) {
        checkMatching(el);
        if (!matching) {
            checkList(el.getElements());
        }
    }

    @Override
    public void visit(ElementDataset el) {
        checkMatching(el);
        if (!matching) {
            el.getElement().visit(this);
        }

    }

    @Override
    public void visit(ElementNamedGraph el) {
        checkMatching(el);
        if (!matching) {
            el.getElement().visit(this);
        }
    }

    @Override
    public void visit(ElementExists el) {
        checkMatching(el);
        if (!matching) {
            el.getElement().visit(this);
        }
    }

    @Override
    public void visit(ElementNotExists el) {
        checkMatching(el);
        if (!matching) {
            el.getElement().visit(this);
        }
    }

    @Override
    public void visit(ElementMinus el) {
        checkMatching(el);
        if (!matching) {
            el.getMinusElement().visit(this);
        }
    }

    @Override
    public void visit(ElementService el) {
        checkMatching(el);
        if (!matching) {
            el.getElement().visit(this);
        }
    }

    @Override
    public void visit(ElementSubQuery el) {
        checkMatching(el);
        if (!matching) {
            el.getQuery().getQueryPattern().visit(this);
        }
    }

}