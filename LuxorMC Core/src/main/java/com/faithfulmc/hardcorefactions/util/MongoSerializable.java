package com.faithfulmc.hardcorefactions.util;

import java.util.Map;

public interface MongoSerializable {
    Map<String, Object> serialize();
}
