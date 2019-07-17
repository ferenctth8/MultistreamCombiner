package com.feritoth.multistreamapp.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.feritoth.multistreamapp.core.CoreData;
import com.feritoth.multistreamapp.core.DataWrapper;
import com.feritoth.multistreamapp.core.QuitCommand;

public class MultistreamProcessor {
	
	private MultistreamProcessor(){}
	
	//Application logger
	private static final Logger LOGGER = LoggerFactory.getLogger(MultistreamProcessor.class);
	
	/* Static method for loading the server configuration */
	public static Properties loadEntityConfiguration(String configurationFilePath) {
		//Initialize property map
		Properties entityProps = new Properties();
		//Load the given info
		try (InputStream configInput = MultistreamProcessor.class.getClassLoader().getResourceAsStream(configurationFilePath)){			
			if (configInput == null){
				LOGGER.error("Unfortunately, the system is unable to locate configuration file on path:" + configurationFilePath);
				return null;
			}
			entityProps.load(configInput);			
		} catch (IOException e) {
			LOGGER.error("Exception encountered during the setup of the server configuration.The encountered exception is:" + e);
			return null;
		}
		return entityProps;
	}
	
	/* Static method for triggering the console input reader */
	public static List<String> returnConsoleInput(String procTermKeyword) throws IOException {
		List<String> allAmounts = new ArrayList<>();
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
		String newRecord = null;
		while (!(newRecord = bufReader.readLine()).equalsIgnoreCase(procTermKeyword)) {
			allAmounts.add(newRecord);
		}
		return allAmounts;
	}

	/* Static method for global amount conversion - used for assembling the input amounts into the final data format accepted by the server */
	public static String convertAmountValues(List<String> allElligibleAmounts) throws JsonProcessingException {
		List<Double> matchingAmounts = new ArrayList<Double>(); 
		allElligibleAmounts.forEach(amount -> {
			//convert each amount value and compose the output as follows
			Double numericalAmount = checkAndConvertAmount(amount);
			if (numericalAmount != null){
				matchingAmounts.add(numericalAmount);
			}
		});
		if (matchingAmounts.isEmpty()){
			LOGGER.warn("No suitable numerical values have been found for transmission to the server!");
			return "";
		} else {
			String finalAmountData = createDataRecord(matchingAmounts);
			LOGGER.info("The final data values are:\n" + finalAmountData);
			return finalAmountData;
		}
	}
		
	/* Static method used for convertibility check and execution for each individual amount value */
	private static Double checkAndConvertAmount(String stringAmount) {
		try {
			Double numericalAmount = Double.valueOf(stringAmount);
			return numericalAmount;
		} catch (NumberFormatException e) {
			LOGGER.info("Unable to convert the given amount " + stringAmount + " as it is not a valid number unfortunately...");
			return null;
		}
	}
		
	/* Static method used for creating the final data format to be passed over to the server */
	private static String createDataRecord(List<Double> matchingAmounts) throws JsonProcessingException {
		String finalNumericalInput = convertDataRecordToXMLLine(matchingAmounts.get(0));
		for (int i = 1; i < matchingAmounts.size(); i++){
			finalNumericalInput += "\n" + convertDataRecordToXMLLine(matchingAmounts.get(i));
		}
		return finalNumericalInput;
	}

	/* Static XML generator method for the amount list */
	private static String convertDataRecordToXMLLine(Double amount) throws JsonProcessingException {
		CoreData newCoreRecord = new CoreData(new Timestamp(System.currentTimeMillis()), amount);
		return generateXMLfromCoreObject(newCoreRecord);
	}
		
	/* Static XML generator method for the halt message entry */
	public static String createSpecialXMLDataRecord(String haltMessage) throws JsonProcessingException {
		QuitCommand quitRecord = new QuitCommand(new Timestamp(System.currentTimeMillis()), haltMessage);
		return generateXMLfromCoreObject(quitRecord);
	}

	/* Static XML marshaller method for both core objects */
	private static String generateXMLfromCoreObject(Serializable coreObject) throws JsonProcessingException {
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, true).setSerializationInclusion(Include.NON_EMPTY);
		String xmlRepresentation = xmlMapper.writeValueAsString(coreObject);
		LOGGER.info("The XML representation of the data is:\n" + xmlRepresentation);
		return xmlRepresentation;
	}
	
	/* Static method for de-serialization of XML into core Java object (regular amount with timestamp) */
	public static CoreData convertXMLDataRecordToCoreData(String data) throws JsonParseException, JsonMappingException, IOException {
		XmlMapper xmlMapper = new XmlMapper();
		CoreData coreData = xmlMapper.readValue(data, CoreData.class);
		return coreData;
	}
		
	/* Static method for converting the final data result into a JSON string */
	public static String convertAmountsToJSON(Map<Timestamp, Double> amountDataMap) throws JsonProcessingException {
		//collect the amount data
		List<DataWrapper> amountDataList = new ArrayList<>();
		amountDataMap.keySet().forEach(timeKey -> {
			amountDataList.add(new DataWrapper(new CoreData(timeKey, amountDataMap.get(timeKey))));
		});
		//convert the list in question to JSON
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, true).setSerializationInclusion(Include.NON_EMPTY);
		String jsonRepresentation = objMapper.writeValueAsString(amountDataList);
		LOGGER.info("The JSON representation of the data is:\n" + jsonRepresentation);
		return jsonRepresentation;
	}
	
	/* Static method for de-serialization of XML into core Java object (halt command case) */
	public static QuitCommand convertXMLDataRecordToHaltCommand(String haltLine) throws JsonParseException, JsonMappingException, IOException {
		XmlMapper xmlMapper = new XmlMapper();
		QuitCommand resignation = xmlMapper.readValue(haltLine, QuitCommand.class);
		return resignation;
	}
	
	/* Static method method for merging the data for the general case - data contains no "Quit" message in here */
	public static String mergeDataForGeneralCase(List<String> newAmounts) throws JsonProcessingException {
		String finalAmounts = MultistreamProcessor.convertAmountValues(newAmounts);
		if (finalAmounts.isEmpty()){
			LOGGER.warn("No suitable amount values have been detected for transmission inside the processed input, nothing to be sent out to server for now...");
			return null;
		} else {
			LOGGER.info("The following values will be ready for transmission:\n" + finalAmounts);
			return finalAmounts;
		}
	}

	/* Static method for merging the data to be sent over to the server under XML format - data contains "Quit" message */
	public static String mergeDataForQuitCase(List<String> allElligibleAmounts, String appTermKeyword) throws JsonProcessingException {
		//process the retrieved values and merge them accordingly
		String finalData = null;
		String finalAmounts = MultistreamProcessor.convertAmountValues(allElligibleAmounts);
		if (StringUtils.isBlank(finalAmounts)){
			finalData = MultistreamProcessor.createSpecialXMLDataRecord(appTermKeyword);
		} else {
			finalData = MultistreamProcessor.convertAmountValues(allElligibleAmounts) + "\n" + MultistreamProcessor.createSpecialXMLDataRecord(appTermKeyword);							
		}
		LOGGER.info("The data set to be sent out to the server is:\n" + finalData);
		return finalData;
	}

}