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

/*
 * Iso3166.java
 *
 * Created on July 24, 2001, 11:46 PM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.lang;


import java.util.HashMap;
import java.util.Locale ;
import java.util.Map;
/** Country code names from ISO 3166.
 *<p>
   An encapsulation of registry
 * of ISO 3166.
 * This was updated on 24th July 2001 from
 * <a href="http://www.din.de/gremien/nas/nabd/iso3166ma/codlstp1/db_en.html">
 * http://www.din.de/gremien/nas/nabd/iso3166ma/codlstp1/db_en.html</a>
 */
public class Iso3166  {

   
    static final Map<String, Iso3166> all = new HashMap<>();

    /** Creates new Iso639 */
    private Iso3166(String code,String engName) {
        this.code = code.toLowerCase(Locale.ENGLISH);
        name = engName;
        all.put(this.code,this);
    }
    
/** Look a country code up in the list.
 * @param countryId The country code to look up.
 * @return The corresponding Iso3166 object,
 * or null if not in the list.
 */    
    static public Iso3166 find(String countryId) {
        return all.get(countryId);
    }
/** The 2 character country code.
 */    
    public final String code;
/** The name of the country (in English).
 */    
    public final String name;
    
    static {
        new Iso3166("AF","AFGHANISTAN");
        new Iso3166("AL","ALBANIA");
        new Iso3166("DZ","ALGERIA");
        new Iso3166("AS","AMERICAN SAMOA");
        new Iso3166("AD","ANDORRA");
        new Iso3166("AO","ANGOLA");
        new Iso3166("AI","ANGUILLA");
        new Iso3166("AQ","ANTARCTICA");
        new Iso3166("AG","ANTIGUA AND BARBUDA");
        new Iso3166("AR","ARGENTINA");
        new Iso3166("AM","ARMENIA");
        new Iso3166("AW","ARUBA");
        new Iso3166("AU","AUSTRALIA");
        new Iso3166("AT","AUSTRIA");
        new Iso3166("AZ","AZERBAIJAN");
        new Iso3166("BS","BAHAMAS");
        new Iso3166("BH","BAHRAIN");
        new Iso3166("BD","BANGLADESH");
        new Iso3166("BB","BARBADOS");
        new Iso3166("BY","BELARUS");
        new Iso3166("BE","BELGIUM");
        new Iso3166("BZ","BELIZE");
        new Iso3166("BJ","BENIN");
        new Iso3166("BM","BERMUDA");
        new Iso3166("BT","BHUTAN");
        new Iso3166("BO","BOLIVIA");
        new Iso3166("BA","BOSNIA AND HERZEGOVINA");
        new Iso3166("BW","BOTSWANA");
        new Iso3166("BV","BOUVET ISLAND");
        new Iso3166("BR","BRAZIL");
        new Iso3166("IO","BRITISH INDIAN OCEAN TERRITORY");
        new Iso3166("BN","BRUNEI DARUSSALAM");
        new Iso3166("BG","BULGARIA");
        new Iso3166("BF","BURKINA FASO");
        new Iso3166("BI","BURUNDI");
        new Iso3166("KH","CAMBODIA");
        new Iso3166("CM","CAMEROON");
        new Iso3166("CA","CANADA");
        new Iso3166("CV","CAPE VERDE");
        new Iso3166("KY","CAYMAN ISLANDS");
        new Iso3166("CF","CENTRAL AFRICAN REPUBLIC");
        new Iso3166("TD","CHAD");
        new Iso3166("CL","CHILE");
        new Iso3166("CN","CHINA");
        new Iso3166("CX","CHRISTMAS ISLAND");
        new Iso3166("CC","COCOS (KEELING) ISLANDS");
        new Iso3166("CO","COLOMBIA");
        new Iso3166("KM","COMOROS");
        new Iso3166("CG","CONGO");
        new Iso3166("CD","CONGO, THE DEMOCRATIC REPUBLIC OF THE");
        new Iso3166("CK","COOK ISLANDS");
        new Iso3166("CR","COSTA RICA");
        new Iso3166("CI","COTE D'IVOIRE");
        new Iso3166("HR","CROATIA");
        new Iso3166("CU","CUBA");
        new Iso3166("CY","CYPRUS");
        new Iso3166("CZ","CZECH REPUBLIC");
        new Iso3166("DK","DENMARK");
        new Iso3166("DJ","DJIBOUTI");
        new Iso3166("DM","DOMINICA");
        new Iso3166("DO","DOMINICAN REPUBLIC");
        new Iso3166("TP","EAST TIMOR");
        new Iso3166("EC","ECUADOR");
        new Iso3166("EG","EGYPT");
        new Iso3166("SV","EL SALVADOR");
        new Iso3166("GQ","EQUATORIAL GUINEA");
        new Iso3166("ER","ERITREA");
        new Iso3166("EE","ESTONIA");
        new Iso3166("ET","ETHIOPIA");
        new Iso3166("FK","FALKLAND ISLANDS (MALVINAS)");
        new Iso3166("FO","FAROE ISLANDS");
        new Iso3166("FJ","FIJI");
        new Iso3166("FI","FINLAND");
        new Iso3166("FR","FRANCE");
        new Iso3166("GF","FRENCH GUIANA");
        new Iso3166("PF","FRENCH POLYNESIA");
        new Iso3166("TF","FRENCH SOUTHERN TERRITORIES");
        new Iso3166("GA","GABON");
        new Iso3166("GM","GAMBIA");
        new Iso3166("GE","GEORGIA");
        new Iso3166("DE","GERMANY");
        new Iso3166("GH","GHANA");
        new Iso3166("GI","GIBRALTAR");
        new Iso3166("GR","GREECE");
        new Iso3166("GL","GREENLAND");
        new Iso3166("GD","GRENADA");
        new Iso3166("GP","GUADELOUPE");
        new Iso3166("GU","GUAM");
        new Iso3166("GT","GUATEMALA");
        new Iso3166("GN","GUINEA");
        new Iso3166("GW","GUINEA-BISSAU");
        new Iso3166("GY","GUYANA");
        new Iso3166("HT","HAITI");
        new Iso3166("HM","HEARD ISLAND AND MCDONALD ISLANDS");
        new Iso3166("VA","HOLY SEE (VATICAN CITY STATE)");
        new Iso3166("HN","HONDURAS");
        new Iso3166("HK","HONG KONG");
        new Iso3166("HU","HUNGARY");
        new Iso3166("IS","ICELAND");
        new Iso3166("IN","INDIA");
        new Iso3166("ID","INDONESIA");
        new Iso3166("IR","IRAN, ISLAMIC REPUBLIC OF");
        new Iso3166("IQ","IRAQ");
        new Iso3166("IE","IRELAND");
        new Iso3166("IL","ISRAEL");
        new Iso3166("IT","ITALY");
        new Iso3166("JM","JAMAICA");
        new Iso3166("JP","JAPAN");
        new Iso3166("JO","JORDAN");
        new Iso3166("KZ","KAZAKSTAN");
        new Iso3166("KE","KENYA");
        new Iso3166("KI","KIRIBATI");
        new Iso3166("KP","KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF");
        new Iso3166("KR","KOREA, REPUBLIC OF");
        new Iso3166("KW","KUWAIT");
        new Iso3166("KG","KYRGYZSTAN");
        new Iso3166("LA","LAO PEOPLE'S DEMOCRATIC REPUBLIC");
        new Iso3166("LV","LATVIA");
        new Iso3166("LB","LEBANON");
        new Iso3166("LS","LESOTHO");
        new Iso3166("LR","LIBERIA");
        new Iso3166("LY","LIBYAN ARAB JAMAHIRIYA");
        new Iso3166("LI","LIECHTENSTEIN");
        new Iso3166("LT","LITHUANIA");
        new Iso3166("LU","LUXEMBOURG");
        new Iso3166("MO","MACAU");
        new Iso3166("MK","MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF");
        new Iso3166("MG","MADAGASCAR");
        new Iso3166("MW","MALAWI");
        new Iso3166("MY","MALAYSIA");
        new Iso3166("MV","MALDIVES");
        new Iso3166("ML","MALI");
        new Iso3166("MT","MALTA");
        new Iso3166("MH","MARSHALL ISLANDS");
        new Iso3166("MQ","MARTINIQUE");
        new Iso3166("MR","MAURITANIA");
        new Iso3166("MU","MAURITIUS");
        new Iso3166("YT","MAYOTTE");
        new Iso3166("MX","MEXICO");
        new Iso3166("FM","MICRONESIA, FEDERATED STATES OF");
        new Iso3166("MD","MOLDOVA, REPUBLIC OF");
        new Iso3166("MC","MONACO");
        new Iso3166("MN","MONGOLIA");
        new Iso3166("MS","MONTSERRAT");
        new Iso3166("MA","MOROCCO");
        new Iso3166("MZ","MOZAMBIQUE");
        new Iso3166("MM","MYANMAR");
        new Iso3166("NA","NAMIBIA");
        new Iso3166("NR","NAURU");
        new Iso3166("NP","NEPAL");
        new Iso3166("NL","NETHERLANDS");
        new Iso3166("AN","NETHERLANDS ANTILLES");
        new Iso3166("NC","NEW CALEDONIA");
        new Iso3166("NZ","NEW ZEALAND");
        new Iso3166("NI","NICARAGUA");
        new Iso3166("NE","NIGER");
        new Iso3166("NG","NIGERIA");
        new Iso3166("NU","NIUE");
        new Iso3166("NF","NORFOLK ISLAND");
        new Iso3166("MP","NORTHERN MARIANA ISLANDS");
        new Iso3166("NO","NORWAY");
        new Iso3166("OM","OMAN");
        new Iso3166("PK","PAKISTAN");
        new Iso3166("PW","PALAU");
        new Iso3166("PS","PALESTINIAN TERRITORY, OCCUPIED");
        new Iso3166("PA","PANAMA");
        new Iso3166("PG","PAPUA NEW GUINEA");
        new Iso3166("PY","PARAGUAY");
        new Iso3166("PE","PERU");
        new Iso3166("PH","PHILIPPINES");
        new Iso3166("PN","PITCAIRN");
        new Iso3166("PL","POLAND");
        new Iso3166("PT","PORTUGAL");
        new Iso3166("PR","PUERTO RICO");
        new Iso3166("QA","QATAR");
        new Iso3166("RE","REUNION");
        new Iso3166("RO","ROMANIA");
        new Iso3166("RU","RUSSIAN FEDERATION");
        new Iso3166("RW","RWANDA");
        new Iso3166("SH","SAINT HELENA");
        new Iso3166("KN","SAINT KITTS AND NEVIS");
        new Iso3166("LC","SAINT LUCIA");
        new Iso3166("PM","SAINT PIERRE AND MIQUELON");
        new Iso3166("VC","SAINT VINCENT AND THE GRENADINES");
        new Iso3166("WS","SAMOA");
        new Iso3166("SM","SAN MARINO");
        new Iso3166("ST","SAO TOME AND PRINCIPE");
        new Iso3166("SA","SAUDI ARABIA");
        new Iso3166("SN","SENEGAL");
        new Iso3166("SC","SEYCHELLES");
        new Iso3166("SL","SIERRA LEONE");
        new Iso3166("SG","SINGAPORE");
        new Iso3166("SK","SLOVAKIA");
        new Iso3166("SI","SLOVENIA");
        new Iso3166("SB","SOLOMON ISLANDS");
        new Iso3166("SO","SOMALIA");
        new Iso3166("ZA","SOUTH AFRICA");
        new Iso3166("GS","SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS");
        new Iso3166("ES","SPAIN");
        new Iso3166("LK","SRI LANKA");
        new Iso3166("SD","SUDAN");
        new Iso3166("SR","SURINAME");
        new Iso3166("SJ","SVALBARD AND JAN MAYEN");
        new Iso3166("SZ","SWAZILAND");
        new Iso3166("SE","SWEDEN");
        new Iso3166("CH","SWITZERLAND");
        new Iso3166("SY","SYRIAN ARAB REPUBLIC");
        new Iso3166("TW","TAIWAN, PROVINCE OF CHINA");
        new Iso3166("TJ","TAJIKISTAN");
        new Iso3166("TZ","TANZANIA, UNITED REPUBLIC OF");
        new Iso3166("TH","THAILAND");
        new Iso3166("TG","TOGO");
        new Iso3166("TK","TOKELAU");
        new Iso3166("TO","TONGA");
        new Iso3166("TT","TRINIDAD AND TOBAGO");
        new Iso3166("TN","TUNISIA");
        new Iso3166("TR","TURKEY");
        new Iso3166("TM","TURKMENISTAN");
        new Iso3166("TC","TURKS AND CAICOS ISLANDS");
        new Iso3166("TV","TUVALU");
        new Iso3166("UG","UGANDA");
        new Iso3166("UA","UKRAINE");
        new Iso3166("AE","UNITED ARAB EMIRATES");
        new Iso3166("GB","UNITED KINGDOM");
        new Iso3166("US","UNITED STATES");
        new Iso3166("UM","UNITED STATES MINOR OUTLYING ISLANDS");
        new Iso3166("UY","URUGUAY");
        new Iso3166("UZ","UZBEKISTAN");
        new Iso3166("VU","VANUATU");
        new Iso3166("VE","VENEZUELA");
        new Iso3166("VN","VIET NAM");
        new Iso3166("VG","VIRGIN ISLANDS, BRITISH");
        new Iso3166("VI","VIRGIN ISLANDS, U.S.");
        new Iso3166("WF","WALLIS AND FUTUNA");
        new Iso3166("EH","WESTERN SAHARA");
        new Iso3166("YE","YEMEN");
        new Iso3166("YU","YUGOSLAVIA");
        new Iso3166("ZM","ZAMBIA");
        new Iso3166("ZW","ZIMBABWE");
        
    }
}
