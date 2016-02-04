/*
 * Copyright 2014 Matteo Bernacchia <kikijikispaccaspecchi@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kikijiki.ryukisenga.styles.data;

import android.util.Xml;

import com.kikijiki.ryukisenga.content.Assets;
import com.kikijiki.ryukisenga.content.XMLFormat;
import com.kikijiki.ryukisenga.content.XMLFormat.StyleFillStyle;
import com.kikijiki.ryukisenga.content.XMLFormat.StyleTag;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;

public class StyleSerializer {
    private static final String ENCODING = "utf8";

    public static void save(StyleData style) {
        StyleSerializer ss = new StyleSerializer();
        ss.saveStyle(style);
    }

    private void saveStyle(StyleData style) {
        OutputStream os = Assets.sdWriteStyle(style.info.file);
        XmlSerializer s = Xml.newSerializer();
        s.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        try {
            s.setOutput(os, ENCODING);
            s.startDocument(ENCODING, true);

            write(s, style);

            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void write(XmlSerializer s, StyleData style) throws IllegalArgumentException, IllegalStateException, IOException {
        s.startTag(null, XMLFormat.StyleTag.style.toString());
        s.attribute(null, XMLFormat.STYLE_NAME_ATTRIBUTE, style.info.name);

        writePaint(s, style.line, StyleTag.line);
        writePaint(s, style.trail, StyleTag.trail);
        writePaint(s, style.background, StyleTag.background);

        s.endTag(null, XMLFormat.StyleTag.style.toString());
        s.endDocument();
    }

    @SuppressWarnings("incomplete-switch")
    private void writePaint(XmlSerializer s, PaintData p, StyleTag tag) throws IllegalArgumentException, IllegalStateException, IOException {
        String tag_name = tag.toString();

        s.startTag(null, tag_name);

        if (tag != StyleTag.background && p.style != StyleFillStyle.asLine) {
            XMLFormat.writeStrokeWidthAttribute(s, p.width);
        }

        XMLFormat.writeFillStyleAttribute(s, p.style);

        switch (p.style) {
            case asLine:
                break;
            case solid:
                XMLFormat.writeFillColorAttribute(s, p.color);
                break;
            case gradient:
                XMLFormat.writeFillGradientAttributes(s, p.gradient.angle);

                String stop_tag = XMLFormat.StyleTag.stop.toString();

                for (GradientStop stop : p.gradient.stop) {
                    s.startTag(null, stop_tag);
                    if (p.gradient.uniform) {
                        XMLFormat.writeFillUniformGradientStopAttributes(s, stop.color);
                    } else {
                        XMLFormat.writeFillGradientStopAttributes(s, stop.offset, stop.color);
                    }
                    s.endTag(null, stop_tag);
                }
                break;
            case tile:
                XMLFormat.writeFillTile(s, p.tile);
                if (p.scale != null)
                    XMLFormat.writeFillScale(s, p.scale);
                break;
        }

        s.endTag(null, tag_name);
    }
}
