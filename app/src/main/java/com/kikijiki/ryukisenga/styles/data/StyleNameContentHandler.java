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

package com.kikijiki.ryukisenga.styles.data;

import android.annotation.SuppressLint;

import com.kikijiki.ryukisenga.content.XMLFormat;
import com.kikijiki.ryukisenga.styles.StyleManager.StyleEntry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressLint("DefaultLocale")
public class StyleNameContentHandler extends DefaultHandler {
    private StyleEntry _e;

    public StyleNameContentHandler(StyleEntry entry) {
        _e = entry;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        XMLFormat.StyleTag tag = XMLFormat.StyleTag.valueOf(qName.toLowerCase());

        switch (tag) {
            case style:
                String name = XMLFormat.styleGetName(attributes);

                if (name != null) {
                    _e.name = name;
                }

                throw new SAXException("Parsing finished");
        }
    }
}
