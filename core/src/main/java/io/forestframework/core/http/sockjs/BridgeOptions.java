package io.forestframework.core.http.sockjs;


import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Specify the event bus bridge options.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class BridgeOptions {

    private List<PermittedOptions> inboundPermitted = new ArrayList<>();
    private List<PermittedOptions> outboundPermitted = new ArrayList<>();

    /**
     * Creates a new instance of {@link BridgeOptions}, without any inbound and outbound permitted.
     */
    public BridgeOptions() {

    }

    /**
     * Creates a new instance of {@link BridgeOptions} by copying the content of another {@link io.vertx.ext.bridge.BridgeOptions}
     *
     * @param that the {@link BridgeOptions} to copy.
     */
    public BridgeOptions(BridgeOptions that) {
        this.inboundPermitted = that.inboundPermitted;
        this.outboundPermitted = that.outboundPermitted;
    }

    /**
     * Creates a new instance of {@link BridgeOptions} from its JSON representation.
     * This method uses the generated converter.
     *
     * @param json the serialized {@link BridgeOptions}
     * @see BridgeOptionsConverter
     */
    public BridgeOptions(JsonObject json) {
        BridgeOptionsConverter.fromJson(json, this);
    }

    /**
     * Serializes the current {@link BridgeOptions} to JSON. This method uses the generated converter.
     *
     * @return the serialized object
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        BridgeOptionsConverter.toJson(this, json);
        return json;
    }

    /**
     * Adds an inbound permitted option to the current {@link BridgeOptions}.
     *
     * @param permitted the inbound permitted
     * @return the current {@link BridgeOptions}.
     */
    public BridgeOptions addInboundPermitted(PermittedOptions permitted) {
        inboundPermitted.add(permitted);
        return this;
    }

    /**
     * @return the list of inbound permitted options. Empty if none.
     */
    public List<PermittedOptions> getInboundPermitteds() {
        return inboundPermitted;
    }

    /**
     * Sets the list of inbound permitted options.
     *
     * @param inboundPermitted the list to use, must not be {@link null}. This method use the direct list reference
     * (and doesn't create a copy).
     * @return the current {@link BridgeOptions}.
     */
    public BridgeOptions setInboundPermitteds(List<PermittedOptions> inboundPermitted) {
        this.inboundPermitted = inboundPermitted;
        return this;
    }

    /**
     * Adds an outbound permitted option to the current {@link BridgeOptions}.
     *
     * @param permitted the outbound permitted
     * @return the current {@link BridgeOptions}.
     */
    public BridgeOptions addOutboundPermitted(PermittedOptions permitted) {
        outboundPermitted.add(permitted);
        return this;
    }

    /**
     * @return the list of outbound permitted options. Empty if none.
     */
    public List<PermittedOptions> getOutboundPermitteds() {
        return outboundPermitted;
    }

    /**
     * Sets the list of outbound permitted options.
     *
     * @param outboundPermitted the list to use, must not be {@link null}. This method use the direct list reference
     * (and doesn't create a copy).
     * @return the current {@link BridgeOptions}.
     */
    public BridgeOptions setOutboundPermitteds(List<PermittedOptions> outboundPermitted) {
        this.outboundPermitted = outboundPermitted;
        return this;
    }
}
