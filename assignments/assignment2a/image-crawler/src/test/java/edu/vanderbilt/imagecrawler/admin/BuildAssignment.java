package edu.vanderbilt.imagecrawler.admin;

import org.junit.Ignore;
import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AssignmentBuilder;
import edu.vanderbilt.imagecrawler.helpers.BuildController;

import static edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler.Type.FORK_JOIN;

@Ignore
public class BuildAssignment {
    /** Just use any method to create the ground-truth and web-pages directories. */
    private static ImageCrawler.Type buildCrawlerType = FORK_JOIN;

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
