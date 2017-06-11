package wechat;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Wechat Servlet.
 * Created by kurtg on 17/1/22.
 */

@WebServlet(urlPatterns = {"/wechat"})
public class WeChatServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");

        // 读取接收到的xml消息
        StringBuffer sb = new StringBuffer();
        InputStream is = request.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, Util.CHARSET));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String xml = sb.toString();

        String result = "";
        String nonce = request.getParameter("nonce");
        String msg_signature = request.getParameter("msg_signature");
        String timestamp = request.getParameter("timestamp");
        if(nonce != null && nonce.length() > 0) {
            result = XmlProcess.replyEncryptXml(xml, nonce, msg_signature, timestamp);
        }
        else result = XmlProcess.replyXml(xml);

        try {
            OutputStream os = response.getOutputStream();
            os.write(result.getBytes(Util.CHARSET));
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");

        String result = "WeChat response server hearing...";

        String echostr = request.getParameter("echostr");
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");
        String signature = request.getParameter("signature");
        if (echostr != null && echostr.length() > 1) {
            String[] attrs = new String[] {Util.Token, timestamp, nonce};
            System.out.println(attrs.toString());
            Arrays.sort(attrs);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 3; i++) {
                sb.append(attrs[i]);
            }
            String sha1 = DigestUtils.sha1Hex(sb.toString());
            if(sha1.equals(signature))
                result = echostr;
        }

        try {
            OutputStream os = response.getOutputStream();
            os.write(result.getBytes(Util.CHARSET));
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}