package telran.bluewhite.parking_event.repository;

import org.springframework.data.repository.CrudRepository;

import telran.bluewhite.parking_event.entity.ParkingEvent;

public interface ParkingEventRepository extends CrudRepository<ParkingEvent, Integer> {

	default Integer deleteByParkingId(Integer parkingId) {
		this.deleteById(parkingId);
		return parkingId;
	}
}