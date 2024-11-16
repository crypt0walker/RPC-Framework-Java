package com.async.rpc.server.server.work;
/**
 * @author async
 * @github crypt0walker
 * @date 2024/11/3
 */

import com.async.rpc.common.message.RpcRequest;
import com.async.rpc.common.message.RpcResponse;
import com.async.rpc.server.provider.ServiceProvider;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @program: simple_RPC
 *
 * @description: 负责启动线程和客户端进行数据传输
 **/
@AllArgsConstructor
public class WorkThread implements Runnable{
    private Socket socket;
    private ServiceProvider serviceProvide;
    @Override
    public void run() {
        try {
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            //读取客户端传过来的request(反序列化读取)
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            //反射调用服务方法获取返回值
            RpcResponse rpcResponse=getResponse(rpcRequest);
            //向客户端写入response（序列化写入）（会先写到发送的缓冲区，调用flush会将缓冲区中内容全部发送）
            oos.writeObject(rpcResponse);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private RpcResponse getResponse(RpcRequest rpcRequest){
        //得到服务名
        String interfaceName=rpcRequest.getInterfaceName();
        //serviceProvide提供了可提供服务的Map，所以先获取rpcRequest中的接口名，以得到服务端相应服务实现类
        Object service = serviceProvide.getService(interfaceName);
        //反射调用方法，Method类是反射API中内置的类，相似的还有Class，Field，Constructor等
        Method method=null;
        try {
            //通过 Method 对象获取方法的名称、返回类型、参数类型、修饰符等详细信息。
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
