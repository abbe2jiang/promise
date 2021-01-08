package org.aj.promise.service.image;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import java.awt.image.BufferedImage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import org.aj.promise.domain.Author;
import org.aj.promise.properties.ImageProperties;
import org.aj.promise.service.storage.StorageException;
import org.aj.promise.service.storage.StorageFileNotFoundException;
import org.aj.promise.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ImageService {
  public static final String IMAGE_SIGN = "image/";
  private String TEMP_IMAGE_DIR = "/images/temp/";
  private static Random random = new Random();

  private final String imageHost;

  private static DateFormat dateFormat = new SimpleDateFormat("MMddYYYY");

  @Autowired
  private StorageService storageService;

  @Autowired
  private VideoService videoService;

  @Autowired
  public ImageService(ImageProperties properties) {
    String host = properties.getHost();
    if (!host.endsWith("/")) {
      host += "/";
    }
    imageHost = host;
  }

  public String getDefaultProfileImageUrl() {
    int num = random.nextInt(20);
    String url = Paths.get(TEMP_IMAGE_DIR, num + ".jpeg").toString();
    // String url = String.format("/image/temp/%d.jpeg", num);
    return url;
  }

  public String uploadImage(MultipartFile file, Author user) {
    try {
      if (file.isEmpty()) {
        return null;
      }
      String fileName = StringUtils.cleanPath(file.getOriginalFilename());

      if (fileName.contains("..")) {
        return null;
      }

      String day = dateFormat.format(new Date());
      Path path = Paths.get(IMAGE_SIGN, day, user.getId() + fileName);
      if (exists(path)) {
        log.info("uploading file exist:{}", path.toString());
        return imageHost + path.toString();
      }
      log.info("uploading file:{}", path.toString());
      path = storageService.store(file, Paths.get(IMAGE_SIGN, day), user.getId() + fileName);
      if (isVideo(path.toString())) {
        videoService.addVideo(path.toString());
      }
      return imageHost + path.toString();
    } catch (StorageException e) {
      log.error("upload image failed", e);
    }
    return null;
  }

  public boolean exists(Path path) {
    return storageService.exists(path);
  }

  public Resource loadImage(String path, String filename) {
    try {
      return storageService.load(Paths.get(IMAGE_SIGN, path, filename));
    } catch (StorageFileNotFoundException e) {
      log.error("loadImage image failed path={},filename={}", path, filename);
    }
    return null;
  }

  public String getVideoPoster(String url) {
    String posterUrl = videoService.getVideoPosterUrl(url);
    if (!storageService.exists(urlToPath(posterUrl))) {
      posterUrl = getDefaultProfileImageUrl();
    }
    return posterUrl;
  }

  public Resource loadImage(String path) {
    try {
      return storageService.load(Paths.get(path));
    } catch (StorageFileNotFoundException e) {
      log.error("loadImage image failed path={}", path, path);
    }
    return null;
  }

  public boolean isVideo(String url) {
    int i = url.lastIndexOf(".");
    if (i < 0) {
      return false;
    }
    String suffix = url.substring(i, url.length()).toLowerCase();
    return Objects.equals(".mp4", suffix) || Objects.equals(".mov", suffix);
  }

  public Path urlToPath(String url) {
    String[] items = url.split(IMAGE_SIGN);
    Path path = Paths.get(url);
    if (items.length == 2) {
      path = Paths.get(IMAGE_SIGN, items[1]);
    }
    return path;
  }

  public String thumbnail(String url, double scale) throws IOException {
    if (url.indexOf(TEMP_IMAGE_DIR) > -1) {
      int index = url.lastIndexOf('.');
      return url.substring(0, index) + "-thumbnail" + url.substring(index);
    }

    String relativeSrcFile = urlToPath(url).toString();
    String relativeDestFile = getDestFile(relativeSrcFile);

    String absoluteSrcFile = storageService.getPath(Paths.get(relativeSrcFile)).toString();
    String absoluteDestFile = storageService.getPath(Paths.get(relativeDestFile)).toString();

    // 避免 iOS 旋转
    BufferedImage image = Thumbnails.of(absoluteSrcFile).useExifOrientation(true).scale(1).asBufferedImage();

    int width = 350;
    int height = 220;
    double rate = (double) height / width;
    int rWidth = image.getWidth();
    int rHeight = image.getHeight();
    if ((double) rHeight / rWidth > rate) { // 太高
      rHeight = (int) (rWidth * rate);
    } else { // 太宽
      rWidth = (int) (rHeight / rate);
    }
    Thumbnails.of(image).sourceRegion(Positions.CENTER, rWidth, rHeight).forceSize(width, height)
        .toFile(absoluteDestFile);
    return imageHost + relativeDestFile;

  }

  @AllArgsConstructor
  public static class Dimension {
    public int width;
    public int height;
  }

  private String getDestFile(String srcFile) {
    int i = srcFile.lastIndexOf(".");
    return srcFile.substring(0, i) + "_thumbnail" + srcFile.substring(i, srcFile.length());
  }

}
