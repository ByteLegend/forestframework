package org.forestframework.annotation;

import org.forestframework.ResponseProcessor;

public @interface ReturnValueProcessedBy {
    Class<? extends ResponseProcessor> value();
}
