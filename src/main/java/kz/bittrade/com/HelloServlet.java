package kz.bittrade.com;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HelloServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        // Allocate a output writer to write the response message into the network socket
        PrintWriter out = response.getWriter();

        // Write the response message, in an HTML page
        try {
            out.println("KAI60O0iyjRXQrDJKRWzs8myGnKiKCjbkqLoX_q9vHg.aW8QiGRa2xtcZV-HV5SFr49QeU-Kq7oS1AZgkisLIW0");
        } finally {
            out.close();  // Always close the output writer
        }
    }
}
