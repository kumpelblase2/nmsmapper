package de.eternalwings.nmsmapper.wrapping;

import de.eternalwings.nmsmapper.processor.NMSProcessor;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class WrappingRegister {
    private final Map<String, Class<?>> cacheMap = new HashMap<String, Class<?>>();

    protected boolean hasWrapperCached(Class<?> type) {
        return this.cacheMap.containsKey(type.getCanonicalName());
    }

    public <T> T newWrappingInstance(Object target, Class<T> interfaceType) {
        Class<? extends T> wrapper;
        if(this.hasWrapperCached(interfaceType)) {
            wrapper = this.getCachedWrapper(interfaceType);
        } else {
            try {
                wrapper = this.getWrapperFromInterface(interfaceType);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return this.createWrapperInstance(target, wrapper);
    }

    protected <T> T createWrapperInstance(Object target, Class<T> wrapper) {
        if(target == null) {
            return null;
        }

        Constructor<T> constructor = this.findConstructor(target.getClass(), wrapper);
        if(constructor != null) {
            try {
                return constructor.newInstance(target);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private <T> Constructor<T> findConstructor(Class<?> paramType, Class<T> wrapper) {
        if(paramType.equals(Object.class)) {
            return null;
        }

        try {
            return wrapper.getConstructor(paramType);
        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return this.findConstructor(paramType.getSuperclass(), wrapper);
        }
    }

    protected <T> Class<? extends T> getCachedWrapper(Class<T> interfaceType) {
        return (Class<? extends T>) this.cacheMap.get(interfaceType.getCanonicalName());
    }

    protected <T> Class<? extends T> getWrapperFromInterface(Class<T> interfaceType) throws ClassNotFoundException {
        String wrapperClassName = NMSProcessor.buildNMSWrapperName(interfaceType.getCanonicalName());
        Class<? extends T> wrapperClass = (Class<? extends T>) this.findClass(wrapperClassName);
        this.cacheClass(interfaceType.getCanonicalName(), wrapperClass);
        return wrapperClass;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    protected void cacheClass(String interfaceName, Class<?> wrapper) {
        this.cacheMap.put(interfaceName, wrapper);
    }
}
