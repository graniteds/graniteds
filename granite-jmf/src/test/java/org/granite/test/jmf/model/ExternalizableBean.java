/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.jmf.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Date;

public class ExternalizableBean implements Externalizable {

    private static final long serialVersionUID = 1L;
	
	private Object nullValue = null;

	private boolean booleanValue = false;

	private char charValue = 0;
	
	private byte byteValue = 0;
	private short shortValue = 0;
	private int intValue = 0;
	private long longValue = 0;
	
	private float floatValue = 0.0F;
	private double doubleValue = 0.0;

	private Boolean booleanObjectValue = null;

	private Character charObjectValue = null;
	
	private Byte byteObjectValue = null;
	private Short shortObjectValue = null;
	private Integer intObjectValue = null;
	private Long longObjectValue = null;
	
	private Float floatObjectValue = null;
	private Double doubleObjectValue = null;
	
	private String stringValue = null;
	private Date dateValue = null;
	
	private byte[] byteArrayValue = null;
	
	private Thread.State enumValue = null;
	
	private ExternalizableBean[] externalizableBeanArrayValue = new ExternalizableBean[2];
	
	private ExternalizableBean thisValue = null;	
	
	private ExternalizableBean otherBeanValue = null;	
	
	public ExternalizableBean() {
		this.externalizableBeanArrayValue[0] = this;
		this.thisValue = this;
	}

	public boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public char getCharValue() {
		return charValue;
	}

	public void setCharValue(char charValue) {
		this.charValue = charValue;
	}

	public byte getByteValue() {
		return byteValue;
	}

	public void setByteValue(byte byteValue) {
		this.byteValue = byteValue;
	}

	public short getShortValue() {
		return shortValue;
	}

	public void setShortValue(short shortValue) {
		this.shortValue = shortValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(long longValue) {
		this.longValue = longValue;
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

	public double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	public Boolean getBooleanObjectValue() {
		return booleanObjectValue;
	}

	public void setBooleanObjectValue(Boolean booleanObjectValue) {
		this.booleanObjectValue = booleanObjectValue;
	}

	public Character getCharObjectValue() {
		return charObjectValue;
	}

	public void setCharObjectValue(Character charObjectValue) {
		this.charObjectValue = charObjectValue;
	}

	public Byte getByteObjectValue() {
		return byteObjectValue;
	}

	public void setByteObjectValue(Byte byteObjectValue) {
		this.byteObjectValue = byteObjectValue;
	}

	public Short getShortObjectValue() {
		return shortObjectValue;
	}

	public void setShortObjectValue(Short shortObjectValue) {
		this.shortObjectValue = shortObjectValue;
	}

	public Integer getIntObjectValue() {
		return intObjectValue;
	}

	public void setIntObjectValue(Integer intObjectValue) {
		this.intObjectValue = intObjectValue;
	}

	public Long getLongObjectValue() {
		return longObjectValue;
	}

	public void setLongObjectValue(Long longObjectValue) {
		this.longObjectValue = longObjectValue;
	}

	public Float getFloatObjectValue() {
		return floatObjectValue;
	}

	public void setFloatObjectValue(Float floatObjectValue) {
		this.floatObjectValue = floatObjectValue;
	}

	public Double getDoubleObjectValue() {
		return doubleObjectValue;
	}

	public void setDoubleObjectValue(Double doubleObjectValue) {
		this.doubleObjectValue = doubleObjectValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public Thread.State getEnumValue() {
		return enumValue;
	}

	public void setEnumValue(Thread.State enumValue) {
		this.enumValue = enumValue;
	}

	public byte[] getByteArrayValue() {
		return byteArrayValue;
	}

	public void setByteArrayValue(byte[] byteArrayValue) {
		this.byteArrayValue = byteArrayValue;
	}

	public ExternalizableBean getOtherBeanValue() {
		return otherBeanValue;
	}

	public void setOtherBeanValue(ExternalizableBean otherBeanValue) {
		this.otherBeanValue = otherBeanValue;
		this.externalizableBeanArrayValue[1] = otherBeanValue;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(null);
		
		out.writeBoolean(booleanValue);

		out.writeChar(charValue);

		out.writeByte(byteValue);
		out.writeShort(shortValue);
		out.writeInt(intValue);
		out.writeLong(longValue);
		
		out.writeFloat(floatValue);
		out.writeDouble(doubleValue);

		out.writeObject(booleanObjectValue);

		out.writeObject(charObjectValue);

		out.writeObject(byteObjectValue);
		out.writeObject(shortObjectValue);
		out.writeObject(intObjectValue);
		out.writeObject(longObjectValue);
		
		out.writeObject(floatObjectValue);
		out.writeObject(doubleObjectValue);

		
		out.writeUTF(stringValue);
		out.writeObject(dateValue);

		out.writeObject(enumValue);
		out.writeObject(byteArrayValue);
		
		out.writeObject(thisValue);
		out.writeObject(otherBeanValue);

		out.writeObject(externalizableBeanArrayValue);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		nullValue = in.readObject();
		
		booleanValue = in.readBoolean();
		
		charValue = in.readChar();
		
		byteValue = in.readByte();
		shortValue = in.readShort();
		intValue = in.readInt();
		longValue = in.readLong();
		
		floatValue = in.readFloat();
		doubleValue = in.readDouble();

		booleanObjectValue = (Boolean)in.readObject();
		
		charObjectValue = (Character)in.readObject();
		
		byteObjectValue = (Byte)in.readObject();
		shortObjectValue = (Short)in.readObject();
		intObjectValue = (Integer)in.readObject();
		longObjectValue = (Long)in.readObject();
		
		floatObjectValue = (Float)in.readObject();
		doubleObjectValue = (Double)in.readObject();
		
		stringValue = in.readUTF();
		dateValue = (Date)in.readObject();
		
		enumValue = (Thread.State)in.readObject();
		byteArrayValue = (byte[])in.readObject();
		
		thisValue = (ExternalizableBean)in.readObject();
		otherBeanValue = (ExternalizableBean)in.readObject();
		
		externalizableBeanArrayValue = (ExternalizableBean[])in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExternalizableBean))
			return false;
		
		ExternalizableBean b = (ExternalizableBean)obj;
		
		return (
			nullValue == b.nullValue &&
			
			booleanValue == b.booleanValue &&
			
			charValue == b.charValue &&
			
			byteValue == b.byteValue &&
			shortValue == b.shortValue &&
			intValue == b.intValue &&
			longValue == b.longValue &&
			
			((Float.isNaN(floatValue) && Float.isNaN(b.floatValue)) || (floatValue == b.floatValue)) &&
			((Double.isNaN(doubleValue) && Double.isNaN(b.doubleValue)) || (doubleValue == b.doubleValue)) &&

			(booleanObjectValue == null ? b.booleanObjectValue == null : booleanObjectValue.equals(b.booleanObjectValue)) &&
			
			(charObjectValue == null ? b.charObjectValue == null : charObjectValue.equals(b.charObjectValue)) &&
			
			(byteObjectValue == null ? b.byteObjectValue == null : byteObjectValue.equals(b.byteObjectValue)) &&
			(shortObjectValue == null ? b.shortObjectValue == null : shortObjectValue.equals(b.shortObjectValue)) &&
			(intObjectValue == null ? b.intObjectValue == null : intObjectValue.equals(b.intObjectValue)) &&
			(longObjectValue == null ? b.longObjectValue == null : longObjectValue.equals(b.longObjectValue)) &&
			
			(floatObjectValue == null ? b.floatObjectValue == null : floatObjectValue.equals(b.floatObjectValue)) &&
			(doubleObjectValue == null ? b.doubleObjectValue == null : doubleObjectValue.equals(b.doubleObjectValue)) &&
			
			(stringValue == null ? b.stringValue == null : stringValue.equals(b.stringValue)) &&
			(dateValue == null ? b.dateValue == null : dateValue.equals(b.dateValue)) &&
			
			(enumValue == b.enumValue) &&
			(Arrays.equals(byteArrayValue, b.byteArrayValue)) &&
			
			(externalizableBeanArrayValue[0] == this && b.externalizableBeanArrayValue[0] == b) &&
			(externalizableBeanArrayValue[1] == otherBeanValue && b.externalizableBeanArrayValue[1] == b.otherBeanValue) &&
			
			thisValue == this &&
			(otherBeanValue == null ? b.otherBeanValue == null : otherBeanValue.equals(b.otherBeanValue))
		);
	}

	@Override
	public int hashCode() {
		// Makes compiler happy (unused)...
		return super.hashCode();
	}
}
