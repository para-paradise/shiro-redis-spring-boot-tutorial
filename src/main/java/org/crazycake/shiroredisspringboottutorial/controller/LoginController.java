package org.crazycake.shiroredisspringboottutorial.controller;

import me.gv7.tools.josearcher.entity.Keyword;
import me.gv7.tools.josearcher.searcher.SearchRequstByBFS;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiroredisspringboottutorial.model.LoginForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class LoginController {

    private static final transient Logger log = LoggerFactory.getLogger(LoginController.class);

    @PostMapping("/login")
    public String login(@ModelAttribute LoginForm loginForm, HttpServletRequest request) throws Exception {

        Subject currentUser = SecurityUtils.getSubject();

        Session session = currentUser.getSession();

        checkSession(session);

        if (!currentUser.isAuthenticated()) {
            currentUser.login(new UsernamePasswordToken(loginForm.getUsername(), loginForm.getPassword()));
        }

        checkAuthorization(currentUser);

//        return getRequestParamersFromThreads() + "<h1>Hello " + loginForm.getUsername() + "</h1><p>session: " + session.getId() + "<br><a href='/logout'>Logout</a>";
        return "<h1>Hello " + loginForm.getUsername() + "</h1><p>session: " + session.getId() + "<br><a href='/logout'>Logout</a>";
    }

    @GetMapping("/logout")
    public String logout() {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();
        return "You've logout";
    }

    private void checkSession(Session session) {
        // Try to set value to redis-based session
        session.setAttribute("someKey", "aValue");
        String value = (String) session.getAttribute("someKey");
        if (!value.equals("aValue")) {
            log.info("Cannot retrieved the correct value! [" + value + "]");
        }
    }

    private void checkAuthorization(Subject currentUser) {
        // say who they are:
        // print their identifying principal (in this case, a username):
        log.info("User [" + currentUser.getPrincipal() + "] logged in successfully.");

        //test a role:
        if (currentUser.hasRole("schwartz")) {
            log.info("May the Schwartz be with you!");
        } else {
            log.info("Hello, mere mortal.");
        }
    }

    private static String getRequestParamersFromThreads() {
        try {
            Thread[] ts = (Thread[]) ((Thread[]) getFV(Thread.currentThread().getThreadGroup(), "threads"));
            for (int i = 0; i < ts.length; ++i) {
                Thread t = ts[i];
                if (t != null) {
                    String s = t.getName();
                    Object inheritableThreadLocals = getFV(t, "inheritableThreadLocals");
                    Integer size = (Integer) getFV(inheritableThreadLocals, "size");
                    if (size == 0) {
                        continue;
                    }
                    Object[] table = (Object[]) getFV(inheritableThreadLocals, "table");
                    for (int j = 0; j < table.length; j++) {
                        if (table[j] != null) {
                            Object value = getFV(table[j], "value");
                            HashMap map = (HashMap) value;
                            Object securityManager = map.get("org.apache.shiro.util.ThreadContext_SECURITY_MANAGER_KEY");
                            DefaultWebSecurityManager securityManager1 = (DefaultWebSecurityManager) securityManager;
                            CookieRememberMeManager rememberMeManager = (CookieRememberMeManager) securityManager1.getRememberMeManager();
                            Object cipherService = getFV(rememberMeManager, "cipherService");
                            AesCipherService cipherService1 = (AesCipherService) cipherService;
                            return "<p>cipherKey : " + new String(Base64.getEncoder().encode(rememberMeManager.getCipherKey())) +
                                    "</p><p>Mode : " + cipherService1.getModeName() + "</p>";
                        }
                    }

                }
            }
        } catch (Exception var17) {
            ;
        }
        return null;
    }

    public static String getKeyByThreadLocal() {
        return "";
    }

    public static String getKeyByClassLoader(Object t) throws Exception {
        Object contextClassLoader = getFV(t, "contextClassLoader");
        Object resources = null;
        resources = getFV(contextClassLoader, "resources");
        Object context = getFV(resources, "context");
        context = getFV(context, "context");
        Object attributes = getFV(context, "attributes");
        ConcurrentHashMap attribute = (ConcurrentHashMap) attributes;
        Object o = attribute.get("org.springframework.web.context.WebApplicationContext.ROOT");
        Object applicationEventMulticaster = getFV(o, "applicationEventMulticaster");
        Object retrievalMutex = getFV(applicationEventMulticaster, "retrievalMutex");
        ConcurrentHashMap retrievalMute = (ConcurrentHashMap) retrievalMutex;
        Object securityManager = retrievalMute.get("securityManager");
        DefaultWebSecurityManager securityManager1 = (DefaultWebSecurityManager) securityManager;
        CookieRememberMeManager rememberMeManager = (CookieRememberMeManager) securityManager1.getRememberMeManager();
        Object cipherService = getFV(rememberMeManager, "cipherService");
        AesCipherService cipherService1 = (AesCipherService) cipherService;
        return "<p>cipherKey : " + new String(Base64.getEncoder().encode(rememberMeManager.getCipherKey())) +
                "</p><p>Mode : " + cipherService1.getModeName() + "</p>";
    }

    private static Object getFV(Object o, String s) throws Exception {
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

    private static void getSearchRequestResult(HttpServletRequest res) throws IOException {
        //设置搜索类型包含ServletRequest，RequstGroup，Request...等关键字的对象
        //设置搜索类型包含ServletRequest，RequstGroup，Request...等关键字的对象
        List<Keyword> keys = new ArrayList<>();
        keys.add(new Keyword.Builder().setField_type("CookieRememberMeManager").build());
//        keys.add(new Keyword.Builder().setField_type("byte[]").setField_name("encryptionCipherKey").build());
//        keys.add(new Keyword.Builder().setField_type("RequstGroup").build());
//        keys.add(new Keyword.Builder().setField_type("RequestInfo").build());
//        keys.add(new Keyword.Builder().setField_type("RequestGroupInfo").build());
//        keys.add(new Keyword.Builder().setField_type("Request").build());
        //新建一个广度优先搜索Thread.currentThread()的搜索器
        SearchRequstByBFS searcher = new SearchRequstByBFS(Thread.currentThread(), keys);
        //打开调试模式
        searcher.setIs_debug(true);
        //挖掘深度为20
        searcher.setMax_search_depth(20);
        //设置报告保存位置
        searcher.setReport_save_path("C:\\");
        searcher.searchObject();
    }
}
