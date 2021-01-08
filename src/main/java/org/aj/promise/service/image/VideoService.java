package org.aj.promise.service.image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aj.promise.domain.Video;
import org.aj.promise.domain.Video.State;
import org.aj.promise.repository.VideoMongoRepository;
import org.aj.promise.service.storage.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class VideoService {
    @Autowired
    VideoMongoRepository videoMongoRepository;

    @Autowired
    private StorageService storageService;

    private String COMPRESSION_SIGN = "_compression";
    private String POSTER_SUFFIX = "_poster.png";
    private String VIDEA_TEMP_DIR = "vedio/temp";

    private Path VIDEA_TEMP_PATH = null;

    private volatile boolean IS_RUN = false;
    private ExecutorService exportMessagePool = Executors.newFixedThreadPool(2);

    public void addVideo(String url) {
        int state = createPoster(url);
        if (state != 0) {
            return;
        }
        Video video = Video.of(url);
        videoMongoRepository.save(video);
        compress();
    }

    private void compress() {
        exportMessagePool.submit(() -> doCompress());
    }

    private void doCompress() {
        try {
            if (IS_RUN) {
                return;
            }
            IS_RUN = true;
            while (true) {
                Page<Video> page = videoMongoRepository.findAllByState(State.Pending, PageRequest.of(0, 10));
                if (page.getContent().size() == 0) {
                    break;
                }
                for (Video item : page.getContent()) {
                    log.info("begin compressing video id={}", item.getId());
                    int result = createCompress(item);
                    log.info("end compressing video id={},result={}", item.getId(), result);
                }
            }
        } catch (Exception e) {
            log.error("doCompress error", e);
        } finally {
            IS_RUN = false;
        }
    }

    private int createPoster(String url) {
        String posterUrl = getVideoPosterUrl(url);

        String srcFile = storageService.getPath(Paths.get(url)).toString();
        String posterFile = storageService.getPath(Paths.get(posterUrl)).toString();

        // ffmpeg -i 'b.mp4' -ss 00:00:01.000 -y -vframes 1 'thumb.png'
        String[] cmd = { "ffmpeg", "-i", srcFile, "-ss", "00:00:01.000", "-y", "-vframes", "1", posterFile };
        return runShell(cmd);
    }

    private int createCompress(Video video) throws IOException {
        String srcUrl = video.getSourceUrl();
        String compressionUrl = getVideoCompressionUrl(srcUrl);

        String srcFile = storageService.getPath(Paths.get(srcUrl)).toString();
        String compressionFile = storageService.getPath(Paths.get(compressionUrl)).toString();
        String tempFile = getVideoTempCompressionFile(srcUrl);

        String[] compressCmd = { "ffmpeg", "-i", srcFile, "-b", "640k", "-y", tempFile };
        int result = runShell(compressCmd);
        if (result == 0) {
            String[] rmCmd = { "mv", "-f", tempFile, compressionFile };
            result = runShell(rmCmd);
        }
        if (result == 0 && Files.exists(Paths.get(compressionFile))) {
            video.setState(State.Success);
            video.setCompressionUrl(compressionUrl);
        } else {
            video.setState(State.Fail);
        }
        video.setUpdateTime(System.currentTimeMillis());
        videoMongoRepository.save(video);
        return result;
    }

    public String getVideoCompressionUrl(String videoUrl) {
        int index = videoUrl.lastIndexOf(".");
        String compressionUrl = videoUrl.substring(0, index) + COMPRESSION_SIGN + videoUrl.substring(index);
        return compressionUrl;
    }

    private String getVideoTempCompressionFile(String videoUrl) throws IOException {
        if (VIDEA_TEMP_PATH == null) {
            Path path = storageService.getPath(Paths.get(VIDEA_TEMP_DIR));
            if (Files.notExists(path)) {
                log.info("create video temp directories: {}", path);
                Files.createDirectories(path);
            }
            VIDEA_TEMP_PATH = path;
        }
        int index = videoUrl.lastIndexOf("/");
        String name = videoUrl.substring(index + 1);
        return VIDEA_TEMP_PATH.resolve(name).toString();
    }

    public String getVideoPosterUrl(String videoUrl) {
        int index = videoUrl.lastIndexOf(".");
        String posterUrl = videoUrl.substring(0, index) + POSTER_SUFFIX;
        return posterUrl;
    }

    private int runShell(String[] cmd) {
        int result = -1;
        Process process = null;
        long start = System.currentTimeMillis();
        try {
            log.info("start shell cmd={}", StringUtils.join(cmd, " "));
            process = Runtime.getRuntime().exec(cmd);
            BufferedReader bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
            String line = null;
            while ((line = bufrError.readLine()) != null) {
                log.info(line);
            }
            process.waitFor();
            result = process.exitValue();
        } catch (Exception e) {
            log.error("runShell error,cmd={}", cmd, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        log.info("end shell time={} state={} cmd={} ", System.currentTimeMillis() - start, result,
                StringUtils.join(cmd, " "));
        return result;
    }
}
