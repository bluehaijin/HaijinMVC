package xyz.haijin.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: RequestMapingMap
 * @Description: 存储方法的访问路径
 * @author: haijin
 *
 */
public class RequestMapingMap {

    private static Map<String, String> packageNameMap = new HashMap<>();//访问的controller路径

    public static String getPackageName(String path) {
        return packageNameMap.get(path);
    }

    public static void putPackageName(String path, String packageName) {
        packageNameMap.put(path, packageName);
    }



    private static Map<String, String> encodingMap = new HashMap<>();//访问的controller编码

    public static String getEncoding(String path) {
        return encodingMap.get(path);
    }

    public static void putEncoding(String path, String packageName) {
        encodingMap.put(path, packageName);
    }

    /**
     * @Field: requesetMap
     *          用于存储方法的访问路径
     */
    private static Map<String, Class<?>> requesetMap = new HashMap<String, Class<?>>();

    public static Class<?> getClassName(String path) {
        return requesetMap.get(path);
    }

    public static void put(String path, Class<?> className) {
        requesetMap.put(path, className);
    }

    public static Map<String, Class<?>> getRequesetMap() {
        return requesetMap;
    }
}
