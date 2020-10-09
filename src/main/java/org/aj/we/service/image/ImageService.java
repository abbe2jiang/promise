package org.aj.we.service.image;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
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

  @Autowired private StorageService storageService;

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
      log.info("upload iamge:{}",fileName);
      String day = dateFormat.format(new Date());
      Path path = storageService.store(file, Paths.get(IMAGE_SIGN, day),
                                       user.getId() + fileName);
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

        String absoluteSrcFile =
            storageService.getPath(Paths.get(relativeSrcFile)).toString();
        String absoluteDestFile =
            storageService.getPath(Paths.get(relativeDestFile)).toString();
        Thumbnails.of(absoluteSrcFile).size(350, 220).toFile(absoluteDestFile);
        return host + relativeDestFile;
      }
    } catch (IOException e) {
      log.error("loadImage image failed", e);
    }
    return null;
  }

  private String getDestFile(String srcFile) {
    int i = srcFile.lastIndexOf(".");
    return srcFile.substring(0, i) + "-thumbnail" +
        srcFile.substring(i, srcFile.length());
  }
}
