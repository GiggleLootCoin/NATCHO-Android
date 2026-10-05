package ai.natcho.app;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import java.net.URLEncoder;
import java.util.*;

public class MainActivity extends Activity {
    private NatchoBrain brain; private LinearLayout chat; private EditText input; private TextView mode,status; private TextToSpeech tts; private SpeechRecognizer speech; private boolean live=false;
    private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private TextView tv(String s,float size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(dp(10),dp(7),dp(10),dp(7));return t;}
    private Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setAllCaps(false);b.setTextColor(Color.WHITE);return b;}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);brain=new NatchoBrain(this);getWindow().setStatusBarColor(Color.rgb(9,10,16));getWindow().setNavigationBarColor(Color.rgb(9,10,16));buildUi();
        tts=new TextToSpeech(this,r->{if(r==TextToSpeech.SUCCESS){int x=tts.setLanguage(Locale.US);status.setText(x==TextToSpeech.LANG_MISSING_DATA||x==TextToSpeech.LANG_NOT_SUPPORTED?"VOICE SETUP NEEDED":"READY • local-first");}});
        if(Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},50);
    }
    private void buildUi(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(8),dp(12),dp(8));root.setBackgroundColor(Color.rgb(9,10,16));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView avatar=new ImageView(this);avatar.setImageResource(R.drawable.natcho_avatar);header.addView(avatar,new LinearLayout.LayoutParams(dp(62),dp(62)));
        LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);TextView title=tv("NATCHO",24);title.setTypeface(null,1);TextView sub=tv("Natcho Average AI  •  Trained by LittleRedBigSmile",11);sub.setTextColor(Color.LTGRAY);names.addView(title);names.addView(sub);header.addView(names,new LinearLayout.LayoutParams(0,-2,1));
        mode=tv(brain.getMode(),13);mode.setGravity(Gravity.CENTER);mode.setBackgroundColor(Color.rgb(35,31,55));mode.setOnClickListener(v->cycleMode());header.addView(mode,new LinearLayout.LayoutParams(dp(90),dp(48)));root.addView(header);
        status=tv("READY • local-first",11);status.setTextColor(Color.LTGRAY);root.addView(status);
        ScrollView scroll=new ScrollView(this);chat=new LinearLayout(this);chat.setOrientation(LinearLayout.VERTICAL);scroll.addView(chat);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addBubble("NATCHO","Ready. Connect PocketPal locally or an online OpenAI-compatible provider. WEB, IMAGE, LIVE voice, and camera are available.");
        LinearLayout tools=new LinearLayout(this);tools.setGravity(Gravity.CENTER);Button web=btn("WEB"),image=btn("IMAGE"),liveBtn=btn("LIVE"),camera=btn("CAM"),setup=btn("SETUP");tools.addView(web);tools.addView(image);tools.addView(liveBtn);tools.addView(camera);tools.addView(setup);root.addView(tools);
        LinearLayout composer=new LinearLayout(this);input=new EditText(this);input.setHint("Ask NATCHO…");input.setHintTextColor(Color.GRAY);input.setTextColor(Color.WHITE);input.setSingleLine(false);Button mic=btn("MIC"),send=btn("SEND");composer.addView(input,new LinearLayout.LayoutParams(0,-2,1));composer.addView(mic);composer.addView(send);root.addView(composer);setContentView(root);
        send.setOnClickListener(v->ask());mic.setOnClickListener(v->listenOnce());liveBtn.setOnClickListener(v->toggleLive(liveBtn));web.setOnClickListener(v->doWeb());image.setOnClickListener(v->doImage());setup.setOnClickListener(v->settings());
        camera.setOnClickListener(v->{Intent i=new Intent("android.media.action.IMAGE_CAPTURE");if(i.resolveActivity(getPackageManager())!=null)startActivityForResult(i,77);else addBubble("NATCHO","No camera app is available.");});
        ObjectAnimator pulse=ObjectAnimator.ofFloat(avatar,"alpha",1f,.55f,1f);pulse.setDuration(1700);pulse.setRepeatCount(ValueAnimator.INFINITE);pulse.start();
    }
    private void addBubble(String who,String text){TextView b=tv(who+"\\n"+text,15);b.setBackgroundColor(who.equals("NATCHO")?Color.rgb(24,25,38):Color.rgb(39,28,55));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(5),0,dp(5));chat.addView(b,lp);chat.post(()->((ScrollView)chat.getParent()).fullScroll(View.FOCUS_DOWN));}
    private void ask(){String p=input.getText().toString().trim();if(p.isEmpty())return;input.setText("");addBubble("YOU",p);status.setText("THINKING • "+brain.getMode());new Thread(()->{String r=brain.ask(p);runOnUiThread(()->{addBubble("NATCHO",r);status.setText("READY • "+brain.getMode());speak(r);if(live)new Handler().postDelayed(()->listenOnce(),500);});}).start();}
    private void speak(String s){if(tts==null)return;int r=tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"natcho");if(r!=TextToSpeech.SUCCESS)status.setText("VOICE • Android TTS error "+r);}
    private void listenOnce(){
        if(!SpeechRecognizer.isRecognitionAvailable(this)){addBubble("NATCHO","Android speech recognition is unavailable on this phone. Typed chat still works.");if(live){live=false;status.setText("VOICE INPUT UNAVAILABLE");}return;}
        if(speech!=null)speech.destroy();speech=SpeechRecognizer.createSpeechRecognizer(this);speech.setRecognitionListener(new RecognitionListener(){
            public void onReadyForSpeech(Bundle b){status.setText("LISTENING");}public void onBeginningOfSpeech(){status.setText("HEARING YOU");}public void onRmsChanged(float v){}public void onBufferReceived(byte[] b){}public void onEndOfSpeech(){status.setText("PROCESSING VOICE");}
            public void onError(int e){status.setText("VOICE INPUT ERROR • "+e);if(live)new Handler().postDelayed(()->listenOnce(),900);}
            public void onResults(Bundle b){ArrayList<String>x=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(x!=null&&!x.isEmpty()){input.setText(x.get(0));ask();}}
            public void onPartialResults(Bundle b){}public void onEvent(int a,Bundle b){}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);speech.startListening(i);
    }
    private void toggleLive(Button b){live=!live;b.setText(live?"STOP":"LIVE");status.setText(live?"LIVE VOICE • listening":"READY");if(live)listenOnce();else if(speech!=null)speech.cancel();}
    private void cycleMode(){String m=brain.getMode();m="AUTO".equals(m)?"ONLINE":("ONLINE".equals(m)?"OFFLINE":"AUTO");brain.setMode(m);mode.setText(m);status.setText("MODE • "+m);}
    private void doWeb(){final EditText q=new EditText(this);q.setHint("What should NATCHO search for?");new AlertDialog.Builder(this).setTitle("WEB SEARCH").setView(q).setPositiveButton("SEARCH",(d,w)->{String s=q.getText().toString().trim();if(s.isEmpty())return;status.setText("SEARCHING WEB");new Thread(()->{String r=brain.webSearch(s);runOnUiThread(()->{addBubble("WEB",r);status.setText("READY");});}).start();}).setNegativeButton("CANCEL",null).show();}
    private void doImage(){final EditText q=new EditText(this);q.setHint("Describe the image to create");new AlertDialog.Builder(this).setTitle("IMAGE GENERATION").setView(q).setPositiveButton("CREATE",(d,w)->{try{String p=URLEncoder.encode(q.getText().toString().trim(),"UTF-8");startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://image.pollinations.ai/prompt/"+p)));}catch(Exception ignored){}}).setNegativeButton("CANCEL",null).show();}
    private void settings(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);EditText lu=new EditText(this);lu.setText(brain.getLocalUrl());lu.setHint("Local endpoint");EditText lm=new EditText(this);lm.setText(brain.getLocalModel());lm.setHint("Local model");EditText ou=new EditText(this);ou.setText(brain.getOnlineUrl());ou.setHint("Online endpoint");EditText om=new EditText(this);om.setText(brain.getOnlineModel());om.setHint("Online model");EditText key=new EditText(this);key.setText(brain.getOnlineKey());key.setHint("Online API key (optional)");key.setInputType(129);box.addView(lu);box.addView(lm);box.addView(ou);box.addView(om);box.addView(key);
        new AlertDialog.Builder(this).setTitle("NATCHO SETUP").setMessage("Local default: PocketPal at 127.0.0.1:11434. Online accepts any OpenAI-compatible endpoint. Keys stay on this phone.").setView(box).setPositiveButton("SAVE",(d,w)->brain.saveSettings(lu.getText().toString().trim(),lm.getText().toString().trim(),ou.getText().toString().trim(),om.getText().toString().trim(),key.getText().toString().trim())).setNegativeButton("CANCEL",null).show();
    }
    @Override protected void onDestroy(){live=false;if(speech!=null)speech.destroy();if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
