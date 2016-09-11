package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.events.*;
import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import com.containersolutions.mesos.utils.StreamHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import ru.jpoint.dvps.mesos.BuildService;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @author tolkv
 * @since 20/04/16
 */
@Slf4j
@Component
public class GradleBuildScheduler implements Scheduler, ApplicationListener<EmbeddedServletContainerInitializedEvent> {
  @Autowired
  TaskInfoGradleBuildDocker taskInfoGradleBuildDocker;
  @Autowired
  BuildService buildService;

  @Override
  public void registered(SchedulerDriver driver, Protos.FrameworkID frameworkId, Protos.MasterInfo masterInfo) {
    this.frameworkID.set(frameworkId);
    applicationEventPublisher.publishEvent(new FrameworkRegistreredEvent(frameworkId, masterInfo));
  }

  @Override
  public void reregistered(SchedulerDriver driver, Protos.MasterInfo masterInfo) {
    applicationEventPublisher.publishEvent(new FrameworkReregistreredEvent(masterInfo));
  }

  @Override
  public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> offers) {
    AtomicInteger acceptedOffers = new AtomicInteger(0);
    AtomicInteger rejectedOffers = new AtomicInteger(0);
    offers.stream()
        .map(offer -> offerStrategyFilter.evaluate(uuidSupplier.get().toString(), offer))
        .filter(StreamHelper.onNegative(
            OfferEvaluation::isValid,
            offerEvaluation -> schedulerDriver.declineOffer(offerEvaluation.getOffer().getId())))
        .forEach(offerEvaluation -> {
          Optional<String> repositoryUrl = buildService.getFirst();

          if (!repositoryUrl.isPresent()) {
            schedulerDriver.declineOffer(offerEvaluation.getOffer().getId());
          } else {
            String s = repositoryUrl.get();
            String taskId = UUID.randomUUID() + "-build-";

            Protos.TaskInfo taskInfo = taskInfoGradleBuildDocker.createWithCommand(
                taskId,
                offerEvaluation.getOffer(),
                offerEvaluation.getResources(),
                s, new ExecutionParameters(offerEvaluation.getEnvironmentVariables(),
                    offerEvaluation.getPortMappings()
                ));
            schedulerDriver.launchTasks(Collections.singleton(offerEvaluation.getOffer().getId()), Collections.singleton(taskInfo));
            stateRepository.store(taskInfo);
          }
        });
  }

  @Override
  public void offerRescinded(SchedulerDriver driver, Protos.OfferID offerId) {
  }

  @Override
  public void statusUpdate(SchedulerDriver driver, Protos.TaskStatus taskStatus) {
    applicationEventPublisher.publishEvent(new StatusUpdateEvent(taskStatus));
  }

  @Override
  public void frameworkMessage(SchedulerDriver driver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, byte[] data) {
    applicationEventPublisher.publishEvent(new FrameworkMessageEvent(data, executorID, slaveID));
  }

  @Override
  public void disconnected(SchedulerDriver driver) {
  }

  @Override
  public void slaveLost(SchedulerDriver driver, Protos.SlaveID slaveId) {
    applicationEventPublisher.publishEvent(new SlaveLostEvent(slaveId));
  }

  @Override
  public void executorLost(SchedulerDriver driver, Protos.ExecutorID executorId, Protos.SlaveID slaveId, int status) {
    applicationEventPublisher.publishEvent(new ExecutorLostEvent(status, executorId, slaveId));
  }

  @Override
  public void error(SchedulerDriver driver, String message) {
    applicationEventPublisher.publishEvent(new ErrorEvent(message));
  }

  public void killTask(Protos.TaskID taskId) {
    driver.get().killTask(taskId);
  }

  @Override
  public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
    String envHostname = null;
    try {
      envHostname = InetAddress.getByName(address).getCanonicalHostName();
    } catch (UnknownHostException e) {
    }
    Protos.FrameworkInfo.Builder frameworkBuilder = Protos.FrameworkInfo.newBuilder()
        .setName(applicationName)
        .setUser("root")
        .setRole(mesosConfig.getRole())
        .setCheckpoint(true)
        .setHostname(envHostname != null ? envHostname : "localhost")
        .setFailoverTimeout(60.0D)
        .setWebuiUrl("http://" + address + ':' + port)
        .setId(
            stateRepository.getFrameworkID().orElseGet(
                () -> Protos.FrameworkID.newBuilder().setValue("").build())
        );

    MesosSchedulerDriver driver = new MesosSchedulerDriver(
        this,
        frameworkBuilder.build(),
        mesosMaster);
    if (!this.driver.compareAndSet(null, driver)) {
      throw new IllegalStateException("Driver already initialised");
    } else {
      driver.start();
    }

    new Thread(driver::join).start();
  }

  @PreDestroy
  public void stop() throws ExecutionException, InterruptedException {
    driver.get().stop(false);
  }

  @Value("${mesos.master}")
  protected String mesosMaster;
  @Value("${spring.application.name}")
  protected String applicationName;
  @Value("${server.address}")
  protected String address;
  @Value("${server.port}")
  protected int port;
  @Autowired
  MesosConfigProperties mesosConfig;
  @Autowired
  OfferStrategyFilter offerStrategyFilter;
  @Autowired
  ApplicationEventPublisher applicationEventPublisher;
  @Autowired
  Supplier<UUID> uuidSupplier;
  @Autowired
  StateRepository stateRepository;

  private AtomicReference<Protos.FrameworkID> frameworkID = new AtomicReference<>();
  private AtomicReference<SchedulerDriver> driver = new AtomicReference<>();
}