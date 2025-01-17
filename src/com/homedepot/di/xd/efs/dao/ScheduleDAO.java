package com.homedepot.di.xd.efs.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.homedepot.di.xd.efs.constants.EFSConstants;
import com.homedepot.di.xd.efs.constants.UploadConstants;
import com.homedepot.di.xd.efs.exception.DataOperationException;
import com.homedepot.di.xd.efs.exception.EFSException;
import com.homedepot.di.xd.efs.to.ScheduleOrderDayTO;
import com.homedepot.di.xd.efs.to.ScheduleTO;
import com.homedepot.di.xd.efs.util.UploadValidationUtil;
import com.homedepot.ta.aa.dao.Inputs;
import com.homedepot.ta.aa.dao.Query;
import com.homedepot.ta.aa.dao.Results;
import com.homedepot.ta.aa.dao.ResultsReader;
import com.homedepot.ta.aa.dao.builder.DAO;
import com.homedepot.ta.aa.dao.builder.UnitOfWork;
import com.homedepot.ta.aa.dao.exceptions.QueryException;

public class ScheduleDAO {
	
	private ScheduleDAO(){}

	private static final Logger LOGGER = Logger.getLogger(ScheduleDAO.class);
	
	// Data source constant
	private static final String SupplyChainDistributionNetwork = "java:comp/env/jdbc/SupplyChainDistributionNetwork.1";
	
	// SQL Query Constants
	private static final String SELECT_SCHN_EFS_SCH_TYP_CD_QUERY = "SELECT DISTINCT SCHN_EFS_SCH_TYP_CD FROM SCHN_EFS_SCH_TYP_CD "
			+ " WHERE 1=1 ";
			//AND SCHN_EFS_SCH_TYP_CD IN ( ?, ?)";
	
	private static final String SELECT_SCHN_EFS_SCH_GRP_QUERY = "SELECT DISTINCT SCHN_EFS_SCH_TYP_CD, SCHN_EFS_SCH_GRP_ID FROM SCHN_EFS_SCH_GRP "
			+ " WHERE 1=1 "; 
			//AND SCHN_EFS_SCH_TYP_CD IN ( ?, ?)";
	
	private static final String SELECT_MAX_WORK_DAY_CD_FOR_LOCATION = "SELECT XREF.SRC_LOC_ID, MAX(HRS.SCHN_EFS_DAY_SEG_CD ) as LAST_WORK_DAY_CD," +
			"abs(to_date(HRS.START_TM, 'HH24:MI:SS') - to_date(HRS.END_TM, 'HH24:MI:SS') ) * 24  AS TOTAL_HOURS_OF_OPERATION" +
			" FROM " +
			"SCHN_EFS_LOC_XREF XREF " +
			"INNER JOIN SCHN_EFS_LOC LOC " +
			" ON XREF.SCHN_LOC_ID = LOC.SCHN_LOC_ID " +
			" INNER JOIN SCHN_EFS_LOC_OPS_HRS HRS " +
			" ON HRS.SCHN_LOC_ID = LOC.SCHN_LOC_ID " +
			"WHERE " +
			"1 = 1 " ;
//			"AND XREF.SRC_LOC_ID ='9100'" +
//			"GROUP BY XREF.SRC_LOC_ID, HRS.START_TM, HRS.END_TM " 

	private static final String SCHEDULE_SEQ_NBR =	"SELECT SCHN_EFS_SCH_SEQ.NEXTVAL AS SCHN_EFS_SCH_SEQ FROM DUAL";
	
	private static final String INSERT_SCHN_EFS_SCH_QUERY = "INSERT INTO SCHN_EFS_SCH " 
            + " (SCHN_EFS_SCH_ID, " 
            + " CRT_SYSUSR_ID, " 
            + " CRT_TS, " 
            + " LAST_UPD_SYSUSR_ID, " 
            + " LAST_UPD_TS, " 
            + " EFF_BGN_DT, " 
            + " EFF_END_DT, " 
            + " SCHN_EFS_SCH_NM, " 
            + " SCHN_EFS_SCH_GRP_ID) " 
            + " VALUES (?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,?,?,?)";
	
	private static final String INSERT_SCHN_EFS_SCH_ORD_DAY_QUERY = "INSERT INTO SCHN_EFS_SCH_ORD_DAY "
			 + "(SCHN_EFS_SCH_ID , "
			 + "ORD_DAY_SEG_CD , "
			 + "CRT_SYSUSR_ID , "
			 + "CRT_TS , "
			 + "LAST_UPD_SYSUSR_ID , "
			 + "LAST_UPD_TS , "
			 + "TNDR_DAY_SEG_CD , "
			 + "TNDR_RLTV_WK_NBR , "
			 + "VNDR_TRN_TM_HRS , "
			 + "SHP_BGN_DAY_SEG_CD , "
			 + "SHP_BGN_RLTV_WK_NBR , "
			 + "SHP_END_DAY_SEG_CD , "
			 + "SHP_END_RLTV_WK_NBR , "
			 + "ORIG_LOC_SHP_BGN_TM , "
			 + "ORIG_LOC_SHP_END_TM , "
			 + "EXPCTD_CARR_DPRT_TM , "
			 + "EXPCTD_SHP_TRST_TM_HRS , "
			 + "ARVL_BGN_DAY_SEG_CD , "
			 + "ARVL_BGN_RLTV_WK_NBR , "
			 + "ARVL_END_DAY_SEG_CD , "
			 + "ARVL_END_RLTV_WK_NBR , "
			 + "EXPCTD_CARR_ARVL_BGN_TM , "
			 + "EXPCTD_CARR_ARVL_END_TM , "
			 + "DC_BGN_SHP_WRK_DAY_SEG_CD , "
			 + "DC_BGN_SHP_WRK_RLTV_WK_NBR, "
			 + "DC_END_SHP_WRK_DAY_SEG_CD , "
			 + "DC_END_SHP_WRK_RLTV_WK_NBR, "
			 + "DC_BGN_WRK_SHFT_SEQ_NBR , "
			 + "DC_END_WRK_SHFT_SEQ_NBR , "
			 + "DC_SHP_WRK_SHFT_CNT , "
			 + "DC_SHP_WRK_HRS ) "
			 + "VALUES (?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	/**
	 * Generates new sequence id based on input sequence query
	 * 
	 * @return integer
	 * @throws EFSException
	 * @throws DataOperationException 
	 */
	public static int getNextSeqId(String seqQuery) throws DataOperationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "getNextLocationSeqId");
		}
		Integer seqNbr;
		try {
			
			seqNbr = DAO.useJNDI(SupplyChainDistributionNetwork)
					.setSQL(seqQuery).get(Integer.class);
			
		} catch (QueryException qe) {
			throw new DataOperationException(EFSConstants.EXCEPTION_OCCURED +
					EFSConstants.QUERY_EXCEPTION_CD, qe);
		}

		return seqNbr.intValue();
	}
	
	/**
	 * Inserts a new Schedule Record
	 * 
	 * @param laneTO
	 * @return
	 * @throws EFSException
	 * @throws DataOperationException 
	 */
	public static boolean createSchedule(ScheduleTO scheduleTO) throws DataOperationException{
		try {
			if(scheduleTO == null){
				throw new QueryException("createSchedule : scheduleTO Can not be null");
			}
			
	    	if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("start createSchedule");
	        }
	    	
	    	boolean isSuccess = DAO.useJNDI(SupplyChainDistributionNetwork)
	    			            .setSQL(INSERT_SCHN_EFS_SCH_QUERY, 
	    			            		scheduleTO.getScheduleId(),
	    			            		scheduleTO.getCreatedUserId(),
	    			            		scheduleTO.getLastUptdUserId(),
	    			            		scheduleTO.getEffectiveBeginDate(),
	    			            		scheduleTO.getEffectiveEndDate(),
	    			            		scheduleTO.getScheduleName(),
	    			            		scheduleTO.getScheduleGroupCode())
	    			            		.debug(LOGGER)
	    					    .success();
	    	
	    	if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("finish createSchedule");
	            LOGGER.debug("Insert into Schedule success ? " + isSuccess);
	        }
	    	
			return isSuccess;
		} catch (QueryException qe) {
			throw new DataOperationException(EFSConstants.EXCEPTION_OCCURED +
					EFSConstants.QUERY_EXCEPTION_CD, qe);
		}
		
	}
	
	/**
	 * Inserts a Schedule Order day record
	 * 
	 * @param orderDayTo
	 * @return
	 * @throws EFSException
	 * @throws DataOperationException 
	 */
	public static boolean createScheduleOrderDay(ScheduleOrderDayTO orderDayTo) throws DataOperationException{
		try {
			if(orderDayTo == null){
				throw new QueryException("createScheduleOrderDay : scheduleTO Can not be null");
			}
			
			if (LOGGER.isDebugEnabled()) {
		        LOGGER.debug("start createSchedule");
		    }
			
			boolean isSuccess = DAO.useJNDI(SupplyChainDistributionNetwork)
					            .setSQL(INSERT_SCHN_EFS_SCH_ORD_DAY_QUERY, 
					            		orderDayTo.getScheduleId(),
					            		orderDayTo.getOrderDayOfWeekCode(),
					            		orderDayTo.getCreatedUserId(),
					            		orderDayTo.getLastUptdUserId(),
					            		orderDayTo.getTenderDayOfWeekCode(),
					            		orderDayTo.getTenderRelativeWeekNumber(),
					            		orderDayTo.getVendorTransportationTimeHours(),
					            		orderDayTo.getShipmentBeginDayOfWeekCode(),
					            		orderDayTo.getShipmentBeginRelativeWeekNumber(),
					            		orderDayTo.getShipmentEndDayOfWeekCode(),
					            		orderDayTo.getShipmentEndRelativeWeekNumber(),
					            		orderDayTo.getOriginLocationShipmentBeginTime(),
					            		orderDayTo.getOriginLocationShipmentEndTime(),
					            		orderDayTo.getExpectedCarrierDepartTime(),
					            		orderDayTo.getExpectedShipmentTransitTimeHours(),
					            		orderDayTo.getArrivalBeginDayOfWeekCode(),
					            		orderDayTo.getArrivalBeginRelativeWeekNumber(),
					            		orderDayTo.getArrivalEndDayOfWeekCode(),
					            		orderDayTo.getArrivalEndRelativeWeekNumber(),
					            		orderDayTo.getExpectedCarrierArrivalBeginTime(),
					            		orderDayTo.getExpectedCarrierArrivalEndTime(),
					            		orderDayTo.getDcBeginShipmentWorkDayOfWeekCode(),
					            		orderDayTo.getDcBeginShipmentWorkRelativeWeekNumber(),
					            		orderDayTo.getDcEndShipmentWorkDayOfWeekCode(),
					            		orderDayTo.getDcEndShipmentWorkRelativeWeekNumber(),
					            		orderDayTo.getDcBeginWorkShiftSequenceNumber(),
					            		orderDayTo.getDcEndWorkShiftSequenceNumber(),
					            		orderDayTo.getDcShipmentWorkShiftCount(),
					            		orderDayTo.getDcShipmentWorkHours())
					            		.debug(LOGGER)
							    .success();
			
			if (LOGGER.isDebugEnabled()) {
		        LOGGER.debug("finish createScheduleOrderDay");
		        LOGGER.debug("Insert into Schedule Order Day success ? " + isSuccess);
		    }
			
			return isSuccess;
		} catch (QueryException qe) {
			throw new DataOperationException(EFSConstants.EXCEPTION_OCCURED +
					EFSConstants.QUERY_EXCEPTION_CD, qe);
		}
	}
	
	/**
	 * Populates Schedule Informations
	 * 
	 * @param schToList
	 * @param inValidList
	 * @return
	 * @throws EFSException
	 * @throws DataOperationException 
	 */
	public static boolean insertRecords(final List<ScheduleTO> schToList, List<ScheduleTO> inValidList) throws DataOperationException {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "insertRecords");
		}
		try{
			new UnitOfWork<Boolean>() {
				@Override
				public Boolean runQueries() throws Exception {
					
					for(ScheduleTO scheduleTo:schToList)
					{
	            		scheduleTo.setScheduleId(getNextSeqId(SCHEDULE_SEQ_NBR));
						ScheduleDAO.createSchedule(scheduleTo);
						
						for (Entry<Short, ScheduleOrderDayTO> entry : scheduleTo.getScheduleOrderDayTO().entrySet())
						{
							//System.out.println(entry.getValue());
							ScheduleOrderDayTO orderDayTo = entry.getValue();
							orderDayTo.setScheduleId(scheduleTo.getScheduleId());
							ScheduleDAO.createScheduleOrderDay(orderDayTo);
						}

					}
					return value;
				}
			};
		}catch (QueryException qe) {
			throw new DataOperationException(EFSConstants.EXCEPTION_OCCURED +
					EFSConstants.QUERY_EXCEPTION_CD, qe);
		}
		return false;
	}

	
	/**
	 * Method is to check the existence of ScheduleTypeCode
	 * 
	 * @param schEfsTypeCode
	 * @return
	 * @throws EFSException
	 * @throws DataOperationException 
	 */
	public static List<Short> validateScheduleTypeCode(List<Short> scheduleTypeCode) throws DataOperationException{
		try{
			
			List<Short> validTypeCode = null;
			String validateScheduleTypeCodeQuery = SELECT_SCHN_EFS_SCH_TYP_CD_QUERY;

			validateScheduleTypeCodeQuery += inListBuilder("SCHN_EFS_SCH_TYP_CD",scheduleTypeCode,"scheduleTypeCode", 500);
			
			validTypeCode = DAO.useJNDI(SupplyChainDistributionNetwork)
					            .setSQL(validateScheduleTypeCodeQuery)
					            		.debug(true)
										.list(Short.class);
			return validTypeCode;
			
		}catch(QueryException qe){
			throw new DataOperationException(EFSConstants.EXCEPTION_OCCURED +
					EFSConstants.QUERY_EXCEPTION_CD, qe);
		}		
	}
	
	
	/**
	 * This method is to check the existence of the ScheduleGroupId 
	 * 
	 * @param schEfsGrpId
	 * @return
	 * @throws EFSException
	 * @throws DataOperationException 
	 */
	public static Set<String> validateScheduleGroupCode(List<Short> scheduleTypeCode) throws DataOperationException{
		try{
			
			final Set<String> validTypeGroupCode = new HashSet<>();
			
			String validateScheduleGroupCodeQuery = SELECT_SCHN_EFS_SCH_GRP_QUERY;

			validateScheduleGroupCodeQuery += inListBuilder("SCHN_EFS_SCH_TYP_CD",scheduleTypeCode,"scheduleTypeCode", 500);
			
			 DAO.useJNDI(SupplyChainDistributionNetwork)
			            .setSQL(validateScheduleGroupCodeQuery)
	            		.debug(true)
	            		.results(new ResultsReader()
							{
								@Override
								public void readResults(Results results, Query query, Inputs inputs)
										throws QueryException {
									while(results.next()){
										if(!UploadValidationUtil.isEmptyString(results.getString("SCHN_EFS_SCH_TYP_CD")) 
												&& !UploadValidationUtil.isEmptyString(results.getString("SCHN_EFS_SCH_GRP_ID"))){
											String key = results.getShort("SCHN_EFS_SCH_TYP_CD")+"_"
													+results.getShort("SCHN_EFS_SCH_GRP_ID");
											validTypeGroupCode.add(key);
										}
									}
								}
		
							});
			 
			return validTypeGroupCode;
			
		}catch(QueryException qe){
			throw new DataOperationException(EFSConstants.EXCEPTION_OCCURED +
					EFSConstants.QUERY_EXCEPTION_CD, qe);
		}		
	}
	
	/**
	 * Method to get the Last Working day for all location
	 * @param sourceLocationId
	 * @return
	 * @throws EFSException
	 * @throws DataOperationException 
	 */
	public static Map<String, String> getMaxWorkDayForLocation() throws DataOperationException{
		try{
			
			final Map<String, String> locationLastWorkDayCode = new HashMap<>();
			
			String getMaxWorkDayCodeQuery = SELECT_MAX_WORK_DAY_CD_FOR_LOCATION;
			
			/*if(sourceLocationId!=null && sourceLocationId.size()>0){
				getMaxWorkDayCodeQuery += inListBuilder("XREF.SRC_LOC_ID",sourceLocationId,"scheduleTypeCode", 500);
			}*/

			//Write now making this to read only SSC work hours
			getMaxWorkDayCodeQuery += "AND XREF.SRC_LOC_ID ='9100' "; 
			
			getMaxWorkDayCodeQuery += " GROUP BY XREF.SRC_LOC_ID, HRS.START_TM, HRS.END_TM"; 
			
			 DAO.useJNDI(SupplyChainDistributionNetwork)
			            .setSQL(getMaxWorkDayCodeQuery)
	            		.debug(true)
	            		.results(new ResultsReader()
							{
								@Override
								public void readResults(Results results, Query query, Inputs inputs)
										throws QueryException {
									while(results.next()){
										if(!UploadValidationUtil.isEmptyString(results.getString("SRC_LOC_ID")) 
												&& results.getShort("LAST_WORK_DAY_CD") > 0 && results.getShort("TOTAL_HOURS_OF_OPERATION") > 0){
											locationLastWorkDayCode.put(results.getString("SRC_LOC_ID"), results.getShort("LAST_WORK_DAY_CD")+"_"+
													results.getShort("TOTAL_HOURS_OF_OPERATION"));
										}
									}
								}
		
							});
			 
			return locationLastWorkDayCode;
			
		}catch(QueryException qe){
			throw new DataOperationException(EFSConstants.EXCEPTION_OCCURED +
					EFSConstants.QUERY_EXCEPTION_CD, qe);
		}		
	}
	

	/**
	 * Utility method to generate IN clause query string
	 * 
	 * @param columnName
	 * @param inList
	 * @param listName
	 * @param maxSize
	 * @return
	 * @throws QueryException
	 */
	public static String inListBuilder(String columnName, List<?> inList ,String listName, int maxSize) throws QueryException{
		StringBuilder buff = new StringBuilder();

		if(inList.size()==0 || inList.size()>maxSize){
			throw new QueryException(listName + " size is " + inList.size() + "; size must be between 1 and " + maxSize );
		}
		buff.append(" AND ");
		buff.append(columnName);
		buff.append(" IN ( '");
		for(int i = 0;i<inList.size();i++){
			if(i>0)
				buff.append("', '" + inList.get(i));
			else
				buff.append( inList.get(i));
		}
		buff.append("' ) ");
		return buff.toString();
	}
}
