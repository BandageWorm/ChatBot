package wechat;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mcs.FilePath;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


/**
 * XML methods.
 * Created by kurtg on 17/1/22.
 */
public class XmlProcess {

    static XmlEntity getEntity(String strXml) {
        XmlEntity msg = null;
        try {
            if (strXml.length() <= 0)
                return new XmlEntity();

            Document document = DocumentHelper.parseText(strXml);
            Iterator<?> iter = document.getRootElement().elementIterator();

            Class<?> c = Class.forName("wechat.XmlEntity");
            msg = (XmlEntity) c.newInstance();//创建这个实体的对象

            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                String tag = e.getName();
                Field field = c.getDeclaredField(tag);
                Method method = c.getDeclaredMethod("set" + tag, field.getType());
                method.invoke(msg, e.getText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    public static String replyXml(String xml) {
        XmlEntity e = getEntity(xml);
        String sender = e.getFromUserName();
        String recver = e.getToUserName();
        String msgType = e.getMsgType();
        String ask;
        switch (msgType) {
            case "text": ask = e.getContent(); break;
            case "voice": ask = e.getRecognition(); break;
            default: return createTxtXml(sender, recver, "由于个人订阅号权限问题，我只能接受文本或语音~");
        }

        Reply reply = new Reply(ask);
        String ans = reply.ans;

        String log = String.format("%s: %s -> %s: %s", msgType, ask, reply.xmlType, ans.length() < 30 ? ans : ans.substring(0, 30));
        saveLog(sender, log);

        switch (reply.xmlType) {
            case txt: return createTxtXml(sender, recver, Util.shrinkMsg(ans));
            case url: return createUrlXml(sender, recver, ans);
            case img: return createImgXml(sender, recver, ans);
        }

        return createTxtXml(sender, recver, reply.getNone());
    }

    public static String replyEncryptXml(String xml, String nonce,String signature,String timestamp) {
        String res = "";
        try {
            Crypt crypt = new Crypt(Util.Token, Util.EncodingAesKey, Util.AppId);
            String encrypt = crypt.getEncryptXml(xml);
            String decryptXml = crypt.decrypt(encrypt);

            String replyTS = "";
            String replyXml;
            if (!crypt.SHA1(timestamp, nonce, encrypt).equals(signature)) {
                XmlEntity e = XmlProcess.getEntity(decryptXml);
                String sender = e.getFromUserName();
                String recver = e.getToUserName();
                replyXml = XmlProcess.createTxtXml(sender, recver, "数字签名校验错误！");
            }
            else {
                replyXml = replyXml(decryptXml);
            }
            res = crypt.createEncryptXml(replyXml, replyTS, nonce);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String createTxtXml(String to, String from, String content) {
        String sb = "";
        sb += "<xml>\n<ToUserName><![CDATA[" + to + "]]></ToUserName>\n";
        sb += "<FromUserName><![CDATA[" + from + "]]></FromUserName>\n";
        sb += "<CreateTime>" + new Date().getTime() + "</CreateTime>\n";
        sb += "<MsgType><![CDATA[text]]></MsgType>\n";
        sb += "<Content><![CDATA[" + content + "]]></Content></xml>";
        return sb;
    }

    public static String createUrlXml(String to, String from, String url) {
        String res = "";
        String html = Util.getHTML(url);

        String title = "该网页暂时无法预览";
        String description  = "暂无";
        String picUrl = "";
        if(html != null ){
            Matcher tm = Pattern.compile("<title>([\\s\\S]+?)</title>").matcher(html);
            if(tm.find()) {
                title = tm.group(1);
            }
            Matcher dm = Pattern.compile("<meta name=\"description\" content=\"([\\s\\S]+?)\">").matcher(html);
            if(dm.find()) {
                description = dm.group(1);
            }
            Matcher pm = Pattern.compile("<div class=\"summary-pic\"[\\s\\S]+?<img src=\"([\\s\\S]+?)\"").matcher(html);
            if(pm.find()) {
                picUrl = pm.group(1);
            }
        }
        res += "<xml><ToUserName><![CDATA[" + to + "]]></ToUserName>\n";
        res += "<FromUserName><![CDATA[" + from + "]]></FromUserName>\n";
        res += "<CreateTime>" + new Date().getTime() + "</CreateTime>\n";
        res += "<MsgType><![CDATA[news]]></MsgType><ArticleCount>1</ArticleCount>\n";
        res += "<Articles><item>";
        res += "<Title><![CDATA[" + title + "]]></Title>\n";
        res += "<Description><![CDATA[" + description + "]]></Description>\n";
        res += "<PicUrl><![CDATA[" + picUrl + "]]></PicUrl>\n";
        res += "<Url><![CDATA[" + url + "]]></Url>\n";
        res += "</item></Articles></xml>";
        return res;
    }

    public static String createImgXml(String to, String from, String mediaID) {
        String sb = "";
        sb += "<xml><ToUserName><![CDATA[" + to + "]]></ToUserName>\n";
        sb += "<FromUserName><![CDATA[" + from + "]]></FromUserName>\n";
        sb += "<CreateTime>" + new Date().getTime() + "</CreateTime><MsgType><![CDATA[image]]></MsgType>\n";
        sb += "<Image><MediaId><![CDATA[" + mediaID + "]]></MediaId></Image></xml>";
        return sb;
    }

    private static void saveLog(String usr, String content) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(FilePath.get("Text\\LogWechat.txt"), true), Util.CHARSET);
            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            osw.write(dateString + " " + usr + "\t" + content + "\n");
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}