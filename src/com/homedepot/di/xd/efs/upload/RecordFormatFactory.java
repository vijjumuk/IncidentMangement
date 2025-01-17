package com.homedepot.di.xd.efs.upload;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author x3aydjb
 *
 */
public class RecordFormatFactory {

	private static final RecordFormat FORMAT_LANE_LOCATION = 
			new RecordFormatImpl(
					FileType.LANE,
			new Field[] {
					new Field("LOCATION_NUMBER", String.class, false),
					new Field("LOCATION_DESTINATION", Integer.class, false),
					new Field("LANE_NAME", String.class, false),
					new Field("VENDOR", String.class, false),
					new Field("PRIMARY_LOCATION", String.class, false),
					new Field("VENDOR_EFF_BEGIN_DATE", Date.class, false),
					new Field("VENDOR_EFF_END_DATE", Date.class, false),
					new Field("LOAD_GROUP_NAME", String.class, false),
					new Field("LOAD_GROUP_EFF_BEGIN_DATE", Date.class, false),
					new Field("LOAD_GROUP_EFF_END_DATE", Date.class, false),
					new Field("LANE_ACTIVE_FLAG", String.class, true)
			});  
	
	private static final RecordFormat FORMAT_SCHEDULE_LOCATION = 
			new RecordFormatImpl(
					FileType.SCHEDULE,
			new Field[] {
							new Field("Location Number", String.class, false),
							new Field("Destination", Integer.class, false),
							new Field("Schedule Type Code", Short.class, false),
							new Field("Schedule Group Code", Short.class, true),
							new Field("Schedule Name", String.class, false),
							new Field("Order Day", Short.class, false),
							new Field("VTT", Short.class, false),
							new Field("Ship Day", Short.class, false),
							new Field("Arrive Day", Short.class, false),
							new Field("Vendor Ready Time", String.class, false),
							new Field("Expected Carrier Depart Time", String.class, false),
							new Field("Transit Time", Short.class, false),
							new Field("DC Appointment Time", String.class, false),
							new Field("DC Start Work Day", Short.class, false),
							new Field("DC End Work Day", Short.class, false),
							new Field("Start Shift Sequence Number", Short.class, false),
							new Field("End Shift Sequence Number", Short.class, false),
							new Field("Number of Shifts Worked", Short.class, false),
							//new Field("DEST_LOC_HRS_WRKD_CNT", Short.class, true),
							new Field("Order Week Number", Short.class, false),
							new Field("Tender Week Number", Short.class, false),
							new Field("Ship Week Number", Short.class, false),
							new Field("Arrive Week Number", Short.class, false),
							new Field("DC Start Work Week Number", Short.class, false),
							new Field("DC End Work Week Number", Short.class, false),
							new Field("Effective Begin Date", Date.class, false),
							new Field("Effective End Date", Date.class, false)
			});
	
	private static final RecordFormat FORMAT_ROUTE_LOCATION = 
			new RecordFormatImpl(
					FileType.ROUTE,
			new Field[] {
							new Field("Route Type Code", Integer.class, true),
							new Field("Route Config Code", Integer.class, true),
							new Field("Route Description", String.class, true),
							new Field("Carrier Code", Integer.class, true),
							new Field("Effective Begin Date", Date.class, true),
							new Field("Effective End Date", Date.class, true)
			});
	
	private static final RecordFormat FORMAT_LOCATION = 
            new RecordFormatImpl(
                         FileType.LOCATION,
            new Field[] {
                         new Field("Location Number", String.class, true),
                         new Field("Location Description", String.class, true),
                         new Field("Address 1", String.class, true),
                         new Field("Address 2", String.class, true),
                         new Field("Address 3", String.class, true),
                         new Field("Address 4", String.class, true),
                         new Field("Address 5", String.class, true),
                         new Field("City", String.class, true),
                         new Field("State", String.class, true),
                         new Field("Zip", String.class, true),
                         new Field("Latitude", String.class, true),
                         new Field("Longitude", String.class, true),
                         new Field("Country Code", String.class, true),
                         new Field("Location Type Code", String.class, true),
                         new Field("Active Flag", String.class, true)
            });

	private static final RecordFormat FORMAT_LOAD_GROUP_PARM = new RecordFormatImpl(
			FileType.LOADGROUP_PARM, new Field[] {
					new Field("Location Number", String.class, true),
					new Field("Destination", String.class, true),
					new Field("Load Group Name", String.class, true),
					new Field("Max Truck Height", String.class, true),
					new Field("TL Cube Threshold", String.class, true),
					new Field("TL Weight Threshold", String.class, true),
					new Field("Directional Rounding Threshold",
							String.class, true),
					new Field("Threshold Increment", String.class, true),
					new Field("Full TL Threshold", String.class, true),
					new Field("Load Configurator", String.class, true),
					new Field("Smoothing Interval", String.class, true),
					new Field("TL Daily Min Qty", String.class, true),
					new Field("TL Daily Max Qty", String.class, true),
					new Field("Effective Begin Date", String.class, true),
					new Field("Effective End Date", String.class, true) });

	private static final RecordFormat FORMAT_VNDR_PARM = new RecordFormatImpl(
			FileType.VNDR_PARM, new Field[] {
					new Field("MVNDR Number", String.class, true),
					new Field("Origin", String.class, true),
					new Field("Destination", String.class, true),
					new Field("Default MVNDR Parameters", String.class, true),
					new Field("Max Pull Forward Days", String.class, true),
					new Field("TL Round Up", String.class, true),
					new Field("TL Round Down", String.class, true),
					new Field("OUTL Threshold", String.class, true),
					new Field("Effective Begin", String.class, true),
					new Field("Effective End", String.class, true), });

	
	private static RecordFormat[] cFormats = new RecordFormat[] {
			FORMAT_LOCATION, FORMAT_LANE_LOCATION, FORMAT_ROUTE_LOCATION,
			FORMAT_LOAD_GROUP_PARM, FORMAT_VNDR_PARM, FORMAT_SCHEDULE_LOCATION
	};
	
	private static RecordFormatFactory cInstance = new RecordFormatFactory();
					
	private RecordFormatFactory() {}

	public static RecordFormatFactory getInstance() {
		return cInstance;
	}
	
	public static RecordFormat getRecordFormatFor(FileType uploadtype) 
			throws FormatException {
		
		for (int i = 0; i < cFormats.length; i++) {
			RecordFormat format = cFormats[i];
			if (format.getUploadType() == uploadtype) {
				return format;
			}
		}
			
		throw new FormatException("Unsupported Upload Format(" +
				uploadtype.name() + ")");
	}

}
