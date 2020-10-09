package org.aj.we.service.storage;

import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  Path store(MultipartFile file, Path path, String fileName)
      throws StorageException;

  Resource load(Path path) throws StorageFileNotFoundException;

  Path getPath(Path path);
}
