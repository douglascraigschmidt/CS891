package edu.vanderbilt.crawler.admin;

import org.junit.Test;

import static edu.vanderbilt.crawler.admin.AssignmentBuilder.buildAndroidAssignment;
import static edu.vanderbilt.crawler.helpers.AdminHelpers.info;
import static edu.vanderbilt.crawler.helpers.AndroidControllers.buildAndroidAssignment3aController;

public class BuildAssignment3a {
    /**
     * Downloads default images from the web using a all filters into
     * a resources ground-truth directory that can be used by JUnit
     * test to check the results of assignments.
     */
    @Test
    public void buildAssignment3a() throws Exception {
        // Use the same controller that the students will use (web version).
        info("Building Android assignment 3a controller ...");
        buildAndroidAssignment(buildAndroidAssignment3aController(false));
    }
}

