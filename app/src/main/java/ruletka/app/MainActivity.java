package ruletka.app;

import android.animation.*;
import android.app.*;
import android.content.Intent;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    WheelView wheel; Button spin; TextView result, giftImage; float angle; ValueAnimator animation;
    final int[] weights = {2,5,12,15,10,16,20,20};
    final Gift[] gifts = {
        new Gift("Большой подарок", "🎁", 0xFFFF5C73),
        new Gift("Сертификат", "🎫", 0xFFFFC857),
        new Gift("Кофе", "☕", 0xFF55C1FF),
        new Gift("Скидка 20%", "🏷", 0xFFFF8FA3),
        new Gift("Шоппер", "👜", 0xFF9B7EDE),
        new Gift("Брелок", "🔑", 0xFF61D095),
        new Gift("Конфеты", "🍬", 0xFFFF9F43),
        new Gift("Спасибо за участие!", "⭐", 0xFFB56CFF)
    };
    @Override public void onCreate(Bundle b) { super.onCreate(b); build(); }
    void build() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER_HORIZONTAL); root.setPadding(dp(20),dp(20),dp(20),dp(24)); root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xFF17103D,0xFF572185,0xFF17103D}));
        TextView title = text("РУЛЕТКА", 32, Color.WHITE); title.setTypeface(Typeface.DEFAULT_BOLD); title.setGravity(Gravity.CENTER); title.setShadowLayer(dp(3),0,dp(3),0xFF812CE6); root.addView(title);
        TextView subtitle=text("ПОДАРКОВ",34,0xFFFFD45D); subtitle.setTypeface(Typeface.DEFAULT_BOLD); subtitle.setGravity(Gravity.CENTER); subtitle.setShadowLayer(dp(3),0,dp(3),0xFFBC641C); root.addView(subtitle);
        TextView hint = text("✦ Крути и забирай свой подарок! ✦", 14, 0xFFDED1FF); hint.setGravity(Gravity.CENTER); root.addView(hint, new LinearLayout.LayoutParams(-1,dp(40)));
        wheel = new WheelView(); root.addView(wheel, new LinearLayout.LayoutParams(-1,-2));
        spin = new Button(this); spin.setText("КРУТИТЬ РУЛЕТКУ"); spin.setTextSize(19); spin.setTypeface(Typeface.DEFAULT_BOLD); spin.setTextColor(Color.WHITE); spin.setAllCaps(false); spin.setBackground(panel(new int[]{0xFFBC52FF,0xFF661CEB},32,0xFFDDB4FF)); spin.setElevation(dp(8)); spin.setOnClickListener(v -> spin()); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(68)); bp.setMargins(dp(8),dp(8),dp(8),dp(22)); root.addView(spin,bp);
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER); card.setPadding(dp(16),dp(16),dp(16),dp(18)); card.setBackground(panel(new int[]{0xFFF9F5FF,0xFFDBD1FF},28,0xFFB88AFF));
        TextView heading=text("Твой подарок",24,0xFF32135E); heading.setTypeface(Typeface.DEFAULT_BOLD); card.addView(heading);
        giftImage=text("🎁",64,0xFF32135E); giftImage.setGravity(Gravity.CENTER); card.addView(giftImage,new LinearLayout.LayoutParams(-1,dp(100)));
        result = text("Нажми на кнопку, чтобы узнать",16,0xFF594179); result.setGravity(Gravity.CENTER); card.addView(result); root.addView(card,new LinearLayout.LayoutParams(-1,-2));
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.addView(root); setContentView(scroll);
    }
    void spin() {
        int ticket=new Random().nextInt(100), choice=0;
        for(int i=0;i<weights.length;i++){ticket-=weights[i];if(ticket<0){choice=i;break;}}
        final int winner=choice;
        final float desired=(360-(winner+.5f)*45)%360;
        float normalized=((angle%360)+360)%360;
        spin.setEnabled(false); spin.setAlpha(.7f); result.setText("Рулетка вращается…");
        animation=ValueAnimator.ofFloat(angle,angle+5400+(desired-normalized+360)%360);
        animation.setDuration(15000); animation.setInterpolator(new DecelerateInterpolator(1.5f));
        animation.addUpdateListener(a->{angle=(float)a.getAnimatedValue();wheel.invalidate();});
        animation.addListener(new AnimatorListenerAdapter(){public void onAnimationEnd(Animator a){
            angle=desired; wheel.invalidate();
            giftImage.setText(gifts[winner].icon);result.setText(gifts[winner].name);spin.setEnabled(true);spin.setAlpha(1);
            startActivity(new Intent(MainActivity.this, WinnerActivity.class)
                .putExtra("gift_name",gifts[winner].name).putExtra("gift_icon",gifts[winner].icon));
        }}); animation.start();
    }
    @Override protected void onPause(){if(animation!=null&&animation.isRunning())animation.pause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(animation!=null&&animation.isPaused())animation.resume();}
    @Override protected void onDestroy(){if(animation!=null){animation.removeAllListeners();animation.cancel();}super.onDestroy();}
    int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
    GradientDrawable panel(int[] colors,int radius,int stroke){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);d.setCornerRadius(dp(radius));d.setStroke(dp(3),stroke);return d;}
    TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    GradientDrawable round(int c,float r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(r);return d;}
    class WheelView extends View { Paint p=new Paint(1); RectF rect=new RectF(); WheelView(){super(MainActivity.this);p.setTypeface(Typeface.DEFAULT_BOLD);}
      @Override protected void onMeasure(int w,int h){int size=MeasureSpec.getSize(w);setMeasuredDimension(size,size+dp(16));}
      protected void onDraw(Canvas c){
        float cx=getWidth()/2f,cy=getHeight()/2f+dp(6),outer=getWidth()/2f-dp(12),rad=outer-dp(16);
        p.setColor(0xFFB66320);c.drawCircle(cx,cy+dp(4),outer,p);
        p.setShader(new LinearGradient(0,cy-outer,0,cy+outer,new int[]{0xFFFFED9A,0xFFFFAC31,0xFFFFDB74},null,Shader.TileMode.CLAMP));c.drawCircle(cx,cy,outer,p);p.setShader(null);
        rect.set(cx-rad,cy-rad,cx+rad,cy+rad);c.save();c.rotate(angle,cx,cy);
        for(int i=0;i<gifts.length;i++){
          p.setColor(gifts[i].color);c.drawArc(rect,i*45-90,45,true,p);p.setColor(0x99FFE9A5);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));c.drawArc(rect,i*45-90,45,true,p);p.setStyle(Paint.Style.FILL);
          double theta=Math.toRadians(i*45+22.5-90);float x=cx+(float)Math.cos(theta)*rad*.65f,y=cy+(float)Math.sin(theta)*rad*.65f;
          c.save();c.rotate(-angle,x,y);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(rad*.23f);p.setColor(Color.WHITE);c.drawText(gifts[i].icon,x,y-(p.ascent()+p.descent())/2,p);c.restore();
        }c.restore();
        for(int i=0;i<24;i++){double theta=Math.toRadians(i*15);p.setColor(0xFFFFF6B0);c.drawCircle(cx+(float)Math.cos(theta)*(outer-dp(8)),cy+(float)Math.sin(theta)*(outer-dp(8)),dp(3.5f),p);}
        p.setColor(0xFFB5681D);c.drawCircle(cx,cy+dp(3),dp(28),p);p.setColor(0xFFFFD35D);c.drawCircle(cx,cy,dp(27),p);p.setColor(0xFFFFF0B1);p.setTextSize(dp(30));p.setTextAlign(Paint.Align.CENTER);c.drawText("★",cx,cy+dp(10),p);
        float top=cy-outer-dp(8);Path pointer=new Path();pointer.moveTo(cx-dp(17),top+dp(17));pointer.cubicTo(cx-dp(25),top-dp(18),cx+dp(25),top-dp(18),cx+dp(17),top+dp(17));pointer.lineTo(cx,top+dp(40));pointer.close();p.setColor(0xFFFF466F);c.drawPath(pointer,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(0xFFFFABB9);c.drawPath(pointer,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawCircle(cx,top+dp(5),dp(8),p);
      }
    }
    static class Gift {String name,icon;int color;Gift(String n,String i,int c){name=n;icon=i;color=c;}}
}
