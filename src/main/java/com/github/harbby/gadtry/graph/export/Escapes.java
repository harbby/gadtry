/*
 * Copyright (C) 2018 The GadTry Authors
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
package com.github.harbby.gadtry.graph.export;

final class Escapes
{
    private Escapes() {}

    /**
     * @return {@code value} as a quoted JSON string literal
     */
    static String jsonString(String value)
    {
        StringBuilder builder = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                default:
                    if (c < 0x20 || c == '\u2028' || c == '\u2029') {
                        builder.append(String.format("\\u%04x", (int) c));
                    }
                    else {
                        builder.append(c);
                    }
            }
        }
        return builder.append('"').toString();
    }

    /**
     * @return {@code value} as a quoted graphviz DOT string
     */
    static String dotString(String value)
    {
        StringBuilder builder = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.append('"').toString();
    }

    /**
     * Escapes text for XML element content and attribute values. Characters that XML 1.0 forbids are dropped.
     */
    static String xml(String value)
    {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&apos;");
                    break;
                case '\n':
                case '\r':
                case '\t':
                    builder.append("&#").append((int) c).append(';');
                    break;
                default:
                    if (c >= 0x20 && c != 0xFFFE && c != 0xFFFF) {
                        builder.append(c);
                    }
            }
        }
        return builder.toString();
    }

    /**
     * Escapes text placed inside a double-quoted mermaid label.
     */
    static String mermaidLabel(String value)
    {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    builder.append("#quot;");
                    break;
                case '\n':
                    builder.append("<br/>");
                    break;
                case '\r':
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }
}
