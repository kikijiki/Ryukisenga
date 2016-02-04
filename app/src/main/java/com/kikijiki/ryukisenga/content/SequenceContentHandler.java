/*
 * Copyright 2014 Matteo Bernacchia <dev@kikijiki.com>
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SequenceContentHandler extends DefaultHandler {
    private String[] _data;

    private StringBuffer _buf;

    private XMLFormat.VectorTag _current_tag;

    public SequenceContentHandler(String[] data) {
        _data = data;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //super.endElement(uri, localName, qName);

        _current_tag = XMLFormat.VectorTag.valueOf(qName.toLowerCase());

//		String data = _buf.toString().trim();
//		
//		switch(_current_tag)
//		{
//		}
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //super.startElement(uri, localName, qName, attributes);

        _current_tag = XMLFormat.VectorTag.valueOf(qName.toLowerCase());
        _buf = new StringBuffer();

        switch (_current_tag) {
            case title:
                //_data[0] = _buf.toString();
                break;
            case style:
                _data[0] = _buf.toString();
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //super.characters(ch, start, length);

        _buf.append(ch, start, length);
    }
}
