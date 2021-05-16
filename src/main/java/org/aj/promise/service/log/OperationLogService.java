package org.aj.promise.service.log;

import java.util.Date;
import java.util.function.Supplier;

import org.aj.promise.domain.Author;
import org.aj.promise.domain.Blog;
import org.aj.promise.domain.OperationLog;
import org.aj.promise.repository.OperationLogMongoRepository;
import org.aj.promise.service.author.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OperationLogService {

  @Autowired
  AuthorService authorService;

  @Autowired
  OperationLogMongoRepository logMongoRepository;

  private void addLog(Supplier<OperationLog> fun) {
    try {
      OperationLog olog = fun.get();
      logMongoRepository.save(olog);
      log.info("addLog log={}", olog);
    } catch (Exception e) {
      log.error("log error", e);
    }
  }

  public void addLoginLog(Author user) {
    addLog(() -> OperationLog.of(OperationLog.Type.LOGIN, user.getId()));
  }

  public void addReadLog(Author user, Blog blog) {
    addLog(() -> OperationLog.of(OperationLog.Type.READ, user.getId(), blog.getId()));
  }

}
