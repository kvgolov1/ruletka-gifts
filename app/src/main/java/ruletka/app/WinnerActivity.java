package ruletka.app;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.media.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.Random;

/** Full-screen prize reveal with a bundled offline musical greeting. */
public class WinnerActivity extends Activity {
    private MediaPlayer player;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private Button soundButton;
    private boolean foreground;
    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        if (change < 0) stopGreeting();
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xFF17103D,0xFF752EAC,0xFF17103D}));
        root.addView(new ConfettiView(),new FrameLayout.LayoutParams(-1,-1));
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL); content.setGravity(Gravity.CENTER);
        content.setPadding(dp(24),dp(40),dp(24),dp(40));
        content.addView(label("ПОЗДРАВЛЯЕМ!",30,0xFFFFDC74));
        TextView caption=label("Твой подарок",20,0xFFF0E0FF);
        caption.setPadding(0,dp(14),0,dp(12)); content.addView(caption);
        TextView image=label(getIntent().getStringExtra("gift_icon"),140,Color.WHITE);
        image.setPadding(0,dp(10),0,dp(10)); content.addView(image,new LinearLayout.LayoutParams(-1,dp(220)));
        TextView name=label(getIntent().getStringExtra("gift_name"),28,Color.WHITE);
        name.setPadding(0,dp(12),0,dp(30)); content.addView(name);
        Button close=button("ВЕРНУТЬСЯ К РУЛЕТКЕ"); close.setOnClickListener(v->finish());
        content.addView(close,new LinearLayout.LayoutParams(-1,dp(64)));
        soundButton=button("ВЫКЛЮЧИТЬ ЗВУК"); soundButton.setTextSize(14);
        soundButton.setOnClickListener(v->{if(player!=null)stopGreeting();else playGreeting();});
        LinearLayout.LayoutParams soundParams=new LinearLayout.LayoutParams(-1,dp(52));
        soundParams.topMargin=dp(16); content.addView(soundButton,soundParams);
        scroll.addView(content); root.addView(scroll); setContentView(root);
    }

    @Override protected void onResume() {super.onResume();foreground=true;playGreeting();}
    @Override protected void onPause() {foreground=false;stopGreeting();super.onPause();}
    @Override protected void onDestroy() {stopGreeting();super.onDestroy();}

    private void playGreeting() {
        stopGreeting(); if(!foreground)return;
        AudioAttributes attributes=new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        int focus;
        if(Build.VERSION.SDK_INT>=26){
            focusRequest=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(attributes).setOnAudioFocusChangeListener(focusListener).build();
            focus=audioManager.requestAudioFocus(focusRequest);
        }else focus=audioManager.requestAudioFocus(focusListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        if(focus!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED){stopGreeting();return;}
        try {
            player=MediaPlayer.create(this,R.raw.celebration,attributes,0);
            if(player==null){stopGreeting();return;}
            player.setOnCompletionListener(p->stopGreeting());
            player.setOnErrorListener((p,what,extra)->{stopGreeting();return true;});
            player.start(); soundButton.setText("ВЫКЛЮЧИТЬ ЗВУК");
        }catch(RuntimeException e){stopGreeting();}
    }
    private void stopGreeting() {
        if(player!=null){player.release();player=null;}
        if(audioManager!=null){
            if(Build.VERSION.SDK_INT>=26){if(focusRequest!=null){audioManager.abandonAudioFocusRequest(focusRequest);focusRequest=null;}}
            else audioManager.abandonAudioFocus(focusListener);
        }
        if(soundButton!=null)soundButton.setText("ПРОСЛУШАТЬ ПОЗДРАВЛЕНИЕ");
    }
    private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private TextView label(String text,int size,int color){
        TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);
        v.setGravity(Gravity.CENTER);v.setTypeface(Typeface.DEFAULT_BOLD);return v;
    }
    private Button button(String text){
        Button b=new Button(this);b.setText(text);b.setTextSize(17);b.setTextColor(Color.WHITE);
        GradientDrawable background=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{0xFFBA51FF,0xFF6B21EA});background.setCornerRadius(dp(28));background.setStroke(dp(2),0xFFDAB3FF);
        b.setBackground(background);return b;
    }
    private class ConfettiView extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private ValueAnimator fall;
        private float phase;
        ConfettiView(){super(WinnerActivity.this);}
        @Override protected void onAttachedToWindow(){
            super.onAttachedToWindow();fall=ValueAnimator.ofFloat(0,1);
            fall.addUpdateListener(a->{phase=(float)a.getAnimatedValue();invalidate();});
            fall.setDuration(6000);fall.setInterpolator(new android.view.animation.LinearInterpolator());
            fall.setRepeatCount(ValueAnimator.INFINITE);fall.start();
        }
        @Override protected void onDetachedFromWindow(){if(fall!=null)fall.cancel();super.onDetachedFromWindow();}
        @Override protected void onDraw(Canvas canvas){
            Random random=new Random(37);int[] colors={0xFFFFD85E,0xFFFF70AB,0xFF5DD8FF,0xFFA780FF};
            for(int i=0;i<65;i++){
                float x=random.nextFloat()*getWidth();float y=((random.nextFloat()+phase)%1)*getHeight();
                paint.setColor(colors[i%4]);canvas.save();canvas.rotate(i*23+phase*160,x,y);
                canvas.drawRoundRect(x,y,x+dp(5),y+dp(10),dp(1),dp(1),paint);canvas.restore();
            }
        }
    }
}
