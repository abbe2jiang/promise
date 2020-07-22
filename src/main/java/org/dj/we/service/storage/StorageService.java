package org.dj.we.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

    Path store(MultipartFile file, Path path, String fileName) throws StorageException;

    Resource load(Path path) throws StorageFileNotFoundException;

    Path getPath(Path path);

}
