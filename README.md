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


## Guice Mental Model
> This page walks through a simplified model of Guice's implementation, which should make it easier to think about how it works.

### Guice is a map
> Fundamentally, Guice helps you create and retrieve objects for your application to use. These objects that your application needs are called **dependencies**.
> You can think of Guice as being a map [**guice-map**]. Your application code declares the dependencies it needs, and Guice fetches them for you from its map. Each entry in the "Guice map" has two parts:
> * **Guice key:** a key in the map which is used to fetch a particular value from the map.
> * **Provider:** a value in the map which is used to create objects for your application.

#### 1. Guice keys
> Guice uses `Key` to identify a dependency that can be resolved using the "Guice map".
```java
class Greeter {
    private final String message;
    private final int count;
    
    // Greeter declares that it needs a string message and an integer
    // representing the number of time the message to be printed.
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
The `Greeter` class declares two dependencies in its constructor and those dependencies are represented as `Key` in Guice:
* `@Message String` --> `Key<String>`
* `@Count int` --> `Key<Integer>`

Applications often have dependencies that are of the same type. Guice uses **binding annotations** to distinguish dependencies that are of the same type, that is to make the type more specific:
```java
final class MultilingualGreeter {
   private String englishGreeting;
   private String spanishGreeting;
   
   @Inject
   MultilingualGreeter(@English String englishGreeting, @Spanish String spanishGreeting) {
      this.englishGreeting = englishGreeting;
      this.spanishGreeting = spanishGreeting;
   }
}
```
`Key` with binding annotations can be created as:
```java
Key<String> englishGreetingKey = Key.get(String.class, English.class);
Key<String> spanishGreetingKey = Key.get(String.class, Spanish.class);
```

✅ To summarize: Guice Key is a type combined with an optional binding annotation used to identify dependencies.

#### 2. Guice `Provider`s
> Guice uses `Provider` to represent factories in the "Guice map" that are capable of creating objects to satisfy dependencies.

`Provider` is an interface with a single method:
```java
interface Provider<T> {
   /** Provides an instance of T.**/
   T get();
}
```
Each class that implements Provider is a bit of code that knows how to give you an instance of T. It could call `new T()`, it could construct T in some other way, or it could return you a precomputed instance from a cache.


### Using Guice
There are two parts to using Guice:
1. **Configuration**: your application adds things into the "Guice map".
2. **Injection**: your application asks Guice to create and retrieve objects from the map.

#### 1. Configuration
> Guice maps are configured using Guice modules (and **Just-In-Time bindings**). A Guice module is a unit of configuration logic that adds things into the Guice map. There are two ways to do this:
> 1. Adding method annotations like `@Provides`
> 2. Using the Guice Domain Specific Language (DSL).

#### 2. Injection
> You don't pull things out of a map, you declare that you need them. This is the essence of dependency injection. If you need something, you don't go out and get it from somewhere, or even ask a class to return you something. Instead, you simply declare that you can't do your work without it, and rely on Guice to give you what you need.

即告诉 Guice 我的程序需要什么对象，Guice 负责创建并返回这个对象。


### Dependencies form a graph
> Dependencies form a directed graph, and injection works by doing a depth-first traversal of the graph from the object you want up through all its dependencies.


## Scopes
> By default, Guice returns a new instance each time it supplies a value. This behaviour is configurable via scopes. Scopes allow you to reuse instances: for the lifetime of an application, a session, or a request.

### Built-in scopes
#### 1. Singleton
> Guice comes with a built-in @Singleton scope that reuses the same instance during the lifetime of an application within a single injector.
> ✅ Both javax.inject.Singleton and com.google.inject.Singleton are supported by Guice, but prefer the standard javax.inject.Singleton since it is also supported by other injection frameworks like Dagger.

#### 2. RequestScope
> The servlet extension includes additional scopes for web apps such as `@RequestScoped`.


### Applying scopes
> Guice uses annotations to identify scopes. Specify the scope for a type by applying the scope annotation to the implementation class. As well as being functional, this annotation also serves as documentation.
> Guice 通过是用**注释**识别范围。

1. `@Singleton` indicates that the class is intended to be threadsafe.
   ```java
   @Singleton
   public class InMemoryTransactionLog implements TransactionLog {
      /* everything here should be threadsafe! */
   }
   ```
2. Scopes can also be configured in bind statements:
   ```java
   bind(TransactionLog.class).to(InMemoryTransactionLog.class).in(Singleton.class);
   ```
3. Scopes can also be configured annotating @Provides methods::
   ```java
   @Provides @Singleton
   TransactionLog provideTransactionLog() {
       ...
   }
   ```

> If there's conflicting scopes on a type and in a bind() statement, the bind() statement's scope will be used. If a type is annotated with a scope that you don't want, bind it to Scopes.NO_SCOPE.


### Eager Singletons

### Choosing a scope
> If the object is stateful, the scoping should be obvious. Per-application is @Singleton, per-request is @RequestScoped, etc. If the object is stateless and inexpensive to create, scoping is unnecessary. Leave the binding unscoped and Guice will create new instances as they're required.

Singletons are most useful for:
* stateful objects, such as configuration or counters
* objects that are expensive to construct or lookup
* objects that tie up resources (占用资源的对象), such as a database connection pool 

### Scopes and Concurrency
> Classes annotated `@Singleton` and `@SessionScoped` must be threadsafe. Everything that's injected into these classes must also be threadsafe. Minimize mutability to limit the amount of state that requires concurrency protection.

### Using `NO_SCOPE` in tests
> If you are testing a Guice module that uses scopes (especially custom scopes) but don't actually care about the scoping of the binding in the tests, you can use Guice's `Scopes.NO_SCOPE` to override a specific scope. `NO_SCOPE` is an implementation of Scope that returns a new instance every time an object is requested.

# Reference
* [Google Guice Wiki](https://github.com/google/guice/wiki)
* TutorialsPoint: [Google Guice -- Constructor Injection](https://www.tutorialspoint.com/guice/guice_constructor_injection.htm)