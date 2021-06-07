package com.example.reload;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RefreshScope
@Component
public class ReloadClassesMonitor {

    @Value("${app.reload.classdir}")
    private String classdir;
    @Value("${app.reload.agentdir}")
    private String agentdir;

    private FileAlterationMonitor monitor;

    @PostConstruct
    public void start() {
        File baseDir = new File(classdir);
        if(!baseDir.exists()) {
            if(!baseDir.mkdirs()) {
                log.error("class目录不存在！目录：" + classdir);
                return;
            }
        }
        FileAlterationObserver observer = new FileAlterationObserver(baseDir);
        observer.addListener(new FileAlterationListenerAdaptor() {

            private List<File> files = new ArrayList<>();

            @Override
            public void onStart(FileAlterationObserver observer) {
                files = new ArrayList<>();
            }

            @Override
            public void onFileCreate(File file) {
                files.add(file);
            }

            @Override
            public void onFileChange(File file) {
                files.add(file);
            }

            @Override
            public void onStop(FileAlterationObserver observer) {
                List<File> tmpFiles = files;
                try {
                    reloadClasses(baseDir, tmpFiles);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });

        monitor = new FileAlterationMonitor(2000, observer);
        try {
            monitor.start();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void reloadClasses(File baseDir, List<File> files) throws Exception {
        if(files.isEmpty()) {
            return;
        }
        List<String> classNames = new ArrayList<>(files.size());
        for (File file : files) {
            // 去掉目录前缀，去掉.class后缀
            String relativePath = file.getAbsolutePath().substring(baseDir.getAbsolutePath().length(), file.getAbsolutePath().length() - 6);
            relativePath = relativePath.replaceAll("[/\\\\]+", ".");
            if(relativePath.startsWith(".")) {
                relativePath = relativePath.substring(1);
            }
            classNames.add(relativePath);
        }
        loadInstrumentationAgent(classNames);
    }

    private void loadInstrumentationAgent(List<String> classNames) throws Exception {
        log.info("reload:" + classNames);
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
            String localAgentDir = this.agentdir;
            if(!StringUtils.hasText(localAgentDir)) {
                String os = System.getProperty("os.name");
                if(os.toLowerCase().startsWith("win")) {
                    localAgentDir = "c:/HotSwapAgent.jar";
                } else {
                    localAgentDir = "/usr/local/hotswap/HotSwapAgent.jar";
                }
            }
            vm.loadAgent(localAgentDir, String.join(",", classNames));
        } finally {
            if(vm != null) {
                vm.detach();
            }
        }
    }

    @PreDestroy
    public void stop() {
        try {
            if(monitor != null) {
                monitor.stop();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

}
