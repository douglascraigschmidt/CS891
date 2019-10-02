package admin;

import org.junit.Ignore;
import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.CrawlerType;

/**
 * Downloads default images from the web using a all filters into
 * a resources ground-truth directory that can be used by JUnit
 * test to check the results of assignments.
 */
@Ignore
public class BuildAssignment {
    @Test
    public void build() throws Exception {
        // Use the same controller that the students will use (web version).
        AssignmentBuilder.buildAssignment(
                BuildController.build(false),
                // This crawler is available in all assignments
                CrawlerType.SEQUENTIAL_LOOPS);
    }
}
