# Errors

### 1. Guice/RecursiveBinding
```
Exception in thread "main" com.google.inject.CreationException: Unable to create injector, see the following errors:

1) [Guice/RecursiveBinding]: Binding points to itself. Key: TransactionLog
  at BillingServiceModule.configure(BillingServiceModule.java:14)
```
This Guice error: `[Guice/RecursiveBinding]: Binding points to itself. Key: TransactionLog` means that in your Guice module (BillingServiceModule.java), you accidentally configured a binding where TransactionLog depends on itself—either directly or indirectly, causing infinite recursion.

**How to fix it:**
1. You need to correct the binding to point to the appropriate implementation class (or provider).
    ```java
    bind(TransactionLog.class).to(DatabaseTransactionLog.class);
    ```
2. Or if you're using a provider:
    ```java
    bind(TransactionLog.class).toProvider(DatabaseTransactionLogProvider.class);
    ```
3. Or **if TransactionLog is itself a concrete class** and doesn't need binding at all, you can simply remove the binding and let Guice construct it automatically.