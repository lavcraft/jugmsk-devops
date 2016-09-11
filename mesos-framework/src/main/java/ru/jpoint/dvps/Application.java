package ru.jpoint.dvps;

import com.containersolutions.mesos.config.annotation.EnableMesosScheduler;
import com.containersolutions.mesos.scheduler.GradleBuildScheduler;
import com.containersolutions.mesos.scheduler.TaskInfoGradleBuildDocker;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Scheduler;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;


/**
 * @author tolkv
 * @since 20/04/16
 */
@Slf4j
@EnableMesosScheduler
@SpringBootApplication
@EnableAutoConfiguration
public class Application {
  @Bean
  public Scheduler scheduler() {
    return new GradleBuildScheduler();
  }

  @Bean
  @Primary
  public TaskInfoGradleBuildDocker taskInfoGradleBuildDocker(){
    return new TaskInfoGradleBuildDocker();
  }
  @PostConstruct
  public void init() {
//    new TaskInfoFactoryDocker();
//    taskInfoFactoryDocker.create("superBuildTask", );
  }

  public static void main(String[] args) {
    new SpringApplicationBuilder(Application.class)
        .run(args);
  }
}
