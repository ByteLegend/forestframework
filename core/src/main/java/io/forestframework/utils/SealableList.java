package io.forestframework.utils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SealableList<E> extends SealableListWrapper<E> {
    private final AtomicBoolean sealState;

    public SealableList() {
        this(new AtomicBoolean());
    }

    private SealableList(AtomicBoolean state) {
        super(new ArrayList<>(), state::get);
        this.sealState = state;
    }

    @Override
    public boolean isSealed() {
        return sealState.get();
    }

    @Override
    public void seal() {
        this.sealState.set(true);
    }
}
