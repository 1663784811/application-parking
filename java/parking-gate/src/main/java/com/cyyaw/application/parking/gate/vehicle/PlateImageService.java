package com.cyyaw.application.parking.gate.vehicle;

import com.cyyaw.application.parking.gate.config.GateProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * 车牌图片落地服务（第一步：下载图片到本地）。
 * <p>识别 {@code img}/{@code numberImg} 的内容形态并落地到 {@code gate.plate.image-dir}：</p>
 * <ul>
 *   <li>http(s):// 开头 → HTTP 下载</li>
 *   <li>Base64 字符串 → 解码后写文件</li>
 *   <li>其它（空 / 已为本地路径） → 原样返回，已存在则返回该路径，否则 null</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlateImageService {

    private static final Pattern BASE64 = Pattern.compile("^[A-Za-z0-9+/]+={0,2}$");

    private final GateProperties gateProperties;
    private HttpClient httpClient;

    /**
     * 把原始图片值落地为本地文件，返回本地路径（null 表示未落地）。
     *
     * @param raw    图片原始值（URL / base64 / 本地路径 / 空）
     * @param prefix 落地文件名前缀（img / number）
     */
    public Path saveImage(String raw, String prefix) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            Path dir = Paths.get(gateProperties.getPlate().getImageDir());
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String trimmed = raw.trim();
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return download(trimmed, dir, prefix);
            }
            if (BASE64.matcher(trimmed).matches() && trimmed.length() >= 16) {
                return write(Base64.getDecoder().decode(trimmed), dir, prefix, "jpg");
            }
            // 既非 URL 也非 base64，按已存在的本地路径处理
            Path asPath = Paths.get(trimmed);
            return Files.exists(asPath) ? asPath : null;
        } catch (Exception e) {
            log.warn("图片落地失败 prefix={}：{}", prefix, e.getMessage());
            return null;
        }
    }

    private Path download(String url, Path dir, String prefix) throws Exception {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        }
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(20)).GET().build();
        HttpResponse<byte[]> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) {
            log.warn("下载图片失败 status={} url={}", resp.statusCode(), url);
            return null;
        }
        return write(resp.body(), dir, prefix, guessExt(url));
    }

    private Path write(byte[] data, Path dir, String prefix, String ext) throws Exception {
        String name = prefix + "-" + System.currentTimeMillis() + "." + ext;
        Path file = dir.resolve(name);
        Files.write(file, data);
        return file;
    }

    private String guessExt(String url) {
        int q = url.indexOf('?');
        String path = q > 0 ? url.substring(0, q) : url;
        int dot = path.lastIndexOf('.');
        int slash = path.lastIndexOf('/');
        if (dot > slash) {
            String e = path.substring(dot + 1).toLowerCase();
            if (e.matches("[a-z0-9]{2,4}")) {
                return e;
            }
        }
        return "jpg";
    }
}
