package io.forestframework.core.http.routing;

import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractMultipleRoutersIntegrationTest extends AbstractHttpIntegrationTest {
    private List<AbstractTraceableRouter> routers = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void setUp() {
        routers.forEach(router -> router.traces.clear());
    }

    protected void addToRouters(AbstractTraceableRouter... routers) {
        this.routers.addAll(Arrays.asList(routers));
    }

    protected List<String> getTraces() {
        return routers.stream().map(router -> router.traces).flatMap(List::stream).collect(Collectors.toList());
    }
}
