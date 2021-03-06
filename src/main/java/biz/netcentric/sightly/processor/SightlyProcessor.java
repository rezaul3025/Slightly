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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Sightly processor Servlet
 * 
 * @author rkarim
 */
public class SightlyProcessor extends HttpServlet {

    /**
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            Class<?> loadedClass = null;
            Map<String, Object> classPropertise = new HashMap<String, Object>();

            //In real application host, port and context root must be configure in property file
            Document doc = Jsoup.connect("http://localhost:8080/Sightly").get();

            String html = doc.html();

            Elements scriptElement = doc.getElementsByTag("script");
            if (scriptElement.attr("type").equals("server/javascript")) {
                String data = scriptElement.html();
                String fullyQualifiedClassName = data.substring(data.indexOf("Packages") + "Packages".length() + 1, data.indexOf(")"));
                //System.out.println(fullyQualifiedClassName);
                loadedClass = Class.forName(fullyQualifiedClassName);
                //Initialize class using parameterize constructor to get example data, in real application data must come from data sources
                Object object = loadedClass.getConstructor(String.class, String.class, boolean.class, int.class).newInstance("Tom Fox", "Jena Fox", true, 5);
                for (Method method : loadedClass.getDeclaredMethods()) {
                    if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                        Object oblist = method.invoke(object);
                        classPropertise.put(method.getName().toLowerCase(), oblist);
                        //System.out.println(oblist);
                    }

                }
            }

            Elements elements = doc.getAllElements();
            for (Element element : elements) {
                Attributes attributes = element.attributes();

                if (hasAttr("data-if", attributes)) {
                    String tagIf = element.outerHtml();

                    String ifAttr = element.attr("data-if");
                    if (loadedClass != null && loadedClass.getName().toLowerCase().contains(ifAttr.substring(0, ifAttr.indexOf(".")))) {

                        if (classPropertise.containsKey("is" + ifAttr.substring(ifAttr.indexOf(".") + 1))) {

                            if (Boolean.valueOf(classPropertise.get("is" + ifAttr.substring(ifAttr.indexOf(".") + 1)).toString())) {
                                html = mapClassProperty(tagIf, loadedClass.getName(), classPropertise, html);
                            } else {
                                html = html.replace(tagIf, "");
                            }
                        }
                    }

                } else if (hasAttr("data-for", attributes)) {
                    String data = element.html();
                    System.out.println("tag : " + data);
                    String forObj = data.substring(data.indexOf("${") + 2, data.indexOf("}"));

                    String forAttr = element.attr("data-for-" + forObj);

                    StringBuffer sb = new StringBuffer();

                    if (loadedClass != null && loadedClass.getName().toLowerCase().contains(forAttr.split("\\.")[0]) && classPropertise.containsKey("get" + forAttr.split("\\.")[1])) {
                        for (Object ob : (List<Object>) classPropertise.get("get" + forAttr.split("\\.")[1])) {

                            sb.append("<div>").append(data.substring(0, data.indexOf("${"))).append(ob).append("</div>");
                        }

                        html = html.replace(data, sb);

                    }

                } else if (element.children().isEmpty()) {
                    String tag = element.outerHtml();

                    if (tag.contains("${")) {

                        html = mapClassProperty(tag, loadedClass.getName(), classPropertise, html);
                    }
                }
            }

            PrintWriter out = resp.getWriter();
            out.write(html);
            out.flush();
        } catch (ClassNotFoundException  ex) {
            Logger.getLogger(SightlyProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(SightlyProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(SightlyProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(SightlyProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(SightlyProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(SightlyProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SightlyProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String mapClassProperty(String tag, String loadedClass, Map<String, Object> classPropertise, String html) {
        String[] tempTagArr = tag.split("\\$");
        for (String temp : tempTagArr) {
            if (temp.startsWith("{")) {
                // System.out.println("temp : " + temp+" cc : "+temp.substring(1, temp.indexOf(".")));
                String attrName = temp.substring(temp.indexOf(".") + 1, temp.indexOf("}"));

                if (loadedClass.toLowerCase().contains(temp.substring(1, temp.indexOf(".")))) {

                    String replaceableAttr = temp.substring(1, temp.indexOf("}") + 1);

                    String newTag = tag.replace("${" + replaceableAttr, classPropertise.get("get" + attrName).toString());
                    //System.out.println("temp : " + temp + " cc : " + newTag);
                    html = html.replace(tag, newTag);
                }
            }
        }
        return html;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doPost(req, resp); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean hasAttr(String key, Attributes attributes) {
        for (Attribute attr : attributes) {
            if (attr.getKey().equals(key)) {
                return true;
            } else if (attr.getKey().startsWith(key)) {
                return true;
            }
        }

        return false;
    }
}
