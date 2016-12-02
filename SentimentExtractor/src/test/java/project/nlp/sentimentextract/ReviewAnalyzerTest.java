package project.nlp.sentimentextract;

import org.junit.*;

import project.nlp.sentimentextract.rule.RuleManager;

import static org.junit.Assert.*;

import java.util.ArrayList;

public class ReviewAnalyzerTest {
	private RuleManager ruleManager;
	//private ReviewAnalyzer reviewAnalyzer;
	
	@Before
	public void before(){
		ruleManager = new RuleManager("extract_rules.test.properties");
	}
	
	@Test
	public void testMappingAspectWithAdj(){
		ReviewAnalyzer reviewAnalyzer = new ReviewAnalyzer(1,"MOVIE_CAST is very good.", ruleManager);
		ArrayList<AspectSentimentTuple> tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(1, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "very good")));
		
		reviewAnalyzer = new ReviewAnalyzer(1,"A really good MOVIE_CAST.", ruleManager);
		tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(1, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "really good")));
	}
	
	@Test
	public void testMappingAspectWithNoun(){
		ReviewAnalyzer reviewAnalyzer = new ReviewAnalyzer(1,"MOVIE_CAST is a very good person.", ruleManager);
		ArrayList<AspectSentimentTuple> tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(1, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "very good person")));
		
		reviewAnalyzer = new ReviewAnalyzer(1,"A really good person is MOVIE_CAST.", ruleManager);
		tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(1, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "really good person")));
	}
	
	@Test
	public void testConjClause(){
		//but clause between adj <-> adj
		ReviewAnalyzer reviewAnalyzer = new ReviewAnalyzer(1,"MOVIE_CAST is very good but the MOVIE_STORY is extremely short.", ruleManager);
		ArrayList<AspectSentimentTuple> tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(2, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "very good","but","extremely short")));
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_STORY", "extremely short","but","very good")));
		
		//but clause between adj <-> noun
		reviewAnalyzer = new ReviewAnalyzer(1,"MOVIE_CAST is a very good person but the MOVIE_STORY is extremely short.", ruleManager);
		tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(2, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "very good person","but","extremely short")));
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_STORY", "extremely short","but","very good person")));
		
		//and clause between adj <-> noun
		reviewAnalyzer = new ReviewAnalyzer(1,"MOVIE_CAST is a very good person and the MOVIE_STORY is extremely short.", ruleManager);
		tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(2, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "very good person","and","extremely short")));
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_STORY", "extremely short","and","very good person")));
		
		//and clause between noun <-> noun
		reviewAnalyzer = new ReviewAnalyzer(1,"MOVIE_CAST is a very good person and the MOVIE_STORY is an extremely short film.", ruleManager);
		tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(2, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "very good person","and","extremely short film")));
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_STORY", "extremely short film","and","very good person")));
	}
	
	@Test
	public void testEmbalishAdj(){
		ReviewAnalyzer reviewAnalyzer = new ReviewAnalyzer(1,"A not really very good MOVIE_CAST", ruleManager);
		ArrayList<AspectSentimentTuple> tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(1, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "not really very good")));
	}
	
	@Test
	public void testEmbalishNoun(){
		ReviewAnalyzer reviewAnalyzer = new ReviewAnalyzer(1,"MOVIE_CAST is not a very good person", ruleManager);
		ArrayList<AspectSentimentTuple> tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(1, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "not very good person")));
	}
	
	@Test
	public void testPrintPOS(){
		ReviewAnalyzer reviewAnalyzer = new ReviewAnalyzer(1,"A not really very good MOVIE_CAST", ruleManager);
		reviewAnalyzer.setPrintPOS(true);
		ArrayList<AspectSentimentTuple> tupleList = reviewAnalyzer.extractAspectSentimentExpression();
		assertEquals(1, tupleList.size());
		assertTrue(tupleList.contains(new AspectSentimentTuple(1,"MOVIE_CAST", "not/RB really/RB very/RB good/JJ")));
	}
	
	
}
