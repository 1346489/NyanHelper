package org.gradle.wrapper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BootstrapWrapperMain {
    public static void main(String[] args) throws Exception {
        // 读取 distributionUrl
        String distUrl = readDistributionUrl();
        String version = distUrl.substring(distUrl.lastIndexOf("/") + 1)
                .replace("gradle-", "").replace("-bin.zip", "").replace(".zip", "");
        Path gradleUserHome = Paths.get(System.getProperty("user.home"), ".gradle");
        Path dists = gradleUserHome.resolve("wrapper/dists").resolve("gradle-" + version + "-bin");
        // 使用已下载的 gradle（CI 中由 gradle-build-action 提供，本地若已安装则直接用）
        String gradleHome = System.getenv("GRADLE_HOME");
        if (gradleHome == null || gradleHome.isEmpty()) {
            // 尝试常见位置
            String[] candidates = {
                System.getProperty("user.home") + "/.sdkman/candidates/gradle/" + version,
                "/opt/homebrew/Cellar/gradle/" + version + "/libexec",
                "/usr/share/gradle-" + version
            };
            for (String c : candidates) { if (new File(c).exists()) { gradleHome = c; break; } }
        }
        List<String> cmd = new ArrayList<>();
        if (gradleHome != null && !gradleHome.isEmpty()) {
            cmd.add(gradleHome + File.separator + "bin" + File.separator + (isWindows() ? "gradle.bat" : "gradle"));
        } else {
            cmd.add(isWindows() ? "gradle.bat" : "gradle");
        }
        for (String a : args) cmd.add(a);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        System.exit(pb.start().waitFor());
    }
    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("windows");
    }
    private static String readDistributionUrl() {
        // 默认值（与实际 properties 保持一致）
        return "https://services.gradle.org/distributions/gradle-8.7-bin.zip";
    }
}
