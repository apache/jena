/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 * * $Id: Iso639.java,v 1.3 2005-02-21 12:10:58 andy_seaborne Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * Iso639.java
 *
 * Created on July 24, 2001, 11:46 PM
 */

package com.hp.hpl.jena.rdf.arp.lang;

import java.util.HashMap;
import java.util.Map;
/** 
 *Language codes from ISO639-1 and ISO639-2.
 *<p>
   An encapsulation of the registry of ISO639-1 and
 * ISO639-2 codes as found at
 * <a href="http://lcweb.loc.gov/standards/iso639-2/englangn.html">
 * http://lcweb.loc.gov/standards/iso639-2/englangn.html</a>
 * on the 24th July 2001, and dated 12th October 2000.
 *
 *
 * @author jjc
 */

public class Iso639  implements LanguageTagCodes  {
    static final Map all = new HashMap();

    /** Creates new Iso639 */
    private Iso639(String engName,String two,String term,String bib, int classify) {
        name = engName;
        twoCharCode = two;
        terminologyCode = term;
        bibliographicCode = bib;
        classification = classify|LT_ISO639;
        put(two,this);
        put(term,this);
        put(bib,this);
    }
    private Iso639(String engName,String two,String term,String bib) {
        this(engName,two,term,bib,0);
    }
       
/** Look an ISO-639 code up in the list.
 * @param lang The two or three character code to look up.
 * @return The corresponding Iso639 object,
 * or null if not in the list.
 */  
    static public Iso639 find(String lang) {
        Iso639 rslt = (Iso639)all.get(lang);
        if ( rslt == null ) {
            if ( lang != null 
                 && lang.compareTo("qaa") >= 0
                 && lang.compareTo("qtz") <= 0
                 && lang.length()==3 ) {
                  return new Iso639("Reserved for local use",null,
                                          lang,lang,LT_LOCAL_USE);
            }
        }
        return rslt;
    }
    /** The bitwise OR of all applicable values
 * from {@link LanguageTagCodes}.
 * The possibilities are:
 * <ul>
 * <li><CODE>LT_ISO639</CODE></li>
 * <li><CODE>LT_ISO639|LT_MULTIPLE</CODE> i.e. <CODE>mul</CODE></li>
 * <li><CODE>LT_ISO639|LT_UNDETERMINED</CODE> i.e. <CODE>und</CODE></li>
 * <li><CODE>LT_ISO639|LT_LOCAL_USE</CODE> e.g. <CODE>qaa</CODE></li>
 *</ul>
 *
 */   
    public final int classification;
    /** The ISO639-1 two character code.
     */
    public final String twoCharCode;
    /** The ISO639-2 three character Terminology Code.
     */
    public final String terminologyCode;
    /** The ISO639-2 three character Bibliographic Code.
     */
    public final String bibliographicCode;
    /** The name of the language in English.
     */
    public final String name;
    
    static private void put(String key,Iso639 v) {
        if ( key!= null ) {
            Iso639 old = (Iso639)all.put(key,v);
            if ( old != null && old != v ) {
                System.err.println("ISO-639 code '" + key + "' is overloaded.");
            }
        }
    }
    
    static {
        new Iso639("Abkhazian","ab","abk","abk");
        new Iso639("Achinese",null,"ace","ace");
        new Iso639("Acoli",null,"ach","ach");
        new Iso639("Adangme",null,"ada","ada");
        new Iso639("Afar","aa","aar","aar");
        new Iso639("Afrihili",null,"afh","afh");
        new Iso639("Afrikaans","af","afr","afr");
        new Iso639("Afro-Asiatic (Other)",null,"afa","afa");
        new Iso639("Akan",null,"aka","aka");
        new Iso639("Akkadian",null,"akk","akk");
        new Iso639("Albanian","sq","sqi","alb");
        new Iso639("Aleut",null,"ale","ale");
        new Iso639("Algonquian languages",null,"alg","alg");
        new Iso639("Altaic (Other)",null,"tut","tut");
        new Iso639("Amharic","am","amh","amh");
        new Iso639("Apache languages",null,"apa","apa");
        new Iso639("Arabic","ar","ara","ara");
        new Iso639("Aramaic",null,"arc","arc");
        new Iso639("Arapaho",null,"arp","arp");
        new Iso639("Araucanian",null,"arn","arn");
        new Iso639("Arawak",null,"arw","arw");
        new Iso639("Armenian","hy","hye","arm");
        new Iso639("Artificial (Other)",null,"art","art");
        new Iso639("Assamese","as","asm","asm");
        new Iso639("Athapascan languages",null,"ath","ath");
        new Iso639("Australian languages",null,"aus","aus");
        new Iso639("Austronesian (Other)",null,"map","map");
        
        new Iso639("Avaric",null,"ava","ava");
        new Iso639("Avestan","ae","ave","ave");
        new Iso639("Awadhi",null,"awa","awa");
        new Iso639("Aymara","ay","aym","aym");
        new Iso639("Azerbaijani","az","aze","aze");
        new Iso639("Balinese",null,"ban","ban");
        new Iso639("Baltic (Other)",null,"bat","bat");
        new Iso639("Baluchi",null,"bal","bal");
        new Iso639("Bambara",null,"bam","bam");
        new Iso639("Bamileke languages",null,"bai","bai");
        new Iso639("Banda",null,"bad","bad");
        new Iso639("Bantu (Other)",null,"bnt","bnt");
        new Iso639("Basa",null,"bas","bas");
        new Iso639("Bashkir","ba","bak","bak");
        new Iso639("Basque","eu","eus","baq");
        new Iso639("Batak (Indonesia)",null,"btk","btk");
        new Iso639("Beja",null,"bej","bej");
        new Iso639("Belarusian","be","bel","bel");
        new Iso639("Bemba",null,"bem","bem");
        new Iso639("Bengali","bn","ben","ben");
        new Iso639("Berber (Other)",null,"ber","ber");
        new Iso639("Bhojpuri",null,"bho","bho");
        new Iso639("Bihari","bh","bih","bih");
        new Iso639("Bikol",null,"bik","bik");
        new Iso639("Bini",null,"bin","bin");
        new Iso639("Bislama","bi","bis","bis");
        new Iso639("Bosnian","bs","bos","bos");
        new Iso639("Braj",null,"bra","bra");
        new Iso639("Breton","br","bre","bre");
        new Iso639("Buginese",null,"bug","bug");
        new Iso639("Bulgarian","bg","bul","bul");
        new Iso639("Buriat",null,"bua","bua");
        new Iso639("Burmese","my","mya","bur");
        
        
        
        
        
        
        new Iso639("Caddo",null,"cad","cad");
        new Iso639("Carib",null,"car","car");
        new Iso639("Catalan","ca","cat","cat");
        new Iso639("Caucasian (Other)",null,"cau","cau");
        new Iso639("Cebuano",null,"ceb","ceb");
        new Iso639("Celtic (Other)",null,"cel","cel");
        new Iso639("Central American Indian (Other)",null,"cai","cai");
        
        new Iso639("Chagatai",null,"chg","chg");
        new Iso639("Chamic languages",null,"cmc","cmc");
        new Iso639("Chamorro","ch","cha","cha");
        new Iso639("Chechen","ce","che","che");
        new Iso639("Cherokee",null,"chr","chr");
        new Iso639("Cheyenne",null,"chy","chy");
        new Iso639("Chibcha",null,"chb","chb");
        new Iso639("Chichewa; Nyanja","ny","nya","nya");
        new Iso639("Chinese","zh","zho","chi");
        new Iso639("Chinook jargon",null,"chn","chn");
        new Iso639("Chipewyan",null,"chp","chp");
        new Iso639("Choctaw",null,"cho","cho");
        new Iso639("Church Slavic","cu","chu","chu");
        new Iso639("Chuukese",null,"chk","chk");
        new Iso639("Chuvash","cv","chv","chv");
        new Iso639("Coptic",null,"cop","cop");
        new Iso639("Cornish","kw","cor","cor");
        new Iso639("Corsican","co","cos","cos");
        new Iso639("Cree",null,"cre","cre");
        new Iso639("Creek",null,"mus","mus");
        new Iso639("Creoles and pidgins (Other)",null,"crp","crp");
        new Iso639("Creoles and pidgins, English-based (Other)",null,"cpe","cpe");
        
        new Iso639("Creoles and pidgins, French-based (Other)",null,"cpf","cpf");
        
        new Iso639("Creoles and pidgins, Portuguese-based (Other)",null,"cpp","cpp");
        
        new Iso639("Croatian","hr","hrv","scr");
        new Iso639("Cushitic (Other)",null,"cus","cus");
        new Iso639("Czech","cs","ces","cze");
        new Iso639("Dakota",null,"dak","dak");
        new Iso639("Danish","da","dan","dan");
        new Iso639("Dayak",null,"day","day");
        new Iso639("Delaware",null,"del","del");
        new Iso639("Dinka",null,"din","din");
        new Iso639("Divehi",null,"div","div");
        new Iso639("Dogri",null,"doi","doi");
        new Iso639("Dogrib",null,"dgr","dgr");
        new Iso639("Dravidian (Other)",null,"dra","dra");
        new Iso639("Duala",null,"dua","dua");
        new Iso639("Dutch","nl","nld","dut");
        new Iso639("Dutch, Middle (ca. 1050-1350)",null,"dum","dum");
        new Iso639("Dyula",null,"dyu","dyu");
        new Iso639("Dzongkha","dz","dzo","dzo");
        
        
        
        
        
        
        new Iso639("Efik",null,"efi","efi");
        new Iso639("Egyptian (Ancient)",null,"egy","egy");
        new Iso639("Ekajuk",null,"eka","eka");
        new Iso639("Elamite",null,"elx","elx");
        new Iso639("English","en","eng","eng");
        new Iso639("English, Middle (1100-1500)",null,"enm","enm");
        new Iso639("English, Old (ca.450-1100)",null,"ang","ang");
        new Iso639("Esperanto","eo","epo","epo");
        new Iso639("Estonian","et","est","est");
        new Iso639("Ewe",null,"ewe","ewe");
        new Iso639("Ewondo",null,"ewo","ewo");
        new Iso639("Fang",null,"fan","fan");
        new Iso639("Fanti",null,"fat","fat");
        new Iso639("Faroese","fo","fao","fao");
        new Iso639("Fijian","fj","fij","fij");
        new Iso639("Finnish","fi","fin","fin");
        new Iso639("Finno-Ugrian (Other)",null,"fiu","fiu");
        new Iso639("Fon",null,"fon","fon");
        new Iso639("French","fr","fra","fre");
        new Iso639("French, Middle (ca.1400-1600)",null,"frm","frm");
        new Iso639("French, Old (842-ca.1400)",null,"fro","fro");
        new Iso639("Frisian","fy","fry","fry");
        new Iso639("Friulian",null,"fur","fur");
        new Iso639("Fulah",null,"ful","ful");
        
        
        
        
        
        
        new Iso639("Ga",null,"gaa","gaa");
        new Iso639("Gaelic (Scots)","gd","gla","gla");
        new Iso639("Gallegan","gl","glg","glg");
        new Iso639("Ganda",null,"lug","lug");
        new Iso639("Gayo",null,"gay","gay");
        new Iso639("Gbaya",null,"gba","gba");
        new Iso639("Geez",null,"gez","gez");
        new Iso639("Georgian","ka","kat","geo");
        new Iso639("German","de","deu","ger");
        //new Iso639("German, Low; Saxon, Low; Low German; Low Saxon",null,"nds","nds");
        new Iso639("German, Middle High (ca.1050-1500)",null,"gmh","gmh");
        
        new Iso639("German, Old High (ca.750-1050)",null,"goh","goh");
        
        new Iso639("Germanic (Other)",null,"gem","gem");
        new Iso639("Gilbertese",null,"gil","gil");
        new Iso639("Gondi",null,"gon","gon");
        new Iso639("Gorontalo",null,"gor","gor");
        new Iso639("Gothic",null,"got","got");
        new Iso639("Grebo",null,"grb","grb");
        new Iso639("Greek, Ancient (to 1453)",null,"grc","grc");
        new Iso639("Greek, Modern (1453-)","el","ell","gre");
        new Iso639("Guarani","gn","grn","grn");
        new Iso639("Gujarati","gu","guj","guj");
        new Iso639("Gwich´in",null,"gwi","gwi");
        new Iso639("Haida",null,"hai","hai");
        new Iso639("Hausa","ha","hau","hau");
        new Iso639("Hawaiian",null,"haw","haw");
        new Iso639("Hebrew","he","heb","heb");
        new Iso639("Herero","hz","her","her");
        new Iso639("Hiligaynon",null,"hil","hil");
        new Iso639("Himachali",null,"him","him");
        new Iso639("Hindi","hi","hin","hin");
        new Iso639("Hiri Motu","ho","hmo","hmo");
        new Iso639("Hittite",null,"hit","hit");
        new Iso639("Hmong",null,"hmn","hmn");
        new Iso639("Hungarian","hu","hun","hun");
        new Iso639("Hupa",null,"hup","hup");
        
        
        
        
        
        
        new Iso639("Iban",null,"iba","iba");
        new Iso639("Icelandic","is","isl","ice");
        new Iso639("Igbo",null,"ibo","ibo");
        new Iso639("Ijo",null,"ijo","ijo");
        new Iso639("Iloko",null,"ilo","ilo");
        new Iso639("Indic (Other)",null,"inc","inc");
        new Iso639("Indo-European (Other)",null,"ine","ine");
        new Iso639("Indonesian","id","ind","ind");
        new Iso639("Interlingua (International Auxiliary Language Association)","ia","ina","ina");
        
        new Iso639("Interlingue","ie","ile","ile");
        new Iso639("Inuktitut","iu","iku","iku");
        new Iso639("Inupiaq","ik","ipk","ipk");
        new Iso639("Iranian (Other)",null,"ira","ira");
        new Iso639("Irish","ga","gle","gle");
        new Iso639("Irish, Middle (900-1200)",null,"mga","mga");
        new Iso639("Irish, Old (to 900)",null,"sga","sga");
        new Iso639("Iroquoian languages",null,"iro","iro");
        new Iso639("Italian","it","ita","ita");
        new Iso639("Japanese","ja","jpn","jpn");
        new Iso639("Javanese","jw","jaw","jav");
        new Iso639("Judeo-Arabic",null,"jrb","jrb");
        new Iso639("Judeo-Persian",null,"jpr","jpr");
        
        
        
        
        
        
        new Iso639("Kabyle",null,"kab","kab");
        new Iso639("Kachin",null,"kac","kac");
        new Iso639("Kalaallisut","kl","kal","kal");
        new Iso639("Kamba",null,"kam","kam");
        new Iso639("Kannada","kn","kan","kan");
        new Iso639("Kanuri",null,"kau","kau");
        new Iso639("Kara-Kalpak",null,"kaa","kaa");
        new Iso639("Karen",null,"kar","kar");
        new Iso639("Kashmiri","ks","kas","kas");
        new Iso639("Kawi",null,"kaw","kaw");
        new Iso639("Kazakh","kk","kaz","kaz");
        new Iso639("Khasi",null,"kha","kha");
        new Iso639("Khmer","km","khm","khm");
        new Iso639("Khoisan (Other)",null,"khi","khi");
        new Iso639("Khotanese",null,"kho","kho");
        new Iso639("Kikuyu","ki","kik","kik");
        new Iso639("Kimbundu",null,"kmb","kmb");
        new Iso639("Kinyarwanda","rw","kin","kin");
        new Iso639("Kirghiz","ky","kir","kir");
        new Iso639("Komi","kv","kom","kom");
        new Iso639("Kongo",null,"kon","kon");
        new Iso639("Konkani",null,"kok","kok");
        new Iso639("Korean","ko","kor","kor");
        new Iso639("Kosraean",null,"kos","kos");
        new Iso639("Kpelle",null,"kpe","kpe");
        new Iso639("Kru",null,"kro","kro");
        new Iso639("Kuanyama","kj","kua","kua");
        new Iso639("Kumyk",null,"kum","kum");
        new Iso639("Kurdish","ku","kur","kur");
        new Iso639("Kurukh",null,"kru","kru");
        new Iso639("Kutenai",null,"kut","kut");
        new Iso639("Ladino",null,"lad","lad");
        new Iso639("Lahnda",null,"lah","lah");
        new Iso639("Lamba",null,"lam","lam");
        new Iso639("Lao","lo","lao","lao");
        new Iso639("Latin","la","lat","lat");
        new Iso639("Latvian","lv","lav","lav");
        new Iso639("Letzeburgesch","lb","ltz","ltz");
        new Iso639("Lezghian",null,"lez","lez");
        new Iso639("Lingala","ln","lin","lin");
        new Iso639("Lithuanian","lt","lit","lit");
        new Iso639("Low German; Low Saxon; German, Low; Saxon, Low",null,"nds","nds");
        //new Iso639("Low Saxon; Low German; Saxon, Low; German, Low",null,"nds","nds");
        new Iso639("Lozi",null,"loz","loz");
        new Iso639("Luba-Katanga",null,"lub","lub");
        new Iso639("Luba-Lulua",null,"lua","lua");
        new Iso639("Luiseno",null,"lui","lui");
        new Iso639("Lunda",null,"lun","lun");
        new Iso639("Luo (Kenya and Tanzania)",null,"luo","luo");
        new Iso639("Lushai",null,"lus","lus");
        
        
        
        
        
        
        new Iso639("Macedonian","mk","mkd","mac");
        new Iso639("Madurese",null,"mad","mad");
        new Iso639("Magahi",null,"mag","mag");
        new Iso639("Maithili",null,"mai","mai");
        new Iso639("Makasar",null,"mak","mak");
        new Iso639("Malagasy","mg","mlg","mlg");
        new Iso639("Malay","ms","msa","may");
        new Iso639("Malayalam","ml","mal","mal");
        new Iso639("Maltese","mt","mlt","mlt");
        new Iso639("Manchu",null,"mnc","mnc");
        new Iso639("Mandar",null,"mdr","mdr");
        new Iso639("Mandingo",null,"man","man");
        new Iso639("Manipuri",null,"mni","mni");
        new Iso639("Manobo languages",null,"mno","mno");
        new Iso639("Manx","gv","glv","glv");
        new Iso639("Maori","mi","mri","mao");
        new Iso639("Marathi","mr","mar","mar");
        new Iso639("Mari",null,"chm","chm");
        new Iso639("Marshall","mh","mah","mah");
        new Iso639("Marwari",null,"mwr","mwr");
        new Iso639("Masai",null,"mas","mas");
        new Iso639("Mayan languages",null,"myn","myn");
        new Iso639("Mende",null,"men","men");
        new Iso639("Micmac",null,"mic","mic");
        new Iso639("Minangkabau",null,"min","min");
        new Iso639("Miscellaneous languages",null,"mis","mis");
        new Iso639("Mohawk",null,"moh","moh");
        new Iso639("Moldavian","mo","mol","mol");
        new Iso639("Mon-Khmer (Other)",null,"mkh","mkh");
        new Iso639("Mongo",null,"lol","lol");
        new Iso639("Mongolian","mn","mon","mon");
        new Iso639("Mossi",null,"mos","mos");
        new Iso639("Multiple languages",null,"mul","mul",LT_MULTIPLE);
        new Iso639("Munda languages",null,"mun","mun");
        new Iso639("Nahuatl",null,"nah","nah");
        new Iso639("Nauru","na","nau","nau");
        new Iso639("Navajo","nv","nav","nav");
        new Iso639("Ndebele, North","nd","nde","nde");
        new Iso639("Ndebele, South","nr","nbl","nbl");
        new Iso639("Ndonga","ng","ndo","ndo");
        new Iso639("Nepali","ne","nep","nep");
        new Iso639("Newari",null,"new","new");
        new Iso639("Nias",null,"nia","nia");
        new Iso639("Niger-Kordofanian (Other)",null,"nic","nic");
        new Iso639("Nilo-Saharan (Other)",null,"ssa","ssa");
        new Iso639("Niuean",null,"niu","niu");
        new Iso639("Norse, Old",null,"non","non");
        new Iso639("North American Indian(Other)",null,"nai","nai");
        
        new Iso639("Northern Sami","se","sme","sme");
        new Iso639("Norwegian","no","nor","nor");
        new Iso639("Norwegian Bokmål","nb","nob","nob");
        new Iso639("Norwegian Nynorsk","nn","nno","nno");
        new Iso639("Nubian languages",null,"nub","nub");
        new Iso639("Nyamwezi",null,"nym","nym");
       // new Iso639("Nyanja; Chichewa","ny","nya","nya");
        new Iso639("Nyankole",null,"nyn","nyn");
        new Iso639("Nyoro",null,"nyo","nyo");
        new Iso639("Nzima",null,"nzi","nzi");
        
        
        
        
        
        
        new Iso639("Occitan (post 1500); Provençal","oc","oci","oci");
        new Iso639("Ojibwa",null,"oji","oji");
        new Iso639("Oriya","or","ori","ori");
        new Iso639("Oromo","om","orm","orm");
        new Iso639("Osage",null,"osa","osa");
        new Iso639("Ossetian; Ossetic","os","oss","oss");
       // new Iso639("Ossetic; Ossetian","os","oss","oss");
        new Iso639("Otomian languages",null,"oto","oto");
        new Iso639("Pahlavi",null,"pal","pal");
        new Iso639("Palauan",null,"pau","pau");
        new Iso639("Pali","pi","pli","pli");
        new Iso639("Pampanga",null,"pam","pam");
        new Iso639("Pangasinan",null,"pag","pag");
        new Iso639("Panjabi","pa","pan","pan");
        new Iso639("Papiamento",null,"pap","pap");
        new Iso639("Papuan (Other)",null,"paa","paa");
        new Iso639("Persian","fa","fas","per");
        new Iso639("Persian, Old (ca.600-400 B.C.)",null,"peo","peo");
        
        new Iso639("Philippine (Other)",null,"phi","phi");
        new Iso639("Phoenician",null,"phn","phn");
        new Iso639("Pohnpeian",null,"pon","pon");
        new Iso639("Polish","pl","pol","pol");
        new Iso639("Portuguese","pt","por","por");
        new Iso639("Prakrit languages",null,"pra","pra");
       // new Iso639("Provençal; Occitan (post 1500)","oc","oci","oci");
        new Iso639("Provençal, Old (to 1500)",null,"pro","pro");
        new Iso639("Pushto","ps","pus","pus");
        
        
        
        
        
        
        new Iso639("Quechua","qu","que","que");
        new Iso639("Raeto-Romance","rm","roh","roh");
        new Iso639("Rajasthani",null,"raj","raj");
        new Iso639("Rapanui",null,"rap","rap");
        new Iso639("Rarotongan",null,"rar","rar");
        // Reserved for local use	qaa-qtz	qaa-qtz
        new Iso639("Romance (Other)",null,"roa","roa");
        new Iso639("Romanian","ro","ron","rum");
        new Iso639("Romany",null,"rom","rom");
        new Iso639("Rundi","rn","run","run");
        new Iso639("Russian","ru","rus","rus");
        
        
        
        
        
        
        new Iso639("Salishan languages",null,"sal","sal");
        new Iso639("Samaritan Aramaic",null,"sam","sam");
        new Iso639("Sami languages (Other)",null,"smi","smi");
        new Iso639("Samoan","sm","smo","smo");
        new Iso639("Sandawe",null,"sad","sad");
        new Iso639("Sango","sg","sag","sag");
        new Iso639("Sanskrit","sa","san","san");
        new Iso639("Santali",null,"sat","sat");
        new Iso639("Sardinian","sc","srd","srd");
        new Iso639("Sasak",null,"sas","sas");
        //new Iso639("Saxon, Low; German, Low; Low Saxon; Low German",null,"nds","nds");
        new Iso639("Scots",null,"sco","sco");
        new Iso639("Selkup",null,"sel","sel");
        new Iso639("Semitic (Other)",null,"sem","sem");
        new Iso639("Serbian","sr","srp","scc");
        new Iso639("Serer",null,"srr","srr");
        new Iso639("Shan",null,"shn","shn");
        new Iso639("Shona","sn","sna","sna");
        new Iso639("Sidamo",null,"sid","sid");
        new Iso639("Sign languages",null,"sgn","sgn");
        new Iso639("Siksika",null,"bla","bla");
        new Iso639("Sindhi","sd","snd","snd");
        new Iso639("Sinhalese","si","sin","sin");
        new Iso639("Sino-Tibetan (Other)",null,"sit","sit");
        new Iso639("Siouan languages",null,"sio","sio");
        new Iso639("Slave (Athapascan)",null,"den","den");
        new Iso639("Slavic (Other)",null,"sla","sla");
        new Iso639("Slovak","sk","slk","slo");
        new Iso639("Slovenian","sl","slv","slv");
        new Iso639("Sogdian",null,"sog","sog");
        new Iso639("Somali","so","som","som");
        new Iso639("Songhai",null,"son","son");
        new Iso639("Soninke",null,"snk","snk");
        new Iso639("Sorbian languages",null,"wen","wen");
        new Iso639("Sotho, Northern",null,"nso","nso");
        new Iso639("Sotho, Southern","st","sot","sot");
        new Iso639("South American Indian (Other)",null,"sai","sai");
        
        new Iso639("Spanish","es","spa","spa");
        new Iso639("Sukuma",null,"suk","suk");
        new Iso639("Sumerian",null,"sux","sux");
        new Iso639("Sundanese","su","sun","sun");
        new Iso639("Susu",null,"sus","sus");
        new Iso639("Swahili","sw","swa","swa");
        new Iso639("Swati","ss","ssw","ssw");
        new Iso639("Swedish","sv","swe","swe");
        new Iso639("Syriac",null,"syr","syr");
        new Iso639("Tagalog","tl","tgl","tgl");
        new Iso639("Tahitian","ty","tah","tah");
        new Iso639("Tai (Other)",null,"tai","tai");
        new Iso639("Tajik","tg","tgk","tgk");
        new Iso639("Tamashek",null,"tmh","tmh");
        new Iso639("Tamil","ta","tam","tam");
        new Iso639("Tatar","tt","tat","tat");
        new Iso639("Telugu","te","tel","tel");
        new Iso639("Tereno",null,"ter","ter");
        new Iso639("Tetum",null,"tet","tet");
        new Iso639("Thai","th","tha","tha");
        new Iso639("Tibetan","bo","bod","tib");
        new Iso639("Tigre",null,"tig","tig");
        new Iso639("Tigrinya","ti","tir","tir");
        new Iso639("Timne",null,"tem","tem");
        new Iso639("Tiv",null,"tiv","tiv");
        new Iso639("Tlingit",null,"tli","tli");
        new Iso639("Tok Pisin",null,"tpi","tpi");
        new Iso639("Tokelau",null,"tkl","tkl");
        new Iso639("Tonga (Nyasa)",null,"tog","tog");
        new Iso639("Tonga (Tonga Islands)","to","ton","ton");
        new Iso639("Tsimshian",null,"tsi","tsi");
        new Iso639("Tsonga","ts","tso","tso");
        new Iso639("Tswana","tn","tsn","tsn");
        new Iso639("Tumbuka",null,"tum","tum");
        new Iso639("Turkish","tr","tur","tur");
        new Iso639("Turkish, Ottoman (1500-1928)",null,"ota","ota");
        new Iso639("Turkmen","tk","tuk","tuk");
        new Iso639("Tuvalu",null,"tvl","tvl");
        new Iso639("Tuvinian",null,"tyv","tyv");
        new Iso639("Twi","tw","twi","twi");
        
        
        
        
        
        
        new Iso639("Ugaritic",null,"uga","uga");
        new Iso639("Uighur","ug","uig","uig");
        new Iso639("Ukrainian","uk","ukr","ukr");
        new Iso639("Umbundu",null,"umb","umb");
        new Iso639("Undetermined",null,"und","und",LT_UNDETERMINED);
        new Iso639("Urdu","ur","urd","urd");
        new Iso639("Uzbek","uz","uzb","uzb");
        new Iso639("Vai",null,"vai","vai");
        new Iso639("Venda",null,"ven","ven");
        new Iso639("Vietnamese","vi","vie","vie");
        new Iso639("Volapük","vo","vol","vol");
        new Iso639("Votic",null,"vot","vot");
        new Iso639("Wakashan languages",null,"wak","wak");
        new Iso639("Walamo",null,"wal","wal");
        new Iso639("Waray",null,"war","war");
        new Iso639("Washo",null,"was","was");
        new Iso639("Welsh","cy","cym","wel");
        new Iso639("Wolof","wo","wol","wol");
        new Iso639("Xhosa","xh","xho","xho");
        new Iso639("Yakut",null,"sah","sah");
        new Iso639("Yao",null,"yao","yao");
        new Iso639("Yapese",null,"yap","yap");
        new Iso639("Yiddish","yi","yid","yid");
        new Iso639("Yoruba","yo","yor","yor");
        new Iso639("Yupik languages",null,"ypk","ypk");
        new Iso639("Zande",null,"znd","znd");
        new Iso639("Zapotec",null,"zap","zap");
        new Iso639("Zenaga",null,"zen","zen");
        new Iso639("Zhuang","za","zha","zha");
        new Iso639("Zulu","zu","zul","zul");
        new Iso639("Zuni",null,"zun","zun");
        
    }
    
    
}
