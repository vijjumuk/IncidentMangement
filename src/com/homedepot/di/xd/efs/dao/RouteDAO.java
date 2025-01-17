package com.homedepot.di.xd.efs.dao;

import java.util.List;

import org.apache.log4j.Logger;

import com.homedepot.di.xd.efs.constants.UploadConstants;
import com.homedepot.di.xd.efs.exception.EFSException;
import com.homedepot.di.xd.efs.to.RouteTO;
import com.homedepot.ta.aa.dao.builder.DAO;
import com.homedepot.ta.aa.dao.builder.UnitOfWork;
import com.homedepot.ta.aa.dao.exceptions.QueryException;

public class RouteDAO {
	private static final Logger LOGGER = Logger.getLogger(LaneDAO.class);
	
	private static final String SupplyChainDistributionNetwork = "java:comp/env/jdbc/SupplyChainDistributionNetwork.1";
	
	//SQL Query Contsants
	private static final String INSERT_EFS_ROUTE_QUERY = "INSERT INTO SCHN_EFS_RTE"
			+ " (SCHN_EFS_RTE_ID,"
			+ " CRT_SYSUSR_ID,"
			+ " CRT_TS,"
			+ " LAST_UPD_SYSUSR_ID,"
			+ " LAST_UPD_TS,"
			+ " EFF_BGN_DT,"
			+ " EFF_END_DT,"
			+ " SCHN_EFS_RTE_TYP_CD,"
			+ " SCHN_EFS_RTE_CONFG_CD,"
			+ " SCHN_EFS_RTE_NM)"
			+ " VALUES (?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,?,?,?,?)";
	
	private static final String SELECT_ROUTE = "SELECT SCHN_EFS_RTE_ID FROM SCHN_EFS_RTE"
			+ " WHERE SCHN_EFS_RTE_TYP_CD=? and"
			+ " SCHN_EFS_RTE_CONFG_CD=? and"
			+ " SCHN_EFS_RTE_NM=? ";
	
	private static final String SELECT_NEXT_RTE_ID = "SELECT SCHN_EFS_RTE_SEQ.NEXTVAL FROM DUAL";
	
	private static boolean createRoute(RouteTO routeTO) throws EFSException{
		try {
			if(routeTO == null){
				throw new QueryException("createRoute : routeTO Can not be null");
			}
			
	    	if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("start createRoute");
	        }
	    	
	    	boolean isSuccess = DAO.useJNDI(SupplyChainDistributionNetwork)
	    			            .setSQL(INSERT_EFS_ROUTE_QUERY,
	    			            		routeTO.getRouteID(),
	    			            		routeTO.getCreatedSysUserId(),
	    			            		//routeTO.getCreatedTs(),
	    			            		routeTO.getLastUptdUserId(),
	    			            		//routeTO.getLastUptdTs(),
	    			            		routeTO.getEffectiveBeginDate(),
	    			            		routeTO.getEffectiveEndDate(),
	    			            		routeTO.getRouteTypeCd(),
	    			            		routeTO.getrouteConfigCd(),
	    			            		routeTO.getRouteNm())
	    			            		.debug(LOGGER)
	    			            		.success();
	    	
	    	if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("finish createRoute");
	            LOGGER.debug("Insert into route success ? " + isSuccess);
	        }
	    	
			return isSuccess;
		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage(),
					UploadConstants.QUERY_EXCEPTION_CD);
		}
		
	}
	public static boolean insertRecords(final List<RouteTO> routeTOList, final List<RouteTO> inValidRouteTOList) throws EFSException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(UploadConstants.ENTERING_METHOD + "insertRecords");
		}
	
		try{
			new UnitOfWork<Boolean>() {
				
				@Override
				public Boolean runQueries() throws Exception{
					if (routeTOList != null && routeTOList.size() > 0){
						for (RouteTO routeTO: routeTOList){
							if(LOGGER.isDebugEnabled()){
								LOGGER.debug("Processing RouteTO : " + routeTO.toString());		
								} //endof: if(LOGGER.isDebugEnabled()){
							
							/*
							 * Check for route existance
							 * if not then insert. If so, then error
							 */
							int route_ID = checkIfRouteExists(routeTO);
							
							if(route_ID == 0){//if there is no record of this route
								//then add the new route
								
								//add sequence
								routeTO.setRouteID(getNextRouteIDSequence());
								createRoute(routeTO);
								
							}else{//then the route has already been added
								//ignore creating a new route. 
								routeTO.setErrorString(UploadConstants.ROUTE_ALREADY_EXISTS);
								inValidRouteTOList.add(routeTO);
								
							}
							
							
						}//end of: for (RouteTO routeTO: routeTOList){
					}//endof: if (routeTOList != null && routeTOList.size() > 0){
					return null;
				}//endof public Boolean runQueries () throws Exception{

				
			};//endof: new UnitOfWork<Boolean>{
		} catch (QueryException qe) {
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage(),
					UploadConstants.QUERY_EXCEPTION_CD);
		}//endof: try/catch
		
		
				
		
		
		
		
		
		
		
		
		
		
		
		return false;
	}//end of insertRecords Method
	
	public static int checkIfRouteExists(RouteTO routeTO) throws EFSException {
		
		try{
			Integer routeID =DAO.useJNDI(SupplyChainDistributionNetwork)
		            .setSQL(SELECT_ROUTE, 
		            		routeTO.getRouteTypeCd(),
		            		routeTO.getrouteConfigCd(),
		            		routeTO.getRouteNm().toUpperCase())
		            		.debug(true)
							.get(Integer.class);
			if(routeID !=null){
				return routeID;
			}
		}catch(QueryException qe){
			throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage(),
					UploadConstants.QUERY_EXCEPTION_CD);
		}
		//	
		return 0;
	}
	
	public static int getNextRouteIDSequence() throws EFSException{
		try{
			Integer routeID =DAO.useJNDI(SupplyChainDistributionNetwork)
		            .setSQL(SELECT_NEXT_RTE_ID)
		            		.debug(true)
							.get(Integer.class);
			if(routeID !=null){
				return routeID;
			}
		}catch(QueryException qe){
				throw new EFSException(UploadConstants.EXCEPTION + qe.getMessage(),
				UploadConstants.QUERY_EXCEPTION_CD);
		}
		//	
		return 0;
		
	}
}
