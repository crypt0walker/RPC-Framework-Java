# RPC-Framework-Java

构建一个简易的Java-RPC框架，逐渐完善并优化功能。

- 构建一个基本的RPC调用——√
- 引入Netty网络应用框架——√
- 引入Zookeeper作为服务注册中心——√
- Netty自定义编码器、解码器及序列化器，实现jsonf序列化方式——√
- 增加其它序列化方式——×
- 客户端建立本地服务缓存并实现动态更新——待完成
- ……

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

![simple_RPC](./images/simple_RPC.png)

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

  <img src="./images/image-20241108215119277.png" alt="image-20241108215119277" style="zoom:50%;" />

  - 这里我们做了优化，同时实现了一个简单Socket类型的对RpcClient接口的实现，以方便客户端选择使用那种类型（choose字段）
  - 由于以上，所以同时也需要对ClientProxy进行重构，更改构造方法以及消息处理的写法（见后续）

- 编写NettyClientInitializer，这是配置netty对消息的处理机制，如编码器、解码器、消息格式等（设置handler）

- 编写NettyClientHandler，这是指定netty对接收消息的处理方式。

### Netty方式实现RpcClient接口

这一步的目的是重写原位于ClientProxy中invoke方法内的

<img src="./images/image-20241108215533427.png" alt="image-20241108215533427" style="zoom:50%;" />

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



# 3. 引入zookeeper作为注册中心

## 3.1 Zookeeper相关知识

### Zookeeper 简介

**Zookeeper** 是一个开源的分布式协调服务，主要用于管理分布式应用中的配置信息、命名服务和分布式同步。它可以看作是一个高性能、高可靠的分布式数据库，专门设计用于存储和管理小型数据，比如配置信息、元数据等。

#### **Zookeeper 的主要特点**

1. **分布式一致性**：使用 **ZAB（Zookeeper Atomic Broadcast）协议**，确保多台 Zookeeper 节点之间的数据一致性。
2. **高性能**：通过内存存储，快速响应读请求，尤其适用于读多写少的场景。
3. **高可用性**：通常部署为一个由多台服务器组成的集群，只要超过一半的节点存活，整个集群就能正常工作。
4. **顺序访问**：为所有事务操作分配唯一递增的事务 ID（zxid），保证了操作的全局顺序。

#### **Zookeeper 的数据模型**

Zookeeper 的数据模型类似于文件系统，每个节点（称为 **znode**）可以存储数据和子节点，构成一棵树状结构。

---

### Zookeeper 在 RPC 中的作用

在 RPC 框架中，Zookeeper 通常作为**注册中心**使用，主要解决分布式环境下服务管理的问题。它的核心作用如下：

#### 1. **服务注册与发现**

   - **服务端注册服务**：
     当服务端启动时，将自己的服务信息（例如服务名称、IP 地址和端口号）注册到 Zookeeper 中的特定节点。
   - **客户端发现服务**：
     客户端调用服务前，会从 Zookeeper 中获取服务端的地址和端口，然后直接与服务端建立连接。

#### 2. **动态管理服务列表**

   - 当一个服务实例下线或不可用时，Zookeeper 会通过临时节点的机制自动移除该服务信息，客户端可以实时更新服务列表，避免调用无效服务。

#### 3. **负载均衡**

   - 客户端可以从 Zookeeper 获取所有可用服务的列表，并通过某种负载均衡算法（如随机、轮询等）选择一个实例进行调用。

#### 4. **高可用性**

   - Zookeeper 作为分布式注册中心，具备高可用性，即使部分节点故障，也不会影响注册和发现的功能。

---

### Zookeeper 在 RPC 中的具体工作流程

#### 1. **服务端启动时**

   - 服务端启动后，向 Zookeeper 注册中心注册服务信息，通常以 **服务名** 作为 Znode 的路径，以 **服务地址（IP+端口）** 作为节点数据存储。

   例如，注册的 Zookeeper 节点结构：

   ```
/services
    /UserService
        192.168.1.100:8080
        192.168.1.101:8081
   ```

#### 2. **客户端调用时**

   - 客户端首先从 Zookeeper 获取目标服务（如 `UserService`）的地址列表。
   - 根据负载均衡策略选定一个地址，建立网络连接并发送 RPC 请求。

#### 3. **服务实例变化时**

   - 如果某个服务实例下线，Zookeeper 会删除对应的临时节点。
   - 客户端监听 Zookeeper 节点的变化事件，并实时更新服务列表。

---

### 为什么引入 Zookeeper？

1. **解决服务动态变化的问题**：
   - 通过 Zookeeper 实现服务的动态注册与发现，客户端无需硬编码服务地址，避免服务上下线导致的问题。

2. **提高系统的扩展性**：
   - 新增服务只需注册到 Zookeeper，客户端会自动感知，无需手动配置。

3. **降低系统耦合性**：
   - 客户端与服务端通过 Zookeeper 解耦，直接通过服务名访问，而非依赖固定地址。

4. **保障服务高可用性**：
   - Zookeeper 提供可靠的节点管理，避免了因单点故障导致服务不可用的问题。



## 3.2 客户端重构

引入zookeeper后最大的不同就在于无需在代码中硬编码所需服务的IP与端口号，而是构建一个服务发现中心，用于向zk注册中心发起请求查询所需服务的IP地址与端口号；相应的原代码中硬编码的IP地址与端口号也需要相应的更改；

### 环境配置

引入Curator包：对zookeeper进行连接操作的工具

```xml
<!--这个jar包应该依赖log4j,不引入log4j会有控制台会有warn，但不影响正常使用-->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-recipes</artifactId>
    <version>5.1.0</version>
</dependency>
```

启动Zookeeper服务器：

管理员权限启动CMD，执行以下命令开启zookeeper服务器（需要提前设置好环境变量）

```cmd
zkServer
```

### 修改ClientProxy

简介——接触IP与端口的硬编码

```java
    //……以上不改变部分省略
	//重写无参构造函数，原写法中在构造函数中就写入IP与端口号
	private RpcClient rpcClient;
    public ClientProxy(){
        rpcClient=new NettyRpcClient();
    }
```

### 修改nettyRpcClient

简介——解除硬编码，并在sendRequest中先去注册中心查找对应服务的ip和端口号再去连接服务器

```java
    //……省略其它
	/* Netty版本（非zookeeper）：将ip与端口写死
    public NettyRpcClient(String host,int port){
        this.host=host;
        this.port=port;
    }
     */
    // zookeeper版本：先去注册中心查找服务对应的ip和端口，再去连接对应服务器
    private ServiceCenter serviceCenter;
    public NettyRpcClient(){
        this.serviceCenter=new ZKServiceCenter();
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        //zookeeper补充，否则无host
        //从注册中心获取对应服务名的host,post
        InetSocketAddress address = serviceCenter.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();
        //zookeeper版本
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
	
```

### 注册中心类

#### ServiceCenter接口

简介——定义服务中心接口

```java
//服务中心接口
public interface ServiceCenter {
    //  查询：根据服务名查找地址
    InetSocketAddress serviceDiscovery(String serviceName);
}

```

#### ZKServiceCenter类

简介——与ZK服务器建立连接并获取相应服务接口

`ZKServiceCenter` 是一个基于 **Zookeeper** 的服务注册中心实现，用于实现 **服务发现功能**。客户端通过此类从 Zookeeper 中查询可用的服务地址，返回 `InetSocketAddress` 供客户端连接使用。

```java
package com.async.rpc.client.serviceCenter;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @program: simple_RPC
 *
 * @description: zookeeper注册中心
 **/
public class ZKServiceCenter implements ServiceCenter {
    // curator 提供的zookeeper客户端,是用来与 Zookeeper 服务器通信的核心对象。
    private CuratorFramework client;
    //定义 Zookeeper 中的根节点路径，所有的服务都注册在这个路径下。
    private static final String ROOT_PATH = "MyRPC";

    //构造函数：负责zookeeper客户端的初始化，并与zookeeper服务端进行连接
    public ZKServiceCenter(){
        // 创建重试策略：指数退避重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 初始化 Zookeeper 客户端
        this.client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181") // Zookeeper 服务器的地址,可以是单个或多个 IP+端口
                .sessionTimeoutMs(40000)        // 会话超时时间，40秒,心跳监听状态超时未响应将失效
                .retryPolicy(policy)            // 设置重试策略
                .namespace(ROOT_PATH)           // 设置根路径（命名空间），操作时所有路径都以这个为前缀
                .build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }
    //服务发现方法
    //向zk注册中心发起查询，根据服务名（接口名）返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // 获取 Zookeeper 中指定服务名称的子节点列表
            //客户端通过服务名 serviceName 向 Zookeeper 查询注册的服务实例列表。
            //返回结果是一个服务实例的地址（如 192.168.1.100:8080）。
            List<String> strings = client.getChildren().forPath("/" + serviceName);

            // 检查列表是否为空——如果不检查若列表为空，后续get(0)则会报异常
            if (strings == null || strings.isEmpty()) {
                System.err.println("No available instances for service: " + serviceName);
                return null; // 或者你可以抛出一个自定义的异常来告知调用者
            }

            // 选择一个服务实例，默认用第一个节点，后续可以加入负载均衡机制
            String string = strings.get(0);

            // 解析并返回 InetSocketAddress
            // 将字符串形式的地址（如 192.168.1.100:8080）转换为 InetSocketAddress，便于后续网络连接。
            return parseAddress(string);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            return null; // 或者根据需求返回一个默认的 InetSocketAddress
        }
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    //将 InetSocketAddress 对象转换为字符串形式 IP:Port，便于存储到 Zookeeper。
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
    
    // 字符串解析为地址
    //将 IP:Port 字符串解析为 InetSocketAddress，便于客户端与服务端建立网络连接。
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":"); // 按 `:` 分割 IP 和端口
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
```

### 测试函数

```java
package com.async.rpc.client;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */

import com.async.rpc.client.proxy.ClientProxy;
import com.async.rpc.common.pojo.User;
import com.async.rpc.common.server.UserService;

/**
 * @program: simple_RPC
 *
 * @description: ZK注册中心版本测试类
 **/
public class TestZKClient {
    public static void main(String[] args) {
        ClientProxy clientProxy=new ClientProxy();
        //ClientProxy clientProxy=new part2.Client.proxy.ClientProxy("127.0.0.1",9999,0);
        UserService proxy=clientProxy.getProxy(UserService.class);

        User user = proxy.getUserByUserId(1);
        System.out.println("从服务端得到的user="+user.toString());

        User u=User.builder().id(100).userName("wxx").gender(true).build();
        Integer id = proxy.insertUserId(u);
        System.out.println("向服务端插入user的id"+id);
    }
}
```

#### 客户端调用

1. **`TestZKClient.main()`**
   客户端入口。调用主函数，创建 `ClientProxy` 实例，并通过动态代理对象调用服务接口方法，例如 `proxy.getUserByUserId(1)`。
2. **`ClientProxy.getProxy()`**
   使用 JDK 动态代理，生成服务接口（如 `UserService`）的代理对象。
   **作用**：当调用代理对象方法时，会转发到 `ClientProxy.invoke()`。
3. **`ClientProxy.invoke()`**
   捕获代理对象方法的调用，将方法名、参数等封装为 `RpcRequest` 对象。
   调用 `NettyRpcClient.sendRequest()`，发送请求到服务端。
4. **`NettyRpcClient.sendRequest()`**
   1. 调用 `ZKServiceCenter.serviceDiscovery()`，从 Zookeeper 获取目标服务的 IP 和端口。
   2. 使用 Netty 建立通道，序列化 `RpcRequest` 对象并发送到服务端。
   3. 等待服务端返回响应。
5. **`ZKServiceCenter.serviceDiscovery()`**
   查询 Zookeeper，获取服务名对应的子节点列表（即服务实例地址）。
   返回第一个服务地址（可扩展为负载均衡策略）。
6. **`NettyClientHandler.channelRead0()`**
   客户端接收到服务端响应时触发。
   将反序列化后的 `RpcResponse` 对象存储在通道的属性中，供 `NettyRpcClient.sendRequest()` 使用。
7. **返回响应到客户端**
   `NettyRpcClient.sendRequest()` 返回 `RpcResponse` 的数据部分到 `ClientProxy.invoke()`，最终返回给 `TestZKClient.main()`。

#### 服务端接收

1. **`SimpleRpcServerImpl.start()`**
   服务端入口。启动 Netty 服务器，监听指定端口。
   为每个客户端连接分配通道，并通过 `NettyServerInitializer` 配置解码器和业务处理器。
2. **`NettyRPCServerHandler.channelRead0()`**
   服务端接收到客户端请求时触发。
   将字节流反序列化为 `RpcRequest` 对象，并调用 `getResponse()` 处理业务逻辑。
3. **`getResponse()`**
   通过反射机制，根据 `RpcRequest` 中的方法名、参数类型、参数值，调用对应的服务实现类（如 `UserServiceImpl`）的方法。
   返回调用结果，封装为 `RpcResponse` 对象。
4. **`UserServiceImpl.getUserByUserId()`**
   实现服务的具体逻辑。例如，模拟从数据库中查询用户数据并返回。
5. **返回响应到客户端**
   服务端将 `RpcResponse` 对象序列化后写回客户端。
   客户端接收后解码并返回给调用方。

#### 交互概览

```
客户端调用:
TestZKClient.main()
    --> ClientProxy.getProxy()
    --> ClientProxy.invoke()
    --> NettyRpcClient.sendRequest()
        --> ZKServiceCenter.serviceDiscovery()
        --> NettyClientHandler.channelRead0()
    --> 返回响应到客户端

服务端接收:
SimpleRpcServerImpl.start()
    --> NettyRPCServerHandler.channelRead0()
    --> getResponse()
        --> UserServiceImpl.getUserByUserId()
    --> 返回响应到客户端

```

## 3.3 服务端重构

服务端引入Zookeeper之后需要做的改动主要是需要进行服务注册，同时也需要与zookeeper服务器建立连接，因为其与客户端一样同样也是zk注册中心的客户端；所以需要修改ServerProvider（原功能是注册服务到本地集合）

### 修改ServiceProvider

简介——由将服务注册至本地改位将服务名与服务实例的网络地址（IP:Port）注册到Zookeeper注册中心，使客户端能够动态发现服务；

**类功能概述**

`ServiceProvider` 是服务端用于管理服务的核心类，其主要职责包括：

1. 本地服务管理：
   - 将服务对象存储到一个本地的 `HashMap` 中，供服务端在接收到客户端请求时快速找到对应的服务实现类。
2. 服务注册：
   - 如果使用 **Zookeeper**，将服务名和服务实例的网络地址（`IP:Port`）注册到 Zookeeper 注册中心，使客户端能够动态发现服务。
3. 获取服务实例：
   - 提供根据服务名查找对应服务实例的方法，用于服务端调用具体的业务逻辑。

```java
package com.async.rpc.server.provider;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */

import com.async.rpc.server.serviceRegister.ServiceRegister;
import com.async.rpc.server.serviceRegister.impl.ZKServiceRegister;
import lombok.AllArgsConstructor;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: simple_RPC
 *
 * @description: 本地服务存放器
 **/
//本地服务存放器
public class ServiceProvider {
    //集合中存放本地服务的实例
    // 使用 HashMap 将服务接口名（String）与对应服务实例对象（Object）映射起来，供服务端查找服务时使用。
    private Map<String,Object> interfaceProvider;

    /*非zookeeper版本：本地注册服务
    public ServiceProvider(){
        this.interfaceProvider=new HashMap<>();
    }
    //本地注册服务
    public void provideServiceInterface(Object service){
        String serviceName=service.getClass().getName();
        Class<?>[] interfaceName=service.getClass().getInterfaces();

        for (Class<?> clazz:interfaceName){
            interfaceProvider.put(clazz.getName(),service);
        }

    }
     */

    //zookeeper注册服务
    private int port;
    private String host;
    //注册服务类
    private ServiceRegister serviceRegister;

    public ServiceProvider(String host, int port) {
        // 服务端的 IP 和端口，用于标识服务实例的位置
        this.host = host;
        this.port = port;

        // 初始化本地服务存储器，使用 HashMap 存储服务名到服务实例的映射
        this.interfaceProvider = new HashMap<>();

        // 初始化 Zookeeper 服务注册器
        this.serviceRegister = new ZKServiceRegister();
    }
    
    //在测试类中调用“serviceProvider.provideServiceInterface(userService);”
    //传入一个服务实例
    public void provideServiceInterface(Object service) {
        // 获取服务的类名（完整路径）
        String serviceName = service.getClass().getName();

        // 获取服务实现类所实现的接口列表
        Class<?>[] interfaceName = service.getClass().getInterfaces();

        // 遍历接口列表
        for (Class<?> clazz : interfaceName) {
            // 1. 将服务接口名和服务实例对象保存到本地的映射表中
            // 服务端可以快速找到实现类以处理具体业务逻辑。
            interfaceProvider.put(clazz.getName(), service);

            // 2. 将服务名和网络地址注册到 Zookeeper 中
            // 客户端可以动态发现服务的 IP 和端口，避免硬编码。
            serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port));
        }
    }
    //获取服务实例
    //从本地存储的 interfaceProvider 映射中，根据接口名查找对应的服务实例。
    //使用场景：当服务端接收到客户端的请求时，通过接口名查找对应的服务实现类，然后通过反射调用具体方法。
    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }
}


```

逻辑关系

```
TestZKServer.main()
    --> 初始化服务实现类 UserServiceImpl
    --> 初始化 ServiceProvider
    --> 调用 ServiceProvider.provideServiceInterface()
        --> 本地存储服务映射 (interfaceProvider.put)
        --> 注册服务到 Zookeeper (serviceRegister.register)
    --> 启动 RpcServer

```

### 实现服务注册接口与类

#### 服务注册接口—ServiceRegister

```java
package com.async.rpc.server.serviceRegister;

import java.net.InetSocketAddress;

/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */
public interface ServiceRegister {
    //  注册：保存服务与地址。
    void register(String serviceName, InetSocketAddress serviceAddress);
}

```

#### 服务注册实现类—ZKServiceRegisterImpl

**类功能概述**

`ZKServiceRegister` 是服务端用于与 **Zookeeper 注册中心** 交互的实现类，实现了 `ServiceRegister` 接口，主要职责是：

1. 初始化 Zookeeper 客户端：
   - 与 Zookeeper 服务端建立连接。
2. 注册服务：
   - 在 Zookeeper 中创建服务名节点，并为每个服务实例添加地址子节点。
   - 服务实例节点使用临时节点，服务端下线时自动删除。

```java
package com.async.rpc.server.serviceRegister.impl;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */

import com.async.rpc.server.serviceRegister.ServiceRegister;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;

/**
 * @program: simple_RPC
 *
 * @description: 向ZK注册中心注册服务
 **/
public class ZKServiceRegister implements ServiceRegister {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";

    //构造函数：负责zookeeper客户端的初始化，并与zookeeper服务端进行连接，同client一致
    public ZKServiceRegister(){
        // 指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是，消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }


    @Override
    public void register(String serviceName, InetSocketAddress serviceAddress) {
        try {
            // 检查服务名节点是否存在，不存在则创建永久节点   
            // serviceName创建成永久节点，服务提供者下线时，不删服务名，只删地址
            // 永久节点通常用于表示一个服务的逻辑名称，例如 /MyRPC/UserService。
            // 服务名称是长期存在的，即使所有服务实例都暂时不可用，服务的逻辑概念仍然保留。
            if(client.checkExists().forPath("/" + serviceName) == null){
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
            }
            // 路径地址，一个/代表一个节点,属于临时节点（因为是具体的服务地址）
            String path = "/" + serviceName +"/"+ getServiceAddress(serviceAddress);
            // 临时节点，服务器下线就删除节点
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            System.out.println("此服务已存在");
        }
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +                    
                serverAddress.getPort();
    }
    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}

```



### 永久节点与临时节点

在使用 **Zookeeper** 作为注册中心时，永久节点和临时节点有各自的作用。两者的配合能够更好地实现服务注册和发现。

#### **1. 永久节点的作用**

- **概念**：

  - 永久节点（`PERSISTENT`）一旦创建，就会一直存在，除非手动删除。
  - 不会因服务端下线或断开连接而自动删除。

- **在服务注册中的作用**：

  1. **存储服务名称**：

     - 永久节点通常用于表示一个服务的逻辑名称，例如 `/MyRPC/UserService`。
     - 服务名称是长期存在的，即使所有服务实例都暂时不可用，服务的逻辑概念仍然保留。

  2. **提供目录结构**：

     - 永久节点可以作为服务实例临时节点的父节点，充当目录作用，便于组织和管理。

     - 示例结构：

       ```
       /MyRPC
           /UserService  <-- 永久节点
               /192.168.1.100:8080  <-- 临时节点
               /192.168.1.101:8081  <-- 临时节点
       ```

- **优势**：

  - 即使当前所有服务实例都下线，永久节点仍然存在，方便客户端知道这个服务名并尝试发现新的实例。
  - 减少重复创建服务名节点的开销。

#### 2. 临时节点的作用

- **概念**：
  - 临时节点（`EPHEMERAL`）与客户端的会话绑定。当会话断开或服务端下线时，临时节点会自动删除。

- **在服务注册中的作用**：
  1. **动态服务实例管理**：
     - 每个服务实例对应一个临时节点，例如 `/MyRPC/UserService/192.168.1.100:8080`。
     - 当服务实例下线时，节点会自动删除，确保客户端不再连接到已不可用的服务。
  2. **实时服务状态更新**：
     - 临时节点的自动删除机制可以保证注册中心的服务信息实时更新，避免使用无效地址。

- **优势**：
  - 无需手动清理下线服务实例的节点。
  - 实现了服务实例的动态注册与注销。

##### **永久节点与临时节点的配合**

通过永久节点和临时节点的组合，可以同时满足 **服务长期存在的稳定性** 和 **服务实例动态管理的灵活性**。

#### **示例：服务注册与发现**

1. **服务端注册服务**：
   - 创建服务名的永久节点 `/MyRPC/UserService`。
   - 为每个服务实例创建临时节点 `/MyRPC/UserService/192.168.1.100:8080`。

2. **服务实例下线**：
   - 当服务端 192.168.1.100:8080 下线时，对应的临时节点 `/MyRPC/UserService/192.168.1.100:8080` 会自动删除。
   - 永久节点 `/MyRPC/UserService` 不受影响。

3. **客户端服务发现**：
   - 客户端通过查询永久节点 `/MyRPC/UserService`，获取其子节点列表（服务实例地址）。
   - 如果没有子节点，表示当前没有可用的服务实例，但服务逻辑名仍然存在，客户端可尝试重新发现。

#### **总结**

- **永久节点** 保证了服务的逻辑存在，不会因为所有服务实例下线而丢失。
- **临时节点** 动态反映服务实例的状态，自动删除无效服务，减少管理开销。

这种设计兼顾了 **服务的长期性** 和 **实例的动态性**，同时实现了服务注册与发现的高效管理。

### 测试函数

```java
package com.async.rpc.server;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/15
 */

import com.async.rpc.common.server.UserService;
import com.async.rpc.common.server.impl.UserServiceImpl;
import com.async.rpc.server.provider.ServiceProvider;
import com.async.rpc.server.server.RpcServer;
import com.async.rpc.server.server.impl.NettyRpcServerImpl;

/**
 * @program: simple_RPC
 *
 * @description: 使用ZK注册中心的测试类
 **/
public class TestZKServer {
    public static void main(String[] args) {
        UserService userService=new UserServiceImpl();
        // 需要提供服务网络地址（本机），用于服务注册
        ServiceProvider serviceProvider=new ServiceProvider("127.0.0.1",9999);
        serviceProvider.provideServiceInterface(userService);

        RpcServer rpcServer=new NettyRpcServerImpl(serviceProvider);
        rpcServer.start(9999);
    }
}
```

### **服务注册的整体流程**

服务注册是分布式系统中服务端向注册中心报告自身服务信息的过程，客户端可通过注册中心动态发现服务实例并与其通信。在这里，我们结合 Zookeeper 和相关代码讲解整个服务注册流程。

#### **流程概述**

1. **服务端启动时注册服务**：
   - 服务端将提供的服务（例如 `UserService`）及其网络地址（`IP:Port`）注册到 Zookeeper。
   - 服务端在 Zookeeper 上创建一个永久节点（服务名）和多个临时节点（服务实例地址）。

2. **服务端下线时自动注销服务**：
   - Zookeeper 自动删除服务实例的临时节点，保证注册中心信息的实时性。

3. **客户端动态发现服务**：
   - 客户端从注册中心查询服务名，获取当前可用的服务实例地址（`IP:Port`），并与服务端建立通信。

#### **代码逻辑和调用流程**

##### **1. 服务端启动服务并注册**

##### **入口：`TestZKServer.main()`**

```java
public static void main(String[] args) {
    // 初始化服务实现类
    UserService userService = new UserServiceImpl();

    // 初始化服务注册器（ServiceProvider），传入服务端的 IP 和端口
    ServiceProvider serviceProvider = new ServiceProvider("127.0.0.1", 9999);

    // 注册服务
    serviceProvider.provideServiceInterface(userService);

    // 启动服务端并监听端口
    RpcServer rpcServer = new NettyRpcServerImpl(serviceProvider);
    rpcServer.start(9999);
}
```

1. **初始化服务实现类**：
   - 创建服务逻辑实现类对象（如 `UserServiceImpl`）。
   - 这是服务端提供的业务逻辑，服务端需要将其注册到注册中心。

2. **注册服务**：
   - 调用 `ServiceProvider.provideServiceInterface`，将服务注册到本地存储和 Zookeeper 注册中心。

3. **启动服务端**：
   - 服务端开始监听端口，等待客户端请求。

##### **2. 注册服务到本地和 Zookeeper**

##### **类：`ServiceProvider`**

```java
public void provideServiceInterface(Object service) {
    // 获取服务类名（实现类）
    String serviceName = service.getClass().getName();

    // 获取服务实现类所实现的所有接口
    Class<?>[] interfaceName = service.getClass().getInterfaces();

    // 遍历接口列表
    for (Class<?> clazz : interfaceName) {
        // 将接口名和服务实例对象存储到本地映射
        interfaceProvider.put(clazz.getName(), service);

        // 注册服务到 Zookeeper
        serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port));
    }
}
```

- **本地注册服务**：
  - 使用 `interfaceProvider` 存储服务接口名和对应的服务实例对象。
  - 供服务端处理请求时，通过接口名快速找到对应的服务实现类。

- **注册到 Zookeeper**：
  - 调用 `serviceRegister.register`：
    1. 创建服务名节点（永久节点）。
    2. 创建服务实例节点（临时节点），保存服务端的 `IP:Port`。

##### **3. 服务实例注册到 Zookeeper**

##### **类：`ZKServiceRegister`**

```java
@Override
public void register(String serviceName, InetSocketAddress serviceAddress) {
    try {
        // 检查服务名节点是否存在
        if (client.checkExists().forPath("/" + serviceName) == null) {
            // 创建永久节点（服务名）
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                .forPath("/" + serviceName);
        }

        // 创建临时节点（服务实例地址）
        String path = "/" + serviceName + "/" + getServiceAddress(serviceAddress);
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
            .forPath(path);
    } catch (Exception e) {
        System.out.println("此服务已存在");
    }
}
```

1. **创建服务名节点（永久节点）**：
   - 每个服务名（例如 `/UserService`）只会创建一次，代表服务的逻辑存在。

2. **创建服务实例节点（临时节点）**：
   - 每个服务实例（例如 `/UserService/127.0.0.1:9999`）为一个临时节点。
   - 当服务端下线或断开连接时，节点会自动删除。

##### **4. 服务端启动监听**

##### **类：`RpcServer`**

```java
public void start(int port) {
    ServerSocket serverSocket = new ServerSocket(port);
    while (true) {
        Socket socket = serverSocket.accept();
        new Thread(new WorkThread(socket, serviceProvider)).start();
    }
}
```

- 启动服务端监听，接受客户端连接。
- 每个连接由一个独立线程 `WorkThread` 处理，线程通过 `ServiceProvider` 查找对应服务并处理业务逻辑。

##### **服务注册节点结构**

假设服务端注册了 `UserService`，IP 为 `127.0.0.1`，端口为 `9999`。

**Zookeeper 节点结构：**

```
/MyRPC
    /UserService           <-- 永久节点
        /127.0.0.1:9999    <-- 临时节点
```

- **永久节点**：
  - `/MyRPC/UserService` 表示服务的逻辑名称。
  - 不会随服务端下线而删除。

- **临时节点**：
  - `/MyRPC/UserService/127.0.0.1:9999` 表示具体的服务实例。
  - 服务端下线时，节点自动删除，保证客户端不连接无效服务。

#### **整体流程图**

```plaintext
1. 服务端启动服务（TestZKServer.main）
    -> 初始化服务实现类 (UserServiceImpl)
    -> 初始化服务提供器 (ServiceProvider)
    -> 调用 provideServiceInterface:
        - 本地存储服务接口和实现类
        - 调用 ZKServiceRegister.register:
            -> 创建永久节点 (UserService)
            -> 创建临时节点 (127.0.0.1:9999)

2. 服务端启动监听
    -> RpcServer.start
    -> 等待客户端请求

3. 服务实例下线
    -> Zookeeper 自动删除临时节点
```

#### **总结**

- 服务注册的整体流程分为三部分：
  1. **本地存储**：将服务接口名和实现类映射到本地，用于服务端快速查找服务实例。
  2. **Zookeeper 注册**：将服务名和实例地址注册到 Zookeeper，供客户端动态发现。
  3. **动态管理**：通过 Zookeeper 临时节点机制，实现服务实例的动态管理（上线/下线）。

- 该流程保证了分布式服务的灵活性和实时性，使客户端能够动态适配服务端的变化。

# 4. 自定义编解码器与序列化

## 4.1 原理知识

### 为什么需要使用 **Netty 自定义编解码器和序列化器**

在基于 **Netty** 的 RPC 框架中，编解码器和序列化器的设计直接影响到 **数据传输的效率**、**兼容性** 和 **灵活性**。使用自定义编解码器和序列化器能够满足特定的需求和优化性能。

#### **1. 编解码器的作用**

**编解码器** 是用于在数据传输过程中将高层协议数据转换为底层传输格式（编码），以及将接收到的底层数据恢复为高层协议数据（解码）的工具。

在 RPC 场景中，客户端和服务端需要通过网络传递 **请求对象（`RpcRequest`）** 和 **响应对象（`RpcResponse`）**。

- **编码器（Encoder）**：
  - 将 Java 对象转换为字节流，以便通过 Netty 发送。
- **解码器（Decoder）**：
  - 将接收到的字节流还原为 Java 对象，供应用逻辑处理。

#### **2. 为什么需要自定义编解码器**

Netty 提供了一些内置的编解码工具（如 `ObjectEncoder` 和 `ObjectDecoder`），虽然简单易用，但存在以下局限性：

**1. 默认序列化方式效率低**

- 默认使用 Java 原生序列化（`ObjectInputStream` 和 `ObjectOutputStream`），性能较差，序列化后的数据较大。
- 不能满足高性能的 RPC 框架需求。

**2. 格式不灵活，难以与其他语言兼容**

- Java 原生序列化方式是专为 Java 设计的，无法与其他语言的客户端或服务端进行通信。

**3. 数据传输容易出现粘包/拆包问题**

- TCP 是流式协议，数据传输时容易出现多个消息被拼接或分拆的问题。自定义编解码器可以通过消息头部添加长度字段来解决。

**4. 无法支持多种序列化格式**

- 不支持其他更高效的序列化方式（如 JSON、Protobuf、Avro 等）。自定义编解码器可以为不同格式提供支持。

#### **3. 序列化器的作用**

**序列化器** 是编解码器的核心，用于将对象转换为字节数组（序列化），以及将字节数组还原为对象（反序列化）。

##### **为什么需要自定义序列化器**

1. **支持多种序列化协议**：
   - 可以灵活切换序列化方式（如 JSON、Protobuf、Avro 等），提升扩展性和兼容性。
2. **优化性能**：
   - 采用更高效的序列化协议（如 Protobuf）以减少序列化时间和传输数据大小。
3. **跨语言支持**：
   - 使用如 JSON 或 Protobuf 的序列化方式，可以实现 Java 与其他语言（如 Python、Go 等）之间的通信。

#### **4. 简单来说为什么需要自定义编解码器**

通过自定义编解码器和序列化器，可以实现：

1. **高性能**：
   - 替换原生序列化，支持更高效的协议（如 Protobuf、Avro）。
2. **灵活性**：
   - 编解码器可以对消息增加额外信息（如消息长度、校验码等），便于数据传输和解析。
3. **跨语言兼容**：
   - 使用 JSON 或 Protobuf 支持多语言通信。
4. **解决粘包/拆包问题**：
   - 在编码时添加消息长度字段，解码时依据长度字段分离消息。

#### **总结**

在 RPC 框架中，使用 Netty 自定义编解码器和序列化器是为了：

1. 提升数据传输效率（通过优化序列化方式）。
2. 提供更强的扩展性（支持多种序列化协议）。
3. 保证数据传输的完整性和正确性（通过长度字段解决粘包/拆包问题）。
4. 支持跨语言通信。

通过自定义设计，编解码器可以完美适配实际需求，提供高效的 RPC 通信能力。

### 编解码器和序列化的关系

在分布式系统中，**编解码器** 和 **序列化** 是网络通信中两个密切相关的概念。两者在数据传输中扮演不同但相辅相成的角色。

- **编解码器** 是消息整体传输协议的一部分，负责处理消息的结构（如长度字段、校验码等）。
- **序列化器** 是编解码器的工具，专注于对象与字节流之间的转换。
- 两者的关系：
  - **编解码器调用序列化器完成核心数据的序列化与反序列化**。
- 分离的好处：
  - 提升模块化设计的灵活性和扩展性。
  - 适应多种序列化协议和复杂的网络传输场景。

## 4.2 自定义解码器与编码器

### netty中的重要组件

**ChannelHandler 与 ChannelPipeline** ChannelHandler 是对 Channel 中数据的处理器，这些处理器可以是系统本身定义好的编解码器，也可以是用户自定义的。这些处理器会被统一添加到一个 ChannelPipeline 的对象中，然后按照添加的类别对 Channel 中的数据进行依次处理。

在上一节中，我们使用netty自带的编码器和解码器 来实现数据的传输

而在这里，我们可以通过**继承netty提供的基类**，实现自定义的编码器和解码器

在common包下建立serializer包，实现自定义编码器，解码器和序列化器

### MessageType

#### 概述

`MessageType` 是一个 **枚举类**，用于定义消息的类型。在 RPC 框架中，它用来区分消息是请求类型（`RpcRequest`）还是响应类型（`RpcResponse`）。

通过这种方式，可以为每种消息分配一个唯一的整数编码（`code`），便于在网络通信中传递和解析消息类型。

#### 代码

```java
package com.async.rpc.common.message;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/17
 */

import lombok.AllArgsConstructor;

/**
 * @program: simple_RPC
 *
 * @description: 指定消息的数据类型
 **/
@AllArgsConstructor
public enum MessageType {
    REQUEST(0),RESPONSE(1);
    private int code;
    public int getCode(){
        return code;
    }
}
```

#### 总结

**MessageType 的意义**：

- 定义了消息类型（请求和响应）以及其对应的整数编码。
- 提供统一的消息类型管理，提升代码的可读性和可维护性。

**优点**：

- 避免硬编码数字常量（如 `0`、`1`），改用枚举使代码更具可读性。
- 枚举类型的类型安全性降低了误用的可能性。





### 自定义编码器myEncoder

#### **概述**

`MyEncoder` 是一个自定义的 **Netty 编码器**，继承自 `MessageToByteEncoder`，用于将高层数据（如 `RpcRequest` 和 `RpcResponse`）编码成字节流，以便在网络上传输。编码时，它将以下信息顺序写入到 `ByteBuf` 中：

1. 消息类型
2. 序列化方式
3. 数据长度
4. 序列化后的数据

#### 代码

```java
/**
 * @program: simple_RPC
 *
 * @description: 自定义编码器
 **/
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {
    //MessageToByteEncoder--Netty提供的编码器基类，用于将消息对象编码为字节流。
    //其子类需要实现 encode() 方法完成具体编码逻辑。
    private Serializer serializer;
    @Override
    //Netty 在发送消息时会自动调用encode方法。
    //ctx：当前通道的上下文，用于管理通道相关的资源。
    //msg：要编码的消息对象（如 RpcRequest 或 RpcResponse）。
    //out：Netty 提供的 ByteBuf，用于存储编码后的字节数据。
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        System.out.println(msg.getClass());
        //1.写入消息类型，用于区分是请求（RpcRequest）还是响应（RpcResponse）。
        // MessageType 是一个枚举，包含请求和响应的类型码。
        if(msg instanceof RpcRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }
        else if(msg instanceof RpcResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }
        //2.写入序列化方式，目的是写入序列化方式的类型码，用于客户端和服务端解析消息时知道使用何种序列化方式
        // out.writeShort：将序列化方式（2 字节）写入 ByteBuf。
        // serializer.getType()：返回当前序列化器的类型（例如：JSON、Protobuf）。
        out.writeShort(serializer.getType());
        // 得到序列化数组
        // serializer.serialize(msg)：将消息对象序列化为字节数组。
        byte[] serializeBytes = serializer.serialize(msg);
        //3.写入数据长度，用于接收端知道需要读取多少字节。
        // out.writeInt(): 将数据长度（4 字节）写入 ByteBuf。
        out.writeInt(serializeBytes.length);
        //4.写入实际的序列化数据，作为消息的主体内容。
        //out.writeBytes():将字节数组的内容逐字节写入 ByteBuf。
        out.writeBytes(serializeBytes);
    }
}
```

#### **完整的数据格式**

最终编码后的数据在 `ByteBuf` 中的结构为：

| 字段           | 字节数 | 描述                                              |
| -------------- | ------ | ------------------------------------------------- |
| **消息类型**   | 2 字节 | 区分请求（`RpcRequest`）和响应（`RpcResponse`）。 |
| **序列化方式** | 2 字节 | 指定序列化器类型（如 JSON、Protobuf）。           |
| **数据长度**   | 4 字节 | 指定实际数据的字节长度。                          |
| **序列化数据** | 不固定 | 具体的序列化字节数组。                            |

#### **总结**

1. **功能概览**：
   - `MyEncoder` 将高层对象（`RpcRequest` 或 `RpcResponse`）编码为适合网络传输的字节流。
   - 通过消息类型、序列化方式和数据长度，保证接收端可以正确解析消息。
2. **关键点**：
   - 写入消息类型用于区分请求和响应。
   - 写入序列化方式支持多种序列化协议。
   - 写入数据长度解决 TCP 粘包/拆包问题。
3. **扩展性**：
   - 编码器与序列化器解耦，支持灵活更换序列化方式。
   - 新增消息类型时只需修改 `MessageType` 枚举即可。

### 自定义解码器myDecoder

#### **概述**

`MyDecoder` 是一个自定义的 **Netty 解码器**，继承自 `ByteToMessageDecoder`，用于将接收到的字节流解析为高层对象（如 `RpcRequest` 和 `RpcResponse`）。在解码过程中：

1. 按顺序解析消息类型、序列化方式、数据长度和具体数据。
2. 根据解析结果恢复为相应的 Java 对象。

#### 代码

```java
package com.async.rpc.common.serializer.myCoder;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/17
 */

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import com.async.rpc.common.message.MessageType;
import java.util.List;
import com.async.rpc.common.serializer.mySerializer.Serializer;
/**
 * @program: simple_RPC
 *
 * @description: 自定义解码器,按照自定义的消息格式解码数据
 **/
//ByteToMessageDecoder:Netty 提供的解码器基类，用于将字节流解析为 Java 对象。
public class MyDecoder extends ByteToMessageDecoder {
    //decode() 方法完成具体解码逻辑。
    @Override
    //channelHandlerContext：通道上下文，管理通道的生命周期和资源。
    //in：Netty 提供的 ByteBuf，表示接收到的数据缓冲区。
    //out：存储解码后的对象，解码完成后会传递给后续的处理器。
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        //1.读取消息类型例如：REQUEST还是RESPONSE
        short messageType = in.readShort();
        // 现在还只支持request与response请求
        if(messageType != MessageType.REQUEST.getCode() &&
                messageType != MessageType.RESPONSE.getCode()){
            System.out.println("暂不支持此种数据");
            return;
        }
        //2.读取序列化的方式&类型
        // 从缓冲区中读取消息类型（2 字节），判断是请求（RpcRequest）还是响应（RpcResponse）。
        short serializerType = in.readShort();
        // 通过消息类型，获取对应的序列化器（如 JSON、Protobuf）。
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if(serializer == null)
            throw new RuntimeException("不存在对应的序列化器");
        //3.读取序列化数组长度（4 字节），用于确定需要读取的字节数。
        // 如果长度信息不完整，ByteToMessageDecoder 会自动等待更多数据到达。
        int length = in.readInt();
        //4.读取序列化数组
        byte[] bytes=new byte[length];
        // 从缓冲区中读取指定长度的字节数据，作为序列化后的数据内容。
        // 将 length 个字节读取到 bytes 数组中。
        in.readBytes(bytes);
        // 使用对应的序列化器将字节数组解析为 Java 对象（如 RpcRequest 或 RpcResponse）。
        Object deserialize= serializer.deserialize(bytes, messageType);
        // 将解码后的对象添加到 out 列表中，供后续处理器使用。
        out.add(deserialize);
    }
}
```



#### **完整流程总结**

1. **读取消息类型**：
   - 判断是请求还是响应，确保支持的消息类型。
2. **读取序列化方式**：
   - 动态获取序列化器实例，用于解析后续的数据。
3. **读取数据长度**：
   - 确定需要读取的字节数，避免数据不完整。
4. **读取序列化数据**：
   - 获取字节数组，作为序列化后的数据内容。
5. **反序列化为对象**：
   - 使用序列化器将字节数组还原为 Java 对象，并传递给后续处理器。

#### **扩展点**

- 支持更多消息类型：
  - 扩展 `MessageType` 枚举，支持心跳消息、错误消息等。
- 多种序列化方式：
  - 增加序列化器类型（如 Kryo、Avro），提升性能。
- 优化错误处理：
  - 针对不支持的消息类型和序列化器提供更清晰的异常提示。



## 4.3 自定义序列化器

### 4.3.1 自定义Json序列化器

#### 概述

`JsonSerializer` 是实现了 `Serializer` 接口的序列化器，用于将对象与字节数组之间进行转换。该类使用 **FastJSON** 作为序列化工具，并根据消息类型（`messageType`）对不同类型的消息（`RpcRequest` 或 `RpcResponse`）进行特殊处理

#### 代码

```java
package com.async.rpc.common.serializer.mySerializer;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/17
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.async.rpc.common.message.RpcRequest;
import com.async.rpc.common.message.RpcResponse;

/**
 * @program: simple_RPC
 *
 * @description: Json格式的序列化器
 **/
public class JsonSerializer implements Serializer {
    //在对象与字节数组之间序列化与反序列化，以便通过网络传输。
    @Override
    public byte[] serialize(Object obj) {
        //使用 FastJSON 将对象转换为 JSON 字符串并序列化为字节数组。
        byte[] bytes = JSONObject.toJSONBytes(obj);
        return bytes;
    }
    // 根据消息类型 messageType，将字节数组反序列化为对应的对象（RpcRequest 或 RpcResponse）。
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        // 传输的消息分为request与response
        switch (messageType){
            case 0://messageType是request，将字节数组解析为RPCRequest对象
                RpcRequest request = JSON.parseObject(bytes, RpcRequest.class);
                //初始化 objects 数组，用于存储解析后的参数。
                Object[] objects = new Object[request.getParams().length];
                // 把json字串转化成对应的对象， fastjson可以读出基本数据类型，不用转化
                // 对转换后的request中的params属性逐个进行类型判断
                //遍历请求参数（params），逐一校验其实际类型是否与参数类型数组（paramsType）中的类型一致。
                for(int i = 0; i < objects.length; i++){
                    Class<?> paramsType = request.getParamsType()[i];
                    //判断每个对象类型是否和paramsTypes中的一致
                    //使用 isAssignableFrom 检查参数的实际类型是否可以赋值给目标类型。（判断一个类是否是另一个类的父类或接口。）
                    if (!paramsType.isAssignableFrom(request.getParams()[i].getClass())){
                        //如果不一致，就行进行类型转换
                        // 将数据强转为JSONObject吼用fastjson提供的方法将其转为目标类型。
                        objects[i] = JSONObject.toJavaObject((JSONObject) request.getParams()[i],request.getParamsType()[i]);
                    }else{
                        //如果一致就直接赋给objects[i]
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                obj = request;
                break;
            case 1://messageType是response，使用 FastJSON 将字节数组解析为 RpcResponse 对象。
                RpcResponse response = JSON.parseObject(bytes, RpcResponse.class);
                Class<?> dataType = response.getDataType();
                //判断转化后的response对象中的data的类型是否正确
                if(! dataType.isAssignableFrom(response.getData().getClass())){
                    response.setData(JSONObject.toJavaObject((JSONObject) response.getData(),dataType));
                }
                obj = response;
                break;
            default:
                System.out.println("暂时不支持此种消息");
                throw new RuntimeException();
        }
        return obj;
    }
    //1 代表json序列化方式
    @Override
    public int getType() {
        return 1;
    }
}
```

这段代码中的类型转换逻辑主要针对方法参数，分为两种情况：

1. **基本数据类型**：  
   - 如 `int`, `float`, `boolean` 等，FastJSON 会自动解析为对应的基本数据类型或其包装类（如 `Integer`）。  
   - **无需转换**，直接赋值即可。

2. **引用类型**：  
   - 对于非基本数据类型（如自定义类、集合类型），FastJSON 默认解析为通用类型（如 `JSONObject`）。  
   - 如果实际类型与目标类型不一致，则通过 `toJavaObject` 将 `JSONObject` 转换为目标类型。

**代码逻辑**：  

- 遍历参数，检查实际类型和目标类型是否一致：  
  - **一致**：直接赋值。  
  - **不一致**：使用 `toJavaObject` 转换为目标类型。  

这样确保了参数类型与方法签名一致，支持复杂参数的动态类型处理。

同时需要在RPCResponse中补充：

```java
//更新：加入传输数据的类型，以便在自定义序列化器中解析
private Class<?> dataType;
```

### 4.3.2 原生Java序列化器

这个类实现了 `Serializer` 接口，使用 Java 原生序列化机制实现了对象和字节数组之间的相互转换。

```java
package com.async.rpc.common.serializer.mySerializer;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/17
 */

import java.io.*;

/**
 * @program: simple_RPC
 *
 * @description: Java原生的序列化器
 **/
public class ObjectSerializer implements Serializer {
    //利用Java io 对象 -》字节数组
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes=null;// 用于存储序列化后的字节数组
        ByteArrayOutputStream bos=new ByteArrayOutputStream();// 用于存储字节数据的缓冲区
        try {
            //是一个对象输出流，用于将 Java 对象序列化为字节流，并将其连接到bos上
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            //刷新 ObjectOutputStream，确保所有缓冲区中的数据都被写入到底层流中。
            oos.flush();
            //将bos其内部缓冲区中的数据转换为字节数组
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    //字节数组 -》对象
    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }

    //0 代表Java 原生序列器
    @Override
    public int getType() {
        return 0;
    }
}
```



### 4.3.3 自定义protobuf序列化器

略

**如果希望支持动态切换：**

此点作为优化项，还要优化序列化器的动态选择，在 `NettyClientInitializer` 和 `NettyServerInitializer` 中不需要直接指定序列化器，而是依赖消息中携带的 `serializerType`。

- 在 `RpcRequest` 或 `RpcResponse` 消息中加入一个字段 `serializerType`，表示序列化方式。
- 客户端发送消息时，将序列化器类型标记为 `JSON` 或 `Java`。
- 服务端解码时，根据 `serializerType` 动态选择对应的序列化器进行反序列化。

在原代码中采用了

**静态配置，代码直接指定**：

- 在 NettyClientInitializer和 NettyServerInitializer中硬编码序列化器实例，例如：

  ```java
  javaCopy codepipeline.addLast(new MyEncoder(new JsonSerializer()));
  pipeline.addLast(new MyDecoder());
  ```

### 4.3.4 更改NettyInitializer 

下述代码是客户端和服务端初始化 Netty 通信管道（`ChannelPipeline`）的实现，主要目的是设置数据传输过程中使用的处理器（Handler），包括自定义的编码器、解码器和业务逻辑处理器。

#### NettyClientInitializer 

```java
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;  // 服务提供者，用于注册和管理本地服务实例

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();  // 获取当前通道的处理器链（管道）

        // 添加解码器：将字节流解码为 RpcRequest 或 RpcResponse 对象
        pipeline.addLast(new MyDecoder());

        // 添加编码器：将 RpcRequest 或 RpcResponse 对象编码为字节流，使用 JsonSerializer 序列化
        pipeline.addLast(new MyEncoder(new JsonSerializer()));
        
        // 添加客户端业务逻辑处理器：处理从服务端接收的响应
        pipeline.addLast(new NettyClientHandler());
    }
}
```

#### NettyServerInitializer 

```java
@AllArgsConstructor
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServiceProvider serviceProvider;  // 服务提供者，用于注册和管理本地服务实例

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();  // 获取当前通道的处理器链
        
        // 添加编码器：将服务端生成的 RpcResponse 对象编码为字节流
        pipeline.addLast(new MyEncoder(new JsonSerializer()));
        
        // 添加解码器：将接收到的字节流解码为 RpcRequest 对象
        pipeline.addLast(new MyDecoder());
        
        // 添加服务端业务逻辑处理器：处理客户端请求，调用本地服务并返回结果
        pipeline.addLast(new NettyRPCServerHandler(serviceProvider));
    }
}

```

记得还要更新message中的RpcResponse里的dataType

```java
//定义返回信息格式RpcResponse（类似http格式）
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RpcResponse implements Serializable {
    //状态码
    private int code;
    //状态信息
    private String message;
    //具体数据
    private Object data;
    //更新：加入传输数据的类型，以便在自定义序列化器中解析
    private Class<?> dataType;
    //构造成功信息
    public static RpcResponse sussess(Object data) {
        RpcResponse response = RpcResponse.builder()
                .code(200)
                .data(data)
                //增加了数据类型后更新
                .dataType(data != null ? data.getClass() : null) // 设置返回数据类型
                .build();
        return response;
    }
    //构造失败信息
    public static RpcResponse fail(){
        return RpcResponse.builder().code(500).message("服务器发生错误").build();
    }
}
```



# 5. 客户端建立本地服务缓存并实现动态更新

现有的本地服务缓存机制存在一个比较大的问题，那就是本地服务缓存和Zookeeper服务的数据一致性问题。如果采用经典的旁路缓存策略（cache aside）：首先在本地缓存中读，读不到再去zk更新。就会存在诸多问题，如：服务更新了一个新地址，而原地址已经在本地缓存中，一直可用，那么新配置的服务地址就一直未被启用，不能做到负载均衡；

这个问题和其它的数据一致性问题，如Redis缓存和后端关系型数据库之间的数据一致性问题，大体相同但是又有一定区别；总结而言，区别是：Redis和关系型数据库之间的配合，所有客户端都需要先和Redis做交互，而本地缓存的zk中心的不同点在于，新服务提供者直接向zk中心注册服务（写操作），而不需要经过本地缓存；所以这个问题和传统的本地缓存和云端服务器一致性问题是差不多的。最简单的方式是轮询更新，我们这里用到比轮询更好的方式，那就是使用zookeeper的watcher机制来做服务的订阅更新。其实这就是设计模式中观察者模式在此处的应用。同样，它也类似与redis与数据库之间用消息队列来通知更新最新数据的模式，而其实zk中其实也用到了消息队列kafka（最新没有）。下

此处本项目的解决方案就是——使用zookeeper的watcher机制来做事件监听；

### 5.1 事件监听机制

#### **watcher概念**

- `zookeeper`提供了数据的`发布/订阅`功能，多个订阅者可同时监听某一特定主题对象，当该主题对象的自身状态发生变化时例如节点内容改变、节点下的子节点列表改变等，会实时、主动通知所有订阅者
- `zookeeper`采用了 `Watcher`机制实现数据的发布订阅功能。该机制在被订阅对象发生变化时会异步通知客户端，因此客户端不必在 `Watcher`注册后轮询阻塞，从而减轻了客户端压力
- `watcher`机制事件上与观察者模式类似，也可看作是一种观察者模式在分布式场景下的实现方式

#### watcher架构

`watcher`实现由三个部分组成

- `zookeeper`服务端
- `zookeeper`客户端
- 客户端的`ZKWatchManager对象`

客户端**首先将 `Watcher`注册到服务端**，同时将 `Watcher`对象**保存到客户端的`watch`管理器中**。当`Zookeeper`服务端监听的数据状态发生变化时，服务端会**主动通知客户端**，接着客户端的 `Watch`管理器会**触发相关 `Watcher`**来回调相应处理逻辑，从而完成整体的数据 `发布/订阅`流程

![image-20241118200512778](./assets/image-20241118200512778.png)

## 5.2 **watchZK 监听zookeeper的实现**

代码功能：创建与zk间的连接，利用监听器监控zk路径下的事件发生，根据事件发生类型更新本地缓冲中的服务列表。

```java
package com.async.rpc.client.serviceCenter.ZKWatcher;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/18
 */

import com.async.rpc.client.cache.serviceCache;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

/**
 * @program: simple_RPC
 *
 * @description: 监听ZK的节点更新
 **/
public class watchZK {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //本地缓存
    serviceCache cache;

    public watchZK(CuratorFramework client,serviceCache  cache){
        this.client=client;
        this.cache=cache;
    }

    /**
     * 监听当前节点和子节点的 更新，创建，删除
     * @param path
     */
    public void watchToUpdate(String path) throws InterruptedException {
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData childData, ChildData childData1) {
                // 第一个参数：事件类型（枚举）
                // 第二个参数：节点更新前的状态、数据
                // 第三个参数：节点更新后的状态、数据
                // 创建节点时：节点刚被创建，不存在 更新前节点 ，所以第二个参数为 null
                // 删除节点时：节点被删除，不存在 更新后节点 ，所以第三个参数为 null
                // 节点创建时没有赋予值 create /curator/app1 只创建节点，在这种情况下，更新前节点的 data 为 null，获取不到更新前节点的数据
                switch (type.name()) {
                    case "NODE_CREATED": // 监听器第一次执行时节点存在也会触发次事件
                        //获取更新的节点的路径
                        String path=new String(childData1.getPath());
                        //按照格式 ，读取
                        String[] pathList= path.split("/");
                        if(pathList.length<=2) break;
                        else {
                            String serviceName=pathList[1];
                            String address=pathList[2];
                            //将新注册的服务加入到本地缓存中
                            cache.addServcieToCache(serviceName,address);
                        }
                        break;
                    case "NODE_CHANGED": // 节点更新
                        if (childData.getData() != null) {
                            System.out.println("修改前的数据: " + new String(childData.getData()));
                        } else {
                            System.out.println("节点第一次赋值!");
                        }
                        System.out.println("修改后的数据: " + new String(childData1.getData()));
                        break;
                    case "NODE_DELETED": // 节点删除
                        String path_d=new String(childData.getPath());
                        //按照格式 ，读取
                        String[] pathList_d= path_d.split("/");
                        if(pathList_d.length<=2) break;
                        else {
                            String serviceName=pathList_d[1];
                            String address=pathList_d[2];
                            //将新注册的服务加入到本地缓存中
                            cache.delete(serviceName,address);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        //开启监听
        curatorCache.start();
    }
}
```

## 5.3 **在ZKServiceCenter 加入缓存和监听器**

更改ZKServiceCenter中的服务发现serviceDiscovery方法，增加本地缓存的查找逻辑，找不到再去zk服务器中找（其实只有初次启动会执行并在zk中找到，因为如果监听器正常，那么后续的更新都会通知到本地缓存，后续本地缓存中没有的，zk中也没有）：

```java
    //服务发现方法
    //向zk注册中心发起查询，根据服务名（接口名）返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            //新增的本地缓存，先从本地缓存中找
            List<String> serviceList=cache.getServcieFromCache(serviceName);
            //如果找不到，再去zookeeper中找
            // 获取 Zookeeper 中指定服务名称的子节点列表
            //客户端通过服务名 serviceName 向 Zookeeper 查询注册的服务实例列表。
            //本地缓存更新版本：如果本地服务列表为空，则向zk查询，返回结果是一个服务实例的地址列表（如 192.168.1.100:8080）。
            if(serviceList==null){
                serviceList = client.getChildren().forPath("/" + serviceName);
            }
//            List<String> serviceList = client.getChildren().forPath("/" + serviceName);

            // 检查列表是否为空——如果不检查若列表为空，后续get(0)则会报异常
            if (serviceList == null || serviceList.isEmpty()) {
                System.err.println("No available instances for service: " + serviceName);
                return null; // 或者你可以抛出一个自定义的异常来告知调用者
            }

            // 选择一个服务实例，默认用第一个节点，后续可以加入负载均衡机制
            String string = serviceList.get(0);

            // 解析并返回 InetSocketAddress
            // 将字符串形式的地址（如 192.168.1.100:8080）转换为 InetSocketAddress，便于后续网络连接。
            return parseAddress(string);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
            return null; // 或者根据需求返回一个默认的 InetSocketAddress
        }
    }
```

