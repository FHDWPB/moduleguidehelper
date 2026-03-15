package moduleguidehelper.io;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import moduleguidehelper.model.bibtex.*;

public class BibTeXParser {

    public static BibTeXDatabase parse(final Reader reader) throws IOException {
        final List<BibTeXToken> tokens = BibTeXParser.tokenize(new CharacterBuffer(reader));
        final BibTeXDatabase result = new BibTeXDatabase();
        final Iterator<BibTeXToken> iterator = tokens.iterator();
        final List<BibTeXToken> buffer = new LinkedList<BibTeXToken>();
        while (iterator.hasNext()) {
            final BibTeXToken token = iterator.next();
            if (token.type() == BibTeXTokenType.AT) {
                if (!buffer.isEmpty() && buffer.stream().anyMatch(t -> t.type() != BibTeXTokenType.WHITESPACE)) {
                    result.add(new BibTeXFreeComment(BibTeXParser.toString(buffer)));
                }
                buffer.clear();
                result.add(BibTeXParser.parseObject(iterator));
            } else {
                buffer.add(token);
            }
        }
        if (!buffer.isEmpty() && buffer.stream().anyMatch(t -> t.type() != BibTeXTokenType.WHITESPACE)) {
            result.add(new BibTeXFreeComment(BibTeXParser.toString(buffer)));
        }
        return result;
    }

    private static BibTeXToken forwardWhiteSpace(
        final Iterator<BibTeXToken> iterator,
        final String phase
    ) throws IOException {
        if (!iterator.hasNext()) {
            throw new IOException(phase + " ended before completion!");
        }
        final BibTeXToken result = iterator.next();
        if (result.type() == BibTeXTokenType.WHITESPACE) {
            if (!iterator.hasNext()) {
                throw new IOException(phase + " ended before completion!");
            }
            return iterator.next();
        }
        return result;
    }

    private static BibTeXTokenType getType(final char c, final BibTeXTokenType currentType) {
        if (Character.isWhitespace(c)) {
            return BibTeXTokenType.WHITESPACE;
        }
        switch (c) {
        case '@':
            return BibTeXTokenType.AT;
        case ',':
            return BibTeXTokenType.COMMA;
        case '=':
            return BibTeXTokenType.ASSIGN;
        case '"':
            return BibTeXTokenType.QUOTE;
        case '{':
            return BibTeXTokenType.OPEN_BRACE;
        case '}':
            return BibTeXTokenType.CLOSE_BRACE;
        case '(':
            return BibTeXTokenType.OPEN_PARENTHESIS;
        case ')':
            return BibTeXTokenType.CLOSE_PARENTHESIS;
        case '#':
            return BibTeXTokenType.CONCAT;
        default:
            if (Character.isLetterOrDigit(c)) {
                return BibTeXTokenType.IDENTIFIERTEXT;
            }
            return BibTeXTokenType.CONTENTTEXT;
        }
    }

    private static List<BibTeXToken> parseBraceExpression(
        final BibTeXTokenType terminator,
        final Iterator<BibTeXToken> iterator
    ) throws IOException {
        int nesting = 1;
        final List<BibTeXToken> result = new LinkedList<BibTeXToken>();
        while (true) {
            if (!iterator.hasNext()) {
                throw new IOException("Brace expression ended before completion!");
            }
            final BibTeXToken token = iterator.next();
            if (token.type() == BibTeXTokenType.OPEN_BRACE) {
                nesting++;
            } else if (token.type() == BibTeXTokenType.CLOSE_BRACE) {
                nesting--;
                if (nesting == 0) {
                    if (terminator != BibTeXTokenType.CLOSE_BRACE) {
                        throw new IOException("Braces do not match!");
                    }
                    break;
                }
            } else if (nesting == 1 && token.type() == terminator) {
                break;
            }
            result.add(token);
        }
        return result;
    }

    private static boolean parseComma(final CharacterBuffer buffer) {
        // TODO Auto-generated method stub
        return false;
    }

    private static String parseIdentifier(final char terminator, final CharacterBuffer buffer) throws IOException {
        final StringBuilder identifierText = new StringBuilder();
        Character c = buffer.getNext(identifierText);
        while (c != null && !c.equals(terminator)) {
            c = buffer.getNext(identifierText);
        }
        if (c == null) {
            throw new IOException("Identifier ended before completion!");
        }
        buffer.appendReadContent(identifierText);
        final String identifier = identifierText.toString().trim();
        if (!identifier.matches("[a-zA-Z][^\",=\\(\\)\\{\\}'%#\\s]*")) {
            throw new IOException("Identifier contains illegal characters!");
        }
        return identifier;
    }

    private static BibTeXObject parseObject(final Iterator<BibTeXToken> iterator) throws IOException {
        if (!iterator.hasNext()) {
            throw new IOException("Command ended before completion!");
        }
        final BibTeXToken command = iterator.next();
        if (!iterator.hasNext()) {
            throw new IOException("Command ended before completion!");
        }
        final BibTeXToken open = iterator.next();
        BibTeXTokenType terminator = BibTeXTokenType.CLOSE_PARENTHESIS;
        switch (open.type()) {
        case OPEN_BRACE:
            terminator = BibTeXTokenType.CLOSE_BRACE;
        case OPEN_PARENTHESIS:
            break;
        default:
            throw new IOException("Command contains illegal characters!");
        }
        switch (command.type()) {
        case IDENTIFIERTEXT:
            switch (command.text().toLowerCase()) {
            case "string":
                final BibTeXToken abbreviation = BibTeXParser.forwardWhiteSpace(iterator, "String");
                if (abbreviation.type() != BibTeXTokenType.IDENTIFIERTEXT) {
                    throw new IOException("String identifier contains illegal characters!");
                }
                final BibTeXToken assign = BibTeXParser.forwardWhiteSpace(iterator, "String");
                if (assign.type() != BibTeXTokenType.ASSIGN) {
                    throw new IOException("String assignment is not defined correctly!");
                }
                final List<BibTeXToken> value = BibTeXParser.parseBraceExpression(terminator, iterator);
                return new BibTeXString(abbreviation.text(), BibTeXParser.parseValue(value));
            case "preamble":
                final List<BibTeXToken> preambleText = BibTeXParser.parseBraceExpression(terminator, iterator);
                return new BibTeXPreamble(BibTeXParser.shrinkSpace(BibTeXParser.toString(preambleText)));
            case "comment":
                final List<BibTeXToken> commentText = BibTeXParser.parseBraceExpression(terminator, iterator);
                return new BibTeXComment(BibTeXParser.toString(commentText));
            default:
            }
            // fall through
        case CONTENTTEXT:
            final BibTeXToken identifier = BibTeXParser.forwardWhiteSpace(iterator, "Entry");
            if (identifier.type() != BibTeXTokenType.IDENTIFIERTEXT) {
                throw new IOException("Entry identifier contains illegal characters!");
            }
            final BibTeXToken comma = BibTeXParser.forwardWhiteSpace(iterator, "Entry");
            if (comma.type() != BibTeXTokenType.COMMA) {
                throw new IOException("Entry identifier is not terminated by a comma!");
            }
            final List<BibTeXToken> content = BibTeXParser.parseBraceExpression(terminator, iterator);
            return new BibTeXEntry(command.text(), identifier.text(), BibTeXParser.parseTags(content));
        default:
            throw new IOException("Command contains illegal characters!");
        }
    }

    private static void parseTags(
        final CharacterBuffer buffer,
        final Map<String, List<String>> tags
    ) throws IOException {
        final String field;
        try {
            field = BibTeXParser.parseIdentifier('=', buffer);
        } catch (final IOException e) {
            if ("Identifier ended before completion!".equals(e.getMessage())) {
                final StringBuilder check = new StringBuilder();
                buffer.appendReadContent(check);
                if (check.toString().isBlank()) {
                    return;
                }
            }
            throw e;
        }
        final List<String> value = new LinkedList<String>();
        BibTeXParser.parseValue(buffer, value);
        tags.put(field, value);
        if (BibTeXParser.parseComma(buffer)) {
            BibTeXParser.parseTags(buffer, tags);
        }
    }

    private static Map<String, BibTeXValue> parseTags(final List<BibTeXToken> content) throws IOException {
        if (content.isEmpty()) {
            return Map.of();
        }
        final Map<String, BibTeXValue> tags = new LinkedHashMap<String, BibTeXValue>();
//        BibTeXParser.parseTags(buffer, tags);
        return tags;
    }

    private static void parseValue(final CharacterBuffer buffer, final List<String> value) throws IOException {
        // TODO Auto-generated method stub
        final StringBuilder sink = new StringBuilder();
        Character c = buffer.getNext(sink);
        while (c != null && Character.isWhitespace(c)) {
            c = buffer.getNext(sink);
        }
        if (c == null) {
            throw new IOException("Value ended before completion!");
        }
        if (c.equals('{')) {
//            value.add(BibTeXParser.parseBraceExpression('}', buffer));
            return;
        }
//        switch (c.charValue()) {
//        case '"':
//            final List<String> quoteText = BibTeXParser.parseBraceExpression('"', buffer);
//            tags.put(field, quoteText);
//            break;
//        case '{':
//            tags.put(field, List.of(bracedText));
//            break;
//        default:
//            buffer.appendReadContent(sink);
//            final StringBuilder nude = new StringBuilder();
//            nude.append(c.charValue());
//            c = buffer.getNext(nude);
//            while (c != null && !c.equals(',')) {
//                c = buffer.getNext(nude);
//            }
//            buffer.appendReadContent(nude);
//            final String nudeText = nude.toString().trim();
//            if (!nudeText.matches("")) {
//                throw new IOException("Found unquoted and unbraced non-numeric and non-identifier value!");
//            }
//        }
    }

    private static BibTeXValue parseValue(final List<BibTeXToken> value) {
        final BibTeXValue result = null;
        final StringBuilder text = new StringBuilder();
        final Iterator<BibTeXToken> iterator = value.iterator();
        while (iterator.hasNext()) {
            final BibTeXToken token = iterator.next();
            switch (token.type()) {
            case ASSIGN:
                break;
            case AT:
                break;
            case CLOSE_BRACE:
                break;
            case CLOSE_PARENTHESIS:
                break;
            case COMMA:
                break;
            case CONCAT:
                break;
            case CONTENTTEXT:
                break;
            case IDENTIFIERTEXT:
                break;
            case OPEN_BRACE:
                break;
            case OPEN_PARENTHESIS:
                break;
            case QUOTE:
                break;
            case WHITESPACE:
                if (!text.isEmpty()) {
                    text.append(" ");
                }
                break;
            default:
                break;

            }
        }
        // TODO Auto-generated method stub
//      if (!value.stream().allMatch(s -> s.matches("(\"(.|\\s)*\"|\\{(.|\\s)*\\})"))) {//TODO
//      throw new IOException("String value has illegal form!");
//  }
        return result;
    }

    private static String shrinkSpace(final String text) {
        String result = text;
        String reduced = result.replaceAll("\\s\\s", " ");
        while (!result.equals(reduced)) {
            result = reduced;
            reduced = result.replaceAll("\\s\\s", " ");
        }
        return result;
    }

    private static List<BibTeXToken> tokenize(final CharacterBuffer buffer) throws IOException {
        final List<BibTeXToken> tokens = new LinkedList<BibTeXToken>();
        StringBuilder text = new StringBuilder();
        BibTeXTokenType currentType = null;
        Character c = buffer.getNext(text);
        while (c != null) {
            final BibTeXTokenType type = BibTeXParser.getType(c, currentType);
            switch (type) {
            case CONTENTTEXT:
                if (currentType == null) {
                    currentType = type;
                } else {
                    switch (currentType) {
                    case WHITESPACE:
                        buffer.appendReadContent(text);
                        tokens.add(new BibTeXToken(currentType, text.toString()));
                        text.setLength(0);
                        text.append(c.charValue());
                        // fall through
                    default:
                        currentType = type;
                    }
                }
                break;
            case IDENTIFIERTEXT:
                if (currentType == null) {
                    currentType = type;
                } else {
                    switch (currentType) {
                    case CONTENTTEXT:
                        break;
                    case WHITESPACE:
                        buffer.appendReadContent(text);
                        tokens.add(new BibTeXToken(currentType, text.toString()));
                        text = new StringBuilder();
                        text.append(c.charValue());
                        // fall through
                    default:
                        currentType = type;
                    }
                }
                break;
            case WHITESPACE:
                if (currentType == null) {
                    currentType = type;
                } else {
                    switch (currentType) {
                    case CONTENTTEXT:
                    case IDENTIFIERTEXT:
                        buffer.appendReadContent(text);
                        tokens.add(new BibTeXToken(currentType, text.toString()));
                        text = new StringBuilder();
                        text.append(c.charValue());
                        // fall through
                    default:
                        currentType = type;
                    }
                }
                break;
            default:
                if (currentType == null) {
                    currentType = type;
                    buffer.appendReadContent(new StringBuilder());
                    tokens.add(new BibTeXToken(type, String.valueOf(c)));
                } else {
                    switch (currentType) {
                    case CONTENTTEXT:
                    case IDENTIFIERTEXT:
                    case WHITESPACE:
                        buffer.appendReadContent(text);
                        tokens.add(new BibTeXToken(currentType, text.toString()));
                        // fall through
                    default:
                        text.setLength(0);
                        currentType = type;
                        buffer.appendReadContent(new StringBuilder());
                        tokens.add(new BibTeXToken(type, String.valueOf(c)));
                    }
                }
            }
            c = buffer.getNext(text);
        }
        buffer.appendReadContent(text);
        final String remainder = text.toString();
        if (!remainder.isEmpty()) {
            tokens.add(new BibTeXToken(currentType, remainder));
        }
        return tokens;
    }

    private static String toString(final List<BibTeXToken> buffer) {
        return buffer.stream().map(BibTeXToken::text).collect(Collectors.joining());
    }

}
