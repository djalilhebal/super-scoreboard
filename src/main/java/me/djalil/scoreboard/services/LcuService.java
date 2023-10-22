package me.djalil.scoreboard.services;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import me.djalil.scoreboard.model.LightGame;
import no.stelar7.api.r4j.basic.utils.Pair;
import no.stelar7.api.r4j.impl.lol.lcu.LCUApi;
import no.stelar7.api.r4j.impl.lol.lcu.LCUConnection;
import no.stelar7.api.r4j.impl.lol.lcu.SimpleSSLContext;

public class LcuService implements IGameProvider {
	private static final Logger LOG = Logger.getLogger(LcuService.class.getName());
	private final static Gson gson = new Gson();

	static final String GAMEFLOW_SESSION_URI = "/lol-gameflow/v1/session";

	/**
	 * Get the current running game from the Gameflow Session endpoint. Null if
	 * there is no running client, no running game, or an error occurs.
	 */
	@Override
	public LightGame getLiveGame() {
		try {
			var httpPayload = fetchLcu(GAMEFLOW_SESSION_URI);
			var lcuSessionGame = gson.fromJson(httpPayload, LcuGameflowSession.class);
			return lcuSessionGame == null ? null : lcuSessionGame.toLight();
		} catch (Exception ex) {
			//ex.printStackTrace();
			return null;
		}
	}

	@Override
	public LightGame getLiveGame(String json) {
		var lcuSessionGame = gson.fromJson(json, LcuGameflowSession.class);
		var game = lcuSessionGame.toLight();
		return game;
	}

	// ---

	public void onNewGame(Consumer<LightGame> listener) {
		listeners.add(listener);
	}

	void fireNewGame(LightGame game) {
		listeners.forEach(l -> l.accept(game));
	}

	private Set<Consumer<LightGame>> listeners = new HashSet<>();

	private void createWebsocket(String password, String port) {
		try {
			String url = String.format("wss://localhost:%s/", port);
			WebSocketFactory factory = new WebSocketFactory();
			factory.setSSLContext(SimpleSSLContext.getInstance("TLS"));
			factory.setVerifyHostname(false);

			ws = factory.createSocket(url);
			ws.setUserInfo("riot", password);
			ws.addListener(new WebSocketAdapter() {
				@Override
				public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
					LOG.info("Connected");
					subscribe("OnJsonApiEvent");
				}

				@Override
				public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
					LOG.log(Level.WARNING, "WS connect error");
					notConnected.signalAll();
					LOG.info("notConnected.signalAll()");
				}

				@Override
				public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
						WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
					LOG.log(Level.WARNING, "WS disconnected");
					notConnected.signalAll();
					LOG.info("notConnected.signalAll()");
				}

				@Override
				public void onTextMessage(WebSocket websocket, String text) {
					LOG.fine("onTextMessage");
					
					JsonElement elem = new JsonParser().parse(text);
					JsonArray arr = elem.getAsJsonArray();

					String id = arr.get(0).toString();
					String eventName = arr.get(1).getAsString();
					JsonObject payload = arr.get(2).getAsJsonObject();
					String data = payload.get("data").toString();
					String uri = payload.get("uri").getAsString();
					
					handleJsonApiEvent(uri, data);
				}
			}); // END WebSocketAdapter

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	void handleJsonApiEvent(String uri, String payload) {
		LOG.log(Level.FINE, "handleJsonApiEvent");
		LOG.log(Level.FINE, String.format("Event(\n\turi=%s \tpayload=%s\n)", uri, payload));

		if (uri.equals(GAMEFLOW_SESSION_URI)) {
			LOG.entering(LcuService.class.getName(), "handleGameflowSession");
			handleGameflowSession(payload);
			LOG.exiting(LcuService.class.getName(), "handleGameflowSession");
		}

	}
	
	private LcuGameflowSession prevSession = null;

	/**
	 * Data is obtained from either an HTTP call or WS event.
	 */
	void handleGameflowSession(String payload) {
		var session = gson.fromJson(payload, LcuGameflowSession.class);
		LOG.fine("handleGameflowSession parsed: " + gson.toJson(session));

		if (session.gameData == null || !LcuGameflowSession.Phase.InProgress.toString().equals(session.phase)) {
			return;
		}
		
		if (prevSession == null || prevSession.gameData.gameId != session.gameData.gameId) {
			LOG.info(String.format("new gameflow session: id=%d phase=%s", session.gameData.gameId, session.phase));
			fireNewGame(session.toLight());
			prevSession = session;
		}
	}

	// WebSocket stuff
	final Lock lock = new ReentrantLock();
	final Condition notConnected = lock.newCondition();

	private WebSocket ws;

	public void subscribe(String eventName) {
		int code = 5;
		ws.sendText(String.format("[%s, \"%s\"]", code, eventName));
	}

	Thread reconnectingThread;
	/**
	 * Defaults to a static 5s.
	 */
	long retryTimeout = 5000;

	/**
	 * It should <ol>
	 * 	 <li> create a reconnecting WebSocket
	 *   <li> and fetch the initial session.
	 * </ol>
	 */
	public void init() {
		reconnectingThread = new Thread() {
			@Override
			public void run() {
				while (!this.isInterrupted()) {
					try {
						lock.lock();
						while (ws != null && ws.getState() != WebSocketState.CLOSED) {
							notConnected.await();
						}
						lock.unlock();
						tryConnect();
						Thread.sleep(retryTimeout);
					} catch (InterruptedException e) {
					}
				}

			}
		};
		reconnectingThread.start();

		handleInitialSession();
		tryConnect();
	}

	// Get the initial session.
	// - There may be an in-progress session (already running).
	// - There may be no active session. Meaning, the client is open, but nothing is
	// happening (phase = "None").
	// - The client may not be running at all, so the call will fail.
	void handleInitialSession() {
		LOG.info("Handle initial session");
		var initialPayload = fetchLcu(GAMEFLOW_SESSION_URI);
		//LOG.info("initial payload " + initialPayload);
		handleGameflowSession(initialPayload);
	}

	void tryConnect() {
		var pair = LCUConnection.getConnectionData();
		if (pair != null) {
			var password = pair.getKey();
			var port = pair.getValue();
			createWebsocket(password, port);
			ws.connectAsynchronously();
		} else {
			LOG.log(Level.INFO, "No connection data. Should retry later");
			lock.lock();
			notConnected.signalAll();
			lock.unlock();
		}
	}

	@Override
	public List<String> getPrincipal() {
		try {
			return getPrincipal1();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Using "/lol-summoner/v1/current-summoner" 
	 * @return {region, summonerName} tuple
	 */
	public List<String> getPrincipal1() {
		String url = "/lol-summoner/v1/current-summoner";
		var json = fetchLcu(url);
		var parsed = gson.fromJson(json, JsonElement.class).getAsJsonObject();

		// XXX: What's the diff between `displayName`, `gameName`, and `internalName`?
		// They seem to contain the same value (including the letter case).
		var summonerName = parsed.get("gameName").getAsString();

		// XXX: `tagLine` ("EUW") is the same as region, no?
		var region = parsed.get("tagLine").getAsString().toLowerCase();

		return List.of(region, summonerName);
	}

	/**
	 * Using "/lol-chat/v1/me"
	* @return {region, summonerName}
	 */
	public List<String> getPrincipal2() {
		String url = "/lol-chat/v1/me";
		var json = fetchLcu(url);
		var parsed = gson.fromJson(json, JsonElement.class).getAsJsonObject();

		// XXX: `gameName` vs `name`?
		var summonerName = parsed.get("gameName").getAsString();

		// XXX: `gameTag` ("EUW") vs `platformId` ("EUW1")?
		var region = parsed.get("gameTag").getAsString().toLowerCase();

		return List.of(region, summonerName);
	}

	/**
	 * Using "/riotclient/region-locale"
	 * @return {region, ""}
	 */
	public List<String> getPrincipal3() {
		String url = "/riotclient/region-locale";
		var json = fetchLcu(url);
		var parsed = gson.fromJson(json, JsonElement.class).getAsJsonObject();

		String summonerName = "";
		// XXX: What's the diff between `region` and `webRegion`? Only the letter case?
		String region = parsed.get("region").getAsString().toLowerCase();

		return List.of(region, summonerName);
	}

	// ---
	
	/**
	 * Normalizes the URL
	 * @param url
	 * @return body as string
	 */
	static String fetchLcu(String url) {
		while (url.startsWith("/")) {
			url = url.substring(1);
		}
		
		var customRes = LCUApi.customUrl(url, null, "GET");
		if (customRes instanceof String) {
			return (String)customRes;
		} else if (customRes instanceof Pair) {
			return (String) ((Pair)customRes).getValue();
		} else {
			LOG.severe("Unexpected res: " + customRes);
			return null;
		}
	}

}
