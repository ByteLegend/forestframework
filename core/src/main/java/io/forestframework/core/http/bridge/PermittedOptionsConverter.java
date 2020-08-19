package io.forestframework.core.http.bridge;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.core.json.JsonObject;

@SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
public class PermittedOptionsConverter {
    public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, PermittedOptions obj) {
        for (java.util.Map.Entry<String, Object> member : json) {
            switch (member.getKey()) {
                case "address":
                    if (member.getValue() instanceof String) {
                        obj.setAddress((String) member.getValue());
                    }
                    break;
                case "addressRegex":
                    if (member.getValue() instanceof String) {
                        obj.setAddressRegex((String) member.getValue());
                    }
                    break;
                case "match":
                    if (member.getValue() instanceof JsonObject) {
                        obj.setMatch(((JsonObject) member.getValue()).copy());
                    }
                    break;
                case "requiredAuthority":
                    if (member.getValue() instanceof String) {
                        obj.setRequiredAuthority((String) member.getValue());
                    }
                    break;
            }
        }
    }

    public static void toJson(PermittedOptions obj, JsonObject json) {
        toJson(obj, json.getMap());
    }

    public static void toJson(PermittedOptions obj, java.util.Map<String, Object> json) {
        if (obj.getAddress() != null) {
            json.put("address", obj.getAddress());
        }
        if (obj.getAddressRegex() != null) {
            json.put("addressRegex", obj.getAddressRegex());
        }
        if (obj.getMatch() != null) {
            json.put("match", obj.getMatch());
        }
        if (obj.getRequiredAuthority() != null) {
            json.put("requiredAuthority", obj.getRequiredAuthority());
        }
    }
}
