package com.example.C;

import android.graphics.Color;
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
public class Path {
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
}
