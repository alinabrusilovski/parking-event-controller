package telran.bluewhite.parking_event;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import telran.bluewhite.parking_event.dto.ParkingEventDto;
import telran.bluewhite.parking_event.entity.ParkingEvent;
import telran.bluewhite.parking_event.repository.ParkingEventRepository;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
public class ParkingEventTest {

	private static final Integer PARKING_ID = 111;
	private static final String CAR_REG_NUMBER = "regNumber123";
	private static final Long TIMESTAMP = 10000L;

	@Autowired
	InputDestination producer;
	@Autowired
	OutputDestination consumer;
	
	private String consumerBindingName = "sendParkingData-in-0";
	private String producerBindingName = "processParkingEvent-out-0";

	@MockBean
	ParkingEventRepository repo;

	ParkingEventDto parkingEventDto = new ParkingEventDto(PARKING_ID, CAR_REG_NUMBER, TIMESTAMP);
	ParkingEventDto droveAwayEventDto = new ParkingEventDto(PARKING_ID, null, TIMESTAMP);

	static ParkingEvent parkingEventEntity = new ParkingEvent(PARKING_ID, CAR_REG_NUMBER, TIMESTAMP);
	static ParkingEvent droveAwayEventEntity = new ParkingEvent(PARKING_ID, null, TIMESTAMP);

	// ============map to mock Redis============
	static HashMap<Integer, ParkingEvent> redisMap = new HashMap<>();

	@BeforeAll
	static void setUpAll() {
		redisMap.clear();
	}

	@Test
	void testParkingArrivalEvent() {
		if (!redisMap.containsKey(PARKING_ID)) {
			when(repo.save(parkingEventEntity)).thenAnswer(new Answer<ParkingEvent>() {
				@Override
				public ParkingEvent answer(InvocationOnMock invocation) throws Throwable {
					ParkingEvent event = invocation.getArgument(0);
					redisMap.put(PARKING_ID, event);
					return invocation.getArgument(0);
				}
			});

			producer.send(new GenericMessage<ParkingEventDto>(parkingEventDto), consumerBindingName);
			Message<byte[]> message = consumer.receive(100, producerBindingName);
			assertNotNull(message);

			assertTrue(redisMap.containsKey(PARKING_ID));
			assertEquals(parkingEventEntity, redisMap.get(PARKING_ID));
		} else {
			when(repo.save(parkingEventEntity)).thenReturn(parkingEventEntity);

			producer.send(new GenericMessage<ParkingEventDto>(parkingEventDto), consumerBindingName);
			Message<byte[]> message = consumer.receive(0, producerBindingName);
			assertNotNull(message);

			redisMap.forEach((k, v) -> System.out.println("2FILLED MAP" + k + ":" + v));

			assertTrue(redisMap.containsKey(PARKING_ID));
			assertEquals(CAR_REG_NUMBER, redisMap.get(PARKING_ID).getCarRegNumber());
		}
		redisMap.forEach((k, v) -> System.out.println("3MAP" + k + ":" + v));
	}

	@Test
	void testParkingDepartureEvent() {
		redisMap.put(PARKING_ID, parkingEventEntity);

		when(repo.findById(PARKING_ID)).thenReturn(Optional.of(droveAwayEventEntity));
		when(repo.deleteByParkingId(PARKING_ID)).thenAnswer(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				redisMap.remove(PARKING_ID);
				return invocation.getArgument(0);
			}
		});

		redisMap.forEach((k, v) -> System.out.println("MAP" + k + ":" + v));

		producer.send(new GenericMessage<ParkingEventDto>(droveAwayEventDto), consumerBindingName);
		Message<byte[]> message = consumer.receive(0, producerBindingName);
		assertNotNull(message);

		assertFalse(redisMap.containsKey(PARKING_ID));
		assertNull(redisMap.get(PARKING_ID));

	}

}
