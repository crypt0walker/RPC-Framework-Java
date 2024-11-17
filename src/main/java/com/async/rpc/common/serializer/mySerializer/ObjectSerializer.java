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

