package xyz.papermodloader.book.converter.exporter;

import xyz.papermodloader.book.util.ProgressLogger;

import java.io.File;
import java.io.IOException;

public interface Exporter {
    void export(File input, File output, ProgressLogger logger) throws IOException;
}
