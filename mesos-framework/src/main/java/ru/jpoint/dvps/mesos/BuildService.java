package ru.jpoint.dvps.mesos;

import com.containersolutions.mesos.scheduler.TaskInfoGradleBuildDocker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author tolkv
 * @since 20/04/16
 */
@Slf4j
@Component
public class BuildService {
  BlockingQueue<String> urlForBuild;
  @Autowired
  TaskInfoGradleBuildDocker taskInfoGradleBuildDocker;

  public void add(String repoUrl) {
    urlForBuild.add(repoUrl);
  }

  public Optional<String> getFirst() {
    if (urlForBuild.isEmpty()) {
      return Optional.empty();
    }
    String poll = urlForBuild.poll();
    return Optional.of(poll);
  }

  public BuildService() {
    this.urlForBuild = new ArrayBlockingQueue<>(10);
  }
}
