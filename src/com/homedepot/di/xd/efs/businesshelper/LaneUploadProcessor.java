/*
 * This program is proprietary to The Home Depot and is not to be 
 * reproduced, used, or disclosed without permission of:
 *    
 *  The Home Depot
 *  2455 Paces Ferry Road, N.W.
 *  Atlanta, GA 30339-4053
 * 
 * File Name: LaneUploadProcessor.java 
 * author: The Home Depot Inc
 */
package com.homedepot.di.xd.efs.businesshelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.homedepot.di.xd.efs.constants.EFSConstants;
import com.homedepot.di.xd.efs.constants.UploadConstants;
import com.homedepot.di.xd.efs.dao.LaneDAO;
import com.homedepot.di.xd.efs.exception.DataOperationException;
import com.homedepot.di.xd.efs.exception.EFSException;
import com.homedepot.di.xd.efs.to.InputFileTO;
import com.homedepot.di.xd.efs.to.LaneLoadGroupTO;
import com.homedepot.di.xd.efs.to.LaneLoadGroupVendorTO;
import com.homedepot.di.xd.efs.to.LaneTO;
import com.homedepot.di.xd.efs.to.ResponseTO;
import com.homedepot.di.xd.efs.util.UploadValidationUtil;

public class LaneUploadProcessor implements IUploadProcessor {

	private static final Logger LOGGER = Logger
			.getLogger(LaneUploadProcessor.class);
	
	@Override
	public void processFile(InputFileTO fileObj, ResponseTO responseTO)
			throws IOException, EFSException {
		processValidHeaderDataForLane(fileObj, responseTO);
	}

	/**
	 * This method is to validate the uploaded file headers
	 */
	public int validateHeader(String[] uploadedHeader) {
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug( UploadConstants.ENTERING_METHOD + "validateHeader");
		}
		
		int hdrValidationCode = UploadValidationUtil.validateHeaderFields(uploadedHeader,
				UploadConstants.getShippingLaneHeader());
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Lane Upload header validation return code is "+hdrValidationCode);
			LOGGER.debug( UploadConstants.EXIT_METHOD + "validateHeader");
		}
		return hdrValidationCode;
	}


	/**
	 * Method will find the valid & invalid records from the file and
	 * Insert/update the records based on the inputs Invalid records will be
	 * mailed to user
	 * 
	 * @param file
	 */
	public void processValidHeaderDataForLane(InputFileTO fileObj,
			ResponseTO responseTO) throws EFSException{
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug( UploadConstants.ENTERING_METHOD + "processValidHeaderDataForLane");
		}
		int totalNoOfRecordProcessed = 0;
		//valid 
		List<LaneTO> validLaneTOList = new ArrayList<>();
		
		//invalid
		List<LaneTO> inValidLaneTOList = new ArrayList<>();
		
		Map<Integer, Object[]> fileContents = null;
		
		List<String> vndrDestKeyList = new ArrayList<String>();
		int duplicateRecords = 0;
		
		try{
			
			StringBuilder errString = null;
			int recCurPosition = 0, count = 0, totalNoOfRecords = 0;
			
			//Read the file content
			fileContents = fileObj.getFileContents();
			
			List<String> vendorNbrList = new ArrayList<>();
			List<String> locationNbrList = new ArrayList<>();
			//List<String> dcNmbrList = new ArrayList<String>();
			
			// Read each record in the file and validate
			for (Map.Entry<Integer, Object[]> entry : fileContents.entrySet()){
				
				if(entry.getKey() == 0){
					continue;
				}
				
				Object[] row = entry.getValue();
				
				/*if(EFSUtil.isRowEmpty(row)){
					continue;
				}*/
				
				errString = new StringBuilder();
				LaneTO laneTO = new LaneTO();
				LaneLoadGroupTO laneLoadGrpTO = new LaneLoadGroupTO();
				LaneLoadGroupVendorTO laneVndrTO = new LaneLoadGroupVendorTO();
				
				//Setting Uploaded User Id
				laneTO.setCreatedUserId(fileObj.getUserId());
				laneTO.setLastUptdUserId(fileObj.getUserId());
				laneLoadGrpTO.setCreatedUserId(fileObj.getUserId());
				laneLoadGrpTO.setLastUptdUserId(fileObj.getUserId());
				laneVndrTO.setCreatedUserId(fileObj.getUserId());
				laneVndrTO.setLastUptdUserId(fileObj.getUserId());
				
				/**
				 * If Origin ID is invalid add it to error string
				 */
				laneTO.setOriginId(row[0].toString());
				if (!UploadValidationUtil.isOriginValid(row[0].toString())) {
					errString.append(UploadConstants.INVALID_ORIGIN);
				}
				
				/**
				 * If Destination ID is invalid add it to error string
				 */
				laneTO.setDestinationId(row[1].toString());//the method should be setDestinationId
				if (!UploadValidationUtil.isDestinationValid(row[1].toString())) {
					errString.append(UploadConstants.INVALID_DESTINATION);
				}
				
				// Check if Lane name exist
				laneTO.setShppingLaneName(row[2].toString());
				if (!UploadValidationUtil.isValidStringLength(row[2].toString(), 100)) {
					errString.append(UploadConstants.INVALID_LANE_NAME);
				} 
				
				// Check if vendor number exist
				laneVndrTO.setMvndrNbr(row[3].toString());
				if(!UploadValidationUtil.isVendorValid(row[3].toString() )){
					errString.append(UploadConstants.INVALID_VNDR);
				}
				
				//Check to see if Different group name is provided if not considered it as DEFAULT
				if(UploadValidationUtil.isEmptyString(row[7].toString())){
					laneLoadGrpTO.setLaneLoadGroupName(UploadConstants.DEFAULT);
				}else{
					laneLoadGrpTO.setLaneLoadGroupName(row[7].toString().trim().toUpperCase());
				}
				
				//check if primary location exist
				if(UploadConstants.YES.equalsIgnoreCase(row[UploadConstants.FIVE].toString().trim()))
					laneVndrTO.setPrimaryLocation(UploadConstants.YES);
				else
					laneVndrTO.setPrimaryLocation(UploadConstants.NO);
				
				// Check if Vendor Effective begin date exist
				// if exist then convert that into Util.Date format
				if(row[5] == null){
					errString.append(UploadConstants.INVALID_VENDOR_EFF_BGN_DT);
				}else{
					laneVndrTO.setEffectiveBeginDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[5]));
				}
					
				
				// Check if Vendor Effective end date exist
				// if exist then convert that into Util.Date format
				if(row[6] == null){
					errString.append(UploadConstants.INVALID_VENDOR_EFF_END_DT);
				}else{
					laneVndrTO.setEffectiveEndDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[6]));
				}
				
				// Check if Load Group Effective begin date exist
				// if exist then convert that into Util.Date format
				if(row[8] == null){
					errString.append(UploadConstants.INVALID_LOAD_GRP_EFF_BGN_DT);
				}else{
					laneLoadGrpTO.setEffectiveBeginDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[8]));
				}
				
				// Check if Load Group Effective end date exist
				// if exist then convert that into Util.Date format
				if(row[9] == null){
					errString.append(UploadConstants.INVALID_LOAD_GRP_EFF_END_DT);
				}else{
					laneLoadGrpTO.setEffectiveEndDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[9]));
				}
				
				// check if Lane Active flag exists if doesn't exist set it as N
				if(UploadConstants.YES.equalsIgnoreCase(row[10].toString())){
					laneTO.setActiveFlag(true);
				}else{
					laneTO.setActiveFlag(false);
				}
				
				/*
				 * checking for vendor + destination combination
				 */
				String originDestVendor = row[0].toString()+"_"+row[1].toString()+"_"+row[3].toString();
				String primaryLocation = laneVndrTO.getPrimaryLocation();
				if(UploadConstants.YES.equalsIgnoreCase(primaryLocation)){
					if(vndrDestKeyList.contains(originDestVendor)){
						errString.append(UploadConstants.PRIMARY_LANE_ALREADY_EXIST);
					}
			//		vndrDestKeyList.add(vndrDest);
				}
				
				laneLoadGrpTO.setLaneLoadGroupVendorTo(laneVndrTO);
				laneTO.setLaneLoadGroupTo(laneLoadGrpTO);
				
				/*
				 * If there is no error message found proceed with 
				 * data processing logic
				 */
				if (errString.toString().isEmpty()) {
					/*
					 *  Adding the vendor number, Origin, destination 
					 *  to list for later database validation
					 */
					locationNbrList.add(row[0].toString());
					locationNbrList.add(row[1].toString());
					//dcNmbrList.add(row[UploadConstants.SECOND].toString());
					vendorNbrList.add(row[3].toString());
					
					validLaneTOList.add(laneTO);
					vndrDestKeyList.add(originDestVendor);
					
					if(LOGGER.isDebugEnabled()){
						LOGGER.debug( "Adding to Valid List : " + laneTO.toString());
					}
				} 
				else {
					laneTO.setErrorString(errString.toString());
					inValidLaneTOList.add(laneTO);
					responseTO.setErrorMsg(errString.toString());
					if(LOGGER.isDebugEnabled()){
						LOGGER.debug( "Adding to InValid List : " + laneTO.toString());
					}
				}
				
				totalNoOfRecords++;

			} //end of While()
			
			//Temporary list to iterate the main list and perform DB operation
			List<LaneTO> laneTOList;
			
			if(validLaneTOList !=null && validLaneTOList.size()>0 ){
				
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug( "Size of validLaneTOList : " + validLaneTOList.size());
				}
				
				/*
				 * To remove the TO's with same vndr + origin + destination + grp name + Primary Location flag
				 */
				
				Set<LaneTO> validLaneSet = new TreeSet<LaneTO>();
				validLaneSet.addAll(validLaneTOList);
				if(validLaneTOList.size() > validLaneSet.size()) {
					duplicateRecords = validLaneTOList.size() - validLaneSet.size();
					if(LOGGER.isDebugEnabled()){
						LOGGER.debug( "No of Duplicate records present : " + duplicateRecords);
					}
				}
				validLaneTOList = new ArrayList<LaneTO>(validLaneSet);
				
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug( "ValidTO list after removing duplicates size : " + validLaneTOList.size() +  "list values :" + validLaneTOList.toString());
				}
				
				// Validate the MVENDOR Exists
				int totalVendorSize = vendorNbrList.size();
				List<String> inputVendorList;
				Map<String, Short> validVendorNbrMap = new HashMap<>();
				for(int i = 0;i < totalVendorSize; i += UploadConstants.MAX_SEL_MULTIPLE) {
					inputVendorList = new ArrayList<>();
					inputVendorList.addAll(vendorNbrList.subList(i, Math.min(i + UploadConstants.MAX_SEL_MULTIPLE, totalVendorSize)));
					LaneDAO.validateMvendorAndGatherInformation(inputVendorList, validVendorNbrMap);
				}
				
				//get the valid EFS Location id for all the locations and populate them in validOriginDestinationMap
				int totalLocationSize = locationNbrList.size();
				Map<String, Integer> validOriginDestinationMap = new HashMap<>();
				List<String> inputLocationList;
				
				//get the valid vendor and destination combination from the db which are present
				//Remove the values from the validLaneList which are present in DB
				totalVendorSize = vendorNbrList.size();
				int counter = totalLocationSize >= totalVendorSize? totalLocationSize:totalVendorSize;
				List<String> vndrDestinationList = new ArrayList<>();
				for(int i=0, j=0; j < counter ; 
						i += (UploadConstants.MAX_SEL_MULTIPLE/2), j += UploadConstants.MAX_SEL_MULTIPLE )
				{
					inputVendorList = new ArrayList<String>();
					inputLocationList = new ArrayList<String>();
					inputVendorList.addAll(vendorNbrList.subList(i, Math.min(i + (UploadConstants.MAX_SEL_MULTIPLE / 2), totalVendorSize)));
					inputLocationList.addAll(locationNbrList.subList(j, Math.min(j + UploadConstants.MAX_SEL_MULTIPLE, totalLocationSize)));
					
					LaneDAO.getValidEFSLocationIds(inputLocationList, validOriginDestinationMap);
					LaneDAO.validateVendorDestinationInfo(inputVendorList, inputLocationList, vndrDestinationList);
				}
				/*
				 *  Below logic is to cut the input list size to 500 
				 *  so the DB insert can be handled without any issues
				 */
				
				for (int readCount = 0; readCount < validLaneTOList.size(); readCount = readCount + UploadConstants.MAX_SEL_MULTIPLE) {
					List<LaneTO> tempTOList = new ArrayList<>();
					tempTOList = validLaneTOList.subList(readCount, Math.min(readCount + UploadConstants.MAX_SEL_MULTIPLE, validLaneTOList.size()));
					laneTOList = new ArrayList<>();
					
					// Iterating the segments
					for(int index = 0; index < tempTOList.size(); index++){
						
						LaneTO laneTO = validLaneTOList.get(index);
						
						// check for valid origin
						if(validOriginDestinationMap.get(laneTO.getOriginId()) == null ){
							laneTO.setErrorString(UploadConstants.INVALID_ORIGIN);
							inValidLaneTOList.add(laneTO);
							continue;
						}else{
							laneTO.setOriginLocId(validOriginDestinationMap.get(laneTO.getOriginId()));
						}
						
						// check for valid destination 
						if(validOriginDestinationMap.get(laneTO.getDestinationId()) == null ){
							laneTO.setErrorString(UploadConstants.INVALID_DESTINATION);
							inValidLaneTOList.add(laneTO);
							continue;
						}else{
							laneTO.setDestinationLocId(validOriginDestinationMap.get(laneTO.getDestinationId()));
						}
						
						//Check to see the vendor number is valid 
						if(laneTO.getLaneLoadGroupTo()!=null){
							if(laneTO.getLaneLoadGroupTo().getLaneLoadGroupVendorTo()!=null ){
								String vendorNbr = laneTO.getLaneLoadGroupTo().getLaneLoadGroupVendorTo().getMvndrNbr(); 
								if(validVendorNbrMap.get(vendorNbr)!=null){
									short deptNbr = validVendorNbrMap.get(vendorNbr);
									laneTO.getLaneLoadGroupTo().getLaneLoadGroupVendorTo().setDepartmentNbr(deptNbr);
									laneTO.getLaneLoadGroupTo().getLaneLoadGroupVendorTo().setMerchendiseBaseCode(UploadConstants.MER_BASE_CODE);
								}else{
									laneTO.setErrorString(UploadConstants.INVALID_VNDR);
									inValidLaneTOList.add(laneTO);
									continue;
								}
							}
						}
						
						//Check to see if Primary Lane for Destination & Vendor already exist in DB.
						String vendorDestination = laneTO.getLaneLoadGroupTo().getLaneLoadGroupVendorTo().getMvndrNbr()
								+"_"+ laneTO.getOriginLocId() +"_" + laneTO.getDestinationLocId();
						LOGGER.debug(" Identify Primary Lane - Key : "+ vendorDestination);
						if(vndrDestinationList.contains(vendorDestination)){
							if(UploadConstants.YES.equalsIgnoreCase(laneTO.getLaneLoadGroupTo().getLaneLoadGroupVendorTo().getPrimaryLocation())){
								laneTO.setErrorString(UploadConstants.PRIMARY_LANE_ALREADY_EXIST);
								inValidLaneTOList.add(laneTO);
							}
						}
						//Adding the TO to valid list so it can be populated to table
						laneTOList.add(laneTO);
					
					}
					
					//Removing the error TOs from the valid list
					laneTOList.removeAll(inValidLaneTOList);
					
					// To insert the valid records from uploaded file
					LaneDAO.insertRecords(laneTOList, inValidLaneTOList);
				}
				
				totalNoOfRecordProcessed = totalNoOfRecords - inValidLaneTOList.size() - duplicateRecords;
			}
			
			responseTO.setReturnMessage(totalNoOfRecordProcessed +"/"+totalNoOfRecords 
					+" successful records uploaded");
			LOGGER.debug(totalNoOfRecordProcessed +"/"+totalNoOfRecords 
					+" successful records uploaded");
			//Sending back the invalid TOs as response.
			if(inValidLaneTOList.size() > 0 ){
				/*responseTO.setReturnMessage("No of Records Processed is " + totalNoOfRecordProcessed +
						" and No of Records errored out is " + inValidLaneTOList.size());
				responseTO.setResults(new ArrayList<LaneTO>(inValidLaneTOList));
				*/
				LOGGER.debug("No of Records Processed is " + totalNoOfRecordProcessed +
						" and No of Records errored out is " + inValidLaneTOList.size());
				LOGGER.debug("Invalid List : "+inValidLaneTOList.toString());
			}
			if(duplicateRecords > 0 ){
				LOGGER.debug("No of Records Processed is " + totalNoOfRecordProcessed +
						" and No of Records errored out due to Duplication is " + duplicateRecords);
			}
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug( UploadConstants.EXIT_METHOD + "processValidHeaderDataForLane");
			}
		}catch(DataOperationException e){
			throw new EFSException(
					EFSConstants.EXCEPTION_OCCURED, e);
		}
	}
}
