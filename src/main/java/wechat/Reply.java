package wechat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcs.FilePath;
import mcs.ReplyServer;

import java.io.*;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Reply {
    static int port = ReplyServer.port;
    static String host = ReplyServer.host;
    public enum ReplyType{reply, question, phone, image, weather, none, direct}
    public enum XmlType{txt, url, img}

    public String ask;
    public ReplyType replyType;
    public String ans;
    public XmlType xmlType;

    private Reply(){}
    public Reply(String ask) {
        this.ask = ask;
        xmlType = XmlType.txt;
        processReply();
    }

    public void processReply() {
        //空字符串
        if(ask == null || ask.matches("\\s+"))
            replyType = ReplyType.none;
        //非查询问句
        else if(ask.matches("[你您].+"))
            replyType = ReplyType.reply;
        //天气
        else if(ask.contains("天气")) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(FilePath.get("Text\\city.csv"))));
                String data = br.readLine();
                Matcher am = Pattern.compile("(\\S+?)的?天气[\\S 　]*").matcher(ask);
                am.find();
                String cityName = am.group(1);
                if (data.contains(cityName)) {
                    replyType = ReplyType.weather;
                    ask = cityName;
                }else {
                    replyType = ReplyType.direct;
                    ans = "格式有误，请试试\"XX（的）天气...\"";
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        //本体，百度百科
        else if(!(ans = getOntology(ask)).equals("")) {
            replyType = ReplyType.direct;
            xmlType = XmlType.url;
        }
        //图片
        else if(ask.matches(".*(啥样|什么样|图片|表情|搜图|美景|景色|摄影).*")) {
            replyType = ReplyType.image;
            xmlType = XmlType.img;
        }
        //百度知道
        else if((ask.matches(".+[吗啥嘛?？]") ||
                ask.matches(".*(如何|怎么|怎样|咋样|什么|哪些|为啥|是否).*") ||
                ask.matches("(假设|假如|如果).*") || ask.matches(".*(怎么办|咋办)"))
                && ask.length() > 5) {
            replyType = ReplyType.question;
        }
        //电话号码
        else if(ask.matches("1\\d{10}")){
            replyType = ReplyType.phone;
        }
        else replyType = ReplyType.reply;

        switch (replyType) {
            case reply: ans = getReply(ask); break;
            case question: ans = getZhidaoAns(ask); break;
            case phone: ans = getPhoneLoc(ask); break;
            case image: ans = getImage(ask); break;
            case weather: ans = getWeather(ask); break;
            case direct: break;
            case none: ans = getNone(); break;
        }
    }

    String getReply(String ask) {
        String reply = "服务器出问题了（>﹏<）我无法理解...";
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            out.writeUTF(ask);

            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            reply = in.readUTF();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return reply;
    }

    String getWeather(String ask) {
        String res = "暂时无法查询到天气信息或城市信息有误╮(๑•́ ₃•̀๑)╭";
        try {
            String url = "http://v.juhe.cn/weather/index?format=2&cityname=" + URLEncoder.encode(ask,"utf-8") + "&key=adf9483506f8af73fbc3eac624fcbb11";
            String json = Util.getHTML(url);
//            String json = Util.readTxt(FilePath.get("Text\\weather.json"));
            JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
            if (jo.get("resultcode").getAsString().equals("200")) {
                res = "";
                JsonObject result = jo.get("result").getAsJsonObject();
                JsonObject sk = result.get("sk").getAsJsonObject();
                res += String.format("实时天气(%s)：", sk.get("time").getAsString());
                res += "温度" + sk.get("temp").getAsString() + "℃，";
                res += sk.get("wind_direction").getAsString();
                res += sk.get("wind_strength").getAsString() + "，";
                res += "湿度" + sk.get("humidity").getAsString() + "。\n";

                JsonObject today = result.get("today").getAsJsonObject();
                res += String.format("今日天气(%s)：", today.get("date_y").getAsString());
                res += today.get("temperature").getAsString() + "，";
                res += today.get("weather").getAsString() + "，";
                res += today.get("wind").getAsString() + "。\n";
                res += "评价：天气" + today.get("dressing_index").getAsString() + "，";
                res += "洗车" + today.get("wash_index").getAsString() + "，";
                res += "旅游" + today.get("travel_index").getAsString() + "，";
                res += "晨练" + today.get("exercise_index").getAsString() + "，";
                res += today.get("dressing_advice").getAsString() + "\n";

                res += "未来天气：\n";
                JsonArray future = result.getAsJsonArray("future");
                for (JsonElement e : future) {
                    JsonObject day = e.getAsJsonObject();
                    String date = day.get("date").getAsString();
                    res += String.format("%s月%s日", date.substring(4, 6), date.substring(6, 8));
                    res += day.get("week").getAsString() + "：";
                    res += day.get("temperature").getAsString() + "，";
                    res += day.get("weather").getAsString() + "。\n";
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    String getZhidaoAns(String ask) {
        try {
            String html = Util.getHTML("https://zhidao.baidu.com/search?word=" + URLEncoder.encode(ask, "utf-8"));
            Matcher dlm = Pattern.compile("<dl([\\s\\S]+?)</dl>").matcher(html);
            while (dlm.find()) {
                Matcher m = Pattern.compile("<a href=\"(http://zhidao.baidu.com/.+?)\"").matcher(dlm.group(1));
                if (m.find()) {
                    String res = getAnsInZhidao(m.group(1));
                    if(res != null) return res;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return getReply(ask);
    }

    private String getAnsInZhidao(String url) {
        String ansHTML = Util.getHTML(url);
        String ans = null;
        if(ansHTML != null) {
            Matcher am = Pattern.compile("<pre id=\"best-content.+?>([\\s\\S]+?)</pre>").matcher(ansHTML);
            if (am.find()) {
                ans = am.group(1);
                ans = ans.replaceAll("<br\\s+?/>", "\n");
                ans = ans.replaceAll("<img class=\"word-replace\" src=\"(.+?)\">","");
            }
        }
        return ans;
    }

    String getPhoneLoc(String phoneNo) {
        String res = "暂时无法查询到该手机号码~";
        String json = Util.getHTML("https://www.iteblog.com/api/mobile.php?mobile=" + phoneNo);
        if (json != null) {
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
            String province = jsonObject.get("province").getAsString();
            String city = jsonObject.get("city").getAsString();
            if (city.equals(province)) city = "";
            String operator = jsonObject.get("operator").getAsString();
            res = "该号码归属地为" + province + city + "，运营商为" + operator +"。";
        }
        return res;
    }

    String getOntology(String word) {
        String res = "";
        try {
            res = Util.getHTML("http://knowledgeworks.cn:30001/?p=" + URLEncoder.encode(word,"utf-8"));
            String query = res.substring(1, res.length() - 1).split(",")[0].replace("\"","").trim();
            String html = Util.getHTML("http://baike.baidu.com/search/none?word=" + URLEncoder.encode(query,"utf-8"));
            Matcher m = Pattern.compile("class=\"result-title\" href=\"(\\S+)\"").matcher(html);
            if (m.find()) res = m.group(1);
            else return "";
            if (res.startsWith("/")) res = "http://baike.baidu.com" + res;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    String getImage(String ask) {
        String picUrl = "http://img.qqzhi.com/upload/img_0_2062652211D556319529_23.jpg";
        try {
            String url = "https://image.baidu.com/search/index?tn=resultjson&ipn=r&ct=201326592&cl=2&lm=-1&st=-1&fm=result&fr=&sf=1&fmq=1497088452491_R&pv=&ic=0&nc=1&z=&se=1&showtab=0&fb=0&width=&height=&face=0&istype=2&ie=utf-8&word=";
            String json = Util.getHTML(url + URLEncoder.encode(ask,"utf-8"));
            JsonObject job = new JsonParser().parse(json).getAsJsonObject();
            JsonArray jar = job.get("data").getAsJsonArray();
            job = jar.get(0).getAsJsonObject();
            picUrl = job.get("middleURL").getAsString();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return Util.getImgID(picUrl);
    }

    String getNone() {
        return "我无法理解或服务器繁忙(╥╯^╰╥)";
    }
}
