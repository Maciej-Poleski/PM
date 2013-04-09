package com.example.C;

import android.content.Context;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Maciej Poleski
 * Date: 07.04.13
 * Time: 21:59
 */
public class Path implements Cloneable {
    long userId;
    int color;
    List<Point> points = new ArrayList<Point>();

    public static Path readFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        Path result = new Path();
        parser.require(XmlPullParser.START_TAG, null, "path");
        String tag = parser.getName();
        String userId = parser.getAttributeValue(null, "userId");
        String color = parser.getAttributeValue(null, "color");
        result.userId = Long.parseLong(userId);
        result.color = Color.parseColor(color);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("point")) {
                result.points.add(Point.readFromXml(parser));
            } else {
                Board.skip(parser);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Path{" +
                "userId=" + userId +
                ", color=" + color +
                ", points=" + points +
                '}';
    }

    @Override
    public Path clone() throws CloneNotSupportedException {
        Path result= (Path) super.clone();
        result.points=new ArrayList<Point>();
        for(Point point : points)
        {
            result.points.add(point.clone());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Path path = (Path) o;

        if (color != path.color) return false;
        if (userId != path.userId) return false;
        if (!points.equals(path.points)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + color;
        result = 31 * result + points.hashCode();
        return result;
    }

    public String getXml(Context context) {
        StringBuilder result = new StringBuilder("<?xml version=\"1.0\"?>\n" +
                "<newpath userId=\"" + ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId() + "\">\n");
        for (Point point : points) {
            result.append(point.getXml());
        }
        result.append("</newpath>\n");
        return result.toString();
    }

    public boolean almostEquals(Path path) {
        if (path == null)
            return false;
        if (points.size() != path.points.size())
            return false;
        for (int i = 0; i < points.size(); ++i)
            if (!points.get(i).almostEquals(path.points.get(i)))
                return false;
        return true;
    }
}
