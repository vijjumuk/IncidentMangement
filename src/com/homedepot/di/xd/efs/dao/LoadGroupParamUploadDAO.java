package com.homedepot.di.xd.efs.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.homedepot.di.xd.efs.constants.UploadConstants;
import com.homedepot.di.xd.efs.exception.EFSException;
import com.homedepot.di.xd.efs.to.LoadGroupParamTO;
import com.homedepot.di.xd.efs.to.ParameterTO;
import com.homedepot.di.xd.efs.util.ValidationUtil;
import com.homedepot.ta.aa.dao.Inputs;
import com.homedepot.ta.aa.dao.Query;
import com.homedepot.ta.aa.dao.Results;
import com.homedepot.ta.aa.dao.ResultsReader;
import com.homedepot.ta.aa.dao.builder.BatchData;
import com.homedepot.ta.aa.dao.builder.DAO;
import com.homedepot.ta.aa.dao.builder.UnitOfWork;
import com.homedepot.ta.aa.dao.exceptions.QueryException;

/**
 * @author 
 * @version $Revision: 1.0 $
 */
public class LoadGroupParamUploadDAO {

	/**
	 * Field LOGGER.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(LoadGroupParamUploadDAO.class);

	// Data source constant
	private static final String SupplyChainDistributionNetwork = "java:comp/env/jdbc/SupplyChainDistributionNetwork.1";

	// Location Column count
	private static final int EFS_LANE_LGRP_PARAM = 11;
	private static final int LGRP_PARAM_UPDATE_COLUMN_COUNT = 10;

	// in list Max size
	private static final int IN_LIST_MAX_SIZE = 500;

	// Insert Query for Lane Load Group Param table
	private static final String INSERT_SHLANE_LGRP_PARM = " INSERT INTO SCHN_SHLANE_LGRP_PARM "
			+ " (SCHN_SHLANE_LGRP_PARM_ID  , "
			+ " CRT_SYSUSR_ID  , "
			+ " CRT_TS  , "
			+ " LAST_UPD_SYSUSR_ID  , "
			+ " LAST_UPD_TS  , "
			+ " SCHN_SHPG_LANE_LOAD_GRP_ID  , "
			+ " LOAD_PLNG_PARM_CD  , "
			+ " EFF_BGN_DT  , "
			+ " EFF_END_DT  , "
			+ " PARM_CHAR_VAL  , "
			+ " PARM_INTG_VAL , "
			+ " PARM_DEC_VAL , "
			+ " PARM_FLG_VAL ) "
			+ " VALUES (? , ? , CURRENT_TIMESTAMP , ? , CURRENT_TIMESTAMP, ? , ? , ? , ? , ? , ? , ? , ? ) ";

	// Query to fetch the Active Location Id
	private static final String READ_ACTIVE_LOCATION_ID = " SELECT XREF.SCHN_LOC_ID, "
			+ " XREF.SRC_LOC_ID  "
			+ " FROM   SCHN_EFS_LOC_XREF XREF, "
			+ " SCHN_EFS_LOC LOC  "
			+ " WHERE  XREF.SCHN_LOC_ID = LOC.SCHN_LOC_ID   "
			+ " AND LOC.ACTV_FLG = 'Y'  ";

	private static final String READ_LOAD_GROUP_ID = "select LGRP.SCHN_SHPG_LANE_LOAD_GRP_ID "
			+ "from SCHN_SHPG_LANE_LOAD_GRP LGRP ,"
			+ "(select schn_shpg_lane_id "
			+ "from schn_shpg_lane "
			+ "where ORIG_SCHN_LOC_ID = ? "
			+ "and SHP_TO_SCHN_LOC_ID = ? "
			+ "and actv_flg='Y') lane "
			+ "where lane.schn_shpg_lane_id = LGRP.schn_shpg_lane_id "
			+ "and LGRP.shpg_lane_load_grp_nm = ? ";

	// Query to fetch the Param code details
	private static final String READ_PARAM_CODE = " SELECT PARM.LOAD_PLNG_PARM_CD AS PARAM_CD, "
			+ " NPARM.PARM_DESC AS PARAM_NM "
			+ " FROM   LOAD_PLNG_PARM_CD PARM , "
			+ " N_LOAD_PLNG_PARM NPARM "
			+ " WHERE  PARM.LOAD_PLNG_PARM_CD = NPARM.LOAD_PLNG_PARM_CD  ";
	
	// Query to fecth the Existing Param details 
		private static final String READ_LOAD_PARAM_DETAILS = " SELECT SCHN_SHLANE_LGRP_PARM_ID, " +
				"       LOAD_PLNG_PARM_CD, " +
				"       SCHN_SHPG_LANE_LOAD_GRP_ID, " +
				"       EFF_BGN_DT, "+
				" 		EFF_END_DT, " +
				"       PARM_CHAR_VAL, " +
				"       PARM_INTG_VAL, " +
				"       PARM_DEC_VAL, " +
				"       PARM_FLG_VAL " +
				" FROM   SCHN_SHLANE_LGRP_PARM " +
				" WHERE  SCHN_SHPG_LANE_LOAD_GRP_ID = ? "+
				" ORDER BY EFF_BGN_DT DESC " ;
		
		//Query to update the Load Group Parameter details
		private static final String UPDATE_LGRP_PARAM = " UPDATE SCHN_SHLANE_LGRP_PARM  " +
				" SET LAST_UPD_SYSUSR_ID = ? , " +
				" LAST_UPD_TS = CURRENT_TIMESTAMP, " +
				" EFF_BGN_DT = ?, " +
				" EFF_END_DT = ?, " +
				" PARM_CHAR_VAL = ?, " +
				" PARM_INTG_VAL = ? , " +
				" PARM_DEC_VAL = ?, " +
				" PARM_FLG_VAL = ? " +
				" WHERE SCHN_SHLANE_LGRP_PARM_ID = ? " +
				" AND LOAD_PLNG_PARM_CD = ? " +
				" AND SCHN_SHPG_LANE_LOAD_GRP_ID = ? " ;
	
	/**
	 * Select query for sequence object for Lane load param id
	 * (SCHN_SHLANE_LGRP_PARM_SEQ).
	 */
	private static final String LANE_LGRP_SEQ_NBR = "SELECT SCHN_SHLANE_LGRP_PARM_SEQ.NEXTVAL FROM DUAL";

	/**
	 * Method createLaneLoadGroupParams.
	 * 
	 * @param loadGroupList
	 *            List<LoadGroupTO>
	 * @param userId
	 *            String
	
	
	 * @return boolean * @throws EFSException */
	public static boolean createLaneLoadGroupParams(
			final List<LoadGroupParamTO> loadGroupList, final String userId)
			throws EFSException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD
					+ "cretaeLaneLoadGroupParams");
		}
		try {
			new UnitOfWork<Boolean>() {

				@Override
				public Boolean runQueries() throws Exception {
					// get next sequence number (SCHN_SHLANE_LGRP_PARM_SEQ) for
					// each valid record
					// And insert into Lane Load group param table
					insertLaneGroupParams(getInsertLoadParamList(loadGroupList,
							userId));

					return true;
				}
			};
		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage(),
					UploadConstants.QUERY_EXCEPTION_CD);
		}
		return true;
	}

	/**
	 * Insert parameter details into SCHN_SHLANE_LGRP_PARM
	 * 
	 * @param laneLoadGrpParamList
	
	
	
	 * @return Success or Failure * @throws QueryException * @throws EFSException **/
	public static boolean insertLaneGroupParams(
			List<ParameterTO> laneLoadGrpParamList)
			throws QueryException, EFSException {
		if (LOGGER.isDebugEnabled()) {

			LOGGER.debug(UploadConstants.ENTERING_METHOD
					+ "insertLaneGroupParams");
		}

		if (laneLoadGrpParamList != null && laneLoadGrpParamList.size() > 0) {

			BatchData laneLoadGrpParamData = new BatchData(EFS_LANE_LGRP_PARAM);
			int row = 0;
			int rowsInserted;
			for (ParameterTO laneLoadGroupParamTO : laneLoadGrpParamList) {
				laneLoadGrpParamData.add(row, 0, getNextLocationSeqId());
				laneLoadGrpParamData.add(row, 1,
						laneLoadGroupParamTO.getCreateUserId());
				laneLoadGrpParamData.add(row, 2,
						laneLoadGroupParamTO.getLastUpdatedUserId());
				laneLoadGrpParamData.add(row, 3,
						laneLoadGroupParamTO.getLoadGroupId());
				laneLoadGrpParamData.add(row, 4,
						laneLoadGroupParamTO.getLoadGroupParamCode());
				laneLoadGrpParamData.add(row, 5,
						laneLoadGroupParamTO.getEffBeginDate());
				laneLoadGrpParamData.add(row, 6,
						laneLoadGroupParamTO.getEffEndDate());
				laneLoadGrpParamData
						.add(row,
								7,
								laneLoadGroupParamTO.getCharValue() != null ? laneLoadGroupParamTO
										.getCharValue() : null);
				laneLoadGrpParamData
						.add(row,
								8,
								laneLoadGroupParamTO.getIntValue() != null ? laneLoadGroupParamTO
										.getIntValue() : null);
				laneLoadGrpParamData
						.add(row,
								9,
								laneLoadGroupParamTO.getDecimalValue() != null ? laneLoadGroupParamTO
										.getDecimalValue() : null);
				laneLoadGrpParamData.add(row, 10, laneLoadGroupParamTO
						.getFlagValue() != null ? laneLoadGroupParamTO
						.getFlagValue().toUpperCase() : null);
				row++;
			}

			rowsInserted = DAO.useJNDI(SupplyChainDistributionNetwork)
					.setSQL(INSERT_SHLANE_LGRP_PARM)
					.batch(laneLoadGrpParamData).length;
			if (laneLoadGrpParamList.size() == rowsInserted) {

				return true;
			}
		}

		return false;
	}

	/**
	 * To get the sequence number for Location ID.
	 * 
	
	
	 * @return integer- sequence next value * @throws EFSException */
	public static int getNextLocationSeqId() throws EFSException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD
					+ "getNextLocationSeqId");
		}
		List<BigDecimal> seqNbrList = new LinkedList<BigDecimal>();
		List<Integer> seqNbr = new ArrayList<Integer>();
		try {
			seqNbrList = DAO.useJNDI(SupplyChainDistributionNetwork)
					.setSQL(LANE_LGRP_SEQ_NBR).list(BigDecimal.class);
			for (BigDecimal id : seqNbrList) {
				seqNbr.add(id.intValue());
			}
		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage(),
					UploadConstants.QUERY_EXCEPTION_CD);
		}
		return seqNbr.get(0);
	}

	/**
	 * To get the sequence number for Lane Load Param Id.
	 * 
	
	
	 * @return integer- sequence next value * @throws EFSException */
	public static int getNextLoadParamSeqId() throws EFSException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD
					+ "getNextLoadParamSeqId");
		}
		List<BigDecimal> seqNbrList = new LinkedList<BigDecimal>();
		List<Integer> seqNbr = new ArrayList<Integer>();
		try {
			seqNbrList = DAO.useJNDI(SupplyChainDistributionNetwork)
					.setSQL(LANE_LGRP_SEQ_NBR).list(BigDecimal.class);
			for (BigDecimal id : seqNbrList) {
				seqNbr.add(id.intValue());
			}
		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage(),
					UploadConstants.QUERY_EXCEPTION_CD);
		}
		return seqNbr.get(0);
	}

	/**
	 * Method will fetch all the active Locations from Location table
	 * 
	 * @param loadGrpTo
	 *            LoadGroupTO
	 * @param error
	 *            StringBuilder
	
	 * @throws EFSException */
	public static void getActiveLocationIdList(final LoadGroupParamTO loadGrpTo,
			final StringBuilder error) throws EFSException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD
					+ "getActiveLoactionIdList");
		}
		try {
			String loactionIdQuery = READ_ACTIVE_LOCATION_ID;
			loactionIdQuery += inListBuilder("SRC_LOC_ID", loadGrpTo,
					"location", IN_LIST_MAX_SIZE);
			DAO.useJNDI(SupplyChainDistributionNetwork).setSQL(loactionIdQuery)
					.debug(true).results(new ResultsReader() {
						@Override
						public void readResults(Results results, Query query,
								Inputs inputs) throws QueryException {
							while (results.next()) {
								if (results.getString("SRC_LOC_ID").equals(
										loadGrpTo.getLocationNbr())) {

									loadGrpTo.setLocationId(results
											.getInt("SCHN_LOC_ID"));
								}

								else if (results.getString("SRC_LOC_ID")
										.equals(loadGrpTo.getDestinationNbr())) {

									loadGrpTo.setDestinationId(results
											.getInt("SCHN_LOC_ID"));
								} else {

									error.append(UploadConstants.LOCATION_ID_DOES_NOT_EXISTS);
								}
							}
						}

					});

		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage());
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.EXIT_METHOD
					+ "getActiveLoactionIdList");
		}

	}

	/**
	 * Method getLoadGroupIdList.
	 * 
	 * @param loadGrpTo
	 *            LoadGroupTO
	 * @param error
	 *            StringBuilder
	
	 * @throws EFSException */
	public static void getLoadGroupIdList(final LoadGroupParamTO loadGrpTo,
			final StringBuilder error) throws EFSException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "getLoadGroupIdList");
		}

		try {

			DAO.useJNDI(SupplyChainDistributionNetwork)
					.setSQL(READ_LOAD_GROUP_ID, loadGrpTo.getLocationId(),
							loadGrpTo.getDestinationId(),
							loadGrpTo.getLoadGrpName().toUpperCase())
					.debug(true).results(new ResultsReader() {
						@Override
						public void readResults(Results results, Query query,
								Inputs inputs) throws QueryException {
							if (results.next()) {

								loadGrpTo.setLoadGrpId(results
										.getInt("SCHN_SHPG_LANE_LOAD_GRP_ID"));
							} else {

								error.append(UploadConstants.LOAD_GROUP_ID_DOES_NOT_EXISTS);
							}

						}
					});
		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION, qe);
		}

	}

	/**
	 * Method will read the existing records for update 
	 * @param loadgrpTo
	
	
	
	 * @param updateLoadGroupParamList List<ParameterTO>
	 * @param insertLoadGroupParamList List<ParameterTO>
	 * @throws EFSException
	 */
	public static void getUpdateLoadParamList(
			final LoadGroupParamTO loadgrpTo,
			final List<ParameterTO> updateLoadGroupParamList,
			final List<ParameterTO> insertLoadGroupParamList)
			throws EFSException {
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "getUpdateLoadParamList");
		}
		try {
			
			final Map<Short,ParameterTO> paramCodeMap = convertListToMap(loadgrpTo);
			//insertLoadGroupParamList = loadgrpTo.getLoadParamList();
			DAO.useJNDI(SupplyChainDistributionNetwork)
					.setSQL(READ_LOAD_PARAM_DETAILS)
					.input(1, loadgrpTo.getLoadGrpId()).debug(true)
					.results(new ResultsReader() {
						@Override
						public void readResults(Results results, Query query,
								Inputs inputs) throws QueryException {
							ParameterTO loadParamGroup = null;
							int count = 0;
							while (results.next()) {
								loadgrpTo.setUpdate(true);
								
								boolean isUpdateRecord = false;
								count++;
								if(count > 10){
									break;
								}
								
								else if(paramCodeMap.containsKey(results.getShort("LOAD_PLNG_PARM_CD"))){
									loadParamGroup = paramCodeMap.get(results.getShort("LOAD_PLNG_PARM_CD"));
									if (loadParamGroup.getCharValue() != null
											&& !(loadParamGroup
													.getCharValue()
													.equalsIgnoreCase(
															results.getString("PARM_CHAR_VAL")))) {
										
										isUpdateRecord = true;
										
										checkDatesForInsert(loadParamGroup, results, insertLoadGroupParamList,updateLoadGroupParamList);
									
									} else if (loadParamGroup.getIntValue() != null
											&& !(loadParamGroup
													.getIntValue().intValue() == results.getInt("PARM_INTG_VAL"))) {

										isUpdateRecord = true;
										checkDatesForInsert(loadParamGroup, results, insertLoadGroupParamList,updateLoadGroupParamList);
									} else if (loadParamGroup.getDecimalValue() != null
											&& !(loadParamGroup
													.getDecimalValue().doubleValue() == results.getBigDecimal("PARM_DEC_VAL").doubleValue())) {

										isUpdateRecord = true;
										checkDatesForInsert(loadParamGroup, results, insertLoadGroupParamList,updateLoadGroupParamList);
									} else if (loadParamGroup.getFlagValue() != null
											&& !(loadParamGroup
													.getFlagValue().equalsIgnoreCase(results.getString("PARM_FLG_VAL")))) {

										isUpdateRecord = true;
										checkDatesForInsert(loadParamGroup, results, insertLoadGroupParamList,updateLoadGroupParamList);
									} 
									
									if (!isUpdateRecord) {
										checkDatesForInsert(loadParamGroup,results,insertLoadGroupParamList,updateLoadGroupParamList);
									}
									
								} 
							}
							
						}
					});
			
		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage());
		}
	}
	
	/**
	 * Method checkDatesForInsert.
	 * @param loadParamGroup ParameterTO
	 * @param results Results
	 * @param insertLoadGroupParamList List<ParameterTO>
	 * @param updateLoadGroupParamList List<ParameterTO>
	 * @throws QueryException
	 */
	private static void checkDatesForInsert(ParameterTO loadParamGroup,
			Results results, List<ParameterTO> insertLoadGroupParamList,
			List<ParameterTO> updateLoadGroupParamList) throws QueryException {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "checkDatesForInsert");
		}
		
		// Uploaded Begin date is overlapping with Existing record - Update Existing record Effective End Date & Insert new record
		if ((loadParamGroup.getEffBeginDate().compareTo(
				results.getDate("EFF_BGN_DT")) > 0) && (loadParamGroup.getEffBeginDate().compareTo(
						results.getDate("EFF_END_DT")) < 0 ) ) {
			ParameterTO paramTO = new ParameterTO();
			setParameter(paramTO, results, loadParamGroup);
			insertLoadGroupParamList.add(loadParamGroup);
			updateLoadGroupParamList.add(paramTO);
		} else 
			// Uploaded Begin date is Greater than Existing record Begin & End date - Go for insert
			if ((loadParamGroup.getEffBeginDate().compareTo( 
				results.getDate("EFF_BGN_DT")) > 0) && (loadParamGroup.getEffBeginDate().compareTo(
						results.getDate("EFF_END_DT")) > 0 )) {
			insertLoadGroupParamList.add(loadParamGroup);
		} else 
			// Uploaded dates are equal then update values only 
			if ((loadParamGroup.getEffBeginDate().compareTo(
				results.getDate("EFF_BGN_DT"))) == 0 && (loadParamGroup.getEffEndDate().compareTo(
						results.getDate("EFF_END_DT")) == 0)) {
			loadParamGroup.setParamId(results.getInt("SCHN_SHLANE_LGRP_PARM_ID"));
			updateLoadGroupParamList.add(loadParamGroup);
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.EXIT_METHOD + "checkDatesForInsert");
		}
	}
	
	/**
	 * Method setParameter.
	 * @param paramTO ParameterTO
	 * @param results Results
	 * @param loadParamGroup ParameterTO
	 * @throws QueryException
	 */
	private static void setParameter(ParameterTO paramTO, Results results, ParameterTO loadParamGroup) throws QueryException{
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "setParameter");
		}
		paramTO.setParamId(results.getInt("SCHN_SHLANE_LGRP_PARM_ID"));
		paramTO.setLoadGroupParamCode(results.getShort("LOAD_PLNG_PARM_CD"));
		paramTO.setCharValue(results.getString("PARM_CHAR_VAL"));
		paramTO.setIntValue(results.getInt("PARM_INTG_VAL"));
		paramTO.setDecimalValue(results.getBigDecimal("PARM_DEC_VAL"));
		paramTO.setFlagValue(results.getString("PARM_FLG_VAL"));
		paramTO.setEffBeginDate(results.getDate("EFF_BGN_DT"));
		paramTO.setEffEndDate(ValidationUtil.getPreviousDay(loadParamGroup.getEffBeginDate()));
		//loadParamGroup.setEffBeginDate(new java.sql.Date(ValidationUtil.getNextDay(results.getDate("EFF_END_DT")).getTime()));
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.EXIT_METHOD + "setParameter");
		}
	}
	

	/**
	 * Method updateLoadGroupParamList.
	 * @param updateLoadGroupList List<LoadGroupParamTO>
	 * @param userId String
	 * @throws EFSException
	 */
	public static void updateLoadGroupParamList(
			List<LoadGroupParamTO> updateLoadGroupList, String userId) throws EFSException{
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "updateLoadGroupParamList");
		}
		try {
		List<ParameterTO> updateLoadParamList = getUpdateLoadParamList(updateLoadGroupList, userId);
			int updated = DAO.useJNDI(SupplyChainDistributionNetwork)
					.setSQL(UPDATE_LGRP_PARAM).debug(true)
					.batch(populateUpdateParamList(updateLoadParamList)).length;
			if (updateLoadParamList.size() == updated) {
				LOGGER.debug("Upldated Successfully");
			}
		} catch (QueryException qe){
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage());
		}
	}
	
	/**
	 * Method populateUpdateParamList.
	 * @param updateLoadParamList List<ParameterTO>
	 * @return BatchData
	 * @throws EFSException
	 */
	private static BatchData populateUpdateParamList(
			List<ParameterTO> updateLoadParamList) throws EFSException {
		BatchData loadGroupParamData = new BatchData(LGRP_PARAM_UPDATE_COLUMN_COUNT);
		int row = 0;
		for ( ParameterTO loadGroupParamTO : updateLoadParamList) {
			loadGroupParamData.add(row, 0,loadGroupParamTO.getLastUpdatedUserId());
			loadGroupParamData.add(row, 1,loadGroupParamTO.getEffBeginDate());
			loadGroupParamData.add(row, 2,loadGroupParamTO.getEffEndDate());
			loadGroupParamData.add(row, 3,loadGroupParamTO.getCharValue() != null ? loadGroupParamTO.getCharValue() : null );
			loadGroupParamData.add(row, 4,loadGroupParamTO.getIntValue() != null ? loadGroupParamTO.getIntValue() : null);
			loadGroupParamData.add(row, 5,loadGroupParamTO.getDecimalValue() != null ? loadGroupParamTO.getDecimalValue() : null );
			loadGroupParamData.add(row, 6,loadGroupParamTO.getFlagValue() != null ? loadGroupParamTO.getFlagValue().toUpperCase() : null);
			loadGroupParamData.add(row, 7,loadGroupParamTO.getParamId());
			loadGroupParamData.add(row, 8,loadGroupParamTO.getLoadGroupParamCode());
			loadGroupParamData.add(row, 9,loadGroupParamTO.getLoadGroupId());
			row++;
			
		}
		return loadGroupParamData;
	}

	
	/**
	 * Utility method to generate IN clause query string
	 * 
	 * @param columnName
	 * @param listName
	 * @param maxSize
	 * @param loadGrpTo
	 *            LoadGroupTO
	
	
	 * @return String * @throws QueryException */
	private static String inListBuilder(String columnName,
			LoadGroupParamTO loadGrpTo, String listName, int maxSize)
			throws QueryException {
		StringBuilder buff = new StringBuilder();

		buff.append(" AND ");
		buff.append(columnName);
		buff.append(" IN ( '");
		buff.append(loadGrpTo.getLocationNbr());
		buff.append("', '" + loadGrpTo.getDestinationNbr());
		buff.append("' ) ");
		return buff.toString();
	}

	/**
	 * Method getParamCodeMap.
	 * 
	
	
	 * @return Map<String,Short> * @throws EFSException */
	public static Map<String, Short> getParamCodeMap() throws EFSException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "getParamCodeMap");
		}

		final Map<String, Short> paramCodeMap = new HashMap<String, Short>();
		try {
			DAO.useJNDI(SupplyChainDistributionNetwork).setSQL(READ_PARAM_CODE)
					.debug(true).results(new ResultsReader() {
						@Override
						public void readResults(Results results, Query query,
								Inputs inputs) throws QueryException {
							while (results.next()) {
								paramCodeMap.put(results.getString("PARAM_NM"),
										results.getShort("PARAM_CD"));

							}
						}

					});
		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage());
		}

		return null;
	}

	/**
	 * Method getInsertLoadParamList.
	 * 
	 * @param loadGroupList
	 *            List<LoadGroupTO>
	 * @param userId
	 *            String
	
	
	 * @return List<LaneLoadGroupParamTO> * @throws EFSException */
	private static List<ParameterTO> getInsertLoadParamList(
			List<LoadGroupParamTO> loadGroupList, String userId) throws EFSException {
		List<ParameterTO> insertLoadParamList = new ArrayList<ParameterTO>();
		for (LoadGroupParamTO loadGrpTo : loadGroupList) {
			for (ParameterTO loadParamTo : loadGrpTo
					.getLoadParamList()) {
				loadParamTo.setCreateUserId(userId);
				loadParamTo.setLastUpdatedUserId(userId);
				loadParamTo.setLoadGroupId(loadGrpTo.getLoadGrpId());
				insertLoadParamList.add(loadParamTo);
			}
		}

		return insertLoadParamList;
	}
	
	
	/**
	 * Method getUpdateLoadParamList.
	 * 
	 * @param loadGroupList
	 *            List<LoadGroupTO>
	 * @param userId
	 *            String
	
	
	 * @return List<LaneLoadGroupParamTO> * @throws EFSException */
	private static List<ParameterTO> getUpdateLoadParamList(
			List<LoadGroupParamTO> loadGroupList, String userId) throws EFSException {
		List<ParameterTO> insertLoadParamList = new ArrayList<ParameterTO>();
		for (LoadGroupParamTO loadGrpTo : loadGroupList) {
			for (ParameterTO loadParamTo : loadGrpTo
					.getLoadParamList()) {
				loadParamTo.setLastUpdatedUserId(userId);
				loadParamTo.setLoadGroupId(loadGrpTo.getLoadGrpId());
				insertLoadParamList.add(loadParamTo);
			}
		}

		return insertLoadParamList;
	}
	
	/**
	 * Method convertListToMap.
	 * @param loadGrpTo LoadGroupParamTO
	 * @return Map<Short,ParameterTO>
	 * @throws EFSException
	 */
	private static Map<Short,ParameterTO> convertListToMap(LoadGroupParamTO loadGrpTo) throws EFSException {
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "convertListToMap");
		}
		
		Map<Short,ParameterTO> paramCodeMap = new HashMap<Short,ParameterTO>();
		for (ParameterTO loadGroupParamTo : loadGrpTo.getLoadParamList()) {
			
			loadGroupParamTo.setEffBeginDate(ValidationUtil.convertStringToDate(loadGrpTo.getEffectiveBeginDate()));
			loadGroupParamTo.setEffEndDate(ValidationUtil.convertStringToDate(loadGrpTo.getEffectiveEndDate()));
			paramCodeMap.put(loadGroupParamTo.getLoadGroupParamCode(), loadGroupParamTo);
		}
		
		return paramCodeMap;
		
	}
}
