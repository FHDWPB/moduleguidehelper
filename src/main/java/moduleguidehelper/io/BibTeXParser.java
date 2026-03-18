package moduleguidehelper.io;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import moduleguidehelper.model.bibtex.*;

public class BibTeXParser {

    private static record ValueAndToken(BibTeXValue value, BibTeXToken token) {}

    private static final String NUMBER_REGEX = "\\d+";

    public static BibTeXDatabase parse(final Reader reader) throws IOException {
        final List<BibTeXToken> tokens = BibTeXParser.tokenize(new CharacterBuffer(reader));
        final BibTeXDatabase result = new BibTeXDatabase();
        final Iterator<BibTeXToken> iterator = tokens.iterator();
        final List<BibTeXToken> buffer = new LinkedList<BibTeXToken>();
        while (iterator.hasNext()) {
            final BibTeXToken token = iterator.next();
            if (token.type() == BibTeXTokenType.AT) {
                if (!buffer.isEmpty() && buffer.stream().anyMatch(t -> t.type() != BibTeXTokenType.WHITESPACE)) {
                    result.add(new BibTeXFreeComment(BibTeXParser.toString(buffer, false)));
                }
                buffer.clear();
                result.add(BibTeXParser.parseObject(iterator));
            } else {
                buffer.add(token);
            }
        }
        if (!buffer.isEmpty() && buffer.stream().anyMatch(t -> t.type() != BibTeXTokenType.WHITESPACE)) {
            result.add(new BibTeXFreeComment(BibTeXParser.toString(buffer, false)));
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
        case '_':
        case '-':
        case ':':
            return BibTeXTokenType.IDENTIFIERTEXT;
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
                if (
                    abbreviation.type() != BibTeXTokenType.IDENTIFIERTEXT
                    || !Character.isLetter(abbreviation.text().charAt(0))
                ) {
                    throw new IOException("String identifier contains illegal characters!");
                }
                final BibTeXToken assign = BibTeXParser.forwardWhiteSpace(iterator, "String");
                if (assign.type() != BibTeXTokenType.ASSIGN) {
                    throw new IOException("String assignment is not defined correctly!");
                }
                final List<BibTeXToken> value = BibTeXParser.parseBraceExpression(terminator, iterator);
                final Iterator<BibTeXToken> valueIterator = value.iterator();
                final ValueAndToken parsedValue = BibTeXParser.parseValue(valueIterator);
                if (valueIterator.hasNext()) {
                    BibTeXToken token = valueIterator.next();
                    if (token.type() == BibTeXTokenType.WHITESPACE && valueIterator.hasNext()) {
                        token = valueIterator.next();
                    }
                    if (token.type() != BibTeXTokenType.WHITESPACE) {
                        throw new IOException("String value contains content after completion!");
                    }
                }
                return new BibTeXString(abbreviation.text(), parsedValue.value());
            case "preamble":
                final List<BibTeXToken> preambleText = BibTeXParser.parseBraceExpression(terminator, iterator);
                return new BibTeXPreamble(BibTeXParser.shrinkSpace(BibTeXParser.toString(preambleText, false)));
            case "comment":
                final List<BibTeXToken> commentText = BibTeXParser.parseBraceExpression(terminator, iterator);
                return new BibTeXComment(BibTeXParser.toString(commentText, false));
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
        final Iterator<BibTeXToken> iterator,
        final Map<String, BibTeXValue> tags
    ) throws IOException {
        if (!iterator.hasNext()) {
            return;
        }
        BibTeXToken identifier = iterator.next();
        if (identifier.type() == BibTeXTokenType.WHITESPACE) {
            if (!iterator.hasNext()) {
                return;
            }
            identifier = iterator.next();
        }
        if (identifier.type() != BibTeXTokenType.IDENTIFIERTEXT) {
            throw new IOException("Tag identifier contains illegal characters!");
        }
        final BibTeXToken assign = BibTeXParser.forwardWhiteSpace(iterator, "Tag");
        if (assign.type() != BibTeXTokenType.ASSIGN) {
            throw new IOException("Tag definition is missing assignment!");
        }
        final ValueAndToken value = BibTeXParser.parseValue(iterator);
        tags.put(identifier.text(), value.value());
        if (iterator.hasNext()) {
            if (value.token() == null) {
                BibTeXToken next = iterator.next();
                if (next.type() == BibTeXTokenType.WHITESPACE && iterator.hasNext()) {
                    next = iterator.next();
                }
                if (next.type() == BibTeXTokenType.COMMA) {
                    BibTeXParser.parseTags(iterator, tags);
                } else if (next.type() != BibTeXTokenType.WHITESPACE || iterator.hasNext()) {
                    throw new IOException("Content after tag not separated by comma!");
                }
            } else {
                switch (value.token().type()) {
                case COMMA:
                    BibTeXParser.parseTags(iterator, tags);
                    break;
                case WHITESPACE:
                    if (iterator.hasNext()) {
                        final BibTeXToken comma = iterator.next();
                        if (comma.type() == BibTeXTokenType.COMMA) {
                            BibTeXParser.parseTags(iterator, tags);
                        } else {
                            throw new IOException("Content after tag not separated by comma!");
                        }
                    }
                    break;
                default:
                    throw new IOException("Content after tag not separated by comma!");
                }
            }
        }
    }

    private static Map<String, BibTeXValue> parseTags(final List<BibTeXToken> content) throws IOException {
        if (content.isEmpty() || (content.size() == 1 && content.getFirst().type() == BibTeXTokenType.WHITESPACE)) {
            return Map.of();
        }
        final Map<String, BibTeXValue> tags = new LinkedHashMap<String, BibTeXValue>();
        final Iterator<BibTeXToken> iterator = content.iterator();
        BibTeXParser.parseTags(iterator, tags);
        return tags;
    }

    private static ValueAndToken parseValue(final Iterator<BibTeXToken> iterator) throws IOException {
        final BibTeXToken start = BibTeXParser.forwardWhiteSpace(iterator, "Value");
        switch (start.type()) {
        case IDENTIFIERTEXT:
            if (iterator.hasNext()) {
                BibTeXToken next = iterator.next();
                if (next.type() == BibTeXTokenType.WHITESPACE) {
                    if (iterator.hasNext()) {
                        next = iterator.next();
                    }
                }
                if (next.type() == BibTeXTokenType.CONCAT) {
                    if (!Character.isLetter(start.text().charAt(0))) {
                        throw new IOException("Identifier does not start with a letter!");
                    }
                    final ValueAndToken parsed = BibTeXParser.parseValue(iterator);
                    return new ValueAndToken(
                        new BibTeXConcatenation(new BibTeXIdentifier(start.text()), parsed.value()),
                        parsed.token()
                    );
                }
                final String text = start.text();
                if (text.matches(BibTeXParser.NUMBER_REGEX)) {
                    return new ValueAndToken(new BibTeXNumber(Integer.parseInt(text)), next);
                }
                return new ValueAndToken(new BibTeXIdentifier(text), next);
            }
            final String startText = start.text();
            if (startText.matches(BibTeXParser.NUMBER_REGEX)) {
                return new ValueAndToken(new BibTeXNumber(Integer.parseInt(startText)), null);
            }
            return new ValueAndToken(new BibTeXIdentifier(startText), null);
        case OPEN_BRACE:
            final List<BibTeXToken> braced = BibTeXParser.parseBraceExpression(BibTeXTokenType.CLOSE_BRACE, iterator);
            final String bracedText = BibTeXParser.toString(braced, true);
            if (bracedText.matches(BibTeXParser.NUMBER_REGEX)) {
                return new ValueAndToken(new BibTeXNumber(Integer.parseInt(bracedText)), null);
            }
            return new ValueAndToken(new BibTeXText(bracedText), null);
        case QUOTE:
            final List<BibTeXToken> quoted = BibTeXParser.parseBraceExpression(BibTeXTokenType.QUOTE, iterator);
            final String content = BibTeXParser.toString(quoted, true);
            final BibTeXValue value =
                content.matches(BibTeXParser.NUMBER_REGEX) ?
                    new BibTeXNumber(Integer.parseInt(content)) :
                        new BibTeXText(content);
            if (iterator.hasNext()) {
                BibTeXToken next = iterator.next();
                if (next.type() == BibTeXTokenType.WHITESPACE) {
                    if (iterator.hasNext()) {
                        next = iterator.next();
                    }
                }
                if (next.type() == BibTeXTokenType.CONCAT) {
                    final ValueAndToken parsed = BibTeXParser.parseValue(iterator);
                    return new ValueAndToken(
                        new BibTeXConcatenation(new BibTeXText(content), parsed.value()),
                        parsed.token()
                    );
                }
                return new ValueAndToken(value, next);
            }
            return new ValueAndToken(value, null);
        default:
            throw new IOException("Value starts with illegal token!");
        }
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

    private static String toString(final List<BibTeXToken> buffer, final boolean shrinkWhiteSpace) {
        return buffer.stream()
            .map(token -> shrinkWhiteSpace && token.type() == BibTeXTokenType.WHITESPACE ? " " : token.text())
            .collect(Collectors.joining());
    }

}
