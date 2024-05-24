import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MetrologyCSVReader {
    public static HashMap<String, HashMap<String, List<Double>>> csvToHashMap(String filename) {
        HashMap<String, HashMap<String, List<Double>>> MsrmtDict = new HashMap<String, HashMap<String, List<Double>>>();
        try {
            Reader reader = Files.newBufferedReader(Paths.get(filename));
            //read the file
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("msrmnt_waferId", "msrmnt_siteId", "msrmnt_xcoord", "msrmnt_ycoord"
                    , "msrmnt_type", "msrmnt_val").parse(reader);
            //
            int CountRow = 0;
            List<String> MsrmtNames = new ArrayList<>();
            MsrmtDict = new HashMap<>();
            int WaferIdIndex = 0;
            int SiteIdIndex = 0;
            int XCoordIndex = 0;
            int YCoordIndex = 0;
            int MeasurementNameIndex = 0;
            int MeasurementValueIndex = 0;
            //
            for (CSVRecord record : records) {
                int RecordLength = record.size();
                HashMap<String, List<Double>> TempMsrmtDict = new HashMap<>();
                List<Integer> TempSites = new ArrayList<>();
                List<Double> TempXCoords = new ArrayList<>();
                List<Double> TempYCoords = new ArrayList<>();
                List<Double> TempMeasurementValues = new ArrayList<>();
                if (CountRow == 0) {
                    for (int r = 0; r < RecordLength; r++) {
                        switch (record.get(r)) {
                            case "msrmnt_waferId":
                                WaferIdIndex = r;
                                break;
                            case "msrmnt_siteId":
                                SiteIdIndex = r;
                                break;
                            case "msrmnt_xcoord":
                                XCoordIndex = r;
                                break;
                            case "msrmnt_ycoord":
                                YCoordIndex = r;
                                break;
                            case "msrmnt_type":
                                MeasurementNameIndex = r;
                                break;
                            case "msrmnt_val":
                                MeasurementValueIndex = r;
                                break;
                        }
                    }
                    CountRow++;
                } else if (CountRow == 1) {
                    List<String> TempMeasurementNames = new ArrayList<>(MsrmtNames);
                    int TempSite = Integer.parseInt(record.get(SiteIdIndex));
                    Double TempXCoord = Double.valueOf(record.get(XCoordIndex));
                    Double TempYCoord = Double.valueOf(record.get(YCoordIndex));
                    String TempMeasurementName = record.get(MeasurementNameIndex);
                    Double TempMeasurementValue = Double.valueOf(record.get(MeasurementValueIndex));
                    //
                    TempSites.add(TempSite);
                    TempXCoords.add(TempXCoord);
                    TempYCoords.add(TempYCoord);
                    TempMeasurementNames.add(TempMeasurementName);
                    TempMeasurementValues.add(TempMeasurementValue);
                    //
                    Set<String> TempMsrValSet = new HashSet<String>(TempMeasurementNames);
                    MsrmtNames = new ArrayList<>(TempMsrValSet);
                    //
//                    TempMsrmtDict.put("SITES", TempSites);
                    TempMsrmtDict.put("X", TempXCoords);
                    TempMsrmtDict.put("Y", TempYCoords);
                    TempMsrmtDict.put("Z", TempMeasurementValues);
                    MsrmtDict.put(record.get(MeasurementNameIndex), TempMsrmtDict);
                    CountRow++;
                } else if (MsrmtNames.contains(record.get(MeasurementNameIndex))) {
                    int TempSite = Integer.parseInt(record.get(SiteIdIndex));
                    Double TempXCoord = Double.valueOf(record.get(XCoordIndex));
                    Double TempYCoord = Double.valueOf(record.get(YCoordIndex));
                    String TempMeasurementName = record.get(MeasurementNameIndex);
                    Double TempMeasurementValue = Double.valueOf(record.get(MeasurementValueIndex));
                    //
//                    MsrmtDict.get(TempMeasurementName).get("SITES").add(TempSite);
                    MsrmtDict.get(TempMeasurementName).get("X").add(TempXCoord);
                    MsrmtDict.get(TempMeasurementName).get("Y").add(TempYCoord);
                    MsrmtDict.get(TempMeasurementName).get("Z").add(TempMeasurementValue);
                    //
                    CountRow++;
                } else if (!MsrmtNames.contains(record.get(MeasurementNameIndex))) {
                    List<String> TempMeasurementNames = new ArrayList<>(MsrmtNames);
                    int TempSite = Integer.parseInt(record.get(SiteIdIndex));
                    Double TempXCoord = Double.valueOf(record.get(XCoordIndex));
                    Double TempYCoord = Double.valueOf(record.get(YCoordIndex));
                    String TempMeasurementName = record.get(MeasurementNameIndex);
                    Double TempMeasurementValue = Double.valueOf(record.get(MeasurementValueIndex));
                    //
                    TempSites.add(TempSite);
                    TempXCoords.add(TempXCoord);
                    TempYCoords.add(TempYCoord);
                    TempMeasurementNames.add(TempMeasurementName);
                    TempMeasurementValues.add(TempMeasurementValue);
                    //
                    Set<String> TempMsrValSet = new HashSet<String>(TempMeasurementNames);
                    MsrmtNames = new ArrayList<>(TempMsrValSet);
//                    TempMsrmtDict.put("SITES", TempSites);
                    TempMsrmtDict.put("X", TempXCoords);
                    TempMsrmtDict.put("Y", TempYCoords);
                    TempMsrmtDict.put("Z", TempMeasurementValues);
                    MsrmtDict.put(record.get(MeasurementNameIndex), TempMsrmtDict);
                    CountRow++;
                }
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return MsrmtDict;
    }
}
