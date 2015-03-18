import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

/**
 * DOUG BLASE
 * CSE467-S15
 * Java SSL server.
 */

/**
 * A Java ssl echo server that uses a self signed certificate.
 * @author Doug Blase
 *
 */
public class DougServer {

	private SSLServerSocketFactory sslserversocketfactory;
	private SSLServerSocket sslserversocket;

	public DougServer() {
		try {
			sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			sslserversocket = (SSLServerSocket) getServerSocket();
			sslserversocket.setSoTimeout(1000);

		}
		catch (Exception exception) {
			exception.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new DougServer().acceptConnections();
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void acceptConnections() {
		while (true) {
			try {
				new Echoer((SSLSocket) sslserversocket.accept())
						.start();
			}
			catch (SocketTimeoutException se) {
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}			
	}

	public ServerSocket getServerSocket() throws Exception {
		// Make sure that JSSE is available
		Security.addProvider(Security.getProvider("SUN"));
		// A keystore is where keys and certificates are kept
		// Both the keystore and individual private keys should be
		// password protected
		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(new FileInputStream("server.keystore"),
				"songtoolong".toCharArray());
		// A KeyManagerFactory is used to create key managers
		javax.net.ssl.KeyManagerFactory kmf = KeyManagerFactory
				.getInstance("SunX509");
		// Initialize the KeyManagerFactory to work with our keystore
		kmf.init(keystore, "garlicbread".toCharArray());
		// An SSLContext is an environment for implementing JSSE
		// It is used to create a ServerSocketFactory
		javax.net.ssl.SSLContext sslc = SSLContext
				.getInstance("SSLv3");
		// Initialize the SSLContext to work with our key managers
		sslc.init(kmf.getKeyManagers(), null, null);
		// Create a ServerSocketFactory from the SSLContext
		ServerSocketFactory ssf = sslc.getServerSocketFactory();
		// Socket to me
		SSLServerSocket serverSocket = (SSLServerSocket) ssf
				.createServerSocket(4433);
		// Authenticate the client?
		serverSocket.setNeedClientAuth(false);
		// Return a ServerSocket on the desired port (443)
		return serverSocket;
	}

	class Echoer extends Thread {
		private SSLSocket sslsocket;

		public Echoer(SSLSocket sock) {
			sslsocket = sock;
		}

		public void run() {
			try {
				InputStream inputstream = sslsocket.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(
						inputstream);
				BufferedReader bufferedreader = new BufferedReader(
						inputstreamreader);

				String string = null;
				PrintWriter pw = new PrintWriter(sslsocket.getOutputStream());
				while ((string = bufferedreader.readLine()) != null) {
					pw.print(string);
					pw.flush();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

