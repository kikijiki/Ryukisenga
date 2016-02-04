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

import android.annotation.SuppressLint;

import com.kikijiki.ryukisenga.content.XMLFormat;
import com.kikijiki.ryukisenga.content.XMLFormat.StyleFillStyle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

@SuppressLint("DefaultLocale")
public class StyleDataContentHandler extends DefaultHandler {
    private StringBuffer _buf;

    private Stack<XMLFormat.StyleTag> _cur = new Stack<XMLFormat.StyleTag>();

    private StyleData _data;

    public StyleDataContentHandler(StyleData data) {
        _data = data;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //super.endElement(uri, localName, qName);

        @SuppressWarnings("unused")
        String data = _buf.toString().trim();

        _cur.pop();
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //super.startElement(uri, localName, qName, attributes);

        _cur.push(XMLFormat.StyleTag.valueOf(qName.toLowerCase()));
        _buf = new StringBuffer();

        switch (_cur.peek()) {
            case style:
                String name = XMLFormat.styleGetName(attributes);

                if (name != null) {
                    _data.info.name = name;
                }
                break;

            case line: {
                XMLFormat.StyleFillStyle fs = XMLFormat.styleGetFillStyle(attributes);

                _data.line.style = fs;
                _data.line.width = XMLFormat.styleGetStrokeWidth(attributes);

                switch (fs) {
                    case solid:
                        _data.line.color = XMLFormat.styleGetFillColor(attributes);
                        break;
                    case gradient:
                        _data.line.gradient.angle = XMLFormat.styleGetGradientAngle(attributes);
                        break;
                }
            }
            break;

            case trail: {
                XMLFormat.StyleFillStyle fs = XMLFormat.styleGetFillStyle(attributes);

                _data.trail.style = fs;
                _data.trail.width = XMLFormat.styleGetStrokeWidth(attributes);

                switch (fs) {
                    case solid:
                        _data.trail.color = XMLFormat.styleGetFillColor(attributes);
                        break;
                    case gradient:
                        _data.trail.gradient.angle = XMLFormat.styleGetGradientAngle(attributes);
                        break;
                    case asLine:
                        break;
                }
            }
            break;

            case background: {
                XMLFormat.StyleFillStyle fs = XMLFormat.styleGetFillStyle(attributes);
                _data.background.style = fs;

                switch (fs) {
                    case solid:
                        _data.background.color = XMLFormat.styleGetFillColor(attributes);
                        break;
                    case gradient:
                        _data.background.gradient.angle = XMLFormat.styleGetGradientAngle(attributes);
                        break;
                    case tile:
                        _data.background.tile = XMLFormat.styleGetFillTile(attributes);
                        _data.background.scale = XMLFormat.styleGetFillScale(attributes);
                        break;
                }
            }
            break;

            case stop: {
                Float offset = XMLFormat.styleGetStopOffset(attributes);
                int color = XMLFormat.styleGetStopColor(attributes);

                switch (_cur.get(_cur.size() - 2)) {
                    case line:
                        _data.line.gradient.addStop(offset, color);
                        break;
                    case trail:
                        _data.trail.gradient.addStop(offset, color);
                        break;
                    case background:
                        _data.background.gradient.addStop(offset, color);
                        break;
                }

            }
            break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //super.characters(ch, start, length);

        _buf.append(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();

        if (_data.trail.style == StyleFillStyle.asLine) {
            _data.trail = _data.line.clone();
            _data.trail.style = StyleFillStyle.asLine;
        }
    }


}
