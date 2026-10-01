package ruletka.app;

import android.animation.*;
import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    WheelView wheel; Button spin; TextView result; ImageView giftImage;
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
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,18,24,18); root.setBackgroundColor(0xFF17152A);
        TextView title = text("РУЛЕТКА ПОДАРКОВ", 24, Color.WHITE); title.setGravity(Gravity.CENTER); root.addView(title, new LinearLayout.LayoutParams(-1,60));
        TextView hint = text("Крути и узнай, что тебе досталось", 15, 0xFFB9B4D0); hint.setGravity(Gravity.CENTER); root.addView(hint, new LinearLayout.LayoutParams(-1,36));
        wheel = new WheelView(); root.addView(wheel, new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout card = new LinearLayout(this); card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(18,8,18,8); card.setBackground(round(0xFF292642,22));
        giftImage = new ImageView(this); giftImage.setImageDrawable(round(0xFF3A345A,60)); card.addView(giftImage,new LinearLayout.LayoutParams(72,72));
        result = text("Подарок появится здесь",17,Color.WHITE); result.setPadding(18,0,0,0); card.addView(result,new LinearLayout.LayoutParams(0,80,1)); root.addView(card,new LinearLayout.LayoutParams(-1,90));
        spin = new Button(this); spin.setText("КРУТИТЬ РУЛЕТКУ"); spin.setTextSize(16); spin.setTextColor(Color.WHITE); spin.setAllCaps(false); spin.setBackground(round(0xFF7C4DFF,28)); spin.setOnClickListener(v -> spin()); root.addView(spin,new LinearLayout.LayoutParams(-1,62));
        setContentView(root);
    }
    void spin() { spin.setEnabled(false); Random r=new Random(); int winner=r.nextInt(gifts.length); float target=1440 + (winner*60f) + r.nextInt(50); ObjectAnimator a=ObjectAnimator.ofFloat(wheel,"rotation",0,target); a.setDuration(2800); a.setInterpolator(new DecelerateInterpolator(2)); a.addListener(new AnimatorListenerAdapter(){public void onAnimationEnd(Animator x){ Gift g=gifts[winner]; result.setText("Твой подарок: "+g.name); giftImage.setImageDrawable(round(g.color,60)); spin.setEnabled(true); }}); a.start(); }
    TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    GradientDrawable round(int c,float r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(r);return d;}
    class WheelView extends View { Paint p=new Paint(1); RectF rect=new RectF(); WheelView(){super(MainActivity.this);p.setTypeface(Typeface.DEFAULT_BOLD);}
      protected void onDraw(Canvas c){super.onDraw(c);float cx=getWidth()/2f,cy=getHeight()/2f,rad=Math.min(cx,cy)-18;rect.set(cx-rad,cy-rad,cx+rad,cy+rad);float sweep=360f/gifts.length;for(int i=0;i<gifts.length;i++){p.setColor(gifts[i].color);c.drawArc(rect,i*sweep-90,sweep,true,p);p.setColor(0x55FFFFFF);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);c.drawArc(rect,i*sweep-90,sweep,true,p);p.setStyle(Paint.Style.FILL);c.save();c.rotate(i*sweep+sweep/2-90,cx,cy);p.setColor(Color.WHITE);p.setTextSize(Math.max(26,rad/6));p.setTextAlign(Paint.Align.CENTER);c.drawText(gifts[i].icon,cx,cy-rad*.52f,p);p.setTextSize(12);c.drawText(gifts[i].name,cx,cy-rad*.25f,p);c.restore();}p.setColor(Color.WHITE);Path tri=new Path();tri.moveTo(cx-18,cy-rad-10);tri.lineTo(cx+18,cy-rad-10);tri.lineTo(cx,cy-rad+22);tri.close();c.drawPath(tri,p);}
    }
    static class Gift {String name,icon;int color;Gift(String n,String i,int c){name=n;icon=i;color=c;}}
}
