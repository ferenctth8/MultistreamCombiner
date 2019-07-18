package com.feritoth.multistreamapp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feritoth.multistreamapp.utility.MultistreamProcessor;

public class MultistreamClientB {

	// Path to the configuration files for the client setup
	public static final String SECOND_CLIENT_CONFIGURATION_FILE = "clientB.properties";
	// The application logger
	private static final Logger LOGGER = LoggerFactory.getLogger(MultistreamClientB.class);
	
	// The client start method
	public void startClient(String clientConfigurationFile)	throws NumberFormatException, IOException, InterruptedException {
		// Invoke the property processor method
		Properties secondClientProps = MultistreamProcessor.loadEntityConfiguration(clientConfigurationFile);
		if (secondClientProps == null) {
			// Print an error and quit the program in case of problems with the configuration
			LOGGER.error("Unable to proceed with server configuration due to unavailable properties...Please check the configuration file path and restart the app!");
			return;
		}

		// Extract next the required properties
		String hostname = secondClientProps.getProperty("host");
		Integer port = Integer.valueOf(secondClientProps.getProperty("port"));
		String appTerminatorKeyword = secondClientProps.getProperty("appTerminator");
		String processingTerminatorKeyword = secondClientProps.getProperty("processingTerminator");
		String bufferCapacity = secondClientProps.getProperty("bufferCapacity");

		// Create next the connection channel and its address
		InetSocketAddress multistreamAddress = new InetSocketAddress(hostname,port);
		SocketChannel multistreamClient = SocketChannel.open(multistreamAddress);
		LOGGER.info("Connecting to Server on host " + hostname + " and port " + port + "...");
		while (true){
			//Get the final input
			String finalTestInput = processConsoleInput(appTerminatorKeyword, processingTerminatorKeyword);
			LOGGER.info("The final test input is:\n" + finalTestInput);
			//Check if this input data is not null
			if (finalTestInput != null){
				//Encode it and send it over the server via an output byte buffer
				byte[] finalMessage = finalTestInput.getBytes();
				ByteBuffer buffer = ByteBuffer.wrap(finalMessage);
				multistreamClient.write(buffer);
				LOGGER.info("Sending: " + finalTestInput);
				//Log out the result received from the server via an input buffer
				ByteBuffer inputBuffer = ByteBuffer.allocate(Integer.valueOf(bufferCapacity));
				multistreamClient.read(inputBuffer);
				String response = new String(inputBuffer.array()).trim();
				LOGGER.info("The current status of processed values is:\n" + response);
				//Break the loop in case of detecting a "Quit" command
				if (StringUtils.containsIgnoreCase(finalTestInput, appTerminatorKeyword)){
					break;
				}
			} else {
				LOGGER.warn("No test input sent over to the server currently - try introducing again some proper numerical values...");
			}			
		}
		
		//Close the client if "Quit" command is detected inside the transmitted input
		multistreamClient.close();	
	}	
	
	//method used for triggering the processing of the console input information
	private String processConsoleInput(String appTermKeyword, String procTermKeyword){
		while(true){
			try{
				//get the amount list to be processed
				List<String> newAmounts = MultistreamProcessor.returnConsoleInput(procTermKeyword);
				//check for the presence of the Quit command inside the supplied data
				if (newAmounts.contains(appTermKeyword)){
				//for affirmative case: pick up the elements up to the given command and process them accordingly
					List<String> allElligibleAmounts = newAmounts.subList(0, newAmounts.indexOf(appTermKeyword));
					if (allElligibleAmounts.isEmpty()){
						//No values found for procession, client will terminate gracefully
						LOGGER.info("No amount values were found for processing, client will thus exit gracefully...");
						return MultistreamProcessor.createSpecialXMLDataRecord(appTermKeyword, System.currentTimeMillis());
				    } else {
						//terminate the client after sending over the processed values to the server
						LOGGER.info("Quit command detected, shutting down client after transmission of the processed values...");
						return MultistreamProcessor.mergeDataForQuitCase(allElligibleAmounts, appTermKeyword);
					}					
				} else {
					//process the values to be sent over to the server
					return MultistreamProcessor.mergeDataForGeneralCase(newAmounts);
				}
			} catch (IOException e){
				LOGGER.error("Unable to run the current client due to the following exception:" + e);
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void main(String[] args) {		
		try {
			new MultistreamClientB().startClient(SECOND_CLIENT_CONFIGURATION_FILE);
		} catch (NumberFormatException | IOException | InterruptedException e) {
			LOGGER.error("Unable to run the application due to the following exception:" + e);
		}
	}

}