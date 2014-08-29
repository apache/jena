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

package com.hp.hpl.jena.rdfxml.xmlinput.lang;

import java.util.HashMap;
import java.util.Map;
/** 
 *Language codes from ISO639-1 and ISO639-2.
 *<p>
   An encapsulation of the registry of ISO639-1 and
 * ISO639-2 codes as found at
 * < a href="http://www.loc.gov/standards/iso639-2/php/English_list.php">
 * http://www.loc.gov/standards/iso639-2/php/English_list.php</a>
 * on 23rd July, 2009
 */

public class Iso639  implements LanguageTagCodes  {
    /* First version: 
     * <a href="http://lcweb.loc.gov/standards/iso639-2/englangn.html">
     * http://lcweb.loc.gov/standards/iso639-2/englangn.html</a>
     * on the 24th July 2001, and dated 12th October 2000.
     */
    static final Map<String, Iso639> all = new HashMap<>();

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
        Iso639 rslt = all.get(lang);
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
            Iso639 old = all.put(key,v);
            if ( old != null && old != v ) {
                System.err.println("ISO-639 code '" + key + "' is overloaded.");
            }
        }
    }
    
    static {
        new Iso639("Abkhazian","ab","abk","abk") ;
        new Iso639("Achinese",null,"ace","ace") ;
        new Iso639("Acoli",null,"ach","ach") ;
        new Iso639("Adangme",null,"ada","ada") ;
        new Iso639("Adygei",null,"ady","ady") ;
        new Iso639("Afar","aa","aar","aar") ;
        new Iso639("Afrihili",null,"afh","afh") ;
        new Iso639("Afrikaans","af","afr","afr") ;
        new Iso639("Afro-Asiatic languages",null,"afa","afa") ;
        new Iso639("Ainu",null,"ain","ain") ;
        new Iso639("Akan","ak","aka","aka") ;
        new Iso639("Akkadian",null,"akk","akk") ;
        new Iso639("Albanian","sq","alb","sqi") ;
        new Iso639("Alemannic",null,"gsw","gsw") ;
        new Iso639("Aleut",null,"ale","ale") ;
        new Iso639("Algonquian languages",null,"alg","alg") ;
        new Iso639("Altaic languages",null,"tut","tut") ;
        new Iso639("Amharic","am","amh","amh") ;
        new Iso639("Angika",null,"anp","anp") ;
        new Iso639("Apache languages",null,"apa","apa") ;
        new Iso639("Arabic","ar","ara","ara") ;
        new Iso639("Aragonese","an","arg","arg") ;
        new Iso639("Arapaho",null,"arp","arp") ;
        new Iso639("Arawak",null,"arw","arw") ;
        new Iso639("Armenian","hy","arm","hye") ;
        new Iso639("Aromanian",null,"rup","rup") ;
        new Iso639("Artificial languages",null,"art","art") ;
        new Iso639("Assamese","as","asm","asm") ;
        new Iso639("Asturian",null,"ast","ast") ;
        new Iso639("Athapascan languages",null,"ath","ath") ;
        new Iso639("Australian languages",null,"aus","aus") ;
        new Iso639("Austronesian languages",null,"map","map") ;
        new Iso639("Avaric","av","ava","ava") ;
        new Iso639("Avestan","ae","ave","ave") ;
        new Iso639("Awadhi",null,"awa","awa") ;
        new Iso639("Aymara","ay","aym","aym") ;
        new Iso639("Azerbaijani","az","aze","aze") ;

        new Iso639("Balinese",null,"ban","ban") ;
        new Iso639("Baltic languages",null,"bat","bat") ;
        new Iso639("Baluchi",null,"bal","bal") ;
        new Iso639("Bambara","bm","bam","bam") ;
        new Iso639("Bamileke languages",null,"bai","bai") ;
        new Iso639("Banda languages",null,"bad","bad") ;
        new Iso639("Bantu languages",null,"bnt","bnt") ;
        new Iso639("Basa",null,"bas","bas") ;
        new Iso639("Bashkir","ba","bak","bak") ;
        new Iso639("Basque","eu","baq","eus") ;
        new Iso639("Batak languages",null,"btk","btk") ;
        new Iso639("Bedawiyet",null,"bej","bej") ;
        new Iso639("Belarusian","be","bel","bel") ;
        new Iso639("Bemba",null,"bem","bem") ;
        new Iso639("Bengali","bn","ben","ben") ;
        new Iso639("Berber languages",null,"ber","ber") ;
        new Iso639("Bhojpuri",null,"bho","bho") ;
        new Iso639("Bihari","bh","bih","bih") ;
        new Iso639("Bikol",null,"bik","bik") ;
        new Iso639("Bilin",null,"byn","byn") ;
        new Iso639("Bini",null,"bin","bin") ;
        new Iso639("Bislama","bi","bis","bis") ;
        new Iso639("Bliss",null,"zbl","zbl") ;
        new Iso639("Bokmål, Norwegian","nb","nob","nob") ;
        new Iso639("Bosnian","bs","bos","bos") ;
        new Iso639("Braj",null,"bra","bra") ;
        new Iso639("Breton","br","bre","bre") ;
        new Iso639("Buginese",null,"bug","bug") ;
        new Iso639("Bulgarian","bg","bul","bul") ;
        new Iso639("Buriat",null,"bua","bua") ;
        new Iso639("Burmese","my","bur","mya") ;

        new Iso639("Caddo",null,"cad","cad") ;
        new Iso639("Castilian","es","spa","spa") ;
        new Iso639("Catalan","ca","cat","cat") ;
        new Iso639("Caucasian languages",null,"cau","cau") ;
        new Iso639("Cebuano",null,"ceb","ceb") ;
        new Iso639("Celtic languages",null,"cel","cel") ;
        new Iso639("Central American Indian languages",null,"cai","cai") ;
        new Iso639("Central Khmer","km","khm","khm") ;
        new Iso639("Chagatai",null,"chg","chg") ;
        new Iso639("Chamic languages",null,"cmc","cmc") ;
        new Iso639("Chamorro","ch","cha","cha") ;
        new Iso639("Chechen","ce","che","che") ;
        new Iso639("Cherokee",null,"chr","chr") ;
        new Iso639("Chewa","ny","nya","nya") ;
        new Iso639("Cheyenne",null,"chy","chy") ;
        new Iso639("Chibcha",null,"chb","chb") ;
        new Iso639("Chinese","zh","chi","zho") ;
        new Iso639("Chinook jargon",null,"chn","chn") ;
        new Iso639("Chipewyan",null,"chp","chp") ;
        new Iso639("Choctaw",null,"cho","cho") ;
        new Iso639("Chuang","za","zha","zha") ;
        new Iso639("Church Slavic","cu","chu","chu") ;
        new Iso639("Chuukese",null,"chk","chk") ;
        new Iso639("Chuvash","cv","chv","chv") ;
        new Iso639("Classical Nepal Bhasa",null,"nwc","nwc") ;
        new Iso639("Classical Syriac",null,"syc","syc") ;
        new Iso639("Cook Islands Maori",null,"rar","rar") ;
        new Iso639("Coptic",null,"cop","cop") ;
        new Iso639("Cornish","kw","cor","cor") ;
        new Iso639("Corsican","co","cos","cos") ;
        new Iso639("Cree","cr","cre","cre") ;
        new Iso639("Creek",null,"mus","mus") ;
        new Iso639("Creoles and pidgins",null,"crp","crp") ;
        new Iso639("Creoles and pidgins, English based",null,"cpe","cpe") ;
        new Iso639("Creoles and pidgins, French-based",null,"cpf","cpf") ;
        new Iso639("Creoles and pidgins, Portuguese-based",null,"cpp","cpp") ;
        new Iso639("Crimean Tatar",null,"crh","crh") ;
        new Iso639("Croatian","hr","hrv","hrv") ;
        new Iso639("Cushitic languages",null,"cus","cus") ;
        new Iso639("Czech","cs","cze","ces") ;

        new Iso639("Dakota",null,"dak","dak") ;
        new Iso639("Danish","da","dan","dan") ;
        new Iso639("Dargwa",null,"dar","dar") ;
        new Iso639("Delaware",null,"del","del") ;
        new Iso639("Dhivehi","dv","div","div") ;
        new Iso639("Dimili",null,"zza","zza") ;
        new Iso639("Dinka",null,"din","din") ;
        new Iso639("Dogri",null,"doi","doi") ;
        new Iso639("Dogrib",null,"dgr","dgr") ;
        new Iso639("Dravidian languages",null,"dra","dra") ;
        new Iso639("Duala",null,"dua","dua") ;
        new Iso639("Dutch","nl","dut","nld") ;
        new Iso639("Dutch, Middle (ca.1050-1350)",null,"dum","dum") ;
        new Iso639("Dyula",null,"dyu","dyu") ;
        new Iso639("Dzongkha","dz","dzo","dzo") ;

        new Iso639("Eastern Frisian",null,"frs","frs") ;
        new Iso639("Efik",null,"efi","efi") ;
        new Iso639("Egyptian (Ancient)",null,"egy","egy") ;
        new Iso639("Ekajuk",null,"eka","eka") ;
        new Iso639("Elamite",null,"elx","elx") ;
        new Iso639("English","en","eng","eng") ;
        new Iso639("English, Middle (1100-1500)",null,"enm","enm") ;
        new Iso639("English, Old (ca.450-1100)",null,"ang","ang") ;
        new Iso639("Erzya",null,"myv","myv") ;
        new Iso639("Esperanto","eo","epo","epo") ;
        new Iso639("Estonian","et","est","est") ;
        new Iso639("Ewe","ee","ewe","ewe") ;
        new Iso639("Ewondo",null,"ewo","ewo") ;

        new Iso639("Fang",null,"fan","fan") ;
        new Iso639("Fanti",null,"fat","fat") ;
        new Iso639("Faroese","fo","fao","fao") ;
        new Iso639("Fijian","fj","fij","fij") ;
        new Iso639("Filipino",null,"fil","fil") ;
        new Iso639("Finnish","fi","fin","fin") ;
        new Iso639("Finno-Ugrian languages",null,"fiu","fiu") ;
        new Iso639("Fon",null,"fon","fon") ;
        new Iso639("French","fr","fre","fra") ;
        new Iso639("French, Middle (ca.1400-1600)",null,"frm","frm") ;
        new Iso639("French, Old (842-ca.1400)",null,"fro","fro") ;
        new Iso639("Friulian",null,"fur","fur") ;
        new Iso639("Fulah","ff","ful","ful") ;

        new Iso639("Ga",null,"gaa","gaa") ;
        new Iso639("Gaelic","gd","gla","gla") ;
        new Iso639("Galibi Carib",null,"car","car") ;
        new Iso639("Galician","gl","glg","glg") ;
        new Iso639("Ganda","lg","lug","lug") ;
        new Iso639("Gayo",null,"gay","gay") ;
        new Iso639("Gbaya",null,"gba","gba") ;
        new Iso639("Geez",null,"gez","gez") ;
        new Iso639("Georgian","ka","geo","kat") ;
        new Iso639("German","de","ger","deu") ;
        new Iso639("German, Low",null,"nds","nds") ;
        new Iso639("German, Middle High (ca.1050-1500)",null,"gmh","gmh") ;
        new Iso639("German, Old High (ca.750-1050)",null,"goh","goh") ;
        new Iso639("Germanic languages",null,"gem","gem") ;
        new Iso639("Gikuyu","ki","kik","kik") ;
        new Iso639("Gilbertese",null,"gil","gil") ;
        new Iso639("Gondi",null,"gon","gon") ;
        new Iso639("Gorontalo",null,"gor","gor") ;
        new Iso639("Gothic",null,"got","got") ;
        new Iso639("Grebo",null,"grb","grb") ;
        new Iso639("Greek, Ancient (to 1453)",null,"grc","grc") ;
        new Iso639("Greek, Modern (1453-)","el","gre","ell") ;
        new Iso639("Greenlandic","kl","kal","kal") ;
        new Iso639("Guarani","gn","grn","grn") ;
        new Iso639("Gujarati","gu","guj","guj") ;
        new Iso639("Gwich'in",null,"gwi","gwi") ;

        new Iso639("Haida",null,"hai","hai") ;
        new Iso639("Haitian","ht","hat","hat") ;
        new Iso639("Hausa","ha","hau","hau") ;
        new Iso639("Hawaiian",null,"haw","haw") ;
        new Iso639("Hebrew","he","heb","heb") ;
        new Iso639("Herero","hz","her","her") ;
        new Iso639("Hiligaynon",null,"hil","hil") ;
        new Iso639("Himachali",null,"him","him") ;
        new Iso639("Hindi","hi","hin","hin") ;
        new Iso639("Hiri Motu","ho","hmo","hmo") ;
        new Iso639("Hittite",null,"hit","hit") ;
        new Iso639("Hmong",null,"hmn","hmn") ;
        new Iso639("Hungarian","hu","hun","hun") ;
        new Iso639("Hupa",null,"hup","hup") ;

        new Iso639("Iban",null,"iba","iba") ;
        new Iso639("Icelandic","is","ice","isl") ;
        new Iso639("Ido","io","ido","ido") ;
        new Iso639("Igbo","ig","ibo","ibo") ;
        new Iso639("Ijo languages",null,"ijo","ijo") ;
        new Iso639("Iloko",null,"ilo","ilo") ;
        new Iso639("Imperial Aramaic (700-300 BCE)",null,"arc","arc") ;
        new Iso639("Inari Sami",null,"smn","smn") ;
        new Iso639("Indic languages",null,"inc","inc") ;
        new Iso639("Indo-European languages",null,"ine","ine") ;
        new Iso639("Indonesian","id","ind","ind") ;
        new Iso639("Ingush",null,"inh","inh") ;
        new Iso639("Interlingua (International Auxiliary Language Association)","ia","ina","ina") ;
        new Iso639("Interlingue","ie","ile","ile") ;
        new Iso639("Inuktitut","iu","iku","iku") ;
        new Iso639("Inupiaq","ik","ipk","ipk") ;
        new Iso639("Iranian languages",null,"ira","ira") ;
        new Iso639("Irish","ga","gle","gle") ;
        new Iso639("Irish, Middle (900-1200)",null,"mga","mga") ;
        new Iso639("Irish, Old (to 900)",null,"sga","sga") ;
        new Iso639("Iroquoian languages",null,"iro","iro") ;
        new Iso639("Italian","it","ita","ita") ;

        new Iso639("Japanese","ja","jpn","jpn") ;
        new Iso639("Javanese","jv","jav","jav") ;
        new Iso639("Jingpho",null,"kac","kac") ;
        new Iso639("Judeo-Arabic",null,"jrb","jrb") ;
        new Iso639("Judeo-Persian",null,"jpr","jpr") ;

        new Iso639("Kabardian",null,"kbd","kbd") ;
        new Iso639("Kabyle",null,"kab","kab") ;
        new Iso639("Kalmyk",null,"xal","xal") ;
        new Iso639("Kamba",null,"kam","kam") ;
        new Iso639("Kannada","kn","kan","kan") ;
        new Iso639("Kanuri","kr","kau","kau") ;
        new Iso639("Kapampangan",null,"pam","pam") ;
        new Iso639("Kara-Kalpak",null,"kaa","kaa") ;
        new Iso639("Karachay-Balkar",null,"krc","krc") ;
        new Iso639("Karelian",null,"krl","krl") ;
        new Iso639("Karen languages",null,"kar","kar") ;
        new Iso639("Kashmiri","ks","kas","kas") ;
        new Iso639("Kashubian",null,"csb","csb") ;
        new Iso639("Kawi",null,"kaw","kaw") ;
        new Iso639("Kazakh","kk","kaz","kaz") ;
        new Iso639("Khasi",null,"kha","kha") ;
        new Iso639("Khoisan languages",null,"khi","khi") ;
        new Iso639("Khotanese",null,"kho","kho") ;
        new Iso639("Kimbundu",null,"kmb","kmb") ;
        new Iso639("Kinyarwanda","rw","kin","kin") ;
        new Iso639("Kirghiz","ky","kir","kir") ;
        new Iso639("Klingon",null,"tlh","tlh") ;
        new Iso639("Komi","kv","kom","kom") ;
        new Iso639("Kongo","kg","kon","kon") ;
        new Iso639("Konkani",null,"kok","kok") ;
        new Iso639("Korean","ko","kor","kor") ;
        new Iso639("Kosraean",null,"kos","kos") ;
        new Iso639("Kpelle",null,"kpe","kpe") ;
        new Iso639("Kru languages",null,"kro","kro") ;
        new Iso639("Kuanyama","kj","kua","kua") ;
        new Iso639("Kumyk",null,"kum","kum") ;
        new Iso639("Kurdish","ku","kur","kur") ;
        new Iso639("Kurukh",null,"kru","kru") ;
        new Iso639("Kutenai",null,"kut","kut") ;

        new Iso639("Ladino",null,"lad","lad") ;
        new Iso639("Lahnda",null,"lah","lah") ;
        new Iso639("Lamba",null,"lam","lam") ;
        new Iso639("Land Dayak languages",null,"day","day") ;
        new Iso639("Lao","lo","lao","lao") ;
        new Iso639("Latin","la","lat","lat") ;
        new Iso639("Latvian","lv","lav","lav") ;
        new Iso639("Letzeburgesch","lb","ltz","ltz") ;
        new Iso639("Lezghian",null,"lez","lez") ;
        new Iso639("Limburgan","li","lim","lim") ;
        new Iso639("Lingala","ln","lin","lin") ;
        new Iso639("Lithuanian","lt","lit","lit") ;
        new Iso639("Lojban",null,"jbo","jbo") ;
        new Iso639("Lower Sorbian",null,"dsb","dsb") ;
        new Iso639("Lozi",null,"loz","loz") ;
        new Iso639("Luba-Katanga","lu","lub","lub") ;
        new Iso639("Luba-Lulua",null,"lua","lua") ;
        new Iso639("Luiseno",null,"lui","lui") ;
        new Iso639("Lule Sami",null,"smj","smj") ;
        new Iso639("Lunda",null,"lun","lun") ;
        new Iso639("Luo (Kenya and Tanzania)",null,"luo","luo") ;
        new Iso639("Lushai",null,"lus","lus") ;

        new Iso639("Macedonian","mk","mac","mkd") ;
        new Iso639("Madurese",null,"mad","mad") ;
        new Iso639("Magahi",null,"mag","mag") ;
        new Iso639("Maithili",null,"mai","mai") ;
        new Iso639("Makasar",null,"mak","mak") ;
        new Iso639("Malagasy","mg","mlg","mlg") ;
        new Iso639("Malay","ms","may","msa") ;
        new Iso639("Malayalam","ml","mal","mal") ;
        new Iso639("Maltese","mt","mlt","mlt") ;
        new Iso639("Manchu",null,"mnc","mnc") ;
        new Iso639("Mandar",null,"mdr","mdr") ;
        new Iso639("Mandingo",null,"man","man") ;
        new Iso639("Manipuri",null,"mni","mni") ;
        new Iso639("Manobo languages",null,"mno","mno") ;
        new Iso639("Manx","gv","glv","glv") ;
        new Iso639("Maori","mi","mao","mri") ;
        new Iso639("Mapuche",null,"arn","arn") ;
        new Iso639("Marathi","mr","mar","mar") ;
        new Iso639("Mari",null,"chm","chm") ;
        new Iso639("Marshallese","mh","mah","mah") ;
        new Iso639("Marwari",null,"mwr","mwr") ;
        new Iso639("Masai",null,"mas","mas") ;
        new Iso639("Mayan languages",null,"myn","myn") ;
        new Iso639("Mende",null,"men","men") ;
        new Iso639("Mi'kmaq",null,"mic","mic") ;
        new Iso639("Minangkabau",null,"min","min") ;
        new Iso639("Mirandese",null,"mwl","mwl") ;
        new Iso639("Mohawk",null,"moh","moh") ;
        new Iso639("Moksha",null,"mdf","mdf") ;
        new Iso639("Moldavian","ro","rum","ron") ;
        new Iso639("Mon-Khmer languages",null,"mkh","mkh") ;
        new Iso639("Mongo",null,"lol","lol") ;
        new Iso639("Mongolian","mn","mon","mon") ;
        new Iso639("Mossi",null,"mos","mos") ;
        new Iso639("Multiple languages",null,"mul","mul") ;
        new Iso639("Munda languages",null,"mun","mun") ;

        new Iso639("N'Ko",null,"nqo","nqo") ;
        new Iso639("Nahuatl languages",null,"nah","nah") ;
        new Iso639("Nauru","na","nau","nau") ;
        new Iso639("Navaho","nv","nav","nav") ;
        new Iso639("Ndebele, North","nd","nde","nde") ;
        new Iso639("Ndebele, South","nr","nbl","nbl") ;
        new Iso639("Ndonga","ng","ndo","ndo") ;
        new Iso639("Neapolitan",null,"nap","nap") ;
        new Iso639("Nepal Bhasa",null,"new","new") ;
        new Iso639("Nepali","ne","nep","nep") ;
        new Iso639("Nias",null,"nia","nia") ;
        new Iso639("Niger-Kordofanian languages",null,"nic","nic") ;
        new Iso639("Nilo-Saharan languages",null,"ssa","ssa") ;
        new Iso639("Niuean",null,"niu","niu") ;
        new Iso639("No linguistic content",null,"zxx","zxx") ;
        new Iso639("Nogai",null,"nog","nog") ;
        new Iso639("Norse, Old",null,"non","non") ;
        new Iso639("North American Indian languages",null,"nai","nai") ;
        new Iso639("Northern Frisian",null,"frr","frr") ;
        new Iso639("Northern Sami","se","sme","sme") ;
        new Iso639("Northern Sotho",null,"nso","nso") ;
        new Iso639("Norwegian","no","nor","nor") ;
        new Iso639("Norwegian Nynorsk","nn","nno","nno") ;
        new Iso639("Nubian languages",null,"nub","nub") ;
        new Iso639("Nuosu","ii","iii","iii") ;
        new Iso639("Nyamwezi",null,"nym","nym") ;
        new Iso639("Nyankole",null,"nyn","nyn") ;
        new Iso639("Nyoro",null,"nyo","nyo") ;
        new Iso639("Nzima",null,"nzi","nzi") ;

        new Iso639("Occitan (post 1500)","oc","oci","oci") ;
        new Iso639("Occitan, Old (to 1500)",null,"pro","pro") ;
        new Iso639("Ojibwa","oj","oji","oji") ;
        new Iso639("Oriya","or","ori","ori") ;
        new Iso639("Oromo","om","orm","orm") ;
        new Iso639("Osage",null,"osa","osa") ;
        new Iso639("Ossetian","os","oss","oss") ;
        new Iso639("Otomian languages",null,"oto","oto") ;

        new Iso639("Pahlavi",null,"pal","pal") ;
        new Iso639("Palauan",null,"pau","pau") ;
        new Iso639("Pali","pi","pli","pli") ;
        new Iso639("Pangasinan",null,"pag","pag") ;
        new Iso639("Panjabi","pa","pan","pan") ;
        new Iso639("Papiamento",null,"pap","pap") ;
        new Iso639("Papuan languages",null,"paa","paa") ;
        new Iso639("Pashto","ps","pus","pus") ;
        new Iso639("Persian","fa","per","fas") ;
        new Iso639("Persian, Old (ca.600-400 B.C.)",null,"peo","peo") ;
        new Iso639("Philippine languages",null,"phi","phi") ;
        new Iso639("Phoenician",null,"phn","phn") ;
        new Iso639("Pohnpeian",null,"pon","pon") ;
        new Iso639("Polish","pl","pol","pol") ;
        new Iso639("Portuguese","pt","por","por") ;
        new Iso639("Prakrit languages",null,"pra","pra") ;

        new Iso639("Quechua","qu","que","que") ;

        new Iso639("Rajasthani",null,"raj","raj") ;
        new Iso639("Rapanui",null,"rap","rap") ;
        new Iso639("Reserved for local use",null,"qaa-qtz","qaa-qtz") ;
        new Iso639("Romance languages",null,"roa","roa") ;
        new Iso639("Romansh","rm","roh","roh") ;
        new Iso639("Romany",null,"rom","rom") ;
        new Iso639("Rundi","rn","run","run") ;
        new Iso639("Russian","ru","rus","rus") ;

        new Iso639("Salishan languages",null,"sal","sal") ;
        new Iso639("Samaritan Aramaic",null,"sam","sam") ;
        new Iso639("Sami languages",null,"smi","smi") ;
        new Iso639("Samoan","sm","smo","smo") ;
        new Iso639("Sandawe",null,"sad","sad") ;
        new Iso639("Sango","sg","sag","sag") ;
        new Iso639("Sanskrit","sa","san","san") ;
        new Iso639("Santali",null,"sat","sat") ;
        new Iso639("Sardinian","sc","srd","srd") ;
        new Iso639("Sasak",null,"sas","sas") ;
        new Iso639("Scots",null,"sco","sco") ;
        new Iso639("Selkup",null,"sel","sel") ;
        new Iso639("Semitic languages",null,"sem","sem") ;
        new Iso639("Serbian","sr","srp","srp") ;
        new Iso639("Serer",null,"srr","srr") ;
        new Iso639("Shan",null,"shn","shn") ;
        new Iso639("Shona","sn","sna","sna") ;
        new Iso639("Sicilian",null,"scn","scn") ;
        new Iso639("Sidamo",null,"sid","sid") ;
        new Iso639("Sign Languages",null,"sgn","sgn") ;
        new Iso639("Siksika",null,"bla","bla") ;
        new Iso639("Sindhi","sd","snd","snd") ;
        new Iso639("Sinhala","si","sin","sin") ;
        new Iso639("Sino-Tibetan languages",null,"sit","sit") ;
        new Iso639("Siouan languages",null,"sio","sio") ;
        new Iso639("Skolt Sami",null,"sms","sms") ;
        new Iso639("Slave (Athapascan)",null,"den","den") ;
        new Iso639("Slavic languages",null,"sla","sla") ;
        new Iso639("Slovak","sk","slo","slk") ;
        new Iso639("Slovenian","sl","slv","slv") ;
        new Iso639("Sogdian",null,"sog","sog") ;
        new Iso639("Somali","so","som","som") ;
        new Iso639("Songhai languages",null,"son","son") ;
        new Iso639("Soninke",null,"snk","snk") ;
        new Iso639("Sorbian languages",null,"wen","wen") ;
        new Iso639("Sotho, Southern","st","sot","sot") ;
        new Iso639("South American Indian languages",null,"sai","sai") ;
        new Iso639("Southern Altai",null,"alt","alt") ;
        new Iso639("Southern Sami",null,"sma","sma") ;
        new Iso639("Sranan Tongo",null,"srn","srn") ;
        new Iso639("Sukuma",null,"suk","suk") ;
        new Iso639("Sumerian",null,"sux","sux") ;
        new Iso639("Sundanese","su","sun","sun") ;
        new Iso639("Susu",null,"sus","sus") ;
        new Iso639("Swahili","sw","swa","swa") ;
        new Iso639("Swati","ss","ssw","ssw") ;
        new Iso639("Swedish","sv","swe","swe") ;
        new Iso639("Syriac",null,"syr","syr") ;

        new Iso639("Tagalog","tl","tgl","tgl") ;
        new Iso639("Tahitian","ty","tah","tah") ;
        new Iso639("Tai languages",null,"tai","tai") ;
        new Iso639("Tajik","tg","tgk","tgk") ;
        new Iso639("Tamashek",null,"tmh","tmh") ;
        new Iso639("Tamil","ta","tam","tam") ;
        new Iso639("Tatar","tt","tat","tat") ;
        new Iso639("Telugu","te","tel","tel") ;
        new Iso639("Tereno",null,"ter","ter") ;
        new Iso639("Tetum",null,"tet","tet") ;
        new Iso639("Thai","th","tha","tha") ;
        new Iso639("Tibetan","bo","tib","bod") ;
        new Iso639("Tigre",null,"tig","tig") ;
        new Iso639("Tigrinya","ti","tir","tir") ;
        new Iso639("Timne",null,"tem","tem") ;
        new Iso639("Tiv",null,"tiv","tiv") ;
        new Iso639("Tlingit",null,"tli","tli") ;
        new Iso639("Tok Pisin",null,"tpi","tpi") ;
        new Iso639("Tokelau",null,"tkl","tkl") ;
        new Iso639("Tonga (Nyasa)",null,"tog","tog") ;
        new Iso639("Tonga (Tonga Islands)","to","ton","ton") ;
        new Iso639("Tsimshian",null,"tsi","tsi") ;
        new Iso639("Tsonga","ts","tso","tso") ;
        new Iso639("Tswana","tn","tsn","tsn") ;
        new Iso639("Tumbuka",null,"tum","tum") ;
        new Iso639("Tupi languages",null,"tup","tup") ;
        new Iso639("Turkish","tr","tur","tur") ;
        new Iso639("Turkish, Ottoman (1500-1928)",null,"ota","ota") ;
        new Iso639("Turkmen","tk","tuk","tuk") ;
        new Iso639("Tuvalu",null,"tvl","tvl") ;
        new Iso639("Tuvinian",null,"tyv","tyv") ;
        new Iso639("Twi","tw","twi","twi") ;

        new Iso639("Udmurt",null,"udm","udm") ;
        new Iso639("Ugaritic",null,"uga","uga") ;
        new Iso639("Uighur","ug","uig","uig") ;
        new Iso639("Ukrainian","uk","ukr","ukr") ;
        new Iso639("Umbundu",null,"umb","umb") ;
        new Iso639("Uncoded languages",null,"mis","mis") ;
        new Iso639("Undetermined",null,"und","und",LT_UNDETERMINED) ;
        new Iso639("Upper Sorbian",null,"hsb","hsb") ;
        new Iso639("Urdu","ur","urd","urd") ;
        new Iso639("Uzbek","uz","uzb","uzb") ;

        new Iso639("Vai",null,"vai","vai") ;
        new Iso639("Venda","ve","ven","ven") ;
        new Iso639("Vietnamese","vi","vie","vie") ;
        new Iso639("Volapük","vo","vol","vol") ;
        new Iso639("Votic",null,"vot","vot") ;

        new Iso639("Wakashan languages",null,"wak","wak") ;
        new Iso639("Walloon","wa","wln","wln") ;
        new Iso639("Waray",null,"war","war") ;
        new Iso639("Washo",null,"was","was") ;
        new Iso639("Welsh","cy","wel","cym") ;
        new Iso639("Western Frisian","fy","fry","fry") ;
        new Iso639("Wolaitta",null,"wal","wal") ;
        new Iso639("Wolof","wo","wol","wol") ;

        new Iso639("Xhosa","xh","xho","xho") ;

        new Iso639("Yakut",null,"sah","sah") ;
        new Iso639("Yao",null,"yao","yao") ;
        new Iso639("Yapese",null,"yap","yap") ;
        new Iso639("Yiddish","yi","yid","yid") ;
        new Iso639("Yoruba","yo","yor","yor") ;
        new Iso639("Yupik languages",null,"ypk","ypk") ;

        new Iso639("Zande languages",null,"znd","znd") ;
        new Iso639("Zapotec",null,"zap","zap") ;
        new Iso639("Zenaga",null,"zen","zen") ;
        new Iso639("Zulu","zu","zul","zul") ;
        new Iso639("Zuni",null,"zun","zun") ;
    }
}
