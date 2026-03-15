package moduleguidehelper.io;

import java.io.*;

public class CharacterBuffer {

    private final char[] buffer;

    private int end;

    private int index;

    private final Reader reader;

    private int start;

    public CharacterBuffer(final int capacity, final Reader reader) {
        this.buffer = new char[capacity];
        this.reader = reader;
        this.start = 0;
        this.index = 0;
        this.end = 0;
    }

    public CharacterBuffer(final Reader reader) {
        this(1024, reader);
    }

    public void appendReadContent(final StringBuilder sink) {
        sink.append(this.buffer, this.start, Math.max(0, this.index - this.start - 1));
        this.start = this.index;
    }

    public Character getNext(final StringBuilder sink) throws IOException {
        if (this.index == this.end) {
            if (this.start == 0) {
                sink.append(this.buffer, 0, this.end);
                this.index = 0;
                this.end = 0;
            }
            for (int i = 0; i < this.end - this.start; i++) {
                this.buffer[i] = this.buffer[this.start + i];
            }
            this.index -= this.start;
            this.end -= this.start;
            this.start = 0;
            final int read = this.reader.read(this.buffer, this.end, this.buffer.length - this.end);
            if (read == -1) {
                sink.append(this.buffer, 0, this.end);
                return null;
            }
            this.end += read;
        }
        return this.buffer[this.index++];
    }

}
