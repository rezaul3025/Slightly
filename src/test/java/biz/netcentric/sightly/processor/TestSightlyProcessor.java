/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biz.netcentric.sightly.processor;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *Test for SightlyProcessor Servlet
 * 
 * @author rkarim
 */
public class TestSightlyProcessor extends Mockito {
    @Test
    public void testServlet() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);       
        HttpServletResponse response = mock(HttpServletResponse.class);   
       ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bout);
        when(response.getWriter()).thenReturn(writer);
        
        new SightlyProcessor().doGet(request, response);
        
        assertTrue(bout.toString().contains("Spouse:Jena Fox"));
    }
}
