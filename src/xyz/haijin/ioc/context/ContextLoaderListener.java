package xyz.haijin.ioc.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Enumeration;

public class ContextLoaderListener implements ServletContextListener {

    private HaijinContext haijinContext;

    public ContextLoaderListener() {
    }

    /**
     * 当Servlet 容器启动Web 应用时调用该方法。在调用完该方法之后，容器再对Filter 初始化，
     * 并且对那些在Web 应用启动时就需要被初始化的Servlet 进行初始化。
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        haijinContext = HaijinContext.getInstance();
        try {
            ServletContext sc = event.getServletContext();
            Enumeration parameters = sc.getInitParameterNames();
            while(parameters.hasMoreElements()) {
                String parameter = (String)parameters.nextElement();
                //相应的键值对存到map中
                System.out.println("参数名："+parameter+"参数值："+sc.getInitParameter(parameter));
                if("pathPackage".equals(parameter)){
                    String value = sc.getInitParameter(parameter);
                    haijinContext.setPath(value);
//                    if(value.indexOf(",")>0){
//                        //按逗号进行分隔
//                        String[] packageNameArr = value.split(",");
//                        for (String packageName : packageNameArr) {
//                            haijinContext.autoLoadBean(packageName);
//                        }
//                    } else {
//                        System.out.println("value:"+value);
//                        haijinContext.autoLoadBean(value);
//                    }
                }
            }
        } catch(Exception e) {
            System.out.println( e.getMessage());
        }
        System.out.println("初始化调用。。。。。。。。。。。。");
    }


    /**
     * 当Servlet 容器终止Web 应用时调用该方法。在调用该方法之前，容器会先销毁所有的Servlet 和Filter 过滤器。
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        haijinContext = null;
        System.out.println("调用销毁。。。。。。。。。。。。");
    }


}
