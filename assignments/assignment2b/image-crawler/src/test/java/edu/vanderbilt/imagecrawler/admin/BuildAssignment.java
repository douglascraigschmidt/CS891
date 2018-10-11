package edu.vanderbilt.imagecrawler.admin;

import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.helpers.AssignmentBuilder;
import edu.vanderbilt.imagecrawler.helpers.DefaultController;

/**
 * @@Doug: there is no point in changing the crawler type between assigments
 * because it doesn't really matter which crawler we use to build the ground-truth
 * and local web-pages directories; since this is the solution project, all crawlers
 * should work equal well and produce the exact same output.
 *
 * The only reason to use a different assignment builder for a specific assignment
 * project is if you decide that not to include both the null and gray scale filters
 * in the ground-truth directory (say you just wanted and assignment to use one filter
 * instead). I don't see that you would need to do this so the default is just to use
 * both filters and to use the FORK_JOIN_1 to create the assignment directories.
 */
public class BuildAssignment {
    /**
     * Downloads default images from the web using a all filters into
     * a resources ground-truth directory that can be used by JUnit
     * test to check the results of assignments.
     */
    @Test
    public void build() throws Exception {
        // Use the same controller that the students will use (web version).
        AssignmentBuilder.buildAssignment(
                DefaultController.build(false),
                // No need to change this between assignments!
                ImageCrawler.Type.FORK_JOIN1);
    }
}
