package edu.vanderbilt.imagecrawler.utils;

/**
 * This class provides a static command line argument crawler that
 * builds an immutable options object.
 */
public class CommandLineOptions {
    /**
     * Just a static utility class.
     */
    private CommandLineOptions() {
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public static Options parseArgs(String argv[]) {
        Options.Builder builder = Options.newBuilder();

        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc++)
                switch (argv[argc]) {
                    case "-d":
                        builder.diagnosticsEnabled(argv[++argc].equals("true"));
                        break;
                    case "-m":
                        builder.maxDepth(Integer.valueOf(argv[++argc]));
                        break;
                    case "-u":
                        builder.rootUrl(argv[++argc]);
                        break;
                    case "-w":
                        break;
                    case "-o":
                        builder.downloadPath(argv[++argc]);
                        break;
                    case "-h":
                    default:
                        printUsage();
                        return null;
                }
        }

        return builder.build();
    }

    /**
     * Print out usage and default values.
     */
    private static void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-d [true|false]");
        System.out.println("-m [maxDepth]");
        System.out.println("-u [rootUrl] (can be \"http://....\" " +
                                   "or \"file://android_assets/...\" " +
                                   "or \"file://java_resources/...\"");
        System.out.println("-o [downloadPath]");
    }
}
