package me.djalil.scoreboard;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Utils {

	public static String readText(String filename) {
		try {
			// NAMELESS
			var folder = "C:\\Users\\Djalil\\Documents\\GitHub\\super-scoreboard\\raw-data\\test-data--euw-6611407126";
			var content = Files.readString(Path.of(folder, filename));
			return content;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	/**
	 * Assert that the field exists.
	 * 
	 * <p>Example
	 * <pre>{@code
	 * var thing = new Thing();
	 * Things.requireExists(thing, "name"); // OK
	 * Things.requireExists(thing, "nom"); // throws Exception
	 * }</pre>
	 * 
	 * <p>
	 * - Inspired by {@link Objects#requireNonNull(Object))
	 * 
	 * @param obj
	 * @param fieldName
	 * @throws IllegalArgumentException if {@code obj.fieldName} does not exist.
	 */
	public static void requireExists(Object obj, String fieldName) {
		try {
			// FIXME: This will fail if the field was desclared in a super class.
			// Maybe just change this method's name to "requireHasOwn"
			// similar to `Object.hasOwn()` in JavaScript.
			obj.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			String className = obj.getClass().getName();
			throw new IllegalArgumentException(String.format("The field '%s' does not exist on class '%s'", fieldName, className));
		}
	}

	/**
	 * Sends a simple HTTP GET request and returns the response as String.
	 * 
	 * - See https://openjdk.org/groups/net/httpclient/intro.html
	 * 
	 * @return String or null if an error occurs.
	 */
	public static String fetch(String url) {
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
			HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
			var body = res.body();
			return body;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// See https://stackoverflow.com/a/53086587
	public static String fetchInsecure(String url) {
	    try {
		    SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());
			
			HttpClient client = HttpClient.newBuilder().sslContext(sslContext).build();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
			HttpResponse<String> res = client.send(request, BodyHandlers.ofString());
			var body = res.body();
			return body;
		} catch (KeyManagementException | NoSuchAlgorithmException | IOException | InterruptedException e) {
			//e.printStackTrace();
		}
	    
		return null;
	}

	private static TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		        public void checkClientTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		        public void checkServerTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    }
		};
}
