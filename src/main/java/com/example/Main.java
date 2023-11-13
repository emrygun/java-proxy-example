package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final Logger jdkProxyLogger = LoggerFactory.getLogger(CachingInvocationHandler.class);
    private static final Logger CGLIBProxyLogger = LoggerFactory.getLogger(CachingMethodInterceptor.class);

    public static void main(String[] args) {
        ClassLoader classLoader = Main.class.getClassLoader();
        InvocationHandler cachingInvocationHandler = new CachingInvocationHandler();
        Calculator calculatorJdkProxyInstance = (Calculator) Proxy.newProxyInstance(
                classLoader,
                new Class[]{Calculator.class},
                cachingInvocationHandler
        );

        Enhancer enhancer = new Enhancer();
        MethodInterceptor cachingMethodInterceptor = new CachingMethodInterceptor();
        enhancer.setSuperclass(SimpleCalculator.class);
        enhancer.setCallback(cachingMethodInterceptor);
        Calculator calculatorCGLIBProxyInstance = (Calculator) enhancer.create();

        // Test
        System.out.println(calculatorJdkProxyInstance.sum(2, 3));
        System.out.println(calculatorJdkProxyInstance.sum(2, 3));
        System.out.println(calculatorJdkProxyInstance.sum(3, 3));
        System.out.println(calculatorJdkProxyInstance.sum(2, 3));
        System.out.println(calculatorJdkProxyInstance.sum(2, 3));
        System.out.println(calculatorJdkProxyInstance.sum(3, 3));
        System.out.println(calculatorJdkProxyInstance.sum(3, 3));
        System.out.println(calculatorJdkProxyInstance.sum(4, 3));
        System.out.println(calculatorJdkProxyInstance.multiply(4, 3));
        System.out.println(calculatorJdkProxyInstance.multiply(4, 3));

        System.out.println(calculatorCGLIBProxyInstance.sum(5, 3));
        System.out.println(calculatorCGLIBProxyInstance.sum(5, 3));
        System.out.println(calculatorCGLIBProxyInstance.sum(5, 3));
        System.out.println(calculatorCGLIBProxyInstance.sum(8, 3));
        System.out.println(calculatorCGLIBProxyInstance.sum(2, 3));
        System.out.println(calculatorCGLIBProxyInstance.sum(3, 3));
        System.out.println(calculatorCGLIBProxyInstance.sum(3, 3));
        System.out.println(calculatorCGLIBProxyInstance.sum(8, 3));
        System.out.println(calculatorCGLIBProxyInstance.multiply(5, 3));
        System.out.println(calculatorCGLIBProxyInstance.multiply(5, 3));
    }

    public interface Calculator {
        int sum(int a, int b);
        int multiply(int a, int b);
    }

    public static class SimpleCalculator implements Calculator {
        public int sum(int a, int b) {
            try {
                Thread.sleep(Duration.ofSeconds(3).toMillis());
            } catch (InterruptedException ignored) { }
            return a + b;
        }

        public int multiply(int a, int b) {
            try {
                Thread.sleep(Duration.ofSeconds(3).toMillis());
            } catch (InterruptedException ignored) { }
            return a * b;
        }
    }

    public class Cache {
        private static final Map<MethodSignature, CacheEntry> cache = new ConcurrentHashMap<>();
        private static final Duration cacheLifetime = Duration.ofMinutes(1);

        public static Object getCachedValue(MethodSignature signature) {
            if (cache.containsKey(signature)) {
                CacheEntry entry = cache.get(signature);
                if (isExpired(entry)) {
                    cache.remove(signature);
                    return null;
                }
                return entry.value;
            }
            return null;
        }

        public static void setCacheValue(MethodSignature signature, Object value) {
            cache.put(signature, CacheEntry.create(value));
        }

        private static boolean isExpired(CacheEntry entry) {
            return LocalDateTime.now().isAfter(entry.dateTime.plus(cacheLifetime));
        }

        public record CacheEntry(Object value, LocalDateTime dateTime) {
            public static CacheEntry create(Object value) {
                return new CacheEntry(value, LocalDateTime.now());
            }
        }

        public record MethodSignature(Method method, Object[] args) {
            @Override
            public int hashCode() {
                return method().getName().hashCode() + Arrays.hashCode(args);
            }

            @Override
            public boolean equals(Object obj) {
                return this.hashCode() == obj.hashCode();
            }

            @Override
            public String toString() {
                var sj = new StringJoiner(",");
                for (Object arg : args) {
                    sj.add(arg.toString());
                }
                return method().getName() + "(" + sj + ")";
            }
        }
    }

    public static class CachingInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            var signature = new Cache.MethodSignature(method, args);
            var cachedValue = Cache.getCachedValue(signature);
            if (cachedValue != null) {
                jdkProxyLogger.info("{} | Cache found.", signature);
                return cachedValue;
            }
            jdkProxyLogger.info("{} | Cache not found.", signature);
            var value = method.invoke(new SimpleCalculator(), args);
            Cache.setCacheValue(signature, value);
            return value;
        }
    }

    public static class CachingMethodInterceptor implements MethodInterceptor {
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            var signature = new Cache.MethodSignature(method, args);
            var cachedValue = Cache.getCachedValue(signature);
            if (cachedValue != null) {
                CGLIBProxyLogger.info("{} | Cache found.", signature);
                return cachedValue;
            }
            CGLIBProxyLogger.info("{} | Cache not found.", signature);
            var value = proxy.invokeSuper(obj, args);
            Cache.setCacheValue(signature, value);
            return value;
        }
    }

}
