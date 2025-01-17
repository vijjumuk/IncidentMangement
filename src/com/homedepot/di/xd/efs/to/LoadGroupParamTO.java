package com.homedepot.di.xd.efs.to;

import java.io.Serializable;
import java.util.List;

/**
 * This class is used for Load Group
 */
public class LoadGroupParamTO implements Serializable {

	/**
	 * Field serialVersionUID. (value is 3739959446853627352)
	 */
	private static final long serialVersionUID = 3739959446853627352L;

	/**
	 * Field locationNumber.
	 */
	private String locationNumber;
	/**
	 * Field locationId.
	 */
	private int locationId;
	/**
	 * Field destinationNumber.
	 */
	private String destinationNumber;
	/**
	 * Field destinationId.
	 */
	private int destinationId;
	/**
	 * Field loadGroupName.
	 */
	private String loadGroupName;
	/**
	 * Field loadGroupId.
	 */
	private int loadGroupId;
	/**
	 * Field shipLaneId.
	 */
	private int shipLaneId;
	/**
	 * Field loadParamList.
	 */
	private List<ParameterTO> loadParamList;
	/**
	 * Field rowNumber.
	 */
	private int rowNumber;
	/**
	 * Field effectiveBeginDate.
	 */
	private String effectiveBeginDate;
	/**
	 * Field effectiveEndDate.
	 */
	private String effectiveEndDate;

	/**
	 * Field isUpdate
	 */
	private boolean isUpdate;

	/**
	 * Method isUpdate.
	 * 
	 * @return
	 */
	public boolean isUpdate() {
		return isUpdate;
	}

	/**
	 * Method setUpdate.
	 * 
	 * @param isUpdate
	 */
	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

	/**
	 * Method getEffectiveBeginDate.
	 * 
	 * @return String
	 */
	public String getEffectiveBeginDate() {
		return effectiveBeginDate;
	}

	/**
	 * Method setEffectiveBeginDate.
	 * 
	 * @param effectiveBeginDate
	 *            String
	 */
	public void setEffectiveBeginDate(String effectiveBeginDate) {
		this.effectiveBeginDate = effectiveBeginDate;
	}

	/**
	 * Method getEffectiveEndDate.
	 * 
	 * @return String
	 */
	public String getEffectiveEndDate() {
		return effectiveEndDate;
	}

	/**
	 * Method setEffectiveEndDate.
	 * 
	 * @param effectiveEndDate
	 *            String
	 */
	public void setEffectiveEndDate(String effectiveEndDate) {
		this.effectiveEndDate = effectiveEndDate;
	}

	/**
	 * Method getRowNumber.
	 * 
	 * @return int
	 */
	public int getRowNumber() {
		return rowNumber;
	}

	/**
	 * Method setRowNumber.
	 * 
	 * @param rowNumber
	 *            int
	 */
	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	/**
	 * Method setLocationNbr.
	 * 
	 * @param locNbr
	 *            String
	 */
	public void setLocationNbr(String locNbr) {

		this.locationNumber = locNbr;

	}

	/**
	 * Method getLocationNbr.
	 * 
	 * @return String
	 */
	public String getLocationNbr() {

		return locationNumber;
	}

	/**
	 * Method setLocationId.
	 * 
	 * @param locId
	 *            int
	 */
	public void setLocationId(int locId) {

		this.locationId = locId;

	}

	/**
	 * Method getLocationId.
	 * 
	 * @return int
	 */
	public int getLocationId() {

		return locationId;
	}

	/**
	 * Method setDestinationNbr.
	 * 
	 * @param destNbr
	 *            String
	 */
	public void setDestinationNbr(String destNbr) {

		this.destinationNumber = destNbr;

	}

	/**
	 * Method getDestinationNbr.
	 * 
	 * @return String
	 */
	public String getDestinationNbr() {

		return destinationNumber;
	}

	/**
	 * Method setDestinationId.
	 * 
	 * @param destId
	 *            int
	 */
	public void setDestinationId(int destId) {

		this.destinationId = destId;

	}

	/**
	 * Method getDestinationId.
	 * 
	 * @return int
	 */
	public int getDestinationId() {

		return destinationId;
	}

	/**
	 * Method setLoadGrpName.
	 * 
	 * @param loadGrpname
	 *            String
	 */
	public void setLoadGrpName(String loadGrpname) {

		this.loadGroupName = loadGrpname;
	}

	/**
	 * Method getLoadGrpName.
	 * 
	 * @return String
	 */
	public String getLoadGrpName() {

		return loadGroupName;
	}

	/**
	 * Method setLoadGrpId.
	 * 
	 * @param loadGrpId
	 *            int
	 */
	public void setLoadGrpId(int loadGrpId) {

		this.loadGroupId = loadGrpId;

	}

	/**
	 * Method getLoadGrpId.
	 * 
	 * @return int
	 */
	public int getLoadGrpId() {

		return loadGroupId;
	}

	/**
	 * Method setShippingLaneId.
	 * 
	 * @param shipLaneId
	 *            int
	 */
	public void setShippingLaneId(int shipLaneId) {

		this.shipLaneId = shipLaneId;
	}

	/**
	 * Method getShippingLaneId.
	 * 
	 * @return int
	 */
	public int getShippingLaneId() {

		return shipLaneId;
	}

	/**
	 * Method setLoadParamList.
	 * 
	 * @param paramCdLiat
	 *            List<LaneLoadGroupParamTO>
	 */
	public void setLoadParamList(List<ParameterTO> paramCdLiat) {

		this.loadParamList = paramCdLiat;

	}

	/**
	 * Method getLoadParamList.
	 * 
	 * @return List<LaneLoadGroupParamTO>
	 */
	public List<ParameterTO> getLoadParamList() {

		return loadParamList;
	}

	/**
	 * Method hashCode.
	 * 
	 * @return int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + loadGroupId;
		result = prime * result + destinationId;
		result = prime * result
				+ ((destinationNumber == null) ? 0 : destinationNumber.hashCode());
		result = prime
				* result
				+ ((effectiveBeginDate == null) ? 0 : effectiveBeginDate
						.hashCode());
		result = prime
				* result
				+ ((effectiveEndDate == null) ? 0 : effectiveEndDate.hashCode());
		result = prime * result + (isUpdate ? 1231 : 1237);
		result = prime * result
				+ ((loadGroupName == null) ? 0 : loadGroupName.hashCode());
		result = prime * result
				+ ((loadParamList == null) ? 0 : loadParamList.hashCode());
		result = prime * result + locationId;
		result = prime * result
				+ ((locationNumber == null) ? 0 : locationNumber.hashCode());
		result = prime * result + rowNumber;
		result = prime * result + shipLaneId;
		return result;
	}

	/**
	 * Method equals.
	 * 
	 * @param obj
	 *            Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoadGroupParamTO other = (LoadGroupParamTO) obj;
		if (loadGroupId != other.loadGroupId)
			return false;
		if (destinationId != other.destinationId)
			return false;
		if (destinationNumber == null) {
			if (other.destinationNumber != null)
				return false;
		} else if (!destinationNumber.equals(other.destinationNumber))
			return false;
		if (effectiveBeginDate == null) {
			if (other.effectiveBeginDate != null)
				return false;
		} else if (!effectiveBeginDate.equals(other.effectiveBeginDate))
			return false;
		if (effectiveEndDate == null) {
			if (other.effectiveEndDate != null)
				return false;
		} else if (!effectiveEndDate.equals(other.effectiveEndDate))
			return false;
		if (isUpdate != other.isUpdate)
			return false;
		if (loadGroupName == null) {
			if (other.loadGroupName != null)
				return false;
		} else if (!loadGroupName.equals(other.loadGroupName))
			return false;
		if (loadParamList == null) {
			if (other.loadParamList != null)
				return false;
		} else if (!loadParamList.equals(other.loadParamList))
			return false;
		if (locationId != other.locationId)
			return false;
		if (locationNumber == null) {
			if (other.locationNumber != null)
				return false;
		} else if (!locationNumber.equals(other.locationNumber))
			return false;
		if (rowNumber != other.rowNumber)
			return false;
		if (shipLaneId != other.shipLaneId)
			return false;
		return true;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		return "LoadGroupParamTO [locationNumber=" + locationNumber + ", locationId="
				+ locationId + ", destinationNumber=" + destinationNumber
				+ ", destinationId=" + destinationId + ", loadGroupName="
				+ loadGroupName + ", loadGroupId=" + loadGroupId + ", shipLaneId="
				+ shipLaneId + ", loadParamList=" + loadParamList
				+ ", rowNumber=" + rowNumber + ", effectiveBeginDate="
				+ effectiveBeginDate + ", effectiveEndDate=" + effectiveEndDate
				+ ", isUpdate=" + isUpdate + "]";
	}

}
