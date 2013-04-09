package com.example.C;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Maciej Poleski
 * Date: 07.04.13
 * Time: 22:00
 */
public class Board implements Cloneable {
    List<Path> paths = new ArrayList<Path>();

    public static Board readFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        Board result = new Board();

        parser.require(XmlPullParser.START_TAG, null, "board");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("path")) {
                result.paths.add(Path.readFromXml(parser));
            } else {
                skip(parser);
            }
        }
        return result;
    }

    /**
     * To nie moje.
     *
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Board{");
        sb.append("paths=").append(paths);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public Board clone() throws CloneNotSupportedException {
        Board result = (Board) super.clone();
        result.paths = new ArrayList<Path>();
        for (Path path : paths)
            result.paths.add(path.clone());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Board board = (Board) o;

        if (!paths.equals(board.paths)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return paths.hashCode();
    }

}
