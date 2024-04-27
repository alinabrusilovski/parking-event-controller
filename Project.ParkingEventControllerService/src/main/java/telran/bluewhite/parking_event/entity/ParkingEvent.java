package telran.bluewhite.parking_event.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;
import lombok.Setter;

@RedisHash("ParkingEvent")
@Getter
@Setter
public class ParkingEvent {
    @Id
    private Integer parkingId;
    private String carRegNumber;
    private Long timestamp;

    public ParkingEvent(Integer parkingId, String carRegNumber, Long timestamp) {
        super();
    	this.parkingId = parkingId;
        this.carRegNumber = carRegNumber;
        this.timestamp = timestamp;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parkingId == null) ? 0 : parkingId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParkingEvent other = (ParkingEvent) obj;
		if (parkingId == null) {
			if (other.parkingId != null)
				return false;
		} else if (!parkingId.equals(other.parkingId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ParkingEvent [parkingId=" + parkingId + ", carRegNumber=" + carRegNumber + ", timestamp=" + timestamp
				+ "]";
	}

	

    
}