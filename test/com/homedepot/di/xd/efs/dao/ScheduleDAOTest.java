package com.homedepot.di.xd.efs.dao;

import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.homedepot.di.xd.efs.to.ScheduleOrderDayTO;
import com.homedepot.di.xd.efs.to.ScheduleTO;
import com.homedepot.homer.util.HomerUnitTestCase;

/**
 * The class <code>ScheduleUploadDAOTest</code> contains tests for the class <code>{@link ScheduleDAO}</code>.
 *
 * @generatedBy CodePro at 3/6/15 6:30 PM
 * @author 565581
 * @version $Revision: 1.0 $
 */
public class ScheduleDAOTest extends HomerUnitTestCase {


	public static int scheduleId = 400;
	public static short orderDayOfWkCd = 1;
	public static int seqNmbr = 100;
	/**
	 * Run the boolean createSchedule(ScheduleTO) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 3/6/15 6:30 PM
	 */
	@Test
	public void testCreateSchedule()
		throws Exception {
		ScheduleTO scheduleTO = new ScheduleTO();
		
		scheduleId++;
		scheduleTO.setScheduleId(scheduleId);
		scheduleTO.setCreatedUserId("NTJ8189");
		scheduleTO.setLastUptdUserId("NTJ8189");
		scheduleTO.setEffectiveBeginDate(new Date(System.currentTimeMillis()));
		scheduleTO.setEffectiveEndDate(new Date(System.currentTimeMillis()));
		scheduleTO.setScheduleName("CARGILL BREAUX BRIDGE HOUSTON CORE");
		scheduleTO.setScheduleGroupCode((short) 12);
		
		da.table("SCHN_EFS_SCH").where("SCHN_EFS_SCH_ID = "+scheduleId);
		da.assert_count(0);
		boolean result = ScheduleDAO.createSchedule(scheduleTO);
		assertTrue(result);
		da.table("SCHN_EFS_SCH").where("SCHN_EFS_SCH_ID = "+scheduleId);
		da.assert_count(1);
	}

	@Test/*(expected = com.homedepot.di.xd.efs.exception.EFSException.class)*/
	public void testGetSequenceId()
		throws Exception {

		String SCHEDULE_SEQ_NBR =	"SELECT SCHN_EFS_SCH_SEQ.NEXTVAL AS SCHN_EFS_SCH_SEQ FROM DUAL";
		
		Assert.assertEquals(ScheduleDAO.getNextSeqId(SCHEDULE_SEQ_NBR), seqNmbr);
		seqNmbr++;
	}

	/**
	 * Run the boolean createScheduleOrderDay(ScheduleOrderDayTO) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 3/6/15 6:30 PM
	 */
	@Test
	public void testCreateScheduleOrderDay()
		throws Exception {
		ScheduleOrderDayTO orderDayTo = new ScheduleOrderDayTO();
		
		scheduleId++;
		orderDayOfWkCd+=2;
		orderDayTo.setScheduleId(scheduleId+10);
		orderDayTo.setOrderDayOfWeekCode(orderDayOfWkCd);
		orderDayTo.setCreatedUserId("NTJ8189");
		orderDayTo.setLastUptdUserId("NTJ8189");
		orderDayTo.setTenderDayOfWeekCode((short)3);
		orderDayTo.setTenderRelativeWeekNumber((short)2);
		orderDayTo.setVendorTransportationTimeHours((short)4);
		orderDayTo.setShipmentBeginDayOfWeekCode((short)6);
		orderDayTo.setShipmentBeginRelativeWeekNumber((short)6);
		orderDayTo.setShipmentEndDayOfWeekCode((short)2);
		orderDayTo.setShipmentEndRelativeWeekNumber((short)5);
		orderDayTo.setOriginLocationShipmentBeginTime("12:22:22");
		orderDayTo.setOriginLocationShipmentEndTime("14:32:22");
		orderDayTo.setExpectedCarrierDepartTime("11:27:12");
		orderDayTo.setExpectedShipmentTransitTimeHours((short) 6);
		orderDayTo.setArrivalBeginDayOfWeekCode((short) 4);
		orderDayTo.setArrivalBeginRelativeWeekNumber((short) 1);
		orderDayTo.setArrivalEndDayOfWeekCode((short) 3);
		orderDayTo.setArrivalEndRelativeWeekNumber((short) 5);
		orderDayTo.setExpectedCarrierArrivalBeginTime("21:27:12");
		orderDayTo.setExpectedCarrierArrivalEndTime("13:27:12");
		orderDayTo.setDcBeginShipmentWorkDayOfWeekCode((short) 1);
		orderDayTo.setDcBeginShipmentWorkRelativeWeekNumber((short) 1);
		orderDayTo.setDcEndShipmentWorkDayOfWeekCode((short) 1);
		orderDayTo.setDcEndShipmentWorkRelativeWeekNumber((short) 1);
		orderDayTo.setDcBeginWorkShiftSequenceNumber((short) 1);
		orderDayTo.setDcEndWorkShiftSequenceNumber((short) 1);
		orderDayTo.setDcShipmentWorkShiftCount((short) 1);
		orderDayTo.setDcShipmentWorkHours((short) 1);

		boolean result = ScheduleDAO.createScheduleOrderDay(orderDayTo);

		assertTrue(result);
	}
	

	/**
	 * Run the List<String> validateScheduleGroupCode(List<Short>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 3/9/15 3:21 PM
	 */
	@Test
	public void testValidateScheduleGroupCode()
		throws Exception {
		List<Short> scheduleTypeCode = new ArrayList<>();
		
		scheduleTypeCode.add((short) 1);
		scheduleTypeCode.add((short) 2);
		scheduleTypeCode.add((short) 3);
		scheduleTypeCode.add((short) 4);

		Set<String> result = ScheduleDAO.validateScheduleGroupCode(scheduleTypeCode);
		Assert.assertEquals(3, result.size());
	}

	/**
	 * Run the List<Short> validateScheduleTypeCode(List<Short>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 3/9/15 3:21 PM
	 */
	@Test
	public void testValidateScheduleTypeCode()
			throws Exception {
		List<Short> scheduleTypeCode = new ArrayList<>();

		scheduleTypeCode.add((short) 1);
		scheduleTypeCode.add((short) 3);
		scheduleTypeCode.add((short) 4);

		List<Short> result = ScheduleDAO.validateScheduleTypeCode(scheduleTypeCode);
		Assert.assertEquals(2, result.size());
	}
	
	/**
	 * Run the List<Short> getMaxWorkDayForLocation() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 3/9/15 3:21 PM
	 */
	@Test(expected = com.homedepot.di.xd.efs.exception.DataOperationException.class)
	public void testGetMaxWorkDayForLocation()
			throws Exception {
		
		Map<String, String> map = ScheduleDAO.getMaxWorkDayForLocation();
		Assert.assertNotNull(map);
	}
	
	@Test
	public void testInListBuilder()
		throws Exception {
		
		String column = "SCHN_EFS_SCH_TYP_CD";
		List<Short> inList = new ArrayList<>();
		inList.add((short) 1);
		String listName = "Type Code List";
		
		String query = ScheduleDAO.inListBuilder(column, inList, listName, 100);
		Assert.assertNotNull(query);
		
	}

	
	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 3/6/15 6:30 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		// add additional set up code here
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 3/6/15 6:30 PM
	 */
	@After
	public void tearDown()
		throws Exception {
		// Add additional tear down code here
	}
	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 3/6/15 6:30 PM
	 *//*
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(ScheduleUploadDAOTest.class);
	}*/
}