package telran.bluewhite.parking_event.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

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

	@SuppressWarnings("null")
	public ParkingEventDto processParkingEvent(ParkingEventDto parkingEventDto) throws NoSuchElementException {

		if (parkingEventDto == null) {
			log.debug("Parking event with ID {} is null", parkingEventDto.parkingId());
			throw new IllegalArgumentException("Parking event DTO cannot be null");
		}

		// !existingEvent.isPresent()
		Optional<ParkingEvent> existingEvent = repository.findById(parkingEventDto.parkingId());
		if (!existingEvent.isPresent()) {

			if (parkingEventDto.carRegNumber() != null) {

				ParkingEvent event = new ParkingEvent(parkingEventDto.parkingId(), parkingEventDto.carRegNumber(),
						parkingEventDto.timestamp());
				repository.save(event);
				log.debug("Parking event with ID {} was successfully saved", event.getParkingId());

//				return parkingEventDto;
			} else
				return null;

		} else if (parkingEventDto.carRegNumber() == null) {
			log.debug("Deleting parking event with ID: {}", parkingEventDto.parkingId());

			repository.deleteByParkingId(existingEvent.get().getParkingId());

			log.debug("Parking event with ID {} was successfully deleted", existingEvent.get().getParkingId());
//			return parkingEventDto;
		} 
//		else {
//
			return parkingEventDto;
//
//		}

	}

}
