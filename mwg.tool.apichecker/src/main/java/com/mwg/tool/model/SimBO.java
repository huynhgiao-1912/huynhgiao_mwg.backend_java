package com.mwg.tool.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimBO implements DiffComparable{
	@JsonAlias({"storeIDListField"})
	public String StoreIDList;
	
	@JsonAlias({"simGroupIDListField"})
	public String SimGroupIDList;
	
	@JsonAlias({"priceField"})
	public double Price;
	
	@JsonAlias({"accountValueField"})
	public int AccountValue;
	
	@JsonAlias({"sIMNODisplayField"})
	public String SimNoDisplay;
	
	@JsonAlias({"elementSumField"})
	public int ElementSum;
	
	@JsonAlias({"discountField"})
	public int Discount;
	
	@JsonAlias({"promotionAccountField"})
	transient public int PromotionAccount;
	
	@JsonAlias({"promotionTextField"})
	transient public String PromotionText;
	
	@JsonAlias({"taxRateField"})
	public int TaxRate;
	
	transient public boolean IsWebShow;
	
	@JsonAlias({"sIMNetworkIDField"})
	public int SimNetworkID;
	
	@JsonAlias({"sIMServiceIDField"})
	public int SimServiceID;
	
	@JsonAlias({"statusField"})
	public int Status;
	
	@JsonAlias({"showPriceField"})
	public int ShowPrice;
	
	
	transient public boolean IsNew;
	
	
	transient public boolean IsHot;
	
	
	transient public boolean IsDeleted;
	
	@JsonAlias({"deletedDateField"})
	transient public Date DeletedDate;
	@JsonAlias({"deletedUserField"})
	transient public String DeletedUser;
	@JsonAlias({"createdDateField"})
	transient public Date CreatedDate;
	
	@JsonAlias({"createdUserField"})
	public String CreatedUser;
	
	@JsonAlias({"updatedDateField"})
	transient public Date UpdatedDate;
	@JsonAlias({"updatedUserField"})
	transient public String UpdatedUser;
	
	@JsonAlias({"sIMNOField"})
	public String SimNo;
	
	@JsonAlias({"productNOField"})
	public String ProductNo;
	
	public String Logo;
	public String SIMNetworkName;
	public int SubGroupID;
	public String SubGroupName;
	
	@JsonAlias({"simGroupNameField"})
	public String SimGroupName;
	
	@JsonAlias({"productNameField"})
	public String ProductName;
	
	@JsonAlias({"mainGoupIDField"})
	transient public int MainGoupID;
	
	@JsonAlias({"brandIDField"})
	public int BrandID;
	
	@JsonAlias({"isWebShowField"})
	public void setIsWebShow(String isWebShow) {
		this.IsWebShow = isWebShow.equals("1") || isWebShow.equals("true") ;
	}
	
	@JsonAlias({"isHotField"})
	public void setIsHot(String IsHot) {
		this.IsHot = IsHot.equals("1") || IsHot.equals("true") ;
	}
	
	@JsonAlias({"isNewField"})
	public void setIsNew(String IsNew) {
		this.IsNew = IsNew.equals("1") || IsNew.equals("true") ;
	}
	@JsonAlias({"isDeletedField"})
	public void setIsDeleted(String IsDeleted) {
		this.IsDeleted = IsDeleted.equals("1") || IsDeleted.equals("true") ;
	}
	
	
	
	@Override
	public String diff(Object object) throws IllegalArgumentException, IllegalAccessException {
		String className = this.getClass().getSimpleName();
		if (object == this) return "";
		if (!(object instanceof SimBO)) return className;
		final SimBO other = (SimBO) object;
		Field[] fields = this.getClass().getDeclaredFields();
		String result = Arrays.stream(fields)
//			.parallel()
			.filter(field ->  !Modifier.isTransient(field.getModifiers()))
			.filter(field -> {
				try {
					Object ob1 = field.get(this);
					Object ob2 = field.get(other);
					return ob1 == null ? ob2 != null : !ob1.equals(ob2);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					return false;
				}
			})
			.map(field -> {
				try {
					Object ob1 = field.get(this);
					Object ob2 = field.get(other);
					
					if(Date.class.equals(field.getType())  && ob2 != null && ob1 != null) {
						long l1 = ((Date)ob1).toInstant().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS).toEpochSecond();
						long l2 = ((Date)ob2).toInstant().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS).toEpochSecond();
						return l1 == l2 ? null : field.getName();
					}
					
					if(LocalDateTime.class.equals(field.getType())  && ob2 != null && ob1 != null) {
						long l1 = ((LocalDateTime)ob1).toEpochSecond(ZoneOffset.UTC);
						long l2 = ((LocalDateTime)ob2).toEpochSecond(ZoneOffset.UTC);
						return l1 == l2 ? null : field.getName();
					}
					
					if(ob1 instanceof DiffComparable && ob2 != null && ob1 != null)
					{
						return ((DiffComparable) ob1).diff(ob2);
					}
				
					return field.getName();
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					return null;
				}
			 })
			.filter(fieldname -> !Objects.isNull(fieldname) && !fieldname.isBlank())
			.map(fieldname ->  className + "." + fieldname + " | ")
			.reduce("", String::concat);
		
		return result;
		
	}
	
}
