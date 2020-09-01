package io.forestframework.core.http.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbstractTraceableRouter {
    protected List<String> traces = Collections.synchronizedList(new ArrayList<>());

    protected void addToTrace(String traceId) {
        traces.add(traceId);
    }
}
