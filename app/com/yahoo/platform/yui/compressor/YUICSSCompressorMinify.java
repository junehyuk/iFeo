package com.yahoo.platform.yui.compressor;

import jargs.gnu.CmdLineParser;
import java.io.*;
import java.nio.charset.Charset;

public class YUICSSCompressorMinify {
    public static void start(String args[]) {
        CmdLineParser parser = new CmdLineParser();

        CmdLineParser.Option verboseOpt = parser.addBooleanOption('v', "verbose");
        CmdLineParser.Option linebreakOpt = parser.addStringOption("line-break");
        CmdLineParser.Option charsetOpt = parser.addStringOption("charset");
        CmdLineParser.Option outputFilenameOpt = parser.addStringOption('o', "output");

        Reader in = null;
        Writer out = null;

        try {

            parser.parse(args);

            boolean verbose = parser.getOptionValue(verboseOpt) != null;

            String charset = (String) parser.getOptionValue(charsetOpt);
            if (charset == null || !Charset.isSupported(charset)) {
                charset = "UTF-8";

                if (verbose) {
                    System.err.println("\n[INFO] Using charset " + charset);
                }
            }

            int linebreakpos = -1;
            String linebreakstr = (String) parser.getOptionValue(linebreakOpt);
            if (linebreakstr != null) {
                try {
                    linebreakpos = Integer.parseInt(linebreakstr, 10);
                } catch (NumberFormatException e) {
                    usage();
                    System.out.println("linebreakstr");
                }
            }

            String type = "css";

            String[] fileArgs = parser.getRemainingArgs();
            java.util.List files = java.util.Arrays.asList(fileArgs);
            if (files.isEmpty()) {
                if (type == null) {
                    usage();
                    System.out.println("fileArgs");
                }
                files = new java.util.ArrayList();
                files.add("-"); // read from stdin
            }

            int i = 0;
            String output = (String) parser.getOptionValue(outputFilenameOpt);
            String pattern[] = output != null ? output.split(":") : new String[0];

            java.util.Iterator filenames = files.iterator();
            while(filenames.hasNext()) {
                String inputFilename = (String)filenames.next();

                try {
                    if (inputFilename.equals("-")) {

                        in = new InputStreamReader(System.in, charset);

                    } else {

                        if (type == null) {
                            int idx = inputFilename.lastIndexOf('.');
                            if (idx >= 0 && idx < inputFilename.length() - 1) {
                                type = inputFilename.substring(idx + 1);
                            }
                        }

                        if (type == null || !type.equalsIgnoreCase("js") && !type.equalsIgnoreCase("css")) {
                            usage();
                            System.out.println(inputFilename + "is not a css file and excluded");
                        }

                        in = new InputStreamReader(new FileInputStream(inputFilename), charset);
                    }

                    String outputFilename = output;
                    // if a substitution pattern was passed in
                    if (pattern.length > 1 && files.size() > 1) {
                        outputFilename = inputFilename.replaceFirst(pattern[0], pattern[1]);
                    }
                    if (type.equalsIgnoreCase("css")) {

                        CssCompressorMinify compressorFEO = new CssCompressorMinify(in);

                        // Close the input stream first, and then open the output stream,
                        // in case the output file should override the input file.
                        in.close(); in = null;

                        if (outputFilename == null) {
                            out = new OutputStreamWriter(System.out, charset);
                        } else {
                            out = new OutputStreamWriter(new FileOutputStream(outputFilename, true), charset);
                        }

                        compressorFEO.compress(out, linebreakpos);
                    }

                } catch (IOException e) {

                    e.printStackTrace();

                } finally {

                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (CmdLineParser.OptionException e) {
            usage();
        }

    }

    private static void usage() {
        System.err.println(
                "\nno js file compressor\nUsage: java -jar yuicompressor-x.y.z.jar [options] [input file]\n\n"

                        + "Global Options\n"
                        + "  -h, --help                Displays this information\n"
                        + "  --type <js|css>           Specifies the type of the input file\n"
                        + "  --charset <charset>       Read the input file using <charset>\n"
                        + "  --line-break <column>     Insert a line break after the specified column number\n"
                        + "  -v, --verbose             Display informational messages and warnings\n"
                        + "  -o <file>                 Place the output into <file>. Defaults to stdout.\n"
                        + "                            Multiple files can be processed using the following syntax:\n"
                        + "                            java -jar yuicompressor.jar -o '.css$:-min.css' *.css\n"
                        + "                            java -jar yuicompressor.jar -o '.js$:-min.js' *.js\n\n"

                        + "If no input file is specified, it defaults to stdin. In this case, the 'type'\n"
                        + "option is required. Otherwise, the 'type' option is required only if the input\n"
                        + "file extension is neither 'js' nor 'css'.");
    }
}