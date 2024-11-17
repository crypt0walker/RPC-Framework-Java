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