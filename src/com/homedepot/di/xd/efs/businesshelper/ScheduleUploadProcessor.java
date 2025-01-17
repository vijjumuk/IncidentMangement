package com.homedepot.di.xd.efs.businesshelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.homedepot.di.xd.efs.constants.EFSConstants;
import com.homedepot.di.xd.efs.constants.UploadConstants;
import com.homedepot.di.xd.efs.dao.ScheduleDAO;
import com.homedepot.di.xd.efs.exception.DataOperationException;
import com.homedepot.di.xd.efs.exception.EFSException;
import com.homedepot.di.xd.efs.to.InputFileTO;
import com.homedepot.di.xd.efs.to.ResponseTO;
import com.homedepot.di.xd.efs.to.ScheduleOrderDayTO;
import com.homedepot.di.xd.efs.to.ScheduleTO;
import com.homedepot.di.xd.efs.util.EFSUtil;
import com.homedepot.di.xd.efs.util.UploadValidationUtil;

public class ScheduleUploadProcessor implements IUploadProcessor {


	private static final Logger LOGGER = Logger
			.getLogger(ScheduleUploadProcessor.class);

	@Override
	public void processFile(InputFileTO fileObj, ResponseTO responseTO)
			throws IOException, EFSException {
		processValidHeaderDataForSchedule(fileObj, responseTO);
	}

	/**
	 * This method is to validate the uploaded file headers
	 */
	public int validateHeader(String[] uploadedHeader) {

		if(LOGGER.isDebugEnabled()){
			LOGGER.debug( UploadConstants.ENTERING_METHOD + "validateHeader");
		}

		int hdrValidationCode = UploadValidationUtil.validateHeaderFields(uploadedHeader,
				UploadConstants.getScheduleHeader());

		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Schedule Upload header validation return code is "+hdrValidationCode);
			LOGGER.debug( UploadConstants.EXIT_METHOD + "validateHeader");
		}
		return hdrValidationCode;
	}


	/**
	 * Process the Schedule Data
	 *  
	 * @param fileObj
	 * @param responseTO
	 * @throws EFSException
	 */
	public static void processValidHeaderDataForSchedule(InputFileTO fileObj,
			ResponseTO responseTO) throws EFSException{

		if(LOGGER.isDebugEnabled()){
			LOGGER.debug( UploadConstants.ENTERING_METHOD + "processValidHeaderDataForSchedule");
		}

		int totalNoOfRecordProcessed = 0;
		Map<String, List<ScheduleTO>> validMap = new HashMap<>();
		List<ScheduleTO> scheduleTOList = null;
		List<ScheduleTO> inValidList = new ArrayList<>();
		List<ScheduleTO> validScheduleList = new ArrayList<>();
		List<ScheduleTO> inputvalidScheduleList =null;
		Set<Short> scheduleTypeCode = new HashSet<>();
		Set<String> validTypeGroupCodeList = new HashSet<>();
		List<String> destinationList = new ArrayList<>();
		Map<Integer, Object[]> fileContents = null;
		Map<String, String> locationLastWorkDayCode = null;

		try{

			int totalNoOfRecords = 0;
			StringBuilder errString = null;

			//Read the file content
			fileContents = fileObj.getFileContents();

			//To get the Last Working day and Hours for all locations 
			locationLastWorkDayCode = ScheduleDAO.getMaxWorkDayForLocation();

			// Read each record in the file and validate
			for (Map.Entry<Integer, Object[]> entry : fileContents.entrySet()){

				if(entry.getKey() == 0){
					continue;
				}

				Object[] row = entry.getValue();
				errString = new StringBuilder();

				ScheduleTO scheduleTo = null; // = new ScheduleTO();
				String origin = null; 
				String destination = null;
				String scheduleType =null;
				short scheduleGroup = 0;

				//If Origin ID is invalid add it to error string
				if (!UploadValidationUtil.isOriginValid(row[0].toString())) {
					errString.append(UploadConstants.INVALID_ORIGIN);
				}else{
					origin = row[0].toString();
				}

				// If Destination ID is invalid add it to error string
				if (!UploadValidationUtil.isDestinationValid(row[1].toString())) {
					errString.append(UploadConstants.INVALID_DESTINATION);
				}else{
					destination = row[1].toString();
					destinationList.add(destination);
				}

				// If Schedule Type Code is invalid add it to error string
				if (!UploadValidationUtil.isNumeric(row[2].toString())) {
					errString.append(UploadConstants.INVALID_SCHEDULE_TYP_CD);
				}else{
					scheduleType = row[2].toString();
				}

				// If Schedule Group Code is null then Default to 1 .
				if(row[3] ==null){
					scheduleGroup = (short) UploadConstants.ONE;

				}
				else{
					//If Schedule Group Code is invalid add it to error string
					if(!UploadValidationUtil.isNumeric(row[3].toString()))
					{
						errString.append(UploadConstants.INVALID_SCHEDULE_GRP_CD);
					}
					else
					{
						scheduleGroup = (short)row[3];
					}
				}


				// Check if  Effective begin date exist
				// if exist then convert that into Util.Date format
				if(row[24] == null){
					errString.append(UploadConstants.INVALID_EFF_BGN_DT);
				}

				// Check if  Effective begin date exist
				// if exist then convert that into Util.Date format
				if(row[25] == null){
					errString.append(UploadConstants.INVALID_EFF_END_DT);
				}


				/**
				 * Check in ValidMap for existence of a record in below combination
				 * if (Location + Destination + Schedule Type CD + Schedule Group CD ) exist.
				 * 		Check for Order Day existence in scheduleOrderDayTO List
				 * 
				 *  Valid OrderDay is from 1 to 7 
				 */
				String key = origin+"_"+destination+"_"+scheduleType+"_"+scheduleGroup; //+"_"+effBeginDt+"_"+effEndDt;
				LOGGER.debug("ValidMap Key is"+key); 

				// This is to handle multiple rows in excel
				if(validMap.get(key) != null){

					java.sql.Date effBeginDate = UploadValidationUtil.convertSUtilDateToSqlDate(row[24]);
					java.sql.Date effEndDate = UploadValidationUtil.convertSUtilDateToSqlDate(row[25]);
					for(ScheduleTO to: validMap.get(key)){

						/*
						 * Check if Effective dates are getting overlaps if yes then check the Order Day
						 */
						LOGGER.debug("Incoming Dates are :  "+effBeginDate +'\t'+effEndDate); 
						LOGGER.debug("Dates already present in TO " +to.getEffectiveBeginDate() +'\t'+to.getEffectiveEndDate());
						if(!UploadValidationUtil.isDateRangeOverlapping(to.getEffectiveBeginDate(), to.getEffectiveEndDate(), 
								effBeginDate, effEndDate)){
							// Need to create a new ScheduleTO to keep different effective Dates 
							scheduleTo = new ScheduleTO();
							scheduleTo.setOriginNumber(origin);
							scheduleTo.setScheduleGroupCode(scheduleGroup);
							populateScheduleTO(scheduleTo, row, errString,fileObj, locationLastWorkDayCode);

							//finally add the scheduleTO to Map 
							if(errString.length() > 0){
								scheduleTo.setErrorString(errString.toString());
								inValidList.add(scheduleTo);
							}else{

								// Adding in these list for latest Database Validation
								scheduleTypeCode.add(scheduleTo.getScheduleTypeCode());
								LOGGER.debug("scheduleTOList Size before"+scheduleTOList.size());
								scheduleTOList.add(scheduleTo);
								LOGGER.debug("scheduleTOList Size after"+scheduleTOList.size());
								validMap.put(key, scheduleTOList);
								break;
							}
						}else{

							// Check to ensure Order day is also not overlapping
							/**
							 * Compare the EFF_DTs from tempTO and compare with the incoming row 
							 * variables eff_dts to find if there is any Overlapping exists
							 */
							Map<Short, ScheduleOrderDayTO> orderDayToMap = to.getScheduleOrderDayTO();

							if(orderDayToMap!=null){
								ScheduleOrderDayTO orderDayTo = null;
								Short orderDay = null;
								if(row[5]!=null){
									orderDay = (Short) row[5];

									/**
									 * Check to see there is any Order Day object exists 
									 * if Not create a new ScheduleOrderDayTO and add in Map
									 * if exist throw error for duplicate order day record
									 */ 
									if(orderDayToMap.get(orderDay) == null){
										LOGGER.debug("Building a new orderDay" +orderDay);
										orderDayTo = new ScheduleOrderDayTO();
										populateScheduleOrderDayTO(orderDayTo,row,orderDay, errString,fileObj, locationLastWorkDayCode);
										LOGGER.debug("orderDayToMap size before" +orderDayToMap.size());
										orderDayToMap.put(orderDay, orderDayTo);
										LOGGER.debug("orderDayToMap size after" +orderDayToMap.size());
									}else{
										LOGGER.debug( UploadConstants.INVALID_ORDER_DAY_RECORD);
										errString.append(UploadConstants.INVALID_ORDER_DAY_RECORD + orderDay);
									}
								}else{
									LOGGER.debug("Order Day is null");
									errString.append(UploadConstants.INVALID_ORDER_DAY + orderDay);
									//continue;
								}
							}

							if(errString.length() > 0){
								LOGGER.debug("Building the Invalid TO");
								scheduleTo = new ScheduleTO();
								scheduleTo.setErrorString(errString.toString());
								scheduleTo.setOriginNumber(origin);
								scheduleTo.setScheduleGroupCode(scheduleGroup);
								scheduleTo.setDestinationNumber((int)row[1]);
								scheduleTo.setScheduleTypeCode((short)row[2]);
								scheduleTo.setEffectiveBeginDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[24]));
								scheduleTo.setEffectiveEndDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[25]));
								scheduleTo.setCreatedUserId(fileObj.getUserId());
								scheduleTo.setLastUptdUserId(fileObj.getUserId());
								// Check for Schedule Name exist or not
								scheduleTo.setScheduleName(row[4].toString());
								if (!UploadValidationUtil.isValidStringLength(row[4].toString(), 100)) {
									errString.append(UploadConstants.INVALID_SCHEDULE_NAME);
								}
								LOGGER.debug("InvalidTO list size before"+inValidList.size());
								inValidList.add(scheduleTo);
								LOGGER.debug("InvalidTO list size after"+inValidList.size());
							}
						}
					}
				}else{

					// First time adding the ScheduleTO to validMap
					scheduleTo = new ScheduleTO();
					scheduleTo.setOriginNumber(origin);
					scheduleTo.setScheduleGroupCode(scheduleGroup);
					populateScheduleTO(scheduleTo, row, errString,fileObj, locationLastWorkDayCode);

					//finally add the scheduleTO to Map 
					if(errString.length() > 0){
						scheduleTo.setErrorString(errString.toString());
						inValidList.add(scheduleTo);
					}else{

						// Adding in these list for latest Database Validation
						scheduleTypeCode.add(scheduleTo.getScheduleTypeCode());
						scheduleTOList = new ArrayList<>();
						scheduleTOList.add(scheduleTo);
						LOGGER.debug("Valid Map Size before"+validMap.size());
						validMap.put(key, scheduleTOList);
						LOGGER.debug("Valid Map Size after"+validMap.size());
					}
				}
				totalNoOfRecords++;
			} //end of Iterating Data
			LOGGER.debug("Valid TO List size :"+validMap.size());
			LOGGER.debug("Invalid TO List size :" +inValidList.size());
			
			for (Entry<String, List<ScheduleTO>> entry : validMap.entrySet())
			{
				//Fetching the list of TOs from the corresponding map entry
				validScheduleList.addAll((new ArrayList<>(entry.getValue())) );
			}

			// Not required since we can achieve using 2nd query itself
			// validScheduleTypeCode = ScheduleUploadDAO.validateScheduleTypeCode(scheduleTypeCode);
			if(validScheduleList.size()>0)
			{
				// Validating against the Database for existence of Type Code and associated Group Code.	
				//validTypeGroupCodeList = ScheduleUploadDAO.validateScheduleGroupCode(scheduleTypeCode);
				
				validTypeGroupCodeList = ScheduleDAO.validateScheduleGroupCode(new ArrayList<Short>(scheduleTypeCode));
			}
			
			/*
			 * Validate the Schedule Type and Group code combination 
			 */
			for(ScheduleTO to: validScheduleList){
				String item = to.getScheduleTypeCode() +"_" + to.getScheduleGroupCode();
				if(!validTypeGroupCodeList.contains(item)){
					to.setErrorString(UploadConstants.INVALID_SCHEDULE_GRP_TYP_CD);
					inValidList.add(to);
				}
			}

			validScheduleList.removeAll(inValidList);

			LOGGER.debug("Valid ScheduleList Size to be inserted is/are "+validScheduleList.size());
			LOGGER.debug("Invalid records found is/are "+inValidList.size());
			
			//List of DTO to be Populated in Database.
			for(int i = 0;i < validScheduleList.size(); i += UploadConstants.MAX_SEL_MULTIPLE) {
				inputvalidScheduleList = new ArrayList<>();
				inputvalidScheduleList.addAll(validScheduleList.subList(i, Math.min(i + UploadConstants.MAX_SEL_MULTIPLE, validScheduleList.size())));
				LOGGER.debug("Input ScheduelList size is " +inputvalidScheduleList.size());
				ScheduleDAO.insertRecords(inputvalidScheduleList, inValidList);
			}
			
			totalNoOfRecordProcessed = totalNoOfRecords - inValidList.size(); 
			
			responseTO.setReturnMessage(totalNoOfRecordProcessed +"/"+totalNoOfRecords 
					+" successful records uploaded");
			LOGGER.debug(totalNoOfRecordProcessed +"/"+totalNoOfRecords 
					+" successful records uploaded");
			
			//Sending back the invalid TOs as response.
			if(LOGGER.isDebugEnabled()){
				if(inValidList.size() > 0 ){
					LOGGER.debug("No of Records Processed is " + totalNoOfRecordProcessed +
							" and No of Records errored out is " + inValidList.size());
					LOGGER.debug("Invalid List : "+inValidList.toString());
				}
				LOGGER.debug( UploadConstants.EXIT_METHOD + "processValidHeaderDataForLane");
			}
		}catch(DataOperationException e){
			throw new EFSException(
					EFSConstants.EXCEPTION_OCCURED, e);
		}
		
	}

	/**
	 * To populate the ScheduleTO with the input data
	 * 
	 * @param scheduleTo
	 * @param row
	 * @param errString
	 * @param fileObj
	 * @param locationLastWorkDayCode
	 */
	public static void populateScheduleTO(ScheduleTO scheduleTo,  Object[] row, StringBuilder errString, 
			InputFileTO fileObj, Map<String, String> locationLastWorkDayCode){

		scheduleTo.setDestinationNumber((int)row[1]);
		scheduleTo.setScheduleTypeCode((short)row[2]);
		scheduleTo.setEffectiveBeginDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[24]));
		scheduleTo.setEffectiveEndDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[25]));
		scheduleTo.setCreatedUserId(fileObj.getUserId());
		scheduleTo.setLastUptdUserId(fileObj.getUserId());
		// Check for Schedule Name exist or not
		scheduleTo.setScheduleName(row[4].toString());
		if (!UploadValidationUtil.isValidStringLength(row[4].toString(), 100)) {
			errString.append(UploadConstants.INVALID_SCHEDULE_NAME);
		}

		ScheduleOrderDayTO orderDayTo = new ScheduleOrderDayTO();
		Short orderDay = null;

		if(row[5]!=null){
			orderDay = (Short) row[5];
			// Populates Order Day DTO
			populateScheduleOrderDayTO(orderDayTo,row,orderDay, errString,fileObj, locationLastWorkDayCode);
			Map<Short, ScheduleOrderDayTO> orderDayToMap = new HashMap<>();
			orderDayToMap.put(orderDay, orderDayTo);
			scheduleTo.setScheduleOrderDayTO(orderDayToMap);
		}else{
			errString.append(UploadConstants.INVALID_ORDER_DAY + orderDay);
		}


	}

	/**
	 * Method populates the ScheduleOrderDayTO with the input data
	 * 
	 * @param orderDayTo
	 * @param row
	 * @param orderDay
	 * @param errString
	 */
	public static void populateScheduleOrderDayTO(ScheduleOrderDayTO orderDayTo, Object[] row,Short orderDay, 
			StringBuilder errString,InputFileTO fileObj, Map<String, String> locationLastWorkDayCode){

		//Setting Order Day
		orderDayTo.setOrderDayOfWeekCode((short)row[5]);
		if(!UploadValidationUtil.isValidWeekDayNumber(row[5].toString())){
			errString.append(UploadConstants.INVALID_ORDER_DAY);
		}

		//Check if shipDay exist
		//Calculate VTT if ship Day exist
		orderDayTo.setShipmentBeginDayOfWeekCode((short) row[7]);  //NINE
		orderDayTo.setShipmentEndDayOfWeekCode((short) row[7] );
		Short shipDay = null;
		if(!UploadValidationUtil.isValidWeekDayNumber(row[7].toString())){
			errString.append(UploadConstants.INVALID_SHIP_DAY);
		}							
		else {
			shipDay=(Short) row[7]; 
			LOGGER.debug("Ship day is"+shipDay+'\t'+"Order day is"+orderDay);
			orderDayTo.setVendorTransportationTimeHours(
					UploadValidationUtil.calculateVendorTranspTime(shipDay,orderDay, locationLastWorkDayCode));
			LOGGER.debug("VTT is" +orderDayTo.getVendorTransportationTimeHours());
		}

		//Check if Arrival Day exist
		orderDayTo.setArrivalBeginDayOfWeekCode((short) row[8]); //TEN
		orderDayTo.setArrivalEndDayOfWeekCode((short) row[8]);
		if(!UploadValidationUtil.isValidWeekDayNumber(row[8].toString())){
			errString.append(UploadConstants.INVALID_ARRIVAL_DAY);
		}

		//Check if vendor Ready Time exist
		orderDayTo.setOriginLocationShipmentBeginTime(EFSUtil.convertToValidTimeFormat(row[9].toString())); //ELEVEN
		orderDayTo.setOriginLocationShipmentEndTime(EFSUtil.convertToValidTimeFormat( row[9].toString()));
		if(!UploadValidationUtil.isValidTimeFormat(row[9].toString())){
			errString.append(UploadConstants.INVALID_VNDR_RD_TYM);
		}

		//Check if  Carrier Depart Time exist
		orderDayTo.setExpectedCarrierDepartTime(EFSUtil.convertToValidTimeFormat(row[10].toString()));//TWELVE
		if(!UploadValidationUtil.isValidTimeFormat(row[10].toString()))
		{
			errString.append(UploadConstants.INVALID_CARRIER_DEPT_TYM);
		}

		//Check if Shipment Transit Time exist
		orderDayTo.setExpectedShipmentTransitTimeHours((short) row[11]); //THIRTEEN
		if(!UploadValidationUtil.isNumeric(row[11].toString()))
		{
			errString.append(UploadConstants.INVALID_SHPMNT_TRANS_TIME);
		}

		//Check if DC Appointment Time exist
		orderDayTo.setExpectedCarrierArrivalBeginTime(EFSUtil.convertToValidTimeFormat(row[12].toString()));//FOURTEEN
		orderDayTo.setExpectedCarrierArrivalEndTime(EFSUtil.convertToValidTimeFormat( row[12].toString()));
		if(!UploadValidationUtil.isValidTimeFormat(row[UploadConstants.THIRTEEN].toString()))
		{
			errString.append(UploadConstants.INVALID_DC_APP_TYM);
		}

		//Check if DC Start Work Day exist
		orderDayTo.setDcBeginShipmentWorkDayOfWeekCode((short) row[13]);//FIFTEEN
		if(!UploadValidationUtil.isValidWeekDayNumber(row[13].toString()))
		{
			errString.append(UploadConstants.INVALID_DC_START_WORK_DAY);

		}

		//Check if DC End Work Day exist
		orderDayTo.setDcEndShipmentWorkDayOfWeekCode((short) row[14]);//SIXTEEN
		if(!UploadValidationUtil.isValidWeekDayNumber(row[14].toString())){
			errString.append(UploadConstants.INVALID_DC_END_WORK_DAY);
		}

		//Check if DC Start Shift Sequence Number exists
		orderDayTo.setDcBeginWorkShiftSequenceNumber((short) row[15]);//SEVENTEEN
		if(!UploadValidationUtil.isNumeric( row[15].toString()))
		{
			errString.append(UploadConstants.INVALID_DC_START_SHFT_SEQ_NBR);
		}

		//Check if DC End Shift Sequence Number exists
		orderDayTo.setDcEndWorkShiftSequenceNumber((short) row[16]);//EIGHTEEN
		if(!UploadValidationUtil.isNumeric( row[16].toString()))
		{
			errString.append(UploadConstants.INVALID_DC_END_SHFT_SEQ_NBR);
		}

		//Check if DC Shift Count exists
		orderDayTo.setDcShipmentWorkShiftCount((short) row[17]);//NINETEEN
		if(!UploadValidationUtil.isNumeric( row[17].toString()))
		{
			errString.append(UploadConstants.INVALID_DC_SHIFT_CNT);
		}

		//Calculating the Work hours based on RESPECTVIE DC 
		/*orderDayTo.setDcShipmentWorkHours(UploadValidationUtil.calculateWorkHours(locationLastWorkDayCode, 
				row[UploadConstants.SECOND].toString(), orderDayTo.getDcShipmentWorkShiftCount()));*/
		orderDayTo.setDcShipmentWorkHours(orderDayTo.getDcShipmentWorkShiftCount() * UploadConstants.DC_WORK_HOURS);

		//Check if OrderWeek Number exists if not populate as ZERO -- No need to set
		//orderDayTo.setOrderDayOfWeekCode((short) row[UploadConstants.NINETEEN]);//TWENTY
		
		//Check if  Tender Relative Week Number exists
		orderDayTo.setTenderRelativeWeekNumber((short) row[19]);//TWENTYONE
		if(!UploadValidationUtil.isNumber( row[19].toString()))
		{
			errString.append(UploadConstants.INVALID_TNDR_RTV_WK_NBR);
		}

		//Check if Ship Week Number exists
		orderDayTo.setShipmentBeginRelativeWeekNumber((short) row[20]);//TWENTYTWO
		orderDayTo.setShipmentEndRelativeWeekNumber((short) row[20]);
		if(!UploadValidationUtil.isNumeric( row[20].toString()))
		{
			errString.append(UploadConstants.INVALID_SHP_WK_NBR);
		}

		//Check if Arrive Week Number exists
		orderDayTo.setArrivalBeginRelativeWeekNumber((short) row[21]);//TWENTYTHREE
		orderDayTo.setArrivalEndRelativeWeekNumber((short) row[21]);
		if(!UploadValidationUtil.isNumeric( row[21].toString()))
		{
			errString.append(UploadConstants.INVALID_ARRIVE_WK_NBR);
		}

		//Check if DC Start Work Week Number exists
		orderDayTo.setDcBeginShipmentWorkRelativeWeekNumber((short) row[22]);//TWENTYFOUR
		if(!UploadValidationUtil.isNumeric( row[22].toString()))
		{
			errString.append(UploadConstants.INVALID_DC_STRT_WRK_WK_NBR);
		}

		//Check if DC End Work Week Number exists
		orderDayTo.setDcEndShipmentWorkRelativeWeekNumber((short) row[23]);//TWENTYFIVE
		if(!UploadValidationUtil.isNumeric( row[23].toString()))
		{
			errString.append(UploadConstants.INVALID_DC_END_WRK_WK_NBR);
		}
		orderDayTo.setCreatedUserId(fileObj.getUserId());
		orderDayTo.setLastUptdUserId(fileObj.getUserId());

	}

}
