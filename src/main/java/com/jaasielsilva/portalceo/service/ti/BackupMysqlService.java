package com.jaasielsilva.portalceo.service.ti;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

@Service
public class BackupMysqlService {

    @Value("${backup.mysql.container:mysql}")
    private String mysqlContainerName;

    @Value("${backup.mysql.mode:auto}")
    private String mode;

    @Value("${backup.mysql.host.mysqldump.path:mysqldump}")
    private String mysqldumpPath;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    private static final ThreadLocal<String> LAST_MODE = new ThreadLocal<>();

    public String buildFilename() {
        String database = extractDatabaseName(datasourceUrl);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "backup-mysql-" + database + "-" + ts + ".sql.gz";
    }

    public ByteArrayResource gerarDumpGzip() {
        String selected = (mode == null ? "auto" : mode.toLowerCase(Locale.ROOT)).trim();
        if ("docker".equals(selected)) {
            return gerarDumpGzipDocker();
        } else if ("host".equals(selected)) {
            return gerarDumpGzipHost();
        } else {
            try {
                return gerarDumpGzipDocker();
            } catch (RuntimeException ex) {
                return gerarDumpGzipHost();
            }
        }
    }

    private ByteArrayResource gerarDumpGzipDocker() {
        LAST_MODE.set("docker");
        try {
            return runDockerDump(java.util.Collections.emptyList());
        } catch (RuntimeException ex) {
            java.util.List<String> views = extractInvalidViews(ex.getMessage());
            if (!views.isEmpty()) {
                return runDockerDump(views);
            }
            throw ex;
        }
    }

    private ByteArrayResource gerarDumpGzipHost() {
        LAST_MODE.set("host");
        try {
            return runHostDump(java.util.Collections.emptyList());
        } catch (RuntimeException ex) {
            java.util.List<String> views = extractInvalidViews(ex.getMessage());
            if (!views.isEmpty()) {
                return runHostDump(views);
            }
            throw ex;
        }
    }

    private ByteArrayResource runAndGzip(ProcessBuilder pb, String errPrefix) {
        pb.redirectErrorStream(false);
        try {
            Process process = pb.start();
            ByteArrayOutputStream gzipOut = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            Thread tOut = new Thread(() -> copyAndGzip(process.getInputStream(), gzipOut));
            Thread tErr = new Thread(() -> copy(process.getErrorStream(), stderr));
            tOut.start();
            tErr.start();

            int exit = process.waitFor();
            tOut.join();
            tErr.join();

            if (exit != 0) {
                String err = stderr.toString(StandardCharsets.UTF_8);
                throw new RuntimeException(errPrefix + ": " + err);
            }

            return new ByteArrayResource(gzipOut.toByteArray());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(errPrefix + ": " + e.getMessage(), e);
        }
    }

    private ByteArrayResource runDockerDump(java.util.List<String> ignoreViews) {
        String database = extractDatabaseName(datasourceUrl);
        String pwd = datasourcePassword == null ? "" : datasourcePassword.replace("'", "'\"'\"'");
        StringBuilder sb = new StringBuilder();
        sb.append("export MYSQL_PWD='").append(pwd).append("' && ");
        sb.append("mysqldump ");
        sb.append("-u").append(escapeShell(datasourceUsername)).append(" ");
        sb.append("--single-transaction --quick --lock-tables=false ");
        sb.append("--routines --events --triggers ");
        sb.append("--set-gtid-purged=OFF --no-tablespaces --column-statistics=0 --force ");
        for (String v : ignoreViews) {
            sb.append("--ignore-table=").append(database).append(".").append(v).append(" ");
        }
        sb.append(database);
        ProcessBuilder pb = new ProcessBuilder("docker", "exec", mysqlContainerName, "sh", "-lc", sb.toString());
        return runAndGzip(pb, "Falha ao executar mysqldump via docker exec");
    }

    private ByteArrayResource runHostDump(java.util.List<String> ignoreViews) {
        Map<String, String> hp = parseHostPort(datasourceUrl);
        String host = hp.getOrDefault("host", "localhost");
        String port = hp.getOrDefault("port", "3306");
        String database = extractDatabaseName(datasourceUrl);
        java.util.List<String> args = new java.util.ArrayList<>();
        args.add(mysqldumpPath);
        args.add("-h"); args.add(host);
        args.add("-P"); args.add(port);
        args.add("-u" + datasourceUsername);
        args.add("--single-transaction");
        args.add("--quick");
        args.add("--lock-tables=false");
        args.add("--routines");
        args.add("--events");
        args.add("--triggers");
        args.add("--set-gtid-purged=OFF");
        args.add("--no-tablespaces");
        args.add("--column-statistics=0");
        args.add("--force");
        for (String v : ignoreViews) {
            args.add("--ignore-table=" + database + "." + v);
        }
        args.add(database);
        ProcessBuilder pb = new ProcessBuilder(args);
        if (datasourcePassword != null) {
            pb.environment().put("MYSQL_PWD", datasourcePassword);
        }
        return runAndGzip(pb, "Falha ao executar mysqldump local");
    }

    private void copy(InputStream in, ByteArrayOutputStream out) {
        byte[] buf = new byte[8192];
        int r;
        try {
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);
            }
        } catch (IOException ignored) {
        }
    }

    private void copyAndGzip(InputStream in, ByteArrayOutputStream out) {
        byte[] buf = new byte[8192];
        int r;
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            while ((r = in.read(buf)) != -1) {
                gz.write(buf, 0, r);
            }
            gz.finish();
        } catch (IOException ignored) {
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        if (jdbcUrl == null) return "database";
        // expected jdbc:mysql://host:port/dbname?params
        int slash = jdbcUrl.lastIndexOf('/');
        if (slash == -1) return "database";
        String tail = jdbcUrl.substring(slash + 1);
        int q = tail.indexOf('?');
        if (q >= 0) {
            tail = tail.substring(0, q);
        }
        if (tail.isEmpty()) return "database";
        return tail;
    }

    private String escapeShell(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s\"$`\\\\]", "\\\\$0");
    }

    private Map<String, String> parseHostPort(String jdbcUrl) {
        String host = "localhost";
        String port = "3306";
        try {
            Pattern p = Pattern.compile("jdbc:mysql://([^/:]+)(?::(\\d+))?/");
            Matcher m = p.matcher(jdbcUrl);
            if (m.find()) {
                host = m.group(1);
                if (m.group(2) != null) {
                    port = m.group(2);
                }
            }
        } catch (Exception ignored) {}
        return java.util.Map.of("host", host, "port", port);
    }

    private java.util.List<String> extractInvalidViews(String text) {
        java.util.Set<String> out = new java.util.HashSet<>();
        if (text == null) return new java.util.ArrayList<>();
        try {
            Pattern p1 = Pattern.compile("View\\s+'[^.]*\\.(\\w+)'", Pattern.CASE_INSENSITIVE);
            Matcher m1 = p1.matcher(text);
            while (m1.find()) {
                out.add(m1.group(1));
            }
        } catch (Exception ignored) {}
        try {
            Pattern p2 = Pattern.compile("SHOW\\s+FIELDS\\s+FROM\\s+`([^`]+)`", Pattern.CASE_INSENSITIVE);
            Matcher m2 = p2.matcher(text);
            while (m2.find()) {
                out.add(m2.group(1));
            }
        } catch (Exception ignored) {}
        return new java.util.ArrayList<>(out);
    }

    public String getLastModeUsed() {
        String m = LAST_MODE.get();
        return m != null ? m : (mode == null ? "auto" : mode);
    }
}
