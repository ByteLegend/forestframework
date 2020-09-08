package io.forestframework.core.http.routing;

import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class AbstractMultipleRoutersIntegrationTest extends AbstractHttpIntegrationTest {
    private List<AbstractTraceableRouter> routers = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void setUp() {
        client = HttpClients.createDefault();
        routers.forEach(router -> router.traces.clear());
    }

    @AfterEach
    void cleanUp() throws IOException {
        client.close();
    }

    protected void addToRouters(AbstractTraceableRouter... routers) {
        this.routers.addAll(Arrays.asList(routers));
    }

    protected List<String> getTraces() {
        return routers.stream().map(router -> router.traces).flatMap(List::stream).collect(Collectors.toList());
    }
}
