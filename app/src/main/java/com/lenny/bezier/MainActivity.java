package com.lenny.bezier;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DIYBezierView diyBezierView;
    private DIYBezierView diyBezierView1;
    private DIYBezierView diyBezierView2;
    private ImageView ivShowPic, iv_bg;
    ScheduledExecutorService scheduledExecutorService;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_bg = findViewById(R.id.iv_bg);
        ivShowPic = findViewById(R.id.img_show);

        diyBezierView = findViewById(R.id.circle_bezier_view);

        diyBezierView1 = findViewById(R.id.circle_bezier_view1);
        diyBezierView1.setBezierCircleColor(DIYBezierView.NATIVE_CIRCLE_COLOR);
        diyBezierView2 = findViewById(R.id.circle_bezier_view2);
        diyBezierView2.setBezierCircleColor(DIYBezierView.SEL_POINT_COLOR);


        diyBezierView.setIsShowHelpLine(true);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        setBackground();
        handleRotate();
    }

    public void onReset(View view) {
        diyBezierView.reset();
        diyBezierView1.reset();
        diyBezierView2.reset();
        scheduledExecutorService.shutdownNow();

    }

    public void onPlay(View view) {

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                diyBezierView.post(new Runnable() {
                    @Override
                    public void run() {
                        diyBezierView.play();
                    }
                });
                diyBezierView1.post(new Runnable() {
                    @Override
                    public void run() {
                        diyBezierView1.play();
                    }
                });
                diyBezierView2.post(new Runnable() {
                    @Override
                    public void run() {
                        diyBezierView2.play();
                    }
                });
            }
        }, 0, 80, TimeUnit.MILLISECONDS);

    }

    public void onLog(View view) {
        List<PointF> controlPointList = diyBezierView.getControlPointList();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        for (int i = 0; i < controlPointList.size(); ++i) {
            stringBuilder.append("第")
                    .append(i)
                    .append("个点坐标(单位dp)：[")
                    .append(px2dip(this, controlPointList.get(i).x))
                    .append(", ")
                    .append(px2dip(this, controlPointList.get(i).y))
                    .append("]")
                    .append("\n");
        }

        Log.i("DIY Bezier", "控制点日志: " + stringBuilder.toString());

    }

    public int px2dip(Context context, float pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    public void handleRotate() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivShowPic, "rotation", 0f, 360f);
        objectAnimator.setDuration(20 * 1000);
        objectAnimator.setRepeatMode(ValueAnimator.RESTART);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatCount(-1);
        objectAnimator.start();
    }

    private void setBackground() {
        Bitmap bitmap = BlurUtil.doBlur(BitmapFactory.decodeResource(getResources(), R.drawable.ic_show), 10, 30);
        iv_bg.setImageBitmap(bitmap);
        iv_bg.setDrawingCacheEnabled(true);
//        getBitmap();
        int color=ImageUtil.getColor(bitmap, 1).getRgb();
        diyBezierView.setBezierCircleColor(color);
        int color3=ImageUtil.getColor(bitmap, 3).getRgb();

        diyBezierView1.setBezierCircleColor(color3);
        int color5=ImageUtil.getColor(bitmap, 0).getRgb();

        diyBezierView2.setBezierCircleColor(color5);


        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(0.7f, 0.7f, 0.7f, 1);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        iv_bg.setColorFilter(colorFilter);

    }
}
