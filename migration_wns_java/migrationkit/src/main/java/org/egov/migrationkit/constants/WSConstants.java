package org.egov.migrationkit.constants;

import java.util.HashMap;

public class WSConstants {
	
	public static final HashMap<String, String> DIGIT_ROAD_CATEGORIES = new HashMap<>();

    static {
        DIGIT_ROAD_CATEGORIES.put("Premix Carpet", "PREMIXCARPET");
        DIGIT_ROAD_CATEGORIES.put("BM and Premix Road", "BMPREMIXROAD");
        DIGIT_ROAD_CATEGORIES.put("Berm Cutting (Katcha)", "BERMCUTTINGKATCHA");
        DIGIT_ROAD_CATEGORIES.put("Brick Paving", "BRICKPAVING");
        DIGIT_ROAD_CATEGORIES.put("CC Road", "CCROAD");
        DIGIT_ROAD_CATEGORIES.put("Interlocking Paver Block", "INTERLOCKINGPAVERBLOCK");
        DIGIT_ROAD_CATEGORIES.put("Under Scheme", "UNDERSCHEME");
        DIGIT_ROAD_CATEGORIES.put("Open Pipe", "OPENPIPE");
    }
}
