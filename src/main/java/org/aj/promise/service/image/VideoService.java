package org.aj.promise.service.image;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
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
    private String ORIGINAL_SIGN = "_original";
    private String POSTER_SUFFIX = "_poster.png";

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
        IS_RUN = false;
    }

    private int createPoster(String url) {
        String posterUrl = getVideoPosterUrl(url);

        String srcFile = storageService.getPath(Paths.get(url)).toString();
        String posterFile = storageService.getPath(Paths.get(posterUrl)).toString();

        // ffmpeg -i 'b.mp4' -ss 00:00:01.000 -y -vframes 1 'thumb.png'
        String[] cmd = { "ffmpeg", "-i", srcFile, "-ss", "00:00:01.000", "-y", "-vframes", "1", posterFile };
        return runShell(cmd);
    }

    private int createCompress(Video video) {
        String srcUrl = video.getSourceUrl();
        String compressionUrl = getVideoCompressionUrl(srcUrl);
        String originalUrl = getVideoOriginalUrl(srcUrl);

        String srcFile = storageService.getPath(Paths.get(srcUrl)).toString();
        String compressionFile = storageService.getPath(Paths.get(compressionUrl)).toString();
        String originalFile = storageService.getPath(Paths.get(originalUrl)).toString();

        String[] compressCmd = { "ffmpeg", "-i", srcFile, "-b", "640k", "-y", compressionFile };
        int result = runShell(compressCmd);
        if (result == 0) {
            String[] rmCmd = { "mv", srcFile, originalFile };
            result = runShell(rmCmd);
        }
        if (result == 0 && Files.exists(Paths.get(compressionFile)) && Files.exists(Paths.get(originalFile))) {
            video.setState(State.Success);
            video.setCompressionUrl(compressionUrl);
            video.setOriginalUrl(originalUrl);
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

    public String getVideoOriginalUrl(String videoUrl) {
        int index = videoUrl.lastIndexOf(".");
        String compressionUrl = videoUrl.substring(0, index) + ORIGINAL_SIGN + videoUrl.substring(index);
        return compressionUrl;
    }

    public String getVideoPosterUrl(String videoUrl) {
        int index = videoUrl.lastIndexOf(".");
        String posterUrl = videoUrl.substring(0, index) + POSTER_SUFFIX;
        return posterUrl;
    }

    private int runShell(String[] cmd) {
        int result = -1;
        Process process = null;
        try {
            log.info("-----------------start shell cmd: {}-----------------", StringUtils.join(cmd, " "));
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            result = process.exitValue();
            BufferedReader bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            BufferedReader bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                log.info(line);
            }
            while ((line = bufrError.readLine()) != null) {
                log.info(line);
            }
        } catch (Exception e) {
            log.error("runShell error,cmd={}", cmd, e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        log.info("-----------------end shell cmd: {} state={}-----------------", StringUtils.join(cmd, " "), result);
        return result;
    }
}
