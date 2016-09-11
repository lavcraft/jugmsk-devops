package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.PortMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * @author tolkv
 * @since 20/04/16
 */
@Slf4j
@Component
public class TaskInfoGradleBuildDocker implements TaskInfoFactory {

  @Value("${mesos.docker.image}")
  protected String dockerImage;

  @Value("${spring.application.name}")
  protected String applicationName;

  @Value("${mesos.docker.network:BRIDGE}")
  protected String networkMode; // May be BRIDGE or HOST

  @Autowired
  MesosConfigProperties mesosConfig;

  public Protos.TaskInfo createWithCommand(String taskId,
                                           Protos.Offer offer,
                                           List<Protos.Resource> resources,
                                           String cmd,
                                           ExecutionParameters executionParameters) {
    List<PortMapping> portMappings = executionParameters.getPortMappings();

    return Protos.TaskInfo.newBuilder().setName(taskId + ".build")
        .setSlaveId(offer.getSlaveId())
        .setTaskId(Protos.TaskID.newBuilder()
            .setValue(taskId))
        .addAllResources(resources)
        .setContainer(Protos.ContainerInfo.newBuilder()
            .setType(Protos.ContainerInfo.Type.DOCKER)
            .setDocker(Protos.ContainerInfo.DockerInfo.newBuilder()
                .setImage(dockerImage)
                .addAllPortMappings(portMappings(portMappings))
                .setNetwork(Protos.ContainerInfo.DockerInfo.Network.BRIDGE)))
        .setCommand(createCommand(cmd))
        .build();
  }

  private Iterable<? extends Protos.ContainerInfo.DockerInfo.PortMapping> portMappings(List<PortMapping> portMappings) {
    if (networkMode.equalsIgnoreCase("BRIDGE")) {
      return portMappings.stream()
          .map(portMapping -> Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder()
              .setHostPort(portMapping.getOfferedPort())
              .setContainerPort(portMapping.getContainerPort().orElseThrow(() -> new IllegalArgumentException("No container port specified for " + portMapping.getName())))
              .build())

          .collect(Collectors.toList());
    } else {
      return emptyList();
    }
  }

  private Protos.CommandInfo createCommand(String cmd) {
    Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder();

    Optional<String> command = Optional.ofNullable(cmd);

    builder.setShell(command.isPresent());
    command.ifPresent(
        (value) -> builder.setValue("git clone " + value + " project && cd project && ls -la && ./gradlew build aP")
    );

    this.mesosConfig.getEnvironment().entrySet().stream()
        .map((kv) -> Protos.Environment.Variable.newBuilder()
            .setName(kv.getKey())
            .setValue(kv.getValue())
            .build())
        .collect(
            Collectors.collectingAndThen(
                Collectors.toList(), (variables) -> builder.setEnvironment(
                    Protos.Environment.newBuilder().addAllVariables(variables)
                )
            )
        );
    return builder.build();
  }
//
//  private Iterable<? extends Protos.ContainerInfo.DockerInfo.PortMapping> portMappings(List<Protos.Resource> resources) {
//    Iterator portsIterator = this.mesosConfig.getResources().getPort().iterator();
//
//    return resources.stream()
//        .filter(Protos.Resource::hasRanges)
//        .filter((resource) -> resource.getName().equals("ports"))
//        .flatMap((resource) -> resource.getRanges().getRangeList().stream())
//        .flatMapToLong((range) -> LongStream.rangeClosed(range.getBegin(), range.getEnd()))
//        .limit((long) this.mesosConfig.getResources().getPort().size())
//        .mapToObj((hostPort) -> Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder()
//            .setHostPort((int) hostPort)
//            .setContainerPort(Integer.parseInt((String) portsIterator.next())).build())
//        .peek((portMapping) -> log.debug("Mapped host=" + portMapping.getHostPort() + "=>" + portMapping.getContainerPort())).collect(Collectors.toList());
//  }

  @Override
  public Protos.TaskInfo create(String taskId, Protos.Offer offer, List<Protos.Resource> resources, ExecutionParameters executionParameters) {
    return null;
  }
}
