package org.crazycake.shiroredisspringboottutorial;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReflectionUtil {

    public static Class<?> getClass(String clazzName) {
        Class<?> clazz = null;
        if (clazzName != null) {
            try {
                clazz = Class.forName(clazzName);
            } catch (Exception e) {
                clazz = makeClass(clazzName);
            }
        }
        return clazz;
    }

    private static Class makeClass(String clazzName) {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.makeClass(clazzName);
        Class clazz;
        try {
            clazz = ctClass.toClass();
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
        ctClass.defrost();
        return clazz;
    }

    public static Object getInstance(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create instance of " + className, e);
        }
    }

    public static byte[] getClassByteArray(Class clazz) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get(clazz.getName());
        return ctClass.toBytecode();
    }

    public static void setFieldValue(Object obj, String fieldname, Object values) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldname);
        field.setAccessible(true);
        field.set(obj, values);
    }

    public static Object getFieldValue(Object obj, String fieldname) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldname);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static String generateClassNameFrom(String packageName) {
        Set<String> classNameSet = getClassNameSetFrom(packageName);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String name1, name2;
        do {
            name1 = getRandomElement(classNameSet, random);
            name2 = getRandomElement(classNameSet, random);
        } while (name1.equals(name2));

        String newName = getFirstThreePackage(name1) + getLastThreePackage(name2);

        if (classNameSet.contains(newName)) {
            return generateClassNameFrom(packageName);
        } else {
            return newName;
        }
    }

    public static Set<String> getClassNameSetFrom(String packageName) {
        Set<String> classSet = new HashSet<>();
        String packagePath = packageName.replace('.', '/');
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol();
                if (protocol.equals("jar")) {
                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    if (jarURLConnection != null) {
                        JarFile jarFile = jarURLConnection.getJarFile();
                        if (jarFile != null) {
                            Enumeration<JarEntry> jarEntries = jarFile.entries();
                            while (jarEntries.hasMoreElements()) {
                                JarEntry jarEntry = jarEntries.nextElement();
                                String jarEntryName = jarEntry.getName();
                                if (jarEntryName.endsWith(".class")) {
                                    String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                    if (!className.contains("$") && className.startsWith(packageName)) {
                                        classSet.add(className);
                                    }
                                }
                            }
                        }
                    }
                } else if (protocol.equals("file")) {
                    listClassesInDirectory(new File(url.getFile()), classSet, packageName);
                }
            }
        } catch (IOException ignored) {
        }
        return classSet;
    }

    // 方便不编译成 jar 时调试
    private static void listClassesInDirectory(File directory, Set<String> classNames, String packageName) {
        File[] files = directory.listFiles();
        String SYMBLE = System.getProperty("os.name").toLowerCase().contains("win") ? "\\" : "/";
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    listClassesInDirectory(file, classNames, packageName);
                } else if (file.getName().endsWith(".class")) {
                    String fullName = file.getPath().replace(".class", "");
                    String path = packageName.replace(".", SYMBLE);
                    classNames.add(fullName.substring(fullName.indexOf(path)).replace(SYMBLE, "."));
                }
            }
        }
    }

    private static String getRandomElement(Set<String> set, ThreadLocalRandom random) {
        return set.stream().skip(random.nextInt(set.size())).findFirst().orElse(null);
    }

    private static String getFirstThreePackage(String className) {
        int index = className.indexOf(".", 11);
        return className.substring(0, index);
    }

    private static String getLastThreePackage(String className) {
        String temp = className.substring(0, className.lastIndexOf("."));
        temp = temp.substring(0, temp.lastIndexOf("."));
        temp = temp.substring(0, temp.lastIndexOf("."));
        className = className.substring(temp.length());
        return className;
    }
}
