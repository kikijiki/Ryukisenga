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

package com.kikijiki.ryukisenga.gallery;

import com.kikijiki.ryukisenga.content.XMLFormat;
import com.kikijiki.ryukisenga.gallery.ArtistPageFragment.ArtistEntry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ArtistPageContentHandler extends DefaultHandler {
    private ArtistEntry _a;

    private XMLFormat.ArtistTag _current_tag;
    private StringBuffer _buf = new StringBuffer();

    public ArtistPageContentHandler(ArtistEntry info) {
        _a = info;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        _current_tag = XMLFormat.ArtistTag.valueOf(qName);

        String data = _buf.toString().trim();

        switch (_current_tag) {
            case name:
                _a.name = data;
                break;
            case info:
                _a.info = data;
                break;
            case email:
                _a.mail = data;
                break;
            case website:
                _a.link = data;
                break;
            case twitter:
                _a.twitter = data;
                break;
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        _buf = new StringBuffer();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        _buf.append(ch, start, length);
    }
}
