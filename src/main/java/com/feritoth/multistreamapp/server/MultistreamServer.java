package com.feritoth.multistreamapp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.feritoth.multistreamapp.core.CoreData;
import com.feritoth.multistreamapp.core.QuitCommand;
import com.feritoth.multistreamapp.utility.MultistreamProcessor;

public class MultistreamServer {
	
	//Path to the configuration file for the server setup
	public static final String SERVER_CONFIGURATION_FILE = "server.properties";
	//The application logger
	private static final Logger LOGGER = LoggerFactory.getLogger(MultistreamServer.class);
	//The data map
	private Map<Timestamp, Double> allAmounts;
	
	public MultistreamServer(){
		allAmounts = new TreeMap<>();
	}
	
	//The server starter method
	public void startMultistreamServer() throws IOException, NumberFormatException, ClassNotFoundException {
		//Open a selector
		Selector selector = Selector.open();
		//Invoke the property processor method
		Properties serverProps = MultistreamProcessor.loadEntityConfiguration(SERVER_CONFIGURATION_FILE);
		if (serverProps == null){
			//Print an error and quit the program in case of problems with the configuration
			LOGGER.error("Unable to proceed with server configuration due to unavailable properties...Please check the configuration file path and restart the app!");
			return;
		}
		
		//Process the given property values next
		String ports = serverProps.getProperty("ports");
		String[] portList = ports.split(", ");
		String haltCommand = serverProps.getProperty("appTerminator");
		String mainHost = serverProps.getProperty("mainHost");
		String bufferCapacity = serverProps.getProperty("bufferCapacity");
		String temporaryMessage = serverProps.getProperty("temporaryMessage");
		
		//Get the addresses associated to the previously fetched host name
		InetAddress[] allAddresses = InetAddress.getAllByName(mainHost);
		if (allAddresses.length == 0){
			LOGGER.error("The selected host name does not have any valid addresses assigned to it - therefore the program will exit due impossibility of creating valid connections...");
			return;
		}
		//Check next the number of ports and valid addresses to see how many connections will need to be created
		int finalNb = 0;
		if (portList.length > allAddresses.length){
			finalNb = allAddresses.length;
		} else {
			finalNb = portList.length;
		}
		
		//Create and configure the ServerSocketChannels for each of the given ports and hosts
		for (int i = 0; i < finalNb; i++){
			ServerSocketChannel multistreamSocket = ServerSocketChannel.open();
			//Bind the channel socket to a local address and configure it for accepting connections
			multistreamSocket.socket().bind(new InetSocketAddress(allAddresses[i], Integer.valueOf(portList[i])));
			//Disable the blocking mode for the given channel
			multistreamSocket.configureBlocking(false);
			//Take the valid operations supported on this channel and register for them a selection key
			int supportedOps = multistreamSocket.validOps();
			SelectionKey selectionKey = multistreamSocket.register(selector, supportedOps);
			LOGGER.info("The selectionKey associated to the current socket is:" + selectionKey);
		}
		
		//Make the server wait for connections for all the configured host-port combinations
		//Keep it running infinitely for now
		while (true){
			LOGGER.info("Server started and listening for connections on all previously created host-port combinations...");
			//Pick a set of keys for whom the corresponding channels are ready for the I/O operations
			int nbOfAvailableChannels = selector.select();
			LOGGER.info("The number of currently available channels is:" + nbOfAvailableChannels);
			//Get next the token responsible for marking the pairing of SelectableChannel with a Selector
			Set<SelectionKey> tokenKeySet = selector.selectedKeys();
			Iterator<SelectionKey> tokenSetIterator = tokenKeySet.iterator();
			
			while(tokenSetIterator.hasNext()){
				//Get next key and check if it is readable or acceptable
				SelectionKey currentTokenKey = tokenSetIterator.next();
				//Check if the channel connected to the key is ready for accepting a new socket connection
				if (currentTokenKey.isAcceptable()){
					//Obtain the multi-stream client, disable its blocking mode and register it for reading
					SocketChannel multistreamAcceptClient = ((ServerSocketChannel) currentTokenKey.channel()).accept();
					multistreamAcceptClient.configureBlocking(false);
					multistreamAcceptClient.register(selector, SelectionKey.OP_READ);
					LOGGER.info("Connection accepted from client on:" + multistreamAcceptClient.getLocalAddress());
				} 
				//Check if the channel connected to the key is ready for reading
				if (currentTokenKey.isReadable()) {
					//Obtain again the multi-stream client, but this time configure it for reading
					SocketChannel multistreamReadWriteClient = (SocketChannel) currentTokenKey.channel();					
					ByteBuffer inputBuffer = ByteBuffer.allocate(Integer.valueOf(bufferCapacity));
					//Read the content coming from the channel as a byte array and then convert it
					multistreamReadWriteClient.read(inputBuffer);
					String content = new String(inputBuffer.array()).trim();
					inputBuffer.clear();
					//Log the message and process it accordingly 
					LOGGER.info("Message received: " + content + " on port:" + multistreamReadWriteClient.socket().getLocalPort());
					String currentResult = processClientInput(content, haltCommand);
					//Check for a null result in order to avoid having NullPointerExceptions
					if (StringUtils.isBlank(currentResult)){
						currentResult = temporaryMessage;
					}
					//Send back the result to the client in question before the eventual shutdown
					inputBuffer = ByteBuffer.wrap(currentResult.getBytes());
					multistreamReadWriteClient.write(inputBuffer);
					inputBuffer.clear();
					//Check if the client needs to be shut down
					if (StringUtils.containsIgnoreCase(content, haltCommand)) {
						multistreamReadWriteClient.close();
						LOGGER.info("The client on port " + multistreamReadWriteClient.socket().getLocalPort() + " and host " + multistreamReadWriteClient.socket().getInetAddress() + " will be shut down on request.");
						LOGGER.info("Server will keep running. Try restarting the given client to establish new connection...");
					}
				}
				//Remove the given element once it has been processed to avoid caching
				tokenSetIterator.remove();
			}
		}
	}	
	
	//Main processor for processing the input coming from client
	public String processClientInput(String clientInput, String terminatorKeyword) throws JsonParseException, JsonMappingException, IOException{
		String haltLine = "";
		boolean containsHaltMessage = StringUtils.containsIgnoreCase(clientInput, terminatorKeyword);
		//split the initial input into tokens and process them appropriately
		String[] dataLines = clientInput.split("\n");
		List<String> inputData = new ArrayList<>(Arrays.asList(dataLines));		
		//check if halt is present or not
		if (containsHaltMessage) {
			//retain the halt message - this is the last element in the array
			haltLine = inputData.remove(inputData.size() - 1);			
		}
		//proceeding with the processing as follows
		return parseAndConvertInputData(inputData,haltLine);		
	}

	//method for parsing and converting the input data coming from the client side
	private String parseAndConvertInputData(List<String> inputData, String haltLine) throws JsonParseException, JsonMappingException, IOException {
		//process the halt command if present before exiting
		if (!StringUtils.isBlank(haltLine)){
			QuitCommand resignation = MultistreamProcessor.convertXMLDataRecordToHaltCommand(haltLine);
			LOGGER.info("Client resignation command has been received:" + resignation.toString());
		}
		//convert first the regular amount values - avoid the processing of an empty list
		if (!inputData.isEmpty()){
			return convertInputAmountData(inputData);
		}
		//return a null value just in case of no previously saved values
		if (allAmounts.isEmpty()){
			return null;
		}
		//otherwise, return the previously saved values
		return MultistreamProcessor.convertAmountsToJSON(allAmounts);
	}
	
	//method for conversion and processing of amounts
	private String convertInputAmountData(List<String> inputDataList) throws JsonParseException, JsonMappingException, IOException {		
		//process each string element as follows
		for (String dataRecord : inputDataList){
			//first convert the XML record into corresponding core object
			CoreData coreData = MultistreamProcessor.convertXMLDataRecordToCoreData(dataRecord);
			//then process the given object as follows
			Set<Timestamp> timeKeySet = allAmounts.keySet();
			if (timeKeySet.contains(coreData.getTimestamp())){
				Double initialAmount = allAmounts.get(coreData.getTimestamp());
				Double newAmount = initialAmount + coreData.getAmount();
				allAmounts.put(coreData.getTimestamp(), newAmount);
			} else {
				allAmounts.put(coreData.getTimestamp(), coreData.getAmount());
			}
		}
		//convert the result into a JSON
		return MultistreamProcessor.convertAmountsToJSON(allAmounts);
	}
	
	public static void main(String[] args){
		try {
			new MultistreamServer().startMultistreamServer();
		} catch (IOException e) {
			LOGGER.error("Unable to run the application due to the following exception:" + e);
		} catch (NumberFormatException e) {
			LOGGER.error("Unable to run the application due to the following conversion exception:" + e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Unable to run the application due to the following absent class:" + e);
		}
	}
	
}