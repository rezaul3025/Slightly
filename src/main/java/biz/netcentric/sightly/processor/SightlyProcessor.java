/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.netcentric.sightly.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author rkarim
 */
public class SightlyProcessor extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            Class<?> loadedClass = null;
            Map<String, Object> classPropertise = new HashMap<String, Object>();

            Document doc = Jsoup.connect("http://localhost:8080/Sightly").get();
            
            String html = doc.html();
            
            Elements scriptElement = doc.getElementsByTag("script");
            if (scriptElement.attr("type").equals("server/javascript")) {
                String data = scriptElement.html();
                String fullyQualifiedClassName = data.substring(data.indexOf("Packages") + "Packages".length() + 1, data.indexOf(")"));
                System.out.println(fullyQualifiedClassName);
                loadedClass = Class.forName(fullyQualifiedClassName);

                Object object = loadedClass.getConstructor(String.class, String.class, boolean.class, int.class).newInstance("hh", "yy", true, 6);
                for (Method method : loadedClass.getDeclaredMethods()) {

                    if (method.isAccessible()) {
                        Object oblist = method.invoke(object);
                        classPropertise.put(method.getName(), oblist);
                    }
                    System.out.println(method.getName());
                }
            }

            Elements elements = doc.getAllElements();
            for (Element element : elements) {

            }
            
            PrintWriter out = resp.getWriter();
            out.write(html);
            out.flush();
        } catch (ClassNotFoundException | SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException ex) {
            Logger.getLogger(SightlyProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void showFields(Object o) throws ClassNotFoundException {
        //Class<?> clazz = o.getClass();

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doPost(req, resp); //To change body of generated methods, choose Tools | Templates.
    }
}
