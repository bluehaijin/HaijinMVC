package xyz.haijin.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import xyz.haijin.annotation.Controller;
import xyz.haijin.annotation.Encoding;
import xyz.haijin.annotation.RequestMapping;
import xyz.haijin.ioc.context.HaijinContext;
import xyz.haijin.json.JSONObject;
import xyz.haijin.util.BeanUtils;
import xyz.haijin.util.RequestMapingMap;
import xyz.haijin.util.ScanClassUtil;
import xyz.haijin.web.context.WebContext;
import xyz.haijin.web.view.DispatchActionConstant;
import xyz.haijin.web.view.View;

/**
 * 作为自定义注解的核心处理器以及负责调用目标业务方法处理用户请求
 */
public class AnnotationHandleServlet extends HttpServlet {

    private String pareRequestURI(HttpServletRequest request){
        String path = request.getContextPath()+"/";
        String requestUri = request.getRequestURI();
        String midUrl = requestUri.replaceFirst(path, "");
        String lasturl = midUrl.substring(0, midUrl.lastIndexOf("."));
        return lasturl;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            this.excute(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            this.excute(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void excute(HttpServletRequest request, HttpServletResponse response) throws Exception {

        //将当前线程中HttpServletRequest对象存储到ThreadLocal中，以便在Controller类中使用
        WebContext.requestHodler.set(request);
        System.out.println("request:"+request.getParameter("name"));
        //将当前线程中HttpServletResponse对象存储到ThreadLocal中，以便在Controller类中使用
        WebContext.responseHodler.set(response);
        //解析url
        String lasturl = pareRequestURI(request);
        //获取要使用的类
        Class<?> clazz = RequestMapingMap.getRequesetMap().get(lasturl);
        //获取包路径
        String packageName = RequestMapingMap.getPackageName(lasturl);
        //获取编码
        String encoding= RequestMapingMap.getEncoding(lasturl);

        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");

        if(encoding != null && !"".equals(encoding.toLowerCase().trim()) ){
            String encodingTmp = encoding.toLowerCase().trim();
            if("gbk".equals(encodingTmp) || "utf-8".equals(encodingTmp)){
                request.setCharacterEncoding(encoding.trim());
                response.setCharacterEncoding(encoding.trim());
            }
        }
        //创建类的实例
//        Object classInstance = BeanUtils.instanceClass(clazz);
        HaijinContext haijinContext = HaijinContext.getInstance();
        Object classInstance = haijinContext.autoLoadBean(packageName,HaijinContext.toLowerCaseFirstOne(clazz.getSimpleName()));
        //获取类中定义的方法
        Method [] methods = BeanUtils.findDeclaredMethods(clazz);
        Method method = null;
        for(Method m:methods){//循环方法，找匹配的方法进行执行
            if(m.isAnnotationPresent(RequestMapping.class)){
                String anoPath = m.getAnnotation(RequestMapping.class).value();
                if(anoPath!=null && !"".equals(anoPath.trim()) && lasturl.equals(anoPath.trim())){
                    //找到要执行的目标方法
                    method = m;
                    break;
                }
            }
        }
        try {
            if(method!=null){
                //执行目标方法处理用户请求
                Object retObject = method.invoke(classInstance);
                //如果方法有返回值，那么就表示用户需要返回视图
                if (retObject!=null) {
                    if (retObject instanceof View) {
                        View view = (View)retObject;
                        //判断要使用的跳转方式
                        if(view.getDispathAction().equals(DispatchActionConstant.FORWARD)){
                            //使用服务器端跳转方式
                            request.getRequestDispatcher(view.getUrl()).forward(request, response);
                        }else if(view.getDispathAction().equals(DispatchActionConstant.REDIRECT)){
                            //使用客户端跳转方式
                            response.sendRedirect(request.getContextPath()+view.getUrl());
                        }else{
                            request.getRequestDispatcher(view.getUrl()).forward(request, response);
                        }
                    } if (retObject instanceof JSONObject) {
                        printWriter(response,retObject+"");
                    }
                    else {
                        String json = (String) retObject;
                        printWriter(response,json);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void printWriter(HttpServletResponse resp,String json) throws IOException {
        PrintWriter wp = resp.getWriter();
        wp.write(json);
        wp.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        /**
         * 重写了Servlet的init方法后一定要记得调用父类的init方法，
         * 否则在service/doGet/doPost方法中使用getServletContext()方法获取ServletContext对象时
         * 就会出现java.lang.NullPointerException异常
         */
        super.init(config);
        System.out.println("---初始化开始---");
        //获取web.xml中配置的要扫描的包
        String basePackage = config.getInitParameter("basePackage");
        //如果配置了多个包，例如：<param-value>xyz.haijin.web.controller,xyz.haijin.web.UI</param-value>
        if (basePackage.indexOf(",")>0) {
            //按逗号进行分隔
            String[] packageNameArr = basePackage.split(",");
            for (String packageName : packageNameArr) {
                initRequestMapingMap(packageName);
            }
        }else {
            initRequestMapingMap(basePackage);
        }
        System.out.println("----初始化结束---");
    }

    /**
     * @Method: initRequestMapingMap
     * @Description:添加使用了Controller注解的Class到RequestMapingMap中
     * @Anthor:haijin
     * @param packageName
     */
    private void initRequestMapingMap(String packageName){
        System.out.println("packageName:"+packageName);
        Set<Class<?>> setClasses =  ScanClassUtil.getClasses(packageName);
        for (Class<?> clazz :setClasses) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                Method [] methods = BeanUtils.findDeclaredMethods(clazz);
                for(Method m:methods){//循环方法，找匹配的方法进行执行
                    if(m.isAnnotationPresent(RequestMapping.class)){
                        String anoPath = m.getAnnotation(RequestMapping.class).value();
                        String encoding = m.getAnnotation(Encoding.class) != null ? m.getAnnotation(Encoding.class).value() : null;
                        if(anoPath!=null && !"".equals(anoPath.trim())){
                            if (RequestMapingMap.getRequesetMap().containsKey(anoPath)) {
                                throw new RuntimeException("RequestMapping映射的地址不允许重复！");
                            }
                            RequestMapingMap.put(anoPath, clazz);
                            RequestMapingMap.putPackageName(anoPath,packageName);
                            RequestMapingMap.putEncoding(anoPath,encoding);
                        }
                    }
                }
            }
        }
    }
}