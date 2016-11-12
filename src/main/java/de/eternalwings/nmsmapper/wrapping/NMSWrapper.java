package de.eternalwings.nmsmapper.wrapping;

public class NMSWrapper {
    private final WrappingRegister register;

    public NMSWrapper() {
        this.register = new WrappingRegister();
    }

    public <T> T wrap(Object source, Class<T> type) {
        return this.register.newWrappingInstance(source, type);
    }
}
