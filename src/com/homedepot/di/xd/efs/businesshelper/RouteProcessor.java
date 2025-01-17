package com.homedepot.di.xd.efs.businesshelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.homedepot.di.xd.efs.constants.EFSConstants;
import com.homedepot.di.xd.efs.constants.UploadConstants;
import com.homedepot.di.xd.efs.dao.RouteDAO;
import com.homedepot.di.xd.efs.exception.EFSException;
import com.homedepot.di.xd.efs.to.InputFileTO;
import com.homedepot.di.xd.efs.to.ResponseTO;
import com.homedepot.di.xd.efs.to.RouteTO;
import com.homedepot.di.xd.efs.util.UploadValidationUtil;

public class RouteProcessor implements IUploadProcessor{
	
	private static final Logger LOGGER = Logger
			.getLogger(LaneUploadProcessor.class);

	@Override
	public void processFile(InputFileTO fileObj, ResponseTO responseTO)
			throws IOException, EFSException {
		// TODO Auto-generated method stub
		processValidHeaderDataForRoute(fileObj,responseTO);
		
	}
	
	public int validateHeader(String[] uploadedHeader){
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug( UploadConstants.ENTERING_METHOD + "validateHeader");
		}
		
		int hdrValidationCode = UploadValidationUtil.validateHeaderFields(uploadedHeader,
				UploadConstants.getRouteHeader());
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Route Upload header validation return code is "+hdrValidationCode);
			LOGGER.debug( UploadConstants.EXIT_METHOD + "validateHeader");
		}
		return hdrValidationCode;
		
	}
	
	public void processValidHeaderDataForRoute(InputFileTO fileObj, ResponseTO responseTO) throws EFSException{
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug( UploadConstants.ENTERING_METHOD + "processValidHeaderDataForRoute");
		}
		
//		int totalNoOfRecordProcessed = 0;
		//valid list
		List <RouteTO> validRouteTOList = new ArrayList<>();
		
		//invald list
		List <RouteTO> invalidRouteTOList = new ArrayList<>();
		
		Map<Integer, Object[]> fileContents = null;
		
//		Date effDate = null;
		
		try{
			StringBuilder errString = null;
			
//			int reCurPosition = 0, count = 0, 
			int totalNoOfRecords=0;
			
			//Reads the file content
			fileContents = fileObj.getFileContents();
			/*final CsvReader reader = new CsvReader(new BufferedReader(
					new InputStreamReader(fileObj.getInputStream(), "UTF-8")));*/
			
			//skips the first record becouse that is where the header info is stored.
			//reader.skipRecord();
			
			//Read each record in the file and validates it.
			//while (reader.readRecord()){

			for (Map.Entry<Integer, Object[]> entry : fileContents.entrySet()){
				//To skip header line
				if(entry.getKey() == 0){
					continue;
				}

				Object[] row = entry.getValue();
				
				/*if(EFSUtil.isRowEmpty(row)){
					continue;
				}*/
				
				errString = new StringBuilder();
				
				RouteTO routeTO = new RouteTO();
				
				//setsUserIDs in Route TO
				routeTO.setCreatedSysUserId(fileObj.getUserId());
				routeTO.setLastUptdUserId(fileObj.getUserId());
				
							
				//if rteID is invalid add it to error string
				routeTO.setRouteTypeCd(row[0].toString().trim());
				if (!UploadValidationUtil.isRouteTypeCodeValid(row[0].toString().trim())) {
						errString.append(UploadConstants.INVALID_RTE_TYP_CD);
				}
				
				//if transTypeCd is invalid add it to error string
				routeTO.setRouteConfigCd(row[1].toString().trim());
				if (!UploadValidationUtil.isRouteTypeCodeValid(row[1].toString().trim())) {
						errString.append(UploadConstants.INVALID_TRANS_TYP_CD);
				}
				
				//if name is invalid add it to error string
				routeTO.setRouteNm(row[2].toString().trim());
				if (!UploadValidationUtil.isRouteDescriptionValid(row[2].toString().trim())){
						errString.append(UploadConstants.INVALID_ROUTE_NM);
				}
				
				//CARRIER LOGIC WILL GO HERE (if we need them)
				
				
				//eff begin date validation
				/*if(UploadValidationUtil.isEmptyString(row[UploadConstants.FIVE].toString())){
					errString.append(UploadConstants.INVALID_ROUTE_EFF_BGN_DT);
				}else{
					effDate = UploadValidationUtil.convertStringToDate(row[UploadConstants.FIVE].toString().trim());
					if(effDate == null){
						errString.append(UploadConstants.INVALID_ROUTE_EFF_BGN_DT);
					}else{
						routeTO.setEffectiveBeginDate(effDate);
					}
					
				}*/
				
				if(row[4] == null){
					errString.append(UploadConstants.INVALID_ROUTE_EFF_BGN_DT);
				}else{
					routeTO.setEffectiveBeginDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[4]));
				}
				
				//eff end date validation
				/*if(UploadValidationUtil.isEmptyString(row[UploadConstants.SIX].toString())){
					errString.append(UploadConstants.INVALID_ROUTE_EFF_END_DT);
				}else{
					effDate = UploadValidationUtil.convertStringToDate(row[UploadConstants.SIX].toString().trim());
					if(effDate == null){
						errString.append(UploadConstants.INVALID_ROUTE_EFF_END_DT);
					}else{
						routeTO.setEffectiveEndDate(effDate);
					}
					
				}*/
				
				if(row[5] == null){
					errString.append(UploadConstants.INVALID_ROUTE_EFF_END_DT);
				}else{
					routeTO.setEffectiveEndDate(UploadValidationUtil.convertSUtilDateToSqlDate(row[5]));
				}
				
				//if no errors loaded in string proceede with processing
				if (errString.toString().isEmpty()){
					//add to valid list
					validRouteTOList.add(routeTO);
					if(LOGGER.isDebugEnabled()){
						LOGGER.debug( "Adding to Valid List : " + routeTO.toString());
					}
					
				}
				else{
					routeTO.setErrorString(errString.toString());
					invalidRouteTOList.add(routeTO);
					responseTO.setErrorMsg(errString.toString());
					
					if(LOGGER.isDebugEnabled()){
						LOGGER.debug( "Adding to InValid List : " + routeTO.toString());
					}
				}
				
				totalNoOfRecords++;
				
				
				
			}//end of while loop
			
			RouteDAO.insertRecords(validRouteTOList, invalidRouteTOList);
			
			int totalNoOfRecordProcessed = totalNoOfRecords - invalidRouteTOList.size(); 
			
			responseTO.setReturnMessage(totalNoOfRecordProcessed +"/"+totalNoOfRecords 
					+" successful records uploaded");
			
		}catch (Exception e){
			throw new EFSException(EFSConstants.EXCEPTION_OCCURED,e);
		}
		
	}
	
	
	

}
