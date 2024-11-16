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
    //集合中存放服务的实例
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

    public ServiceProvider(String host,int port){
        //需要传入服务端自身的网络地址
        this.host=host;
        this.port=port;
        this.interfaceProvider=new HashMap<>();
        this.serviceRegister=new ZKServiceRegister();
    }
    //这部分有点不理解？
    public void provideServiceInterface(Object service){
        String serviceName=service.getClass().getName();
        Class<?>[] interfaceName=service.getClass().getInterfaces();

        for (Class<?> clazz:interfaceName){
            //本机的映射表
            interfaceProvider.put(clazz.getName(),service);
            //在注册中心注册服务
            serviceRegister.register(clazz.getName(),new InetSocketAddress(host,port));
        }
    }
    //获取服务实例
    public Object getService(String interfaceName){
        return interfaceProvider.get(interfaceName);
    }
}

