package project.nlp.sentimentextract.rule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuleManager {
	
	private static final Logger logger = LogManager.getLogger(RuleManager.class);
	private ArrayList<String> aspectList = new ArrayList<String>();
	private ArrayList<String> ruleList = new ArrayList<String>();
	
	public RuleManager(String configFile){
		
		try{
			Properties prop = new Properties();
			prop.load(this.getClass().getClassLoader().getResourceAsStream(configFile));
			
			for(Enumeration<String> e = (Enumeration<String>) prop.propertyNames();e.hasMoreElements();){
				String propName = e.nextElement();
				if("aspect".equals(propName)){
					String aspects[] = prop.getProperty("aspect").split(",");
					aspectList.addAll(Arrays.asList(aspects));
				}else{
					ruleList.add(prop.getProperty(propName));
				}
			}
			
		}catch(Exception e){
			logger.error("Can't extract configFile:" + configFile,e);
		}
		
	}
	public ArrayList<String> getRuleList() {
		return ruleList;
	}
	public String parseRule(String rule,String aspectToken){
		return rule.replaceAll("_ASPECTS_", aspectToken);
	}
	public ArrayList<String> getAspectList() {
		return aspectList;
	}
}
