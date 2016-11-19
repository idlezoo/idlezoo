package idlezoo.game.services.inmemory;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("default")
public class Storage {

	private final ConcurrentHashMap<String, InMemoryZoo> zoos = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Integer, InMemoryZoo> zooIds = new ConcurrentHashMap<>();

	private Integer waitingFighter;

	public ConcurrentHashMap<String, InMemoryZoo> getZoos() {
		return zoos;
	}

	public ConcurrentHashMap<Integer, InMemoryZoo> getZooIds() {
		return zooIds;
	}

	public InMemoryZoo getZoo(String name) {
		return zoos.get(name);
	}

	public InMemoryZoo getZoo(Integer id) {
		return zooIds.get(id);
	}

	public Integer getWaitingFighter() {
		return waitingFighter;
	}

	public void setWaitingFighter(Integer waitingFighter) {
		this.waitingFighter = waitingFighter;
	}

	public void clear() {
		zooIds.clear();
		zoos.clear();
		waitingFighter = null;
	}

}
