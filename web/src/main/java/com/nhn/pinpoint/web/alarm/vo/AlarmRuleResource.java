package com.nhn.pinpoint.web.alarm.vo;

import org.apache.ibatis.type.Alias;

import com.nhn.pinpoint.web.alarm.MainCategory;
import com.nhn.pinpoint.web.alarm.SubCategory;

/**
 * 
 * @author koo.taejin
 */
@Alias("alarmRule")
public class AlarmRuleResource {

	private Integer id;
	private Integer alarmRuleGroupId;
	private Integer alarmRuleSubCategoryId;
	private Integer thresholdRule;
	private boolean compareRule;
	private Integer continuosTime;
	private String alarmRuleName;
	private String alarmRuleDescrption;
	
//	private MainCategory mainCategory;
//	private SubCategory subCategory;

	private String mainCategory;
	private String subCategory;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAlarmRuleGroupId() {
		return alarmRuleGroupId;
	}

	public void setAlarmRuleGroupId(Integer alarmRuleGroupId) {
		this.alarmRuleGroupId = alarmRuleGroupId;
	}

	public Integer getAlarmRuleSubCategoryId() {
		return alarmRuleSubCategoryId;
	}

	public void setAlarmRuleSubCategoryId(Integer alarmRuleSubCategoryId) {
		this.alarmRuleSubCategoryId = alarmRuleSubCategoryId;
	}

	public Integer getThresholdRule() {
		return thresholdRule;
	}

	public void setThresholdRule(Integer thresholdRule) {
		this.thresholdRule = thresholdRule;
	}

	public boolean isCompareRule() {
		return compareRule;
	}

	public void setCompareRule(boolean compareRule) {
		this.compareRule = compareRule;
	}

	public Integer getContinuosTime() {
		return continuosTime;
	}

	public void setContinuosTime(Integer continuosTime) {
		this.continuosTime = continuosTime;
	}

	public String getAlarmRuleName() {
		return alarmRuleName;
	}

	public void setAlarmRuleName(String alarmRuleName) {
		this.alarmRuleName = alarmRuleName;
	}

	public String getAlarmRuleDescrption() {
		return alarmRuleDescrption;
	}

	public void setAlarmRuleDescrption(String alarmRuleDescrption) {
		this.alarmRuleDescrption = alarmRuleDescrption;
	}


	public MainCategory getMainCategory() {
		return MainCategory.getValue(mainCategory);
	}

	public void setMainCategory(String mainCategory) {
		this.mainCategory = mainCategory;
	}

	public SubCategory getSubCategory() {
		return SubCategory.getValue(subCategory);
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public String ruleToString() {
		StringBuilder rule = new StringBuilder();
		
		rule.append(getMainCategory().getName() + " ");
		rule.append(getSubCategory().getName() + " ");
		rule.append(">=" + thresholdRule + getSubCategory().getUnit() + " ");
		rule.append("(" + getContinuosTime() + "ms)");
		
		return rule.toString();
	}

	@Override
	public String toString() {
		return "AlarmRuleResource [id=" + id + ", alarmRuleGroupId=" + alarmRuleGroupId + ", alarmRuleSubCategoryId=" + alarmRuleSubCategoryId
				+ ", thresholdRule=" + thresholdRule + ", compareRule=" + compareRule + ", continuosTime=" + continuosTime + ", alarmRuleName=" + alarmRuleName
				+ ", alarmRuleDescrption=" + alarmRuleDescrption + ", mainCategory=" + mainCategory + ", subCategory=" + subCategory + "]";
	}
	
	
//	public String getMainCategory() {
//		return mainCategory;
//	}
//
//	public void setMainCategory(String mainCategory) {
//		this.mainCategory = mainCategory;
//	}
//
//	public String getSubCategory() {
//		return subCategory;
//	}
//
//	public void setSubCategory(String subCategory) {
//		this.subCategory = subCategory;
//	}
	

}
