package edu.cyberconnector.search;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import edu.cyberconnector.utils.SysDir;


public class GranulesTool {
    public static void appendToLog(File log, String text) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(log, true));
            out.write(text);
            out.close();
        }
        catch (IOException e) {
            System.out.println("IOException occurred" + e);
        }

    }


    public static void indexCollectionGranules(GranulesRequest request) {
        File log = new File(SysDir.thredds_harvester_path + "/cc-index_collection_granules.log");

        String indexerPath = SysDir.thredds_harvester_path + "/index_collection_granules.py";

        ProcessBuilder indexerPB = new ProcessBuilder("python3", indexerPath, request.collection_url, request.collection_name);
        appendToLog(log, indexerPB.command().toString() + "\n");

        indexerPB.directory(new File(SysDir.thredds_harvester_path));

        indexerPB.redirectErrorStream(true);
        indexerPB.redirectOutput(ProcessBuilder.Redirect.appendTo(log));


        try {
            Process p = indexerPB.start();
            p.waitFor(180, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Granule> getCollectionGranules(GranulesRequest request) {
        File log = new File(SysDir.thredds_harvester_path + "/cc-get_collection_granules.log");

        String indexReaderPath = SysDir.thredds_harvester_path + "/get_collection_granules.py";

        ProcessBuilder readerPB = new ProcessBuilder("python3", indexReaderPath, request.collection_url, request.time_start, request.time_end);
        appendToLog(log, readerPB.command().toString() + "\n");

        readerPB.directory(new File(SysDir.thredds_harvester_path));


        String jsonOutput;
        List<Granule> granules = null;

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Process p = readerPB.start();
            jsonOutput = IOUtils.toString(p.getInputStream());
            granules = objectMapper.readValue(jsonOutput, new TypeReference<List<Granule>>(){});
        } catch (Exception e) {
            e.printStackTrace();
        }


        return granules;
    }
}
