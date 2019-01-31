package ru.javaops.masterjava.web;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileAsByteArrayManager {
    public byte[] readfileAsBytes(File file) throws IOException {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "r")) {
            byte[] bytes = new byte[(int) accessFile.length()];
            accessFile.readFully(bytes);
            return bytes;
        }
    }

    public void writeFile(byte[] bytes, String fileName) throws IOException {
        File file = new File(fileName);
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            accessFile.write(bytes);
        }
    }
}
