package com.example.C;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

/**
 * User: Maciej Poleski
 * Date: 09.04.13
 * Time: 17:30
 */
public class MyView extends View {
    private Board board = new Board();
    private Path currentPath = null;
    private int width;
    private int height;
    private MyActivity activity;

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

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        try {
            this.board = board.clone();
        } catch (CloneNotSupportedException e) {
            this.board = board;
        }
    }

    public void addMyPaths(List<Path> myPaths) {
        if (myPaths == null)
            return;
        for (Path path : myPaths) {
            path.color = Color.RED;
            board.paths.add(path);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private double normalizePoint(double point) {
        if (point < 0)
            return 0;
        if (point > 1)
            return 1;
        return point;
    }

    private Point getPointFromEvent(MotionEvent event) {
        Point result = new Point(event.getX() / width, event.getY() / height);
        //if (result.x < 0 || result.y < 0 || result.x > 1 || result.y > 1)
        //    return null;
        result.x = normalizePoint(result.x);
        result.y = normalizePoint(result.y);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point point = getPointFromEvent(event);
        if (point != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    currentPath = new Path();
                    currentPath.points.add(point);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    currentPath.points.add(point);
                    invalidateCurrent();
                    return true;
                case MotionEvent.ACTION_UP:
                    currentPath.points.add(point);
                    invalidateCurrent();
                    currentPath.color = Color.RED;
                    board.paths.add(currentPath);
                    activity.addNewPath(currentPath);
                    currentPath = null;
                    return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP && currentPath != null && currentPath.points.size() >= 1) {
            invalidateCurrent();
            currentPath.color = Color.RED;
            board.paths.add(currentPath);
            activity.addNewPath(currentPath);
            currentPath = null;
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void invalidateCurrent() {
        int numOfPoints = currentPath.points.size();
        double x1 = currentPath.points.get(numOfPoints - 2).x * width;
        double y1 = currentPath.points.get(numOfPoints - 2).y * height;
        double x2 = currentPath.points.get(numOfPoints - 1).x * width;
        double y2 = currentPath.points.get(numOfPoints - 1).y * height;
        if (x1 > x2) {
            double t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            double t = y1;
            y1 = y2;
            y2 = t;
        }
        invalidate((int) Math.floor(x1)-1, (int) Math.floor(y1)-1, (int) Math.ceil(x2)+1, (int) Math.ceil(y2)+1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (board == null)
            return;
        for (Path path : board.paths) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(path.color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            android.graphics.Path graphicsPath = new android.graphics.Path();
            graphicsPath.moveTo((float) path.points.get(0).x * width, (float) path.points.get(0).y * height);
            for (int i = 1; i < path.points.size(); ++i) {
                graphicsPath.lineTo((float) path.points.get(i).x * width, (float) path.points.get(i).y * height);
            }
            canvas.drawPath(graphicsPath, paint);
        }
        if (currentPath != null) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            android.graphics.Path graphicsPath = new android.graphics.Path();
            graphicsPath.moveTo((float) currentPath.points.get(0).x * width, (float) currentPath.points.get(0).y * height);
            for (int i = 1; i < currentPath.points.size(); ++i) {
                graphicsPath.lineTo((float) currentPath.points.get(i).x * width, (float) currentPath.points.get(i).y * height);
            }
            canvas.drawPath(graphicsPath, paint);
        }
    }

    public MyActivity getActivity() {
        return activity;
    }

    public void setActivity(MyActivity activity) {
        this.activity = activity;
    }
}
