 /**
   * Project 5
   * @author Evan Pagryzinski, epagryzi, 808
   * @author Jacquelyn Reh, jreh, 803
   */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class SafeWalkServer extends Thread implements Runnable {
	ArrayList<Socket> clientList = new ArrayList<Socket>();
	ArrayList<PrintWriter> outList = new ArrayList<PrintWriter>();
	ArrayList<String> inList = new ArrayList<String>();
	private ServerSocket serverSocket;

	/**
	 * Construct the server, set up the socket.
	 * 
	 * @throws SocketException
	 *             if the socket or port cannot be obtained properly.
	 * @throws IOException
	 *             if the port cannot be reused.
	 */
	public SafeWalkServer(int port) throws SocketException, IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
	}

	/**
	 * Construct the server and let the system allocate it a port.
	 * 
	 * @throws SocketException
	 *             if the socket or port cannot be obtained properly.
	 * @throws IOException
	 *             if the port cannot be reused.
	 */
	public SafeWalkServer() throws SocketException, IOException {
		System.out.println("Port not specified. Using free port 8888");
		serverSocket = new ServerSocket(0);
		serverSocket.setReuseAddress(true);
	}

	/**
	 * Return the port number on which the server is listening.
	 */
	public int getLocalPort() {
		return serverSocket.getLocalPort();
	}

	/**
	 * check validity
	 */
    public static int isValidity(String check) {
        if(check.substring(0,1).equals(":")) {
        	if(check.equals(":LIST_PENDING_REQUESTS")) {
        		return 2;
        	}
        	if(check.equals(":RESET")) {
        		return 3;
        	}
        	if(check.equals(":SHUTDOWN")) {
        		return 4;
        	}
        		
        }
                
        else { 
        	String[] parts = check.split(",");
        	if(parts.length == 4) {
                        if(parts[1].equals("EE") ||parts[1].equals("LWSN") || parts[1].equals("PMU") || parts[1].equals("PUSH")) {
                                if(parts[2].equals("EE") || parts[2].equals("LWSN") || parts[2].equals("PMU") || parts[2].equals("PUSH") || parts[2].equals("*")) {
                                        if(!parts[1].equals(parts[2])) {
                                                return 1;
                                        }
                                }
                        }
                 }
        } //else
               	return 0;
} //close isValidity
    /*
     * 0 - false -- nothing occured, return the error
     * 1 - true -- all valid no errors or commands
     * 2 - Pending list command
     * 3 - Reset
     * 4 - shut down
     */


	/**
	 * Start a loop to accept incoming connections.
	 */
	public void run() {
		try {
		while (true) {
			Socket client = serverSocket.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream());
			String a = (String)in.readLine();
			
			if(isValidity(a) == 0) {
				String asd = "ERROR: invalid request";
				out.write(asd);
				out.flush();
				client.close();
				out.close();
				in.close();
			} //close 0 if
			
			
			if(isValidity(a) == 2) {
				String listli = "[";
				if (inList.size() != 0){
				for(int i = 0; i < inList.size(); i++) {
					listli += "[" + inList.get(i) + "]";
					if (i + 1 < inList.size()) {
					listli += ",";
					}
				} //close for
				listli += "]";
				listli = listli.replaceAll(",", ", ");
				}
				out.write(listli);
				out.flush();
				client.close();
			} //close 2 if
			
			
			if(isValidity(a) == 3) {
				for(int j = 0; j < clientList.size(); j++) {
					outList.get(j).write("ERROR: connection reset");
					outList.get(j).flush();
					clientList.get(j).close();
				} //close for
				out.write("RESPONSE: success");
				out.flush();
				client.close();
			} //close if 3
			
			
			if(isValidity(a) == 4) {
				for(int j = 0; j < clientList.size(); j++) {
					outList.get(j).write("ERROR: connection reset");
					outList.get(j).flush();
					clientList.get(j).close();
				} //close for
				out.write("RESPONSE: success");
				out.flush();
				client.close();
				serverSocket.close();
				
				
				
			} //close 4
			
			
			if(isValidity(a) == 1) {
			String[] aList = a.split(",");
			
			if(inList.size() != 0) {
			for (int x = 0; x < outList.size(); x++) {
				String b = (String) inList.get(x).toString();
				String[] bList = b.split(",");
				if (aList[1].toString().equals(bList[1].toString()) && (aList[2].toString().equals(bList[2].toString()) || ((aList[2].toString().equals("*") ^ bList[2].toString().equals("*"))))) {
					if (aList[2].equals("*") && bList[2].equals("*")) {
						outList.add(out);
						inList.add(a);
						clientList.add(client);
						break;
					} else {
					out.write("RESPONSE: " + inList.get(x));
					out.flush();
					outList.get(x).write("RESPONSE: " + a);
					outList.get(x).flush();
					clientList.get(x).close();
					outList.remove(x);
					inList.remove(x);
					clientList.remove(x);
					client.close();
					}
				} else {
					outList.add(out);
					inList.add(a);
					clientList.add(client);
					break;
				}
			}
			} else if (inList.size() == 0){
			outList.add(out);
			inList.add(a);
			clientList.add(client);
			}
			}
		}
		} catch (Exception e) {
			
		}
	}
	
	
	/*
	 * 
	 *  
    :LIST_PENDING_REQUESTS
        Write to client socket all unpaired request messages, in the form of 4-tuples (see example below), from oldest to newest request, and close the client socket that originated this command.
    :RESET
        For each waiting request message, respond with ERROR: connection reset and close its socket, before discarding the request.
        For the client that originated the command, respond with RESPONSE: success and close its socket.
    :SHUTDOWN
        Gracefully terminate the server (i.e., do what :RESET does, close the socket and whatever streams your server uses, and exit the run loop).


	 */
	
	
	public static void main(String[] args) throws SocketException, IOException, ClassNotFoundException {
		if (args.length == 1) {
			if (Integer.parseInt(args[0]) > 1025 || Integer.parseInt(args[0]) < 65535) {
				SafeWalkServer sws = new SafeWalkServer(Integer.parseInt(args[0]));
				sws.run();
			}
		} else {
			SafeWalkServer sws = new SafeWalkServer();
			sws.run();
		}
	}
}
