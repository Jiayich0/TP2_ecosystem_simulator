package simulator.model.region;

import java.util.List;

import simulator.model.JSONable;
import simulator.model.animal.AnimalInfo;

public interface RegionInfo extends JSONable {
	public List<AnimalInfo> getAnimalsInfo();
}