# README
> Guice is a lightweight dependency inject framework for Java.
> Put simply, Guice alleviates the need for factories and the use of `new` in your Java code.
> Think of Guice's `@Inject` as the new `new`.
> You will still need to write factories in some cases, but your code will not depend directly on them.

## [Motivation](https://github.com/google/guice/wiki/Motivation)
### 1. Direct constructor calls
Direct constructor calls，即直接通过 `new` 关键字创建对象。
```java
CreditCardProcessor processor = new PaypalCreditCardProcessor();
TransactionLog transactionLog = new DatabaseTransactionLog();
```
这种写法不利于模块化和可测试性。
编译时依赖于 real credit card processor (即 `PayPayCreditProcessor()`)，这就意味着测试代码时会真正 charge a credit card。

### 2. Factories
> A factory class decouples the client and implementing class. A simple factory uses static methods to get and set mock implementations for interfaces. A factory implemented with some boilerplate code.

工厂类将客户端和实现类分离。
```java
public class CreditCardProcessorFactory {
    private static CreditCardProcessor instance;

    public static void setInstance(CreditCardProcessor processor) {
        instance = processor;
    }

    public static CreditCardProcessor getInstance() {
        if (instance == null) {
            return new SquareCreditCardProcessor();
        }
        return instance;
    }
}
```
通过工厂类就可以写出正确的测试类。只需通过工厂类的 `setter()` 方法传入 fake credit card processor 即可。
```java
private final InMemoryTransactionLog transactionLog = new InMemoryTransactionLog();
private final FakeCreditCardProcessor processor = new FakeCreditCardProcessor();    // 虚拟的测试类，测试时不会引入意外操作

@Override
public void setUp() {
    TransactionLogFactory.setInstance(transactionLog);
    CreditCardProcessorFactory.setInstance(processor);
}
```

### 3. Dependency Inject
> Like the factory, dependency injection is just a design pattern. The core principle is to **separate behaviour from dependency resolution**. In our example, the RealBillingService is not responsible for looking up the TransactionLog and CreditCardProcessor. Instead, they're passed in as constructor parameters:

Dependency injection 的原则是将**功能**与**依赖解析**分离，即 `RealBillingService` 只专注 `chargeOrder()` 方法的实现，并不用在意其依赖(`TransactionLog` and `CreditCardProcessor`)是如何实现并创建的。
```java
public class RealBillingService implements BillingService {
    // 对于其依赖如何实现并创建，不用关注，而是通过依赖注入实现。
    private final CreditCardProcessor processor;
    private final TransactionLog transactionLog;
    
    public RealBillingService(CreditCardProcessor processor, TransactionLog transactionLog) {
        this.processor = processor;
        this.transactionLog = transactionLog;
    }
    
    // 该类只用关注方法的实现。
    public Receipt chargeOrder(PizzaOrder order, CreditCard creditCard) {
        try {
            ChargeResult result = processor.charge(creditCard, order.getAmount());
            transactionLog.logChargeResult(result);
        
            return result.wasSuccessful()
                    ? Receipt.forSuccessfulCharge(order.getAmount())
                    : Receipt.forDeclinedCharge(result.getDeclineMessage());
        } catch (UnreachableException e) {
            transactionLog.logConnectException(e);
            return Receipt.forSystemFailure(e.getMessage());
        }
    }
}
```

### 4. Dependency Injection with Guice
> The dependency injection pattern leads to code that's modular and testable, and Guice makes it easy to write. To use Guice in our billing example, we first need to tell it how to map our interfaces to their implementations. This configuration is done in a Guice module, which is any Java class that implements the Module interface:

在 module 中，定义了如何将 interfaces 映射到其实现类。
在下面的例子中：
1. `TransactionLog.class` 映射的实现类是 `DatabaseTransactionLog.class`。那么在程序中需要使用 `TransactionLog` 对象时，就用调用 `DatabaseTransactionLog` 对象。
2. `CreditCardProcessor.class` 映射的实现类是 `PaypalCreditCardProcessor.class`。
3. `BillingService.class` 映射的实现类是 `RealBillingService.class`。

```java
public class BillingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TransactionLog.class).to(DatabaseTransactionLog.class);
        bind(CreditCardProcessor.class).to(PaypalCreditCardProcessor.class);
        bind(BillingService.class).to(RealBillingService.class);
    }
}
```

在具体使用时，使用 `Injector` 可以用于获取任何绑定的类型。
```java
public static void main(String[] args) {
    Injector injector = Guice.createInjector(new BillingModule());
    BillingService billingService = injector.getInstance(BillingService.class);
    // ...
}
```

# Reference
* [Google Guice Wiki](https://github.com/google/guice/wiki)