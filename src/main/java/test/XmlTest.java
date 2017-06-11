package test;

import org.junit.Test;
import wechat.XmlProcess;

/**
 * Normal XML test.
 * Created by kurtg on 17/6/11.
 */
public class XmlTest {
    String TextFormat = "<xml>\n" +
            "<ToUserName><![CDATA[Bob]]></ToUserName>\n" +
            "<FromUserName><![CDATA[Alice]]></FromUserName>\n" +
            "<CreateTime>1496410515000</CreateTime>\n" +
            "<MsgType><![CDATA[text]]></MsgType>\n" +
            "<Content><![CDATA[%s]]></Content>\n" +
            "<MsgId>235433234564</MsgId></xml>";

    @Test
    public void testTxt() {
        String xml = String.format(TextFormat, "什么时候去香港旅游最好呀");
        System.out.println(xml + "\n\n");
        String replyXml = XmlProcess.replyXml(xml);
        System.out.println(replyXml);
    }
}
