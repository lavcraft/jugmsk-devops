package ru.jpoint.dvps.mesos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tolkv
 * @since 20/04/16
 */
@Slf4j
@RestController
public class BuildTaskController {

  @Autowired
  BuildService buildService;

  @RequestMapping("/tasks")
  public ResponseEntity tasks(@RequestParam String repoUrl) {

    buildService.add(repoUrl);

    return ResponseEntity.ok().build();
  }
}
