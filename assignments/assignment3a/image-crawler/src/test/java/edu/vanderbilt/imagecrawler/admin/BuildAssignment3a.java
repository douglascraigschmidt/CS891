package edu.vanderbilt.imagecrawler.admin;

import org.junit.Test;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;

import static edu.vanderbilt.imagecrawler.admin.AssignmentBuilder.buildAssignment;
import static edu.vanderbilt.imagecrawler.helpers.Controllers.buildAssignment3aController;

public class BuildAssignment3a {
    /**
     * Downloads default images from the web using a all filters into
     * a resources ground-truth directory that can be used by JUnit
     * test to check the results of assignments.
     */
    @Test
    public void buildAssignment3a() throws Exception {
        // Use the same controller that the students will use (web version).
        buildAssignment(
                buildAssignment3aController(false),
                ImageCrawler.Type.SEQUENTIAL_LOOPS);
    }
}

