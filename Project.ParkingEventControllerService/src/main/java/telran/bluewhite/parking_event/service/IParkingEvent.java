package telran.bluewhite.parking_event.service;

import java.util.NoSuchElementException;

import telran.bluewhite.parking_event.dto.ParkingEventDto;

public interface IParkingEvent {

	ParkingEventDto processParkingEvent(ParkingEventDto parkingEventDto) throws NoSuchElementException;

}
