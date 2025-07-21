package stryckyzzzComponents.StryckyzzzClasses;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import StryckyzzzInterfaces.Reloadable;

public class StryckyzzzTextAreas implements Reloadable {

	private HashMap<Component, List<StryckyzzzTextArea>> staMap;

	private List<StryckyzzzTextArea> staList;

	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * Instantiates the maps and array needed to :
	 * <p>
	 * know where each StryckyzzzTextArea is connected to what menu and all StryckyzzzTextArea in one Array
	 */
	public StryckyzzzTextAreas() {
		staMap = new HashMap<Component, List<StryckyzzzTextArea>>();
		staList = new ArrayList<StryckyzzzTextArea>();
	}
	
	/**
	 * Adds in a thread safe way the parameters to the map and the list as logic dictates
	 * @param c
	 * @param sta
	 */
	public synchronized void addComponentLinked(Component c, StryckyzzzTextArea sta) {
		if (c == null || sta == null) return;

		staList.add(sta);

		staMap.computeIfAbsent(c, k -> new ArrayList<>()).add(sta);
	}

	public synchronized void add(StryckyzzzTextArea sta) {
		if (sta == null) return;
		staList.add(sta);
	}

	/**
	 * Reload all StryckyzzzTextArea to their new language
	 * <p>
	 * MultiThreaded
	 */
	@Override
	public void reload() {
		synchronized (staList) {
			for (StryckyzzzTextArea sta : staList) {
				executor.execute(sta::reload);
			}
		}
	}
	
	/**
	 * Only reload StryckyzzzTextArea to their new language in one component, usefull to show use multiple languages in the app
	 * <p>
	 * MultiThreaded
	 */
	@Override
	public void reloadMapped(Component c) {
		if (c == null) return;

		List<StryckyzzzTextArea> mapped;
		synchronized (staMap) {
			mapped = staMap.get(c);
			if (mapped == null) return;
			mapped = new ArrayList<>(mapped);
		}

		for (StryckyzzzTextArea sta : mapped) {
			executor.execute(sta::reload);
		}
	}
	
}
