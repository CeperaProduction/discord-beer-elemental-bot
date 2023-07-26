package me.cepera.discord.bot.beerelemental.remote;

import java.util.Arrays;
import java.util.Objects;

public class FileData {

    private final String fileName;

    private final String contentType;

    private final byte[] bytes;

    public FileData(String fileName, String contentType, byte[] bytes) {
        super();
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bytes);
        result = prime * result + Objects.hash(contentType, fileName);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileData other = (FileData) obj;
        return Arrays.equals(bytes, other.bytes) && Objects.equals(contentType, other.contentType)
                && Objects.equals(fileName, other.fileName);
    }

    @Override
    public String toString() {
        return "FileData [fileName=" + fileName + ", contentType=" + contentType + ", size="
                + bytes.length + "]";
    }

}
