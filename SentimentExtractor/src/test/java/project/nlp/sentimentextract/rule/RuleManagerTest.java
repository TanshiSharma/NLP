package project.nlp.sentimentextract.rule;

import org.junit.*;
import static org.junit.Assert.*;

public class RuleManagerTest {
	
	private RuleManager ruleManager;
	
	@Before
	public void before(){
		ruleManager = new RuleManager("extract_rules.test.properties");
	}
	
	@Test
	public void testParseRule(){
		assertEquals("{tag:/JJ.*/} > {word:MOVIE_PLOT}", ruleManager.parseRule("{tag:/JJ.*/} > {word:_ASPECTS_}", "MOVIE_PLOT"));
	}
}
