package telran.bluewhite.parking_event.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;

import telran.bluewhite.parking_event.entity.ParkingEvent;
import telran.bluewhite.parking_event.repository.ParkingEventRepository;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import telran.bluewhite.parking_event.dto.ParkingEventDto;

@Service
@Slf4j
public class ParkingEventImpl implements IParkingEvent {

	@Autowired
	ParkingEventRepository repository;

	private ParkingEventDto previousEventDto;

	private ApplicationContext applicationContext;

	public ParkingEventImpl(ParkingEventRepository repository, ApplicationContext applicationContext) {
		this.repository = repository;
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings("null")
	public ParkingEventDto processParkingEvent(ParkingEventDto parkingEventDto) throws NoSuchElementException {

		if (parkingEventDto == null) {
			log.debug("Parking event with ID {} is null", parkingEventDto.parkingId());
			throw new IllegalArgumentException("Parking event DTO cannot be null");
		}

		StreamBridge sb = applicationContext.getBean(StreamBridge.class);
		String bindingName = applicationContext.getEnvironment().getProperty("app.event.producer.binding.name",
				"processParkingEvent-out-0");
		if (previousEventDto != null && parkingEventDto.carRegNumber().equals(previousEventDto.carRegNumber())) {
			log.debug("Car registration number {} is the same as the previous event. Sending back to the broker.",
					parkingEventDto.carRegNumber());
			sb.send(bindingName, parkingEventDto);
			log.trace("Event was sended to broker");
			return parkingEventDto;
		}

		if (parkingEventDto.carRegNumber() == null) {
			log.debug("Deleting parking event with ID: {}", parkingEventDto.parkingId());
			Optional<ParkingEvent> existingEvent = repository.findById(parkingEventDto.parkingId());
			if (existingEvent.isPresent()) {

				repository.deleteByParkingId(existingEvent.get().getParkingId());

				log.debug("Parking event with ID {} was successfully deleted", existingEvent.get().getParkingId());
				return parkingEventDto;
			} else {
				log.debug("Parking event with ID {} was not found for deletion", parkingEventDto.parkingId());
				throw new NoSuchElementException(
						"Record with parkingId " + parkingEventDto.parkingId() + " does not exist");
			}
		}

		ParkingEvent event = new ParkingEvent(parkingEventDto.parkingId(), parkingEventDto.carRegNumber(),
				parkingEventDto.timestamp());
		log.debug("New parking event was created: {}", event);
		repository.save(event);
		log.debug("Parking event with ID {} was successfully saved", event.getParkingId());

		return new ParkingEventDto(event.getParkingId(), event.getCarRegNumber(), event.getTimestamp());
	}
}
