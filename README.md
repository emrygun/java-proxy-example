# Java Proxy Example

Usage example of Jdk proxy and CGLIB proxy on a simple caching scenario.

Scenario with 2 proxy instances of Calculator implementation:
```java
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

```
Output log:
```java
INFO com.example.Main$CachingInvocationHandler -- sum(2,3) | Cache not found.
5
INFO com.example.Main$CachingInvocationHandler -- sum(2,3) | Cache found.
5
INFO com.example.Main$CachingInvocationHandler -- sum(3,3) | Cache not found.
6
INFO com.example.Main$CachingInvocationHandler -- sum(2,3) | Cache found.
5
INFO com.example.Main$CachingInvocationHandler -- sum(2,3) | Cache found.
5
INFO com.example.Main$CachingInvocationHandler -- sum(3,3) | Cache found.
6
INFO com.example.Main$CachingInvocationHandler -- sum(3,3) | Cache found.
6
INFO com.example.Main$CachingInvocationHandler -- sum(4,3) | Cache not found.
7
INFO com.example.Main$CachingInvocationHandler -- multiply(4,3) | Cache not found.
12
INFO com.example.Main$CachingInvocationHandler -- multiply(4,3) | Cache found.
12
INFO com.example.Main$CachingMethodInterceptor -- sum(5,3) | Cache not found.
8
INFO com.example.Main$CachingMethodInterceptor -- sum(5,3) | Cache found.
8
INFO com.example.Main$CachingMethodInterceptor -- sum(5,3) | Cache found.
8
INFO com.example.Main$CachingMethodInterceptor -- sum(8,3) | Cache not found.
11
INFO com.example.Main$CachingMethodInterceptor -- sum(2,3) | Cache found.
5
INFO com.example.Main$CachingMethodInterceptor -- sum(3,3) | Cache found.
6
INFO com.example.Main$CachingMethodInterceptor -- sum(3,3) | Cache found.
6
INFO com.example.Main$CachingMethodInterceptor -- sum(8,3) | Cache found.
11
INFO com.example.Main$CachingMethodInterceptor -- multiply(5,3) | Cache not found.
15
INFO com.example.Main$CachingMethodInterceptor -- multiply(5,3) | Cache found.
15
```
