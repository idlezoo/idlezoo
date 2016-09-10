package idlemage.game.services;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import idlemage.game.domain.Mage;

@Service
public class GameService {

	@Autowired
	private ResourcesService gameResources;
	
	private final ConcurrentHashMap<String, Mage> mages = new ConcurrentHashMap<>();

	public void createMage(String username) {
		mages.put(username, new Mage(gameResources));
	}

	public Mage getMage(String username) {
		return mages.get(username);
	}

	public ConcurrentHashMap<String, Mage> getMages() {
		return mages;
	}

}
