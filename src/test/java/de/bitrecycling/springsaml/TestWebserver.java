package de.bitrecycling.springsaml;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TestWebserver  {

	static final int port = 8989;
	static final String newLine = "\r\n";
	private static final String FETCH_OPTIONS = "{ credentials: 'include' }";
	private static String baseURL = "https://api.test.de";
	private static String checkIsLoggedInUrl = "/authentication/isLoggedIn";
	private static String putUserSettingsUrl = "/user/settings";


	public static void main(String[] args) {
		try {
			ServerSocket socket = new ServerSocket(port);

			while (true) {
				Socket connection = socket.accept();
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					PrintStream output = new PrintStream(new BufferedOutputStream(connection.getOutputStream()));

					handleRequest(input, output);

				} catch (Throwable t) {
					System.err.println("Error handling request: " + t);
				}
			}
		} catch (Throwable t) {
			System.err.println("Could not start server: " + t);
		}
	}

	private static void handleRequest(BufferedReader input, PrintStream output) throws Exception {
		String request = input.readLine();

		if (request == null) {
			return;
		}

		if (request.startsWith("GET")) {
			handleGET(request, output);
		}
		if (request.startsWith("PUT")) {
			handlePUT(request, output);
		}
		if (request.startsWith("POST")) {
			handlePOST(request, output);
		}
		if (request.startsWith("DELETE")) {
			handleDELETE(request, output);
		}
		if (request.startsWith("OPTIONS")) {
			handleOPTIONS(request, output);
		}

		while (true) {

			String ignore = input.readLine();
			//            System.out.println(ignore);
			if (ignore == null || ignore.length() == 0) {
				break;
			}
		}

		output.close();
	}

	private static void handleGET(String request, PrintStream pout) {
		if (request.startsWith("GET / ")) {
			String response = getTestHtmlPage();
			writeResponse(response, "200 OK", "text/html", pout);
		}

		if (request.startsWith("GET /testAuthCookie.js")) {
			String response = getTestJavaScript();
			writeResponse(response, "200 OK", "application/javascript", pout);
		}
	}

	private static void handlePUT(String request, PrintStream pout) {
		if (request.startsWith("PUT / ")) {
			String response = "{\"status\":\"ok\"}";
			writeResponse(response, "200 OK", "application/javascript", pout);
		}
	}

	private static void handlePOST(String request, PrintStream pout) {
		if (request.startsWith("POST / ")) {
			String response = "{\"status\":\"ok\"}";
			writeResponse(response, "200 OK", "application/javascript", pout);
		}
	}

	private static void handleDELETE(String request, PrintStream pout) {
		if (request.startsWith("DELETE / ")) {
			String response = "{\"status\":\"ok\"}";
			writeResponse(response, "200 OK", "application/javascript", pout);
		}
	}

	private static void handleOPTIONS(String request, PrintStream pout) {
		if (request.startsWith("OPTIONS / ")) {
			String response = "{\"status\":\"ok\"}";
			writeResponse(response, "200 OK", "application/javascript", pout);
		}
	}

	private static void writeResponse(String response, String status, String contentType, PrintStream pout) {
		pout.print(
				"HTTP/1.1" + status + newLine +
						"Content-Type: " + contentType + newLine +
						//                                        ORIGIN_HEADER + newLine +
						"Date: " + new Date() + newLine +
						"Content-length: " + response.length() + newLine + newLine +
						response
		);
	}

	private static String getTestHtmlPage() {
		return "<!DOCTYPE html>\n" +
				"<html lang=\"en\">\n" +
				"<head>" +
				"   <meta charset=\"UTF-8\">\n" +
				"    <title>Spring SAML Test Website for Session Cookie</title>\n" +
				"    <style type=\"text/css\" media=\"screen\">\n" +
				"      body{" +
				"          font-family:sans-serif;\n"+
				"      }\n" +
				
				"    </style>\n"+
				"</head>\n" +
				"<body>\n" +
				"<h1>Test if the cookie is transferred correctly from the browser</h1>\n" +
				"<h2>After successful login the check shall respond with 'true'</h2>\n" +
				"<button onclick=\"checkLoginState()\"> Check Login</button>\n" +"Logged-In-State: <b " +
				"id=\"lin\">init</b><br>\n" +
				"<h2>Cookie is 'visible' to JavaScript (HttpOnly is not set) </h2>\n" +
				"Cookie visible: <b id=\"cook\">no</b><br>\n" +
				"\n" +
				
				"<a href=\"#\" onclick=\"putUserSettings()\"> PUT user settings</a>\n" +
				"<form action=\"" + baseURL + "/logout\" method=\"POST\">" +
				"<button type=\"submit\">Logout</button>\n" +
				"</form>\n" +

				"<script src=\"testAuthCookie.js\"></script>\n" +
				"</body>\n" +
				"</html>";
	}

	private static String getTestJavaScript() {

		return "async function checkLoginState() {\n" +
				"    let response = await fetch(\" " +
				baseURL + checkIsLoggedInUrl
				+ "\", " +
				FETCH_OPTIONS +
				" );" +
				"\n" +
				"    if (response.ok) {\n" +
				"        let result = await response.json();\n" +
				"        document.getElementById('lin').textContent = result;\n" +
				"    } else {\n" +
				"        alert(response.status);\n" +
				"    }\n" +
				"    document.getElementById('cook').textContent = document.cookie;\n" +
				"}\n" +
				"\n\n" +
				"async function putUserSettings(url = '" + baseURL + putUserSettingsUrl + "'" +
				", data = {\"settings\":\"{}\"}) {\n" +
				"  const response = await fetch(url, {\n" +
				"    method: 'PUT',\n" +
				"    mode: 'cors', \n" +
				"    cache: 'no-cache', \n" +
				"    credentials: 'include', \n" +
				"    headers: {\n" +
				"      'Content-Type': 'application/json'\n" +
				"    },\n" +
				"    referrerPolicy: 'no-referrer', \n" +
				"    body: JSON.stringify(data) \n" +
				"  });\n" +
				"  return response.json(); \n" +
				"}\n\n";
	}
}
