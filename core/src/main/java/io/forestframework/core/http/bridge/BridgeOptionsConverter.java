package io.forestframework.core.http.bridge;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@SuppressFBWarnings("SF_SWITCH_NO_DEFAULT")
public class BridgeOptionsConverter {
    public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, BridgeOptions obj) {
        for (java.util.Map.Entry<String, Object> member : json) {
            switch (member.getKey()) {
                case "inboundPermitteds":
                    if (member.getValue() instanceof JsonArray) {
                        java.util.ArrayList<PermittedOptions> list = new java.util.ArrayList<>();
                        ((Iterable<Object>) member.getValue()).forEach(item -> {
                            if (item instanceof JsonObject) {
                                list.add(new PermittedOptions((io.vertx.core.json.JsonObject) item));
                            }
                        });
                        obj.setInboundPermitteds(list);
                    }
                    break;
                case "outboundPermitteds":
                    if (member.getValue() instanceof JsonArray) {
                        java.util.ArrayList<PermittedOptions> list = new java.util.ArrayList<>();
                        ((Iterable<Object>) member.getValue()).forEach(item -> {
                            if (item instanceof JsonObject) {
                                list.add(new PermittedOptions((io.vertx.core.json.JsonObject) item));
                            }
                        });
                        obj.setOutboundPermitteds(list);
                    }
                    break;
            }
        }
    }

    public static void toJson(BridgeOptions obj, JsonObject json) {
        toJson(obj, json.getMap());
    }

    public static void toJson(BridgeOptions obj, java.util.Map<String, Object> json) {
        if (obj.getInboundPermitteds() != null) {
            JsonArray array = new JsonArray();
            obj.getInboundPermitteds().forEach(item -> array.add(item.toJson()));
            json.put("inboundPermitteds", array);
        }
        if (obj.getOutboundPermitteds() != null) {
            JsonArray array = new JsonArray();
            obj.getOutboundPermitteds().forEach(item -> array.add(item.toJson()));
            json.put("outboundPermitteds", array);
        }
    }
}
