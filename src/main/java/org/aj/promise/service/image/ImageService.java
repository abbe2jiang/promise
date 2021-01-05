package org.aj.promise.service.image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.imageio.ImageIO;

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
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ImageService {
  public static final String IMAGE_SIGN = "image/";
  private final String imageHost;

  private static DateFormat dateFormat = new SimpleDateFormat("MMddYYYY");

  @Autowired
  private StorageService storageService;

  @Autowired
  public ImageService(ImageProperties properties) {
    String host = properties.getHost();
    if (!host.endsWith("/")) {
      host += "/";
    }
    imageHost = host;
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
      log.info("upload iamge:{}", fileName);
      String day = dateFormat.format(new Date());
      Path path = storageService.store(file, Paths.get(IMAGE_SIGN, day), user.getId() + fileName);
      return imageHost + path.toString();
    } catch (StorageException e) {
      log.error("upload image failed", e);
    }
    return null;
  }

  public Resource loadImage(String path, String filename) {
    try {
      return storageService.load(Paths.get(IMAGE_SIGN, path, filename));
    } catch (StorageFileNotFoundException e) {
      log.error("loadImage image failed path={},filename={}", path, filename);
    }
    return null;
  }

  public String videoThumbnail(String url) {
    try {
      String[] items = url.split(IMAGE_SIGN);
      if (items.length != 2) {
        return null;
      }
      String host = items[0];
      String relativeSrcFile = Paths.get(IMAGE_SIGN, items[1]).toString();
      int index = relativeSrcFile.lastIndexOf(".");
      String relativeDestFile =  relativeSrcFile.substring(0, index) + "-video.jpg";

      String absoluteSrcFile = storageService.getPath(Paths.get(relativeSrcFile)).toString();
      String absoluteDestFile = storageService.getPath(Paths.get(relativeDestFile)).toString();

      FFmpegFrameGrabber fFmpegFrameGrabber = new FFmpegFrameGrabber(absoluteSrcFile);
      fFmpegFrameGrabber.start();

      Frame frame = null;
      int frameLen = fFmpegFrameGrabber.getLengthInFrames();
      for (int i = 0; i <= 5 && i < frameLen; i++) {
        frame = fFmpegFrameGrabber.grabImage();
      }
      if (frame != null) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bufferedImage = converter.getBufferedImage(frame);
        File outPut = new File(absoluteDestFile);
        ImageIO.write(bufferedImage, "jpg", outPut);
      }
      fFmpegFrameGrabber.stop();
      fFmpegFrameGrabber.close();
      return host + relativeDestFile;
    } catch (Exception e) {
      log.error("loadImage video failed", e);
      return null;
    }
  }

  public boolean isVideo(String url) {
    int i = url.lastIndexOf(".");
    return i > 0 && Objects.equals(".mp4", url.substring(i, url.length()));
  }

  public String thumbnail(String url, double scale) {
    try {
      String[] items = url.split(IMAGE_SIGN);
      if (items.length == 2) {
        String host = items[0];

        String relativeSrcFile = Paths.get(IMAGE_SIGN, items[1]).toString();
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
        return host + relativeDestFile;
      }
    } catch (IOException e) {
      log.error("loadImage image failed", e);
    }
    return null;
  }

  @AllArgsConstructor
  public static class Dimension {
    public int width;
    public int height;
  }

  private String getDestFile(String srcFile) {
    int i = srcFile.lastIndexOf(".");
    return srcFile.substring(0, i) + "-thumbnail" + srcFile.substring(i, srcFile.length());
  }

}
