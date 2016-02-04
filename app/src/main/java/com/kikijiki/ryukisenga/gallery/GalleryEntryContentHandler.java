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
import com.kikijiki.ryukisenga.gallery.ArtistPageFragment.GalleryEntry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GalleryEntryContentHandler extends DefaultHandler {
    private GalleryEntry _out;
    private XMLFormat.VectorTag _current_tag;
    private StringBuffer _buf = new StringBuffer();

    public GalleryEntryContentHandler(GalleryEntry out) {
        _out = out;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //super.endElement(uri, localName, qName);

        _current_tag = XMLFormat.VectorTag.valueOf(qName);
        String data = _buf.toString().trim();

        switch (_current_tag) {
            case title:
                _out.data.title = data;
                break;
            case style:
                _out.data.style = data;
                break;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //super.startElement(uri, localName, qName, attributes);
        _current_tag = XMLFormat.VectorTag.valueOf(qName);
        _buf = new StringBuffer();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //super.characters(ch, start, length);
        _buf.append(ch, start, length);
    }
}
