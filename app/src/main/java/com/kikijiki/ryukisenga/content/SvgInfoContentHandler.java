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

package com.kikijiki.ryukisenga.content;

import com.kikijiki.ryukisenga.content.SVGUti.SvgTag;
import com.kikijiki.ryukisenga.drawing.vec;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SvgInfoContentHandler extends DefaultHandler {
    private vec _size;
    private vec _offset;

    public SvgInfoContentHandler(vec size, vec offset) {
        _size = size;
        _offset = offset;
        _offset.x = -1;
        _offset.y = -1;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase(SvgTag.svg.toString())) {
            vec size = SVGUti.getSize(attributes);
            float[] viewbox = SVGUti.getViewBox(attributes);

            if (viewbox == null) {
                _size.x = size.x;
                _size.y = size.y;
            } else {
                _size.x = viewbox[2] - viewbox[0];
                _size.y = viewbox[3] - viewbox[1];

                _offset.x = viewbox[0];
                _offset.y = viewbox[1];
            }

            throw new SAXException("Info");
        }
    }
}