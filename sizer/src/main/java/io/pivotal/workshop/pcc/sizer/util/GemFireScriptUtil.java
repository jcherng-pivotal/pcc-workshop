package io.pivotal.workshop.pcc.sizer.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class GemFireScriptUtil {

    public static void startGemFire() throws IOException, InterruptedException {
        runScript(System.getProperty("user.dir") + "/scripts/sh/startGemFire.sh");
        Thread.sleep(1000);
    }

    public static void shutdownGemFire() throws IOException, InterruptedException {
        runScript(System.getProperty("user.dir") + "/scripts/sh/shutdownGemFire.sh");
        Thread.sleep(1000);
    }

    public static void recreateRegions() throws IOException, InterruptedException {
        runScript(System.getProperty("user.dir") + "/scripts/sh/create-regions.sh");
        Thread.sleep(1000);
    }

    public static void recreateIndexes() throws IOException, InterruptedException {
        runScript(System.getProperty("user.dir") + "/scripts/sh/create-indexes.sh");
        Thread.sleep(1000);
    }

    private static void runScript(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        new Thread(new StreamGobbler(process.getInputStream())).start();
        new Thread(new StreamGobbler(process.getErrorStream())).start();
        process.waitFor();
    }

    private static class StreamGobbler implements Runnable {

        BufferedReader stream;

        public StreamGobbler(InputStream stream) {
            this.stream = new BufferedReader(new InputStreamReader(stream));
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = stream.readLine()) != null) {
                    log.info(line);
                }
                stream.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
