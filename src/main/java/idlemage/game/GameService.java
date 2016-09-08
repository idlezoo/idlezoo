package idlemage.game;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class GameService {

	private final ConcurrentHashMap<String, Mage> mages = new ConcurrentHashMap<>();

	public void createMage(String username) {
		mages.put(username, new Mage());
	}

	public Mage getMage(String username) {
		return mages.get(username);
	}

	public ConcurrentHashMap<String, Mage> getMages() {
		return mages;
	}

}
