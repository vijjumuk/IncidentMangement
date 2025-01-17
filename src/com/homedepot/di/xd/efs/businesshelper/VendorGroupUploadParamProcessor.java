package com.homedepot.di.xd.efs.businesshelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.homedepot.di.xd.efs.constants.UploadConstants;
import com.homedepot.di.xd.efs.dao.VendorGroupParamUploadDAO;
import com.homedepot.di.xd.efs.exception.DataOperationException;
import com.homedepot.di.xd.efs.exception.EFSException;
import com.homedepot.di.xd.efs.to.InputFileTO;
import com.homedepot.di.xd.efs.to.ParameterTO;
import com.homedepot.di.xd.efs.to.ResponseTO;
import com.homedepot.di.xd.efs.to.VendorGroupParamTO;
import com.homedepot.di.xd.efs.util.EFSUtil;
import com.homedepot.di.xd.efs.util.UploadValidationUtil;
import com.homedepot.di.xd.efs.util.ValidationUtil;

/**
 * @author 533705
 * @version $Revision: 1.0 $
 */
public class VendorGroupUploadParamProcessor implements IUploadProcessor {

	
	private static final Logger LOGGER = Logger
			.getLogger(VendorGroupUploadParamProcessor.class);

	/**
	 * Method processFile.
	 * @param fileObj InputFileTO
	 * @param responseTO ResponseTO
	
	
	
	 * @throws IOException * @throws EFSException * @see com.homedepot.di.xd.efs.businesshelper.IUploadProcessor#processFile(InputFileTO, ResponseTO) */
	@Override
	public void processFile(InputFileTO fileObj, ResponseTO responseTO)
			throws IOException, EFSException {
		processVendorParamData(fileObj ,responseTO);
		
	}

	/**
	 * Method processVendorParamData.
	 * @param fileObj InputFileTO
	 * @param responseTO ResponseTO
	
	 * @throws EFSException */
	public void processVendorParamData(InputFileTO fileObj,
			ResponseTO responseTO) throws EFSException {
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "processVendorParamData" );
		}
		
		// to store error String.
		StringBuilder errString = null;

		
		// List to set the param code details
		List<ParameterTO> paramToList = new ArrayList<ParameterTO>();
		
		// List to set the param code details
		List<VendorGroupParamTO> validVendorParmToList = new ArrayList<VendorGroupParamTO>();
		
		// List to set the param code details
		List<VendorGroupParamTO> invalidVendorParmToList = new ArrayList<VendorGroupParamTO>();
		
		// Key to find duplicate records.
		List<String> uniqueLoadGroupDetails = new ArrayList<String>();

		int rowNumber = 1;
		
		StringBuilder key = new StringBuilder();

		Map<Integer, Object[]> fileContents = null;

		// Read the file content
		fileContents = fileObj.getFileContents();
		try {
			// Read each record in the file and validate
			for (Map.Entry<Integer, Object[]> entry : fileContents.entrySet()) {

				if (entry.getKey() == 0) {
					continue;
				}

				// String[] row = java.util.Arrays.copyOf(entry.getValue(),
				// entry.getValue().length, String[].class);

				Object[] row = entry.getValue();

				rowNumber++;

				VendorGroupParamTO vendorGroupParamTO = new VendorGroupParamTO();

				// to store error messages.
				errString = new StringBuilder();

				// Set row number, create and update user id.
				vendorGroupParamTO.setRowNumber(rowNumber);
				vendorGroupParamTO.setCreateUserId(fileObj.getUserId());
				vendorGroupParamTO.setLastUpdatedUserId(fileObj.getUserId());

				// Validate Vendor , Shipping Lane , parameter details
				validateVendorDetails(row, errString, vendorGroupParamTO,
						paramToList);

				// location Number - Destination - load Group Name
				if (EFSUtil.isEmpty(errString.toString())) {

					key = key.append(row[UploadConstants.FIRST].toString());

					if (vendorGroupParamTO.getOrigin() != null
							& vendorGroupParamTO.getDestination() != null) {
						key.append('-')
								.append(row[UploadConstants.SECOND].toString())
								.append('-')
								.append(row[UploadConstants.THIRD].toString());
					}

					if (uniqueLoadGroupDetails.contains(key.toString())
							&& key != null) {
						errString.append(UploadConstants.DUPLICATE_RECORD);
						errString.append(",");
					} else {
						// perform Datebase validation for the each record
						performDataBaseValidation(vendorGroupParamTO,
								validVendorParmToList, invalidVendorParmToList,
								errString);
						uniqueLoadGroupDetails.add(key.toString());
					}

					key.setLength(0);

				} else {
					invalidVendorParmToList.add(vendorGroupParamTO);
				}

			}
			if (!EFSUtil.isEmpty(validVendorParmToList)) {
				VendorGroupParamUploadDAO.createVendorLoadGroupParams(
						validVendorParmToList, fileObj.getUserId());
			}
			
			responseTO.setReturnMessage(validVendorParmToList.size()+ "/" + (rowNumber-1) + " successful records uploaded.");
		
		} catch (DataOperationException doe) {
			throw new EFSException(doe.getMessage());
		}
					
	}

	/**
	 * Method validateVendorDetails.
	 * @param row Object[]
	 * @param errString StringBuilder
	 * @param vendorParamTO VendorGroupParamTO
	 * @param vendorParmToList List<ParameterTO>
	
	 * @throws EFSException */
	public void validateVendorDetails(Object[] row, StringBuilder errString,
			VendorGroupParamTO vendorParamTO, List<ParameterTO> vendorParmToList) throws EFSException {
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "validateVendorDetails" );
		}
		boolean isValidDate =  true;
		
		if(!(UploadValidationUtil.isVendorValid(row[UploadConstants.FIRST].toString().trim()))) {
			errString.append(UploadConstants.INVALID_VNDR);
			errString.append(',');
		} else {
			vendorParamTO.setVendorNumber(row[UploadConstants.FIRST].toString().trim());
		}
		
		if (row[UploadConstants.SECOND] == null || row[UploadConstants.SECOND].toString().trim().isEmpty() ){
			vendorParamTO.setOrigin(null);
		} else if(!(UploadValidationUtil.isOriginValid(row[UploadConstants.SECOND].toString().trim()))) {
			errString.append(UploadConstants.INVALID_ORIGIN);
			errString.append(',');
		} else {
			vendorParamTO.setOrigin(row[UploadConstants.SECOND].toString().trim());
		}
		
		
		if (row[UploadConstants.THIRD] == null || row[UploadConstants.THIRD].toString().trim().isEmpty() ){
			vendorParamTO.setDestination(null);
		} else if(!(UploadValidationUtil.isDestinationValid(row[UploadConstants.THIRD].toString().trim()))) {
			errString.append(UploadConstants.INVALID_DESTINATION);
			errString.append(',');
		} else {
			vendorParamTO.setDestination(row[UploadConstants.THIRD].toString().trim());
		}
		
		// Set the Default Vendor flag, default to Y.
		if (ValidationUtil.isEmpty(row[UploadConstants.FOURTH].toString().trim())) {
			vendorParamTO.setDefaultFlag(UploadConstants.YES);
		}
		else if (ValidationUtil.isFlagValid(row[UploadConstants.FOURTH]
				.toString().trim())) {
			vendorParamTO
					.setDefaultFlag(row[UploadConstants.FOURTH].toString().trim().toUpperCase());
		} else {
			errString.append(UploadConstants.INVALID_DEFAULT_FLG);
			errString.append(',');
		}
		
		if (!(errString != null) || errString.toString().isEmpty()) {
			if (vendorParamTO.getDefaultFlag().equalsIgnoreCase(
					UploadConstants.YES)
					&& (vendorParamTO.getOrigin() != null && vendorParamTO
							.getDestination() != null)) {
				errString
						.append(UploadConstants.INVALID_DEFAULT_MVNDR_PARAMETER);
				errString.append(',');
			} else if (vendorParamTO.getDefaultFlag().equalsIgnoreCase(
					UploadConstants.NO)
					&& !(vendorParamTO.getOrigin() != null && vendorParamTO
							.getDestination() != null)) {
				errString
						.append(UploadConstants.INVALID_DEFAULT_MVNDR_PARAMETER);
				errString.append(',');
			}
		}
		
		
		// Validate Effective begin & Effective End date
		vendorParamTO.setEffectiveBeginDate(row[UploadConstants.NINE]
				.toString());

		if (!(ValidationUtil.isDateValid((vendorParamTO.getEffectiveBeginDate())))) {

			errString.append(UploadConstants.INVALID_EFFECTIVE_BEGIN_DATE);
			errString.append(',');
			isValidDate = false;
		}

		vendorParamTO
				.setEffectiveEndDate(row[UploadConstants.TEN].toString());
		if (!(ValidationUtil.isDateValid((vendorParamTO.getEffectiveEndDate())))) {

			errString.append(UploadConstants.INVALID_EFFECTIVE_END_DATE);
			errString.append(',');
			isValidDate = false;
		}
		if (isValidDate
				&& (ValidationUtil.convertStringToDate(vendorParamTO
						.getEffectiveEndDate()).compareTo(ValidationUtil
						.convertStringToDate(vendorParamTO
								.getEffectiveBeginDate()))) < 0) {
			errString
					.append(UploadConstants.EFFETIVE_BEGIN_DATE_GREATER_THAN_END_DATE);
			errString.append(',');
		}
		
		validateVendorParameterDetails(row, errString, vendorParmToList);
		vendorParamTO.setLoadParamList(vendorParmToList);
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(UploadConstants.EXIT_METHOD + "validateVendorDetails" );
		}
	}

	/**
	 * Method validateVendorParameterDetails.
	 * @param row Object[]
	 * @param errString StringBuilder
	 * @param vendorParmToList List<ParameterTO>
	 */
	public void validateVendorParameterDetails(Object[] row,
			StringBuilder errString, List<ParameterTO> vendorParmToList) {
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "validateVendorParameterDetails" );
		}
		
		if(ValidationUtil.isMaxPullFwdDaysValid(row[UploadConstants.FIVE].toString().trim())) {
			vendorParmToList.add(setLoadGroupParmValues(UploadConstants.MAX_PULL_FWD_DAYS, row[UploadConstants.FIVE].toString().trim()));
		} else {
			errString
			.append(UploadConstants.INVALID_MAX_PULL_FWD_DAYS);
			errString.append(',');
		}
		
		if(ValidationUtil.isFlagValid(row[UploadConstants.SIX].toString().trim())) {
			vendorParmToList.add(setLoadGroupParmValues(UploadConstants.TL_ROUND_UP, row[UploadConstants.SIX].toString().trim()));
		} else {
			errString
			.append(UploadConstants.INVALID_TL_ROUND_UP);
			errString.append(',');
		}
		
		if(ValidationUtil.isFlagValid(row[UploadConstants.SEVEN].toString().trim())) {
			vendorParmToList.add(setLoadGroupParmValues(UploadConstants.TL_ROUND_DOWN, row[UploadConstants.SEVEN].toString().trim()));
		} else {
			errString
			.append(UploadConstants.INVALID_TL_ROUND_DOWN);
			errString.append(',');
		}
		
		if(ValidationUtil.isOUTLThresholdValid(row[UploadConstants.EIGHT].toString().trim())) {
			vendorParmToList.add(setLoadGroupParmValues(UploadConstants.OUTL_THRESHOLD, row[UploadConstants.EIGHT].toString().trim()));
		} else {
			errString
			.append(UploadConstants.INVALID_OULT_THRESHOLD);
			errString.append(',');
		}
		
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(UploadConstants.EXIT_METHOD + "validateVendorParameterDetails" );
		}
	}
	
	/**
	 * Set the Parameter code and values for the load group
	 * 
	 * @param parmName
	 * @param parmValue
	
	
	 * @return ParameterTO */
	private ParameterTO setLoadGroupParmValues(String parmName,
			String parmValue) {
		
		Map<String, Short> parmNameAndCode = new HashMap<String, Short>();
		parmNameAndCode.put(UploadConstants.MAX_PULL_FWD_DAYS, (short) 4);
		parmNameAndCode.put(UploadConstants.TL_ROUND_UP, (short)1);
		parmNameAndCode.put(UploadConstants.TL_ROUND_DOWN, (short)2);
		parmNameAndCode.put(UploadConstants.OUTL_THRESHOLD, (short)7);
		
		
		ParameterTO vndrGrpParm = new ParameterTO();
		if (parmName.equalsIgnoreCase(UploadConstants.MAX_PULL_FWD_DAYS)) {
			
			vndrGrpParm.setLoadGroupParamCode(parmNameAndCode.get(UploadConstants.MAX_PULL_FWD_DAYS));
			vndrGrpParm.setIntValue(Integer.valueOf(parmValue));
		} else if (parmName.equalsIgnoreCase(UploadConstants.TL_ROUND_UP)) {
		
			vndrGrpParm.setLoadGroupParamCode(parmNameAndCode.get(UploadConstants.TL_ROUND_UP));
			vndrGrpParm.setCharValue(parmValue);
		} else if (parmName.equalsIgnoreCase(UploadConstants.TL_ROUND_DOWN)) {
			
			vndrGrpParm.setLoadGroupParamCode(parmNameAndCode.get(UploadConstants.TL_ROUND_DOWN));
			vndrGrpParm.setCharValue(parmValue);
		} else if (parmName.equalsIgnoreCase(UploadConstants.OUTL_THRESHOLD)) {
		
			vndrGrpParm.setLoadGroupParamCode(parmNameAndCode.get(UploadConstants.OUTL_THRESHOLD));
			vndrGrpParm.setDecimalValue(new BigDecimal(parmValue));
		} else {
			vndrGrpParm = null;
		}
		return vndrGrpParm;
	}

	/**
	 * Method performDataBaseValidation.
	 * @param vendorGroupParamTO VendorGroupParamTO
	 * @param validVendorParmToList List<VendorGroupParamTO>
	 * @param invalidVendorParmToList List<VendorGroupParamTO>
	 * @param errString StringBuilder
	 * @throws EFSException
	 * @throws DataOperationException 
	 */
	public void performDataBaseValidation(VendorGroupParamTO vendorGroupParamTO,
			List<VendorGroupParamTO> validVendorParmToList,
			List<VendorGroupParamTO> invalidVendorParmToList,
			StringBuilder errString) throws EFSException {
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "performDataBaseValidation" );
		}
		List<Integer> vendorGroupIdList = null;
		VendorGroupParamTO vendorGroupTO = null;
		try {
		if (vendorGroupParamTO.getDefaultFlag().equalsIgnoreCase(UploadConstants.NO)) {
				VendorGroupParamUploadDAO.getActiveLocationIdList(
						vendorGroupParamTO, errString);
				if (EFSUtil.isEmpty(errString.toString())) {

					VendorGroupParamUploadDAO.getLoadGroupIdList(
							vendorGroupParamTO, errString);

					if (EFSUtil.isEmpty(errString.toString())) {

						vendorGroupIdList = VendorGroupParamUploadDAO.getLoadGroupVendorId(
								vendorGroupParamTO, errString,false);
						if (EFSUtil.isEmpty(errString.toString())) {
							vendorGroupParamTO.setVendorGroupId(vendorGroupIdList.get(0));
							validVendorParmToList.add(vendorGroupParamTO);
						} else {
							invalidVendorParmToList.add(vendorGroupParamTO);
						}
					} else {
						invalidVendorParmToList.add(vendorGroupParamTO);
					}
				} else {

					invalidVendorParmToList.add(vendorGroupParamTO);
				}
		} else {
			vendorGroupIdList = VendorGroupParamUploadDAO.getLoadGroupVendorId(
					vendorGroupParamTO, errString,true);
			for (Integer groupId : vendorGroupIdList) {
				vendorGroupTO = new  VendorGroupParamTO();
				vendorGroupTO = vendorGroupParamTO;
				vendorGroupTO.setVendorGroupId(groupId);
				validVendorParmToList.add(vendorGroupTO);
			}
		}
		} catch (DataOperationException doe) {
			throw new EFSException(doe.getMessage());
		}
		
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.EXIT_METHOD + "performDataBaseValidation" );
		}
	}
	
	
}
