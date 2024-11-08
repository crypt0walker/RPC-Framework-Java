# RPC-Framework-Java

构建一个简易的Java-RPC框架，逐渐完善并优化功能。

- 构建一个基本的RPC调用——√
- 引入Netty网络应用框架——√

# 1. 实现一个基本的RPC调用

## 1.1 需求分析

**背景**：进程A与进程B位于不同服务器，此时A想要远程调用B的某方法；

## 1.2 客户端代码结构

### ClientProxy 类

#### 代码逻辑

`ClientProxy` 是一个动态代理类，利用 Java 的反射机制来拦截对接口方法的调用。其核心逻辑如下：

1. **动态代理创建**：实现 `InvocationHandler` 接口，重写 `invoke` 方法。
2. **请求构建**：在 `invoke` 方法中，构建一个 `RpcRequest` 对象，包含接口名、方法名、参数等信息。
3. **请求发送**：使用 `IOClient` 发送 `RpcRequest` 到服务端，等待服务端的响应。
4. **响应处理**：将从服务端接收到的 `RpcResponse` 对象返回给调用者。

#### 主要功能

- **透明化远程调用**：用户在调用远程服务时，无需关心具体的网络细节，使用本地方法调用的方式，简化开发。
- **请求构建和发送**：负责请求的构建、序列化和发送，提升了代码的清晰度和可维护性。
- **异常处理**：可以在发送请求和处理响应的过程中捕获异常，提高了代码的健壮性。

### IOClient 类

#### 代码逻辑

`IOClient` 负责与服务端进行网络通信，核心逻辑如下：

1. **Socket 创建**：通过 `new Socket(host, port)` 方法创建与服务端的连接。
2. **输入输出流**：创建 `ObjectOutputStream` 和 `ObjectInputStream` 用于对象的序列化和反序列化。
3. **发送请求**：使用 `oos.writeObject(request)` 将 `RpcRequest` 对象发送到服务端。
4. **接收响应**：使用 `ois.readObject()` 接收服务端返回的 `RpcResponse`，并进行类型转换。
5. **异常处理**：在通信过程中捕获并处理 `IOException` 和 `ClassNotFoundException`。

#### 主要功能

- **底层通信管理**：负责管理与服务端的网络连接和数据传输，封装了通信细节。
- **对象序列化**：支持将请求和响应对象进行序列化和反序列化，使得网络传输更为高效。
- **错误处理**：通过捕获异常，确保在通信出现问题时，能够返回相应的错误信息或处理逻辑。

### TestClient 类

#### 代码逻辑

`TestClient` 用于测试客户端与服务端的功能，主要逻辑包括：

1. **创建请求对象**：根据需要调用的服务构建一个或多个 `RpcRequest` 对象。
2. **调用 IOClient**：利用 `IOClient.sendRequest(host, port, request)` 发送请求到服务端。
3. **处理响应**：接收 `RpcResponse`，并根据返回的数据进行打印或其他处理。

#### 主要功能

- **功能验证**：验证客户端与服务端之间的通信是否正常，确保请求能正确发送并接收响应。
- **调试工具**：作为调试工具，便于开发者快速测试服务的可用性，检查返回结果。
- **示例代码**：提供示例用法，帮助其他开发者了解如何构建请求和处理响应。

### 整体流程

1. **方法调用**：用户通过 `ClientProxy` 调用远程服务的某个方法。
2. **请求构建**：`ClientProxy` 在 `invoke` 方法中构建 `RpcRequest` 对象，设置必要的字段（如接口名、方法名、参数）。
3. **请求发送**：调用 `IOClient.sendRequest` 方法，将请求发送到指定的服务端。
4. **响应接收**：服务端处理请求后，将结果封装在 `RpcResponse` 中并发送回客户端。
5. **结果返回**：`ClientProxy` 接收响应，提取结果并返回给调用者，完成整个调用过程。

这种设计使得客户端的调用逻辑清晰，网络通信细节被有效封装，提升了系统的整体可维护性和可扩展性。

根据你提供的项目结构图和之前的代码内容，这个项目是一个典型的RPC（远程过程调用）系统。下面是对整个项目结构的解释和各部分的关联关系：

## 1.3 服务端代码结构

从您提供的代码和文件结构来看，服务端代码主要由几个部分组成，每个部分都承担着RPC服务中的关键角色：

### 1.3.1 RpcServer 接口

- **定义**：这是一个简单的接口，定义了RPC服务器应有的两个基本功能：`start` 和 `stop`。
- **作用**：`start(int port)` 方法用于启动服务器并监听指定端口上的客户端连接。`stop()` 方法用于停止服务器。

### 1.3.2 SimpleRpcServerImpl 类

- **实现 RpcServer 接口**：这个类实现了 `RpcServer` 接口，具体实现了服务器的启动和停止逻辑。
- **服务器逻辑**：
  - **`start(int port)`**：创建一个 `ServerSocket` 并绑定到指定的端口，然后进入一个无限循环，不断接受来自客户端的连接。对于每个连接，它创建一个新的 `WorkThread` 线程来处理。
  - **无 `stop()` 实现**：当前版本中 `stop()` 方法是空的，实际应用中可能需要实现安全关闭服务器的逻辑。

### 1.3.3 WorkThread 类

- **职责**：负责处理单个客户端请求。
- **工作流程**：
  - 使用 `ObjectInputStream` 读取客户端发送的 `RpcRequest`。
  - 调用 `getResponse` 方法处理请求，此方法通过 `ServiceProvider` 查找并调用相应的服务。
  - 使用 `ObjectOutputStream` 将处理结果 `RpcResponse` 发回客户端。

### 1.3.4 ServiceProvider 类

- **功能**：本地服务注册和查找。
- **工作原理**：
  - **注册服务**：`provideServiceInterface` 方法允许注册服务实例及其接口，这样服务就可以通过接口名被查找和调用。
  - **获取服务**：`getService` 方法根据接口名返回已注册的服务实例。

### 1.3.5 整体工作流程

1. **启动服务器**：`SimpleRpcServerImpl` 的 `start` 方法启动服务器，监听指定端口。
2. **接收连接**：对于每个接入的客户端连接，创建一个新的 `WorkThread` 线程处理请求。
3. **请求处理**：
   - `WorkThread` 读取并解析客户端发送的 `RpcRequest`。
   - 使用 `ServiceProvider` 查找请求中指定的服务接口对应的实例。
   - 反射调用服务实例的方法，并捕获执行结果。
4. **响应返回**：将执行结果包装成 `RpcResponse`，回传给客户端。

### 1.3.6 实现细节

- **反射调用**：`WorkThread` 中的 `getResponse` 使用 Java 反射机制动态调用服务方法，这为RPC框架提供了强大的灵活性，允许在运行时调用任何已注册的服务。
- **多线程处理**：通过为每个客户端连接创建独立线程来处理请求，这样可以提高服务的并发处理能力。

这个设计允许服务端高效地管理和处理来自不同客户端的请求，确保了RPC调用的高效性和灵活性。

## 1.4 Common代码结构

在您的 `common` 包中，代码结构被设计为通用组件，这意味着这些组件可以被服务端和客户端共同使用，以确保双方在通信中对数据格式和服务接口有一致的理解和实现。下面是 `common` 包中各个组件的详细解释和它们之间的关联：

### 1.4.1 消息定义 (`message` 包)

#### RpcRequest

- **作用**：定义了 RPC 调用的请求格式。
- **字段**：
  - `interfaceName`：服务接口名，指明请求针对的是哪个服务。
  - `methodName`：要调用的方法名称。
  - `params`：调用方法时传递的参数数组。
  - `paramsType`：参数的类型数组，用于反射中准确地定位方法。

#### RpcResponse

- **作用**：定义了 RPC 调用的响应格式。
- **字段**：
  - `code`：状态码，类似于 HTTP 状态码，如 `200` 表示成功，`500` 表示错误。
  - `message`：状态信息，提供响应的附加信息或错误信息。
  - `data`：具体的响应数据，可以是任何由服务方法返回的对象。
- **方法**：
  - `success(Object data)`：创建一个表示成功的响应。
  - `fail()`：创建一个表示失败的响应，通常在处理请求时发生错误时使用。

### 1.4.2 数据模型 (`pojo` 包)

#### User

- **作用**：定义了一个用户数据模型，这是服务间传递的一个共用对象。
- **字段**：
  - `id`：用户的标识符。
  - `userName`：用户的名称。
  - `gender`：用户的性别。
- **构造器**：使用 Lombok 提供的注解 `@Builder`、`@Data`、`@NoArgsConstructor` 和 `@AllArgsConstructor` 来简化类的构建和使用。

### 1.4.3 服务接口 (`server` 包)

#### UserService

- **作用**：定义了用户服务的接口，这些方法是客户端可以远程调用的方法。
- **方法**：
  - `getUserByUserId(Integer id)`：根据用户 ID 获取用户详情。
  - `insertUserId(User user)`：插入一个新用户并返回 ID，展示了服务可以有创建或更新数据的能力。

### 1.4.4 服务实现（暂时错误地放在 `common` 下）

#### UserServiceImpl

- **作用**：实现了 `UserService` 接口，提供了方法的具体逻辑。
- **注意**：这个实现类不应该放在 `common` 包中。通常服务实现应放在服务端特定的包中，因为它包含了业务逻辑的具体实现，而不应被客户端直接访问。这里的放置可能是一个结构上的错误，应该移到服务端的 `server` 包下的 `impl` 子包中。

### 总结

您的 `common` 包设计得很好，使得服务端和客户端可以共享数据模型和接口定义，但需要调整服务实现类的位置以遵循良好的软件开发实践。确保 `common` 仅包含双方共享的元素，而具体的业务逻辑实现则应放在服务端的命名空间中。

## 1.5 整体代码结构

![simple_RPC](H:\Java\project\RPC-java\simple_RPC\simple_RPC.png)

### 1.5.1 **com.async.rpc 包结构**

这个项目被组织在 `com.async.rpc` 包中，包含了客户端、服务端和公共部分的代码。

#### A. **client (客户端)**

- **proxy**
  - **ClientProxy**: 动态代理类，用于创建服务接口的代理对象。通过这些代理对象，客户端可以像调用本地方法一样调用远程服务。
  - **IOClient**: 负责网络通信，即发送序列化的 `RpcRequest` 和接收 `RpcResponse`。
  - **TestClient**: 用于测试和演示如何使用客户端的类。

#### B. **common (公共部分)**

- **message**
  - **RpcRequest**: 封装了RPC调用的请求信息，比如调用哪个接口的哪个方法，参数是什么。
  - **RpcResponse**: 封装了RPC调用的响应信息，包括返回结果或错误信息。
- **pojo**
  - **User**: 可能是一个普通的POJO（Plain Old Java Object），用于在RPC调用中传输用户信息。

#### C. **server (服务器端)**

- **impl**
  - **UserServiceImpl**: 实现了 `UserService` 接口的类，定义了具体的业务逻辑。
- **provider**
  - **ServiceProvider**: 服务提供注册中心，管理和提供注册的服务实例。
- **work**
  - **WorkThread**: 可能是处理接收到的请求的线程类。
- **RpcServer**: 负责监听网络请求，接收客户端的调用。
- **TestServer**: 服务器端的测试启动类，用于启动RPC服务。

### 1.5.2 **关联关系**

- **客户端 (Client)** 使用 `ClientProxy` 生成特定服务的代理对象，通过这些代理对象发起RPC调用。
- **服务请求** 通过 `IOClient` 发送到服务器，请求包含在 `RpcRequest` 中。
- **服务器 (Server)** 接收请求后，通过 `ServiceProvider` 查找具体的服务实例（如 `UserServiceImpl`），然后调用相应的方法处理请求。
- **处理结果** 被封装在 `RpcResponse` 中，通过服务器的 `RpcServer` 和工作线程 `WorkThread` 发回给客户端。
- **客户端** 接收响应，并通过其代理对象的调用返回结果给调用者。

### 1.5.3 **执行流程**

1. **启动**：`TestServer` 启动服务端，`TestClient` 启动客户端。
2. **调用**：客户端通过代理对象调用方法。
3. **处理**：服务器端接收请求，查找服务，执行逻辑，返回结果。
4. **响应**：客户端接收结果，处理输出。

这个项目通过分层的结构清晰地区分了客户端和服务器端的职责，同时通过公共部分（`common`）连接两端，确保了RPC调用的一致性和可维护性。

## 1.6 代码问题

抱歉让你感到困惑，让我们简化一下解释，更直接地联系到你之前提到的代码：

### 如何理解和使用 Java 反射处理 RPC 请求

在你的 `WorkThread` 类中，服务器端使用反射来动态处理来自客户端的 RPC 请求。这里，我们将分解代码的主要部分，让每一步都易于理解：

#### 1. **获取方法和执行方法**

```java
// 从服务提供者中获取服务实例
Object service = serviceProvider.getService(rpcRequest.getInterfaceName());

// 使用反射获取服务类中的方法
Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());

// 调用该方法并传递参数
Object result = method.invoke(service, rpcRequest.getParams());
```

**解释**：

- **获取服务实例**：`getService(interfaceName)` 从注册的服务中找到客户端请求对应的服务对象。
- **反射获取方法**：`getMethod()` 根据客户端请求提供的方法名和参数类型，从服务实例中找到对应的方法。
- **执行方法**：`invoke()` 调用找到的方法，并将从客户端接收到的参数传递给该方法。

#### 2. **封装响应**

```java
return RpcResponse.success(result);
```

**解释**：

- 如果方法调用成功，`result` 将是方法执行的返回值。这个返回值被包装在一个 `RpcResponse` 对象中，标记为成功，并将结果发送回客户端。



### RpcRequest和RpcResponse为什么要实现Serializable接口

在Java中，`Serializable` 接口用于允许一个类的对象可以被序列化和反序列化。序列化是指将对象的状态转换为可以存储或传输的形式，通常是为了在网络之间发送对象或者将其保存到磁盘上。反序列化则是将这种形式重新构建回对象。

在RPC（远程过程调用）框架中，`RpcRequest` 和 `RpcResponse` 对象需要在网络之间进行传输。例如：

- **`RpcRequest`**：客户端需要向服务端发送请求对象，该对象包含了要调用的接口名、方法名、参数列表等信息。这个对象在客户端生成后，需要被序列化成流（如字节流），以便通过网络发送到服务端。
- **`RpcResponse`**：服务端处理完客户端的调用请求后，会生成一个响应对象，包含了方法执行的结果（如返回值、状态码、错误信息等）。这个响应对象同样需要被序列化后发送回客户端。

实现`Serializable`接口允许这两种对象在客户端和服务端之间通过网络进行有效的序列化和反序列化传输。这是实现分布式系统中对象远程通信的基本要求之一。如果不实现此接口，Java 默认的序列化机制将无法应用，导致无法将对象状态转换为流，从而无法通过网络发送这些对象。

### 使用接口名作为服务类名

在RPC（远程过程调用）框架中，使用接口名作为服务类名的设计选择是由几个关键原因驱动的，特别是在使用动态代理的情况下：

1. **抽象和解耦**：在许多设计模式和高级编程实践中，接口被用来定义一个类（实现）必须遵循的方法和行为的模板，而不暴露这个类的内部实现细节。这样做可以使客户端代码与服务器端的具体实现解耦，只依赖于接口。这意味着服务的实现可以变化（如可以被更新或重构），只要它们遵循相同的接口，客户端代码无需更改。

2. **动态代理**：动态代理是一种强大的Java特性，允许在运行时创建一个实现了一或多个接口的对象。这个代理对象可以在调用任何方法时，实现一些自定义行为（如网络调用），然后可能（或可能不）将调用转发到一个实际的对象。在RPC框架中，动态代理被用来创建服务接口的代理实例。当这些接口的方法被调用时，代理将这些调用封装成网络请求，而不是本地方法调用。

3. **网络调用的封装**：使用接口名作为服务类名允许RPC框架封装方法调用为网络请求。当你调用一个接口方法时，你并不需要知道这个方法是在本地执行还是远程执行，动态代理隐藏了这些细节。代理会自动构建一个`RpcRequest`对象，包含接口名、方法名和参数等信息，然后通过网络发送到服务端。服务端接收到这个请求后，解析接口名和方法名，找到相应的服务对象，执行方法，并将结果封装在`RpcResponse`对象中返回给客户端。

因此，使用接口名作为服务类名是为了利用Java的接口和动态代理特性，以简化远程服务调用的开发和维护，同时提供清晰的API界面给客户端开发者。这种方式增强了系统的可维护性和可扩展性。



### 为什么ClientProxy 要实现innovation handler

`InvocationHandler` 是Java中的一个接口，它是动态代理的核心部分。在Java动态代理机制中，`InvocationHandler` 用来定义当动态代理对象的方法被调用时，应该执行什么操作。

**为什么 `ClientProxy` 要实现 `InvocationHandler`？**

1. **动态处理方法调用**：`ClientProxy` 实现 `InvocationHandler` 后，可以在其 `invoke` 方法中自定义处理逻辑，这样，当代理对象的任何方法被调用时，实际上是执行 `invoke` 方法中的代码。

2. **转换本地调用为远程调用**：在RPC框架中，`ClientProxy` 的 `invoke` 方法负责将对接口的本地方法调用转换成网络请求，发送到远程服务器执行，然后将执行结果返回给调用者。

这种设计允许你使用一个代理对象透明地进行远程服务调用，而不必关心网络通信的细节。

### 为什么getProxy中泛型是Class<T>而不是<T>？

- **`Class<T>`**：表示一个具体类型的 `Class` 对象，用于获取该类型的元数据，如类名、方法、字段等。通过传入 `Class<T>`，你可以告诉方法你希望创建哪个接口的代理。
- **`<T>`**：只是一个类型参数的声明，单独使用并没有包含任何具体类型的信息。

### getProxy获取代理对象的解释

好的，这里是简洁版的解释：

#### 代码解析

```java
Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
```

1. **创建动态代理**：使用 `Proxy.newProxyInstance` 创建一个代理对象。
2. **参数解释**：
   - **`clazz.getClassLoader()`**：获取接口的类加载器。
   - **`new Class[]{clazz}`**：指定要代理的接口。
   - **`this`**：当前 `ClientProxy` 实例，处理方法调用。
3. **返回代理对象**：该代理对象实现了指定接口，方法调用会转发到 `ClientProxy` 的 `invoke` 方法。

#### 功能总结

当调用代理对象的方法时，`ClientProxy` 会处理请求并与远程服务通信。



# 2. 引入netty框架

## Netyy是什么？

> 见RPC前置知识，以及详细原理讲解https://blog.csdn.net/qq_35190492/article/details/113174359?spm=1001.2014.3001.5506 

#### Netty 简介

Netty 是一个基于 Java 的高性能、异步的网络应用框架，广泛用于开发服务器端和客户端的通信应用。它封装了 Java NIO 的底层复杂性，为构建高性能、高并发的网络程序提供了简便的 API 和工具。Netty 特别适用于构建高吞吐量、低延迟的网络应用，例如分布式系统中的服务通信、游戏服务器、消息中间件等。

#### Netty 的特点

1. **异步和事件驱动**：Netty 采用异步非阻塞的 I/O 模型，基于事件驱动机制来处理请求。这样可以充分利用系统资源，减少线程阻塞，提高并发性能。

2. **高性能**：Netty 对 Java NIO 进行了大量优化，拥有更高效的内存管理机制，可以轻松处理成千上万的并发连接。

3. **易于使用**：Netty 提供了高级 API 屏蔽底层的 NIO 操作，使得编写网络应用变得简单明了，便于维护和扩展。

4. **灵活的协议支持**：Netty 支持多种协议（如 HTTP、WebSocket、TCP、UDP 等），并允许开发者轻松自定义协议。

5. **跨平台支持**：Netty 支持跨平台，可以在 Linux、Windows 等多个操作系统上高效运行。

#### Netty 的核心组件

1. **Channel**：代表一个网络连接，支持异步 I/O 操作，例如读写数据和连接关闭。

2. **EventLoop**：负责管理 Channel 的 I/O 事件处理，通常在单线程上运行，确保 I/O 操作的线程安全。

3. **ChannelFuture**：用于监听异步操作的结果，例如连接、读写操作是否成功。

4. **ChannelHandler 和 ChannelPipeline**：Netty 中的事件处理器，ChannelHandler 用于处理 I/O 事件，如接收数据、编码、解码，所有的处理器会按照顺序放入 ChannelPipeline 中形成处理链。

5. **Bootstrap 和 ServerBootstrap**：帮助类，用于简化客户端和服务器的启动配置。

#### Netty 的工作流程

1. **启动**：通过 `Bootstrap` 或 `ServerBootstrap` 配置服务端或客户端的启动参数和管道。
2. **事件循环**：Netty 的事件循环会监听 Channel 上的事件，将不同的事件传递给对应的 `ChannelHandler` 进行处理。
3. **处理链**：数据在 `ChannelPipeline` 中的 `ChannelHandler` 之间传递，每个 `ChannelHandler` 都可以对数据进行编码、解码或其他处理。
4. **结果回调**：Netty 通过 `ChannelFuture` 提供异步操作的回调，确保事件完成时可以执行后续逻辑。

### 引入依赖

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.51.Final</version>
    <scope>compile</scope>
</dependency>
```

## 客户端重构

客户端的Netty方式重构主要集中在几个点：

- 以Netty的方式实现RpcClient接口

  <img src="./C:/Users/16232/AppData/Roaming/Typora/typora-user-images/image-20241108215119277.png" alt="image-20241108215119277" style="zoom:50%;" />

  - 这里我们做了优化，同时实现了一个简单Socket类型的对RpcClient接口的实现，以方便客户端选择使用那种类型（choose字段）
  - 由于以上，所以同时也需要对ClientProxy进行重构，更改构造方法以及消息处理的写法（见后续）

- 编写NettyClientInitializer，这是配置netty对消息的处理机制，如编码器、解码器、消息格式等（设置handler）

- 编写NettyClientHandler，这是指定netty对接收消息的处理方式。

### Netty方式实现RpcClient接口

这一步的目的是重写原位于ClientProxy中invoke方法内的

<img src="./C:/Users/16232/AppData/Roaming/Typora/typora-user-images/image-20241108215533427.png" alt="image-20241108215533427" style="zoom:50%;" />

首先对传输的类定义接口RpcClient

好处是：在ClientProxy中用接口定义一个传输类属性，可以灵活选择不同方式的传输类，耦合性低。

```java
public interface RpcClient {
    //定义底层通信的方法
    RpcResponse sendRequest(RpcRequest request);
}
```

传输类NettyRpcClient类 实现接口（其实就是原IOClient中的内容改造）

增加了Netty的相关配置：

- Bootstrap：Netty 的客户端启动引导类，用于配置客户端
- EventLoopGroup：事件循环组，用于处理所有IO操作的线程池组，提供包括NIO、epoll、BIO等不同模型

其中需要对bootstrap绑定eventLoopGroup，设定channel类型（NIO）和netty客户端的处理器（即nettyClientInitializer）

> 关于Netty使用中各字段含义可见RPC前置知识

```java
package com.async.rpc.client.rpcClient.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

/**
 * @program: simple_RPC
 *
 * @description: 使用netty的发送方法实现类
 **/
public class NettyRpcClient implements RpcClient {
    private String host;
    private int port;
    // Netty 的客户端启动引导类，用于配置客户端
    private static final Bootstrap bootstrap;
    // 事件循环组，用于处理网络事件
    //EventLoopGroup是Netty的channel包组件，用于处理所有IO操作的线程池组，提供包括NIO、epoll、BIO等不同模型。
    private static final EventLoopGroup eventLoopGroup;
    public NettyRpcClient(String host,int port){
        this.host=host;
        this.port=port;
    }
    //netty客户端初始化
    static {
        // 创建一个 NIO 事件循环组
        eventLoopGroup = new NioEventLoopGroup();
        // 创建 Bootstrap 实例
        bootstrap = new Bootstrap();
        //将eventLoopGroup绑定到bootstrap，并且设定channel类型和netty客户端的处理器
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                //NettyClientInitializer这里 配置netty对消息的处理机制
                //即使用nettyClientInitializer来设置处理器链
                .handler(new NettyClientInitializer());
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            //创建一个channelFuture对象用于监控操作执行情况，代表这一个操作事件，sync方法表示阻塞直到connect完成
            ChannelFuture channelFuture  = bootstrap.connect(host, port).sync();
            //channel表示一个连接的单位，类似socket
            Channel channel = channelFuture.channel();
            // 通过channel发送数据
            channel.writeAndFlush(request);
            // sync() 等待通道关闭，确保数据完全发送
            channel.closeFuture().sync();
            // 阻塞的获得结果，通过给channel设计别名，获取特定名字下的channel中的内容（这个在handlder中设置）
            // AttributeKey是，线程隔离的，不会由线程安全问题。
            // 当前场景下选择堵塞获取结果
            // 其它场景也可以选择添加监听器的方式来异步获取结果 channelFuture.addListener...
            // 使用 AttributeKey 从通道的上下文中获取响应数据
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
            // 获取存储在通道属性中的响应对象
            RpcResponse response = channel.attr(key).get();
            System.out.println(response);
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

```

同样以简单Socket的形式实现RpcClient接口（这样就不使用原IOClient文件了）

```java
package com.async.rpc.client.rpcClient.impl;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */
/**
 * @program: simple_RPC
 *
 * @description: 使用Socket的发送方法实现类，替代原IOClient功能
 **/
public class SimpleSocketRpcCilent implements RpcClient {
    private String host;
    private int port;
    public SimpleSocketRpcCilent(String host, int port){
        this.host=host;
        this.port=port;
    }
    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        try {
            // 创建与服务端的 Socket 连接
            Socket socket = new Socket(host, port);
            // 创建输出流，用于发送请求对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            // 创建输入流，用于接收响应对象
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            // 发送 RpcRequest 对象（写入缓冲区）
            oos.writeObject(request);
            oos.flush(); // 使缓冲区中数据发送
            // 从服务端接收 RpcResponse 对象，并进行类型转换
            RpcResponse response = (RpcResponse) ois.readObject();
            return response; // 返回响应
        } catch (IOException | ClassNotFoundException e) {
            // 捕获异常，打印堆栈跟踪信息
            e.printStackTrace();
            return null; // 返回 null 表示出错
        }
    }
}

```

### NettyClientInitializer类——配置netty对**消息的处理机制**

- 指定编码器（将消息转为字节数组），解码器（将字节数组转为消息）

- 指定消息格式，消息长度，解决**沾包问题**
  - 什么是沾包问题？
  - netty默认底层通过TCP 进行传输，TCP**是面向流的协议**，接收方在接收到数据时无法直接得知一条消息的具体字节数，不知道数据的界限。由于TCP的流量控制机制，发生沾包或拆包，会导致接收的一个包可能会有多条消息或者不足一条消息，从而会出现接收方少读或者多读导致消息不能读完全的情况发生
  - 在发送消息时，先告诉接收方消息的长度，让接收方读取指定长度的字节，就能避免这个问题
- 指定对接收的消息的处理handler——NettyClientHandler

注：这里的addLast没有先后顺序，netty通过加入的类实现的**接口**来自动识别类实现的是什么功能

```java
package com.async.rpc.client.netty.nettyInitializer;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

/**
 * @program: simple_RPC
 *
 * @description: 配置netty对消息的处理机制，如编码器、解码器、消息格式等（设置handler）
 *
 **/
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    // 初始化通道并配置处理器链
    protected void initChannel(SocketChannel ch) throws Exception {
        // 获取通道的管道，用于依次添加处理器
        ChannelPipeline pipeline = ch.pipeline();
        // 添加一个长度字段解码器，解决 TCP 粘包/拆包问题
        pipeline.addLast(
                //基于消息长度字段的解码器。它在每条消息的头部增加一个长度字段，接收方可以通过读取长度字段，知道需要读取的字节数。
                new LengthFieldBasedFrameDecoder(
                        Integer.MAX_VALUE, // 数据帧的最大长度，防止因数据过大导致内存溢出
                        0,                 // 长度字段的起始偏移量，0 表示从数据帧的开头开始
                        4,                 // 长度字段的长度，表示长度字段占用4个字节
                        0,                 // 长度调整量，不调整数据帧的长度
                        4                  // 跳过前4个字节，即长度字段的长度，不包含在数据帧内容中
                )
        );
        //计算当前待发送消息的长度，写入到前4个字节中
        // 添加一个长度字段前置器，在消息头部插入消息长度字段
        // 用于在发送时自动在消息头部添加4个字节的长度字段
        pipeline.addLast(new LengthFieldPrepender(4));

        // 使用 Java 序列化方式的编码器，将 Java 对象编码成字节流以便传输，netty的自带的解码编码支持传输这种结构
        pipeline.addLast(new ObjectEncoder());
        //使用了Netty中的ObjectDecoder，它用于将字节流解码为 Java 对象。
        //在ObjectDecoder的构造函数中传入了一个ClassResolver 对象，用于解析类名并加载相应的类。
        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                // 使用反射机制根据类名加载类
                return Class.forName(className);
            }
        }));
        // 添加客户端业务逻辑处理器，处理接收到的响应
        pipeline.addLast(new NettyClientHandler());
    }
}
```

### NettyClientHandler——指定对接收消息的处理方式

```java
package com.async.rpc.client.netty.handler;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

/**
 * @program: simple_RPC
 *
 * @description: 指定netty对接收消息的处理方式
 **/
// 客户端业务逻辑处理器，用于接收和处理来自服务器的响应
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    // 当通道读取到数据时调用
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        // 使用 AttributeKey 将响应对象存储在通道的属性中，供后续获取
        // 接收到response, 给channel设计别名，让sendRequest里读取response
        AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
        // 将服务器返回的响应对象存储在通道属性中
        ctx.channel().attr(key).set(response);
        // 关闭通道，表示处理完成
        ctx.channel().close();
    }
//// 捕获并处理异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常处理
        cause.printStackTrace();
        // 关闭通道，释放资源
        ctx.close();
    }
}
```

### SocketChannel

`SocketChannel` 是 Netty 中表示客户端与服务器之间 TCP 连接的通道。每一个 `SocketChannel` 对象都代表一个网络连接，它是 Java NIO 中 `SocketChannel` 的扩展，用于更高效和灵活的网络通信。在 Netty 中，它是一个核心组件，负责在应用程序和底层网络之间进行数据传输。

- **特点**：
  - **双向通信**：`SocketChannel` 既可以读数据（接收数据），也可以写数据（发送数据）。
  - **非阻塞 I/O**：`SocketChannel` 支持异步和非阻塞 I/O 操作，可以在不阻塞线程的情况下处理大量连接。
  - **事件驱动**：`SocketChannel` 的所有 I/O 操作都可以通过事件来触发，因此不必一直等待连接的数据到达或数据完全发送，极大提高了性能。

- **典型应用场景**：
  - 在客户端应用程序中，`SocketChannel` 表示客户端与服务器之间的一个 TCP 连接。
  - 在服务器应用程序中，每当接收到一个新的连接请求，Netty 都会为这个连接创建一个新的 `SocketChannel` 实例。

在您的代码中，`NettyClientInitializer` 的泛型指定为 `SocketChannel`，表示它会为每个新的 `SocketChannel` 连接配置相应的处理器。

```java
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    // 初始化通道并配置处理器链
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 处理器配置代码
    }
}
```

### ChannelPipeline

`ChannelPipeline` 是一个用于组织和管理处理器（Handler）的链，通常称为处理器链（Pipeline）。它可以看作是一个数据处理的流水线，将数据经过一系列的处理器逐步处理后传递到应用程序中。

- **作用**：
  - **管理数据流的处理器链**：Netty 中所有的数据处理都需要经过 `ChannelPipeline`。每当数据到达 `SocketChannel` 或从 `SocketChannel` 发送出去时，都会经过 `ChannelPipeline` 中的各个处理器进行处理。
  - **动态调整处理器链**：`ChannelPipeline` 支持在应用程序运行时动态添加、移除或替换处理器，从而极大地提高了系统的灵活性。

- **工作流程**：
  - 当数据被接收到时，它会从 `ChannelPipeline` 的头部依次经过每个 `Inbound Handler` 处理器。
  - 发送数据时，它会从 `ChannelPipeline` 的尾部依次经过每个 `Outbound Handler` 处理器。

在您的代码中，`ChannelPipeline` 被用来设置多个处理器（编码器、解码器和业务逻辑处理器），这些处理器会对进入或离开 `SocketChannel` 的数据进行处理。

```java
ChannelPipeline pipeline = ch.pipeline();
```

#### ChannelPipeline` 中的处理器配置示例

在代码中，`NettyClientInitializer` 配置了 `ChannelPipeline`，并通过 `pipeline.addLast(...)` 的方式将多个处理器按顺序加入到管道中，处理数据的编码、解码以及业务逻辑。

- **LengthFieldBasedFrameDecoder**：处理从 `SocketChannel` 读取的数据，用于防止 TCP 的粘包/拆包问题。
- **LengthFieldPrepender**：在数据发送之前，添加长度字段，方便接收端知道数据的边界。
- **ObjectEncoder** 和 **ObjectDecoder**：负责将 Java 对象编码成字节流或将字节流解码为 Java 对象。
- **NettyClientHandler**：这是客户端的业务逻辑处理器，处理接收到的数据并返回相应的结果。

#### `ChannelPipeline` 配置代码示例

```java
// 获取通道的管道，用于依次添加处理器
ChannelPipeline pipeline = ch.pipeline();

// 添加解码器，解决 TCP 粘包/拆包问题
pipeline.addLast(new LengthFieldBasedFrameDecoder(...));

// 添加编码器，在消息头部加入长度字段
pipeline.addLast(new LengthFieldPrepender(4));

// 对象编码和解码器，用于序列化/反序列化 Java 对象
pipeline.addLast(new ObjectEncoder());
pipeline.addLast(new ObjectDecoder(new ClassResolver() {
    @Override
    public Class<?> resolve(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}));

// 添加业务处理器，处理从服务器接收到的响应
pipeline.addLast(new NettyClientHandler());
```

#### 总结

- **`SocketChannel`**：代表客户端和服务器之间的 TCP 连接，用于传输数据。
- **`ChannelPipeline`**：用于管理和组织 `SocketChannel` 的处理器链，所有数据的编码、解码和处理逻辑都在 `ChannelPipeline` 中按顺序进行。

通过 `SocketChannel` 和 `ChannelPipeline` 的结合，Netty 提供了高度灵活和高性能的数据处理框架，使得应用程序可以在高并发场景下进行高效的网络通信。

### ChannelHandlerContext

`ChannelHandlerContext` 是 Netty 中的一个关键接口，用于表示 `ChannelHandler` 和 `ChannelPipeline` 之间的关联。它充当了 `Channel` 和 `ChannelHandler` 之间的桥梁，允许 `ChannelHandler` 与管道（`ChannelPipeline`）或通道（`Channel`）进行交互，从而实现对数据的处理、事件的传递和资源的管理。

#### 作用和功能

在 Netty 中，`ChannelHandlerContext` 提供了以下几方面的功能：

1. **触发事件**：
   - `ChannelHandlerContext` 允许一个 `ChannelHandler` 主动触发下一个 `ChannelHandler` 处理特定事件。例如，调用 `ctx.write()` 可以触发出站数据的处理，调用 `ctx.fireChannelRead()` 可以将数据传递给下一个入站处理器。

2. **访问通道（Channel）**：
   - `ChannelHandlerContext` 提供了对所属 `Channel` 的引用，这样 `ChannelHandler` 可以通过 `ctx.channel()` 访问当前的 `Channel`。`Channel` 是通道的抽象，封装了底层 I/O 操作。

3. **获取通道的属性（Attributes）**：
   - 可以通过 `ctx.channel().attr()` 设置和获取与通道关联的自定义属性。这个功能特别适合跨线程传递数据，在客户端代码中，使用 `AttributeKey` 存储服务器返回的响应对象，使得在客户端的其他地方也能读取这些属性。

4. **管道操作**：
   - 通过 `ChannelHandlerContext` 可以访问 `ChannelPipeline`，从而可以动态地添加、移除或替换处理器。这对于实现灵活的处理链非常有用。

5. **异常处理**：
   - `ChannelHandlerContext` 提供了 `exceptionCaught` 方法，可以捕获处理过程中的异常。调用 `ctx.close()` 可以关闭通道并释放资源，防止资源泄漏。

#### 示例代码中的 `ChannelHandlerContext` 使用

在 `NettyClientHandler` 中，`ChannelHandlerContext` 被用来存储服务器返回的 `RpcResponse` 对象：

```java
@Override
protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
    // 使用 AttributeKey 将响应对象存储在通道的属性中，供后续获取
    AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
    // 将服务器返回的响应对象存储在通道属性中
    ctx.channel().attr(key).set(response);
    // 关闭通道，表示处理完成
    ctx.channel().close();
}
```

代码解析

1. `ctx.channel().attr(key).set(response);`：通过 `ChannelHandlerContext` 获取当前 `Channel` 的属性，并使用 `AttributeKey` 将服务器的响应对象 `RpcResponse` 存储在通道的属性中，以便客户端其他部分可以获取该响应数据。

2. `ctx.channel().close();`：处理完响应后，关闭通道，表明当前请求的生命周期已结束。

#### 总结

`ChannelHandlerContext` 是连接 `ChannelHandler` 和 `ChannelPipeline` 的关键对象，提供了许多控制和交互功能。通过 `ChannelHandlerContext`，处理器可以访问通道、触发事件、管理通道属性以及处理异常，使得 Netty 的数据处理机制非常灵活和高效。

## 服务端重构

服务端的Netty方式重构主要集中在几个点：

- 以Netty的方式实现RpcServer接口（之前就写过了）

  ServerBootStrap启动器和两个服务线程组：

  - bossGroup负责建立连接
  - workGroup负责具体的IO请求

- NettyServerInitializer，这是配置netty对消息的处理机制，如编码器、解码器、消息格式等（设置handler）

- NettyRPCServerHandler，这是指定netty对接收消息的处理方式。

### Netty方式实现RpcServer接口

> 此处关于ServiceProvider、NioEventLoopGroup、ChannelFuture等的知识见RPC前置知识

```java
package com.async.rpc.server.server.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */

/**
 * @program: simple_RPC
 *
 * @description: netty类型的实现RpcServer接口的类
 **/
@AllArgsConstructor
public class NettyRpcServerImpl implements RpcServer {
    private ServiceProvider serviceProvider;
    @Override
    public void start(int port) {
        // netty 服务线程组boss负责建立连接， work负责具体的请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        System.out.println("netty服务端启动了");
        try {
            //启动netty服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //初始化
            serverBootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class)
                    //NettyClientInitializer这里 配置netty对消息的处理机制
                    .childHandler(new NettyServerInitializer(serviceProvider));
            //同步堵塞
            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();
            //死循环监听
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {

    }
}

```

### NettyServerInitializer

```java
package com.async.rpc.server.netty.nettyInitializer;


/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 */
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //消息格式 【长度】【消息体】，解决沾包问题
        pipeline.addLast(
                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
        //计算当前待发送消息的长度，写入到前4个字节中
        pipeline.addLast(new LengthFieldPrepender(4));

        //使用Java序列化方式，netty的自带的解码编码支持传输这种结构
        pipeline.addLast(new ObjectEncoder());
        //使用了Netty中的ObjectDecoder，它用于将字节流解码为 Java 对象。
        //在ObjectDecoder的构造函数中传入了一个ClassResolver 对象，用于解析类名并加载相应的类。
        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
            @Override
            public Class<?> resolve(String className) throws ClassNotFoundException {
                return Class.forName(className);
            }
        }));

        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}

```

### NettyRPCServerHandler

```java
package com.async.rpc.server.netty.handler;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/8
 *  * 因为是服务器端，我们知道接受到请求格式是RPCRequest
 *  * Object类型也行，强制转型就行
 */
@AllArgsConstructor
public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private ServiceProvider serviceProvider;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        //接收request，读取并调用服务
        RpcResponse response = getResponse(request);
        ctx.writeAndFlush(response);
        ctx.close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
    private RpcResponse getResponse(RpcRequest rpcRequest){
        //得到服务名
        String interfaceName=rpcRequest.getInterfaceName();
        //得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        //反射调用方法
        Method method=null;
        try {
            method= service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke=method.invoke(service,rpcRequest.getParams());
            return RpcResponse.sussess(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RpcResponse.fail();
        }
    }
}

```



### Netty全过程流程

下面将结合您的代码，详细讲解 Netty 执行流程，包括客户端的请求发送、服务端的请求接收与处理、客户端的响应接收这三部分。

#### 1. 客户端发送请求的流程

1. **客户端调用 `RpcClient.sendRequest()`**：
   - 客户端代码中，`RpcClient.sendRequest()` 是发送请求的入口。该方法负责将请求数据（如 `RpcRequest` 对象）通过 Netty 发送给服务端。

2. **初始化 `NettyClientInitializer` 并设置编码器**：
   - 在 `NettyClientInitializer` 中，配置了消息的处理机制，包含编码器（`ObjectEncoder`）和粘包/拆包处理器（`LengthFieldBasedFrameDecoder` 和 `LengthFieldPrepender`）。
   - **Encoder编码**：`ObjectEncoder` 将 `RpcRequest` 对象序列化成字节流，以便在网络上传输。

3. **数据发送**：
   - `sendRequest` 方法中使用 `channel.writeAndFlush(request)`，将编码后的请求数据发送到服务端。
   - 此时，Netty 会使用配置的 `LengthFieldPrepender` 将消息的长度添加到数据头部，然后通过 `SocketChannel` 将字节流发送到服务端。

#### 2. 服务端接收并处理请求的流程

1. **服务端接收请求 - `RpcServer`**：
   - 服务端的 `RpcServer` 监听指定端口，等待客户端的连接。当接收到客户端请求时，会为每个连接创建一个新的 `SocketChannel`。

2. **初始化 `NettyServerInitializer` 并设置解码器**：
   - 在 `NettyServerInitializer` 中，配置了解码器（`ObjectDecoder`）和粘包/拆包处理器（`LengthFieldBasedFrameDecoder` 和 `LengthFieldPrepender`）。
   - **Decoder解码**：`ObjectDecoder` 将字节流解码为 `RpcRequest` 对象，以便服务端能够读取请求的具体内容。

3. **业务处理 - `NettyRPCServerHandler`**：
   - 解码完成后，`NettyRPCServerHandler` 处理接收到的 `RpcRequest`，并调用 `getResponse` 方法完成业务逻辑。
   - `getResponse` 方法使用反射，根据请求的 `methodName` 和 `params` 调用服务端的相应方法并获取返回结果，生成 `RpcResponse` 对象。

4. **返回结果**：
   - 业务处理完成后，`NettyRPCServerHandler` 将 `RpcResponse` 对象写回通道，Netty 会通过 `ObjectEncoder` 将 `RpcResponse` 编码为字节流，然后发送给客户端。

#### 3. 客户端接收响应的流程

1. **客户端接收响应 - `NettyClientInitializer`**：
   - 客户端通道 `SocketChannel` 接收到服务端发回的 `RpcResponse` 字节流，进入 `NettyClientInitializer` 进行解码。
   - **Decoder解码**：`ObjectDecoder` 将字节流解码为 `RpcResponse` 对象，以便客户端业务代码可以读取响应内容。

2. **处理响应 - `NettyClientHandler`**：
   - 解码后的 `RpcResponse` 进入 `NettyClientHandler` 进行处理。
   - `NettyClientHandler` 使用 `AttributeKey` 将 `RpcResponse` 存储在通道的属性中，以便 `sendRequest` 方法中能够读取该响应数据。

3. **获取结果并返回给上层**：
   - `sendRequest` 方法读取通道属性中的 `RpcResponse`，获取服务端的处理结果。
   - 最终，`sendRequest` 返回 `RpcResponse` 中的数据给调用方，完成整个请求-响应的处理流程。

#### 执行流程图

```
客户端:
RpcClient.sendRequest() -> NettyClientInitializer -> Encoder -> SocketChannel.writeAndFlush(request)

服务端:
SocketChannel.read() -> NettyServerInitializer -> Decoder -> NettyRPCServerHandler -> getResponse() -> writeAndFlush(response)

客户端接收:
SocketChannel.read() -> NettyClientInitializer -> Decoder -> NettyClientHandler -> sendRequest()
```

#### 结合代码示例

**客户端 - 发送请求**

```java
public RpcResponse sendRequest(RpcRequest request) {
    // 连接到服务器，发送请求数据
    ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
    Channel channel = channelFuture.channel();
    channel.writeAndFlush(request); // 使用 Encoder 编码并发送数据
    channel.closeFuture().sync();
    return channel.attr(key).get(); // 从通道属性中获取响应结果
}
```

**服务端 - 接收并处理请求**

```java
protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
    RpcResponse response = getResponse(request); // 使用反射调用服务端方法，获取结果
    ctx.writeAndFlush(response); // 将响应发送给客户端
}
```

**客户端 - 处理响应**

```java
protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
    ctx.channel().attr(key).set(response); // 存储响应数据供 sendRequest 方法读取
    ctx.channel().close();
}
```

通过以上流程，客户端发送请求并获取响应，服务端处理请求并返回结果，这就是 Netty 的数据处理全过程。





