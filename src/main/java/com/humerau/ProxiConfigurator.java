package com.humerau;

public interface ProxiConfigurator {
    Object replaceWithProxyIfNeeded(Object t, Class implClass);
}
