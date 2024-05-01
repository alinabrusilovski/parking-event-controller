package telran.bluewhite.parking_event;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import lombok.extern.slf4j.Slf4j;
import telran.bluewhite.parking_event.dto.ParkingEventDto;
import telran.bluewhite.parking_event.service.IParkingEvent;

@SpringBootApplication
@Slf4j
public class ParkingEventApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingEventApplication.class, args);
	}

	@Autowired
	IParkingEvent service;

	@Autowired
	StreamBridge sb;

	@Value("${app.event.producer.binding.name:sendParkingData-out-0}")
	String bindingName;

	@Bean
	Consumer<ParkingEventDto> processParkingEvent() {
		return (eventDto) -> {
			if (eventDto != null) {
					ParkingEventDto event = service.processParkingEvent(eventDto);
					log.trace("Getting new event from service {}", event);
					sb.send(bindingName, event);
					log.trace("Event was sended to broker");
			} else {
				System.out.println("Received null event");
			}
		};
	}

}