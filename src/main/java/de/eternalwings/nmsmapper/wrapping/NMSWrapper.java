package de.eternalwings.nmsmapper.wrapping;

public class NMSWrapper {
    private static final NMSWrapper globalWrapper = new NMSWrapper();

    private final WrappingRegister register;

    public NMSWrapper() {
        this(new WrappingRegister());
    }

    public NMSWrapper(WrappingRegister register) {
        this.register = register;
    }

    public <T> T wrap(Object source, Class<T> type) {
        if(type.isAssignableFrom(source.getClass())) {
            return (T) source;
        }

        return this.register.newWrappingInstance(source, type);
    }

    public static NMSWrapper wrapper() {
        return globalWrapper;
    }

    public static <T> T wrapOf(Object source, Class<T> type) {
        return wrapper().wrap(source, type);
    }
}
