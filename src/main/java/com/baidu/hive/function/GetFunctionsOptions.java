package com.baidu.hive.function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetFunctionsOptions {

    protected static final Logger l4j =
            LoggerFactory.getLogger(GetFunctionsOptions.class.getName());

    private final Options options = new Options();
    private org.apache.commons.cli.CommandLine commandLine;

    public GetFunctionsOptions() {

        // -database database
        options.addOption(OptionBuilder
                                  .hasArg()
                                  .withArgName("p")
                                  .withLongOpt("path")
                                  .withDescription(
                                          "The path of sql files")
                                  .create());
        options.addOption(OptionBuilder
                                  .hasArg()
                                  .withArgName("s")
                                  .withLongOpt("suffix")
                                  .withDescription(
                                          "The suffix of sql files")
                                  .create());
    }

    public CommandLine processOptions(String[] argv) {
        try {
            return new GnuParser().parse(options, argv);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
