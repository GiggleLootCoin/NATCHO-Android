package ai.natcho.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public final class NatchoBrain {
    private final SharedPreferences prefs;
    private static final String PREFS="natcho";
    private static final String DEFAULT_LOCAL="http://127.0.0.1:11434/v1/chat/completions";
    public NatchoBrain(Context c){prefs=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
    public String getMode(){return prefs.getString("mode","AUTO");}
    public void setMode(String m){prefs.edit().putString("mode",m).apply();}
    public String getLocalUrl(){return prefs.getString("local_url",DEFAULT_LOCAL);}
    public String getLocalModel(){return prefs.getString("local_model","local");}
    public String getOnlineUrl(){return prefs.getString("online_url","https://api.openai.com/v1/chat/completions");}
    public String getOnlineModel(){return prefs.getString("online_model","gpt-4o-mini");}
    public String getOnlineKey(){return prefs.getString("online_key","");}
    public void saveSettings(String lu,String lm,String ou,String om,String key){
        prefs.edit().putString("local_url",lu).putString("local_model",lm).putString("online_url",ou).putString("online_model",om).putString("online_key",key).apply();
    }
    public String ask(String prompt){
        String mode=getMode();
        if("OFFLINE".equals(mode)||("AUTO".equals(mode)&&localReachable())){
            String r=callChat(getLocalUrl(),getLocalModel(),"",prompt); if(r!=null)return r;
        }
        if(!"OFFLINE".equals(mode)&&!getOnlineKey().isEmpty()){
            String r=callChat(getOnlineUrl(),getOnlineModel(),getOnlineKey(),prompt); if(r!=null)return r;
        }
        return "NATCHO is ready. "+("OFFLINE".equals(mode)?"Offline mode is active. ":"No connected chat provider is configured. ")+"Use SETUP to connect PocketPal or an OpenAI-compatible provider.";
    }
    private boolean localReachable(){
        try{HttpURLConnection c=(HttpURLConnection)new URL(getLocalUrl()).openConnection();c.setConnectTimeout(700);c.setReadTimeout(700);c.setRequestMethod("HEAD");int x=c.getResponseCode();c.disconnect();return x>0;}catch(Exception e){return false;}
    }
    private String callChat(String endpoint,String model,String key,String prompt){
        try{
            JSONObject body=new JSONObject();body.put("model",model);
            JSONArray msgs=new JSONArray();
            msgs.put(new JSONObject().put("role","system").put("content","You are NATCHO (Natcho Average AI), a practical personal AI trained by LittleRedBigSmile. Be accurate, direct, curious, kind, and transparent about limits. Never claim a tool ran if it did not. Apply Red's Ways Of Thinking: question assumptions, seek useful truth, protect the user's agency, prefer simple working solutions, and learn from corrections."));
            msgs.put(new JSONObject().put("role","user").put("content",prompt));body.put("messages",msgs);body.put("temperature",0.7);
            byte[] data=body.toString().getBytes(StandardCharsets.UTF_8);
            HttpURLConnection c=(HttpURLConnection)new URL(endpoint).openConnection();c.setConnectTimeout(8000);c.setReadTimeout(30000);c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
            if(!key.isEmpty())c.setRequestProperty("Authorization","Bearer "+key);
            try(OutputStream os=c.getOutputStream()){os.write(data);}
            if(c.getResponseCode()<200||c.getResponseCode()>=300){c.disconnect();return null;}
            BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);br.close();c.disconnect();
            JSONArray choices=new JSONObject(sb.toString()).optJSONArray("choices");if(choices==null||choices.length()==0)return null;
            JSONObject msg=choices.getJSONObject(0).optJSONObject("message");return msg==null?null:msg.optString("content",null);
        }catch(Exception e){return null;}
    }
    public String webSearch(String query){
        try{
            String q=URLEncoder.encode(query,"UTF-8");HttpURLConnection c=(HttpURLConnection)new URL("https://html.duckduckgo.com/html/?q="+q).openConnection();c.setConnectTimeout(8000);c.setReadTimeout(12000);c.setRequestProperty("User-Agent","Mozilla/5.0 NATCHO/0.1");
            BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();String line;while((line=br.readLine())!=null)s.append(line).append(' ');br.close();c.disconnect();
            String out=s.toString().replaceAll("<[^>]+>"," ").replaceAll("&quot;","\"").replaceAll("&#x27;","'").replaceAll("&amp;","&").replaceAll("\\\\s+"," ").trim();
            return out.length()>5000?out.substring(0,5000):out;
        }catch(Exception e){return "Web search unavailable right now: "+e.getClass().getSimpleName();}
    }
}
