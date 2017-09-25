/*
 * Copyright 2008 - 2017 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.shamrock.as.google.gate.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class GoogleParseConstants {

    public static final Map<String, String> citiesTranslateMapping = new HashMap<>();
    static {
        citiesTranslateMapping.put("münchen", "munich");
        citiesTranslateMapping.put("düsseldorf", "dusseldorf");
        citiesTranslateMapping.put("zürich", "zurich");
        citiesTranslateMapping.put("bruxelles", "brussels");
        citiesTranslateMapping.put("brussel", "brussels");
        citiesTranslateMapping.put("wien", "vienna");
        citiesTranslateMapping.put("rohm", "rome");
        citiesTranslateMapping.put("roma", "rome");
        citiesTranslateMapping.put("rōma", "rome");
    }

    public static final class Dublin {

        private static final Map<String, String> m = new HashMap<>();
        static {
            m.put("Arklow", "Y14");
            m.put("Ashbourne", "A84");
            m.put("Athenry", "H65");
            m.put("Athlone", "N37");
            m.put("Athy", "R14");
            m.put("Bagenalstown", "R21");
            m.put("Balbriggan", "K32");
            m.put("Ballina", "F26");
            m.put("Ballinasloe", "H53");
            m.put("Ballincollig", "P31");
            m.put("Ballinrobe", "F31");
            m.put("Ballyboughal", "A41");
            m.put("Ballyhaunis", "F35");
            m.put("Ballymote", "F56");
            m.put("Bandon", "P72");
            m.put("Bantry", "P75");
            m.put("Belturbet", "H14");
            m.put("Birr", "R42");
            m.put("Blackrock", "A94");
            m.put("Boyle", "F52");
            m.put("Bray", "A98");
            m.put("Bundoran", "F94");
            m.put("Caherciveen", "V23");
            m.put("Cahir", "E21");
            m.put("Carlow", "R93");
            m.put("Carrick On Shannon", "N41");
            m.put("Carrick On Suir", "E32");
            m.put("Carrickmacross", "A81");
            m.put("Carrigaline", "P43");
            m.put("Carrignavar", "T34");
            m.put("Cashel", "E25");
            m.put("Castlebar", "F23");
            m.put("Castleblayney", "A75");
            m.put("Castlerea", "F45");
            m.put("Cavan", "H12");
            m.put("Charleville", "P56");
            m.put("Clifden", "H71");
            m.put("Clonakilty", "P85");
            m.put("Clones", "H23");
            m.put("Clonmel", "E91");
            m.put("Cobh", "P24");
            m.put("Cootehill", "H16");
            m.put("Cork City Northside", "T23");
            m.put("Cork City Southside", "T12");
            m.put("Crookstown", "P14");
            m.put("Curragh", "R56");
            m.put("Drogheda", "A92");
            m.put("Dublin 1", "D01");
            m.put("Dublin 10", "D10");
            m.put("Dublin 11", "D11");
            m.put("Dublin 12", "D12");
            m.put("Dublin 13", "D13");
            m.put("Dublin 14", "D14");
            m.put("Dublin 15", "D15");
            m.put("Dublin 16", "D16");
            m.put("Dublin 17", "D17");
            m.put("Dublin 18", "D18");
            m.put("Dublin 2", "D02");
            m.put("Dublin 20", "D20");
            m.put("Dublin 22", "D22");
            m.put("Dublin 24", "D24");
            m.put("Dublin 3", "D03");
            m.put("Dublin 4", "D04");
            m.put("Dublin 5", "D05");
            m.put("Dublin 6", "D06");
            m.put("Dublin 6W", "D6W");
            m.put("Dublin 7", "D07");
            m.put("Dublin 8", "D08");
            m.put("Dublin 9", "D09");
            m.put("Dun Laoghaire", "A96");
            m.put("Dunboyne", "A86");
            m.put("Dundalk", "A91");
            m.put("Dunmanway", "P47");
            m.put("Dunshaughlin", "A85");
            m.put("Edenderry", "R45");
            m.put("Enfield", "A83");
            m.put("Enniscorthy", "Y21");
            m.put("Fermoy", "P61");
            m.put("Galway", "H91");
            m.put("Garristown", "A42");
            m.put("Glanmire", "T45");
            m.put("Gorey", "Y25");
            m.put("Greystones", "A63");
            m.put("Kells", "A82");
            m.put("Kenmare", "V93");
            m.put("Kildare", "R51");
            m.put("Kilkenny", "R95");
            m.put("Kilmacthomas", "X42");
            m.put("Kilmallock", "V35");
            m.put("Kilrush", "V15");
            m.put("Kinsale", "P17");
            m.put("Knock", "F12");
            m.put("Letterkenny", "F92");
            m.put("Lifford", "F93");
            m.put("Limerick", "V94");
            m.put("Listowel", "V31");
            m.put("Longford", "N39");
            m.put("Loughrea", "H62");
            m.put("Lucan", "K78");
            m.put("Lusk", "K45");
            m.put("Macroom", "12");
            m.put("Malahide", "K36");
            m.put("Mallow", "P51");
            m.put("Maynooth", "W23");
            m.put("Midleton", "P25");
            m.put("Miltown Malbay", "V95");
            m.put("Mitchelstown", "P67");
            m.put("Monaghan", "H18");
            m.put("Monasterevin", "W34");
            m.put("Mullingar", "N91");
            m.put("Naas", "W91");
            m.put("Nenagh", "E45");
            m.put("New Ross", "Y34");
            m.put("Newbridge", "W12");
            m.put("Newcastle West", "V42");
            m.put("Oldtown", "A45");
            m.put("Portlaoise", "R32");
            m.put("Roscommon", "F42");
            m.put("Roscrea", "E53");
            m.put("Rush", "K56");
            m.put("Rylane", "P32");
            m.put("Shannon", "V14");
            m.put("Skerries", "K34");
            m.put("Skibbereen", "P81");
            m.put("Sligo", "F91");
            m.put("Swords", "K67");
            m.put("Thurles", "E41");
            m.put("Tipperary", "E34");
            m.put("Tralee", "V92");
            m.put("Trim", "C15");
            m.put("Tuam", "H54");
            m.put("Tullamore", "R35");
            m.put("Waterford", "X91");
            m.put("Watergrasshill", "T56");
            m.put("Westport", "F28");
            m.put("Wexford", "Y35");
            m.put("Wicklow", "A67");
            m.put("Youghal", "P36");
        }

        public static final Pattern IRISH_POSTCODES_PATTERN = Pattern.compile("[A-Z][0-9]{2}\\s*[A-Z0-9]{4}", Pattern.CASE_INSENSITIVE);

        private Dublin() {}
    }

    private GoogleParseConstants() {}
}
