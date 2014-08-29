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

package com.hp.hpl.jena.rdfxml.xmlinput.states;

import java.io.FileWriter ;
import java.io.IOException ;
import java.lang.reflect.InvocationTargetException ;
import java.util.* ;

import org.xml.sax.Attributes ;
import org.xml.sax.SAXParseException ;

import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.* ;

/**
 * For each state s, for each element-attribute event e1, - test s, e1 - if s,
 * e1 is not an error + test s, e1; Description; test s,e1, eg:prop; test s, e1,
 * end; for each element-attribute event e2 + test s, e1, e2 if s, e1, e2 is not
 * an error
 */
public class TestData implements ARPErrorNumbers{
    
    private static final URIReference foo = URIReference.createNoChecks("http://foo/");
    private static final URIReference bar = URIReference.createNoChecks("http://bar/");

    static TestHandler xmlHandler = new TestHandler();
//    static {
//        try {
//            xmlHandler.initParse("http://example.org/", "");
//        } catch (SAXParseException e) {
//           e.printStackTrace();
//        }
//        
//    }
    static String dataFile = "testing/arp/state.txt";
    static AbsXMLContext xmlContext;
    
    static { 
        try {
            xmlContext= new XMLBaselessContext(xmlHandler,
                    ERR_RESOLVING_AGAINST_RELATIVE_BASE).withBase(xmlHandler,"http://example.org/base/");
        } catch (SAXParseException e) {
            throw new RuntimeException(e);
        }
    }
    static TestFrame testFrame = new TestFrame(xmlHandler, xmlContext);
    
    static char white[] = { 32, 32, 32, 32, 32 };

    static char black[] = { 97, 98, 99, 100, 101 };

    private static final AttrEvent xmlSpace = new AttrEvent(QName.xml("space"));
    static Event allEvents[] = { 
           new ElementEvent(QName.rdf("li")),
            new ElementEvent(QName.rdf("Description")),
            new ElementEvent("F",QName.rdf("RDF")),
            new ElementEvent(QName.eg("Goo")),
            new AttrEvent(QName.xml("base")),
            new AttrEvent("g", QName.xml("lang"), "en"),
            new AttrEvent(QName.eg("foo")), 
            xmlSpace,
            new AttrEvent("B", QName.rdf("bagID"), "en"),
            new AttrEvent(QName.rdf("about")),
            new AttrEvent("h", QName.rdf("aboutEach"), "en"),
            new AttrEvent("H", QName.rdf("aboutEachPrefix"), "en"),
            new AttrEvent(QName.rdf("ID")), new AttrEvent(QName.rdf("nodeID")),
            new AttrEvent(QName.rdf("resource")),
            new AttrEvent(QName.rdf("type")),
            new AttrEvent(QName.rdf("datatype")),
            new AttrEvent("C", QName.rdf("parseType"), "Collection"),
            new AttrEvent("L", QName.rdf("parseType"), "Literal"),
            new AttrEvent("R", QName.rdf("parseType"), "Resource"),
            new InternalEvent("e", "</end>") {
                @Override
                FrameI apply(FrameI from, Attributes att) throws SAXParseException {
                    from.endElement();
                    return from.getParent();
                }
            }, new InternalEvent("O", "object") {
                @Override
                FrameI apply(FrameI from, Attributes att) {
                    ((WantsObjectFrameI) from).theObject(foo);
                    return from;
                }
            }, new InternalEvent("W", "white") {
                @Override
                FrameI apply(FrameI from, Attributes att) throws SAXParseException {
                    from.characters(white, 0, 5);
                    return from;
                }
            }, new InternalEvent("Q", "'abcde'") {
                @Override
                FrameI apply(FrameI from, Attributes att) throws SAXParseException {
                    from.characters(black, 0, 5);
                    return from;
                }
            }, 
            new InternalEvent("P", "pred-object") {
                @Override
                FrameI apply(FrameI from, Attributes att) {
                    ((HasSubjectFrameI) from).aPredAndObj(foo,bar);
                    return from;
                }
            }, };

    static Map<String, Event> short2Event = new HashMap<>();
    static {
        for ( Event allEvent : allEvents )
        {
            String key = allEvent.oneChar;
            if ( short2Event.get( key ) != null )
            {
                System.err.println( "Duplicate event code: " + key );
            }
            short2Event.put( key, allEvent );
        }
    }
    static Map<Class< ? extends FrameI>, String> state2Name = new HashMap<>();

    static Map<Class<? extends FrameI>, String> state2ShortName = new HashMap<>();

    static Map<String, Class<? extends FrameI>> shortName2State = new HashMap<>();

    static Map<Class<? extends FrameI>, Object[]> state2Args = new HashMap<>();

    static void add(String sh, String nm, Class< ? extends FrameI> f, Object args[]) {
        state2Name.put(f, nm);
        sh = getSimpleName(f);
        if (shortName2State.get(sh) != null) {
            System.err.println("Duplicate: " + sh);
        }
        state2Args.put(f, args);
        shortName2State.put(sh, f);
        state2ShortName.put(f, sh);
    }

    private static String getSimpleName(Class<? extends FrameI> f) {
        return XMLHandler.getSimpleName(f);
    }

    static AttributeLexer ap = new AttributeLexer(testFrame, 0, 0);
    static {
        add("ix", "inner-xml-literal", InnerXMLLiteral.class,
                new Object[] { testFrame, "foo", testFrame.namespaces });
        add("xl", "xml-literal", OuterXMLLiteral.class, new Object[] {
                testFrame, xmlContext });
        add("ip", "vanilla-prop-elt", WantLiteralValueOrDescription.class,
                new Object[] { testFrame, xmlContext });
        add("tl", "typed-literal", WantTypedLiteral.class, new Object[] {
                testFrame, "http://ex/dt", xmlContext });
        add("cl", "collection", RDFCollection.class, new Object[] {
                testFrame, xmlContext });
        add("tp", "top-level", WantTopLevelDescription.class,
                new Object[] { testFrame, ap });
        add("em", "empty-prop-elt", WantEmpty.class, new Object[] {
                testFrame, xmlContext });
        add("de", "inside-Description", WantPropertyElement.class,
                new Object[] { testFrame, xmlContext });
        add("RD", "looking-for-RDF", LookingForRDF.class, new Object[] {
                testFrame, ap });
    }

    int localCount;

    int globalCount;

    private EventList eventList = new EventList();

    public TestData() {
        super();
    }
//    String characters[] = {
//            "G",
//            "G e",
//            "G e G",
//            "Q",
//            "Q G",
//            "Q e",
//            "e",
//            "P",
//            "O",
//        };
    String characters[] = {
        "<eg:Goo>",
        "<eg:Goo> </end>",
        "<eg:Goo> </end> <eg:Goo>",
        "'abcde'",
        "'abcde' <eg:Goo>",
        "'abcde' </end>",
        "</end>",
        "pred-object",
        "object",
    };

    boolean inCharacterize = false;
    void characterize(Class< ? extends FrameI> f){
        inCharacterize = true;
        int sz = eventList.size;
        StringBuffer rslt = new StringBuffer();
        String skip = null;
        eventList.test(f);
        rslt.append(eventListName(f,null));
        rslt.append(" $ " + testInfo(f) + " {");
        if ( eventList.testResult.getClass() != LookingForRDF.class)

            for ( String character : characters )
            {
                if ( skip != null && character.startsWith( skip ) )
                {
                    continue;
                }
                skip = null;
                addEvents( character );
                rslt.append( " " + character + " $ " );
                boolean testV = eventList.test( f );
                rslt.append( testInfo( f ) + " ;" );
                eventList.size = sz;
                if ( !testV )
                {
                    skip = character;
                    continue;
                }
            }
        rslt.append(" }");
        data.add(rslt.toString());
        inCharacterize = false;
    }

    private String eventListName(Class< ? extends FrameI> f, Class< ? extends FrameI> f2) {
        StringBuffer rslt = new StringBuffer();
        rslt.append(stateName(f, f2));
        for (int i=0;i<eventList.size;i++) {
           rslt.append(' ');
           rslt.append(eventList.events[i].oneChar);
        }
        return rslt.toString();
    }

    private String stateName(Class< ? extends FrameI> f, Class< ? extends FrameI> f2) {
        return f==f2?"*":state2ShortName.get(f);
    }

    private void addEvents(String string) {
        String all[] = string.split(" ");
        for ( String anAll : all )
        {
            eventList.add( short2Event.get( anAll ) );
        }
    }

    private String testInfo(Class< ? extends FrameI> f) {
        return 
        eventList.testFailure ? (eventList.testException ? "!" : "?") :
           (stateName(eventList.testResult.getClass(),f) + " " + 
                xmlHandler.info() + " " + testFrame.info());
    }

    static Class<?> tryClasses[] = { FrameI.class, AbsXMLLiteral.class,
            HasSubjectFrameI.class, WantsObjectFrameI.class };

  
    static FrameI create(Class<? extends FrameI> cl) throws InstantiationException, IllegalAccessException, InvocationTargetException {
       FrameI frame = null; 
       Object args[] = state2Args.get(cl);
        Class<?> types[] = new Class<?>[args.length];
        for (int i = 1; i < args.length; i++) {
            types[i] = args[i].getClass();
            if (types[i]==XMLContext.class)
                types[i] = AbsXMLContext.class;
        }
        if (cl == InnerXMLLiteral.class)
            types[2] = Map.class;
        for ( Class<?> tryClass : tryClasses )
        {
            types[0] = tryClass;

            try
            {
                frame = cl.getConstructor( types ).newInstance( args );
                break;
            }
            catch ( NoSuchMethodException e )
            {
                continue;
            }
        }
        return frame;
    }
    
    void expand(Class< ? extends FrameI> f) {
        if (AbsXMLLiteral.class.isAssignableFrom(f))
            return;
        if (randomPurgeXMLAttrs())
            return;
        localCount++;
        globalCount++;
        if (localCount % 20000 == 0)
            stats(f);
        if (!eventList.test(f)) {
            if (!shorterTestFails(f)) 
                data.add(eventListName(f,null)+" $ " + testInfo(f));
            return;
        }
        characterize(f);
        if (eventList.size >= (AbsXMLLiteral.class.isAssignableFrom(f) ? 3 :
            eventList.testResult instanceof LookingForRDF ? 2
                : 8))
            return;
        for ( Event allEvent : allEvents )
        {
            if ( allEvent.isAttribute() )
            {
                Event e = eventList.last();
                if ( !( e.isElement() || ( e.isAttribute() && e.hashCode() < allEvent.hashCode() ) ) )
                {
                    continue;
                }
            }
            else if ( true )
            {
                continue;
            }
            eventList.add( allEvent );
            expand( f );
            eventList.pop();
        }
    }
    
    private Random dice = new Random(23);
    
    private boolean randomPurgeXMLAttrs() {
        int weight = 0;
        eventList.rewind();
        while (eventList.hasNext()) {
            Event e = eventList.next();
            if ( e==xmlSpace)
                weight += 2;
            else if ( e.isAttribute() && ((AttrEvent)e).q.uri.equals(Names.xmlns) )
                weight ++;
        }
        while (weight-- >0)
            if (dice.nextBoolean())
                return true;
        return false;
    }

    private boolean shorterTestFails(Class< ? extends FrameI> f) {
        if (eventList.size <= 2)
            return false;
          for (int i=1;i<eventList.size-1;i++){
              EventList copy = eventList.copy();
              copy.delete(i);
              if (!copy.test(f))
                  return true;
          }
          return false;
    }

    Set<String> data = new TreeSet<>(new Comparator<String>(){
        @Override
        public int compare(String arg1, String arg2) {
            StringBuffer b1 = new StringBuffer(arg1).reverse();
            StringBuffer b2 = new StringBuffer(arg2).reverse();
            return b1.toString().compareTo(b2.toString());
        }}
            );

    void stats(Class< ? extends FrameI> f) {
        if (false)
        System.out.println(state2ShortName.get(f) + ":" + state2Name.get(f)
                + ":" + getSimpleName(f) + "  " + localCount + "/"
                + globalCount);

    }

    void test1() throws IOException {
        Iterator<Class< ? extends FrameI>> it = state2Name.keySet().iterator();
        while (it.hasNext()) {
            Class< ? extends FrameI> f = it.next();
//            System.out.println(state2ShortName.get(f) + ":" + state2Name.get(f)
//                    + ":" + f.getSimpleName());
            localCount = 0;
            for ( Event allEvent : allEvents )
            {
                if ( allEvent.isElement() )
                {
                    eventList.clear();
                    eventList.add( allEvent );
                    expand( f );
                }
            }
            stats(f);
        }
        try ( FileWriter fw = new FileWriter(dataFile) ) {
            Iterator<String> it2 = data.iterator();
            while (it.hasNext()) {
                fw.write(it2.next());
                fw.write('\n');
            }
        }
    }

    static public void main(String args[]) throws IOException {
        long start = System.currentTimeMillis();
        new TestData().test1();
        System.out.println((System.currentTimeMillis()-start) + " ms");
    }

    public static String stateLongName(String sh) {
        return state2Name.get(shortName2State.get(sh));
    }

    public static Class< ? extends FrameI> toState(String sh) {
        return shortName2State.get(sh);
    }

}
