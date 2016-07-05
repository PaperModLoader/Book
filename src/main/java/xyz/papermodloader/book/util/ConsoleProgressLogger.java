package xyz.papermodloader.book.util;

public class ConsoleProgressLogger implements ProgressLogger {
    @Override
    public void onProgress(int current, int total) {
        System.out.println(current + "/" + total);
    }
}
