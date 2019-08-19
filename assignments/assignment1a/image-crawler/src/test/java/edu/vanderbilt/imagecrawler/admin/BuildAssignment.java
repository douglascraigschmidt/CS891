package edu.vanderbilt.imagecrawler.admin;

import org.junit.Ignore;
import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AssignmentBuilder;
import edu.vanderbilt.imagecrawler.helpers.BuildController;

@Ignore
public class BuildAssignment {
    /** Just use any method to create the ground-truth and web-pages directories. */
    private static ImageCrawler.Type buildCrawlerType = ImageCrawler.Type.SEQUENTIAL_LOOPS;

    /**
     * Downloads default images from the web using a all filters into
     * a resources ground-truth directory that can be used by JUnit
     * test to check the results of assignments.
     */
    @Test
    public void build() throws Exception {
        // Use the same controller that the students will use (web version).
        AssignmentBuilder.buildAssignment(
                BuildController.build(false),
                buildCrawlerType);
    }
}
