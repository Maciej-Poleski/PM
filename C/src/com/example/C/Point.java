package com.example.C;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * User: Maciej Poleski
 * Date: 07.04.13
 * Time: 21:57
 */
public class Point {
    double x, y;

    public static Point readFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        Point result = new Point();

        parser.require(XmlPullParser.START_TAG, null, "point");
        String tag = parser.getName();
        String x = parser.getAttributeValue(null, "x");
        String y = parser.getAttributeValue(null, "y");
        parser.require(XmlPullParser.END_TAG, null, "point");
        result.x = Double.parseDouble(x);
        result.y = Double.parseDouble(y);
        return result;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (Double.compare(point.x, x) != 0) return false;
        if (Double.compare(point.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
