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

## [Getting Started](https://github.com/google/guice/wiki/GettingStarted)
> Guice is a framework that makes it easier for your application to use the dependency injection (DI) pattern.

> Dependency injection is a design pattern wherein classes declare their dependencies as arguments instead of creating those dependencies directly. 类将其依赖项声明为参数，而不是直接创建这些依赖项。

### Core Guice Concepts
#### 1. `@Inject` constructor
> Java class constructors that are annotated with `@Inject` can be called by Guice through a process called **constructor injection**, during which the constructors' arguments will be created and provided by Guice.

```java
class Greeter {
    private final String message;
    private final int count;

    // Greeter declares that it needs a string message and an integer representing the number of time the message to be printed.
    // The @Inject annotation marks this constructor as eligible to be used by Guice.
    @Inject
    Greeter(@Message String message, @Count int count) {
        this.message = message;
        this.count = count;
    }

    void sayHello() {
        for (int i = 0; i < count; i++) {
            System.out.println(message);
        }
    }
}
```
> In the example above, the `Greeter` class has a constructor that is called when application asks Guice to create an instance of `Greeter`. Guice will create the two arguments required, then invoke the constructor. The `Greeter` class's constructor arguments are its dependencies and applications use `Module` to tell Guice how to satisfy those dependencies.

> Injection is a process of injecting dependency into an object. Constructor injection is quite common. In tis process, dependency is injected as argument to the constructor. 
> Injection 即将依赖注入到一个对象中。在 constructor injection (构造器注入) 过程中，依赖作为参数 (argument) 传入到构造器中。

`@Inject` 是 Guice（和其他依赖注入框架，如 Jakarta/JSR-330）提供的注解，用于告诉框架：**这个构造函数、字段或方法需要被自动注入依赖对象**。

#### 2. Guice modules
> Applications contain objects that declare dependencies on other objects, and those dependencies form graphs.
> Guice modules allow applications to specify how to satisfy those dependencies.

对象和其依赖构建成一个 graph，一个对象本身可以是另一个对象的依赖。Guice module 定义这些依赖。

在 Guice 中，`Module` 是用来告诉 Guice 如何“绑定”接口和它的实现的配置类。你可以认为 Module 是一个“配置容器”，让 Guice 知道：
* 你有哪些依赖（例如：接口）
* 你希望使用哪些实现类
* 是否要使用单例、注解等绑定方式

假设你有如下接口和实现：
```java
public interface TransactionLog {
    void log(String message);
}

public class FileTransactionLog implements TransactionLog {
    public void log(String message) {
        System.out.println("Log to file: " + message);
    }
}
```

你可以通过 Module 指定绑定关系：
```java
public class BillingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TransactionLog.class).to(FileTransactionLog.class);
    }
}
```
这行的意思是：“每当有人请求 TransactionLog 的实例时，Guice 会自动创建 FileTransactionLog。”


#### 3. Guice injectors
> To bootstrap your application, you'll need to create a Guice `Injector` with one or more modules in it. 
```java
public final class MyWebServer {
    public void start() {
        // ...
    }

    public static void main(String[] args) {
    // Creates an injector that has all the necessary dependencies needed to
    // build a functional server.
    Injector injector = Guice.createInjector(
            new RequestLoggingModule(),
            new RequestHandlerModule(),
            new AuthenticationModule(),
            new DatabaseModule()
            //...
            );
    
    // Bootstrap the application by creating an instance of the server then
    // start the server to handle incoming requests.
    injector.getInstance(MyWebServer.class)
            .start();
    }
}
```
> **The injector internally holds the dependency graphs described in your application**. When you request an instance of a given type, the injector figures out what objects to construct, resolves their dependencies, and wires everything together.

Guice 中的 `Injector` 是 依赖注入容器的核心接口，负责：
* 创建对象（包括自动注入依赖）
* 管理依赖图（Dependency Graph）
* 执行依赖绑定逻辑（来自 Module）
* 控制作用域（如单例）

你可以把 `Injector` 理解为一个“**工厂 + 容器 + 控制器**”。

**Step 1:** 定义接口和实现类
```java
public interface TransactionLog {
    void log(String message);
}

public class FileTransactionLog implements TransactionLog {
    public void log(String message) {
        System.out.println("Logged to file: " + message);
    }
}
```

**Step 2:** 写 Module
```java
public class BillingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TransactionLog.class).to(FileTransactionLog.class);
    }
}
```

**Step 3:** 创建 `Injector` 
```java
Injector injector = Guice.createInjector(new MyModule());
```
这会做两件事：
1. 读取 `Module` 配置（执行 `MyModule.configure()`）
2. 构建依赖图，并准备在需要时自动创建对象

**Step 4:** 使用 Injector 获取实例（即依赖注入）
```java
BillingService service = injector.getInstance(BillingService.class);
```
* Guice 会自动递归注入所有依赖（如构造函数中 @Inject 的依赖）


✅Guice 的核心逻辑：
1. 执行 `Module.configure()`，建立接口与实现的映射表
2. 在调用 `getInstance(Foo.class)` 时：
    * 查找 Foo 的构造函数（是否带 @Inject）
    * 查找构造函数参数类型，并递归调用 getInstance(...)
    * 创建实例，注入所有依赖
    * 如果是单例绑定，缓存下来


# Reference
* [Google Guice Wiki](https://github.com/google/guice/wiki)
* TutorialsPoint: [Google Guice -- Constructor Injection](https://www.tutorialspoint.com/guice/guice_constructor_injection.htm)