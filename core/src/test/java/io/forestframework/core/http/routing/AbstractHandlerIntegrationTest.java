package io.forestframework.core.http.routing;

import org.junit.jupiter.api.BeforeEach;

public class AbstractHandlerIntegrationTest extends AbstractHttpIntegrationTest {
    protected AbstractTraceableRouter router;
    @BeforeEach
    void setUp() {
        router.traces.clear();
    }
}
