package project.nlp.sentimentextract;

import org.junit.*;
import static org.junit.Assert.*;

public class CoreferenceParserTest {

	@Test
	public void testParseContent(){
		assertEquals(" The MOVIE_PLOT is about wars . The MOVIE_PLOT is a sad story .", CoreferenceParser.parseContent("The MOVIE_PLOT is about wars. It is a sad story."));
		assertEquals(" The MOVIE_PLOT is good . I love The MOVIE_PLOT .", CoreferenceParser.parseContent("The MOVIE_PLOT is good. I love it."));
	}
}
