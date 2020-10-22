package org.aj.we.service.image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Coordinate;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.util.BufferedImages;
import net.coobird.thumbnailator.Thumbnails.Builder;

import org.aj.we.domain.Author;
import org.aj.we.properties.ImageProperties;
import org.aj.we.service.storage.StorageException;
import org.aj.we.service.storage.StorageFileNotFoundException;
import org.aj.we.service.storage.StorageService;
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

  public String thumbnail(String url, double scale) {
    try {
      String[] items = url.split(IMAGE_SIGN);
      if (items.length == 2) {
        String host = items[0];

        String relativeSrcFile = Paths.get(IMAGE_SIGN, items[1]).toString();
        String relativeDestFile = getDestFile(relativeSrcFile);

        String absoluteSrcFile = storageService.getPath(Paths.get(relativeSrcFile)).toString();
        String absoluteDestFile = storageService.getPath(Paths.get(relativeDestFile)).toString();

        BufferedImage image = ImageIO.read(new File(absoluteSrcFile));
        int width = 350;
        int height = 220;
        double rate = (double) height / width;
        Builder<File> builder = Thumbnails.of(absoluteSrcFile);
        int rWidth = image.getWidth();
        int rHeight = image.getHeight();
        if ((double) rHeight / rWidth > rate) { // 太高
          rHeight = (int) (rWidth * rate);
        } else { // 太宽
          rWidth = (int) (rHeight / rate);
        }
        builder.sourceRegion(Positions.CENTER, rWidth, rHeight).forceSize(width, height).toFile(absoluteDestFile);
        return host + relativeDestFile;
      }
    } catch (IOException e) {
      log.error("loadImage image failed", e);
    }
    return null;
  }

  private String getDestFile(String srcFile) {
    int i = srcFile.lastIndexOf(".");
    return srcFile.substring(0, i) + "-thumbnail" + srcFile.substring(i, srcFile.length());
  }
}
