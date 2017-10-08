package edu.vanderbilt.imagecrawler.admin;

import org.junit.Test;

import static edu.vanderbilt.imagecrawler.admin.AssignmentBuilder.buildAssignment;
import static edu.vanderbilt.imagecrawler.helpers.AdminHelpers.info;
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
        info("Building Assignment 3a controller ...");
        buildAssignment(buildAssignment3aController(false));
    }
}

