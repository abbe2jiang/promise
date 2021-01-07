package org.aj.promise.repository;


import org.aj.promise.domain.Video;
import org.aj.promise.domain.Video.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoMongoRepository extends MongoRepository<Video, String> {
  Page<Video> findAllByState(State state,Pageable pageable);
}
