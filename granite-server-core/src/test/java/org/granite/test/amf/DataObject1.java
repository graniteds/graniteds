package org.granite.test.amf;

import java.io.Serializable;
import java.util.Date;

import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;

@ExternalizedBean(type=DefaultExternalizer.class)
public class DataObject1 implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int property1;
	private String property2;
	private boolean property3;
	private String property4;
	private double property5;
	private Date property6;
	private String property7;
	private String property8;
	private String property9;
	private String property10;
	private DataObject2 object;
	
	public int getProperty1() {
		return property1;
	}
	public void setProperty1(int property1) {
		this.property1 = property1;
	}
	public String getProperty2() {
		return property2;
	}
	public void setProperty2(String property2) {
		this.property2 = property2;
	}
	public boolean isProperty3() {
		return property3;
	}
	public void setProperty3(boolean property3) {
		this.property3 = property3;
	}
	public String getProperty4() {
		return property4;
	}
	public void setProperty4(String property4) {
		this.property4 = property4;
	}
	public double getProperty5() {
		return property5;
	}
	public void setProperty5(double property5) {
		this.property5 = property5;
	}
	public Date getProperty6() {
		return property6;
	}
	public void setProperty6(Date property6) {
		this.property6 = property6;
	}
	public String getProperty7() {
		return property7;
	}
	public void setProperty7(String property7) {
		this.property7 = property7;
	}
	public String getProperty8() {
		return property8;
	}
	public void setProperty8(String property8) {
		this.property8 = property8;
	}
	public String getProperty9() {
		return property9;
	}
	public void setProperty9(String property9) {
		this.property9 = property9;
	}
	public String getProperty10() {
		return property10;
	}
	public void setProperty10(String property10) {
		this.property10 = property10;
	}
	public DataObject2 getObject() {
		return object;
	}
	public void setObject(DataObject2 object) {
		this.object = object;
	}

//	@Override
//	public int hashCode() {
//		int h = property1;
//		
//		h = hash(property2) + (31 * h);
//		h = (property3 ? 1 : 0) + (31 * h);
//		h = hash(property4) + (31 * h);
//		h = hash(property5) + (31 * h);
//		h = hash(property6) + (31 * h);
//		h = hash(property7) + (31 * h);
//		h = hash(property8) + (31 * h);
//		h = hash(property9) + (31 * h);
//		h = hash(property10) + (31 * h);
//		h = System.identityHashCode(object) + (31 * h);
//		
//		return h;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (obj == this)
//			return true;
//		if (!(obj instanceof DataObject1))
//			return false;
//		
//		DataObject1 do1 = (DataObject1)obj;
//		return (
//			property1 == do1.property1 &&
//			eq(property2, do1.property2) &&
//			property3 == do1.property3 &&
//			eq(property4, do1.property4) &&
//			eq(property5, do1.property5) &&
//			eq(property6, do1.property6) &&
//			eq(property7, do1.property7) &&
//			eq(property8, do1.property8) &&
//			eq(property9, do1.property9) &&
//			eq(property10, do1.property10) &&
//			eq(object, do1.object)
//		);
//	}
//	
//	private static int hash(String s) {
//		return (s == null ? 0 : s.hashCode());
//	}
//	
//	private static int hash(Date d) {
//		return (d == null ? 0 : d.hashCode());
//	}
//	
//	private static int hash(double d) {
//		return Double.valueOf(d).hashCode();
//	}
//	
//	private static boolean eq(String s1, String s2) {
//		return (s1 == null ? s2 == null : s1.equals(s2));
//	}
//	
//	private static boolean eq(double d1, double d2) {
//		return (Double.isNaN(d1) ? Double.isNaN(d2) : d1 == d2);
//	}
//	
//	private static boolean eq(Date d1, Date d2) {
//		return (d1 == null ? d2 == null : d1.equals(d2));
//	}
//	
//	private static boolean eq(DataObject2 d1, DataObject2 d2) {
//		return (d1 == null ? d2 == null : d1.equals(d2));
//	}
}
