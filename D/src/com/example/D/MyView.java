package com.example.D;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorEvent;
import android.util.AttributeSet;
import android.view.View;

/**
 * User: Maciej Poleski
 * Date: 09.04.13
 * Time: 17:30
 */
public class MyView extends View {
    private final float[] speed = {0.f, 0.f};
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private long timeOfLastEvent = 0;
    private boolean leftCorner;
    private boolean topCorner;
    private boolean rightCorner;
    private boolean bottomCorner;
    private float radius;
    private float x;
    private float y;
    private int width;
    private int height;
    private MyActivity activity;

    {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
    }

    public MyView(Context context) {
        super(context);
        init();
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        x = w / 2.f;
        y = h / 2.f;
        radius = (w + h) / 20.f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, radius, paint);
    }

    public MyActivity getActivity() {
        return activity;
    }

    public void setActivity(MyActivity activity) {
        this.activity = activity;
    }

    private void normalize() {
        float zeroSpeed = 1;
        if (x - radius < 0) {
            x = radius;
            speed[0] = Math.abs(speed[0] * 0.8f);
        } else if (x + radius > width) {
            x = width - radius;
            speed[0] = -Math.abs(speed[0] * 0.8f);
        }
        if (x - radius < 1 && speed[0] < zeroSpeed) {
            leftCorner = true;
            speed[0] = 0;
        } else if (x + radius > width - 1 && -speed[0] < zeroSpeed) {
            rightCorner = true;
            speed[0] = 0;
        }
        if (y - radius < 0) {
            y = radius;
            speed[1] = Math.abs(speed[1] * 0.8f);
        } else if (y + radius > height) {
            y = height - radius;
            speed[1] = -Math.abs(speed[1] * 0.8f);
        }
        if (y - radius < 1 && speed[1] < zeroSpeed) {
            topCorner = true;
            speed[1] = 0;
        } else if (y + radius > height - 1 && -speed[1] < zeroSpeed) {
            bottomCorner = true;
            speed[1] = 0;
        }
    }

    private void forgetAboutCorners() {
        leftCorner = topCorner = rightCorner = bottomCorner = false;
    }

    public void receiveEvent(SensorEvent sensorEvent) {
        float localSpeed[] = {-sensorEvent.values[0], sensorEvent.values[1]};
        influenceForceByCorners(localSpeed);
        long dt = 0;
        if (timeOfLastEvent != 0) {
            dt = sensorEvent.timestamp - timeOfLastEvent;
            dt /= 10000000;
        }
        timeOfLastEvent = sensorEvent.timestamp;
        localSpeed[0] *= dt / 2;
        localSpeed[1] *= dt / 2;
        speed[0] += localSpeed[0];
        speed[1] += localSpeed[1];
        float oldX = x, oldY = y;
        x += speed[0] * dt;
        y += speed[1] * dt;
        forgetAboutCorners();
        normalize();
        invalidate((int) (Math.min(x, oldX) - radius - 1),
                (int) (Math.min(y, oldY) - radius - 1),
                (int) (Math.max(x, oldX) + radius + 1),
                (int) (Math.max(y, oldY) + radius + 1));
    }

    private void influenceForceByCorners(float[] localSpeed) {
        if (leftCorner) {
            localSpeed[0] = Math.max(0, localSpeed[0]);
        }
        if (topCorner) {
            localSpeed[1] = Math.max(0, localSpeed[1]);
        }
        if (rightCorner) {
            localSpeed[0] = Math.min(0, localSpeed[0]);
        }
        if (bottomCorner) {
            localSpeed[1] = Math.min(0, localSpeed[1]);
        }
    }


}
