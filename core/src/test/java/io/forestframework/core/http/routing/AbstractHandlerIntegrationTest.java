package io.forestframework.core.http.routing;

import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class AbstractHandlerIntegrationTest extends AbstractHttpIntegrationTest {
    protected AbstractTraceableRouter router;

    @BeforeEach
    void setUp() {
        client = HttpClients.createDefault();
        router.traces.clear();
    }

    @AfterEach
    void cleanUp() throws IOException {
        client.close();
    }
}
